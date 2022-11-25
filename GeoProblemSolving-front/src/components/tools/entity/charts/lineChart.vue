<style>
@import "../../../../../static/css/jquery.jexcel.css";
.labelTile {
  font-size: larger;
  font-weight: bold;
  margin: 5px 0;
}
</style>
<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <!-- coding for your tools // begin-->      
      <vue-scroll :ops="ops" style="height: calc(100vh - 40px)">
        <div style="width: 95%; padding:10px;margin-left:10px; float:left; margin-top:20px;">
          <Label class="labelTile">Type of chart:</Label>
          <Select
            v-model="chooseType"
            style="width:180px; margin-left: 7px;"
            :placeholder="chartTypePlaceholder"
            @on-change="chooseTypeChange"
          >
            <Option v-for="item in normalChart" :value="item.value" :key="item.value">{{ item.label }}</Option>
          </Select>
          <RadioGroup v-model="SelectAxis" @on-change="sendRadioParams(SelectAxis, 'axis') ">
            <Radio label="X-Axis" class="labelTile" style="margin-left:7px;"></Radio>
            <Input v-model="SelectX" style="width:180px; margin-left:7px;" readonly/>
            <Radio label="Y-Axis" class="labelTile" style="margin-left:7px;"></Radio>
            <Input v-model="SelectY" style="width:180px; margin-left:7px;" readonly/>
            <Radio
              v-model="smoothed"
              @click.native="sendSmoothedParams(smoothed, 'smoothed') "
              class="labelTile"
              style="margin-left:7px;"
            >Smooth</Radio>
          </RadioGroup>
          <Button @click="Visualize" style="margin-left:7px;">Visualization</Button>
          <Button v-if="visualization" @click="back2Table" style="margin-left:7px;">Select data</Button>
        </div>
        <div style=" float:left; width: 90%; margin: 20px 20px;">
          <div
            v-show="visualization"
            id="visualization"
            style="width:calc(75vw); height:calc(90vh);background-color:#f8f8f9; margin: 15px 10px; padding:2% 10%;"
          ></div>
          <div v-show="!visualization">
            <div id="mytable" style="height:calc(90vh);"></div>
          </div>
        </div>
      </vue-scroll>
    </div>
  </div>
</template>
<script>
import * as socketApi from "@/api/socket.js";
import csv from "@static/js/jquery.csv.min.js";
import jexcel from "@static/js/jquery.jexcel.js";
import XLSX from "xlsx";
import * as echarts from "echarts";
export default {
  data() {
    return {
       ops: {
        bar: {
          background: "#808695",
        },
      },
      // basic info
      activityInfo: {},
      toolId: "",
      participants: [],
      userInfo: {},
      resources: [],

      sidebarHeight: 0,
      visualization: false,
      testData: [],
      columnHeader: [],
      excelData: [],
      columnNameList: [],
      // 选中的参考值
      normalChart: [
        { value: "Line", label: "Individual line" },
        { value: "Stacked-line", label: "Stacked line" }
      ],
      chartTypePlaceholder: "Choose one type of charts",
      //data and params
      DataX: [],
      DataY: [],
      SelectX: "",
      SelectY: "",
      SelectAxis: "",
      chartData: [],
      chooseType: "",
      Charts: null,
      chartSettings: {},
      smoothed: false,
      // page info
      pageParams: { pageId: "", userId: "", userName: "" },
      userInfo: {},
      //协同消息
      socket_content: {},
      participants: [],
      olParticipants: [],
      resources: [],
      dataUrl: "",
      selectFile: {},
    };
  },
  beforeDestroy() {
    // this.socketApi.close();
  },
  created() {},
  mounted() {
    this.Charts = echarts.init(document.getElementById("visualization"));
    // 加载协同组件
    loadCollabComponent();
    this.init();
    this.getStepInfo();
  },
  methods: {
    getStepInfo() {
      if (componentStatus) {
        // 获取数据
        this.activityInfo = activityInfo;
        this.toolId = toolId;
        this.participants = onlineMembers;
        this.userInfo = userInfo;
        this.resources = resources;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
        loadResChannel = this.loadResources;
      } else {
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },
    init() {
      $("#app").css("min-width", "0");
      $("#app").css("min-height", "0");
      this.sidebarHeight = window.innerHeight + "px";
      $("#mytable").jexcel({
        data: this.testData,
        minDimensions: [20, 20],
        onselection: this.selectData,
        onchange: this.tableChange
      });
    },
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "visualize") {
        // this.chooseType = content.chartType;
        this.DataX = content.dataX;
        this.DataY = content.dataY;
        this.smoothed = content.smooth;
        this.showCharts();
      } else if (behavior == "select"){
        this.chooseType = content.value;
      } else if (behavior == "selectData"){
        if(this.SelectAxis == "X-Axis"){
          this.SelectX = content.value;
        } else if (this.SelectAxis == "Y-Axis"){
          this.SelectY = content.value;
        }
      } else if (behavior == "radio"){
        //编辑信息协同2 输入参数
        if(content.type == "smoothed"){
          this.smoothed = !this.smoothed;
        }
      } 
       else if (behavior == "params"){
        //编辑信息协同2 输入参数
        if(content.stateIndex == "axis"){
          this.SelectAxis = content.inputs;
        } else if (content.stateIndex == "SelectX"){
          this.SelectX =  content.inputs;
        } else if (content.stateIndex == "SelectY"){
          this.SelectY =  content.inputs;
        }
        
      } 
    },
    getSocketData(data) {
      // socket数据操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "select"){
        // console.log(content);
        // this.selectData = content;
        // console.log(this.selectData);
      } else if(behavior == "file") {
        this.selectFile = content.data;
        if (/\.(csv|xls|xlsx)$/.test(this.selectFile.suffix.toLowerCase())) {
          var that = this;
          var xhr = new XMLHttpRequest();
          xhr.open("GET", this.selectFile.address, true);
          xhr.responseType = "blob";
          xhr.onload = function(e) {
            if (this.status == 200) {
              var file = this.response;
              that.fillTable(file);
            }
          };
          xhr.send();
        } else {
          this.$Message.error("Worry data format!");
        }
      }
    },
    getSocketComputation(data) {
      
    },
    loadResources(resList) {
      let type = "";
      for (let i = 0; i < resList.length; i++) {
        // your function
        this.selectFile = resList[0];
        if (/\.(csv|xls|xlsx)$/.test(this.selectFile.suffix.toLowerCase())) {
          var that = this;
          var xhr = new XMLHttpRequest();
          xhr.open("GET", this.selectFile.address, true);
          xhr.responseType = "blob";
          xhr.onload = function(e) {
            if (this.status == 200) {
              var file = this.response;
              that.fillTable(file);
            }
          };
          xhr.send();
        } else {
          this.$Message.error("Worry data format!");
        }
      }
      let send_content = {
        type: "resource",
        sender: this.userInfo.userId,
        behavior: "file",
        content: {
          data: this.selectFile,
        }
      };
      sendCustomOperation(send_content);
    },
    fillTable(file) {
      var that = this;
      var fileReader = new FileReader();
      fileReader.readAsBinaryString(file);
      fileReader.onload = ev => {
        try {
          const data = ev.target.result;
          const workbook = XLSX.read(data, {
            type: "binary"
          });
          const wsname = workbook.SheetNames[0]; //取第一张表
          const ws = XLSX.utils.sheet_to_json(workbook.Sheets[wsname]);
          let arr = Object.keys(ws[0]);
          that.columnNameList = arr;
          //生成json表格内容
          let list1 = [];
          let list2 = [];
          for (var i = 0; i < ws.length; i++) {
            for (var key in ws[i]) {
              list1.push(ws[i][key]);
            }
            list2[i] = list1;
            list1 = [];
          }
          that.columnHeader = arr;
          that.excelData = list2;

          $("#mytable").jexcel({
            data: that.excelData,
            colHeaders: that.columnHeader,
            tableOverflow: false,
            minDimensions: [20, 20],
            csvHeaders: true,
            onselection: that.selectData,
            onchange: that.tableChange
          });
          that.$refs.upload.value = "";
        } catch (e) {
          return false;
        }
      };
      fileReader.onerror = function() {
        this.$Message.error("Input data error.");
        that.showFile = false;
        that.uploadGeoJson = null;
      };
    },
    selectData(obj, startCell, endCell) {
      let start = startCell[0].id;
      let end = endCell[0].id;
      let startXY = start.split("-");
      let endXY = end.split("-");
      if (this.SelectAxis == "") {
        return;
      }

      try {
        start =
          String.fromCharCode(parseInt(startXY[0]) + "A".charCodeAt()) +
          (parseInt(startXY[1]) + 1);
        end =
          String.fromCharCode(parseInt(endXY[0]) + "A".charCodeAt()) +
          (parseInt(endXY[1]) + 1);
      } catch (e) {}

      if (this.SelectAxis == "X-Axis") {
        this.socket_content["startX"] = startXY;
        this.socket_content["endX"] = endXY;
        this.SelectX = start + "->" + end;
        this.DataX = this.getData(startXY, endXY);
      } else if (this.SelectAxis == "Y-Axis") {
        this.socket_content["startY"] = startXY;
        this.socket_content["endY"] = endXY;
        this.SelectY = start + "->" + end;
        this.DataY = this.getData(startXY, endXY);
      }
       // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "selectData",
        content: {
          type: this.SelectAxis,
          value: start + "->" + end,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    tableChange(oldValue,newValue){
      // console.log(oldValue);
      // console.log(newValue);
    },
    getData(start, end) {
      //取值
      let data = [];
      let minx =
        parseInt(start[0]) <= parseInt(end[0])
          ? parseInt(start[0])
          : parseInt(end[0]);
      let maxx =
        parseInt(start[0]) >= parseInt(end[0])
          ? parseInt(start[0])
          : parseInt(end[0]);
      let miny =
        parseInt(start[1]) <= parseInt(end[1])
          ? parseInt(start[1])
          : parseInt(end[1]);
      let maxy =
        parseInt(start[1]) >= parseInt(end[1])
          ? parseInt(start[1])
          : parseInt(end[1]);
      for (let i = minx; i <= maxx; i++) {
        let colData = [];
        for (let j = miny; j <= maxy; j++) {
          let value = $("#mytable").jexcel("getValue", i + "-" + j);
          colData.push(value);
        }
        data.push(colData);
      }
      for (let i = 0; i < data.length; i++) {
        let header = $("#mytable").jexcel("getHeader", i + minx);
        data[i].unshift(header);
      }
      return data;
    },
    back2Table() {
      this.visualization = false;
    },
    showCharts() {
      this.visualization = true;
      let dataLength =
        this.DataX[0].length <= this.DataY[0].length
          ? this.DataX[0].length
          : this.DataY[0].length;
      if (this.chooseType == "Line") {
        var option = {
          grid:{
            width: 400,
            borderWidth:1
          },
          xAxis: {
            type: "category",
            name: this.DataX[0][0],
            data: []
          },
          yAxis: {
            type: "value",
            name: this.DataY[0][0]
          },
          series: [
            {
              data: [],
              type: "line",
              smooth: this.smoothed
            }
          ]
        };
        for (let i = 1; i < dataLength; i++) {
          option.xAxis.data.push(this.DataX[0][i]);
          option.series[0].data.push(this.DataY[0][i]);
        }
        if (this.Charts != null) {
          this.Charts.dispose();
          this.Charts = null;
          this.Charts = echarts.init(document.getElementById("visualization"));
        }
        this.Charts.setOption(option);
      } else if (this.chooseType == "Stacked-line") {
        var option = {
          grid:{
            width: 400,
            borderWidth:1
          },
          tooltip: {
            trigger: "axis"
          },
          legend: {
            itemWidth: 20,
            data: []
          },
          xAxis: {
            type: "category",
            name: this.DataX[0][0],
            data: []
          },
          yAxis: {
            type: "value"
          },
          series: []
        };
        // X axis
        for (var i = 1; i < dataLength; i++) {
          option.xAxis.data.push(this.DataX[0][i]);
        }
        // Y axis
        for (var j = 0; j < this.DataY.length; j++) {
          let line = {
            data: [],
            type: "line",
            name: this.DataY[j][0],
            smooth: this.smoothed
          };
          option.legend.data.push(this.DataY[j][0]);
          for (var i = 1; i < dataLength; i++) {
            line.data.push(this.DataY[j][i]);
          }
          option.series.push(line);
        }

        if (this.Charts != null) {
          this.Charts.dispose();
          this.Charts = null;
          this.Charts = echarts.init(document.getElementById("visualization"));
        }
        this.Charts.setOption(option);
      }
    },
    Visualize() {
      if (this.DataX.length == 0 || this.DataY.length == 0) {
        return;
      }
      let send_content = {
        type: "operation",
        sender: this.userInfo.userId,
        behavior: "visualize",
        content: {
          chartType: this.chooseType,
          dataX: this.DataX,
          dataY: this.DataY,
          smooth: this.smoothed,
        }
      };
      sendCustomOperation(send_content);

      this.showCharts();
    },
    chooseTypeChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "chooseType",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    sendSmoothedParams: function (modelInEvent, stateIndex) {
      this.smoothed = !this.smoothed;
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "radio",
        content: {
          type: stateIndex,
          value: modelInEvent,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    sendRadioParams: function (modelInEvent, stateIndex) {
      sendInputParams(modelInEvent, stateIndex);
    },
    sendInputParams: function (modelInEvent, stateIndex) {
      sendInputParams(modelInEvent, stateIndex);
    },
    getSocketConnect(data) {
      let socketData = data;

      if (socketData.from === "Test") {
      } else if (socketData.type === "members") {
        let members = data.message
          .replace("[", "")
          .replace("]", "")
          .replace(/\s/g, "")
          .split(",");
        this.olParticipants = members;
        this.olParticipantChange();
      } else {
        if (socketData.operate === "visualize") {
          this.chooseType = socketData.chartType;
          try {
            let start =
              String.fromCharCode(
                parseInt(socketData.startX[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.startX[1]) + 1);
            let end =
              String.fromCharCode(
                parseInt(socketData.endX[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.endX[1]) + 1);
            this.SelectX = start + "->" + end;
            this.DataX = this.getData(socketData.startX, socketData.endX);

            start =
              String.fromCharCode(
                parseInt(socketData.startY[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.startY[1]) + 1);
            end =
              String.fromCharCode(
                parseInt(socketData.endY[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.endY[1]) + 1);
            this.SelectY = start + "->" + end;
            this.DataY = this.getData(socketData.startY, socketData.endY);
          } catch (e) {}
          this.showCharts();
        } else if (socketData.operate === "selectdata") {
          this.dataUrl = socketData.pathURL;
          this.viewData();
        }
      }
    },
  }
};
</script>




<style>
@import "../../../../../static/css/jquery.jexcel.css";
</style>
<template>
  <Row>
    <toolStyle
      :participants="participants"
      :resources="resources"
      v-on:resourceUrl="selecetResource"
    ></toolStyle>
    <div style="width: 300px; padding:30px;margin-left:60px; float:left">
      <h3>Select data:</h3>
      <RadioGroup v-model="SelectAxis">
        <Radio label="Place" style="padding:20px 0 10px 0"></Radio>
        <Input v-model="SelectName" style="width:200px" readonly />
        <Radio label="Value" style="padding:20px 0 10px 0"></Radio>
        <Input v-model="SelectValue" style="width:200px" readonly />
        <Radio label="Coordinate" style="padding:20px 0 10px 0"></Radio>
        <Input v-model="Coords" style="width:200px" readonly />
      </RadioGroup>
      <Button @click="Visualize" style="margin-top:30px">Visualization</Button>
      <Button v-if="visualization" @click="back2Table" style="margin-top:30px">Select data</Button>
    </div>
    <div style="padding-top:30px; float:left; width: calc(100vw - 400px)">
      <div
        v-show="visualization"
        id="visualization"
        style="width: calc(100vw - 400px); height:calc(90vh);background-color:#f8f8f9"
      ></div>
      <div v-show="!visualization">
        <div id="mytable" style="height:calc(90vh)"></div>
      </div>
    </div>
  </Row>
</template>
<script>
import * as socketApi from "@/api/socket.js";
import csv from "@static/js/jquery.csv.min.js";
import jexcel from "@static/js/jquery.jexcel.js";
import XLSX from "xlsx";
import echarts from "echarts";
import toolStyle from "../toolStyle";
import "echarts/extension/bmap/bmap";
import { MP } from "@static/js/baidu-map.js";
export default {
  components: { toolStyle },
  data() {
    return {
      sidebarHeight: 0,
      visualization: false,
      testData: [],
      columnHeader: [],
      excelData: [],
      columnNameList: [],
      //data and params
      DataX: [],
      DataY: [],
      DataZ: [],
      SelectName: "",
      SelectValue: "",
      Coords: "",
      SelectAxis: "",
      chartData: [],
      chooseType: "",
      Charts: null,
      chartSettings: {},
      // page info
      pageParams: { pageId: "", userId: "", userName: "" },
      userInfo: {},
      //协同消息
      socket_content: {},
      participants: [],
      olParticipants: [],
      resources: [],
      dataUrl: ""
    };
  },
  beforeDestroy() {
    // this.socketApi.close();
  },
  mounted() {
    this.init();
    this.getResources();
    this.startWebSocket();
    this.$nextTick(() => {
      let _this = this;
      MP("zkBKRbVfjeOMnIZFXtaTcr9b");
    });
  },
  methods: {
    init() {
      $("#app").css("min-width", "0");
      $("#app").css("min-height", "0");
      this.sidebarHeight = window.innerHeight + "px";
      $("#mytable").jexcel({
        data: this.testData,
        minDimensions: [20, 20],
        onselection: this.selectData
      });
      this.getStepInfo();
      this.getUserInfo();
    },
    getStepInfo() {
      if (
        this.$route.params.groupID == undefined ||
        this.$route.params.groupID == ""
      ) {
        var href = window.location.href;
        var url = href.split("&");

        for (var i = 0; i < url.length; i++) {
          if (/groupID/.test(url[i])) {
            this.pageParams.pageId = url[i].match(/groupID=(\S*)/)[1];
            continue;
          }

          if (/userID/.test(url[i])) {
            this.pageParams.userId = url[i].match(/userID=(\S*)/)[1];
            continue;
          }

          if (/userName/.test(url[i])) {
            this.pageParams.userName = url[i].match(/userName=(\S*)/)[1];
            continue;
          }
        }
      } else {
        this.pageParams.pageId = this.$route.params.groupID;
        this.pageParams.userId = this.$route.params.userID;
        this.pageParams.userName = this.$route.params.userName;
      }
    },
    getUserInfo() {
      this.userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
      if (this.userInfo == {}) {
        this.axios
          .get(
            "/GeoProblemSolving/user" +
            "?key=userId" +
            "&value=" +
              this.pageParams.userId
          )
          .then(res => {
            if (res.data.code == 0) {
              this.$set(this, "userInfo", res.data.data);
            }
          })
          .catch(err => {});
      }
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
            onselection: that.selectData
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

      if (this.SelectAxis == "Place") {
        this.socket_content["startX"] = startXY;
        this.socket_content["endX"] = endXY;
        this.SelectName = start + "->" + end;
        this.DataX = this.getData(startXY, endXY);
      } else if (this.SelectAxis == "Value") {
        this.socket_content["startY"] = startXY;
        this.socket_content["endY"] = endXY;
        this.SelectValue = start + "->" + end;
        this.DataY = this.getData(startXY, endXY);
      } else if (this.SelectAxis == "Coordinate") {
        this.socket_content["startY"] = startXY;
        this.socket_content["endY"] = endXY;
        this.Coords = start + "->" + end;
        this.DataZ = this.getData(startXY, endXY);
      }
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
      let dataLength =
        this.DataX[0].length <= this.DataY[0].length
          ? this.DataX[0].length
          : this.DataY[0].length;
      dataLength =
        dataLength <= this.DataZ[0].length ? dataLength : this.DataZ[0].length;

      var convertData = function(data) {
        var res = [];
        for (var i = 0; i < data.length; i++) {
          var geoCoord = geoCoordMap[data[i].name];
          if (geoCoord) {
            res.push({
              name: data[i].name,
              value: geoCoord.concat(data[i].value)
            });
          }
        }
        return res;
      };

      var data = [],
        geoCoordMap = {},
        maxValue = 0;
      for (var i = 1; i < dataLength; i++) {
        if (this.DataY[0][i] > maxValue) {
          maxValue = this.DataY[0][i];
        }
        data.push({
          name: this.DataX[0][i],
          value: this.DataY[0][i]
        });
        geoCoordMap[this.DataX[0][i].toString()] = [
          this.DataZ[0][i],
          this.DataZ[1][i]
        ];
      }

      var option = {
        tooltip: {
          trigger: "item"
        },
        bmap: {
          center: [104.114129, 37.550339],
          zoom: 5,
          roam: true,
          coordinateSystem: "bmap",
          mapStyle: {
            styleJson: [
              {
                featureType: "water",
                elementType: "all",
                stylers: {
                  color: "#d1d1d1"
                }
              },
              {
                featureType: "land",
                elementType: "all",
                stylers: {
                  color: "#f3f3f3"
                }
              },
              {
                featureType: "railway",
                elementType: "all",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "highway",
                elementType: "all",
                stylers: {
                  color: "#fdfdfd"
                }
              },
              {
                featureType: "highway",
                elementType: "labels",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "arterial",
                elementType: "geometry",
                stylers: {
                  color: "#fefefe"
                }
              },
              {
                featureType: "arterial",
                elementType: "geometry.fill",
                stylers: {
                  color: "#fefefe"
                }
              },
              {
                featureType: "poi",
                elementType: "all",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "green",
                elementType: "all",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "subway",
                elementType: "all",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "manmade",
                elementType: "all",
                stylers: {
                  color: "#d1d1d1"
                }
              },
              {
                featureType: "local",
                elementType: "all",
                stylers: {
                  color: "#d1d1d1"
                }
              },
              {
                featureType: "arterial",
                elementType: "labels",
                stylers: {
                  visibility: "off"
                }
              },
              {
                featureType: "boundary",
                elementType: "all",
                stylers: {
                  color: "#fefefe"
                }
              },
              {
                featureType: "building",
                elementType: "all",
                stylers: {
                  color: "#d1d1d1"
                }
              },
              {
                featureType: "label",
                elementType: "labels.text.fill",
                stylers: {
                  color: "#999999"
                }
              }
            ]
          }
        },
        series: [
          {
            name: this.DataY[0][0],
            type: "scatter",
            coordinateSystem: "bmap",
            data: convertData(data),
            symbolSize: function(val) {
              // console.log(val);
              return val[2]/maxValue*50;
            },
            label: {
              formatter: "{b}",
              position: "right",
              show: false
            },
            itemStyle: {
              color: "blue"
            },
            emphasis: {
              label: {
                show: true
              }
            }
          }
        ]
      };

      if (this.Charts == null) {
        this.Charts = echarts.init(document.getElementById("visualization"));
      }
      this.Charts.setOption(option);
    },
    Visualize() {
      if (this.DataX.length == 0 || this.DataY.length == 0) {
        return;
      }
      this.socket_content["chartType"] = this.chooseType;
      this.socket_content["operate"] = "visualize";
      this.socketApi.sendSock(this.socket_content, this.getSocketConnect);
      // this.socket_content = {};

      this.visualization = true;
      this.showCharts();
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
            this.SelectName = start + "->" + end;
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
            this.SelectValue = start + "->" + end;
            this.DataY = this.getData(socketData.startY, socketData.endY);

            start =
              String.fromCharCode(
                parseInt(socketData.startZ[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.startZ[1]) + 1);
            end =
              String.fromCharCode(
                parseInt(socketData.endZ[0]) + "A".charCodeAt()
              ) +
              (parseInt(socketData.endZ[1]) + 1);
            this.Coords = start + "->" + end;
            this.DataZ = this.getData(socketData.startZ, socketData.endZ);
          } catch (e) {}
          this.visualization = true;
          this.showCharts();
        } else if (socketData.operate === "selectdata") {
          this.dataUrl = socketData.pathURL;
          this.viewData();
        }
      }
    },
    startWebSocket() {
      if (this.pageParams.pageId == undefined || this.pageParams.pageId == "") {
        this.$Message.error("Lose the information of current step.");
        return false;
      }

      let roomId = this.pageParams.pageId;
      this.socketApi.initWebSocket(
        "ChartsServer/" + "mapScatter" + roomId,
        this.$store.state.IP_Port
      );

      this.send_msg = {
        type: "test",
        from: "Test",
        content: "TestChat"
      };
      this.socketApi.sendSock(this.send_msg, this.getSocketConnect);
    },
    getResources() {
      if (this.pageParams.pageId == undefined || this.pageParams.pageId == "") {
        this.$Message.error("Lose the information of current step.");
        return false;
      }

      this.resources = [];
      this.axios
        .get(
          "/GeoProblemSolving/folder/inquiry?folderId=" + this.pageParams.pageId
        )
        .then(res => {
          // 写渲染函数，取到所有资源
          if (res.data !== "None") {
            for (let i = 0; i < res.data.files.length; i++) {
              if (
                res.data.files[i].type == "data" &&
                /\.(xls|xlsx|csv)$/.test(res.data.files[i].name.toLowerCase())
              ) {
                this.resources.push(res.data.files[i]);
              }
            }
          } else {
            this.resources = [];
          }
        })
        .catch(err => {
          console.log(err.data);
        });
    },
    selecetResource(url) {
      this.dataUrl = url;

      // 协同
      this.send_content = {
        operate: "selectdata",
        pathURL: this.dataUrl
      };
      this.socketApi.sendSock(this.send_content, this.getSocketConnect);

      this.viewData();
    },
    viewData() {
      if (/\.(csv|xls|xlsx)$/.test(this.dataUrl.toLowerCase())) {
        var that = this;
        var xhr = new XMLHttpRequest();
        xhr.open("GET", this.dataUrl, true);
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

      this.showFile = false;
    },
    olParticipantChange() {
      let userIndex = -1;

      // 自己刚上线，olParticipants空
      if (this.participants.length == 0) {
        for (let i = 0; i < this.olParticipants.length; i++) {
          this.axios
            .get(
              "/GeoProblemSolving/user" +
            "?key=userId" +
            "&value=" +
                this.olParticipants[i]
            )
            .then(res => {
              if (res.data.code == 0) {
                this.participants.push(res.data.data);
              } else {
              }
            });
        }
      } else {
        // members大于olParticipants，有人上线；小于olParticipants，离线
        if (this.olParticipants.length > this.participants.length) {
          for (var i = 0; i < this.olParticipants.length; i++) {
            for (var j = 0; j < this.participants.length; j++) {
              if (this.olParticipants[i] == this.participants[j].userId) {
                break;
              }
            }
            if (j == this.participants.length) {
              userIndex = i;
              break;
            }
          }

          // 人员渲染
          var that = this;
          this.axios
            .get(
              "/GeoProblemSolving/user" +
            "?key=userId" +
            "&value=" +
                this.olParticipants[userIndex]
            )
            .then(res => {
              if (res.data.code == 0) {
                that.participants.push(res.data.data);
                if (userIndex != -1) {
                }
              } else {
              }
            });
        } else if (this.olParticipants.length < this.participants.length) {
          for (var i = 0; i < this.participants.length; i++) {
            for (var j = 0; j < this.olParticipants.length; j++) {
              if (this.participants[i].userId == this.olParticipants[j]) {
                break;
              }
            }
            if (j == this.olParticipants.length) {
              userIndex = i;
              break;
            }
          }
          this.participants.splice(userIndex, 1);
        }
      }
    }
  }
};
</script>

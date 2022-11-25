<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <vue-scroll :ops="ops" style="height: calc(100vh - 40px)">
        <!-- coding for your tools // begin-->

        <!-- ClimateTimeSeries -->
        <div style="margin: 30px 20px">
          <Label class="labelTile">ClimateTimeSeries:</Label>
          <Select
            v-model="climateTimeSeries"
            style="width: 200px"
            @on-change="climateTimeSeriesChange"
          >
            <Option value="Monthly_AverageAllYears">Monthly_AverageAllYears</Option>
            <Option value="Monthly_RandomYears">Monthly_RandomYears</Option>
            <Option value="Monthly_SequencedYears">Monthly_SequencedYears</Option>
            <Option value="Daily_AverageAllYears">Daily_AverageAllYears</Option>
            <Option value="Daily_RandomYears">Daily_RandomYears</Option>
            <Option value="Daily_SequencedYears">Daily_SequencedYears</Option>
          </Select>
        </div>

        <!-- ClimateFile -->
        <div style="margin: 30px 20px">
          <Button
            type="info"
            style="width: 200px"
            :loading="boolClimateFile"
            @click="selectClimateFile"
          >
            <span v-if="!boolClimateFile"
              >Select climate file</span
            >
            <span v-else>Importing data...</span>
          </Button>
          <Label class="labelTile" style="margin-left: 20px"
            >ClimateFile:
            {{ climateFile.name + climateFile.suffix }}</Label
          >
        </div>

        <!-- ClimateFileFormat -->
        <div style="margin: 30px 20px">
          <Label class="labelTile">ClimateFileFormat:</Label>
          <Select v-model="climateFileFormat" style="width: 200px" @on-change="climateFileFormatChange">
            <Option value="Monthly_Temp-C_Precip-mmMonth">Monthly_Temp-C_Precip-mmMonth</Option>
            <Option value="Monthly_Temp-K_Precip-kgm2Sec">Monthly_Temp-K_Precip-kgm2Sec</Option>
            <Option value="Monthly_Temp-K_Precip-mmMonth">Monthly_Temp-K_Precip-mmMonth</Option>
            <Option value="Daily_Temp-C_Precip-mmDay">Daily_Temp-C_Precip-mmDay</Option>
            <Option value="Daily_Temp-K_Precip-kgm2Sec">Daily_Temp-K_Precip-kgm2Sec</Option>
            <Option value="Daily_Temp-K_Precip-mmDay">Daily_Temp-K_Precip-mmDay</Option>
          </Select>
        </div>

        <!-- SpinUpClimateTimeSeries -->
        <div style="margin: 30px 20px">
          <Label class="labelTile">SpinUpClimateTimeSeries:</Label>
          <Select v-model="spinUpClimateTimeSeries" style="width: 200px" @on-change="spinUpClimateTimeSeriesChange">
            <Option value="Monthly_AverageAllYears">Monthly_AverageAllYears</Option>
            <Option value="Monthly_RandomYears">Monthly_RandomYears</Option>
            <Option value="Monthly_SequencedYears">Monthly_SequencedYears</Option>
            <Option value="Daily_AverageAllYears">Daily_AverageAllYears</Option>
            <Option value="Daily_RandomYears">Daily_RandomYears</Option>
            <Option value="Daily_SequencedYears">Daily_SequencedYears</Option>
          </Select>
        </div>

        <!-- SpinUpClimateFile -->
        <div style="margin: 30px 20px">
          <Button
            type="info"
            style="width: 200px"
            :loading="boolSpinUpClimateFile"
            @click="selectSpinUpClimateFile"
          >
            <span v-if="!boolSpinUpClimateFile"
              >Select spin up climate file</span
            >
            <span v-else>Importing data...</span>
          </Button>
          <Label class="labelTile" style="margin-left: 20px"
            >spinUpClimateFile:
            {{ spinUpClimateFile.name + spinUpClimateFile.suffix }}</Label
          >
        </div>

        <!-- SpinUpClimateFileFormat -->
        <div style="margin: 30px 20px">
          <Label class="labelTile">SpinUpClimateFileFormat:</Label>
          <Select v-model="spinUpClimateFileFormat" style="width: 200px" @on-change="spinUpClimateFileFormatChange">
            <Option value="Monthly_Temp-C_Precip-mmMonth">Monthly_Temp-C_Precip-mmMonth</Option>
            <Option value="Monthly_Temp-K_Precip-kgm2Sec">Monthly_Temp-K_Precip-kgm2Sec</Option>
            <Option value="Monthly_Temp-K_Precip-mmMonth">Monthly_Temp-K_Precip-mmMonth</Option>
            <Option value="Daily_Temp-C_Precip-mmDay">Daily_Temp-C_Precip-mmDay</Option>
            <Option value="Daily_Temp-K_Precip-kgm2Sec">Daily_Temp-K_Precip-kgm2Sec</Option>
            <Option value="Daily_Temp-K_Precip-mmDay">Daily_Temp-K_Precip-mmDay</Option>
          </Select>
        </div>
        <Divider></Divider>
        <Button
          @click="beforeSubmit()"
          type="success"
          style="margin: 0 20px 20px; width: 200px"
          >Submit</Button
        >
        <Button
          @click="beforeResetAll()"
          type="warning"
          style="margin: 0 20px 20px; width: 200px"
          >Clear All</Button
        >
        <!-- // end -->
      </vue-scroll>
    </div>
  </div>
</template>
<script>
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
      selectData: {},
      //
      boolClimateFile: false,
      boolSpinUpClimateFile: false,
      climateTimeSeries: "",
      climateFile: {},
      climateFileFormat: "",
      spinUpClimateTimeSeries: "",
      spinUpClimateFile: {},
      spinUpClimateFileFormat: "",
    };
  },
  created() {},
  mounted() {
    // 加载协同组件
    loadCollabComponent();
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
    selectClimateFile() {
      this.boolClimateFile = !this.boolClimateFile;
      this.boolSpinUpClimateFile = false;
    },
    selectSpinUpClimateFile() {
      this.boolClimateFile = false;
      this.boolSpinUpClimateFile = !this.boolSpinUpClimateFile;
    },
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "select"){
        this[content.type] = content.value;
      } else if (behavior == "reset"){
        this.resetAll();
      } else if (behavior == "final-submit"){
        // this.submit();
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has submit the list of selected tree species information '
                ])
              }
          });
      } else if (behavior == "success"){
        // success
        this.$Notice.success({
          title: 'Submit successfully',
          duration: 10,});
      }

    },
    getSocketComputation(data) {

    },
    getClimateConfigFile(data) {},
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
        if(content.type != "" && content.type == "climateFile"){
          this.climateFile = content.data;
          this.boolClimateFile = false;
        } else if (content.type != "" && content.type == "spinUpClimateFile") {
          this.spinUpClimateFile = content.data;
          this.boolSpinUpClimateFile = false;
        }
      }
    },
    loadResources(resList) {
      let type = "";
      for (let i = 0; i < resList.length; i++) {
        // your function
        if (this.boolClimateFile) {
          type = "climateFile";
          this.selectData = resList[0];
          this.climateFile = resList[0];
          this.boolClimateFile = false;
        } else if (this.boolSpinUpClimateFile) {
          type = "spinUpClimateFile";
          this.selectData = resList[0];
          this.spinUpClimateFile = resList[0];
          this.boolSpinUpClimateFile = false;
        }
      }
      let send_content = {
        type: "resource",
        sender: this.userInfo.userId,
        behavior: "file",
        content: {
          type: type,
          data: this.selectData,
        }
      };
      sendCustomOperation(send_content);
    },
    beforeResetAll(){
      this.resetAll();

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "reset",
        content: {
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    resetAll(){
      this.climateTimeSeries = "";
      this.climateFile = {};
      this.climateFileFormat = "";
      this.spinUpClimateTimeSeries = "";
      this.spinUpClimateFile = {};
      this.spinUpClimateFileFormat = "";
    },
    beforeSubmit(){
      this.submit();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "final-submit",
        content: {
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    submit() {
      let aid = this.activityInfo.aid;
      let graphId = this.activityInfo.parent;
      let toolId = this.toolId;
      let participant = this.participants;
      let submitInfo = {};
      let climateConfig = {};
      climateConfig.climateTimeSeries = this.climateTimeSeries;
      climateConfig.climateFile = this.climateFile.address;
      climateConfig.climateFileFormat = this.climateFileFormat;
      climateConfig.spinUpClimateTimeSeries = this.spinUpClimateTimeSeries;
      climateConfig.spinUpClimateFile = this.spinUpClimateFile.address;
      climateConfig.spinUpClimateFileFormat = this.spinUpClimateFileFormat;
      submitInfo.aid = aid;
      submitInfo.graphId = graphId;
      submitInfo.toolId = toolId;
      submitInfo.participant = participant;
      submitInfo.climateConfig = climateConfig;
      this.axios
        .post("/GeoProblemSolving/landis/climateConfig",submitInfo)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
            // this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // success
            this.$Notice.success({
              title: 'Submit successfully',
              duration: 10,});
            // websocket
            let paramsMsg = {
              type: "operation",
              behavior: "success",
              content: {
              },
              sender: this.userInfo.userId,
            };
            sendCustomOperation(paramsMsg);
            let operationResult = res.data.data;
            let oid = operationResult.operationId
            loadingBackendOperation(oid)
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    climateTimeSeriesChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "climateTimeSeries",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    climateFileFormatChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "climateFileFormat",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    spinUpClimateTimeSeriesChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "spinUpClimateTimeSeries",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    spinUpClimateFileFormatChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "spinUpClimateFileFormat",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
  },
};
</script>
<style scoped>
.labelTile {
  font-size: larger;
  font-weight: bold;
  margin: 5px 0;
}
</style>

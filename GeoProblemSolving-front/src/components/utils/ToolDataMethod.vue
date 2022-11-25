<template>
  <div class="main">
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content" class="scrollbar">
      <div id="edit-mask" title="The other participant is operating."></div>
      <el-row>
        <el-col
          :span="22"
          :offset="1"
          v-if="dataService.hasOwnProperty('metaDetail')"
        >
          <vue-scroll
            :ops="scrollOps"
            style="height: calc(100vh - 100px)"
          >
            <el-divider
              direction="vertical"
              style="height: 1000px; float: left"
            ></el-divider>
            <div class="_params-group">
              <div class="stateTitle">Input</div>
              <el-divider class="stateTitleDivider"></el-divider>
              <div
                v-for="(input, index) in inputData"
                :key="index"
                class="event"
              >
                <el-row>
                  <el-col :span="17" class="_event-desc">
                    <span class="event_name" :title="input.name">{{
                      input.name
                    }}</span>
                    <p class="event_desc" :title="input.description">
                      {{ input.description }}
                    </p>
                  </el-col>
                  <el-col :span="6" :offset="1">
                    <div
                      v-if="
                        input.hasOwnProperty('url') &&
                        input.url != '' &&
                        input.urlName != ''
                      "
                    >
                      <div class="select-data select-data-line">
                        <div class="data-name">{{ input.urlName }}</div>
                        <el-button
                          type="success"
                          icon="el-icon-download"
                          size="mini"
                          circle
                          @click="download(input)"
                        ></el-button>
                        <el-button
                          type="warning"
                          icon="el-icon-close"
                          size="mini"
                          circle
                          @click="remove(input, index)"
                        ></el-button>
                      </div>
                    </div>
                    <div v-else>
                      <el-button
                        type="primary"
                        plain
                        @click="selectDataDialog(index)"
                      >
                        Select data
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>

            <!--          Parameter 显示部分-->
            <div class="_params-group" v-if="inputParameter.length != 0">
              <div class="stateTitle">Parameter</div>
              <el-divider class="stateTitleDivider"></el-divider>
              <div
                v-for="(parameter, index) in inputParameter"
                :key="index"
                class="event"
              >
                <el-row>
                  <el-col :span="17" class="_event-desc">
                    <span class="event_name" :title="parameter.name">{{
                      parameter.name
                    }}</span>
                    <p class="event_desc" :title="parameter.description">
                      {{ parameter.description }}
                    </p>
                  </el-col>
                  <el-col :span="6" :offset="1">
                    <el-input
                      style="width: 200px"
                      v-model="parameter.value"
                      placeholder="Enter the parameter"
                      @focus="sendInputTyping(index, 'in')"
                      @change="sendToolInputParams(index)"
                      @blur="sendInputTyping(index, 'out')"
                      class="typing"
                      :ref="'input' + index"
                    ></el-input>
                  </el-col>
                </el-row>
              </div>
            </div>
            <!--          OUTPUT 相关-->
            <div class="_params-group">
              <div class="stateTitle">Output</div>
              <el-divider class="stateTitleDivider"></el-divider>

              <div
                v-for="(output, index) in outputData"
                :key="index"
                class="event"
              >
                <el-row style="height: 100px">
                  <el-col :span="17" class="_event-desc">
                    <span class="event_name" :title="output.name">{{
                      output.name
                    }}</span>
                  </el-col>
                  <div>
                    <div
                      class="_btn-group"
                      v-if="type == 'Processing' || type == 'Conversion'"
                    >
                      <el-button
                        plain
                        round
                        type="warning"
                        @click="download(output)"
                        v-if="output.hasOwnProperty('url') && output.url != ''"
                        >Download
                      </el-button>
                      <!-- <el-button
                        plain
                        round
                        type="warning"
                        @click="bind(output)"
                        :class="{ bindClass: output.bind }"
                        v-if="output.hasOwnProperty('url') && output.url != ''"
                        >Bind
                      </el-button> -->
                    </div>
                    <div
                      v-else-if="
                        type == 'Visualization' &&
                        output.hasOwnProperty('url') &&
                        output.url != ''
                      "
                    >
                      <div class="output-pic" @mouseenter="maskVisible">
                        <el-avatar
                          shape="square"
                          :size="80"
                          fit="fill"
                          :src="output.url"
                        ></el-avatar>
                      </div>
                      <div v-show="showMask" @mouseout="maskInvisible">
                        <div class="mask">
                          <i
                            class="el-icon-view mask-icon"
                            style="margin-left: 20px"
                            @click="zoomOutPicDialog(output.url)"
                          />
                          <i
                            class="el-icon-download mask-icon"
                            @click="download(output)"
                          />
                        </div>
                      </div>

                      <!-- <img
                        src="https://bkimg.cdn.bcebos.com/pic/0e2442a7d933c89566f98647d91373f082020002?x-bce-process=image/resize,m_lfit,w_480,limit_1"
                        height="90"
                        width="200"
                      /> -->
                    </div>
                  </div>
                </el-row>
              </div>
            </div>
          </vue-scroll>
        </el-col>
      </el-row>

      <el-row ref="invokeButton">
        <el-button
          type="primary"
          @click="invokeTest"
          style="float: right; width: 110px; margin-right: 15%"
        >
          <i class="el-icon-setting"></i>&nbsp;Invoke
        </el-button>
      </el-row>

      <el-dialog
        :visible.sync="selectDataDialogShow"
        width="1000px"
        title="Select data from Resource Center or Upload"
        :close-on-click-modal="false"
        :destroy-on-close="true"
      >
        <resource-list @selectData="selectData"></resource-list>
      </el-dialog>
      <el-dialog
        :visible.sync="zoomOutPicDialogShow"
        width="1000px"
        title="Output Picture"
        :close-on-click-modal="false"
        :destroy-on-close="true"
      >
        <img :src="zoomUrl" width="950" />
      </el-dialog>
    </div>
  </div>
</template>

<!--协同工具 zhngzhng-->
<script>
import { get, post } from "@/axios";
import resourceList from "../common/resource/UserAndActivityResourceList";

export default {
  components: {
    resourceList,
  },
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      //toolId
      id: "",
      dataToken: "",
      type: "", ///Processing,Visualization,Data

      dataService: {},
      dataServiceDetail: {},

      record: {},
      selectDataDialogShow: false,
      url: "",
      inputIndex: "",
      showMask: false,
      zoomOutPicDialogShow: false,
      zoomUrl: "",

      //zhngzhng
      toolId: "",
      aid: "",
      toolInfo: {},
      inputData: [],
      inputParameter: [],
      outputData: [],
    };
  },
  mounted() {
    this.toolId = window.frameElement.id;
    //初始化协同模板 collaboration.loadCollabComponent -> initComponent
    loadCollabComponent();
    this.initTool();
  },

  methods: {
    initTool: function () {
      this.initLoading();
      this.getStepInfo();
      this.getToolInfo();
      this.fullscreenLoading.close();
    },
    initLoading: function () {
      this.fullscreenLoading = this.$loading({
        lock: true,
        text: "Initialization",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, .7)",
      });
    },
    getStepInfo: function () {
      if (componentStatus) {
        //读取信息
        this.aid = activityInfo.aid;
        this.userInfo = userInfo;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          null,
          this.getSocketComputation
        );
      } else {
        setTimeout(() => {
          this.getStepInfo();
        }, 1000);
      }
    },
    /*
      toolId 从iframe id 中获得
      不用等待其他内容加载完成
      获取metaDetail用于生成工具
      1. 查看服务是否在线
      2. 在线则显示，并赋值
       */
    getToolInfo: function () {
      this.$axios
        .get("/GeoProblemSolving/tool/" + this.toolId)
        .then((res) => {
          if (res.data.code == 0) {
            /*
              获取到 toolInfo 读取token,查看服务是否在线
               */
            let tool = res.data.data;
            let token = tool.token;
            this.$axios
              .get("/GeoProblemSolving/dataContainer/" + token)
              .then((res) => {
                if (res.data.code == 0) {
                  //这里需要那么多参数吗
                  this.$set(this, "toolInfo", tool);
                  //数据处理方法 id
                  this.id = tool.dataMethodId;
                  this.dataToken = tool.token;
                  this.type = tool.dataMethodType;
                  this.dataService = tool.dataMethodMeta.data;
                  this.inputData = tool.dataMethodMeta.data.metaDetail.input;
                  this.outputData = tool.dataMethodMeta.data.metaDetail.output;
                  this.inputParameter =
                    tool.dataMethodMeta.data.metaDetail.parameter;
                } else {
                  this.$Notice.error({
                    title: "Fail",
                    desc: "Loading tool fail, Node offline.",
                  });
                }
              })
              .catch((err) => {
                this.$Notice.error({
                  title: "Fail",
                  desc: "Loading tool fail.",
                });
              });
          }
        })
        .catch((err) => {
          this.$Notice.error({ title: "Fail", desc: "Loading tool fail." });
        });
    },

    async getRemoteData() {
      let json = {
        serverId: this.id,
        token: this.dataToken,
      };
      let { id } = await post(`/GeoProblemSolving/dataContainer/remote`, json);
      let remoteDataList = id;
      let inputList = this.inputData;

      inputList.forEach((input) => {
        console.log(remoteDataList);
        let selectIdUrl = remoteDataList.filter(
          (el) => el.file_name == input.name
        );
        input.url = selectIdUrl[0].url;
        input.urlName = input.name;
      });

      this.$forceUpdate();
    },

    async invokeTest() {
      this.initLoading();
      let inputs = this.inputData;
      let urls = {};
      inputs.forEach((input) => {
        urls[input.name] = input.url;
      });

      let paramList = "";
      let params = this.dataService.metaDetail.parameter;
      for (let index = 0; index < params.length; index++) {
        paramList += params[index].value;
        if (index != params.length - 1) {
          paramList += ",";
        }
      }
      runTool();

      sendDataOperation(this.aid, this.id, this.dataToken, this.inputData, urls, paramList);
    },
    // webSocket 回调函数
    getSocketComputation: function (data) {
      //在coll.js 层面完成可视化
      if (data.computeSuc) {
        let output = data.outputInfo;
        let keys = Object.keys(output);
        keys.forEach((key) => {
          this.outputData.forEach((item, index) => {
            if (item.name == key) {
              this.$set(this.outputData[index], "url", output[key]);
            }
          });
        });
        this.$refs.invokeButton.$el.innerHTML = null;
        this.fullscreenLoading.close();
        this.$Notice.success({
          title: "Compute successfully",
          desc: ""
        });

        // record
        // this.operationApi.analysisRecordUpdate(
        //   this.aid,
        //   data.operationId,
        //   data.outputRes
        // );
      } else {
        this.fullscreenLoading.close();
        this.$Notice.error({
          title: "Fail",
          desc: "Compute fail, try again or connect with admin.",
        });
      }
    },

    getSocketOperation: function (data) {
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      if (behavior == "message") {
        let index = content.inputNum;
        let importer = content.importer;
        if (content.inOrOut == "in") {
          this.$refs["input" + index][0].$el.firstElementChild.style.borderColor = "red";
        } else if (content.inOrOut == "out") {
          this.$refs["input" + index][0].$el.firstElementChild.style.borderColor = "#4caf50";
        }
      } else if (behavior == "data") {
        let type = content.addOrRemove;
        if (type != "add") {
          let index1 = content.inputs.index;
          let event = content.inputs.event;
          this.$set(this.inputData, index1, event);
        } else {
          this.inputData = content.inputs;
        }
      } else if (behavior == "params") {
        this.inputParameter = content.inputs;
      } else if (behavior == "run") {
        this.initLoading();
      }
    },

    async bind(event) {
      let json = {
        name: event.name,
        description: event.description,
        type: "data",
        userUpload: false,
        address: event.url,
        uploaderId: userInfo.userId,
        uploaderName: userInfo.name,
        privacy: "private",
      };

      let data = await post(
        "/GeoProblemSolving/rip/file/bind/" + activityInfo.aid,
        json
      );

      // let data = await post("/GeoProblemSolving/resource/bind", json);
      event.bind = true;
      this.$forceUpdate();
      this.$message({
        message: "You have bind to the resource center successfully!",
        type: "success",
      });
    },

    selectDataDialog(val) {
      this.inputIndex = val;
      this.selectDataDialogShow = true;
    },

    selectData(val) {
      //从resourceList传过来
      let index = this.inputIndex;
      this.dataService.metaDetail.input[index].url = val.address;
      this.dataService.metaDetail.input[index].urlName = val.name;
      this.dataService.metaDetail.input[index].uid = val.uid;



      this.selectDataDialogShow = false;

      this.inputData = this.dataService.metaDetail.input;
      //  直接将selectData 发过去就可以
      selectDataOperation(this.inputData, "add");
    },

    download(event) {
      let url = event.url;
      if (typeof(url) == "string"){
        url = url.slice(-36);
      }
      const a = document.createElement("a");
      a.style.display = "none";
      a.href = this.$store.getters.resProxy + "/data/" + url;
      a.click();
      a.remove();
    },
    remove(event, index) {
      event.url = "";
      event.urlName = "";
      this.$set(this.inputData, index, event);
      let content = {
        index: index,
        event: event,
      };
      selectDataOperation(event, "remove");
    },
    maskVisible() {
      this.showMask = true;
    },
    maskInvisible() {
      this.showMask = false;
    },
    zoomOutPicDialog(url) {
      this.zoomUrl = url;
      this.zoomOutPicDialogShow = true;
    },

    /*
      协同相关操作
      工具开发者
      应该是面向说明文档（协同模板）来写工具
       */
    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
    sendToolInputParams: function (index) {
      sendInputParams(this.inputParameter, "0");
    },
  },
};
</script>

<style scoped>
.main {
  position: relative;
}

.state-name {
  line-height: 2;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.state-desc {
  margin: 0px 0px 15px 0px;
  padding: 4px 0px;
  line-height: 2;
  background-color: #f3f3f3;
  font-size: 16px;
  font-style: italic;
}

.el-tabs__item {
  font-size: 16px;
}

.el-tabs__item:hover {
  color: #00bbd8;
  background-color: #b5dce244;
}

.el-tabs__item.is-active {
  color: #00bbd8;
}

.el-tabs__active-bar {
  background-color: #00bbd8;
}

.leftContainer {
  background-color: rgba(142, 200, 255, 0.2);
  border-radius: 5px;
  box-shadow: 0px 0px 4px rgb(203, 207, 212);
  padding: 20px 0;
}

.modelState {
  color: rgb(37, 44, 66);
  font-size: 18px;
  font-family: "微软雅黑";
  margin: 1% 0;
}

.stateTitle {
  font-size: 20px;
  font-weight: 600;
  color: rgb(87, 173, 253);
  font-style: italic;
}

.stateTitleDivider.el-divider--horizontal {
  height: 2px;
  margin: 10px 0;
}

.stateTitleDivider.el-divider {
  background-color: rgb(140, 144, 148);
}

.event {
  padding: 15px 0 0 50px;
}

.event:hover {
  background-color: #c4d9f734;
}

.event_name {
  font-size: 16px;
  font-weight: 600;
  /* padding: 10px 0; */
}

.event_desc {
  font-size: 14px;
  font-style: italic;
  margin: 10px 0;
  color: rgb(94, 94, 94);
  word-wrap: break-word;
}

.eventDivider.el-divider--horizontal {
  margin: 10px 0;
}

.des {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  /* !autoprefixer: off */
  -webkit-box-orient: vertical;
  font-size: 14px;
}

.title {
  font-weight: 600;
  font-size: 20px;
  margin: 8px 0 10px 0;
}

.data-name {
  float: left;
  width: 200px;
  font-weight: 600;
  font-size: 16px;
}

.select-data-line {
  margin: 0 0 0 0;
  line-height: 40px;
}

.select-data {
  background-color: #f0f9eb;
  display: inline-block;
  height: 40px;
  padding: 0 10px;

  font-size: 12px;
  color: #3a701f;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
}

.select-data:hover {
  cursor: pointer;
  transition: all 0.2s ease-out;
  background-color: #cffab8;
}

.bindClass {
  background-color: rgb(255 231 231 / 72%);
  size: 20px;
  border-color: #8f2727;
  color: #8f2727;
}

.left-divider /deep/ .el-divider {
  background-color: #27298f;
}

.el-card__body {
  padding: 0;
}

.output-pic {
  position: absolute;
  right: 18%;
}

.mask {
  position: absolute;
  width: 80px;
  height: 80px;
  top: 0;
  backdrop-filter: blur(5px);
  background: rgba(230, 230, 230, 0.3);
  right: 18%;
}

/* .mask:hover {
    top: 0;
  } */
.mask-icon {
  font-size: 20px;
  color: white;
  text-align: center;
  vertical-align: middle;
  line-height: 80px;
  font-weight: 600;
}

.mask:hover {
  cursor: pointer;
}

.typing input {
  border-color: #ed4014;
}

.typing > input {
  border-color: #ed4014;
}

.typing > input:focus {
  border-color: #ed4014;
}
</style>

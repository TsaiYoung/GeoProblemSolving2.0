<!--模型 调用页面-->
<template>
  <div class="main">
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content" class="scrollbar">
      <div id="edit-mask" title="The other participant is operating."></div>
      <vue-scroll :ops="scrollOps" style="height: calc(100vh - 50px)">
        <el-row
          class="state-container"
          v-for="(state, index) in stateList"
          :key="index"
          style="padding: 10px"
        >
          <el-col class="leftContainer" :span="5">
            <el-col :offset="1" :span="22">
              <div class="modelState">
                <p class="state-name">{{ state.name }}</p>
                <p class="state-desc">{{ state.description }}</p>
              </div>
            </el-col>
          </el-col>
          <el-col class="dataContainer" :span="17" :offset="1">
            <div class="_params-group">
              <el-row v-if="inEventList(state).length !== 0" class="stateTitle"
                >Input
              </el-row>
              <el-divider class="stateTitleDivider"></el-divider>
              <div class="events">
                <!--              获取 state 中的输入 event-->
                <el-row
                  v-for="(modelInEvent, inEventIndex) in inEventList(state)"
                  :key="inEventIndex"
                  class="event"
                >
                  <el-row>
                    <el-col :span="17" class="_event-desc">
                      <span class="event_name" :title="modelInEvent.name">
                        <span
                          v-show="modelInEvent.optional == 'False'"
                          style="color: red"
                          >*</span
                        >
                        {{ modelInEvent.name }}
                      </span>
                      <p class="event_desc" :title="modelInEvent.description">
                        {{ modelInEvent.description }}
                      </p>
                    </el-col>

                    <!--                  输入 event 为参数-->
                    <el-row v-if="modelInEvent.datasetItem.type == `internal`">
                      <div v-if="filterUdxNode(modelInEvent)">
                        <el-table
                          border
                          :data="filterUdxNode(modelInEvent)[0].UdxNode"
                        >
                          <el-table-column
                            prop="name"
                            label="Parameter"
                            width="180"
                          ></el-table-column>
                          <el-table-column
                            prop="description"
                            label="Description"
                            width="180"
                          ></el-table-column>
                          <el-table-column
                            prop="type"
                            label="Type"
                          ></el-table-column>
                          <el-table-column label="Value">
                            <template slot-scope="scope">
                              <el-input
                                v-model="scope.row.value"
                                @focus="sendInputTyping(inEventIndex, 'in')"
                                @change="
                                  sendInputParams(
                                    modelInEvent,
                                    index,
                                    inEventIndex
                                  )
                                "
                                @blur="sendInputTyping(inEventIndex, 'out')"
                                :ref="'input' + inEventIndex"
                              ></el-input>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                      <div v-else>
                        <div
                          v-if="
                            modelInEvent.hasOwnProperty('url') &&
                            modelInEvent.url != '' &&
                            modelInEvent.urlName != ''
                          "
                        >
                          <div class="select-data select-data-line">
                            <div class="data-name">
                              {{ modelInEvent.urlName }}
                            </div>
                            <el-button
                              type="success"
                              icon="el-icon-download"
                              size="mini"
                              circle
                              @click="download(modelInEvent)"
                            ></el-button>
                            <el-button
                              type="warning"
                              icon="el-icon-close"
                              size="mini"
                              circle
                              @click="remove(modelInEvent, index)"
                            ></el-button>
                            <!-- <i
                            class="el-icon-close"
                            @click="remove(modelInEvent)"
                          ></i> -->
                          </div>
                        </div>
                        <div v-else>
                          <el-button
                            type="primary"
                            plain
                            @click="selectDataDialog(modelInEvent)"
                          >
                            Select data
                          </el-button>
                        </div>
                      </div>
                    </el-row>
                    <el-row v-else>
                      <!-- <el-col :span="6" :offset="1"> -->

                      <div
                        v-if="
                          modelInEvent.hasOwnProperty('url') &&
                          modelInEvent.url != '' &&
                          modelInEvent.urlName != ''
                        "
                      >
                        <div class="select-data select-data-line">
                          <div class="data-name">
                            {{ modelInEvent.urlName }}
                          </div>
                          <el-button
                            type="success"
                            icon="el-icon-download"
                            size="mini"
                            circle
                            @click="download(modelInEvent)"
                          ></el-button>
                          <el-button
                            type="warning"
                            icon="el-icon-close"
                            size="mini"
                            circle
                            @click="remove(modelInEvent, index)"
                          ></el-button>
                        </div>
                      </div>
                      <div v-else>
                        <el-button
                          type="primary"
                          plain
                          @click="selectDataDialog(modelInEvent)"
                        >
                          Select data
                        </el-button>
                      </div>
                      <!-- </el-col> -->
                    </el-row>
                  </el-row>
                  <el-row>
                    <el-divider class="eventDivider"></el-divider>
                  </el-row>
                </el-row>
              </div>
            </div>

            <!--        output 相关-->
            <div class="_params-group">
              <el-row v-if="outEventList(state).length !== 0" class="stateTitle"
                >Output
              </el-row>
              <div class="events">
                <el-row
                  v-for="(modelOutEvent, outEventIndex) in outEventList(state)"
                  :key="outEventIndex"
                  class="event"
                >
                  <el-row>
                    <el-col :span="17" class="_event-desc">
                      <span class="event_name" :title="modelOutEvent.name">{{
                        modelOutEvent.name
                      }}</span>
                      <p class="event_desc" :title="modelOutEvent.eventDesc">
                        {{ modelOutEvent.description }}
                      </p>
                    </el-col>
                    <el-col :span="6" :offset="1">
                      <div class="_btn-group">
                        <el-button
                          plain
                          round
                          type="warning"
                          @click="download(modelOutEvent)"
                          v-if="
                            modelOutEvent.hasOwnProperty('url') &&
                            modelOutEvent.url != ''
                          "
                          >Download
                        </el-button>
                        <!-- <el-button
                          plain
                          round
                          type="warning"
                          @click="bind(modelOutEvent)"
                          :class="{ bindClass: modelOutEvent.bind }"
                          v-if="
                            modelOutEvent.hasOwnProperty('url') &&
                            modelOutEvent.url != ''
                          "
                          >Bind
                        </el-button> -->
                      </div>
                    </el-col>
                  </el-row>
                </el-row>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row v-show="invokeButtonShow">
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
      </vue-scroll>
    </div>
  </div>
</template>

<script>
import file from "./File";
import resourceList from "../common/resource/UserAndActivityResourceList";
import { get, del, post, put, patch } from "@/axios";
// import func from 'vue-editor-bridge';

export default {
  components: {
    file,
    resourceList,
  },

  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      doi: this.$route.params.doi,
      modelIntroduction: {},
      stateList: {},
      modelInstance: {},
      datasetItem: {},
      timer: {},
      fullscreenLoading: {},
      componentDisables: false,
      inputFile: false,
      userId: "testUserId1234",
      stepId: "testStepId1234",
      recordList: [],
      md5: "",
      invokeForm: {
        ip: "",
        port: "",
        pid: "",
        username: "",
        inputs: [
          {
            statename: "",
            event: "",
            url: "",
            tag: "",
          },
        ],
        outputs: [
          {
            statename: "",
            event: "",
            template: {
              type: "", //id|none
              value: "", //if tyoe=none value=""
            },
          },
        ],
      },
      // aid, serviceId, serviceLocation, computeModel, userName, inputs, outputs, callback
      invokeInfo: {
        aid: "",
        serviceId: "",
        computeModel: true,
        serviceIp: "",
        servicePort: "",
        inputs: {},
        outputs: {},
      },
      // status: true,
      record: {},
      userInfo: {},
      bindFileName: "",
      selectDataDialogShow: false,
      currentStateIndex: "",
      currentEvent: "",
      toolId: "",
      activityResource: [],
      toolInfo: [],
      mdlJson: {},
      //活动id
      aid: "",

      inputEventList: [],
      outputEventList: [],
      invokeButtonShow: true
    };
  },

  mounted() {
    this.toolId = window.frameElement.id;
    loadCollabComponent();
    this.getStepInfo();
    this.init();
  },

  methods: {
    getStepInfo: function () {
      if (componentStatus) {
        //从协同组件中获取需要的数据
        this.activityResource = resources;
        this.userId = userInfo.userId;
        this.aid = activityInfo.aid;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
        loadVersionChannel = this.loadVersionInfo;
      } else {
        //避免this指向问题
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },

    getSocketOperation: function (data) {
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      if (behavior == "message") {
        //有人进入的提示
        let index = content.inputNum;
        if (content.inOrOut == "in") {
          this.$refs[
            "input" + index
          ][0].$el.firstElementChild.style.borderColor = "red";
        } else {
          this.$refs[
            "input" + index
          ][0].$el.firstElementChild.style.borderColor = "#4caf50";
        }
      } else if (behavior == "data") {
        let type = content.addOrRemove;
        if (type == "add") {
          //输入文件
          let stateIndex = content.inputs.stateIndex;
          let eventIndex = content.inputs.eventIndex;
          let url = content.inputs.url;
          let urlName = content.inputs.urlName;
          let uid = content.inputs.uid;
          this.$set(this.stateList[stateIndex].Event[eventIndex], "url", url);
          this.$set(this.stateList[stateIndex].Event[eventIndex], "uid", uid);
          this.$set(
            this.stateList[stateIndex].Event[eventIndex],
            "urlName",
            urlName
          );
        } else {
          let eventId = content.inputs.eventId;
          let stateIndex1 = content.inputs.stateIndex;
          let events = this.stateList[stateIndex1].Event;
          let findIndex = events.findIndex((item) => item.eventId == eventId);
          this.$set(this.stateList[stateIndex1].Event[findIndex], "url", "");
          this.$set(
            this.stateList[stateIndex1].Event[findIndex],
            "urlName",
            ""
          );
          this.$set(this.stateList[stateIndex1].Event[findIndex], "uid", "");
        }
      } else if (behavior == "params") {
        //输入参数
        let inEvent = content.inputs;
        let eventIndex = this.stateList[content.stateIndex].Event.findIndex(
          (event) => event.eventId == inEvent.eventId
        );
        this.$set(
          this.stateList[content.stateIndex].Event[eventIndex],
          "datasetItem",
          inEvent.datasetItem
        );
      } else if (behavior == "run") {
        this.loading();
      }
    },

    getSocketData: function(data) {
      // socket数据操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if(behavior == "load-version"){
        console.log(content);
      }
    },

    loadVersionInfo(version) {
      console.log(version);
    },

    initLoading: function () {
      this.fullscreenLoading = this.$loading({
        lock: true,
        text: "Initialization",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
      });
    },

    getSocketComputation: function (data) {
      if (data != undefined && data != null) {
        if (data.computeSuc) {
          let computeOutputs = data.outputInfo;
          this.getStateEventOut(computeOutputs);
          this.invokeButtonShow = false;
        } else {
          this.fullscreenLoading.close();
          this.$Notice.error({
            title: "Fail",
            desc: "Compute fail, try again or connect with admin.",
          });
        }
      }
    },

    getModelInfo: function () {
      this.$axios
        .get("/GeoProblemSolving/tool/" + this.toolId)
        .then((res) => {
          if (res.data.code == 0) {
            this.$set(this, "toolInfo", res.data.data);
            let tool = res.data.data;
            this.mdlJson = tool.mdlJson;
            this.datasetItem =
              tool.mdlJson.ModelClass[0].Behavior[0].RelatedDatasets[0].DatasetItem;
            this.md5 = tool.computableModelMd5;
            this.stateList = tool.mdlJson.convertMdlJson;
          } else {
            this.$Notice.error({
              title: "Fail",
              desc: "Loading compute tool fail.",
            });
          }
        })
        .catch((err) => {
          this.$Notice.error({
            title: "Fail",
            desc: "Loading compute tool fail.",
          });
        });
    },

    init: function () {
      this.initLoading();
      this.getStepInfo();
      this.getModelInfo();
      this.initTask();
      this.fullscreenLoading.close();
    },

    /*
      初始化任务(mounted 阶段)
      获取是否有部署好的模型
      若有则生成 invoke 界面
      若无则 init failed
       */
    initTask: function () {
      setTimeout(() => {
        this.axios
          .get(
            "/GeoProblemSolving/modelTask/createTask/" +
              this.md5 +
              "/" +
              userInfo.userId
          )
          .then((res) => {
            if (res.data.code == 0) {
              let data2 = res.data.data;
              this.invokeInfo.serviceIp = data2.ip;
              this.invokeInfo.servicePort = data2.port;
              this.invokeInfo.serviceId = this.md5;
            } else {
              this.$Notice.error({ title: "Tool initialization failed.",desc: res.data.msg});
            }
          })
          .catch((err) => {
            this.$Notice.error({ title: "Tool initialization failed."});
          });
      }, 1500);
    },

    //invoke --form表单创建
    getStateEvent() {
      let stateList = this.stateList;
      let input = [];
      let output = [];
      for (let i = 0; i < stateList.length; i++) {
        let events = stateList[i].Event;
        for (let j = 0; j < events.length; j++) {
          //判断数据类型 如果是input--对应url
          let detail = {};
          detail["statename"] = stateList[i].name;
          detail["event"] = events[j].name;
          //文件
          if (events[j].type == "response") {
            if (events[j].hasOwnProperty("url")) {
              detail["tag"] = events[j].name;
              detail["url"] = events[j].url;

              detail["uid"] = events[j].uid;
              input.push(detail);
            } else {
              continue;
            }
          } else if (events[j].type == "noresponse") {
            //参数
            let template = {};
            let outputTemplate = events[j].datasetItem;
            // console.log(outputTemplate);
            //如果是external template["type"] = id,不然为空
            if (outputTemplate.type === "external") {
              template = {
                type: "id",
                value: outputTemplate.externalId,
              };
            } else {
              template = {
                type: "none",
                value: "",
              };
            }
            detail["template"] = template;
            output.push(detail);
          }
        }
      }
      //将链接与state 对应起来
      // this.invokeForm.inputs = input;
      // this.invokeForm.outputs = output;
      this.invokeInfo.inputs = input;
      this.invokeInfo.ouputs = output;
    },

    async invokeTest() {
      this.loading();
      await this.createFilefromParam();
      this.getStateEvent();
      //传递开始运行信息
      runTool();
      //测试数据没有弄 直接运行 根据ip+id

      // this.operationApi.getActivityDoc(this.aid);
      // let operationId = guid();
      // this.operationApi.analysisRecord(
      //   this.aid,
      //   operationId,
      //   "",
      //   this.userId,
      //   this.toolInfo.tid,
      //   "Geographical simulation",
      //   this.invokeInfo.inputs,
      //   [],
      //   [],
      //   onlineMembers
      // );

      // console.log(this.invokeInfo.inputs);
      // console.log(this.invokeInfo.outputs);
      sendModelOperation(
        this.aid,
        this.invokeInfo.serviceId,
        this.invokeInfo.serviceIp,
        this.invokeInfo.servicePort,
        this.invokeInfo.inputs,
        this.invokeInfo.outputs
      );

      // refreshForm["ip"] = this.invokeForm.ip;
      // refreshForm["port"] = this.invokeForm.port;
      // refreshForm["tid"] = data;
      // if (data == null) {
      //   this.$message({
      //     message: "You have run the model failed",
      //     type: "error",
      //   });
      //   // this.status = false;
      // } else {
      //   // this.status = false;
      //   this.getOutputs(refreshForm);
      // }
    },

    async createFilefromParam() {
      let stateList = this.stateList;
      for (let i = 0; i < stateList.length; i++) {
        let events = stateList[i].Event;
        for (let j = 0; j < events.length; j++) {
          //判断如果是参数的话，重新绑定成为一个文件 之后上传 返回url绑定到mdl中去
          if (
            events[j].type == "response" &&
            events[j].datasetItem.hasOwnProperty("UdxDeclaration") &&
            events[j].datasetItem.UdxDeclaration[0].UdxNode != "" &&
            !events[
              j
            ].datasetItem.UdxDeclaration[0].UdxNode[0].UdxNode[0].hasOwnProperty(
              "UdxNode"
            )
          ) {
            let content = "";
            let uploadFileForm = new FormData();

            let udxNodeList =
              events[j].datasetItem.UdxDeclaration[0].UdxNode[0].UdxNode;
            for (let k = 0; k < udxNodeList.length; k++) {
              if (udxNodeList[k].hasOwnProperty("value")) {
                // content += `<XDO name="${udxNodeList[k].name}" kernelType="${udxNodeList[k].type}" value="${udxNodeList[k].value}" />`;
                content += `<XDO name="${udxNodeList[k].name}" kernelType="string" value="${udxNodeList[k].value}" />`;
              }
            }
            if (content != "") {
              content = "<Dataset> " + content + " </Dataset>";
              let file = new File([content], events[j].name + ".xml", {
                type: "text/plain",
              });
              uploadFileForm.append("file", file);

              // this.createConfigFile();
              await this.submitUpload(i, j, uploadFileForm);
            }
          }
        }
      }
    },

    async submitUpload(stateIndex, eventIndex, uploadFileForm) {
      let data = await post(
        `/GeoProblemSolving/dataContainer/uploadSingle`,
        uploadFileForm
      );
      let resultId = `http://${this.$store.state.DataServer}/data/${data}`;
      this.$set(this.stateList[stateIndex].Event[eventIndex], "url", resultId);
    },

    //动态绑定 更新outputs部分
    getStateEventOut: function (outputs) {
      let outList = this.stateList;
      outList.forEach((state, index) => {
        state.Event.forEach((event, eventIndex) => {
          outputs.forEach((el) => {
            if (el.statename == event.stateName && el.event == event.name) {
              this.$set(this.stateList[index].Event[eventIndex], "url", el.url);
              this.$set(
                this.stateList[index].Event[eventIndex],
                "urlName",
                `${el.tag}.${el.suffix}`
              );
            }
          });
        });
      });
      this.fullscreenLoading.close();
      this.$Notice.success({
        title: "Compute successfully",
        desc: ""
      });
    },

    download(event) {
      let url = event.url;
      let uid;
      if (typeof(url) == "string"){
        uid = url.slice(-36);
      }
      const a = document.createElement("a");
      a.style.display = "none";
      a.href = this.$store.getters.resProxy + "/data/" + uid;
      a.target = "_self";
      a.click();
      a.remove();
    },

    urlToBlob(the_url, callback) {
      let xhr = new XMLHttpRequest();
      xhr.open("get", the_url, true);
      xhr.send();
      var that = this;
      xhr.onreadystatechange = function () {
        if (this.readyState === 4) {
          let headers = xhr.getAllResponseHeaders();
          //打印文件名，这里打印的是编码（因为兼容不同语言的数据文件名字）后的，前端用unescape('xxx')去解码，解码后是对应的名字
          that.bindFileName = headers.split(";")[1].split("=")[1];
        }
      };

      xhr.onload = function () {
        if (this.status == 200) {
          if (callback) {
            callback(this.response);
          }
        }
      };
    },

    async bind(event) {
      let address = event.url.split("?")[0];
      let json = {
        name: event.urlName,
        description: event.description,
        type: "data",
        userUpload: false,
        address: address,
        uploaderId: this.userId,
        uploaderName: userInfo.name,
        privacy: "private",
      };

      let data = await post(
        "/GeoProblemSolving/rip/file/bind/" + this.aid,
        json
      );
      event.bind = true;
      this.$forceUpdate();
      this.$message({
        message: "You have bind to the resource center successfully!",
        type: "success",
      });
    },

    async bindAll() {},

    inEventList(state) {
      return state.Event.filter((value) => {
        // console.log(value);
        return value.type === "response";
      });
    },

    outEventList(state) {
      return state.Event.filter((value) => {
        return value.type === "noresponse";
      });
    },

    filterUdxNode(event) {
      if (event.datasetItem.hasOwnProperty("UdxDeclaration")) {
        if (event.datasetItem.UdxDeclaration[0].UdxNode != "") {
          if (
            event.datasetItem.UdxDeclaration[0].UdxNode[0].UdxNode[0].hasOwnProperty(
              "UdxNode"
            )
          ) {
            return false;
          } else {
            let udxNode = event.datasetItem.UdxDeclaration[0].UdxNode;
            return udxNode;
          }
        }
      }
    },

    loading() {
      this.fullscreenLoading = this.$loading({
        lock: true,
        text: "Calculating",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
      });
    },

    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
    sendInputParams: function (modelInEvent, stateIndex, index) {
      sendInputParams(modelInEvent, stateIndex);
    },

    // async getAllRecords() {
    //   let stepId = this.stepId;
    //   //根据用户ID查找所欲的modelInstance
    //   let data = await this.axios.get(
    //     `/GeoProblemSolving/modelItem/getAllRecords/${stepId}`
    //   );
    //   if (data.status == "200") {
    //     console.log("返回记录成功");
    //     // this.recordList = data.data.data;
    //     let recordList = data.data.data;
    //     console.log(recordList);
    //     for (let i = 0; i < recordList.length; i++) {
    //       let modelInstanceId = recordList[i].modelInstanceId;
    //       console.log(modelInstanceId);
    //       let item = await this.axios.get(
    //         `/GeoProblemSolving/modelItem/getModelInstance/${modelInstanceId}`
    //       );
    //       console.log(item);
    //       this.recordList.push(item);
    //     }
    //   } else {
    //     console.log("返回记录失败");
    //   }
    //   console.log(this.recordList);
    // },

    selectData(val) {
      let dataContainer = this.$store.state.DataServer;
      let stateIndex = this.stateList.findIndex(
        (state) => state.name == this.currentEvent.stateName
      );

      let eventIndex = this.stateList[stateIndex].Event.findIndex(
        (event) => event.name == this.currentEvent.name
      );
      this.$set(
        this.stateList[stateIndex].Event[eventIndex],
        "url",
        val.address
      );
      this.$set(
        this.stateList[stateIndex].Event[eventIndex],
        "urlName",
        val.name
      );
      //set input id
      this.$set(
        this.stateList[stateIndex].Event[eventIndex],
        "uid",
        val.uid
      );
      this.selectDataDialogShow = false;
      //将stateIndex 与 eventIndex
      let content = {
        stateIndex: stateIndex,
        eventIndex: eventIndex,
        url: val.address,
        urlName: val.name,
        uid: val.uid
      };

      selectDataOperation(content, "add");
    },
    selectDataDialog(event) {
      this.currentEvent = event;
      this.selectDataDialogShow = true;
    },
    remove(event, stateIndex) {
      event.url == "";
      event.urlName = "";
      let content = {
        stateIndex: stateIndex,
        eventId: event.eventId,
      };

      selectDataOperation(content, "remove");
    },
  },
  beforeDestroy() {
    clearInterval(this.timer);
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
  margin: 20px 0 10px 0;
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
</style>

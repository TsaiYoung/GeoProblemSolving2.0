<style scoped>

.tool-content {
  width: 85px;
  height: 85px;
  float: left;
  margin: 5px;
  cursor: pointer;
}
.modelToolBtn {
  height: 60px;
  width: 60px;
  float: left;
  padding: 0;
  color: #00c0ff;
}
.modelToolsetBtn {
  height: 60px;
  width: 60px;
  float: left;
  padding: 0;
  color: #19be6b;
}
.ellipsis {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: top;
}
.toolCard >>> .ivu-card-head {
  padding: 11px 16px;
}
.toolCard >>> .ivu-card-body {
  padding: 0 8px;
}
</style>
<template>
  <div>
    <Card dis-hover class="toolCard" >
      <div slot="title">
        <span style="font-weight: 700;">Tools</span>
        <Button
          shape="circle"
          size="small"
          icon="md-refresh"
          @click="refreshTool"
          title="Refresh Tool"
          style=" float:right; margin-top: -5px; margin-right: 15px;"
        ></Button>
        <manage-tools
          v-if="
            permissionIdentity(activityInfo.permission, userRole, 'manage_tool')
          "
          :activity-info="activityInfo"
          :tool-list="toolList"
          @updateStepTools="stepToolListChanged"
          title="Manage toolsets and tools"
          style="margin-top: -5px;margin-right: 10px;float:right;"
        ></manage-tools>
      </div>
      <div style="display: flex; justify-content: space-between">
        <div style="width: 100%">
          <div
            v-if="toolList != undefined && toolList.length < 1"
            style="text-align: center"
          >
            <h2 style="color: #808695">No tools</h2>
            <small style="color: #dcdee2" v-if="permissionIdentity(activityInfo.permission, userRole, 'manage_tool')"
              >*Click the button to add tools.</small
            >
            <small style="color: #dcdee2" v-else
              >*You do not have permission to manage tools.</small
            >
          </div>
          <vue-scroll :ops="ops" style="max-height: calc(50vh - 220px); padding-top: 5px" v-else>
            <Card
              class= "tool-content"
              v-show="toolsetLevel > 0"
            ><Icon
                type="md-arrow-back"
                size="70"
                style="margin-top:5px"
                title="Back to last level"
                color="#5089b8"
                @click="back2LastLevel()"
            /></Card>
            <div
              v-for="tool in toolList"
              :key="tool.tid"
            >
              <Card
                class= "tool-content"
                style=" background-color: #d3f1b1;"
                v-if="tool.toolSet"
              >
                <div
                  style="text-align: center; cursor: pointer"
                  @click="expandTool(tool)"
                >
                  <Tooltip placement="bottom" max-width="400" :transfer="true" content=tool.toolName>
                    <img
                      :src="tool.toolImg"
                      v-if="tool.toolImg != ''"
                      style="height: 100%; max-height: 50px"
                    />
                    <avatar
                      :username="tool.toolName"
                      :size="50"
                      style="margin-bottom: 6px"
                      v-else
                    ></avatar>
                    <div
                      slot="content"
                      v-if="
                        tool.description !== '' ||
                        (tool.tags != undefined && tool.tags.length > 0)
                      "
                    >
                      <span>{{ tool.description }}</span>
                      <template
                        v-if="tool.tags != undefined && tool.tags.length > 0"
                      >
                        <br />
                        <span>
                          <i>{{ tool.tags.join(" | ") }}</i>
                        </span>
                      </template>
                    </div>
                  </Tooltip>
                  <h4
                    :title="tool.toolName"
                    style="
                      display: block;
                      overflow: hidden;
                      white-space: nowrap;
                      text-overflow: ellipsis;
                    "
                  >
                    {{ tool.toolName }}
                  </h4>
                </div>
              </Card>
              <Card
                class= "tool-content" v-else
              >
                <div
                  style="text-align: center; cursor: pointer"
                  @click="useTool(tool)"
                >
                  <Tooltip placement="bottom" max-width="400" :transfer="true">
                    <img
                      :src="tool.toolImg"
                      v-if="tool.toolImg != '' && tool.toolImg != null"
                      style="height: 100%; max-height: 50px"
                    />
                    <avatar
                      :username="tool.toolName"
                      :size="50"
                      style="margin-bottom: 6px"
                      v-else
                    ></avatar>
                    <div
                      slot="content"
                      v-if="
                        tool.description !== '' ||
                        (tool.tags != undefined && tool.tags.length > 0)
                      "
                    >
                      <span>{{ tool.description }}</span>
                      <template
                        v-if="tool.tags != undefined && tool.tags.length > 0"
                      >
                        <br />
                        <span>
                          <i>{{ tool.tags.join(" | ") }}</i>
                        </span>
                      </template>
                    </div>
                  </Tooltip>
                  <h4
                    :title="tool.toolName"
                    style="
                      display: block;
                      overflow: hidden;
                      white-space: nowrap;
                      text-overflow: ellipsis;
                    "
                  >
                    {{ tool.toolName }}
                  </h4>
                </div>
              </Card>
            </div>
          </vue-scroll>
        </div>
      </div>
    </Card>
    <Modal
      v-model="jupyterModal"
      title="Open a jupyter notebook environment"
      @on-ok="checkJupyterUser"
      ok-text="Yes"
      cancel-text="Cancel"
    >
      <h3>
        Note: Jupyter notebooks will be accessed by all members in this project.
      </h3>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
    <!-- <Modal v-model="openToolModal" title="Open tool">
      <h2>How would you like to open this tool?</h2>
      <small style="color: #ff9900"
        >*Some tools are not supported to be used in page.</small
      >
      <div slot="footer" style="text-align: center">
        <Button @click="openTool" type="primary" style="margin: 0 15px"
          >This page</Button
        >
        <Button @click="openToolNewpage" style="margin: 0 15px"
          >New page</Button
        >
      </div>
    </Modal> -->
  </div>
</template>
<script>
import manageTools from "@/components/tools/toolToStepModal";
import Avatar from "vue-avatar";
import { get, del, post, put } from "@/axios";
import loginModal from "../../../user/userState/loginModal.vue";
export default {
  props: ["activityInfo","projectInfo"],
  components: {
    manageTools,
    Avatar,
    loginModal,
  },
  watch: {
    activityInfo(val) {
      // console.log(val);
      this.getAllTools();
    },
  },
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      userInfo: this.$store.getters.userInfo,
      userRole: "visitor",
      toolIdList: [],
      toolList: [],
      toolsetLevel: 0,
      lastLevelList: [],
      panelList: [],
      jupyterModal: false,
      selectedTool: {},
      operationStore: false,
      toolSetId: false,
      // openToolModal: false,
      //恢复登录的模态框
      tempLoginModal: false,
    };
  },
  beforeDestroy() {
    window.removeEventListener("message", this.toolMsgHandle, false);
    window.removeEventListener("message", getActivityInfo, false);
  },
  created() {
    this.roleIdentity();
    // thie.getProjectInfo();
  },
  mounted() {
    this.getAllTools();
    Date.prototype.Format = function (fmt) {
      var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S+": this.getMilliseconds(), //毫毛
      };
      if (/(y+)/.test(fmt))
        fmt = fmt.replace(
          RegExp.$1,
          (this.getFullYear() + "").substr(4 - RegExp.$1.length)
        );
      for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
          fmt = fmt.replace(
            RegExp.$1,
            RegExp.$1.length == 1
              ? o[k]
              : ("00" + o[k]).substr(("" + o[k]).length)
          );
      return fmt;
    };
  },
  methods: {
    roleIdentity() {
      this.userRole = this.userRoleApi.roleIdentify(
        this.activityInfo.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, role, operation) {
      return this.userRoleApi.permissionIdentity(
        JSON.parse(permission),
        role,
        operation
      );
    },
    // getProjectInfo(){
    //   let url = window.location.href;
    //   let result = {};
    //   if(url.indexOf("/activityInfo/") != -1){
    //     let urlStr = url.split("/activityInfo/");
    //     if(urlStr[1].indexOf("?") != -1){
    //       result.aid = urlStr[1].split("?")[0];
    //     }else{
    //       result.aid = urlStr[1];
    //     }
    //   }
    //   this.$axios.get("/GeoProblemSolving/project/" + result.aid)
    //     .then(res => {
    //       if(res.data.code == 0){
    //         this.projectInfo = res.data.data;
    //       } else {
    //         console.log(res.data.msg);
    //       }
    //     })
    //     .catch(err => {
    //       this.$Message.error("Loading project failed.")
    //     })
    // },

    changeLoginModal(status) {
      this.tempLoginModal = status;
      console.log(status);
      //刷新工具
      if( status == false){
        this.back2LastLevel();
      }
    },
    refreshTool() {
      this.toolsetLevel = 0;
      this.lastLevelList = [];

      //重新获取this.activityInfo.toolList
      let aid = this.activityInfo.aid;
      let level = this.activityInfo.level;
      let url = "";
      if (level == 0 || level == null) {
        url = "/GeoProblemSolving/project/" + aid;
      } else if (level == 1) {
        url = "/GeoProblemSolving/subproject/" + aid;
      } else if (level > 1) {
        url = "/GeoProblemSolving/activity/" + aid;
      }
      this.axios
        .get(url)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.toolIdList = res.data.data.toolList;
            if (this.toolIdList != undefined && this.toolIdList.length !== 0) {
              this.getToolInfos();
            }
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },

    getAllTools() {
      this.toolIdList = this.activityInfo.toolList;
      if (this.toolIdList != undefined && this.toolIdList.length !== 0) {
        this.getToolInfos();
      }
    },
    async getToolInfos() {
      // let data = await get("/GeoProblemSolving/tool/all/" + this.toolIdList.toString())
      this.axios
        .get(
          "/GeoProblemSolving/tool/all/" +
            this.toolIdList.toString()
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else {
            let data = res.data.data;
            this.$set(this, "toolList", data);
          }
        })
        .catch((err) => {});
    },
    stepToolListChanged(tools, toolsets) {
      this.toolIdList = toolsets;
      this.toolIdList.push.apply(this.toolIdList, tools);
      this.getToolInfos();
    },
    // showTools(toolsetInfo) {
    //   this.toolsetToolList = toolsetInfo.toolList;
    //   this.showToolsetToolsModal = true;
    // },
    useTool(toolInfo) {
      this.selectedTool = toolInfo;
      this.openTool();
      // this.openToolModal = true;
    },
    back2LastLevel() {
      this.toolList = Object.assign([], this.lastLevelList);
      this.toolsetLevel--;
    },
    expandTool(toolset) {
      this.lastLevelList = Object.assign([], this.toolList);
      this.toolIdList = toolset.toolList;
      this.getToolInfos();
      this.toolsetLevel++;
      this.toolSetId = toolset.tid;
    },
    openTool() {
      let toolInfo = this.selectedTool;
      // this.openToolModal = false;

      if (toolInfo.scope == "outer") {
        this.openToolNewpage(toolInfo);
      } else if (toolInfo.scope == "inner") {
        this.openToolByPanel(toolInfo);
      } else {
        this.openToolByPanel(toolInfo);
      }
    },
    openToolByPanel(toolInfo) {
      //判断tool的类型，三种 modelItem（模型容器提供计算能力）, dataMethod(数据容器提供计算能力), webTool(自行开发，如mapTool)
      let routerUrl = toolInfo.toolUrl;
      if (toolInfo.backendType == "webTool") {
        routerUrl = toolInfo.toolUrl;
        if(toolInfo.toolUrl.indexOf("GeoProblemSolving") == -1 && toolInfo.toolUrl.indexOf("GeoProblemSolving") == -1){
          if(window.location.host.indexOf("geomodeling") != -1){
            routerUrl = "/GeoProblemSolving" + toolInfo.toolUrl
          } else if(window.location.host.indexOf("geofuturelab") != -1){
            routerUrl = "/GeoProblemSolving" + toolInfo.toolUrl
          }
        }
      } else if (toolInfo.backendType == "modelItem") {
        routerUrl = "/GeoProblemSolving/computeModel";
      } else if (toolInfo.backendType == "dataMethod") {
        routerUrl = "/GeoProblemSolving/dataMethod";
      }

      var toolContent = `<iframe src="${routerUrl}" id="${toolInfo.tid}" style="width: 100%; height:100%;" frameborder="0"></iframe>`;

      try {
        var panel = jsPanel.create({
        theme: "success",
        footerToolbar: `<p></p>`,
        contentSize: "800 400",
        id: toolInfo.toolName.replace(/ /g, '-'),
        headerTitle: toolInfo.toolName,
        content: toolContent,
        container: "div#drawflow",
        dragit: {
          containment: 5,
        },
        closeOnEscape: true,
        disableOnMaximized: true,
        onbeforeclose: () => {
          if (this.operationStore) {
            return true;
          } else {
            return confirm(
              "Operations has not been bind to tasks. Close panel immediately?"
            );
          }
        },
      });
      $(".jsPanel-content").css("font-size", "0");
      this.panelList.push(panel);//
      this.$emit("toolPanel", panel);//

      // 设置iframe 父子页面消息传输处理
      window.addEventListener("message", this.toolMsgHandle, false);
      let activity = this.activityInfo;
      let userInfo = this.userInfo;
      let taskList = this.operationApi.getTaskList();
      let iFrame = document.getElementById(toolInfo.tid);
      //iframe加载完毕后再发送消息，否则子页面接收不到message
      iFrame.onload = function () {
        //iframe加载完立即发送一条消息
        iFrame.contentWindow.postMessage(
          {
            user: userInfo,
            activity: activity,
            tasks: taskList,
            tid: toolInfo.tid,
            type: "activity",
          },
          "*"
        );
      };
      }catch(err) {
        this.$Notice.info({
          title: "Wrong url.",
          desc:"The tool's url is wrong. "
        });
      }
    },

    toolMsgHandle(event) {
      if (event.data.type === "task") {
        let operations = event.data.operations;
        let behavior = event.data.behavior;
        for (let i = 0; i < operations.length; i++) {
          let operationType = operations[i].type;
          switch (operationType) {
            case "resource": {
              if (behavior === "record") {
                this.operationApi.resOperationRecord(
                  this.activityInfo.aid,
                  operations[i].id,
                  event.data.task,
                  behavior,
                  this.userInfo.userId,
                  operations[i].content
                );
                // this.$store.commit("updateTempOperations", {
                //   behavior: "add",
                //   operation: operations[i],
                // });
              } else if (behavior === "bind") {
                this.operationApi.bindTempOperation2Task(
                  this.activityInfo.aid,
                  operations[i].id,
                  event.data.task
                );
                // this.$store.commit("updateTempOperations", {
                //   behavior: "remove",
                //   operation: operations[i],
                // });
              }
              break;
            }
            case "communication": {
              break;
            }
            case "geo-analysis": {
              if (behavior === "record") {
                this.operationApi.analysisRecord(
                  this.activityInfo.aid,
                  operations[i].id,
                  event.data.task,
                  this.userInfo.userId,
                  operations[i].toolId,
                  operations[i].purpose,
                  operations[i].inputs,
                  operations[i].outputs,
                  operations[i].params,
                  operations[i].participants
                );
              } else if (behavior === "bind") {
                this.operationApi.bindTempOperation2Task(
                  this.activityInfo.aid,
                  operations[i].id,
                  event.data.task
                );
              }
              break;
            }
          }

          if (behavior === "record") {
            this.$store.commit("updateTempOperations", {
              behavior: "add",
              operation: operations[i],
            });
          } else if (behavior === "bind") {
            this.$store.commit("updateTempOperations", {
              behavior: "remove",
              operation: operations[i],
            });
            this.operationStore = true;
          }
        }
      }else if (event.data.type === "task-record-backend"){
        this.operationApi.getActivityDoc(this.activityInfo.aid);
        let operation = this.operationApi.getOperationInfo(event.data.oid);
        this.$store.commit("updateTempOperations", {
          behavior: "add",
          operation: operation,
        });
      }
    },
    openToolNewpage(toolInfo) {
      // this.openToolModal = false;

      if (toolInfo.toolName == "Jupyter notebook") {
        this.jupyterModal = true;
        return;
      }

      //要发送的参数
      let params = {
        userName: this.userInfo.name,
        userId: this.userInfo.userId,
        groupID: this.activityInfo.aid,
      };
      var toolURL = `${toolInfo.toolUrl}`;
      window["pageParams"] = params;

      // open
      window.open(toolURL);
    },
    checkJupyterUser() {
      this.axios
        .get(
          "/GeoProblemSolving/jupyter/inquiry?projectId=" +
            this.projectInfo.aid
        )
        .then((res) => {
          if (res.data == "None") {
            this.prepareJupyter();
          } else {
            this.jupyterLogin(res.data[0].jupyterUserId);
          }
        })
        .catch((err) => {});
    },
    jupyterLogin(jupyterUserId) {
      let jupyterUrl = "";
      if (this.$store.state.IP_Port == "localhost:8080") {
        jupyterUrl = "http://172.21.212.83";
      } else if (
        this.$store.state.IP_Port == "118.190.246.198:80" ||
        this.$store.state.IP_Port == "www.geofuturelab.com"
      ) {
        jupyterUrl = "http://118.190.246.198:8000";
      }

      let loginInfo = {
        JupyterUser: jupyterUserId,
        userId: this.userInfo.userId,
        projectId: this.projectInfo.aid,
      };
      let info = JSON.stringify(loginInfo);

      let data = new FormData();
      data.append("login-info", info);
      this.axios
        .post(jupyterUrl + "/hub/login?next=/hub/user/" + jupyterUserId, data)
        .then((res) => {
          let url = jupyterUrl + "/hub/user/" + jupyterUserId;
          window.open(url);
        })
        .catch((err) => {
          let url = jupyterUrl + "/hub/user/" + jupyterUserId;
          window.open(url);
        });
    },
    prepareJupyter() {
      let jupyterUrl = "";
      if (this.$store.state.IP_Port == "localhost:8080") {
        jupyterUrl = "http://172.21.212.83";
      } else if (
        this.$store.state.IP_Port == "118.190.246.198:80" ||
        this.$store.state.IP_Port == "www.geofuturelab.com"
      ) {
        jupyterUrl = "http://118.190.246.198:8000";
      }

      let name_jupyterhub = this.projectInfo.aid;
      name_jupyterhub = name_jupyterhub.replace(/[-]/g, "");

      this.axios
        .post(
          jupyterUrl + "/hub/api/users/" + name_jupyterhub,
          { name: name_jupyterhub },
          {
            headers: {
              Authorization: "token 50e3fa2f34c74d36b09e967733a621b0",
            },
          }
        )
        .then((res) => {
          this.createJupyterUser(name_jupyterhub);
        })
        .catch((err) => {
          throw err;
        });
    },
    createJupyterUser(name_jupyterhub) {
      let data = {
        projectId: this.projectInfo.aid,
        jupyterUserId: name_jupyterhub,
      };
      this.axios
        .post("/GeoProblemSolving/jupyter/create", data)
        .then((res) => {
          if (res.data == "Success") {
            this.$Notice.info({
              desc: "Create Jupyter notebook successfully. It can be used now.",
            });
          }
        })
        .catch((err) => {});
    },
  },
};
</script>

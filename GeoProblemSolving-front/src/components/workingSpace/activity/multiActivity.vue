<template>
  <div>
    <Menu
      mode="horizontal"
      :active-name="activeMenu"
      style="height: 38px; line-height: 38px;"
      @on-select="changeMenuItem"
    >
      <MenuItem name="Introduction">
        <Icon type="ios-paper"/>
        Introduction
      </MenuItem>
      <MenuItem name="Tasks">
        <Icon type="ios-construct"/>
        Task
      </MenuItem>
      <MenuItem name="Activities">
        <Icon type="logo-steam"/>
        Pathway
      </MenuItem>
      <MenuItem name="Resources">
        <Icon type="ios-albums"/>
        Resource
      </MenuItem>
    </Menu>
    <div v-show="activeMenu=='Introduction'">
      <activity-show
        :activityInfo="activityInfo"
        :nameConfirm="nameConfirm"
        :userInfo="userInfo"
        :projectInfo="projectInfo"
        v-on:enterActivity="enterActivity"
        v-on:enterRootActivity="enterRootActivity"
      ></activity-show>
    </div>
    <div v-show="activeMenu=='Resources'">
      <folder-tree
        style="margin-top: 20px"
        :activityInfo="activityInfo"
        @toolPanel="listenToolPanel"
      ></folder-tree>
    </div>
    <div v-show="activeMenu=='Tasks'">
      <task-manager :activityInfo="activityInfo" :projectInfo="projectInfo" :childActivities="childActivities"
                    :userInfo="userInfo" ref="task"></task-manager>
    </div>
    <div v-show="activeMenu=='Activities'">
      <process-manager :activityInfo="activityInfo" :childActivities="childActivities" :projectInfo="projectInfo"
                       :userInfo="userInfo" ref="processLink"></process-manager>
    </div>
    <mini-chatroom
      :activityInfo="activityInfo"
    ></mini-chatroom>
  </div>
</template>
<script>
  import activityShow from "./activityShow.vue";
  import folderTree from "../../resources/folderTree";
  import taskManager from "./utils/taskManger.vue";
  import processManager from "./utils/processManager.vue";
  import miniChatroom from "./utils/miniChatroom";
  import * as socketApi from "../../../api/socket";

  export default {
    props: [
      "activityInfo",
      "userInfo",
      "projectInfo",
      "childActivities",
      "nameConfirm"
    ],
    components: {
      activityShow,
      folderTree,
      taskManager,
      processManager,
      miniChatroom
    },
    data() {
      return {
        activeMenu: "Introduction",
        userRole: "visitor",
        processSocketId: "",
        taskSocketId: "",
        lastMenuName: "Introduction"
      };
    },
    mounted() {
      //load activity xml
      this.operationApi.getActivityDoc(this.activityInfo.aid);
    },
    beforeDestroy() {
      this.closeTaskSocket();
    },
    watch: {
      activityInfo: {
        immediate: true,
        handler(){
          this.taskSocketId = `OperationServer/task${this.projectInfo.aid}/${this.activityInfo.aid}`;
          this.processSocketId = `OperationServer/process${this.projectInfo.aid}/${this.activityInfo.aid}`;
        }
      }
    },
    methods: {
      enterActivity(activity) {
        this.$emit('enterActivity', activity);
      },
      enterRootActivity() {
        this.$emit('enterRootActivity');
      },
      listenToolPanel(data) {
        this.panel = data;
      },
      changeMenuItem(name) {
        //Changing the show component
        this.activeMenu = name;
        if (this.lastMenuName == "Tasks"){
          this.closeTaskSocket(this.taskSocketId);
        }else if (this.lastMenuName === "Activities"){
          this.closeTaskSocket(this.processSocketId);
          //Clean the component's parameters.
          this.$refs.processLink.collaboratingInfoList = [];
          this.$refs.processLink.collaboratingId = [];
          this.$refs.processLink.collaIndex = -1;
          //Maybe the problem occurred in here.
          this.$refs.processLink.participants = [];
          this.$refs.processLink.collLinkUser = [];
        }
        //Connecting with socket.
        if (name === "Tasks"){
          //Send the participants info in here.
          this.initTaskSocket(this.taskSocketId, name);
        }else if (name === "Activities"){
          this.initTaskSocket(this.processSocketId, name)
        }
        this.lastMenuName = name;
      },

      initTaskSocket: function (socketId, name) {
        let linkStatus = socketApi.getSocketInfo(socketId).linked;
        if (linkStatus !== undefined && linkStatus) {
          console.log(`${this.projectInfo.name}: ${this.activityInfo.name} has connected.`);
          return;
        }
        socketApi.initWebSocket(socketId);
        let sockMsg = {
          type: "test",
          sender: this.userInfo.userId
        };
        if (name == "Activities") {
          socketApi.sendSock(socketId, sockMsg, this.$refs.processLink.socketOnMessage);
          let inProcessLinkSock = {
            sender: this.userInfo.userId,
            senderName: this.userInfo.name,
            type: "general",
            content: {
              behavior: "inActivities"
            }
          };
          //进入 Activities 页面
          socketApi.sendSock(socketId, inProcessLinkSock, this.$refs.processLink.socketOnMessage);

        } else if (name == "Tasks") {
          socketApi.sendSock(socketId, sockMsg, this.$refs.task.socketOnMessage);
        }
      },
      closeTaskSocket(socketId) {
        if (socketApi.getSocketInfo(socketId).linked) {
          socketApi.close(socketId);
          if (!socketApi.getSocketInfo(socketId).linked) {
            console.log("Closed successfully");
          }
        }
      },
    },
  };
</script>
<style scoped>
</style>

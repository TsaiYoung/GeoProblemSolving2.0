<template>
  <div>
    <div style="display: flex; background-color: #f8f8f9">
      <div
        style="flex: 1; border-left: 1px solid #dcdee2; background-color: white"
      >
        <Menu
          mode="horizontal"
          :active-name="activeMenu"
          style="height: 38px; line-height: 38px; z-index: 1"
          @on-select="changeMenuItem"
        >
          <!-- <MenuItem name="Workspace">
            <Icon type="ios-globe" />Workspace
          </MenuItem>
          <MenuItem name="Task">
            <Icon type="ios-git-network"/>Task management
          </MenuItem> -->
          <MenuItem name="Workflow">
            <Icon type="ios-git-network"/>Workflow
          </MenuItem>
          <!-- <MenuItem name="Introduction">
            <Icon type="ios-paper" />About
          </MenuItem> -->
        </Menu>

        <!-- <div v-show="activeMenu == 'Introduction'">
          <activity-show :activityInfo="activityInfo"></activity-show>
        </div> -->
        <!-- <div v-show="activeMenu == 'Workspace'">
          <vue-scroll
            :ops="scrollOps"
            style="height: calc(100vh - 225px); margin-top: 5px"
          >
            <universal-space
              :activityInfo="activityInfo"
              :participants="participants"
              :projectInfo="projectInfo"
            ></universal-space>
          </vue-scroll>
        </div>
        <div v-show="activeMenu == 'Task'">
          <task-manager :activityInfo="activityInfo" :userInfo="userInfo" :projectInfo="projectInfo" ref="task"></task-manager>
        </div> -->
        <div v-show="activeMenu == 'Workflow'">
          <new-workflow :activityInfo="activityInfo" :userInfo="userInfo" :projectInfo="projectInfo" ref="workflow"></new-workflow>
        </div>
      </div>
    </div>
    <Modal
      v-model="inviteModal"
      width="660px"
      title="Invite new members"
      @on-ok="inviteMembers"
      ok-text="Ok"
      cancel-text="Cancel"
    >
      <Transfer
        :data="potentialMembers"
        :target-keys="invitingMembers"
        :list-style="listStyle"
        :render-format="memberRender"
        :titles="['Members of the parent activity', 'Members of this activity']"
        filter-placeholder="Enter key words..."
        filterable
        :filter-method="filterMethod"
        @on-change="handleChange"
      ></Transfer>
    </Modal>
    <Modal
      v-model="removeMemberModal"
      width="400px"
      title="Remove this member"
      @on-ok="removeUser()"
      ok-text="Ok"
      cancel-text="Cancel"
    >
      <h4 style="color: red">Are you sure to remove this member?</h4>
    </Modal>
    <Modal
      v-model="memberRoleModal"
      width="400px"
      title="Set the role of this member"
      @on-ok="updateUserRole()"
      ok-text="Ok"
      cancel-text="Cancel"
    >
      <h4>Select user role</h4>
      <Select v-model="roleSelected" text="Role" style="margin: 10px 0">
        <Option value="manager" v-show="roleCompare(userRole, 'manager') != -1"
          >Manager</Option
        >
        <Divider style="margin: 5px 0"></Divider>
        <Option value="core" disabled>Core team</Option>
        <Option
          value="researcher"
          v-show="roleCompare(userRole, 'researcher') != -1"
          >Researcher</Option
        >
        <Option value="expert" v-show="roleCompare(userRole, 'expert') != -1"
          >Expert</Option
        >
        <Option
          value="decision-maker"
          v-show="roleCompare(userRole, 'decision-maker') != -1"
          >Decision-maker</Option
        >
        <Option
          value="core-member"
          v-show="roleCompare(userRole, 'core-member') != -1"
          >Core-member</Option
        >
        <Divider style="margin: 5px 0"></Divider>
        <Option value="ordinary" disabled>Ordinary team</Option>
        <Option
          value="stakeholder"
          v-show="roleCompare(userRole, 'stakeholder') != -1"
          >Stakeholder</Option
        >
        <Option
          value="consultant"
          v-show="roleCompare(userRole, 'consultant') != -1"
          >Consultant</Option
        >
        <Option
          value="ordinary-member"
          v-show="roleCompare(userRole, 'ordinary-member') != -1"
          >Ordinary-member</Option
        >
      </Select>
    </Modal>
    <Modal
      v-model="quitActivityModal"
      width="400px"
      title="Leave this activity"
      @on-ok="leaveActivity()"
      ok-text="Ok"
      cancel-text="Cancel"
    >
      <h4 style="color: red">Are you sure to quit this subproject?</h4>
    </Modal>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
import { get, del, post, put } from "../../../axios";
// import activityShow from "./activityShow.vue";
import taskManager from "./utils/taskManger.vue";
import universalSpace from "./funcs/universalSpace.vue";
import newWorkflow from "./utils/newWorkflow.vue";
import contextRes from "./funcs/contextAndResource.vue";
import dataProcessing from "./funcs/dataProcessing.vue";
import dataVisual from "./funcs/dataVisualization.vue";
import modelBuild from "./funcs/modelBuild.vue";
import modelEvaluation from "./funcs/modelEvaluation.vue";
import geoSimulation from "./funcs/geoSimulation.vue";
import geoAnalysis from "./funcs/geoAnalysis.vue";
import decisionMaking from "./funcs/decisionMaking.vue";
import * as socketApi from "../../../api/socket";
export default {
  props: ["activityInfo","userInfo","projectInfo"],
  components: {
    Avatar,
    // activityShow,
    taskManager,
    universalSpace,
    newWorkflow,
    contextRes,
    dataProcessing,
    dataVisual,
    modelBuild,
    modelEvaluation,
    geoSimulation,
    geoAnalysis,
    decisionMaking,
  },
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      activeMenu: "Workspace",
      // userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
      userRole: "visitor",
      participants: [],
      // add
      potentialMembers: [],
      invitingMembers: [],
      inviteModal: false,
      // remove
      delUserBtn: false,
      slctDletMember: {},
      removeMemberModal: false,
      // leave
      quitActivityModal: false,
      listStyle: { width: "280px", height: "360px" },
      // update
      slctRoleMember: {},
      memberRoleModal: false,
      userRoleBtn: false,
      roleSelected: "",
      taskSocketId: "OperationServer/workflow"
    };
  },
  created() {
    this.userRole = this.roleIdentity(this.activityInfo);
    this.getParticipants();
  },
  mounted() {
    this.operationApi.getActivityDoc(this.activityInfo.aid);
    this.startSocket();
  },
  watch: {
    activityInfo: {
      immediate: true,
      handler(){
          this.taskSocketId = `OperationServer/workflow${this.projectInfo.aid}/${this.activityInfo.aid}`;
      }
    }
  },
  beforeDestroy() {
    this.closeTaskSocket();
  },
  methods: {
    startSocket(){
      let that = this;
      setTimeout(function(){
        that.initTaskSocket();
      },3000);
    },
    roleIdentity(activity) {
      return this.userRoleApi.roleIdentify(
        activity.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, role, operation) {
      if (permission == undefined)
        permission = JSON.stringify(this.userRoleApi.getDefault());
      if (operation == "auto_join") {
        if (JSON.parse(permission).auto_join.visitor == "Yes") return true;
        else if (JSON.parse(permission).auto_join.visitor == "No") return false;
        else {
          return this.getParentPermission();
        }
      } else {
        return this.userRoleApi.permissionIdentity(
          JSON.parse(permission),
          role,
          operation
        );
      }
    },
    roleCompare(role1, role2) {
      return this.userRoleApi.roleCompare(role1, role2);
    },
    async preInvitation() {
      let url = "";
      let activity = this.activityInfo;
      if (activity.level == 1) {
        url = "/GeoProblemSolving/project/" + activity.parent + "/user";
      } else if (activity.level == 2) {
        url = "/GeoProblemSolving/subproject/" + activity.parent + "/user";
      } else if (activity.level > 2) {
        url = "/GeoProblemSolving/activity/" + activity.parent + "/user";
      } else {
        return;
      }
      let data = await get(url);

      this.potentialMembers = [];
      this.invitingMembers = [];
      let candidates = data.members;
      for (let i = 0; i < candidates.length; i++) {
        this.potentialMembers.push({
          key: this.potentialMembers.length,
          userId: candidates[i].userId,
          name: candidates[i].name,
          role: candidates[i].role,
          domain: candidates[i].domain,
          disabled: this.participants.contains(candidates[i]),
        });
      }
      this.invitingMembers = this.getTargetKeys();
      this.inviteModal = true;
    },
    getTargetKeys() {
      return this.potentialMembers
        .filter((item) => {
          return item.disabled;
        })
        .map((item) => item.key);
    },
    memberRender(item) {
      return `<span title="${item.name} - ${item.role}">${item.name} - ${item.role}</span>`;
    },
    filterMethod(data, query) {
      return data.name.indexOf(query) > -1;
    },
    handleChange(newTargetKeys) {
      this.invitingMembers = newTargetKeys;
    },
    inviteMembers() {
      let activity = this.activityInfo;

      // add members
      for (var i = 0; i < this.invitingMembers.length; i++) {
        let index = this.invitingMembers[i];
        if (this.participants.contains(this.potentialMembers[index])) continue;

        let user = this.potentialMembers[index];
        let url = "";
        if (activity.level == 1) {
          url =
            "/GeoProblemSolving/subproject/" +
            activity.aid +
            "/user?userId=" +
            user.userId;
        } else if (activity.level > 1) {
          url =
            "/GeoProblemSolving/activity/" +
            activity.aid +
            "/user?userId=" +
            user.userId;
        } else {
          return;
        }

        this.axios
          .post(url)
          .then((res) => {
            if (res.data.code == 0) {
              this.participants.push(user);
              // update actvitiy doc
              // this.operationApi.participantUpdate(
              //   this.activityInfo.aid,
              //   "join",
              //   user.userId,
              //   user.name,
              //   "ordinary-member",
              //   user.domain
              // );
              //notice
              let notice = {
                recipientId: user.userId,
                type: "notice",
                content: {
                  title: "Join activity",
                  description:
                    "You have been invited by " +
                    this.userInfo.name +
                    " to join the activity: " +
                    activity.name +
                    " in project: " +
                    this.projectInfo.name +
                    " , and now you are a member in this activity!",
                  approve: "unknow",
                  projectId: this.projectInfo.aid,
                  projectName: this.projectInfo.name,
                  activityId: activity.aid,
                  activityName: activity.name,
                  activityLevel: activity.level,
                  invitorName: this.userInfo.name,
                  invitorId: this.userInfo.userId,
                },
              };
              this.sendNotice(notice);
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });
      }
    },
    updateUserRole() {
      let member = this.slctRoleMember;
      let activity = this.activityInfo;
      let role = this.roleSelected;

      // get url
      let url = "";
      if (activity.level == 0) {
        url =
          "/GeoProblemSolving/project/" +
          activity.aid +
          "/user?userId=" +
          member.userId +
          "&role=" +
          role;
      } else if (activity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          activity.aid +
          "/user?userId=" +
          member.userId +
          "&role=" +
          role;
      } else if (activity.level > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          activity.aid +
          "/user?userId=" +
          member.userId +
          "&role=" +
          role;
      } else {
        return;
      }

      this.axios
        .put(url)
        .then((res) => {
          if (res.data.code == 0) {
            this.$Notice.info({ desc: "Change the member role successfully" });
            this.operationApi.participantUpdate(
              this.activityInfo.aid,
              "role",
              member.userId,
              member.name,
              role,
              user.domain
            );
            this.getParticipants();

            //notice
            let notice = {
              recipientId: member.userId,
              type: "notice",
              content: {
                title: "Role changed",
                description:
                  "Your role in the activity: " +
                  activity.name +
                  ", project: " +
                  this.projectInfo.name +
                  " has changed to " +
                  role +
                  ".",
                approve: "unknow",
                projectId: this.projectInfo.aid,
                projectName: this.projectInfo.name,
                activityId: activity.aid,
                activityName: activity.name,
                activityLevel: activity.level,
                // removerName: this.userInfo.name,
                // removerId: this.userInfo.userId,
              },
            };
            this.sendNotice(notice);
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    selectMember(member) {
      if (operation == "delete") {
        this.slctDletMember = member;
        this.removeMemberModal = true;
      } else if (operation == "role") {
        this.slctRoleMember = member;
        this.memberRoleModal = true;
        this.roleSelected = member.role;
      }
    },
    removeUser() {
      let url = "";
      let member = this.slctDletMember;
      let activity = this.activityInfo;
      if (activity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          activity.aid +
          "/user?userId=" +
          member.userId;
      } else if (activity.level > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          activity.aid +
          "/user?userId=" +
          member.userId;
      } else {
        return;
      }

      this.axios
        .delete(url)
        .then((res) => {
          if (res.data.code == 0) {
            this.operationApi.getActivityDoc(this.activityInfo.aid)
            this.operationApi.participantUpdate(
              this.activityInfo.aid,
              "remove",
              member.userId,
              member.name,
              member.role,
              user.domain
            );

            let index = this.participants.indexOf(member);
            this.participants.splice(index, 1);
            //notice
            let notice = {
              recipientId: member.userId,
              type: "notice",
              content: {
                title: "Leave activity",
                description:
                  "You have been remove from the activity: " +
                  activity.name +
                  " in project: " +
                  this.projectInfo.name +
                  ".",
                approve: "unknow",
                projectId: this.projectInfo.aid,
                projectName: this.projectInfo.name,
                activityId: activity.aid,
                activityName: activity.name,
                activityLevel: activity.level,
                removerName: this.userInfo.name,
                removerId: this.userInfo.userId,
              },
            };
            this.sendNotice(notice);
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    leaveActivity() {
      let member = this.userInfo;
      let activity = this.activityInfo;
      // get url
      let url = "";
      if (activity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          activity.aid +
          "/user?userId=" +
          member.userId;
      } else if (activity.level > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          activity.aid +
          "/user?userId=" +
          member.userId;
      } else {
        return;
      }

      this.axios
        .delete(url)
        .then((res) => {
          if (res.data.code == 0) {
            // update activity doc
            this.operationApi.participantUpdate(
              activity.aid,
              "remove",
              member.userId,
              "",
              "",
              []
            );

            //notice
            let managers = this.userRoleApi.getMemberByRole(
              activity,
              "manager"
            );
            for (var i = 0; i < managers.length; i++) {
              let notice = {
                recipientId: managers[i],
                type: "notice",
                content: {
                  title: "Leave activity",
                  description:
                    member.name +
                    " left the activity: " +
                    activity.name +
                    " in project: " +
                    this.projectInfo.name +
                    ".",
                  approve: "unknow",
                  projectId: this.projectInfo.aid,
                  projectName: this.projectInfo.name,
                  activityId: activity.aid,
                  activityName: activity.name,
                  activityLevel: activity.level,
                  leaverName: this.userInfo.name,
                  leaverId: this.userInfo.userId,
                },
              };
              this.sendNotice(notice);

              window.location.href =
                "/GeoProblemSolving/projectInfo/" +
                this.projectInfo.aid +
                "?aid=" +
                activity.parent +
                "&level=" +
                (activity.level - 1).toString();
            }
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    sendNotice(notice) {
      this.axios
        .post("/GeoProblemSolving/notice/save", notice)
        .then((result) => {
          if (result.data == "Success") {
            this.$emit("sendNotice", notice);
          } else {
            this.$Message.error("Notice fail.");
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    gotoPersonalSpace(id) {
      if (id == this.$store.getters.userId) {
        this.$router.push({ name: "overView" });
      } else {
        this.$router.push({ name: "MemberDetailPage", params: { id: id } });
      }
    },
    changeMenuItem(name) {
      this.activeMenu = name;
    //   if (name == "Workflow"){
    //     this.initTaskSocket();
    //   }else {
    //     this.closeTaskSocket();
    //   }
    },
    getParticipants() {
      let url = "";
      let activity = this.activityInfo;
      if (activity.level == 0) {
        url = "/GeoProblemSolving/project/" + activity.aid + "/user";
      } else if (activity.level == 1) {
        url = "/GeoProblemSolving/subproject/" + activity.aid + "/user";
      } else if (activity.level > 1) {
        url = "/GeoProblemSolving/activity/" + activity.aid + "/user";
      }
      //callback setTimeBack
      this.$axios
        .get(url)
        .then((res) => {
          if (res.data.code == 0) {
            this.creatorInfo = res.data.data.creator;
            this.participants = res.data.data.members;
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    initTaskSocket(){
      if (!socketApi.getSocketInfo(this.taskSocketId).linked){
        socketApi.initWebSocket(this.taskSocketId);
        let sockMsg = {
          type: "test",
          sender: this.userInfo.userId
        };
        socketApi.sendSock(this.taskSocketId, sockMsg, this.$refs.workflow.socketOnMessage);
      }else {
        console.log(`workflow${this.projectInfo.name}: ${this.activityInfo.name}task has connected.`);
      }
    },
    closeTaskSocket(){
      if (socketApi.getSocketInfo(this.taskSocketId).linked){
        socketApi.close(this.taskSocketId);
      }
    },
  },
};
</script>
<style scoped>
.memberImg {
  width: 40px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.memberName {
  height: 20px;
  padding: 0px 10px;
  width: 100%;
}
.memberDetail {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
.memberRole {
  height: 20px;
  padding: 0px 10px;
  width: 100%;
}
.memberName span {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.memberRole span {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
</style>

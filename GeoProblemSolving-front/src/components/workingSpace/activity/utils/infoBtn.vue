<template>
  <div>
    <Button icon="ios-paper" @click="infoModal = true">Introduction</Button>
    <Modal
      class="info"
      v-model="infoModal"
      width="900px"
      title="Activity information"
      ok-text="OK"
      cancel-text="Cancel"
    >
      <div style="padding: 5px 10px">
        <div style="width: 70%; float: left; padding: 20px">
          <vue-scroll :ops="scrollOps" style="margin-top: 10px; height: 440px">
            <div>
              <div>
                <h2>{{ activityInfo.name }}</h2>
              </div>
              <div style="heigth: 50%; width: 50%; float: left">
                <div style="margin: 10px 0">
                  <Label>Purpose:</Label>
                  <span style="margin-left: 10px">{{
                    activityInfo.purpose
                  }}</span>
                </div>
                <div style="margin: 10px 0">
                  <Label>Total participants:</Label>
                  <span style="margin-left: 10px">{{
                    participants.length
                  }}</span>
                </div>
                <div style="margin: 10px 0">
                  <Label>Last active time:</Label>
                  <span style="margin-left: 10px">{{
                    activityInfo.activeTime.split(" ")[0]
                  }}</span>
                </div>
              </div>
              <div style="heigth: 50%; width: 50%; float: left">
                <div style="margin: 10px 0">
                  <Label>Creator:</Label>
                  <span
                    style="margin-left: 10px; cursor: pointer; color: #2d8cf0"
                    @click="gotoPersonalSpace(creatorInfo.userId)"
                    >{{ creatorInfo.name }}
                  </span>
                </div>
                <div
                  style="margin: 10px 0"
                  v-if="
                    permissionIdentity(
                      activityInfo.permission,
                      userRole,
                      'manage_resource'
                    )
                  "
                >
                  <Label>Permission:</Label>
                  <span
                    style="margin-left: 10px; cursor: pointer; color: #2d8cf0"
                    @click="modifyPermission"
                  >
                    <Icon type="ios-create" />
                  </span>
                </div>
              </div>
            </div>
            <Divider style="margin: 10px 0"></Divider>
            <div>
              <h3 style="margin: 8px 0">Description</h3>
              <div
                style="
                  float: left;
                  min-height: 40px;
                  max-height: 300px;
                  overflow-y: auto;
                  word-break: break-word;
                  margin-left: 10px;
                "
              >
                {{ activityInfo.description }}
              </div>
            </div>
          </vue-scroll>
        </div>
        <div
          style="
            width: 30%;
            float: left;
            padding: 0 20px;
            margin: 20px 0;
            border-left: 1px solid lightgray;
          "
        >
          <div>
            <span style="font-size: 1.17em; font-weight: bold"
              >Participants</span
            >
            <Icon
              v-if="
                activityInfo.level > 0 &&
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'manage_member'
                )
              "
              type="md-trash"
              size="16"
              title="Remove users"
              @click="delUserBtn = !delUserBtn"
              style="
                float: right;
                margin: 5px 10px 0 0;
                cursor: pointer;
                color: #ed4014;
              "
            />
            <Icon
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'manage_member'
                )
              "
              type="md-people"
              size="16"
              title="Change user role"
              @click="userRoleBtn = !userRoleBtn"
              style="
                float: right;
                margin: 5px 10px 0 0;
                cursor: pointer;
                color: #8bc34a;
              "
            />
            <Icon
              v-if="
                activityInfo.level > 0 &&
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'invite_member'
                )
              "
              type="md-person-add"
              size="16"
              title="Invite users"
              @click="preInvitation()"
              style="
                float: right;
                margin: 5px 10px 0 0;
                cursor: pointer;
                color: #2d8cf0;
              "
            />
            <Icon
              v-if="
                activityInfo.level > 0 &&
                userRole != 'visitor' &&
                activityInfo.creator != userInfo.userId
              "
              type="md-log-out"
              size="16"
              title="Leave the activity"
              @click="quitActivityModal = true"
              style="float: right; margin: 5px 10px 0 0; cursor: pointer"
            />
          </div>
          <vue-scroll :ops="scrollOps" style="margin-top: 10px; height: 420px">
            <Card
              style="margin: 5px 0"
              :padding="5"
              v-for="member in participants"
              :key="member.userId"
            >
              <div style="display: flex; align-items: center">
                <div
                  v-if="
                    delUserBtn &&
                    member.userId != userInfo.userId &&
                    roleCompare(userRole, member.role) == 1
                  "
                  style="cursor: pointer; margin-right: 10px"
                  @click="selectMember(member, 'delete')"
                >
                  <Icon type="md-remove-circle" size="20" color="#ed4014" />
                </div>
                <div
                  v-if="
                    userRoleBtn &&
                    member.userId != userInfo.userId &&
                    roleCompare(userRole, member.role) == 1
                  "
                  title="Set user role"
                  style="cursor: pointer; margin-right: 10px"
                  @click="selectMember(member, 'role')"
                >
                  <Icon type="md-people" size="20" color="#8bc34a" />
                </div>
                <div
                  v-if="
                    userRoleBtn &&
                    (member.userId == userInfo.userId ||
                      roleCompare(userRole, member.role) != 1)
                  "
                  :title="
                    member.role.charAt(0).toUpperCase() + member.role.slice(1)
                  "
                  style="cursor: default; margin-right: 10px"
                >
                  <Icon type="md-people" size="20" color="grey" />
                </div>
                <div
                  @click="gotoPersonalSpace(member.userId)"
                  style="display: flex; align-items: center; cursor: pointer"
                >
                  <div class="memberImg" style="position: relative">
                    <img
                      v-if="member.avatar != '' && member.avatar != undefined"
                      :src="avatarUrl(member.avatar)"
                      style="width: 40px; height: 40px"
                    />
                    <avatar
                      v-else
                      :username="member.name"
                      :size="40"
                      :rounded="true"
                    />
                    <div class="onlinecircle"></div>
                  </div>
                  <div class="memberDetail">
                    <div class="memberName">
                      <span>{{ member.name }}</span>
                    </div>
                    <div class="memberRole">
                      <span>{{
                        member.role.charAt(0).toUpperCase() +
                        member.role.slice(1)
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </Card>
          </vue-scroll>
        </div>
      </div>
    </Modal>
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
      <h4 style="color: red">Are you sure to quit this activity?</h4>
    </Modal>
  </div>
</template>
<script>
import { get, del, post, put } from "@/axios";
import Avatar from "vue-avatar";
export default {
  props: ["activityInfo", "projectInfo"],
  components: {
    Avatar,
  },
  data() {
    return {
      infoModal: false,
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      // projectInfo:{},
      userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
      userRole: "visitor",
      // Members
      creatorInfo: {},
      participants: [],
      // add
      potentialMembers: [],
      invitingMembers: [],
      inviteModal: false,
      // remove
      delUserBtn: false,
      slctDletMember: {},
      removeMemberModal: false,
      //update
      slctRoleMember: {},
      memberRoleModal: false,
      userRoleBtn: false,
      roleSelected: "",
      // leave
      quitActivityModal: false,

      listStyle: { width: "280px", height: "360px" },
    };
  },
  computed: {},
  created() {
    this.userRole = this.roleIdentity(this.activityInfo);
    // this.getProjectInfo(); //通过传值代替
    this.getParticipants();
  },
  mounted() {},
  methods: {
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
    async getParentPermission() {
      let url = "";
      if (this.appliedActivity.level == 0 || this.appliedActivity.level == 1) {
        return this.projectInfo.members.contains(this.userInfo);
      } else if (this.appliedActivity.level == 2) {
        url = "/GeoProblemSolving/subproject/" + this.appliedActivity.parent;
        let parent = await get(url);
        return parent.members.contains(this.userInfo);
      } else if (this.appliedActivity.level > 2) {
        url = "/GeoProblemSolving/activity/" + this.appliedActivity.parent;
        let parent = await get(url);
        return parent.members.contains(this.userInfo);
      }
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
      this.axios
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
    modifyPermission() {
      window.location.href =
        "/GeoProblemSolving/permission/" +
        this.activityInfo.level +
        "/" +
        this.activityInfo.aid;
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
      console.log(data);

      this.potentialMembers = [];
      this.invitingMembers = [];
      let candidates = data.members;
      for (let i = 0; i < candidates.length; i++) {
        this.potentialMembers.push({
          key: this.potentialMembers.length,
          userId: candidates[i].userId,
          avatar: candidates[i].avatar,
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
      // return `<span title="${item.name} - ${item.role}">${item.name}</span>`;
    },
    filterMethod(data, query) {
      return data.name.indexOf(query) > -1;
    },
    handleChange(newTargetKeys) {
      this.invitingMembers = newTargetKeys;
    },
    inviteMembers() {
      let activity = this.activityInfo;
      let inviteList = [];

      // add members
      for (var i = 0; i < this.invitingMembers.length; i++) {
        let index = this.invitingMembers[i];
        if (this.participants.contains(this.potentialMembers[index])) continue;
        let user = this.potentialMembers[index];
        inviteList.push(user.userId);
      }

      let url = "";
      if (activity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          activity.aid +
          "/userBatch?userIds=" +
          inviteList.toString();
      } else if (activity.level > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          activity.aid +
          "/userBatch?userIds=" +
          inviteList.toString();
      } else {
        return;
      }

      this.axios
        .post(url)
        .then((res) => {
          if (res.data.code == 0) {
            for (var i = 0; i < this.invitingMembers.length; i++) {
              let index = this.invitingMembers[i];
              if (this.participants.contains(this.potentialMembers[index]))
                continue;
              let user = this.potentialMembers[index];
              this.participants.push(user);
              this.$Notice.info({ desc: "Invite member successfully" });

              // update activity doc
              // this.operationApi.participantUpdate(
              //   activity.aid,
              //   "join",
              //   user.userId,
              //   user.name,s
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
            }
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
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
            this.slctRoleMember = role;
            this.$Notice.info({ desc: "Change the member role successfully" });
            // update activity doc
            this.operationApi.participantUpdate(
              activity.aid,
              "role",
              member.userId,
              member.name,
              role,
              member.domain
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
    selectMember(member, operation) {
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
            // update activity doc
            this.operationApi.participantUpdate(
              activity.aid,
              "remove",
              member.userId,
              "",
              "",
              []
            );

            let index = this.participants.indexOf(member);
            this.participants.splice(index, 1);
            this.$Notice.info({ desc: "Remove member successfully" });

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
                "/GeoProblemSolving/activityInfo/" +
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
            this.$store.commit("addNotification", notice);
            // this.$emit("sendNotice", notice);
            // this.$Notice.info({ desc: "Send notice successfully" });
          } else {
            // this.$Notice.error({ desc: "Fail to send notice" });
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    gotoPersonalSpace(id) {
      if (id == this.$store.getters.userId) {
        window.location.href = "/GeoProblemSolving/newPersonalPage/overView";
        // this.$router.push({name: "overView"})
      } else {
        window.location.href = "/GeoProblemSolving/memberPage/" + id;
      }
    },
    avatarUrl(url) {
      let avatarUrl = this.$store.state.UserServer + url;
      return avatarUrl;
    },
  },
};
</script>
<style scoped>
.info >>> .ivu-modal-body {
  height: 500px;
  padding: 0 10px;
}
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

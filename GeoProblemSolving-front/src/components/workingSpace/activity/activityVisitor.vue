<!--workspace introduction-->
<template>
  <div style="padding: 5px 10px">
    <div
      style="
        height: calc(100vh - 110px);
        width: 75%;
        float: left;
        padding: 20px;
      "
    >
      <vue-scroll
        :ops="scrollOps"
        style="margin-top: 10px; height: calc(100vh - 165px)"
      >
        <div>
          <div>
            <h2>{{ activityInfo.name }}</h2>
          </div>
          <div style="heigth: 50%; width: 50%; float: left">
            <div style="margin: 10px 0">
              <Label>Purpose:</Label>
              <span style="margin-left: 10px">{{ activityInfo.purpose }}</span>
            </div>
            <div style="margin: 10px 0">
              <Label>Total participants:</Label>
              <span style="margin-left: 10px">{{ participants.length }}</span>
            </div>
            <!-- <div style="margin: 10px 0">
              <Label>Total child activities:</Label>
              <span
                v-if="activityInfo.children != undefined"
                style="margin-left: 10px"
                >{{ activityInfo.children.length }}</span
              >
              <span
                v-else
                style="margin-left: 10px; cursor: pointer; color: #2d8cf0"
                >0</span
              >
            </div> -->
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
            <div style="margin: 10px 0">
              <Label>Last active time:</Label>
              <span style="margin-left: 10px">{{
                activityInfo.activeTime.split(" ")[0]
              }}</span>
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
                max-height: 120px;
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
        width: 25%;
        float: left;
        padding: 0 20px;
        margin: 20px 0;
        border-left: 1px solid lightgray;
      "
    >
      <div>
        <span style="font-size: 1.17em; font-weight: bold">Participants</span>
      </div>
      <vue-scroll
        :ops="scrollOps"
        style="margin-top: 10px; height: calc(100vh - 135px)"
      >
        <Card
          style="margin: 5px 0"
          :padding="5"
          v-for="member in participants"
          :key="member.name"
        >
          <div style="display: flex; align-items: center">
            <div
              @click="gotoPersonalSpace(member.userId)"
              style="display: flex; align-items: center; cursor: pointer"
            >
              <div class="memberImg" style="position: relative">
                <img
                  v-if="member.avatar != undefined && member.avatar != ''"
                  :src="avatarUrl(member.avatar)"
                  style="width: 40px; height: 40px"
                />
                <avatar
                  v-else-if="member.name != undefined && member.name != ''"
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
                  <span>{{ member.role.charAt(0).toUpperCase() + member.role.slice(1) }}</span>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </vue-scroll>
    </div>
  </div>
</template>
<script>
import { get, del, post, put } from "@/axios";
import * as socketApi from "@/api/socket.js";
import Avatar from "vue-avatar";
export default {
  components: {
    Avatar,
  },
  props: ["activityInfo","userInfo"],
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      projectInfo: {},
      // userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
      userRole: "visitor",
      // Members
      creatorInfo: {},
      participants: [],
    };
  },
  created() {
    this.userRole = this.roleIdentity(this.activityInfo);
    this.getParticipants();
  },
  mounted() {},
  methods: {
    avatarUrl(url) {
      let avatarUrl = this.$store.state.UserServer + url;
      return avatarUrl;
    },
    roleIdentity(activity) {
      return this.userRoleApi.roleIdentify(activity.members, this.userInfo.userId);
    },
    permissionIdentity(permission, role, operation) {
      return this.userRoleApi.permissionIdentity(
        JSON.parse(permission),
        role,
        operation
      );
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
    preCreation() {
      this.$Notice.info({ desc: "Please join the activity firstly." });
      return;
    },
    gotoPersonalSpace(id) {
      if (id == this.$store.getters.userId) {
        window.location.href = "/GeoProblemSolving/newPersonalPage/overView";
      } else {
        window.location.href = "/GeoProblemSolving/memberPage/" + id;
      }
    },
  },
};
</script>
<style scoped>
.childrenCard >>> .ivu-card-head {
  padding: 5px 10px;
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

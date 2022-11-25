<!--workspace introduction-->
<template>
  <div style="padding: 5px 10px">
    <div
      style="
        height: calc(100vh - 250px);
        width: 75%;
        float: left;
        padding: 20px;
      "
    >
      <vue-scroll
        :ops="scrollOps"
        style="margin-top: 10px; height: calc(100vh - 280px)"
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
            <div style="margin: 10px 0">
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
                <Icon type="ios-create" :size="20" />
              </span>
            </div>
            <div style="margin: 10px 0">
              <Label>Last active time:</Label>
              <span style="margin-left: 10px">{{
                activityInfo.activeTime.split(" ")[0]
              }}</span>
            </div>
          </div>
          <div
            style="margin: 10px 0"
            v-if="activityInfo.type == 'Activity_Group'"
          >
            <Label style="float: left">Description: </Label>
            <div
              style="
                float: left;
                min-height: 40px;
                max-height: 120px;
                overflow-y: auto;
                word-break: break-word;
                margin-left: 10px;
              "
              :title="activityInfo.description"
            >
              {{ activityInfo.description }}
            </div>
          </div>
        </div>
        <Divider style="margin: 10px 0"></Divider>
        <div v-if="activityInfo.type == 'Activity_Group'">
          <h4 style="margin: 8px 0">Child activities</h4>
          <div
            v-for="item in activityInfo.children"
            :key="item.aid"
            class="childrenCard"
            style="display: inline-block"
          >
            <Card
              v-if="roleIdentity(item) == 'visitor'"
              style="
                height: 160px;
                width: 220px;
                float: left;
                margin: 0 10px 10px 0;
                cursor: pointer;
              "
            >
              <p slot="title" :title="item.name" class="activityTitle">
                {{ item.name }}
              </p>
              <div slot="extra" style="margin-top: -10px; margin-right: -5px">
                <Tooltip
                  trigger="hover"
                  content="Apply to join this activity"
                  placement="bottom"
                  ><Icon
                    type="md-log-in"
                    class="changeInviteColor"
                    size="small"
                    style="cursor: pointer; color: #2d8cf0"
                    @click="preApplication(item)"
                  />
                </Tooltip>
              </div>
              <div
                style="margin-top: 5px"
                :title="item.description"
                class="childDescription"
              >
                {{ item.description }}
              </div>
            </Card>
            <Card
              v-else
              style="
                height: 160px;
                width: 220px;
                float: left;
                margin: 0 10px 10px 0;
                cursor: pointer;
              "
              @click.native="enterActivity(item)"
            >
              <p slot="title" :title="item.name">{{ item.name }}</p>
              <div slot="extra" style="margin-top: -10px; margin-right: -5px">
                <Icon type="ios-person" :size="20"> </Icon>
              </div>
              <div
                style="margin-top: 5px"
                :title="item.description"
                class="childDescription"
              >
                {{ item.description }}
              </div>
            </Card>
          </div>
          <div style="display: inline-block">
            <Card
              style="
                height: 160px;
                width: 220px;
                float: left;
                margin: 0 10px 10px 0;
                cursor: pointer;
              "
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'manage_child_activity'
                )
              "
            >
              <div
                style="text-align: center; margin-top: 40px; color: #2d8cf0"
                title="Create activity"
                @click="preCreation()"
              >
                <Icon type="ios-add" size="80" />
              </div>
            </Card>
          </div>
        </div>
        <div v-else>
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
      <vue-scroll
        :ops="scrollOps"
        style="margin-top: 10px; height: calc(100vh - 280px)"
      >
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
                    member.role.charAt(0).toUpperCase() + member.role.slice(1)
                  }}</span>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </vue-scroll>
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
      <h4 style="color: red">Are you sure to quit this activity?</h4>
    </Modal>
    <Modal
      v-model="applyJoinActivityModal"
      title="Apply to join the activity"
      width="800px"
      :mask-closable="false"
    >
      <div>
        <Form ref="applyJoinForm" :model="applyJoinForm" :label-width="120">
          <FormItem label="Reason" prop="reason">
            <Input v-model="applyJoinForm.reason" :rows="4" type="textarea" />
          </FormItem>
        </Form>
      </div>
      <div slot="footer" style="display: inline-block">
        <Button type="primary" @click="applyJoinActivity()" style="float: right"
          >OK</Button
        >
        <Button
          @click="applyJoinActivityModal = false"
          style="float: right; margin-right: 15px"
          >Cancel
        </Button>
      </div>
    </Modal>
    <Modal
      v-model="createActivityModel"
      title="Create a new child activity"
      width="800"
      :mask-closable="false"
    >
      <Form
        ref="activityForm"
        :model="activityForm"
        :rules="activityCreateRule"
        :label-width="120"
      >
        <FormItem label="Name" prop="name">
          <Input
            type="text"
            v-model="activityForm.name"
            placeholder="Fill in the name (less than 60 characters) ..."
          />
        </FormItem>
        <FormItem label="Description" prop="description">
          <Input
            v-model="activityForm.description"
            placeholder="Fill in the description..."
            :rows="4"
            type="textarea"
          />
        </FormItem>
        <FormItem label="Activity type:" prop="type">
          <Select
            v-model="activityForm.type"
            placeholder="Select the type of this activity"
            readonly
          >
            <Option value="Activity_Default">Select type later</Option>
            <Option value="Activity_Unit">Single activity</Option>
            <Option value="Activity_Group">Multi activities</Option>
          </Select>
        </FormItem>
        <FormItem
          label="Purpose:"
          prop="purpose"
          v-show="activityForm.type == 'Activity_Unit'"
        >
          <Select
            v-model="activityForm.purpose"
            placeholder="Select the purpose of this activity"
            readonly
          >
            <Option v-for="item in purposes" :key="item.index" :value="item">{{
              item
            }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer" style="display: inline-block">
        <Button
          type="primary"
          @click="createActivity('activityForm')"
          style="float: right"
          >OK</Button
        >
        <Button
          @click="createActivityModel = false"
          style="float: right; margin-right: 15px"
          >Cancel</Button
        >
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>
<script>
import { get, del, post, put } from "@/axios";
import Avatar from "vue-avatar";
import loginModal from "../../user/userState/loginModal.vue";
export default {
  components: {
    Avatar,
    loginModal,
  },
  props: ["activityInfo", "nameConfirm", "userInfo", "projectInfo"],
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      // userInfo: {},
      userRole: "visitor",
      // Members
      creatorInfo: {},
      participants: [],
      // login
      tempLoginModal: false,
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
      // apply
      applyJoinActivityModal: false,
      createActivityModel: false,
      appliedActivity: {},
      applyJoinForm: {
        reason: "",
      },
      // Activity
      activityForm: {
        name: "",
        description: "",
        parent: "",
        creator: "",
        level: -1,
        permission: JSON.stringify(this.userRoleApi.getDefault()),
        type: "Activity_Default",
        purpose: "Other purpose",
      },
      activityCreateRule: {
        name: [
          {
            required: true,
            message: "The name should not be empty and more than 60 characters",
            trigger: "blur",
            type: "string",
            max: 60,
          },
        ],
        description: [
          {
            required: true,
            message: "The description should not be empty",
            trigger: "blur",
          },
        ],
        purpose: [
          {
            required: true,
            message: "The purpose should not be empty",
            trigger: "blur",
          },
        ],
      },
      purposes: [
        "Context definition & resource collection",
        "Data processing",
        "Data visualization",
        "Geo-analysis model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Data analysis",
        "Decision making",
        "Other purpose",
      ],
      listStyle: { width: "280px", height: "360px" },
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
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    preCreation() {
      if (
        !this.permissionIdentity(
          this.activityInfo.permission,
          this.userRole,
          "manage_child_activity"
        )
      ) {
        return;
      }
      this.activityForm.parent = this.activityInfo.aid;
      this.activityForm.creator = this.userInfo.userId;
      this.activityForm.level = this.activityInfo.level + 1;
      this.createActivityModel = true;
    },
    createActivity(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          //检查是否已经存在
          for (let i = 0; i < this.nameConfirm.length; i++) {
            if (this.nameConfirm[i] == this.activityForm.name) {
              this.$Message.error("Repeated activity naming.");
              return;
            }
          }
          let url = "";
          if (this.activityForm.level == 1) {
            // subproject
            url = "/GeoProblemSolving/subproject";
          } else if (this.activityForm.level > 1) {
            // activity
            url = "/GeoProblemSolving/activity";
          }
          this.axios
            .post(url, this.activityForm)
            .then((res) => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                this.$emit("enterActivity", res.data.data);
              } else {
                console.log(res.data.msg);
              }
            })
            .catch((err) => {
              throw err;
            });
          this.createActivityModel = false;
        }
      });
    },
    enterActivity(activity) {
      if (this.roleIdentity(activity) == "visitor") {
        this.$Message.info("Please join this activity.");
      }
      this.$emit("enterActivity", activity);
    },
    preApplication(activity) {
      this.appliedActivity = activity;
      if (
        this.permissionIdentity(activity.permission, "visitor", "auto_join")
      ) {
        this.joinActivity();
      } else {
        this.applyJoinActivityModal = true;
      }
    },
    joinActivity() {
      let url = "";
      if (this.appliedActivity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          this.appliedActivity.aid +
          "/user?userId=" +
          this.userInfo.userId;
      } else if (this.appliedActivity.levell > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          this.appliedActivity.aid +
          "/user?userId=" +
          this.userInfo.userId;
      } else {
        return;
      }

      this.axios
        .post(url)
        .then((res) => {
          if (res.data.code == 0) {
            window.location.href =
              "/GeoProblemSolving/activityInfo/" +
              this.projectInfo.aid +
              "?aid=" +
              this.appliedActivity.aid +
              "&level=" +
              this.appliedActivity.level;
          } else if (res.data.code == -3) {
            this.$Notice.info({
              desc: "You has already been a member of the activity.",
            });
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    applyJoinActivity(activity) {
      let managers = this.userRoleApi.getMemberByRole(
        this.appliedActivity,
        "manager"
      );
      for (var i = 0; i < managers.length; i++) {
        let notice = {
          recipientId: managers[i],
          type: "apply",
          content: {
            title:
              "Application of joining the activity: " +
              this.appliedActivity.name,
            projectId: this.projectInfo.aid,
            activityId: this.appliedActivity.aid,
            activityName: this.appliedActivity.name,
            activityLevel: this.appliedActivity.level,
            userEmail: this.userInfo.email,
            userName: this.userInfo.name,
            userId: this.userInfo.userId,
            userDomain: this.userInfo.domain,
            description: this.applyJoinForm.reason,
            approve: "unknow",
          },
        };
        this.sendNotice(notice);
      }
      this.applyJoinActivityModal = false;
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
            //添加活动文档和发送信息
            for (var i = 0; i < this.invitingMembers.length; i++) {
              let index = this.invitingMembers[i];
              if (this.participants.contains(this.potentialMembers[index]))
                continue;
              let user = this.potentialMembers[index];
              this.participants.push(user);
              // this.operationApi.participantUpdate(
              //   this.activityInfo.aid,
              //   "join",
              //   user.userId,
              //   user.name,
              //   "ordinary-member",
              //   user.domain
              // );
              this.$Notice.info({ desc: "Invite member successfully" });

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
            this.operationApi.getActivityDoc(this.activityInfo.aid);
            this.operationApi.participantUpdate(
              this.activityInfo.aid,
              "remove",
              member.userId,
              member.name,
              member.role,
              member.domain
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
            this.slctRoleMember.role = role;
            this.$Notice.info({ desc: "Change the member role successfully" });
            this.operationApi.participantUpdate(
              this.activityInfo.aid,
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

      this.$axios
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
            //
            this.$Notice.info({
              title: "Leave the activity",
              desc: "Success!",
            });
            this.$emit("enterRootActivity");
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
  width: 90%;
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
.childDescription {
  display: -webkit-box;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 5;
  overflow: hidden;
}
.activityTitle >>> .ivu-card-head p {
  width: 90%;
}
</style>

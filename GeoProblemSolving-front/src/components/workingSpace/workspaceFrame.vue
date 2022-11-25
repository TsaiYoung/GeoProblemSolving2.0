<template>
  <div
    style="display: flex; background-color: #eee; height: calc(100vh - 130px)"
  >
    <Button
      title="Activity tree"
      @click="unfoldTree"
      v-show="treeFold"
      class="foldTreeBtn"
    >
      <Icon
        type="md-arrow-dropright-circle"
        size="20"
        title="Unfold the activity tree"
      />
    </Button>

    <!--  Activity List部分  -->
    <Card
      dis-hover
      class="activityCard"
      id="ActivityTree"
      style="height: calc(100vh - 130px)"
    >
      <span slot="title" style="font-size: 18px; font-weight: 700"
        >Activity list</span
      >
      <Icon
        slot="title"
        type="md-refresh"
        size="18"
        title="Refresh the activity tree"
        style="cursor: pointer; margin-left: 6px"
        @click="locateActivity"
      />
      <Icon
        slot="extra"
        type="ios-arrow-dropleft"
        size="25"
        title="Collapse the activity tree"
        style="cursor: pointer"
        @click="foldTree"
      />
      <vue-scroll :ops="scrollOps">
        <div style="padding-right: 15px">
          <Tree :data="activityTree" :render="renderStyle"></Tree>
        </div>
      </vue-scroll>
    </Card>

    <!--    使用slot控制是否显示-->
    <Card
      dis-hover
      class="workspaceCard"
      id="ActivityContent"
      style="height: calc(100vh - 120px)"
    >
      <h3 slot="title">
        <Breadcrumb style="display: inline-block" separator=">">
          <BreadcrumbItem v-for="name in cascader" :key="name"
            >{{ name }}
          </BreadcrumbItem>
        </Breadcrumb>
      </h3>
      <div slot="extra">
        <Button
          v-if="
            slctActivity.level > 0 ||
            (slctActivity.level == 0 && slctActivity.type != 'Activity_Default')
          "
          icon="md-create"
          size="small"
          style="margin-top: -10px; margin-right: 10px"
          title="Edit information"
          @click="preEditting()"
          v-show="
            permissionIdentity(
              slctActivity.permission,
              roleIdentity(slctActivity),
              'edit_info'
            )
          "
          >Edit</Button
        >
        <Button
          v-if="
            slctActivity.level > 0 && roleIdentity(slctActivity) == 'visitor'
          "
          icon="md-log-in"
          size="small"
          style="margin-top: -10px; margin-right: 10px"
          title="Apply to join the activity"
          @click="preApplication()"
          >Join</Button
        >
        <Button
          v-if="
            slctActivity.level > 0 && userInfo.userId === slctActivity.creator
          "
          icon="md-trash"
          size="small"
          title="Delete activity"
          style="margin-top: -10px"
          @click="activityDeleteModal = true"
          >Delete</Button
        >
        <Button
          v-if="slctActivity.level == 0"
          type="primary"
          icon="ios-bookmark"
          size="small"
          style="margin-top: -10px; margin-right: 10px"
          title="Back to project page"
          @click="backToProject()"
          >Back</Button
        >
      </div>
      <!-- <Spin size="large" fix v-if="spinShow"></Spin> -->
      <!--      默认contentType为0，根据contType编号进行模块显示-->
      <type-choose
        v-if="contentType == 0"
        :activityInfo="slctActivity"
        :userInfo="userInfo"
        @typeChanged="typeChanged"
        :key="contentId"
      ></type-choose>
      <single-activity
        v-else-if="contentType == 1"
        :activityInfo="slctActivity"
        :userInfo="userInfo"
        :projectInfo="projectInfo"
        :key="contentId"
      ></single-activity>
      <multi-activity
        v-else-if="contentType == 2"
        :activityInfo="slctActivity"
        :userInfo="userInfo"
        :projectInfo="projectInfo"
        :childActivities="childActivities"
        :nameConfirm="nameConfirm"
        :key="contentId"
        v-on:enterActivity="enterActivity"
        v-on:enterRootActivity="enterRootActivity"
      ></multi-activity>
      <activity-visitor
        v-else-if="contentType == 3"
        :activityInfo="slctActivity"
        :userInfo="userInfo"
        :key="contentId"
      ></activity-visitor>
    </Card>
    <!-- edit project -->
    <Modal
      v-model="activityEditModal"
      title="Edit information of the activity"
      :mask-closable="false"
    >
      <Form
        ref="editActivityForm"
        :model="editActivityForm"
        :rules="activityEditRule"
        :label-width="120"
      >
        <FormItem label="Name" prop="name" v-if="slctActivity.level != 0">
          <Input
            type="text"
            v-model="editActivityForm.name"
            placeholder="Fill in the name (less than 60 characters) ..."
          ></Input>
        </FormItem>
        <FormItem
          label="Description"
          prop="description"
          v-if="slctActivity.level != 0"
        >
          <Input
            v-model="editActivityForm.description"
            placeholder="Fill in the description..."
            :rows="4"
            type="textarea"
          ></Input>
        </FormItem>
        <FormItem
          label="Activity type:"
          prop="type"
          v-show="slctActivity.type != 'Activity_Default'"
        >
          <Select
            v-model="editActivityForm.type"
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
          v-show="editActivityForm.type == 'Activity_Unit'"
        >
          <Select
            v-model="editActivityForm.purpose"
            placeholder="Select the purpose of this activity"
            readonly
          >
            <Option v-for="item in purposes" :key="item.index" :value="item"
              >{{ item }}
            </Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer" style="display: inline-block">
        <Button
          type="primary"
          @click="editActivity('editActivityForm')"
          style="float: right"
          >OK
        </Button>
        <Button
          @click="activityEditModal = false"
          style="float: right; margin-right: 15px"
          >Cancel
        </Button>
      </div>
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
    <Modal v-model="activityDeleteModal" title="Delete the current activity">
      <h3>Do you really want to delete this activity?</h3>
      <h3 style="color: red; margin-top: 10px">
        * The selected activity and its all child activities will be deleted!
      </h3>
      <div slot="footer" style="display: inline-block">
        <Button
          type="primary"
          @click="activityDeleteModal = false"
          style="float: right"
          >Think again...
        </Button>
        <Button @click="delActivity" style="float: right; margin-right: 15px"
          >Yes
        </Button>
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>
<script>
import typeChoose from "./activity/typeChoose.vue";
import singleActivity from "./activity/singleActivity.vue";
import multiActivity from "./activity/multiActivity.vue";
import activityVisitor from "./activity/activityVisitor.vue";
import loginModal from "../user/userState/loginModal.vue";

export default {
  components: {
    typeChoose,
    singleActivity,
    multiActivity,
    activityVisitor,
    loginModal,
  },
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      contentId: "",
      treeFold: false,
      cascader: [],
      nameConfirm: [],
      activityTree: [],
      projectInfo: {},
      userInfo: {},
      slctActivity: {},
      childActivities: [],
      parentActivity: {},
      // grandchildren: [],
      editActivityForm: {
        name: "",
        description: "",
        parent: "",
        creator: "",
        level: -1,
        permission: JSON.stringify(this.userRoleApi.getDefault()),
        type: "Activity_Default",
        purpose: "Other purpose",
      },
      activityEditRule: {
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
      },
      applyJoinForm: {
        reason: "",
      },
      //恢复登录的模态框
      tempLoginModal: false,
      activityEditModal: false,
      applyJoinActivityModal: false,
      activityDeleteModal: false,
      contentType: -1,
      purposes: [
        "Context definition & resource collection",
        "Data processing",
        "Data analysis",
        "Data visualization",
        "Geo-analysis model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Decision making",
        "Other purpose",
      ],
      // spinShow: false,
    };
  },
  beforeRouteEnter: (to, from, next) => {
    next((vm) => {
      if (!vm.$store.getters.userState || vm.$store.getters.userId == "") {
        vm.$router.push({ name: "Login" });
      } else {
        next();
      }
    });
  },
  created() {},
  mounted() {
    // load activity doc
    let urlInfo = this.getUrlInfo();
    this.operationApi.getActivityDoc(urlInfo.aid);

    this.initInfo();
    // this.locateActivity();
    window.addEventListener("resize", this.reSize);

    Array.prototype.contains = function (obj) {
      var i = this.length;
      while (i--) {
        if (
          (this[i] != undefined && this[i] === obj) ||
          (this[i].userId != undefined && this[i].userId === obj.userId) ||
          (this[i].tid != undefined && this[i].tid === obj.tid) ||
          (this[i].tid != undefined && this[i].tid === obj) ||
          (this[i].tsId != undefined && this[i].tsId === obj.tsId) ||
          (this[i].id != undefined && this[i].id === obj.id) ||
          (this[i].aid != undefined && this[i].aid === obj.aid)
        ) {
          return true;
        }
      }
      return false;
    };
  },
  methods: {
    initInfo: function () {
      this.handleSpinShow(1000);
      this.userInfo = this.$store.getters.userInfo;
      let urlInfo = this.getUrlInfo();
      this.$axios
        .get("/GeoProblemSolving/project/" + urlInfo.aid)
        .then((res) => {
          //offline model
          if (res.data.code == 0) {
            // this.$set(this, "projectInfo", res.data.data[0])
            this.projectInfo = res.data.data;
            if (urlInfo.search.level == 0 || urlInfo.search.level == null) {
              this.getProjectInfo();
            } else if (urlInfo.search.level == 1) {
              //跳转到subproject
              this.getSubprojectBranch(urlInfo.search.aid);
            } else if (urlInfo.search.level > 1) {
              //跳转到activity
              this.getActivityBranch(urlInfo.search.aid);
            }
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          this.$Message.error("Loading project failed.");
        });
      if (this.userInfo.organizations == null) {
        this.userInfo.organizations = [];
      }
      if (this.userInfo.domain == null) {
        this.userInfo.domain = [];
      }
    },
    handleSpinShow(time) {
      this.$Spin.show();
      setTimeout(() => {
        this.$Spin.hide();
      }, time);
    },
    getUrlInfo: function () {
      let url = window.location.href;
      let result = {};
      if (url.indexOf("/activityInfo/") != -1) {
        let urlStr = url.split("/activityInfo/");
        if (urlStr[1].indexOf("?") != -1) {
          result.aid = urlStr[1].split("?")[0];
        } else {
          result.aid = urlStr[1];
        }
      }
      if (url.indexOf("?") != -1) {
        let urls = url.split("?");
        result.search = {};
        result.search.content = this.getURLParameter("content", urls[1]);
        result.search.aid = this.getURLParameter("aid", urls[1]);
        result.search.level = this.getURLParameter("level", urls[1]);
        return result;
      } else {
        result.search = {};
        result.search.level = null;
        return result;
      }
    },
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    // isJoinedFatherActivity(){
    //   let fatherActivityRole = this.roleIdentity(this.parentActivity);
    //   if(fatherActivityRole == "visitor"){
    //     return false;
    //   } else {
    //     return true;
    //   }
    // },
    roleIdentity(activity) {
      return this.userRoleApi.roleIdentify(
        activity.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, role, operation) {
      if (permission == undefined)
        permission = JSON.stringify(this.userRoleApi.getDefault());
      if (operation == "observe") {
        if (JSON.parse(permission).observe.visitor == "Yes") return true;
        else if (JSON.parse(permission).observe.visitor == "No") return false;
        else {
          return this.getParentPermission();
        }
      } else if (operation == "auto_join") {
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
    async getParentPermission() {
      let url = "";
      if (this.slctActivity.level == 0 || this.slctActivity.level == 1) {
        return this.projectInfo.members.contains(this.userInfo);
      } else if (this.slctActivity.level == 2) {
        return this.parentActivity.members.contains(this.userInfo);
      }
    },
    //设置限制内容
    renderStyle(h, { root, node, data }) {
      let props = {};
      let style = {};
      let on = {};
      let name = data.name;

      if (this.slctActivity.aid !== data.aid) {
        style = {
          width: "100%",
        };
        on = {
          click: () => {
            this.slctActivity = data;
            this.locateActivity();
          },
        };
      } else {
        style = {
          width: "100%",
          backgroundColor: "lightblue",
          cursor: "default",
        };
      }
      return h(
        "span",
        {
          style: {
            display: "inline-block",
            width: "100%",
          },
        },
        [
          h("span", [
            h(
              "Button",
              {
                props: props,
                style: style,
                on: on,
                attrs: { title: name },
              },
              [
                h(
                  "div",
                  {
                    style: {
                      overflow: "hidden",
                      maxWidth: "120px",
                      textOverflow: "ellipsis",
                      margin: "0 auto",
                    },
                  },
                  name
                ),
              ]
            ),
          ]),
        ]
      );
    },
    enterActivity(activity) {
      this.slctActivity = activity;
      this.locateActivity();
    },
    enterRootActivity() {
      this.handleSpinShow(1000);
      this.getProjectInfo();
    },
    foldTree() {
      this.treeFold = true;
      document.getElementById("ActivityTree").style.width = 0;
      document.getElementById("ActivityContent").style.width =
        window.innerWidth - 10 + "px";
    },
    unfoldTree() {
      this.treeFold = false;
      document.getElementById("ActivityTree").style.width = 250 + "px";
      document.getElementById("ActivityContent").style.width =
        window.innerWidth - 260 + "px";
    },
    reSize() {
      if (this.treeFold) {
        document.getElementById("ActivityTree").style.width = 0;
        document.getElementById("ActivityContent").style.width =
          window.innerWidth - 10 + "px";
      } else {
        document.getElementById("ActivityTree").style.width = 250 + "px";
        document.getElementById("ActivityContent").style.width =
          window.innerWidth - 260 + "px";
      }
    },
    backToProject() {
      window.location.href =
        "/GeoProblemSolving/projectInfo/" +
        this.projectInfo.aid +
        "?content=overview";
    },
    locateActivity() {
      // let content = this.getURLParameter("content");
      // let aid = this.getURLParameter("aid");
      // let level = this.getURLParameter("level");
      let level = this.slctActivity.level;
      let aid = this.slctActivity.aid;

      // load activity doc
      let result = this.operationApi.getActivityDoc(aid);
      if (result === "empty") {
        this.operationApi.activityDocInit(this.slctActivity, this.userInfo);
      }

      if (level > 1) {
        this.getActivityBranch(aid);
      } else if (level == 1) {
        this.getSubprojectBranch(aid);
      } else {
        this.handleSpinShow(1000);
        this.getProjectInfo();
      }
    },
    getURLParameter(name, url) {
      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
      var r = url.substr(0).match(reg);

      if (r != null) {
        return unescape(r[2]);
      }
      return null;
    },
    getProjectInfo() {
      this.cascader = [this.projectInfo.name];
      this.activityTree = [];
      if (this.projectInfo.type == "Activity_Group") {
        this.$axios
          .get(
            "/GeoProblemSolving/project/" + this.projectInfo.aid + "/children"
          )
          .then((res) => {
            //offline model
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let children = res.data.data;
              this.childActivities = children;
              this.nameConfirm = [];
              // 处理掉异常
              for (let i = 0; i < children.length; i++) {
                children[i].children = []; //清除child后只能显示两(三)级level
                this.nameConfirm.push(children[i].name);
              }

              var root = Object.assign({}, this.projectInfo);
              this.nameConfirm.push(root.name);
              root["expand"] = true;
              root["children"] = children;

              // update activity tree
              this.activityTree.push(root);
              this.slctActivity = root;
              this.parentActivity = null;
              this.setContent(this.slctActivity);
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });
      } else {
        let root = Object.assign({}, this.projectInfo);
        this.activityTree.push(root);
        this.slctActivity = root;
        this.nameConfirm.push(root.name);
        this.parentActivity = null;
        this.setContent(this.slctActivity);
      }
    },
    getSubprojectBranch(aid) {
      // init
      this.cascader = [];
      this.activityTree = [];
      // url
      let url = "/GeoProblemSolving/subproject/" + aid + "/lineage";

      this.axios
        .get(url)
        .then((res) => {
          //offline model
          if (res.data == "Offline") {
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            let branch = res.data.data;
            this.childActivities = branch.children;

            this.buildActivityTree(
              branch.ancestors,
              branch.brothers,
              branch.children
            );
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    getActivityBranch(aid) {
      // init
      this.cascader = [];
      this.activityTree = [];
      // url
      let url = "/GeoProblemSolving/activity/" + aid + "/lineage";

      this.axios
        .get(url)
        .then((res) => {
          //offline model

          if (res.data == "Offline") {
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            let branch = res.data.data;
            this.childActivities = branch.children;
            this.buildActivityTree(
              branch.ancestors,
              branch.brothers,
              branch.children
            );
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    buildActivityTree(ancestors, brothers, children) {
      this.activityTree = [];
      this.nameConfirm = [];
      // child activities normalization
      let role = this.roleIdentity(ancestors[0]);
      if (children.length > 0 && role != "visitor") {
        for (var i = 0; i < children.length; i++) {
          children[i].children = [];
          this.nameConfirm.push(children[i].name);
        }
        ancestors[0].children = children;
        ancestors[0]["expand"] = true;
      } else {
        ancestors[0].children = [];
      }

      // brother activities normalization
      for (var i = 0; i < brothers.length; i++) {
        if (brothers[i].aid !== ancestors[0].aid) {
          brothers[i].children = [];
          this.nameConfirm.push(brothers[i].name);
        }
      }
      ancestors[1].children = brothers;

      // ancestor activities normalization
      for (let i = 1; i < ancestors.length; i++) {
        let parentActivity = ancestors[i];
        this.nameConfirm.push(parentActivity.name);
        let current = ancestors[i - 1];

        let childrenNum = parentActivity.children.length;
        parentActivity["expand"] = true;

        for (let j = 0; j < childrenNum; j++) {
          if (
            Object.prototype.toString.call(parentActivity.children[j]) ==
            "[object String]"
          ) {
            if (parentActivity.children[j] == current.aid) {
              parentActivity.children = [current];
              break;
            }
          } else if (
            Object.prototype.toString.call(parentActivity.children[j]) ==
            "[object Object]"
          ) {
            if (parentActivity.children[j].aid == current.aid) {
              parentActivity.children[j] = current;
              break;
            }
          }
        }
      }

      // update activity tree
      var root = ancestors[ancestors.length - 1];
      this.activityTree.push(root);
      this.slctActivity = ancestors[0];
      this.parentActivity = ancestors[1];

      // load activity doc
      let result = this.operationApi.getActivityDoc(this.slctActivity.aid);
      if (result === "empty") {
        this.operationApi.activityDocInit(this.slctActivity, this.userInfo);
      }
      this.setContent(this.slctActivity);

      // Cascader names
      for (let i = ancestors.length - 1; i >= 0; i--) {
        this.cascader.push(ancestors[i].name);
      }
    },
    typeChanged(data) {
      this.slctActivity.type = data.type;
      if (this.slctActivity.level == 0) {
        this.projectInfo.type = data.type;
      }
      this.updatePathway("type", data.purpose);
      this.setContent(this.slctActivity);
    },
    activityInfo(data) {
      this.activityInfo = data;
    },
    setContent(activity) {
      //更改当前页面的url，且不刷新页面
      // var originUrl = window.location.href;
      // var valiable = originUrl.split("?")[0];
      // window.history.pushState(
      //   null,
      //   null,
      //   valiable + "?aid=" + activity.aid + "&level=" + activity.level + ""
      // );
      this.$router.push({
        name: "workspaceContent",
        params: { projectId: this.projectInfo.aid },
        query: { aid: activity.aid, level: activity.level },
      });
      this.contentId = Math.random();
      if (
        this.roleIdentity(activity) == "visitor" &&
        !this.permissionIdentity(
          this.slctActivity.permission,
          "visitor",
          "observe"
        )
      ) {
        this.contentType = 3;
      } else {
        if (activity.type == "Activity_Default") {
          this.contentType = 0;
        } else if (activity.type == "Activity_Unit") {
          this.contentType = 1;
        } else if (activity.type == "Activity_Group") {
          this.contentType = 2;
        }
      }
      // console.log(this.contentType)
      // this.contentType = 3;
    },
    preEditting() {
      let children = [];
      let activity = Object.assign({}, this.slctActivity);
      if (activity.children != undefined) {
        for (let i = 0; i < activity.children.length; i++) {
          children.push(activity.children[i].aid);
        }
        activity.children = children;
      }
      this.editActivityForm = activity;
      this.activityEditModal = true;
    },
    editActivity(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          // Update the activity type
          if (this.editActivityForm.type != this.slctActivity.type) {
            if (this.slctActivity.type == "Activity_Unit") {
              this.editActivityForm.purpose = "Other purpose";
            } else if (this.slctActivity.type == "Activity_Group") {
              if (
                this.slctActivity.children != undefined &&
                this.slctActivity.children.length > 0
              ) {
                this.$Notice.info({
                  desc: "Please make sure no child activity existing, before changing the type of current activity.",
                });
                this.activityEditModal = false;
                return;
              } else {
                this.editActivityForm.pathway = [];
              }
            }
            // update activity doc
            // this.operationApi.activityUpdate("type", this.editActivityForm);
            this.updatePathway("type", this.editActivityForm.purpose);
          } else if (
            this.editActivityForm.purpose != this.slctActivity.purpose
          ) {
            this.updatePathway("type", this.editActivityForm.purpose);
          } else {
            // update activity doc
            // this.operationApi.activityUpdate("other", this.editActivityForm);
          }

          // Update the activity name
          if (this.editActivityForm.name != this.slctActivity.name) {
            this.updatePathway("name", this.editActivityForm.name);
          }

          // url
          let url = "";
          if (this.slctActivity.level == 1) {
            url = "/GeoProblemSolving/subproject/" + this.slctActivity.aid;
          } else if (this.slctActivity.level > 1) {
            url = "/GeoProblemSolving/activity/" + this.slctActivity.aid;
          } else {
            url = "/GeoProblemSolving/project/" + this.slctActivity.aid;
          }
          this.axios
            .put(url, this.editActivityForm)
            .then((res) => {
              //offline model
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                this.$Notice.success({ title: "Result", desc: "Success!" });
                // updata slctActivity
                this.slctActivity.name = this.editActivityForm.name;
                this.slctActivity.description =
                  this.editActivityForm.description;
                this.slctActivity.type = this.editActivityForm.type;
                this.slctActivity.purpose = this.editActivityForm.purpose;
                this.activityEditModal = false;
                // cascader
                let index = this.cascader.length - 1;
                this.$set(this.cascader, index, this.editActivityForm.name);
                // update activity tree

                // change content
                this.setContent(this.slctActivity);
              } else {
                this.$Notice.error({ title: "Result", desc: res.data.msg });
                this.activityEditModal = false;
              }
            })
            .catch((err) => {
              throw err;
            });
        }
      });
    },
    updatePathway(type, content) {
      if (this.parentActivity) {
        if (this.parentActivity.pathway != undefined) {
          if (type === "name") {
            let pathway = JSON.stringify(this.parentActivity.pathway);
            // let faceName = new RegExp(this.slctActivity.name, "g");
            // pathway.replace(faceName, content);
            let newpathway = pathway.replaceAll(
              '"' + this.slctActivity.name + '"',
              '"' + content + '"'
            );
            this.parentActivity.pathway = JSON.parse(newpathway);
          } else if (type === "type") {
            for (let i = 0; i < this.parentActivity.pathway.length; i++) {
              if (
                this.parentActivity.pathway[i].aid === this.slctActivity.aid
              ) {
                this.parentActivity.pathway[i].category =
                  this.getStepCategroy(content);
              }
            }
          } else if (type === "delete") {
            this.removePathwayNode(content);
          }
        }

        // url
        let url = "";
        if (this.parentActivity.level == 1) {
          url = "/GeoProblemSolving/subproject/" + this.parentActivity.aid;
        } else if (this.parentActivity.level > 1) {
          url = "/GeoProblemSolving/activity/" + this.parentActivity.aid;
        } else {
          url = "/GeoProblemSolving/project/" + this.parentActivity.aid;
        }
        let data = {
          aid: this.parentActivity.aid,
          pathway: this.parentActivity.pathway,
        };
        this.axios
          .put(url, data)
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code !== 0) {
              this.$Notice.info({
                desc: "Fail to update the pathway in the parent activity!",
              });
            }
          })
          .catch((err) => {
            throw err;
          });
      }
    },
    // remove
    removePathwayNode(aid) {
      if (this.parentActivity.pathway != undefined) {
        let nodeId = -1;
        for (var i = 0; i < this.parentActivity.pathway.length; i++) {
          if (this.parentActivity.pathway[i].aid == aid) {
            nodeId = this.parentActivity.pathway[i].id;
            // remove mode
            this.parentActivity.pathway.splice(i, 1);
          }
        }
        // normalize
        if (nodeId != -1) {
          for (var i = 0; i < this.parentActivity.pathway.length; i++) {
            let node = this.parentActivity.pathway[i];
            // node
            if (node.id > nodeId) {
              node.id = node.id - 1;
            }
            // last
            for (var j = 0; j < node.last.length; j++) {
              if (node.last[j].id == nodeId) {
                node.last.splice(j, 1);
              } else if (node.last[j].id > nodeId) {
                node.last[j].id = node.last[j].id - 1;
              }
            }
            // next
            for (var j = 0; j < node.next.length; j++) {
              if (node.next[j].id == nodeId) {
                node.next.splice(j, 1);
              } else if (node.next[j].id > nodeId) {
                node.next[j].id = node.next[j].id - 1;
              }
            }
          }
        }
      }
    },
    getStepCategroy(purpose) {
      let category;
      if (purpose == "Context definition & resource collection") {
        category = 0;
      } else if (purpose == "Data processing") {
        category = 1;
      } else if (purpose == "Data visualization") {
        category = 2;
      } else if (purpose == "Geo-analysis model construction") {
        category = 3;
      } else if (purpose == "Model effectiveness evaluation") {
        category = 4;
      } else if (purpose == "Geographical simulation") {
        category = 5;
      } else if (purpose == "Data analysis") {
        category = 6;
      } else if (purpose == "Decision making") {
        category = 7;
      } else {
        category = 8;
      }
      return category;
    },
    preApplication() {
      //判断活动权限，如果无需申请许可，则直接加入；如果需要其他成员批准，则通过apply请求
      this.axios
        .get(
          `/GeoProblemSolving/activityDriven/user/${this.slctActivity.parent}/${this.slctActivity.aid}`
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code === 0) {
            if (res.data.data) {
              this.joinActivity();
            } else {
              if (
                this.permissionIdentity(
                  this.slctActivity.permission,
                  "visitor",
                  "auto_join"
                )
              ) {
                this.joinActivity();
              } else {
                this.applyJoinActivityModal = true;
              }
            }
          }
        })
        .catch((err) => {});
    },
    joinActivity() {
      //无需批准，直接加入
      let url = "";
      if (this.slctActivity.level == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          this.slctActivity.aid +
          "/user?userId=" +
          this.userInfo.userId;
      } else if (this.slctActivity.level > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          this.slctActivity.aid +
          "/user?userId=" +
          this.userInfo.userId;
      } else {
        return;
      }

      this.$axios
        .post(url)
        .then((res) => {
          //offline model
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.$Notice.info({ title: "Join the activity", desc: "Success!" });
            this.enterActivity(this.slctActivity);

            // // update activity doc
            // this.operationApi.participantUpdate(
            //   this.slctActivity.aid,
            //   "join",
            //   this.userInfo.userId,
            //   this.userInfo.name,
            //   "ordinary-member",
            //   this.userInfo.domain
            // );
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
    applyJoinActivity() {
      //需要获得manager批准 方可加入
      let managers = this.userRoleApi.getMemberByRole(
        this.slctActivity,
        "manager"
      );
      for (var i = 0; i < managers.length; i++) {
        let notice = {
          recipientId: managers[i],
          type: "apply",
          content: {
            title:
              "Application of joining the activity: " + this.slctActivity.name,
            projectId: this.projectInfo.aid,
            activityId: this.slctActivity.aid,
            activityName: this.slctActivity.name,
            activityLevel: this.slctActivity.level,
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
    },
    sendNotice(notice) {
      this.axios
        .post("/GeoProblemSolving/notice/save", notice)
        .then((res) => {
          //offline model
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data == "Success") {
            this.$Notice.info({ desc: "Send application successfully." });
            this.applyJoinActivityModal = false;
            this.$emit("sendNotice", notice);
          } else {
            this.$Message.error("Notice fail.");
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    delActivity() {
      if (this.userInfo.userId !== this.slctActivity.creator) return;
      let aid = this.slctActivity.aid;
      let url = "";
      if (this.slctActivity.level == 1) {
        url = "/GeoProblemSolving/subproject?aid=" + aid;
      } else if (this.slctActivity.level > 1) {
        url = "/GeoProblemSolving/activity?aid=" + aid;
      }
      this.$axios
        .delete(url)
        .then((res) => {
          //offline model

          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.$Notice.info({ title: "Delete", desc: "Success!" });
            // activity document
            // this.operationApi.activityRecord(
            //   "",
            //   "remove",
            //   this.userInfo.userId,
            //   this.slctActivity
            // );
            // this.operationApi.deleteActivityDoc(this.slctActivity.aid);
            // update pathway
            this.updatePathway("delete", aid);
            this.activityDeleteModal = false;
            //更新activityTree
            this.enterActivity(this.parentActivity);
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
  },
};
</script>
<style scoped>
.foldTreeBtn {
  padding: 5px 8px 6px;
  margin-right: 5px;
  border: 0px;
}

.activityCard {
  margin-right: 5px;
  width: 250px;
  overflow-y: auto;
  border: 0;
}

.activityCard >>> .ivu-card-body {
  padding: 5px 10px;
}

.workspaceCard {
  width: calc(100vw - 260px);
  background-color: white;
  border: 0;
  overflow-x: auto;
}

.workspaceCard >>> .ivu-card-body {
  padding: 5px;
}
</style>

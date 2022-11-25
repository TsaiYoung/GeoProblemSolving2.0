<template>
  <div>
    <div id="title">
      <h1 style="text-align: center; margin-top: 10px">Message</h1>
      <h3 style="text-align: center; margin-bottom: 10px">
        You can view and process your messages here
      </h3>
    </div>
    <Row>
      <Col span="22" offset="1">
        <Card dis-hover>
          <CheckboxGroup
            v-model="selectedNoticeType"
            style="display: inline-block; margin-left: 1%"
          >
            <Checkbox label="noticeList">
              <span>Notice</span>
              <span class="badge">{{
                readNoticeList.length + unreadNoticeList.length
              }}</span>
            </Checkbox>
            <Checkbox label="replyList">
              <span>Reply</span>
              <span class="badge">{{
                readReplyList.length + unreadReplyList.length
              }}</span>
            </Checkbox>
            <Checkbox label="applyList">
              <span>Apply</span>
              <span class="badge">{{
                readApplyList.length + unreadApplyList.length
              }}</span>
            </Checkbox>
          </CheckboxGroup>

          <Divider type="vertical" style="margin-left: 20px" />
          <span style="margin: 0 10px"></span>
          <CheckboxGroup v-model="readOrUnread" style="display: inline-block">
            <Checkbox label="read">
              <span>Read</span>
              <span class="badge">{{ readCount }}</span>
            </Checkbox>
            <Checkbox label="unRead">
              <span>Unread</span>
              <span class="badge">{{ unReadCount }}</span>
            </Checkbox>
          </CheckboxGroup>
        </Card>
        <!--  通知详情 -->
        <div>
          <Card dis-hover class="customCard">
            <!-- <h3 slot="title">Notification Detail</h3> -->
            <div :style="{ height: contentHeight - 230 + 'px' }">
              <vue-scroll :ops="ops">
                <div v-if="selectNoteNum != 0">
                  <!--
                  未读消息(分三种 notice, reply, apply)
                  notice: 只有已读按钮 不需要更新对象
                  reply: 只有回复按钮  不需要更新对方
                  apply: 有同意与拒绝按钮 需要实时更新对象通知数量
                     -->
                  <div v-if="readOrUnread.includes('unRead')">
                    <div
                      v-for="(item, unreadIndex) in unReadNoteList"
                      :key="unreadIndex"
                    >
                      <div>
                        <Card style="position: relative">
                          <Badge dot>
                            <h4
                              style="font-size: 15px"
                              @click="getNoticeInfo(item)"
                            >
                              {{ item.content.title }}
                            </h4>
                          </Badge>
                          <br />
                          <span
                            style="
                              font-weight: 400;
                              font-size: 17px;
                              display: -moz-inline-box;
                              display: inline-block;
                              width: 90%;
                            "
                            >{{ item.content.description }}</span
                          >
                          <small
                            style="
                              font-size: 13px;
                              position: absolute;
                              right: 120px;
                              bottom: 5px;
                            "
                            >{{ item.createdTime }}</small
                          >
                          <div
                            style="
                              position: absolute;
                              border-left: 1px solid lightgray;
                              right: 10px;
                              top: 10px;
                              height: 60px;
                              width: 80px;
                            "
                          >
                            <Icon
                              v-if="item.type == 'apply'"
                              type="md-close"
                              title="Refuse"
                              class="applyBtn"
                              style="color: #ff9900"
                              @click="refuseApply(item)"
                            />
                            <Icon
                              v-if="item.type == 'apply'"
                              type="md-checkmark"
                              title="Approve"
                              class="applyBtn"
                              style="color: #47cb89"
                              @click="approveApply(item)"
                            />
                            <Icon
                              v-if="item.type == 'notice'"
                              type="md-checkmark"
                              title="Read"
                              class="applyBtn"
                              style="color: #47cb89"
                              @click="readNotice(item.noticeId)"
                            />
                            <Icon
                              v-if="item.type == 'reply'"
                              type="md-checkmark"
                              class="applyBtn"
                              style="color: #47cb89"
                            />
                          </div>
                        </Card>
                      </div>
                    </div>
                  </div>

                  <!--            已读消息不区分具体类型   仅有删除按钮    -->
                  <div v-if="readOrUnread.includes('read')">
                    <div
                      v-for="(item, readIndex) in readNoteList"
                      :key="readIndex"
                    >
                      <Card>
                        <!--                  <h4 style="font-size: 13px">{{item.content.title}}</h4>-->
                        <h4
                          style="font-size: 15px"
                          @click="getReadNoticeInfo(item)"
                        >
                          {{ item.content.title }}
                        </h4>
                        <br />
                        <p style="font-weight: 400; font-size: 17px">
                          {{ item.content.description }}
                        </p>
                        <small
                          style="
                            font-size: 13px;
                            position: absolute;
                            right: 120px;
                            bottom: 5px;
                          "
                          >{{ item.createdTime }}</small
                        >
                        <div
                          style="
                            position: absolute;
                            border-left: 1px solid lightgray;
                            right: 10px;
                            top: 10px;
                            height: 60px;
                            width: 80px;
                          "
                        >
                          <Icon
                            slot="extra"
                            type="md-close"
                            title="delete"
                            class="applyBtn"
                            style="color: #ed4014"
                            @click="deleteNotice(item)"
                          />
                        </div>
                      </Card>
                    </div>
                  </div>
                </div>

                <!--          无通知消息  -->
                <div v-else>
                  <Card dis-hover style="border: transparent">
                    <h1 style="color: darkgray; text-align: center">
                      No notifications
                    </h1>
                  </Card>
                </div>
              </vue-scroll>
            </div>

            <!-- 消息展示 -->
            <Modal v-model="noticeInfoModal" title="Message Info">
              <Table
                v-if="slctMessage.type == 'apply'"
                :columns="selectedNoticeColumns"
                :data="selectedApplyData"
                stripe
                border
                @on-row-click="tableRowClick"
                :show-header="false"
              ></Table>
              <Table
                v-if="slctMessage.type == 'notice'"
                :columns="selectedNoticeColumns"
                :data="selectedNoticeData"
                stripe
                border
                @on-row-click="tableRowClick"
                :show-header="false"
              ></Table>
              <div slot="footer">
                <Button
                  v-if="slctMessage.type == 'apply'"
                  type="warning"
                  @click="refuseApply(slctMessage)"
                  >Reject</Button
                >
                <Button
                  v-if="slctMessage.type == 'apply'"
                  type="success"
                  @click="approveApply(slctMessage)"
                  >Agree</Button
                >
                <Button
                  v-if="slctMessage.type == 'notice'"
                  type="success"
                  @click="readNotice(slctMessage.noticeId)"
                  >Read</Button
                >
                <Button type="primary" @click="noticeInfoModal = false"
                  >OK</Button
                >
              </div>
            </Modal>
            <Modal v-model="readNoticeInfoModal" title="Message Info">
              <Table
                v-if="slctMessage.type == 'apply'"
                :columns="selectedReadNoticeColumns"
                :data="selectedReadApplyData"
                stripe
                border
                @on-row-click="tableRowClick"
                :show-header="false"
              ></Table>
              <Table
                v-if="slctMessage.type == 'notice'"
                :columns="selectedReadNoticeColumns"
                :data="selectedReadNoticeData"
                stripe
                border
                @on-row-click="tableRowClick"
                :show-header="false"
              ></Table>
              <div slot="footer">
                <Button type="error" @click="deleteNotice(slctMessage)"
                  >Delete</Button
                >
                <Button type="primary" @click="readNoticeInfoModal = false"
                  >OK</Button
                >
              </div>
            </Modal>
          </Card>
        </div>
      </Col>
    </Row>
  </div>
</template>

<script>
export default {
  name: "notification",
  data() {
    return {
      unreadNoticeList: [],
      unreadReplyList: [],
      unreadApplyList: [],
      readNoticeList: [],
      readReplyList: [],
      readApplyList: [],
      noticeUnreadCount: 0,
      replyUnreadCount: 0,
      applyUnreadCount: 0,
      ops: {
        bar: {
          background: "#808695",
        },
      },
      contentHeight: "",
      noticeInfoModal: false,
      readNoticeInfoModal: false,
      slctMessage: {},
      selectedNoticeData: [],
      selectedApplyData: [],
      selectedNoticeColumns: [
        {
          title: "key",
          key: "key",
          minWidth: 10,
          width: 150,
        },
        {
          title: "value",
          key: "value",
          render: (h, params) => {
            return h("span", {
              domProps: { innerHTML: this.operateFormatter(h, params) },
            });
          },
        },
      ],
      selectedReadApplyData: [],
      selectedReadNoticeData: [],
      selectedReadNoticeColumns: [
        {
          title: "key",
          key: "key",
          minWidth: 10,
          width: 150,
        },
        {
          title: "value",
          key: "value",
          render: (h, params) => {
            return h("span", {
              domProps: { innerHTML: this.operateFormatter(h, params) },
            });
          },
        },
      ],
      readOrUnread: ["unRead"],
      selectedNoticeType: ["noticeList", "replyList", "applyList"],
    };
  },
  created() {},
  mounted() {
    this.loadNotifications();
    this.resizeContent();
  },
  computed: {
    unReadCount: function () {
      let unReadNum = 0;
      if (this.selectedNoticeType.includes("noticeList")) {
        unReadNum += this.noticeUnreadCount;
      }
      if (this.selectedNoticeType.includes("replyList")) {
        unReadNum += this.replyUnreadCount;
      }
      if (this.selectedNoticeType.includes("applyList")) {
        unReadNum += this.applyUnreadCount;
      }
      return unReadNum;
    },
    readCount: function () {
      let readNum = 0;
      if (this.selectedNoticeType.includes("noticeList")) {
        readNum += this.readNoticeList.length;
      }
      if (this.selectedNoticeType.includes("replyList")) {
        readNum += this.readReplyList.length;
      }
      if (this.selectedNoticeType.includes("applyList")) {
        readNum += this.readApplyList.length;
      }
      return readNum;
    },
    selectNoteNum: function () {
      let selectedNoteNum = 0;
      if (this.readOrUnread.includes("read")) {
        selectedNoteNum += this.readCount;
      }
      if (this.readOrUnread.includes("unRead")) {
        selectedNoteNum += this.unReadCount;
      }
      return selectedNoteNum;
    },
    readNoteList: function () {
      let selectedType = this.selectedNoticeType;
      let readList = [];
      if (selectedType.includes("applyList")) {
        readList = readList.concat(this.readApplyList);
      }
      if (selectedType.includes("replyList")) {
        readList = readList.concat(this.readReplyList);
      }
      if (selectedType.includes("noticeList")) {
        readList = readList.concat(this.readNoticeList);
      }
      //将所有read消息按照时间降序排序
      for (let i = 0; i < readList.length; i++) {
        readList[i].createdTimeNew = new Date(
          readList[i].createdTime.replace(/-/g, "/")
        ).getTime();
      }
      readList.sort(function (a, b) {
        return b.createdTimeNew > a.createdTimeNew ? 1 : -1;
      });
      return readList;
    },
    unReadNoteList: function () {
      let selectedType = this.selectedNoticeType;
      let unreadList = [];
      if (selectedType.includes("applyList")) {
        unreadList = unreadList.concat(this.unreadApplyList);
      }
      if (selectedType.includes("replyList")) {
        unreadList = unreadList.concat(this.unreadReplyList);
      }
      if (selectedType.includes("noticeList")) {
        unreadList = unreadList.concat(this.unreadNoticeList);
      }
      //将所有unread消息按照时间降序排序
      for (let i = 0; i < unreadList.length; i++) {
        unreadList[i].createdTimeNew = new Date(
          unreadList[i].createdTime.replace(/-/g, "/")
        ).getTime();
      }
      unreadList.sort(function (a, b) {
        return b.createdTimeNew > a.createdTimeNew ? 1 : -1;
      });
      return unreadList;
    },
  },
  methods: {
    resizeContent() {
      if (window.innerHeight > 675) {
        this.contentHeight = window.innerHeight - 120;
      } else {
        this.contentHeight = 555;
      }
      window.onresize = () => {
        return (() => {
          this.resizeContent();
        })();
      };
    },
    //table样式
    operateFormatter(h, params) {
      let key = params.row.key;
      if (key == "Activity Name" || key == "Applicant") {
        let result =
          '<a style="text-decoration: underline; font-weight:bold;">' +
          params.row.value +
          "</a>";
        return result;
      } else {
        return params.row.value;
      }
    },
    tableRowClick(data) {
      //根据key判断类型，选择跳转用户界面或activity界面
      if (data.key == "Applicant" && data.aid != null) {
        this.$router.push("/memberPage/" + data.aid);
      } else if (data.key == "Activity Name" && data.aid != null) {
        let url = "";
        if (data.level == 0) {
          this.$router.push("/activityInfo/" + data.aid);
        } else {
          this.$router.push(
            "/activityInfo/" +
              data.projectId +
              "?aid=" +
              data.aid +
              "&level=" +
              data.level
          );
        }
      }
    },
    //点选通知信息显示
    getNoticeInfo(file) {
      this.slctMessage = file;
      if (this.slctMessage.type == "apply") {
        this.selectedApplyData = [
          {
            key: "Message Title",
            value: file.content.title,
          },
          {
            key: "Message Type",
            value: file.type,
          },
          {
            key: "Activity Name",
            value: file.content.activityName,
            aid: file.content.activityId,
            level: file.content.activityLevel,
            projectId: file.content.projectId,
          },
          {
            key: "Description",
            value: file.content.description,
          },
          {
            key: "Applicant",
            value: file.content.userName,
            aid: file.content.userId,
          },
          // {
          //   key: "Uploader",
          //   value: file.uploaderName,
          // },
          {
            key: "Message Time",
            value: file.createdTime,
          },
        ];
      } else if (this.slctMessage.type == "notice") {
        this.selectedNoticeData = [
          {
            key: "Message Title",
            value: file.content.title,
          },
          {
            key: "Message Type",
            value: file.type,
          },
          {
            key: "Description",
            value: file.content.description,
          },
          {
            key: "Activity Name",
            value: file.content.activityName,
            aid: file.content.activityId,
            level: file.content.activityLevel,
            projectId: file.content.projectId,
          },
          // {
          //   key: "Sender",
          //   value: file.content.userName,
          //   aid: file.content.userId
          // },
          // {
          //   key: "Uploader",
          //   value: file.uploaderName,
          // },
          {
            key: "Message Time",
            value: file.createdTime,
          },
        ];
      }
      this.noticeInfoModal = true;
    },
    getReadNoticeInfo(file) {
      this.slctMessage = file;
      if (this.slctMessage.type == "apply") {
        this.selectedReadApplyData = [
          {
            key: "Message Title",
            value: file.content.title,
          },
          {
            key: "Message Type",
            value: file.type,
          },
          {
            key: "Activity Name",
            value: file.content.activityName,
            aid: file.content.activityId,
            level: file.content.activityLevel,
            projectId: file.content.projectId,
          },
          {
            key: "Description",
            value: file.content.description,
          },
          {
            key: "Applicant",
            value: file.content.userName,
            aid: file.content.userId,
          },
          // {
          //   key: "Uploader",
          //   value: file.uploaderName,
          // },
          {
            key: "Message Time",
            value: file.createdTime,
          },
        ];
      } else if (this.slctMessage.type == "notice") {
        this.selectedReadNoticeData = [
          {
            key: "Message Title",
            value: file.content.title,
          },
          {
            key: "Message Type",
            value: file.type,
          },
          {
            key: "Description",
            value: file.content.description,
          },
          {
            key: "Activity Name",
            value: file.content.activityName,
            aid: file.content.activityId,
            level: file.content.activityLevel,
            projectId: file.content.projectId,
          },
          {
            key: "Message Time",
            value: file.createdTime,
          },
        ];
      }
      this.readNoticeInfoModal = true;
    },
    loadNotifications() {
      this.axios
        .get(
          "/GeoProblemSolving/notice/inquiry?" +
            "key=recipientId" +
            "&value=" +
            this.$store.getters.userId
        )
        .then((res) => {
          if (res.data !== "Fail") {
            let notifications = res.data;
            let unreadNoticeListTemp = [];
            let unreadReplyListTemp = [];
            let unreadApplyListTemp = [];
            let readNoticeListTemp = [];
            let readReplyListTemp = [];
            let readApplyListTemp = [];
            let noticeUnreadCount = 0;
            let replyUnreadCount = 0;
            let applyUnreadCount = 0;
            for (let i = 0; i < notifications.length; i++) {
              //  先判断是否为已读
              if (
                notifications[i].type == "notice" ||
                notifications[i].type === "work"
              ) {
                // noticeListTest.push(notifications[i]);
                if (notifications[i].state === "unread") {
                  unreadNoticeListTemp.push(notifications[i]);
                  noticeUnreadCount++;
                } else {
                  readNoticeListTemp.push(notifications[i]);
                }
              } else if (notifications[i].type === "reply") {
                // replyListTest.push(notifications[i]);
                if (notifications[i].state === "unread") {
                  unreadReplyListTemp.push(notifications[i]);
                  replyUnreadCount++;
                } else {
                  readReplyListTemp.push(notifications[i]);
                }
              } else if (notifications[i].type === "apply") {
                // applyListTest.push(notifications[i]);
                if (notifications[i].state === "unread") {
                  unreadApplyListTemp.push(notifications[i]);
                  applyUnreadCount++;
                } else {
                  readApplyListTemp.push(notifications[i]);
                }
              }
            }
            //使用set方式就不必担心数组push带来的累加问题
            this.$set(this, "readApplyList", readApplyListTemp);
            this.$set(this, "readReplyList", readReplyListTemp);
            this.$set(this, "readNoticeList", readNoticeListTemp);
            this.$set(this, "unreadApplyList", unreadApplyListTemp);
            this.$set(this, "unreadNoticeList", unreadNoticeListTemp);
            this.$set(this, "unreadReplyList", unreadReplyListTemp);
            this.$set(this, "noticeUnreadCount", noticeUnreadCount);
            this.$set(this, "replyUnreadCount", replyUnreadCount);
            this.$set(this, "applyUnreadCount", applyUnreadCount);
          } else {
            this.$Message.error("load notifications fail");
          }
        })
        .catch((err) => {
          this.$Message.error("load notifications fail");
        });
    },
    deleteNotice(notice) {
      this.axios
        .get(
          "/GeoProblemSolving/notice/delete" + "?noticeId=" + notice.noticeId
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data == "Success") {
            this.readNoticeInfoModal = false;
            this.$Message.success("delete notification success.");
            if (notice.state == "unread") {
              this.$emit("readNotification");
            }
            this.loadNotifications();
          } else {
            this.$Message.error("delete notification fail.");
          }
        })
        .catch((err) => {
          this.$Message.error("delete notification fail.");
        });
    },
    readAllNotice() {
      var unreadList = this.noticeList.filter(function (notice) {
        if (notice.state == "unread") {
          return notice;
        }
      });
      var unreadCount = unreadList.length;
      for (var i = 0; i < unreadList.length; i++) {
        this.axios
          .get(
            "/GeoProblemSolving/notice/read" +
              "?noticeId=" +
              unreadList[i].noticeId
          )
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({ name: "Login" });
            } else if (res.data == "Success") {
              this.$emit("readNotification");
              this.$store.commit("getUserInfo");
              if (--unreadCount == 0) {
                this.loadNotifications();
              }
            } else {
              this.$Message.error("update notification fail.");
            }
          })
          .catch((err) => {
            this.$Message.error("update notification fail.");
          });
      }
    },
    readNotice(noticeId) {
      this.axios
        .get("/GeoProblemSolving/notice/read" + "?noticeId=" + noticeId)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data == "Success") {
            this.noticeInfoModal = false;
            this.$emit("readNotification");
            this.$store.commit("getUserInfo");
            this.loadNotifications();
          } else {
            this.$Message.error("update notification fail.");
          }
        })
        .catch((err) => {
          this.$Message.error("update notification fail.");
        });
    },
    gotoWork(noticeId, subProjectId) {
      //路由跳转好像和回调的关系有点问题，这样在回调之前就已经跳转了，回调的内容好像没啥用
      this.axios
        .get("/GeoProblemSolving/notice/read" + "?noticeId=" + noticeId)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data == "Success") {
            this.$emit("readNotification");
            this.loadNotifications();
          } else {
            this.$Message.error("update notification fail.");
          }
        })
        .catch((err) => {
          this.$Message.error("update notification fail.");
        });

      this.$router.push(`./project/${id}/subproject`);
    },
    refuseApply(apply) {
      let updateApply = new URLSearchParams();
      updateApply.append("noticeId", apply.noticeId);
      updateApply.append("content.approve", "false");
      updateApply.append("state", "read");
      this.axios
        .post("/GeoProblemSolving/notice/update", updateApply)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data == "Success") {
            this.noticeInfoModal = false;
            this.$emit("readNotification");
            this.loadNotifications();
            let replyNotice = {};
            replyNotice["recipientId"] = apply.content.userId;
            replyNotice["type"] = "notice";
            replyNotice["content"] = {
              title: "Result for application",
              description:
                "Sorry, you were refused to join the activity: " +
                apply.content.activityName +
                " .",
              projectId: apply.content.projectId,
              activityId: apply.content.activityId,
              activityName: apply.content.activityName,
              activityLevel: apply.content.activityLevel,
            };
            this.axios
              .post("/GeoProblemSolving/notice/save", replyNotice)
              .then((result) => {
                if (result.data == "Success") {
                  this.$emit("sendNotice", {
                    type: "Notice",
                    recipientId: apply.content.userId,
                  });
                } else {
                  this.$Message.error("reply fail.");
                }
              })
              .catch((err) => {
                this.$Message.error("reply fail.");
              });
          } else {
            this.$Message.error("update notification fail.");
          }
        })
        .catch((err) => {
          this.$Message.error("update notification fail.");
        });
    },
    approveApply(apply) {
      let updateApply = new URLSearchParams();
      updateApply.append("noticeId", apply.noticeId);
      updateApply.append("content.approve", "true");
      updateApply.append("state", "read");
      // 更新通知
      this.axios
        .post("/GeoProblemSolving/notice/update", updateApply)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data == "Success") {
            this.noticeInfoModal = false;
            this.$emit("readNotification");
            this.loadNotifications();

            //update project members
            this.joinActivity(apply);

            //reply to applicant
            let replyNotice = {};
            replyNotice["recipientId"] = apply.content.userId;
            replyNotice["type"] = "notice";
            replyNotice["content"] = {
              title: "Result for application",
              description:
                "Welcome to join the activity: " +
                apply.content.activityName +
                " .",
              projectId: apply.content.projectId,
              activityId: apply.content.activityId,
              activityName: apply.content.activityName,
              activityLevel: apply.content.activityLevel,
            };
            this.axios
              .post("/GeoProblemSolving/notice/save", replyNotice)
              .then((result) => {
                if (result.data == "Success") {
                  this.$emit("sendNotice", {
                    type: "Notice",
                    recipientId: apply.content.userId,
                  });
                  let resultEmailBody = {};
                  resultEmailBody["recipient"] = apply.content.userEmail;
                  resultEmailBody["mailTitle"] = "Join project result";
                  resultEmailBody["mailContent"] =
                    "Hello, " +
                    apply.content.userName +
                    ", Welcome to join the activity: " +
                    apply.content.activityName +
                    " .";
                  this.axios
                    .post("/GeoProblemSolving/email/send", resultEmailBody)
                    .then((res) => {
                      if (res.data == "Success") {
                        this.$Notice.success({
                          title: "Result for application",
                          desc: "The process result email has been sent,if he/she doesn't online,the email will remind the joiner in time.",
                        });
                        this.$emit("sendNotice", {
                          type: "Notice",
                          recipientId: apply.content.userId,
                        });
                      } else {
                        this.$Notice.error({
                          title: "Email send fail",
                          desc: "The invitation isn't be sent successfully.",
                        });
                      }
                    })
                    .catch((err) => {
                      console.log(err.data);
                    });
                } else {
                  this.$Message.error("reply fail.");
                }
              })
              .catch((err) => {
                this.$Message.error("reply fail.");
              });

            // 发送审核通过的邮件
            // let resultEmailBody = {};
            // resultEmailBody["recipient"] =
          } else {
            this.$Message.error("update notification fail.");
          }
        })
        .catch((err) => {
          this.$Message.error("update notification fail.");
        });
    },
    joinActivity(apply) {
      let url = "";
      if (apply.content.activityLevel == 0) {
        url =
          "/GeoProblemSolving/project/" +
          apply.content.activityId +
          "/user?userId=" +
          apply.content.userId;
      } else if (apply.content.activityLevel == 1) {
        url =
          "/GeoProblemSolving/subproject/" +
          apply.content.activityId +
          "/user?userId=" +
          apply.content.userId;
      } else if (apply.content.activityLevel > 1) {
        url =
          "/GeoProblemSolving/activity/" +
          apply.content.activityId +
          "/user?userId=" +
          apply.content.userId;
      } else {
        return;
      }

      this.axios
        .post(url)
        .then((res) => {
          if (res.data.code == 0) {
            this.$Message.info("Successfully approval.");

            // // update activity doc
            // this.operationApi.participantUpdate(
            //   apply.content.activityId,
            //   "join",
            //   apply.content.userId,
            //   apply.content.userName,
            //   "ordinary-member",
            //   apply.content.userDomain
            // );
          } else if (res.data.code == -3) {
            this.$Message.info(
              "The applicant has already been a member of the activity: " +
                apply.content.activityName +
                " ."
            );
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
.customCard {
  opacity: 0.95;
}

.badge {
  display: inline-block;
  min-width: 10px;
  padding: 3px 7px;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
  color: #fff;
  text-align: center;
  white-space: nowrap;
  vertical-align: baseline;
  background-color: #999;
  border-radius: 10px;
}

.noticeDeleteBtn {
  float: right;
  color: #ff7800;
  cursor: pointer;
}

.detailContent {
  padding: 15px 10px;
  height: 500px;
  overflow-y: auto;
  background-color: #f7f7f7;
}

.applyBtn {
  float: right;
  margin: 25px 10px;
  cursor: pointer;
  size: 35px;
}
</style>

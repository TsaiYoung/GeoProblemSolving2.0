<template>
  <div>
    <Badge :count="receivedChatMsgs.length" id="msgPanelBtn">
      <Button
        @click="openPanel = !openPanel"
        title="Mini chatbox"
        shape="circle"
        size="large"
      >
        <Icon type="ios-chatboxes" size="30" color="#2d8cf0" />
      </Button>
    </Badge>
    <div id="msgPanel" v-show="openPanel">
      <Tabs v-model="msgType" type="card">
        <TabPane name="chats" label="Chat" style="margin-top: -16px">
          <div>
            <div class="contentPanel">
              <div class="contentBody topnav_box" id="contentBody" style="height: 355px">
                  <div
                    style="display: flex"
                    v-for="(item, index) in allChatMsgs"
                    :key="index"
                  >
                    <template v-if="item.type == 'members'">
                      <div class="chat-notice">{{ item.content }}</div>
                    </template>
                    <template
                      v-else-if="item.sender.userId === $store.getters.userId"
                    >
                      <div style="width: 95%">
                        <div class="chat-bubble-r chat-bubble-right">
                          {{ item.content }}
                          <Icon
                            type="ios-pulse"
                            color="red"
                            v-show="item.status === 'sending'"
                            title="Message sending"
                            class="msg-status"
                          />
                          <Icon
                            type="md-alert"
                            color="red"
                            v-show="item.status === 'failed'"
                            title="Failed to send this message"
                            class="msg-status"
                          />
                        </div>
                      </div>
                      <div class="user_detail">
                        <div class="u_img">
                          <img
                            v-if="
                              item.sender.avatar != undefined &&
                              item.sender.avatar != ''
                            "
                            :src="item.sender.avatar"
                            :title="item.sender.name"
                            class="user_img"
                            style="
                              width: 25px;
                              height: 25px;
                              vertical-align: middle;
                              border-radius: 1em;
                            "
                          />
                          <avatar
                            class="user_img"
                            :username="item.sender.name"
                            :size="25"
                            :title="item.sender.name"
                            v-else
                          ></avatar>
                        </div>
                      </div>
                    </template>
                    <template v-else>
                      <div
                        class="user_detail"
                        style="margin-left: 2.5%; margin-top: 5px"
                      >
                        <div class="u_img">
                          <img
                            v-if="
                              item.sender.avatar != undefined &&
                              item.sender.avatar != ''
                            "
                            :src="$store.state.UserServer + item.sender.avatar"
                            :title="item.sender.name"
                            class="user_img"
                            style="
                              width: 25px;
                              height: 25px;
                              vertical-align: middle;
                              border-radius: 1em;
                            "
                          />
                          <avatar
                            class="user_img"
                            :username="item.sender.name"
                            :size="30"
                            :title="item.sender.name"
                            v-else
                          ></avatar>
                        </div>
                      </div>
                      <div style="width: 95%">
                        <div class="chat-bubble-l chat-bubble-left">
                          {{ item.content }}
                        </div>
                      </div>
                    </template>
                  </div>
              </div>
              <div class="contentFooter">
                <div class="input_panel">
                  <Input
                    placeholder="Enter message..."
                    class="message_input"
                    v-model="typingMsg"
                    @keyup.enter.native="send(typingMsg)"
                  />
                </div>
                <div class="send_panel">
                  <Button class="send_message_btn" @click="send(typingMsg)"
                    >Send</Button
                  >
                </div>
              </div>
            </div>
          </div>
          <div class="message-records" v-show="recordsShow">
            <div style="margin: 5px 0 0 5px; color: #5093f8">
              Records
              <span
                style="float: right; margin-right: 10px; cursor: pointer"
                v-if="!recordListShow"
                @click="detailRecords('')"
                ><Icon type="md-arrow-round-back"
              /></span>
            </div>
            <Divider size="small" />
            <vue-scroll :ops="scrollOps" style="height: 335px; margin-top: 5px">
              <template v-if="recordListShow">
                <template
                  v-if="msgRecords != undefined && msgRecords.length > 0"
                >
                  <Card
                    class="records-item"
                    v-for="record in msgRecords"
                    :key="record.recordId"
                  >
                    <h4 style="margin-bottom: 5px">{{ record.createdTime }}</h4>
                    <div class="message-text" v-if="getRecordsFinish">
                      Participants:
                      <span
                        v-for="user in record.participants"
                        :key="user.userId"
                      >
                        <img
                          v-if="user.avatar != undefined && user.avatar != ''"
                          :src="$store.state.UserServer + user.avatar"
                          :title="user.name"
                          style="
                            width: 25px;
                            height: 25px;
                            vertical-align: middle;
                            border-radius: 1em;
                            margin-left: 5px;
                          "
                        />
                        <avatar
                          :username="user.name"
                          :size="25"
                          :title="user.name"
                          style="margin-left: 5px;"
                          v-else
                        ></avatar>
                      </span>
                    </div>
                    <div
                      style="
                        height: 14px;
                        background-color: #f5f5f5;
                        font-size: 12px;
                        text-align: center;
                        cursor: pointer;
                        margin: 5px -15px 0 -15px;
                      "
                      @click="detailRecords(record.recordId)"
                    >
                      More...
                    </div>
                  </Card>
                </template>
                <template v-else
                  ><Card style="font-size: 12px; text-align: center" dis-hover
                    >No records</Card
                  ></template
                >
              </template>
              <Card v-else>
                <div
                  v-for="(list, index) in msgRecordDatails"
                  :key="index"
                  class="record-item"
                >
                  <div style="margin-bottom: 2.5px; font-size: 12px">
                    <div class="message-from-id">
                      <div
                        style="color: #37881f"
                        v-if="list.sender.userId == $store.getters.userId"
                      >
                        {{ list.sender.name }}
                      </div>
                      <div class="message-from-name" v-else>
                        {{ list.sender.name }}
                      </div>
                    </div>
                    <div class="message-time">{{ list.time }}</div>
                    <div>
                      <template
                        v-if="list.type === 'message_pic'"
                        class="message-piture-border"
                      >
                        <img
                          :src="list.content"
                          class="message-piture-content"
                        />
                      </template>
                      <template v-else-if="list.type === 'message_tool'">
                        <simple-public-card
                          :toolFrom="JSON.parse(list.content)"
                          :isOpenTool="isOpenTool"
                          class="tool-outer"
                        ></simple-public-card>
                      </template>
                      <template v-else class="message-item-content">{{
                        list.content
                      }}</template>
                    </div>
                  </div>
                </div>
              </Card>
            </vue-scroll>
          </div>
        </TabPane>
        <Button
          @click="openChatRecords()"
          size="small"
          slot="extra"
          title="Message records"
          v-show="msgType == 'chats'"
        >
          <Icon type="md-time" />
        </Button>
      </Tabs>
    </div>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
export default {
  components: {
    Avatar,
  },
  props: ["activityInfo"],
  data() {
    return {
      //basic
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      participants: [],
      msgType: "chats",
      unreadRecords: 0,
      unreadChats: 0,
      openPanel: false,
      // chats
      typingMsg: "",
      receivedChatMsgs: [],
      allChatMsgs: [],
      // records
      recordsShow: false,
      recordListShow: true,
      getRecordsFinish: false,
      msgRecords: [],
      msgRecordDatails: [],
      aid: "",
    };
  },
  computed: {
    avatar() {
      let avatarUrl = this.$store.state.UserServer + this.$store.getters.avatar;
      return avatarUrl;
    },
  },
  watch: {},
  updated: function () {
    if (this.openPanel) {
      for (let i = 0; i < this.receivedChatMsgs.length; i++) {
        this.allChatMsgs.push(this.receivedChatMsgs.shift());
      }
    }
    this.$nextTick(() => {
      //滑块跨到最底端
      var div = this.$el.querySelector(".contentBody");
      div.scrollTop = div.scrollHeight;
    });
  },
  mounted() {
    this.startWebSocket();
    this.getParticipants();

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
            this.participants = res.data.data.members;
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    openChatRecords() {
      if (this.recordsShow) {
        this.recordsShow = false;
      } else {
        this.recordsShow = true;
        this.axios
          .get("/GeoProblemSolving/msgRecords/records/" + this.activityInfo.aid)
          .then((res) => {
            if (res.data.code === 0) {
              this.msgRecords = [];
              for(let i=0;i<res.data.data.length;i++){
                let record = res.data.data[i];
                let date = new Date(record.createdTime)
                record.createdTime = date.Format("yyyy-MM-dd HH:mm:ss");
                this.msgRecords.push(record);
              }
              console.log(this.msgRecords);
              this.getRecordsFinish = true;
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });
      }
    },
    detailRecords(recordId) {
      if (this.recordListShow) {
        this.recordListShow = false;
        this.axios
          .get("/GeoProblemSolving/msgRecords/record/" + recordId)
          .then((res) => {
            if (res.data.code === 0) {

              this.msgRecordDatails = [];
              for(let i=0;i<res.data.data.length;i++){
                let message = res.data.data[i];
                let date = new Date(message.time)
                message.time = date.Format("yyyy-MM-dd HH:mm:ss");
                this.msgRecordDatails.push(message);
              }
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });
      } else {
        this.recordListShow = true;
        this.msgRecordDatails = [];
      }
    },
    startWebSocket() {
      this.aid = this.activityInfo.aid;
      this.socketApi.initWebSocket("MsgServer/" + this.activityInfo.aid);
      let send_msg = {
        type: "test",
        sender: this.$store.getters.userId,
        receivers: [this.$store.getters.userId],
      };
      this.socketApi.sendSock("MsgServer/" + this.activityInfo.aid, send_msg, this.getSocketConnect);
    },
    getSocketConnect(data) {
      this.receivedChatMsgs = [];
      let chatMsg = data;
      if (chatMsg.type === "members") {
        this.memberNoticeClear();
        if (chatMsg.behavior == "on") {
          chatMsg["content"] = chatMsg.activeUser.name + " join the meeting.";
          this.receivedChatMsgs.push(chatMsg);
        } else if (chatMsg.behavior == "off") {
          chatMsg["content"] = chatMsg.activeUser.name + " leave the room.";
          this.receivedChatMsgs.push(chatMsg);
        }
      } else if (data.type === "message") {
        //判断消息的发出者
        if (chatMsg.content != "") {
          let msgCheck = false; // 更新消息状态

          for (let i = 0; i < this.allChatMsgs.length; i++) {
            let time = new Date(this.allChatMsgs[i].time);
            let current = new Date();
            let threshold = 1000 * 60 * 3;
            if (this.allChatMsgs[i].time == chatMsg.time) {
              this.allChatMsgs[i].status = "done";
              msgCheck = true;
            } else if (
              (this.allChatMsgs[i].status ==
                "sending" && current - time > threshold)
            ) {
              this.allChatMsgs[i].status = "failed";
            }
          }

          if (!msgCheck) {
            this.receivedChatMsgs.push(chatMsg);
          }
        }
      } else if (chatMsg.type == "message-cache") {
        for (let i = 0; i < chatMsg.content.length; i++) {
          if (chatMsg.content[i] != "") {
            this.receivedChatMsgs.push(chatMsg.content[i]);
          }
        }
      } else if (chatMsg.type == "message-store") {
        // this.operationApi.communicationRecord(
        //   this.activityInfo.aid,
        //   "",
        //   "",
        //   "",
        //   chatMsg.recordId,
        //   chatMsg.time,
        //   chatMsg.participants
        // );
        // chatMsg["type"] = "members";
        // chatMsg["content"] = "You have the only person in the meeting.";
        // this.memberNoticeClear();
        // this.receivedChatMsgs.push(chatMsg);
      } else if (chatMsg.type == "test") {
        chatMsg["type"] = "members";
        chatMsg["content"] = "You have joined the meeting.";
        this.memberNoticeClear();
        this.receivedChatMsgs.push(chatMsg);
      }
    },
    //chat
    send(msg) {
      //修改时间格式使其统一
      this.typingMsg = msg;
      let myDate = new Date().Format("yyyy-MM-dd HH:mm:ss");
      let current_time = myDate.toLocaleString(); //获取日期与时间
      // 消息不为空
      if (!this.typingMsg.match(/^[ ]*$/)) {
        if (this.socketApi.getSocketInfo("MsgServer/" + this.activityInfo.aid).linked) {
          let send_msg = {
            type: "message",
            sender: this.$store.getters.userId,
            content: this.typingMsg,
            time: current_time,
          };
          this.socketApi.sendSock("MsgServer/" + this.activityInfo.aid, send_msg, this.getSocketConnect);
          send_msg["status"] = "sending";
          send_msg["sender"] = {
            userId: this.$store.getters.userId,
            name: this.$store.getters.userName,
            avatar: this.$store.getters.avatar === "" ? "" : this.avatar,
          };
          this.allChatMsgs.push(send_msg);
        } else {
          let chatMsg = {
            type: "members",
            content: "Lost the connection with others.",
          };
          this.memberNoticeClear();
          this.allChatMsgs.push(chatMsg);
        }
      }
      this.typingMsg = "";
    },
    memberNoticeClear(){
      for(let i = this.allChatMsgs.length - 1; i >= 0; i--) {
        if(this.allChatMsgs[i].type == "members"){
          this.allChatMsgs.splice(i, 1);
        }
      }
    },
  },
  beforeDestroy() {
    this.socketApi.close("MsgServer/" + this.aid);
  }
};
</script>
<style scoped>
.ivu-divider-horizontal {
  margin: 10px 0 0 0;
}
.content {
  margin-left: 10px;
  word-break: break-word;
}
#msgPanelBtn {
  z-index: 1001;
  position: fixed;
  right: 30px;
  bottom: 70px;
  cursor: pointer;
  background-color: #fff;
}
#msgPanel {
  z-index: 1000;
  width: 340px;
  height: 450px;
  position: fixed;
  right: 55px;
  bottom: 85px;
  background-color: #5093f8;
  padding: 5px;
}
#msgPanel > .ivu-tabs-card > .ivu-tabs-content > .ivu-tabs-tabpane {
  background: #fff;
  padding: 10px;
}
#msgPanel > .ivu-tabs.ivu-tabs-card > .ivu-tabs-bar .ivu-tabs-tab {
  border-color: transparent;
}
#msgPanel > .ivu-tabs-card > .ivu-tabs-bar .ivu-tabs-tab-active {
  border-color: #fff;
}
.contentPanel {
  /* display: flex;
  flex-direction: column;
  flex: auto; */
  height: 100%;
  width: 100%;
}
.contentBody {
  flex: 1;
  padding: 5px;
  overflow-y: auto;
}
.chat-notice {
  color: lightgrey;
  /* position: absolute; */
  margin: auto;
}
.user_detail {
  height: 60px;
}
.msg-status {
  position: relative;
  float: right;
  margin-top: 3px;
  margin-left: 10px;
}
.chat-bubble-r {
  position: relative;
  margin: 12px;
  padding: 5px 8px;
  word-break: break-word;
  background: #fff;
  border: 1px solid lightgray;
  border-radius: 5px;
  min-width: 20%;
  float: right;
}
.chat-bubble-l {
  position: relative;
  margin: 12px;
  padding: 5px 8px;
  word-break: break-word;
  background: #fff;
  border: 1px solid lightgray;
  border-radius: 5px;
  min-width: 20%;
  float: left;
}
.chat-bubble-left:before {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  left: -20px;
  top: 5px;
  border: 10px solid;
  border-color: transparent #989898 transparent transparent;
}
.chat-bubble-left:after {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  left: -16px;
  top: 7px;
  border: 8px solid;
  border-color: transparent #989898 transparent transparent;
}
.chat-bubble-right:before {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  right: -20px;
  top: 5px;
  border: 10px solid;
  border-color: transparent transparent transparent lightgray;
}
.chat-bubble-right:after {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  right: -16px;
  top: 7px;
  border: 8px solid;
  border-color: transparent transparent transparent lightgray;
}
.contentFooter {
  display: flex;
  height: 30px;
}
.user_img {
  background-color: lightblue;
  margin-top: 12px;
  margin-left: 5px;
}
.input_panel {
  width: 85%;
}
.message_input {
  height: 40px;
}
.send_panel {
  display: flex;
}
.send_message_btn {
  height: 32px;
}
.message-records {
  width: 310px;
  height: 390px;
  position: fixed;
  right: 10px;
  bottom: 8px;
  background-color: white;
  padding: 5px;
  border-radius: 0.5em;
  box-shadow: 0px 0px 10px gray;
}
.records-item {
  margin-bottom: 10px;
}
.message-text {
  font-size: 12px;
  display: flex;
  width: 290px;
}
.message-sender {
  color: #15b169;
}
.record-item {
  padding: 5px 0;
}
.message-time {
  color: lightgray;
}
.message-from-name {
  color: #2d8cf0;
  width: 300px;
  font-size: 16px;
}
.message-piture-border {
  max-height: 300px;
  max-width: 400px;
}
.message-piture-content {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 100%;
}
.tool-outer {
  width: 120px;
}
.message-item-content {
  color: gray;
  word-wrap: break-word;
  width: 100%;
}

.topnav_box::-webkit-scrollbar {
  width: 5px;
  height:10px;
  background-color:transparent;
}
.topnav_box::-webkit-scrollbar-track {
  border-radius: 10px;
  background-color:transparent;

}
.topnav_box::-webkit-scrollbar-thumb {
  border-radius: 10px;
  height: 30px;
  background-color:#b5b1b1;
}
</style>

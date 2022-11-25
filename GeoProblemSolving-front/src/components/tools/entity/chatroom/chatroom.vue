<style scoped>
.extendedPanel >>> .ivu-tabs-bar {
  margin-bottom: 0;
}
.chatPanel {
  display: flex;
  width: 100%;
}

.extendedPanel {
  width: 300px;
  height: 100%;
  /* z-index: 999; */
}

/* 右侧布 */
.contentPanel {
  /* display: flex;
  flex-direction: column;
  flex: auto; */
  height: 350px;
  width: 100%;
}

/* 信息主题列表显示窗口 */
.contentBody {
  flex: 1;
  /* padding: 25px; */
  overflow-y: auto;
  background-color: #afafa53a;
}

/* 发送信息部分 */
.contentFooter {
  height: 160px;
  width: 100%;
  padding: 4px 5px 4px 5px;
  background-color: lightgray;
}
.contentFooter2 {
  height: 200px;
  width: 100%;
  padding: 4px 5px 4px 5px;
  background-color: lightgray;
}

.message_bar {
  display: flex;
  height: 52px;
}

.u_avater {
  padding: 10px;
  width: 2.5%;
  margin-right: 5%;
}

.send_emoji {
  /* text-align: center; */
  width: 35px;
  margin: 0 10px;
}
.send_emoji >>> #btn-emoji-default {
  margin: 0;
}
.send_emoji >>> #btn-emoji-default > div > img.emoji {
  width: 25px;
  height: 25px;
}
.send_emoji >>> #btn-emoji-default:hover {
  -webkit-transform: translateY(-4px);
  -ms-transform: translateY(-4px);
  transform: translateY(-4px);
  -webkit-transition: all 0.2s ease-out;
  transition: all 0.2s ease-out;
}
.chatbox_btn {
  /* text-align: center; */
  width: 25px;
  margin: 7px 10px 0 0;
}
.send_people {
  margin-right: 2px;
}
.send_icon:hover {
  cursor: pointer;
  -webkit-transform: translateY(-4px);
  -ms-transform: translateY(-4px);
  transform: translateY(-4px);
  -webkit-transition: all 0.2s ease-out;
  transition: all 0.2s ease-out;
}
.select {
  width: 150px;
}
/* .select >>> .el-select > .el-input__inner {
  height: 35px;
} */

/* 发送信息部分结束 */

.inputText >>> textarea.ivu-input {
  border: none;
  height: auto;
  width: 100%;
  resize: none;
  font-size: 16px;
}
.sent_to_tip {
  line-height: 40px;
  font-size: 18px;
  color: rgb(92, 93, 94);
  margin-right: 10px;
  font-weight: 600;
}
.chat-notice {
  color: darkgreen;
  width: 90%;
  text-align: center;
  margin: 10px 0;
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
</style>
<template>
  <Row>
    <div class="chatPanel">
      <div class="contentPanel">
        <div
          v-if="useChatCss"
          class="contentBody"
          ref="contentBody"
          style="height: calc(100vh - 160px)"
        >
          <div v-for="(list, index) in msglist" :key="index">
            <template v-if="list.type == 'members'">
              <div class="chat-notice">{{ list.content }}</div>
            </template>
            <template v-else-if="list.sender.userId === userInfo.userId">
              <bubble-right :message="list"></bubble-right>
            </template>
            <template v-else>
              <bubble-left :message="list"></bubble-left>
            </template>
          </div>
        </div>
        <div
          v-else
          class="contentBody"
          ref="contentBody"
          style="height: calc(100vh - 200px)"
        >
          <div v-for="(list, index) in msglist" :key="index">
            <template v-if="list.type == 'members'">
              <div class="chat-notice">{{ list.content }}</div>
            </template>
            <template v-else-if="list.sender.userId === userInfo.userId">
              <bubble-right :message="list"></bubble-right>
            </template>
            <template v-else>
              <bubble-left :message="list"></bubble-left>
            </template>
          </div>
        </div>
        <div class="contentFooter" v-if="useChatCss">
          <Row>
            <Col :span="24">
              <Row>
                <Col :span="14" style="display: flex">
                  <div class="send_emoji">
                    <twemoji-picker
                      :emojiData="emojiDataAll"
                      :emojiGroups="emojiGroups"
                      :pickerHeight="300"
                      :pickerWidth="500"
                      @emojiUnicodeAdded="addEmoji"
                      title="Emoji"
                    ></twemoji-picker>
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-time"
                      @click.native="showRecords()"
                      size="25"
                      title="Messge records"
                      class="send_icon"
                    />
                  </div>
                  <div class="chatbox_btn" title="Send image">
                    <Upload multiple :before-upload="beforeUploadPic" action>
                      <Icon type="md-image" size="25" class="send_icon" />
                    </Upload>
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-body"
                      size="25"
                      class="send_icon"
                      title="Online participants"
                      @click.native="showParticipants()"
                    />
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-hammer"
                      size="25"
                      class="send_icon"
                      title="Send tool"
                      @click.native="toolModalShow = true"
                    />
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-water"
                      size="25"
                      class="send_icon"
                      title="Geographic concepts"
                      @click.native="showConcepts()"
                    />
                  </div>

                  <div class="chatbox_btn">
                    <Icon
                      type="md-send"
                      @click.native="sendMsg"
                      title="Send message"
                      size="25"
                      class="send_icon"
                    />
                  </div>
                </Col>
                <Col :span="10" style="width: 250px; display: flex">
                  <div class="send_people">
                    <div class="sent_to_tip">Send to</div>
                  </div>
                  <div class="send_people">
                    <el-select v-model="sendToMemberId" class="select">
                      <el-option
                        v-for="item in selectReceiver"
                        :value="item.userId"
                        :label="item.name"
                        :key="item.userId"
                        >{{ item.name }}</el-option
                      >
                    </el-select>
                  </div>
                </Col>
              </Row>
              <Row>
                <Col :span="24">
                  <Input
                    :rows="4"
                    type="textarea"
                    class="inputText"
                    v-model="message"
                    @keydown.native="handleKeyCode($event)"
                  ></Input>
                  <!-- <div id="a" contentEditable="true" >{{message}}</div> -->
                </Col>
              </Row>
            </Col>
          </Row>
        </div>
        <div class="contentFooter2" v-else>
          <Row>
            <Col :span="24">
              <Row>
                <Col :span="14" style="display: flex">
                  <div class="send_emoji">
                    <twemoji-picker
                      :emojiData="emojiDataAll"
                      :emojiGroups="emojiGroups"
                      :pickerHeight="300"
                      :pickerWidth="500"
                      @emojiUnicodeAdded="addEmoji"
                      title="Emoji"
                    ></twemoji-picker>
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-time"
                      @click.native="showRecords()"
                      size="25"
                      title="Messge records"
                      class="send_icon"
                    />
                  </div>
                  <div class="chatbox_btn" title="Send image">
                    <Upload multiple :before-upload="beforeUploadPic" action>
                      <Icon type="md-image" size="25" class="send_icon" />
                    </Upload>
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-body"
                      size="25"
                      class="send_icon"
                      title="Online participants"
                      @click.native="showParticipants()"
                    />
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-hammer"
                      size="25"
                      class="send_icon"
                      title="Send tool"
                      @click.native="toolModalShow = true"
                    />
                  </div>
                  <div class="chatbox_btn">
                    <Icon
                      type="md-water"
                      size="25"
                      class="send_icon"
                      title="Geographic concepts"
                      @click.native="showConcepts()"
                    />
                  </div>

                  <div class="chatbox_btn">
                    <Icon
                      type="md-send"
                      @click.native="sendMsg"
                      title="Send message"
                      size="25"
                      class="send_icon"
                    />
                  </div>
                </Col>
                <Col :span="10" style="width: 250px; display: flex">
                  <div class="send_people">
                    <div class="sent_to_tip">Send to</div>
                  </div>
                  <div class="send_people">
                    <el-select v-model="sendToMemberId" class="select">
                      <el-option
                        v-for="item in selectReceiver"
                        :value="item.userId"
                        :label="item.name"
                        :key="item.userId"
                        >{{ item.name }}</el-option
                      >
                    </el-select>
                  </div>
                </Col>
              </Row>
              <Row>
                <Col :span="24">
                  <Input
                    :rows="4"
                    type="textarea"
                    class="inputText"
                    v-model="message"
                    @keydown.native="handleKeyCode($event)"
                  ></Input>
                  <!-- <div id="a" contentEditable="true" >{{message}}</div> -->
                </Col>
              </Row>
            </Col>
          </Row>
        </div>
      </div>

      <div class="extendedPanel" v-show="extendedPeopleShow">
        <Tabs type="card">
          <TabPane label="Participants">
            <Card
              v-for="(participant, index) in participants"
              :key="'online' + index"
              style="margin: 2.5%"
              :padding="5"
            >
              <div style="display: flex; align-items: center">
                <div class="memberImg" style="position: relative">
                  <img
                    v-if="
                      participant.avatar != '' &&
                      participant.avatar != undefined &&
                      participant.avatar != null
                    "
                    :src="avatarUrl(participant.avatar)"
                    style="width: 40px; height: 40px"
                  />
                  <avatar
                    v-else
                    :username="participant.name"
                    :size="40"
                    :rounded="false"
                  ></avatar>
                </div>
                <div class="memberDetail">
                  <div class="memberName">
                    <span>{{ participant.name }}</span>
                  </div>
                  <div class="memberRole">
                    <span>{{ participant.role }}</span>
                  </div>
                </div>
              </div>
            </Card>
          </TabPane>
        </Tabs>
      </div>

      <div class="extendedPanel" v-show="extendedRecordShow">
        <Tabs type="card">
          <TabPane label="Records">
            <records
              :groupId="activityInfo.aid"
              :userId="$store.getters.userId"
              :panelHeight="panelHeight"
            ></records>
          </TabPane>
        </Tabs>
      </div>

      <div class="extendedPanel" v-if="extendedConceptsShow">
        <Tabs type="card">
          <TabPane label="Concepts">
            <concepts :msgConcepts="msgConcepts"></concepts>
          </TabPane>
          <TabPane label="ConceptMap">
            <conceptMap
              :msgConceptMap="msgConceptMap"
              :panelHeight="panelHeight"
            ></conceptMap>
          </TabPane>
        </Tabs>
      </div>

      <Modal v-model="toolModalShow" width="820" footer-hide>
        <tool-modal
          :userId="$store.getters.userId"
          @selectedTools="sendTools"
          footer-hide
        ></tool-modal>
      </Modal>
    </div>
    <!-- </Col> -->
  </Row>
</template>
<script>
import toolModal from "@/components/common/tools/toolModal";
import chatMember from "@/components/common/card/chat/chatMember";
import chatManager from "@/components/common/card/chat/chatManager";
import Avatar from "vue-avatar";
import { Upload } from "element-ui";
import { TwemojiPicker } from "@kevinfaguiar/vue-twemoji-picker";
import EmojiAllData from "@kevinfaguiar/vue-twemoji-picker/emoji-data/en/emoji-all-groups.json";
import EmojiDataAnimalsNature from "@kevinfaguiar/vue-twemoji-picker/emoji-data/en/emoji-group-animals-nature.json";
import EmojiDataFoodDrink from "@kevinfaguiar/vue-twemoji-picker/emoji-data/en/emoji-group-food-drink.json";
import EmojiGroups from "@kevinfaguiar/vue-twemoji-picker/emoji-data/emoji-groups.json";

import bubbleRight from "./components/bubbleRight";
import bubbleLeft from "./components/bubbleLeft";
import records from "./components/records";
import concepts from "./components/concepts";
import conceptMap from "./components/conceptMap";

export default {
  components: {
    Avatar,
    toolModal,
    "twemoji-picker": TwemojiPicker,
    "el-upload": Upload,
    bubbleRight,
    bubbleLeft,
    chatMember,
    chatManager,
    records,
    concepts,
    conceptMap,
  },

  computed: {
    emojiDataAll() {
      return EmojiAllData;
    },
    emojiGroups() {
      return EmojiGroups;
    },
  },

  data() {
    return {
      activityInfo: {},
      userInfo: {},
      participants: [],
      message: "",
      msglist: [],
      panelHeight: 0,
      panelWidth: 0,
      useChatCss: true,

      windowStatus: "focus", //监听浏览器失焦事件
      //在线成员
      extendedPeopleShow: false,
      //工具对话框
      toolModalShow: false,
      //语义概念库
      extendedConceptsShow: false,
      toolConceptsShow: false,
      //user infomation
      selectReceiver: [
        {
          name: "All",
          userId: "All",
        },
      ],
      sendToMemberId: "All",
      // msgRecords: [],
      //聊天记录
      extendedRecordShow: false,
      msgConcepts: [],
      msgConceptMap: [],
      tid: ""
    };
  },

  mounted() {
    this.reSize();
    window.addEventListener("resize", this.reSize);
    window.addEventListener("blur", this.winBlur, true); //监听浏览器失焦事件
    window.addEventListener("focus", this.winFocus, true); //监听浏览器失焦事件
    window.addEventListener("resize", this.initSize, true);
    window.addEventListener("message", this.getActivityInfo, true);

    this.initSize();
    this.supportNotify();
  },
  methods: {
    reSize() {
        if (window.innerWidth < 610) {
          this.useChatCss = false;
        } else {
          this.useChatCss = true;
        }
    },
    avatarUrl(url) {
      let avatarUrl = this.$store.state.UserServer + url;
      return avatarUrl;
    },
    getActivityInfo(event) {
      if (event.data.type === "activity") {
        this.activityInfo = event.data.activity;
        this.tid = event.data.tid;
        this.userInfo = event.data.user;
        // console.log(this.userInfo)
        this.participants.push(this.userInfo);
        this.startWebSocket();
      }
    },
    initSize() {
      $("#app").css("min-width", "0");
      $("#app").css("min-height", "0");
      this.panelHeight = window.innerHeight;
      this.panelWidth = window.innerWidth;
    },

    startWebSocket() {
      this.socketApi.initWebSocket("MsgServer/" + this.activityInfo.aid + this.tid);
      let send_msg = {
        type: "test",
        sender: this.$store.getters.userId,
        receivers: [this.$store.getters.userId],
        geoConcepts: false,
      };
      this.socketApi.sendSock(
        "MsgServer/" + this.activityInfo.aid + this.tid,
        send_msg,
        this.getSocketConnect
      );
    },

    sendMsg() {
      // 消息不为空
      if (!this.message.match(/^[ ]*$/)) {
        let send_msg = {
          type: "message",
          sender: this.$store.getters.userId,
          content: this.message,
          geoConcepts: this.extendedConceptsShow,
        };
        this.send(send_msg);
      }
      this.message = "";
    },
    sendPic(messageUrl) {
      let send_msg = {
        type: "message_pic",
        sender: this.$store.getters.userId,
        content: messageUrl,
        geoConcepts: false,
      };
      this.send(send_msg);
    },
    sendTools(tools) {
      let send_msg;
      tools.forEach((item) => {
        send_msg = {
          type: "message_tool",
          sender: this.$store.getters.userId,
          content: item,
          geoConcepts: false,
        };
        this.send(send_msg);
        this.toolModalShow = false;
      });
    },
    send(message) {
      // time
      let myDate = new Date().Format("yyyy-MM-dd HH:mm:ss");
      let current_time = myDate.toLocaleString(); //获取日期与时间
      message["time"] = current_time;
      // receivers
      if (this.sendToMemberId !== "All") {
        message["receivers"] = [this.sendToMemberId];
      }

      // send
      if (
        this.socketApi.getSocketInfo("MsgServer/" + this.activityInfo.aid + this.tid)
          .linked
      ) {
        this.socketApi.sendSock(
          "MsgServer/" + this.activityInfo.aid + this.tid,
          message,
          this.getSocketConnect
        ); //连接后台onopen方法

        message["status"] = "sending";
        message["sender"] = {
          userId: this.$store.getters.userId,
          name: this.$store.getters.userName,
        };
        this.msglist.push(message);
      } else {
        let chatMsg = {
          type: "members",
          content: "You are disconnecting with others.",
        };
        this.memberNoticeClear();
        this.msglist.push(chatMsg);
      }
    },
    async beforeUploadPic(file) {
      let formData = new FormData();
      formData.append("messPic", file);
      let data = await this.axios.post(
        "/GeoProblemSolving/chat/picture",
        formData
      );
      let messageUrl = `http://${window.location.host}${data.data.data}`;
      this.sendPic(messageUrl);
    },
    getSocketConnect(data) {
      let chatMsg = data; //data传回onopen方法里的值

      if (chatMsg.type === "members") {
        this.memberNoticeClear();
        if (chatMsg.behavior == "on") {
          chatMsg["content"] = chatMsg.activeUser.name + " join the meeting.";
          this.msglist.push(chatMsg);
        } else if (chatMsg.behavior == "off") {
          chatMsg["content"] = chatMsg.activeUser.name + " quit the meeting.";
          this.msglist.push(chatMsg);
        }
        this.updateParticipants(chatMsg.participants);
      } else if (
        chatMsg.type === "message" ||
        chatMsg.type === "message_pic" ||
        chatMsg.type === "message_tool"
      ) {
        //判断消息的发出者
        if (chatMsg.content != "") {
          let msgCheck = false; // 更新消息状态

          for (let i = 0; i < this.msglist.length; i++) {
            let time = new Date(this.msglist[i].time);
            let current = new Date();
            let threshold = 1000 * 60 * 3;
            if (
              this.msglist[i].status == "sending" &&
              this.msglist[i].time == chatMsg.time
            ) {
              this.msglist[i].status = "done";
              msgCheck = true;
            } else if (
              this.msglist[i].status == "sending" &&
              current - time > threshold
            ) {
              this.msglist[i].status = "failed";
            }
          }
          try {
            if (chatMsg.type === "message_tool") {
              let content = chatMsg.content;
              chatMsg.content = JSON.parse(content);
            }
          } catch (err) {
            console.log(err);
          }

          if (!msgCheck) {
            this.msglist.push(chatMsg);
            this.sendNotify(chatMsg);
          }

          // geographic concepts
          // if (chatMsg.frequency != undefined && chatMsg.geoConcepts != "") {
          //   this.msgConcepts = chatMsg.frequency;
          //   this.msgConceptMap = chatMsg.conceptMap;
          //   this.extendedConceptsShow = true;
          // }
        }
      } else if (chatMsg.type == "message-store") {
        // let operationId = this.operationApi.communicationRecord(
        //   this.activityInfo.aid,
        //   "",
        //   "",
        //   chatMsg.recordId,
        //   chatMsg.time,
        //   chatMsg.participants
        // );
        let computationResult = {
          type: "task-record-backend",
          oid: chatMsg.oid
        }
        window.parent.postMessage(computationResult, "*");
        // 生成临时操作记录
        // let resOperation = {
        //   id: operationId,
        //   type: "communication",
        //   resRef: resList[i].uid,
        //   operator: chatMsg.participants,
        // };
        // this.$store.commit("updateTempOperations", {
        //   behavior: "add",
        //   operation: resOperation,
        // });

        chatMsg["type"] = "members";
        chatMsg["content"] = "You have the only person in the meeting.";
        this.memberNoticeClear();
        this.msglist.push(chatMsg);
      } else if (chatMsg.type == "message-cache") {
        for (let i = 0; i < chatMsg.content.length; i++) {
          if (chatMsg.content[i] != "") {
            if (chatMsg.content[i].sender.userId == this.userInfo.userId) {
              chatMsg.content[i].status = "done";
            }
            this.msglist.push(chatMsg.content[i]);
          }
        }
      } else if (chatMsg.type === "concepts") {
        if (
          chatMsg.frequency != undefined &&
          chatMsg.geoConcepts != undefined
        ) {
          this.msgConcepts = chatMsg.frequency;
          this.msgConceptMap = chatMsg.conceptMap;
          this.extendedConceptsShow = true;
        }
      } else if (chatMsg.type == "test") {
        chatMsg["type"] = "members";
        chatMsg["content"] = "You have joined the meeting.";
        this.memberNoticeClear();
        this.msglist.push(chatMsg);
      }
    },
    updateParticipants(participants) {
      this.selectReceiver = [
        {
          name: "All",
          userId: "All",
        },
      ];
      for (let i = 0; i < participants.length; i++) {
        let user = participants[i];
        if (user.userId !== this.$store.getters.userId) {
          this.selectReceiver.push(user);
        }
      }
      this.participants = [];
      if (participants != undefined && participants.length > 1) {
        this.participants = participants;
      }
    },

    showRecords() {
      this.extendedRecordShow = !this.extendedRecordShow;
      this.extendedConceptsShow = false;
      this.extendedPeopleShow = false;
    },
    showConcepts() {
      this.extendedConceptsShow = !this.extendedConceptsShow;
      this.extendedRecordShow = false;
      this.extendedPeopleShow = false;
    },
    showParticipants() {
      this.extendedPeopleShow = !this.extendedPeopleShow;
      this.extendedRecordShow = false;
      this.extendedConceptsShow = false;
    },

    //notice
    winBlur() {
      this.windowStatus = "blur";
    },
    winFocus() {
      this.windowStatus = "focus";
    },

    supportNotify() {
      //检查浏览器是否支持
      if (!("Notification" in window)) {
        window.alert("This browser does not support desktop notification");
      } else {
        var Notification =
          window.Notification ||
          window.mozNotification ||
          window.webkitNotification;
        if (Notification && Notification.permission === "granted") {
          // 检查用户是否同意接受通知
          Notification.requestPermission();
        } else {
          // 否则我们需要向用户获取权限
          Notification.requestPermission();
        }
      }
    },

    sendNotify(msg) {
      if (this.windowStatus === "blur") {
        var notify = new Notification("You have got a new message", {
          tag: msg.sender.userId + msg.time,
          icon: require("@/assets/images/OGMS2.png"), //通知的缩略图,
          body: "from   " + msg.sender.name, //通知的具体内容
          renotify: true,
        });

        //通知显示
        notify.onshow = function () {
          //5s自行停止通知
          setTimeout(notify.close.bind(notify), 5000);
        };

        //单击通知，跳转到页面
        notify.onclick = function () {
          window.focus();
          notify.close();
        };

        //报错处理
        notify.onerror = function () {
          console.log("Error notice in chat tool message!");
        };

        //通知关闭
        notify.onclose = function () {
          console.log("Chat tool message notice is closed!");
        };
      }
    },

    addEmoji(emoji) {
      this.message = this.message + emoji;
      // console.log(this.message);
    },

    handleKeyCode(event) {
      if (event.keyCode == 13 && event.ctrlKey) {
        this.message += "\n"; //换行
      } else if (event.keyCode == 13) {
        this.sendMsg(); //提交的执行函数
        event.preventDefault(); //禁止回车的默认换行
      }
    },
    memberNoticeClear(){
      for(let i = this.msglist.length - 1; i >= 0; i--) {
        if(this.msglist[i].type == "members"){
          this.msglist.splice(i, 1);
        }
      }
    },
  },

  beforeDestroy() {
    this.socketApi.close("MsgServer/" + this.activityInfo.aid);

    window.removeEventListener("resize", this.reSize);
    window.removeEventListener("resize", this.initSize);
    window.removeEventListener("blur", this.winBlur, true); //监听浏览器失焦事件
    window.removeEventListener("focus", this.winFocus, true); //监听浏览器失焦事件
    window.removeEventListener("resize", this.initSize, true);
    window.removeEventListener("message", this.getActivityInfo, false);
  },

  beforeRouteEnter: (to, from, next) => {
    next((vm) => {
      if (!vm.$store.getters.userState || vm.$store.getters.userId == "") {
        vm.$router.push({
          name: "Login",
        });
      } else {
      }
    });
  },

  updated() {
    this.$nextTick(() => {
      //滑块跨到最底端
      var div = this.$el.querySelector(".contentBody");
      div.scrollTop = div.scrollHeight;
    });
  },
};
</script>

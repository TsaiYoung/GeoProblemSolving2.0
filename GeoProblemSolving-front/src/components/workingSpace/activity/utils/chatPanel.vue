<template>
  <Row>
    <div class="chatPanel">
      <transition name="left">
        <div class="contentPanel">
          <div class="contentHeader">
            <div class="chatObject">
              <div class="panelTitle">Meeting</div>
            </div>
            <div class="chatRecord">
              <Button type="default" @click="showRecords" size="small">Records</Button>
            </div>
          </div>
          <div class="contentBody" id="contentBody">
            <div style="display:flex" v-for="(list,index) in msglist" :key="index">
              <template v-if="list.type === 'notice'">
                <div class="chat-notice">{{list.content}}</div>
              </template>
              <template v-else-if="list.fromid === $store.getters.userId ">
                <div style="width:95%">
                  <div class="chat-bubble-r chat-bubble-right">{{list.content}}</div>
                </div>
                <div class="user_detail">
                  <div class="u_img">
                    <avatar class="user_img" :username="list.from" :size="35"></avatar>
                  </div>
                  <div class="u_name">{{list.from}}</div>
                </div>
              </template>
              <template v-else>
                <div class="user_detail" style="margin-left:2.5%;margin-top:5px;">
                  <div class="u_img">
                    <avatar class="user_img" :username="list.from" :size="35"></avatar>
                  </div>
                  <div class="u_name">
                    <span style="font-size:3px;">{{list.from}}</span>
                  </div>
                </div>
                <div style="width:95%">
                  <div class="chat-bubble-l chat-bubble-left">{{list.content}}</div>
                </div>
              </template>
            </div>
          </div>
          <div class="contentFooter">
            <div>
              <Input
                placeholder="Enter message..."
                icon="ios-link"
                v-model="message"
                type="textarea"
                :autosize="{ minRows: 5, maxRows: 5 }"
              />
              <div class="send_panel">
                <Button class="send_message_btn" @click="send(message)">Send</Button>
              </div>
            </div>
          </div>
        </div>
      </transition>
      <transition name="right">
        <div class="searchPanel" v-show="searchPanelShow">
          <div class="searchHeader">
            <p>Message Records</p>
          </div>
          <!-- 日期选择器 -->
          <div style="display:flex">
            <DatePicker
              type="date"
              placeholder="Select date"
              class="date_pick"
              format="yyyy-MM-dd"
              :value="query_date"
              @on-change="find"
            ></DatePicker>
          </div>
          <!-- 搜索框 -->
          <div class="search_msg">
            <Input search placeholder="Enter something..." />
          </div>
          <!-- 聊天记录列表 -->
          <div class="searchmessageList" id="searchmessageList">
            <div class="message_record_board">
              <div style="display:flex" v-for="(list,index) in currentPageData" :key="'A'+index">
                <div class="single_record">
                  <span style="color:#2d8cf0; margin-right:2%">{{list.from}}:</span>
                  <span style="color:lightgray; margin-right:2%">{{list.time}}</span>
                  <div style="color:gray">{{list.content}}</div>
                </div>
              </div>
            </div>
          </div>
          <!-- 分页 -->
          <div class="message_record_page">
            <Page
              :current="currentPage"
              :total="totalMsg"
              :page-size="pageSize"
              @on-change="changeRecordPage"
              simple
              ref="page"
            />
          </div>
        </div>
      </transition>
    </div>
  </Row>
</template>
<script>
import * as socketApi from "@/api/socket.js";
import Avatar from "vue-avatar";
export default {
  props: ["activityInfo", "participants"],
  components: {
    Avatar,
  },
  data() {
    return {
      chatPanelHeight: window.innerHeight + "px",
      searchPanelShow: false,
      message_panelObj: {
        // grid:1,
        right: "300px",
        borderRight: "0px",
        searchPanelShow: false,
      },
      message_notice: {
        color: "green",
        right: "0px",
        borderRight: "0px",
      },
      // message
      message: "",
      msglist: [],
      send_msg: [],
      query_date: "",
      // 下拉框的测试数据
      onlineParticipants: [],
      onlineUserIdList: [],
      offlineParticipants: [],
      windowStatus: "focus", //监听浏览器失焦事件
      // 聊天记录
      msgRecords: [],
      totalMsg: 0, //数据总条数
      currentPage: 1, //当前页数 ，默认为1
      pageSize: 10, // 每页显示数量
      currentPageData: [], //当前页显示内容
      maxPage: 1, //页码最大值
      //日期选择器选择的index
      msgR_datepicker: [], //日期选择器的第一条记录
      msgindex: 0,
      msgR_prev: [],
      msgR_next: [],
    };
  },
  beforeDestroy() {
    this.socketApi.close();
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
  updated: function () {
    this.$nextTick(function () {
      var div = document.getElementById("contentBody");
      var div2 = document.getElementById("searchmessageList");
      div.scrollTop = div.scrollHeight - 60;
      div2.scrollTop = div.scrollHeight;
    });
  },
  mounted() {
    $("#app").css("min-width", "0");
    window.addEventListener("blur", this.winBlur); //监听浏览器失焦事件
    window.addEventListener("focus", this.winFocus); //监听浏览器失焦事件

    this.init();
    this.supportNotify();

    //修改时间格式使其统一
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
    init() {
      this.startWebSocket(this.activityInfo.aid);
    },
    send(msg) {
      this.message = msg;
      let myDate = new Date().Format("yyyy-MM-dd HH:mm:ss");
      let current_time = myDate.toLocaleString(); //获取日期与时间

      // 消息不为空
      if (this.message !== "") {
        this.send_msg = {
          type: "message",
          from: this.$store.getters.userName,
          fromid: this.$store.getters.userId,
          content: this.message,
          time: current_time,
        };
        if (this.socketApi.getSocketInfo().linked) {
          this.msglist.push(this.send_msg);
          this.msgRecords.push(this.send_msg);
          this.socketApi.sendSock(this.send_msg, this.getSocketConnect); //连接后台onopen方法
        } else {
          let chatMsg = {
            type: "notice",
            content: "You are disconnecting with others.",
          };
          this.msglist.push(chatMsg);
        }
      }
      this.message = "";
    },
    startWebSocket(id) {
      this.socketApi.initWebSocket(
        "ChatServer/" + id,
        this.$store.state.IP_Port
      );
      this.send_msg = {
        type: "test",
        from: "Test",
        content: "TestChat",
      };
      this.socketApi.sendSock(this.send_msg, this.getSocketConnect);
    },
    getSocketConnect(data) {
      var chatMsg = data; //data传回onopen方法里的值
      if (data.type === "members") {
        let members = data.content
          .replace("[", "")
          .replace("]", "")
          .replace(/\s/g, "")
          .split(",");
        this.onlineUserIdList = members;
        this.judgeonlineParticipant();
      } else if (data.type === "message") {
        //判断消息的发出者
        if (chatMsg.content != "") {
          this.msglist.push(chatMsg);
          this.msgRecords.push(chatMsg);
          this.sendNotify(chatMsg);
        }
      } else if (data.type === "notice") {
        //上线下线提示
        if (chatMsg.behavior != "" && chatMsg.userId != "") {
          this.msglist.push(chatMsg);
        }
        // 在线成员变化
        this.judgeonlineParticipant(chatMsg);
      } else if (chatMsg.type == undefined && chatMsg.length > 0) {
        for (let i = 0; i < chatMsg.length; i++) {
          if (chatMsg[i].content != "") {
            this.msglist.push(chatMsg[i]);
            this.msgRecords.push(chatMsg[i]);
          }
        }
      }
    },
    //判断在线的用户列表
    judgeonlineParticipant(msg) {
      if (msg == undefined || msg == "") {
        // initial
        this.onlineParticipants = [];
        this.offlineParticipants = [];
        for (let i = 0; i < this.participants.length; i++) {
          if (this.onlineUserIdList.includes(this.participants[i].userId)) {
            this.onlineParticipants.push(this.participants[i]);
          } else {
            this.offlineParticipants.push(this.participants[i]);
          }
        }
      } else {
        if (msg.behavior == "off") {
          // offline
          for (let i = 0; i < this.onlineParticipants.length; i++) {
            if (msg.userId == this.onlineParticipants[i].userId) {
              let offperson = this.onlineParticipants.splice(i, 1);
              this.offlineParticipants.push(offperson[0]);
            }
          }
        } else if (msg.behavior == "on") {
          // online
          for (let i = 0; i < this.offlineParticipants.length; i++) {
            if (msg.userId == this.offlineParticipants[i].userId) {
              let onperson = this.offlineParticipants.splice(i, 1);
              this.onlineParticipants.push(onperson[0]);
            }
          }
        }
      }
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
          tag: msg.fromid + msg.time,
          icon: require("@/assets/images/OGMS2.png"), //通知的缩略图,
          body: "from   " + msg.from, //通知的具体内容
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
          console.log("error");
        };

        //通知关闭
        notify.onclose = function () {
          console.log("HTML5桌面消息关闭！！！");
        };
      }
    },
    //日期选择器
    find(seledate) {
      let msgtime = "";
      for (let i = 0; i < this.msgRecords.length; i++) {
        this.msgtime = this.msgRecords[i].time.trim().split(" ")[0];
        if (this.msgtime === seledate) {
          this.msgindex = i; //获得选择日期的第一条记录的index
          break;
        }
      }
      this.$set(this, "msgR_datepicker", this.msgRecords[this.msgindex]); //获得选择日期的第一条记录

      this.msgR_page = Math.ceil(this.msgindex / this.pageSize) + 1; //获得记录所在页码
      this.msgR_prev = [];
      this.msgR_next = [];
      for (let j = 0; j < this.msgRecords.length; j++) {
        if (j < this.msgindex) {
          this.msgR_prev.push(this.msgRecords[j]);
        } else if (j > this.msgindex) {
          this.msgR_next.push(this.msgRecords[j]);
        }
      }
      this.changeRecordPage(this.msgR_page);
      this.msgindex = "";
    },
    //查找所有聊天记录
    showRecords() {
      //聊天记录框
      this.searchPanelShow = !this.searchPanelShow;
      this.message_panelObj["right"] = 0;
      this.msgRecords = [];

      if (this.searchPanelShow === true) {
        this.axios
          .get(
            "/GeoProblemSolving/message/inquiry" +
              "?type=message" +
              "&key=roomId" +
              "&value=" +
              this.activityInfo.aid
          )
          .then((res) => {
            if (res.data != "None" && res.data != "Fail") {
              for (let i = 0; i < res.data.length; i++) {
                let message = JSON.parse(res.data[i].content);
                this.msgRecords.push(message);
              }
              this.totalMsg = res.data.length; //获取数据总条数
              this.$set(
                this,
                "maxPage",
                Math.ceil(this.totalMsg / this.pageSize)
              );
              this.currentPage = this.maxPage; //获取最后一页的聊天记录
              this.changeRecordPage(this.maxPage);
            }
          });
      }
    },
    //聊天记录分页,没有按日期查找，倒序排列
    changeRecordPage(index) {
      let beforePage = this.currentPage; //翻页之前的页码 判断前后
      let begin = this.totalMsg - (this.maxPage - index + 1) * this.pageSize;
      let end = this.totalMsg - (this.maxPage - index) * this.pageSize;
      //如果聊天记录为第一页，那么 显示最后不足pagesize的所有msg
      if (index === 1) {
        this.currentPageData = this.msgRecords.slice(0, end);
      } else {
        this.currentPageData = this.msgRecords.slice(begin, end);
      }
      this.currentPage = index;
      this.$refs.page.currentPage = index; //分页强制固定
    },
  },
};
</script>
<style scoped>
.chatPanel {
  display: flex;
  overflow-y: hidden;
}

.name {
  height: 28px;
  margin-top: 2px;
  line-height: 28px;
  padding-left: 5%;
  padding-right: 5%;
  margin-bottom: 2px;
  width: 100%;
  font-size: 18px;
  font-family: Helvetica;
}
.right-enter,
.right-leave-to {
  transform: translate3d(100%, 0, 0);
}
.right-leave,
.right-enter-to {
  transform: translate3d(0, 0, 0);
}
.right-enter-active,
.right-leave-active {
  transition: all 0.2s;
}

.date_pick {
  padding: 10px;
  width: 60%;
  margin-left: 2.5%;
  margin-right: 2.5%;
}

.date_pick_btn {
  width: 20%;
  margin-left: 5%;
  margin-right: 5%;
  height: 32px;
  margin-top: 10px;
}

.search_msg {
  padding: 10px;
  width: 90%;
  margin-left: 2.5%;
}

.searchHeader {
  height: 40px;
}

.searchHeader p {
  height: 20px;
  padding: 10px;
  line-height: 15px;
  font-size: 15px;
  text-align: center;
}

.searchPanel {
  background-color: linen;
}

.searchmessageList {
  overflow-y: auto;
}

.message_record_page {
  height: 20px;
  padding: 10px;
  line-height: 15px;
  text-align: center;
}

.message_record_board {
  margin-left: 5%;
  margin-right: 5%;
}

.single_record {
  padding: 5px;
}

/* 右侧布 */
.contentPanel {
  display: flex;
  flex-direction: column;
  flex: auto;
}

.contentHeader {
  border-bottom: 1px solid lightgray;
  display: flex;
}

/* 信息主题列表显示窗口 */
.contentBody {
  padding: 25px;
  overflow-y: auto;
  background-color: #e7e7e73a;
  height: calc(100vh - 285px);
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

.user_detail {
  height: 60px;
}

.u_name {
  height: 20px;
  margin-top: 5px;
  /* margin-left: 10px; */
  text-align: center;
  font-size: 10px;
  line-height: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 信息主体结束 */
/* 发送信息部分 */
.contentFooter {
  height: 124px;
  padding: 4px 5px;
  background-color: lightgray;
}

.send_panel {
  position: absolute;
  bottom: 10px;
  right: 10px;
  z-index: 10;
}

.u_avater {
  padding: 10px;
  width: 2.5%;
  margin-right: 5%;
}

.user_img {
  background-color: lightblue;
  margin-top: 2px;
  margin-left: 5px;
}

.send_message_btn {
  height: 32px;
  margin-top: 10px;
  /* margin-bottom: 10px; */
}
/* 发送信息部分结束 */

.chatObject {
  height: 40px;
  padding: 10px;
  display: flex;
  align-items: center;
}

.panelTitle {
  font-size: 1.17em;
  font-weight: bold;
}

.chatRecord {
  display: flex;
  align-items: center;
  margin-left: auto;
  margin-right: 10px;
}

.chat-notice {
  color: darkgreen;
  width: 90%;
  text-align: center;
}
</style>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
#logo {
  position: absolute;
  width: 129px;
  height: 40px;
  z-index: 1;
  margin-top: 5px;
  margin-left: 2.5%;
  cursor: pointer;
}

.header span {
  font-size: 15px;
  font-weight: bold;
}

.nav-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

header {
  height: 60px;
  top: 0;
  z-index: 99;
  position: absolute;
  width: 100%;
  flex: 0 0 auto;
  position: absolute;
  min-width: 1200px;
}

.content {
  /* flex: 1 0 auto; */
  margin-top: 60px;
}

footer {
  background-color: #515a6e;
  height: 60px;
  width: 100%;
  bottom: 0;
  flex: 0 0 auto;
}

.userState {
  position: absolute;
  width: 15%;
  top: 0px;
  z-index: 1;
  display: inline-block;
  min-width: fit-content;
}

.navPart {
  width: 85%;
  display: inline-block;
}

.menuItem a {
  font-size: 1.2em;
  font-weight: bold;
  color: rgba(255, 255, 255, 1);
}

.userImg {
  width: 40px;
  height: 40px;
  vertical-align: middle;
}

.footerTop {
  text-align: center;
  color: white;
  font-weight: bold;
  margin-top: 10px;
}

.footerBottom {
  text-align: center;
  color: white;
  font-size: 0.8em;
}

.menuItemCell {
  font-weight: 900;
}

.menuItemCell:hover {
  background-color: #808695;
  color: white;
}
</style>
<template>
  <div class="nav-container">
    <div style="position: fixed; z-index: 99">
      <header style="position: inherit; min-width: -webkit-fill-available">
        <img
          src="@/assets/images/OGMS.png"
          id="logo"
          class="pic"
          @click="goHome"
          style="cursor: pointer; margin-left: 5%"
        />
        <div v-if="useMenuCSS">
          <div class="navPart">
            <Menu
              class="header"
              mode="horizontal"
              theme="dark"
              :active-name="activeMenu"
              @on-select="turnContent"
              :style="`z-index:0;background:` + headerBgColor"
              width="auto"
            >
              <MenuItem name="home" class="menuItem" style="margin-left: 35%">
                <span>Home</span>
              </MenuItem>
              <MenuItem name="projects" class="menuItem">
                <span>Projects</span>
              </MenuItem>
              <MenuItem name="resources" class="menuItem">
                <span>Resources</span>
              </MenuItem>
              <!-- <MenuItem name="toolsCenter" class="menuItem">
                <span>Tools</span>
              </MenuItem> -->
              <!-- <MenuItem name="community" class="menuItem">
                <span>Community</span>
              </MenuItem>-->
              <MenuItem name="help" class="menuItem">
                <span>Help</span>
              </MenuItem>
            </Menu>
          </div>
          <div class="userState">
            <Menu
              mode="horizontal"
              theme="dark"
              :active-name="activeMenu"
              @on-select="unlogin"
              :style="`z-index:0;background:` + headerBgColor"
              v-show="!userState"
              class="menuItem"
            >
              <MenuItem name="login">
                <span>Login</span>
              </MenuItem>
              <MenuItem name="register">
                <span>Register</span>
              </MenuItem>
            </Menu>
            <Menu
              mode="horizontal"
              theme="dark"
              @on-select="logged"
              :style="`z-index:0;background:` + headerBgColor"
              v-show="userState"
              class="menuItem"
            >
              <MenuItem name="notification">
                <Badge :count="unreadNoticeCount" id="noticeBadge">
                  <Icon type="ios-notifications-outline" size="25"></Icon>
                </Badge>
              </MenuItem>
              <MenuItem name="personal" style="width: 100px">
                <Dropdown @on-click="changeSelect" placement="bottom-start">
                  <div @click="toPersonalPage">
                    <img
                      v-bind:src="avatar"
                      v-if="
                        avatar != '' && avatar != undefined && avatar != null
                      "
                      :title="userName"
                      style="width: 40px; height: 40px; vertical-align: middle"
                    />
                    <avatar
                      :username="userName"
                      :size="40"
                      style="margin-top: 10px"
                      :title="userName"
                      v-else
                    />
                  </div>
                  <DropdownMenu slot="list">
                    <DropdownItem name="logout">Logout</DropdownItem>
                  </DropdownMenu>
                </Dropdown>
              </MenuItem>
            </Menu>
          </div>
        </div>

        <div v-else>
          <Menu mode="horizontal" theme="dark" style="z-index: 0"></Menu>
          <Dropdown style="position: fixed; top: 10px; right: 20px">
            <Button
              icon="md-menu"
              style="color: white; background-color: #808695"
            ></Button>
            <DropdownMenu slot="list">
              <CellGroup style="width: 200px">
                <Cell
                  @click.native="turnToURL('/GeoProblemSolving/home')"
                  class="menuItemCell"
                  >Home
                </Cell>
                <Cell
                  @click.native="turnToURL('/GeoProblemSolving/projectList')"
                  class="menuItemCell"
                  >Projects
                </Cell>
                <Cell
                  @click.native="turnToURL('/GeoProblemSolving/publicResource')"
                  class="menuItemCell"
                  >Resources
                </Cell>
                <Cell
                  @click.native="turnToURL('/GeoProblemSolving/toolsCenter')"
                  class="menuItemCell"
                  >Tools
                </Cell>
                <Cell
                  @click.native="turnToURL('/GeoProblemSolving/help')"
                  class="menuItemCell"
                  >Help
                </Cell>
                <Divider style="margin: 5px 0" />
                <Cell
                  v-show="!userState"
                  @click.native="login"
                  class="menuItemCell"
                  >Login
                </Cell>
                <Cell
                  v-show="!userState"
                  @click.native="turnToURL('/GeoProblemSolving/register')"
                  class="menuItemCell"
                  >Register
                </Cell>
                <Cell
                  v-show="userState"
                  @click.native="turnToURL('/GeoProblemSolving/notification')"
                  class="menuItemCell"
                  >Notification
                </Cell>
                <Cell
                  v-show="userState"
                  @click.native="toPersonalPage"
                  class="menuItemCell"
                  >Personal Page
                </Cell>
                <Cell
                  v-show="userState"
                  @click.native="logout"
                  class="menuItemCell"
                  >Logout
                </Cell>
              </CellGroup>
            </DropdownMenu>
          </Dropdown>
        </div>
      </header>
    </div>
    <div class="content" :style="{ minHeight: contentHeight }">
      <router-view
        @sendNotice="sendMessage"
        @readNotification="readNotificationNum"
        @changeAvatar="changeAvatarUrl"
      ></router-view>
    </div>
    <footer>
      <h2 class="footerTop">
        <i>Open Geographic Modeling and Simulation</i>
      </h2>
      <p class="footerBottom">
        Copyright © 2011-2022 OpenGMS. All rights reserved.
      </p>
    </footer>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
import { get, del, post, put } from "../axios";
import * as socketApi from "../api/socket";

export default {
  name: "HelloWorld",
  data() {
    return {
      activeMenu: "",
      //消息机制
      noticeSocket: null,
      unreadNoticeCount: 0,
      timer: null,
      contentHeight: window.innerHeight - 120 + "px",
      useMenuCSS: false,
      avatarUrl: "",
    };
  },
  created() {
    this.setMenuTitle(this.$route.name);
    var that = this;
    var timer = window.setInterval(function () {
      if (that.$store.getters.userState) {
        that.linkSocket();
        window.clearInterval(timer);
      }
    }, 10);
  },
  mounted() {
    this.reSize();
    window.addEventListener("resize", this.reSize);
  },
  beforeDestroy: function () {
    window.removeEventListener("resize", this.reSize);
  },
  updated() {
    $(".userState sup").css("margin-top", "20px");
  },
  components: {
    Avatar,
  },
  computed: {
    userState() {
      return this.$store.getters.userState;
    },
    userName() {
      return this.$store.getters.userName;
    },
    avatar() {
      let avatarStr = this.$store.getters.avatar;
      if (this.avatarUrl.indexOf("/avatar/") != -1) {
        let imgId = this.avatarUrl.split("/avatar/");
        return this.$store.state.UserServer + "/avatar/" + imgId[1];
      }
      if (avatarStr != "" && avatarStr != undefined && avatarStr != null) {
        this.avatarUrl =
          this.$store.state.UserServer + this.$store.getters.avatar;
      }
      return this.avatarUrl;
    },
    headerBgColor() {
      return this.$route.name === "Home" ? "none" : "";
    },
    notifications() {
      return this.$store.getters.notifications;
    },
  },
  watch: {
    "$route.name": function (newVal, oldVal) {
      this.setMenuTitle(newVal);
    },
    notifications(newVal, oldVal) {
      if (newVal.length > 0) {
        for (let i = 0; i < newVal.length; i++) {
          this.sendMessage(newVal[i]);
        }
        this.$store.commit("cleanNotification");
      }
    },
  },
  methods: {
    turnToURL(url) {
      window.location.href = url;
    },
    linkSocket() {
      if (this.$store.getters.userState) {
        this.initWebSocket();
        this.getUnreadNoticeCount();
      }
    },
    setMenuTitle(newVal) {
      switch (newVal) {
        case "PublicResource": {
          this.activeMenu = "resources";
          break;
        }
        case "toolsCenter": {
          this.activeMenu = "toolsCenter";
          break;
        }
        case "Help": {
          this.activeMenu = "help";
          break;
        }
        case "Login": {
          this.activeMenu = "login";
          break;
        }
        case "Register": {
          this.activeMenu = "register";
          break;
        }
        default:
          this.activeMenu = "";
      }
    },
    reSize() {
      if (window.innerHeight > 675) {
        this.contentHeight = window.innerHeight - 120 + "px";
      } else {
        this.contentHeight = 675 - 120 + "px";
      }
      if (window.innerWidth < 1200) {
        this.useMenuCSS = false;
      } else {
        this.useMenuCSS = true;
      }
    },
    turnContent(name) {
      if (name === "home") {
        window.location.href = "/GeoProblemSolving/home";
      } else if (name == "projects") {
        window.location.href = "/GeoProblemSolving/projectList";
      } else if (name == "resources") {
        this.$router.replace({ name: "PublicResource" });
      } else if (name == "toolsCenter") {
        this.$router.replace({ name: "toolsCenter" });
      } else if (name == "community") {
        this.$router.replace({ name: "Community" });
      } else if (name == "help") {
        this.$router.replace({ name: "Help" });
      }
    },
    goHome() {
      window.location.href = "/GeoProblemSolving/home";
    },
    login() {
      var pageUrl = window.location.href;
      this.axios
        .get("/GeoProblemSolving/user/login?pageUrl=" + pageUrl)
        .then((res) => {
          window.location.href = res.data;
        });
    },
    unlogin(name) {
      if (name === "login") {
        this.$router.push({ name: "Login" });
        // var pageUrl = window.location.href;
        // this.axios
        //   .get("/GeoProblemSolving/user/login?pageUrl="+pageUrl)
        //   .then(res=>{
        //     window.location.href = res.data;
        //   })
      } else if (name == "register") {
        this.$router.push({ name: "Register" });
      }
    },
    logged(name) {
      if (name === "notification") {
        this.$router.push({ name: "notification" });
      } else if (name === "personal") {
      }
    },
    toPersonalPage() {
      this.$router.push({ name: "overView" });
      // this.$router.push({ name: "PersonalPage" });
    },
    // 获取到通知的数量
    getUnreadNoticeCount() {
      this.unreadNoticeCount = 0;
      //get请求发送的是用户id
      this.axios
        .get(
          "/GeoProblemSolving/notice/inquiry" +
            "?key=recipientId" +
            "&value=" +
            this.$store.getters.userId
        )
        .then((res) => {
          let noticeList = res.data;
          let unreadCount = 0;
          for (let i = 0; i < noticeList.length; i++) {
            if (noticeList[i].state === "unread") {
              unreadCount++;
              continue;
            }
          }
          this.$set(this, "unreadNoticeCount", unreadCount);
        })
        .catch((err) => {
          console.log("失败的原因是" + err.data);
        });
    },
    initWebSocket() {
      socketApi.initWebSocket("NoticeSocket");
      socketApi.sendSock(
        "NoticeSocket",
        { type: "test" },
        this.getSocketMessage
      );
    },
    onOpen() {
      // console.log("NoticeSocket连接成功！");
    },
    sendMessage: function (data) {
      socketApi.sendSock("NoticeSocket", data, this.getSocketMessage);
    },
    getSocketMessage(data) {
      if (data.type != "members"){
        this.unreadNoticeCount++;
        this.$Message.info("You have a new notice!");
      }

      // console.log(data);
    },
    readNotificationNum() {
      let newCount = this.unreadNoticeCount;
      if (newCount > 0) {
        this.unreadNoticeCount = newCount - 1;
      }
    },
    changeSelect(name) {
      if (name == "logout") {
        this.logout();
      }
    },
    changeAvatarUrl: function (url) {
      this.avatarUrl = url;
    },
    logout() {
      this.axios
        .get("/GeoProblemSolving/user/logout")
        .then((res) => {
          this.$store.commit("userLogout");
          sessionStorage.removeItem("userInfo");
          window.location.href = "/GeoProblemSolving/home";
          socketApi.close("NoticeSocket");
        })
        .catch((err) => {
          confirm("logout fail!");
        });
    },
  },
};
</script>

<style scoped>
  .personalPage{
    background-color:#fff;
  }

 .personalPageWithPic{
    background-image: url(../../../../static/Images/logogrey.png) ;
    width: 100%;
    height: 100%;
    background-position: center center;
    background-repeat: no-repeat;
    background-size: 60%;
  }

  .personalPageRight{
    background-color: lightgray;
    filter: alpha(opacity=40);
    opacity: 0.2;

  }

  .sideItem {
    height: 58px;
    border: 2px solid transparent;
    box-sizing: border-box;
    transition: color .3s;
    /*padding-left: 40px;*/
    /*padding-right: 65px;*/
  }

  .sideItem:hover {
    backdrop-filter: blur(5px);
    background-color: rgba(255, 255, 255, 0.4);
    box-shadow: 0px 0px 5px 0px rgba(0, 0, 0, 0.23);
    border-width: 1px 0 1px 1px;
    border-style: solid;
    border-color: #e0e0e0;
    padding-left: 30px;
  }

  label {
    font-family: Lato,sans-serif;
    font-size: 16px
  }

  .sideContainer {
    height: 100%;
    padding-bottom: 150px;
  }

  a {
    text-decoration: none;
    color: black;
  }

  .router-link-active {
    text-decoration: none;
  }

  .customFont {
    font-size: 16px;
    margin: 0;
    font-family: 'Open Sans', sans-serif;
  }
</style>
<template>
  <div style="display: flex" class="personalPage">
     <div v-if="useNavCSS" style="display: flex"  class ="personalPageWithPic">
       <div style="width:200px;">
         <Menu style="height: calc(100vh - 120px);z-index: 2;" width="auto" :active-name="$route.name">
           <MenuGroup>
             <MenuItem name="overView" to="overView" class="sideItem">
               <Icon type="ios-book" size="25"/>
               <label>Overview</label>
             </MenuItem>

             <MenuItem name="project" to="project" class="sideItem">
               <Icon type="ios-cube" size="25"/>
               <label>Project</label>
             </MenuItem>

             <MenuItem name="resourceList" to="resourceList" class="sideItem">
               <Icon type="ios-cloud" size="25"/>
               <label>Resource</label>
             </MenuItem>

             <MenuItem name="tool" to="tool" class="sideItem">
               <Icon type="ios-cog" size="25"/>
               <label>Tool</label>

             </MenuItem>

             <MenuItem name="todoList" to="todoList" class="sideItem">
               <Icon type="ios-clipboard" size="25"/>
               <label>Todo List</label>
             </MenuItem>
           </MenuGroup>

           <MenuGroup >
             <MenuItem name="userInfo" to="userInfo" class="sideItem">
               <Icon type="ios-contact-outline" size="25"/>
               <label>My Account</label>
             </MenuItem>

             <MenuItem name="notification" to="notification" class="sideItem">
               <Icon type="ios-mail-open-outline" size="25"/>
               <label>Message</label>
             </MenuItem>
           </MenuGroup>
         </Menu>
       </div>
       <div style="width: calc(100vw - 200px);position: relative;" >
         <div
           class="personalPageRight"
           style="width: calc(100vw - 200px);position: absolute;height: calc(100vh - 120px);"
         >
         </div>
         <router-view
           style="margin-top: 10px"
           v-on="$listeners"
           v-bind="$attrs"
         ></router-view>
       </div>
     </div>

     <!-- v-else -->
     <div v-else style="display: flex" class ="personalPageWithPic">
       <div style="width:80px;">
         <Menu style="height: calc(100vh - 120px);z-index: 2;" width="auto" :active-name="$route.name">
           <MenuGroup>
             <MenuItem name="overView" to="overView" class="sideItem">
               <Icon type="ios-book" title="Overview" size="25" />

             </MenuItem>

             <MenuItem name="project" to="project" class="sideItem">
               <Icon type="ios-cube" title="Project" size="25"/>

             </MenuItem>

             <MenuItem name="resourceList" to="resourceList" class="sideItem">
               <Icon type="ios-cloud" title="Resource" size="25"/>

             </MenuItem>

             <MenuItem name="tool" to="tool" class="sideItem">
               <Icon type="ios-cog" title="Tool" size="25"/>


             </MenuItem>

             <MenuItem name="todoList" to="todoList" class="sideItem">
               <Icon type="ios-clipboard" title="Todo List" size="25"/>

             </MenuItem>
           </MenuGroup>

           <MenuGroup >
             <MenuItem name="userInfo" to="userInfo" class="sideItem">
               <Icon type="ios-contact-outline" title="User Information" size="25"/>

             </MenuItem>

             <MenuItem name="notification" to="notification" class="sideItem">
               <Icon type="ios-mail-open-outline" title="Notification" size="25"/>

             </MenuItem>
           </MenuGroup>
         </Menu>
       </div>

       <div style="width: calc(100vw - 70px);">
         <div
           class="personalPageRight"
           style="width: calc(100vw - 70px);position: absolute;height: calc(100vh - 120px);"
         >
         </div>
         <router-view
           style="margin-top: 10px"
           v-on="$listeners"
           v-bind="$attrs"
         ></router-view>
       </div>
     </div>

  </div>

</template>

<script>
  import projectVue from "../subPage/project.vue";
  // import ToWork from "../../projects/toWork";
  import notification from "../subPage/notification";
  import todoList from "../subPage/todoList";
  import Project from "../subPage/project";
  import tool from "../subPage/tool";
  import resourceList from "../subPage/resourceList";

  export default {
    beforeRouteEnter: (to, from, next) => {
      next(vm => {
        if (!vm.$store.getters.userState || vm.$store.getters.userId == "") {
          vm.$router.push({ name: "Login" });
        }
      });
    },
    name: "newPersonalPage",
    components: {
      Project,
      // ToWork,
      projectVue,
      notification,
      "todo-list": todoList,
      tool,
      resourceList
    },
    data() {
      return {
        menuWidth: "",
        contentHeight: "",
        useNavCSS: true,
        // personalPageWithPic: {
        //   backgroundImage: "url(" + require("../../../assets/images/OGMS1.png") + ") ",
        //   backgroundPosition: "center center",
        //   backgroundRepeat: "no-repeat",
        //   backgroundSize: "cover",
        // },
      }
    },
    created() {
    },
    mounted() {
      this.reSize();
      window.addEventListener("resize", this.reSize);

      //判断是否是临时用户
      let userId = JSON.parse(sessionStorage.getItem("userInfo")).userId;
      if(userId.length >= 40){
        this.$Message.warning({
          content: 'You are using a temporary account, please use your personal space after logging in.',
          duration: 15,
          closable: true
        });
        this.$store.commit("userLogout");
        this.$router.push({ name: "Login" });
      }
    },
    beforeDestroy: function () {
      window.removeEventListener("resize", this.reSize);
    },

    methods: {
      resizeContent() {
        if (window.innerHeight > 1440) {
          this.menuWidth = 240;
        } else {
          this.menuWidth = 170;
        }
        if (window.innerHeight > 675) {
          this.contentHeight = window.innerHeight - 120 + "px";
        } else {
          this.contentHeight = 555 + "px";
        }
        window.onresize = () => {
          return (() => {
            this.resizeContent();
          })();
        };
      },
      reSize() {
        // if (window.innerHeight > 675) {
        //   this.contentHeight = window.innerHeight - 120 + "px";
        // } else {
        //   this.contentHeight = 675 - 120 + "px";
        // }
        if (window.innerWidth < 1200) {
          this.useNavCSS = false;
        } else {
          this.useNavCSS = true;
        }
      },
    }
  }
</script>

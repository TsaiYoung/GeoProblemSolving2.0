<style scoped>
.titleJoin{
  text-align: center;
  margin-top: 70px;
}
</style>

<template>
  <div>
    <Row>
      <Col span="18" offset="3" :style="{ height: contentHeight }">
        <div class="titleJoin">
          <h1 >Join the project</h1>
        </div>
        <div
          style="
            display: flex;
            justify-content: center;
            height: 100%;
            padding: 80px;
          "
        >
          <Form :label-width="120">
            <FormItem label="Project name">
              <Input
                v-model="projectInfo.name"
                disabled
                style="width: 600px"
              ></Input>
            </FormItem>
            <br/>
            <FormItem label="ProjectId" style="display: none">
              <Input v-model="projectId" disabled style="width: 600px"></Input>
            </FormItem>
            <FormItem label="Email">
              <Input v-model="email" disabled style="width: 600px"></Input>
            </FormItem>

            <div
              v-if="registeredHintShow"
              style="
                margin-left: 120px;
                display: flex;
                align-items: center;
                width: 600px;
              "
            >
              <Icon
                type="ios-information-circle-outline"
                :size="30"
                color="yellowGreen"
              />
              <span style="font-size: 10px;margin-left:10px">
                {{ this.registeredHint }}
                <br/>Now you need to enter your password and click the
                <strong style="font-weight: bold; color: blue">Join</strong>
                button for becoming a member of the project.
              </span>
            </div>
            <div
              v-if="passwordInputShow"
              style="
                margin-left: 120px;
                display: flex;
                align-items: center;
                width: 600px;
              "
            >
              <Icon
                type="ios-information-circle-outline"
                :size="30"
                color="red"
              />
              <span style="font-size: 10px;margin-left:10px">
                {{ this.unregisteredHint }}
                <br/>If you input your password here and click the
                <strong style="font-weight: bold; color: blue"
                >Register and join</strong
                >
                button, your account would be created and you will join this
                project.
              </span>
            </div>

            <FormItem label="Password" style="margin-top: 20px">
              <Input
                v-model="password"
                type="password"
                style="width: 600px"
              ></Input>
            </FormItem>
            <br/>
            <div style="display: flex; justify-content: center">
              <Button
                type="default"
                @click="joinProject()"
                v-if="registeredHintShow"
              >
                <span style="font-weight: bold; font-size: 1.2em">Join</span>
              </Button>
              <Button type="default" @click="registerAndJoin()" v-if="passwordInputShow">
                <span style="font-weight: bold; font-size: 1.2em"
                >Register and join</span
                >
              </Button>
            </div>
          </Form>
        </div>
      </Col>
    </Row>
  </div>
</template>
<script>
  import md5 from "js-md5";
  import {get, del, post, put} from "../../axios";

  export default {
    components: {md5},
    data() {
      return {
        headerWidth: "",
        contentHeight: "",
        userInfo: {},
        email: "",
        password: "",
        projectId: "",

        passwordInputShow: false,
        //true表示已经注册，只需要join
        registeredHintShow: false,
        registeredHint:
          "This email has been registered, you have been a user in our platform.",
        unregisteredHint: "This email has not been registered in our platform.",
        unregisteredOperate:
          "If you fill in your password and Submit it, your account would be created and you will join this project.",
        // navaigation页面的变量
        //消息机制
        noticeSocket: null,
        unreadNoticeCount: 0,
        timer: null,
        // 项目信息
        projectInfo: [],
      };
    },
    mounted() {
      // navigation页面的
      this.getProjectInfo();
      this.headerWidth = window.innerWidth + "px";
      this.contentHeight = window.innerHeight - 180 + "px";
      this.projectId = this.$route.params.id;
      this.email = this.$route.params.email;
      this.judgeMailRegister();
    },
    computed: {
      userState() {
        return this.$store.getters.userState;
      },
    },
    methods: {
      getProjectInfo() {
        this.axios
          .get("/GeoProblemSolving/project/" + this.$route.params.id)
          .then((res) => {
            if (res.data.code == 0) {
              this.projectInfo = res.data.data;
            }
          })
          .catch((err) => {
            throw err;
          });
      },
      judgeMailRegister() {
        //先判断是否有登录状态
        // if (this.$store.getters.userState && this.$store.getters.userEmail == this.email) {
        //   //当前就是被邀请者在线，直接join跳转
        //   this.registeredHintShow = true;
        //   this.passwordInputShow = false;
        // } else {

        //有登录状态,直接 T 就行，反正不管是不是被邀请这都是要输入密码的
        //判断此账户是否注册，若未注册则快速注册

        if (this.$store.getters.userState) {
          this.axios
            .get("/GeoProblemSolving/user/logout")
            .then((res) => {
              this.$store.commit("userLogout");
              sessionStorage.removeItem("userInfo");
            })
            .catch((err) => {
              // confirm("logout fail!");
            });
        }

        this.axios
          .get("/GeoProblemSolving/user/registered/" + this.email)
          .then((res) => {
            console.log(res);
            //已被注册
            if (res.data.code == 0) {
              this.registeredHintShow = true;
              this.passwordInputShow = false;
              this.userInfo = res.data.data;
              this.$Message.info(
                "You have used this email to register an account."
              );
            } else if (res.data.code == -1) {
              //用户还未注册
              this.registeredHintShow = false;
              this.passwordInputShow = true;
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });

        // }

      },
      joinProject() {
        if (this.password == "") {
          this.$Message.error("Please fill in your password.");
          return;
        }
        //登录
        this.axios
          .get(
            "/GeoProblemSolving/user/login/" +
            this.email + "/" +
            md5(this.password)
          )
          .then((res) => {
            if (res.data.code === -2) {
              this.$Message.error("Invalid account or password.");
            } else {
              this.$store.commit("userLogin", res.data.data);
              let userId = res.data.data.userId;
              this.axios.post("/GeoProblemSolving/project/" + this.projectId + "/user?userId=" + userId)
                .then(res => {
                  if (res.data.code == 0) {
                    window.location.href = "/GeoProblemSolving/projectInfo/" + this.$route.params.id;
                  } else if (res.data.code == -3) {
                    this.$Notice.info({
                      title: "Warning",
                      desc: 'Member already exists in the project.'
                    })
                    window.location.href =
                      "/GeoProblemSolving/projectInfo/" + this.$route.params.id
                  }
                })
                .catch(err=>{
                  this.$Message.error(err);
                })
            }
          }).catch(err=>{
            this.$Notice.error({title: "error", desc: "Internet disconnected."});
        });


      },
      async registerAndJoin() {
        if (this.password == "") {
          this.$Message.error("Please fill in your password.");
          return;
        }
        // register in UserServer
        let userInfo = {
          email: this.email,
          name: this.email,
          password: md5(this.password),
        };

        // add userInfo in database
        this.axios
          .post("/GeoProblemSolving/user", userInfo)
          .then((res) => {
            if (res.data.code == 0 || res.data.code == -3) {
              // join project
              this.joinProject(res.data.data.userId);
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            throw err;
          });
      },
      autoLogin() {
        this.axios
          .get(
            "/GeoProblemSolving/user/login/" +
            this.email + "/" +
            md5(this.password)
          )
          .then((res) => {
            if (res.data.code === -2) {
              this.$Message.error("Invalid account or password.");
            } else {
              this.$store.commit("userLogin", res.data.data);
              window.location.href =
                "/GeoProblemSolving/projectInfo/" + this.$route.params.id;
            }
          });
      },
    },
  };
</script>

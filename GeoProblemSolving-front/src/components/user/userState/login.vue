<style scoped>
  .layout {
    background-color: #ffffff;
    --headerHeight: 60px;
    /*--footerHeight: 60px;*/
    overflow: hidden;
  }

  .content {
    background-image: url("../../../assets/images/c5.jpg");
    background-size: cover;
    display: flex;
    justify-content: center;
    /* background-attachment:fixed; */
    margin: 0 auto;
  }

  .loginDiv {
    width: 450px;
    border: 1px solid rgba(133, 115, 92, 0.4);
    position: absolute;
    left: 60%;
    top: 5%;
    background-color: rgba(255, 255, 255, 1);
  }

  .loginTitle {
    text-align: center;
    vertical-align: middle;
    line-height: 50px;
    color: #332d23;
    font-size: 25px;
    font-weight: bold;
    padding-top: 40px;
    margin-left: 20%;
    margin-right: 20%;
    border-bottom: 1px solid rgba(133, 115, 92, 0.4);
  }

  .login-content {
    padding: 50px 10px 20px 20px;
    height: 350px;
    margin-left: 10%;
    margin-right: 10%;
  }

  .ivu-form .ivu-form-item-label {
    text-align: right;
    vertical-align: middle;
    float: left;
    font-size: 14px;
    color: #495060;
    line-height: 1.5;
    padding: 5px 12px 10px 0;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
  }

  .ivu-input {
    width: 90%;
    font-size: 13px;
  }

  #logo {
    position: absolute;
    width: 129px;
    height: 40px;
    z-index: 1;
    margin-top: 5px;
    margin-left: 2.5%;
  }

  .loginBtn {
    font-size: 15px;
  }

  .loginBtn:hover {
    color: rgb(54, 72, 109);
    background-color: white;
    font-size: 15px;
  }

  img {
    cursor: pointer;
  }

  .reUseDiv {
    display: flex;
    text-align: center;
    justify-content: center;
    margin-top: 10px;
  }

  .reciverSpan {
    width: 100px;
    text-align: center;
  }

  .resetReuseDiv {
    display: flex;
    align-items: center;
    padding: 10px;
  }

  .resetReuseDiv p,
  .resetReuseDiv input {
    width: 100%;
  }
</style>
<template>
  <div class="layout">
    <div
      class="content"
      ref="homePage"
      v-if="UserState == false"
      v-bind:style="contentStyle"
    >
      <div class="loginDiv" v-bind:style="loginStyle">
        <div class="loginTitle">Log in</div>
        <div class="login-content">
          <Form ref="loginForm" :model="loginForm" :rules="loginFormRule">
            <FormItem prop="user" label="Email" :label-width="100">
              <Input
                placeholder="Please input your email"
                style="width: 90%"
                v-model="loginForm.user"
                type="text"
              />
            </FormItem>
            <FormItem prop="password" label="Password" :label-width="100">
              <Input
                placeholder="Please input your password"
                style="width: 90%"
                v-model="loginForm.password"
                @keyup.enter.native="login('loginForm')"
                type="password"
              />
            </FormItem>
            <div class="reUseDiv">
              <Checkbox v-model="checked">Keep me logged in</Checkbox>
            </div>
            <div class="reUseDiv">
              <p>
                Forgot password?
                <a @click="resetModalSHow = true">Reset</a>
              </p>
            </div>
            <br/>
            <FormItem>
              <div class="reUseDiv">
                <Button
                  type="default"
                  @click="login('loginForm')"
                  class="loginBtn"
                >Log in
                </Button
                >
                <Button
                  type="default"
                  @click="register"
                  class="loginBtn"
                  style="margin-left: 10px"
                >Register
                </Button
                >
              </div>
            </FormItem>
          </Form>
        </div>
      </div>
    </div>
    <Modal
      v-model="resetModalSHow"
      @on-ok="sendResetEmail"
      ok-text="Confirm"
      cancel-text="Cancel"
      title="Reset password board"
    >
      <div class="resetReuseDiv">
        <span class="reciverSpan">Reciever:</span>
        <Input v-model="loginForm.user"/>
      </div>
      <div class="resetReuseDiv">
        <Icon
          type="ios-information-circle-outline"
          :size="20"
          color="lightblue"
        />
        <p>
          We will send you an email with a url you can visit it and reset your
          password, if you agree, you can click the Confirm button and you will
          get an email soon.
        </p>
      </div>
    </Modal>
  </div>
</template>

<script>
  import md5 from "js-md5";

  export default {
    components: {md5},
    computed: {
      UserState() {
        return this.$store.getters.userState;
      },
    },
    created() {
      if (this.$store.getters.userState) {
        this.$router.replace({name: "overView"});
      }
    },
    data() {
      return {
        loginForm: {
          user: "",
          password: "",
        },
        loginFormRule: {
          user: [
            {
              required: true,
              message: "User name can not be empty",
              trigger: "blur",
            },
          ],
          password: [
            {
              required: true,
              message: "Password can not be empty",
              trigger: "blur",
            },
            {
              type: "string",
              min: 3,
              message: "The length of password can not be less than 6 characters",
              trigger: "blur",
            },
          ],
        },
        loginStyle: {
          marginTop: "",
        },
        contentStyle: {
          height: "",
        },
        checked: false,
        changePwdEmailStyle:
          "This email is used for help you reset your password, you can click this URL ",
        urlAddress:
          "http://" +
          this.$store.state.IP_Port +
          "/GeoProblemSolving/resetPassword/",
        resetModalSHow: false,
      };
    },
    mounted() {
      window.addEventListener("resize", this.initSize);
      this.getlocalStorage();
      this.initSize();
    },
    methods: {
      initSize() {
        this.contentStyle.height = window.innerHeight - 120 + "px";
        this.loginStyle.marginTop = window.innerHeight / 5 + "px";
      },
      login(form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            if (this.checked == true) {
              localStorage.setItem("user", this.loginForm.user);
              var password = this.loginForm.password;
              // password = this.encrypto(password);
              localStorage.setItem("password", password);
              localStorage.setItem("statusRecord", this.checked);
            } else if (this.checked == false) {
              localStorage.setItem("user", "");
              localStorage.setItem("password", "");
              localStorage.setItem("statusRecord", false);
            }
            let email = this.loginForm.user;
            //前段md5 加密，后端再进行 shaHex256加密
            let passwordAESURI = md5(this.loginForm.password);
            this.axios
              .get(
                "/GeoProblemSolving/user/login/" + email + "/" + passwordAESURI
              )
              .then((res) => {
                if (res.data.code === 0){
                  this.$store.commit("userLogin", res.data.data);
                  //跳转到 -1 页面
                  this.$router.go(-1);
                }else {
                  this.$Message.error("Invalid account or password.");
                  this.loginForm.password = "";
                }
              });
          } else {
            this.$Message.error(
              "Invalid account or password."
            );
          }
        });
      },
      goBack() {
        window.location.href = "/GeoProblemSolving/home";
      },
      register() {
        this.$router.push({name: "Register"});
      },
      goHome() {
        window.location.href = "/GeoProblemSolving/home";
      },
      getlocalStorage() {
        this.loginForm.user = localStorage.getItem("user");
        this.loginForm.password = localStorage.getItem("password");
        // 将字符串格式的true转换为boolean模式的true
        if (localStorage.getItem("statusRecord")) {
          this.checked = eval(localStorage.getItem("statusRecord"));
        }
      },
      encrypto(context) {
        // var CryptoJS = require("crypto-js");
        // var key = CryptoJS.enc.Utf8.parse("NjnuOgmsNjnuOgms");
        // var iv = CryptoJS.enc.Utf8.parse("NjnuOgmsNjnuOgms");
        // var encrypted = "";
        // if (typeof context == "string") {
        // } else if (typeof context == "object") {
        //   context = JSON.stringify(context);
        // }
        // var srcs = CryptoJS.enc.Utf8.parse(context);
        // encrypted = CryptoJS.AES.encrypt(srcs, key, {
        //   iv: iv,
        //   mode: CryptoJS.mode.CBC,
        //   padding: CryptoJS.pad.Pkcs7
        // });
        // return encrypted.toString();
      },
      decrypto(context) {
        // var CryptoJS = require("crypto-js");
        // var key = CryptoJS.enc.Utf8.parse("NjnuOgmsNjnuOgms");
        // var iv = CryptoJS.enc.Utf8.parse("NjnuOgmsNjnuOgms");
        // var decrypt = CryptoJS.AES.decrypt(context, key, {
        //   iv: iv,
        //   mode: CryptoJS.mode.CBC,
        //   padding: CryptoJS.pad.Pkcs7,
        // });
        // var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
        // return decryptedStr.toString();
      },
      goRegister() {
        this.$router.push({name: "Register"});
      },
      sendResetEmail() {
        this.axios
          .get(
            "/GeoProblemSolving/user/sendEmail/" +
            this.loginForm.user
          )
          .then((res) => {
            if (res.data.code == 0) {
              this.$Notice.success({title: "Waiting", desc: "The email is sending."});
              this.$router.push({
                name: "resetPassword",
                params: {email: this.loginForm.user},
              });
            } else if (res.data.code == -3) {
              this.$Notice.error({
                title: "Fail",
                desc: "The email has not been registered.",
              });
            }else {
              this.$Notice.error({
                title: "Error",
                desc: "Please contact with manager."
              })
            }
          })
          .catch((err) => {
            throw err;
          });
      },
      // register() {
      //   this.$router.push({name: "Register"});
      // },
    },
  };
</script>

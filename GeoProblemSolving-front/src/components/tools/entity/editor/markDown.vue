<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <!-- coding for your tools // begin-->      
      <vue-scroll :ops="ops" style="height: calc(100vh - 40px)">
      <div style="padding: 10px">
        <div>
          <Button 
            @click="LoadMarkDownFile()"
            :loading="boolMarkDownFile"
            type="info"
          >
            <span v-if="!boolMarkDownFile"
              >Load markdown file</span
            >
            <span v-else>Importing file...</span>
          </Button>
          <Button @click="beforeSave()" style="float: right; margin-right:10px;" type="warning">Save</Button>
        </div>
        <mavon-editor 
          :toolbars="toolbars"
          :toolbarsBackground="'#f9f9f9'"
          :subfield="false"
          defaultOpen="preview"
          @save="tempSave"
          @change="change"
          @imgAdd="imgAdd"
          @imgDel="imgDel"
          ref="md"
          style="height: 82vh; margin-top: 10px; max-width: 90vw; min-width:100px; z-index: 10;"
          v-model="content"
        />
      </div>
      </vue-scroll>
      
      <!-- // end -->
    </div>
    <Modal
      v-model="newAddMdModal"
      width="600"
      title="Save file"
    >

      <Form
        ref="uploadDataInfo"
        :model="uploadDataInfo"
        :rules="uploadDataRule"
        :label-width="100"
        label-position="left"
        inline
      >
        <FormItem label="Type" prop="suffix">
          <RadioGroup v-model="uploadDataInfo.suffix" style="width: 100%" @on-change="sendInputParams(uploadDataInfo.suffix, 'suffix') ">
            <Radio label=".md">MarkDown</Radio>
            <Radio label=".txt">TXT</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Privacy" prop="privacy" style="width: 100%">
          <RadioGroup v-model="uploadDataInfo.privacy" style="width: 100%" @on-change="sendInputParams(uploadDataInfo.privacy, 'privacy') ">
            <Radio label="private">Private</Radio>
            <Radio label="public">Public</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Name" prop="name" style="width: 100%">
          <Input
            type="text"
            v-model="uploadDataInfo.name"
            @on-change="
              sendInputParams(
                uploadDataInfo.name,
                'name'
              )
            "
            id="input_name"
            class="addOrEditInputs"
          />
        </FormItem>
        <FormItem label="Description" prop="description" style="width: 100%">
          <Input
            type="textarea"
            :rows="3"
            v-model="uploadDataInfo.description"
            @on-change="
              sendInputParams(
                uploadDataInfo.description,
                'description'
              )
            "
            id="input_description"
            class="addOrEditInputs"
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="newAddMdModal = false">Cancel</Button>
        <Button type="success" @click="save('uploadDataInfo')"
          >Save</Button
        >
      </div>
    </Modal>
  </div>
</template>
<script>
import {mavonEditor} from 'mavon-editor';
import "mavon-editor/dist/css/index.css";

export default {
  components: {
    mavonEditor,
  },
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      // basic info
      activityInfo: {},
      toolId: "",
      participants: [],
      userInfo: {},
      resources: [],
      selectFile: {},
      //
      boolMarkDownFile: false,
      loadMD: false,
      newAddMdModal: false,
      uploadDataInfo: {
        suffix: ".md",
        privacy: "private",
        type: "others",
        description: "",
        name: "",
      },
      uploadDataRule: {
        suffix: [
          {
            required: true,
            message: "file type cannot be empty",
            trigger: "blur",
          },
        ],
        privacy: [
          {
            required: true,
            message: "file privacy cannot be empty",
            trigger: "blur",
          },
        ],
        name: [
          {
            required: true,
            message: "file name cannot be empty",
            trigger: "blur",
          },
        ],
        description: [
          {
            message: "file description cannot be empty",
            trigger: "blur",
          },
        ],
      },

      //参数
      content: `# 开始创作...`,
      htmlRes: ``,
      html: "",
      toolbars: {
          bold: true, // 粗体
          italic: true, // 斜体
          header: true, // 标题
          underline: true, // 下划线
          strikethrough: true, // 中划线
          mark: true, // 标记
          superscript: true, // 上角标
          subscript: true, // 下角标
          quote: true, // 引用
          ol: true, // 有序列表
          ul: true, // 无序列表
          link: true, // 链接
          imagelink: true, // 图片链接
          code: true, // code
          table: true, // 表格
          fullscreen: false, // 全屏编辑
          readmodel: false, // 沉浸式阅读
          htmlcode: true, // 展示html源码
          help: true, // 帮助
          /* 1.3.5 */
          undo: true, // 上一步
          redo: true, // 下一步
          trash: true, // 清空
          save: true, // 保存（触发events中的save事件）
          navigation: true, // 导航目录
          alignleft: true, // 左对齐
          aligncenter: true, // 居中
          alignright: true, // 右对齐
          subfield: true, // 单双栏模式
          preview: true // 预览
      },
    };
  },
  created() {},
  mounted() {
    // 加载协同组件
    loadCollabComponent();
    this.getStepInfo();
  },
  methods: {
    getStepInfo() {
      if (componentStatus) {
        // 获取数据
        this.activityInfo = activityInfo;
        this.toolId = toolId;
        this.participants = onlineMembers;
        this.userInfo = userInfo;
        this.resources = resources;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
        loadResChannel = this.loadResources;
      } else {
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },
    getSocketComputation(data) {
      
    },
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "open"){
        this.socketLoadMarkDown(content.data);
      } else if (behavior == "save-open"){
        if (content.type == "edit") {
          this.uploadDataInfo = {
            suffix: this.selectFile.suffix,
            privacy: this.selectFile.privacy,
            type: "others",
            description: this.selectFile.description,
            name: this.selectFile.name,
          };
          this.newAddMdModal = true;
        } else {
          this.uploadDataInfo = {
            suffix: ".md",
            privacy: "private",
            type: "others",
            description: "",
            name: "",
          };
          this.newAddMdModal = true;
        }

      } else if (behavior == "params"){
        //编辑信息协同2 add-input  输入参数
        let index = content.stateIndex;
        // document.getElementById('input_' + index).children[0].children[1].value = content.inputs;
        this.uploadDataInfo[index] = content.inputs;
      } else if (behavior == "temp-save"){
        //md内容协同显示
        this.content = content.data;
      } else if (behavior == "submit") {
        // add\edit表单提交协同
        // this.submit(content.type);
        this.newAddMdModal = false;
        this.$Notice.success({
          title: 'Operation notice',
          duration: 10,
          render: h => {
            return h('span', [
              'Saved successfully '
            ])
          }
        });
      }
    },
    getSocketData(data) {},
    loadResources(resList) {
      let fileInfo = {};
      for (let i = 0; i < resList.length; i++) {
        // your function
        if (this.boolMarkDownFile) {
          if(resList[0].suffix == ".md" || resList[0].suffix == ".txt"){
            fileInfo.name = resList[0].name;
            fileInfo.address = resList[0].address;
            fileInfo.suffix = resList[0].suffix;
            fileInfo.fileSize = resList[0].fileSize;
            fileInfo.description = resList[0].description;
            fileInfo.privacy = resList[0].privacy;
            fileInfo.uid = resList[0].uid;
            this.axios
              .post("/GeoProblemSolving/md/loadMarkDown/", fileInfo)
              .then((res) => {
                console.log(res);
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  // this.$router.push({ name: "Login" });
                  this.tempLoginModal = true;
                } else if (res.data.code == 0) {
                  this.content = res.data.data.txt;
                  // this.$refs.md.d_render = res.data.data.html;
                  // this.$refs.md.d_value = res.data.data.txt;
                  this.loadMD = true;
                  this.selectFile = fileInfo;
                } else {
                  console.log(res.data.msg);
                }
              })
              .catch((err) => {
                console.log(err.data);
              });
          } else {
            alert("This file format is not supported!");
          }
          this.boolMarkDownFile = false;
        } 
      }
      let send_content = {
        type: "operation",
        sender: this.userInfo.userId,
        behavior: "open",
        content: {
          data: fileInfo,
        }
      };
      sendCustomOperation(send_content);
    },
    socketLoadMarkDown(fileInfo){
      this.axios
        .post("/GeoProblemSolving/md/loadMarkDown/", fileInfo)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.content = res.data.data.txt;
            // this.$refs.md.d_render = res.data.data.html;
            // this.$refs.md.d_value = res.data.data.txt;
            this.loadMD = true;
            this.selectFile = fileInfo;
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    LoadMarkDownFile(){
      this.boolMarkDownFile = !this.boolMarkDownFile;
    },
    beforeSave(){
      this.tempSave();
      if (this.loadMD){
        // 保存文件到原路径
        this.uploadDataInfo = {
          suffix: this.selectFile.suffix,
          privacy: this.selectFile.privacy,
          type: "others",
          description: this.selectFile.description,
          name: this.selectFile.name,
        };
        this.newAddMdModal = true;
      } else {
        // 新建文件保存
        this.uploadDataInfo = {
          suffix: ".md",
          privacy: "private",
          type: "others",
          description: "",
          name: "",
        };
        this.newAddMdModal = true;
      }
      // websocket
      let send_content = {
        type: "operation",
        sender: this.userInfo.userId,
        behavior: "save-open",
        content: {
          type: this.loadMD ? "edit" : "new",
        }
      };
      sendCustomOperation(send_content);
    },
    save(){
      this.submit(this.loadMD ? "edit" : "new");
      
    },
    submit(type){
      // uploadDataInfo、content后台把content转换成相应的md文件
      let fileInfo = {};
      fileInfo.name = this.uploadDataInfo.name;
      fileInfo.suffix = this.uploadDataInfo.suffix;
      fileInfo.privacy = this.uploadDataInfo.privacy;
      fileInfo.type = this.uploadDataInfo.type;
      fileInfo.description = this.uploadDataInfo.description;
      fileInfo.value = this.content;
      fileInfo.aid = this.activityInfo.aid;
      fileInfo.userId = this.userInfo.userId;
      fileInfo.paths = "0";
      fileInfo.graphId = this.activityInfo.parent;
      fileInfo.uid = this.selectFile.uid;
      if( type == "edit"){
        // 如果是edit，则后台修改原数据
        this.axios
        .post("/GeoProblemSolving/md/update", fileInfo)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // 上传成功
            this.newAddMdModal = false;
            this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'Saved successfully '
                ])
              }
            });
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
      } else {
        // 如果是new，则后台新建file
        this.axios
        .post("/GeoProblemSolving/md/new", fileInfo)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // 上传成功
            this.newAddMdModal = false;
            this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'Saved successfully '
                ])
              }
            });
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
      }
      // websocket
      let send_content = {
        type: "operation",
        sender: this.userInfo.userId,
        behavior: "submit",
        content: {
        }
      };
      sendCustomOperation(send_content);
      
    },

    imgAdd (pos, file) {
        // 第一步.将图片上传到服务器.
        console.log(file);
    },
    imgDel (pos) {
        delete this.img_file[pos]
    },
    change (value, render) {
        // this.html = render
    },
    tempSave(){
      // websocket
      let send_content = {
        type: "operation",
        sender: this.userInfo.userId,
        behavior: "temp-save",
        content: {
          data: this.content,
        }
      };
      sendCustomOperation(send_content);
    },
    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
    sendInputParams: function (modelInEvent, stateIndex) {
      sendInputParams(modelInEvent, stateIndex);
    },


    arrayContains(array, data) {
      var i = array.length;
      while (i--) {
        if (array[i].name != undefined && array[i].name === data.name) {
          return i;
        }
      }
      return i;
    },
  },
};
</script>
<style scoped>
</style>
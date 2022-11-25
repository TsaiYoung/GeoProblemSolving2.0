<style scoped>
/* @import "//fonts.useso.com/css?family=Roboto:300,400,500,700,400italic"; */
@import "https://cdn.bootcss.com/material-design-icons/3.0.1/iconfont/material-icons.css";
* {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
canvas {
  position: absolute;
  z-index: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 100%;
}
.layout {
  background-color: rgb(236, 236, 236);
  height: 100%;
  margin-left: 60px;
}
.header {
  background-color: #2196f3;
  position: relative;
  display: flex;
  align-items: center;
}
.logo {
  display: flex;
  align-items: center;
  font-size: 24px;
  color: white;
  padding: 10px 20px;
}
.nav {
  display: flex;
  width: 80%;
  /* justify-content: center; */
}
.edit_panel {
  /* background-color: aqua; */
  width: 20%;
  /* justify-content属性保证里面的元素垂直居中使用center，居左使用flex-front属性，居右则使用flex-right属性 ,space-between则是两端对齐,space-around则是手拉手对齐方式*/
  /* 不用再加入width:50%；margin-left:25%;margin-right:25%了 */
  justify-content: center;
  display: flex;
  align-content: center;
}

.content {
  overflow: hidden;
  width: 100%;
  height: calc(100vh - 56px);
  display: flex;
}
.content-left {
  width: 280px;
  background-color: white;
}
.content-right {
  width: calc(100% - 280px);
  height: 100%;
  padding: 10px 20px;
  background-color: rgba(0, 0, 0, 0);
}
.body {
  position: relative;
  background-color: white;
  border-radius: 5px;
  height: 100%;
  margin: 0 auto;
}
.setterSize {
  padding: 5%;
  font-size: 0.5rem;
}
.selected {
  background: rgba(0, 0, 0, 0.35) !important;
}
.tab {
  margin-right: 5px;
}
.icon_style {
  width: 20%;
  margin-left: 10%;
  margin-top: 5px;
}
.font_style {
  line-height: 30px;
  font-size: 15px;
  margin-top: 5px;
  margin-left: 10%;
}
.setting_style {
  margin-top: 10px;
  height: 40px;
  display: flex;
}
.edit-btn {
  margin-right: 5px;
}
</style>
<template>
  <div>
    <toolStyle
      :style="{height:windowHeight+'px'}"
      :resources="resources"
      v-on:resourceUrl="selecetResource"
    ></toolStyle>
    <div class="layout">
      <div class="header">
        <div class="logo">
          <span>Drawing</span>
        </div>
        <div class="nav">
          <Button
            label="Clear"
            class="tab demo-flat-button"
            icon="md-close"
            @click="tabfun('clear')"
          >
            <span>Clear</span>
          </Button>
          <Button type="default" class="edit-btn" @click="Drawcancel()">
            <Icon type="md-arrow-round-back" />
          </Button>
          <Button type="default" class="edit-btn" @click="Drawrestore()">
            <Icon type="md-arrow-round-forward" />
          </Button>
          <Button
            type="default"
            class="edit-btn"
            @click="canvas_copy()"
            style="margin-left:10px"
          >Copy</Button>
          <Button type="default" class="edit-btn" @click="canvas_paste()">Paste</Button>
          <Button
            :label="chooseColorBtn"
            class="tab demo-flat-button"
            icon="ios-color-palette"
            @click="setColor()"
            :color="color.hex"
            style="margin-left:10px"
          >
            <span>{{this.chooseColorBtn}}</span>
          </Button>
          <Button
            label="Download"
            class="tab demo-flat-button"
            icon="md-cloud-download"
            @click="tabfun('save')"
            style="margin-left:10px"
          >
            <span>Download</span>
          </Button>
          <a href="javascript:void(0);" ref="download" download="picture.png" v-show="false"></a>
          <Button
            label="Download"
            class="tab demo-flat-button"
            icon="ios-download"
            @click="tabfun('resource')"
          >
            <span>Save to resources</span>
          </Button>
        </div>
        <PhotoshopPicker
          v-model="color"
          v-if="ischoosecolor"
          style="position:absolute;z-index:10;top:56px;"
          @ok="setColor()"
          @cancel="falseColor()"
        />
      </div>
      <div class="content" :style="{height:windowHeight-56+'px'}">
        <div class="content-left">
          <div class="setterSize">
            <span>Thickness of line:{{penSize}}</span>
            <Slider v-model="penSize" :step="1" :max="30"></Slider>
            <span>Dotted length:{{lineType[0]}}</span>
            <Slider v-model="lineType[0]" :step="1" :max="100"></Slider>
            <span>Dotted interval:{{lineType[1]}}</span>
            <Slider v-model="lineType[1]" :step="1" :max="100"></Slider>
          </div>
          <div v-for="tool in tools" :key="tool.index">
            <div class="setting_style" @click="drawType(tool)" :class="{'selected':tool.ischoose}">
              <span class="font_style">{{tool.name}}</span>
            </div>
          </div>
        </div>
        <div class="content-right">
          <div
            class="body"
            :style="{width:canvasSize.width + 'px', height: canvasSize.height + 'px' }"
          >
            <canvas id="canvas" ref="canvas" :style="{cursor:curcursor}"></canvas>
            <canvas id="canvas_bak" ref="canvas_bak" :style="{cursor:curcursor}"></canvas>
          </div>
        </div>
      </div>
      <Modal
        v-model="uploadFileModal"
        title="Save to resource"
        @on-ok="save2Resource('formValidate')"
        ok-text="Submit"
        cancel-text="Cancel"
        width="600px"
      >
        <Form
          ref="formValidate"
          :model="formValidate"
          :rules="ruleValidate"
          :label-width="80"
          style="margin-left:20px"
        >
          <FormItem label="Name" prop="fileName">
            <Input v-model="formValidate.fileName" placeholder="*.png" style="width: 400px" />
          </FormItem>
          <FormItem label="Description" prop="fileDescription">
            <Input
              v-model="formValidate.fileDescription"
              type="textarea"
              placeholder="Enter something..."
              style="width: 400px"
            />
          </FormItem>
        </Form>
      </Modal>
    </div>
  </div>
</template>
<script>
import { Photoshop } from "vue-color";
import toolStyle from "./toolStyle";
export default {
  components: {
    PhotoshopPicker: Photoshop,
    toolStyle
  },
  data() {
    return {
      windowHeight: window.innerHeight,
      //关于复制
      copyimgdata: "",
      //关于协同 -- point:记录点集，line 记录线，lines记录线集
      points: [],
      lines: [],
      storeLines: [], // 用于前进后退
      send_line: [],
      //将canvas的宽高设置好
      canvasSize: {
        width: window.screen.availWidth - 380,
        height: window.screen.availHeight - 180
      },
      canvas: this.$refs.canvas,
      canvasTop: 67,
      canvasLeft: 360,
      context: null,
      canvas_bak: this.$refs.canvas_bak,
      context_bak: null,
      ischoosecolor: false,
      toolsToggle: false,
      chooseColorBtn: "Select color",
      isMouseDown: false,
      color: {
        hex: "#2196f3",
        hsl: {
          h: 205,
          s: 86,
          l: 0,
          a: 1
        },
        hsv: {
          h: 205,
          s: 86,
          v: 92,
          a: 1
        },
        rgba: {
          r: 33,
          g: 150,
          b: 243,
          a: 1
        },
        a: 1
      },
      penSize: 1,
      lineType: [0, 0],
      canDraw: false,
      curcursor: "auto",
      canvasList: [],
      storeCanvasList: [],
      tools: [
        {
          name: "Pencil",
          icon: "mode_edit",
          fun: "pencil",
          ischoose: false
        },
        {
          name: "Beeline",
          icon: "remove",
          fun: "line",
          ischoose: false
        },
        {
          name: "Circle",
          icon: "panorama_fish_eye",
          fun: "circle",
          ischoose: false
        },
        {
          name: "Rectangle",
          icon: "crop_square",
          fun: "square",
          ischoose: false
        },
        {
          name: "Graffiti",
          icon: "brush",
          fun: "handwriting",
          ischoose: false
        },
        {
          name: "Eraser",
          icon: "border_style",
          fun: "rubber",
          ischoose: false
        }
      ],
      tabs: [
        {
          name: "Clear",
          icon: "md-close",
          fun: "clear"
        },
        {
          name: "Download",
          icon: "ios-download",
          fun: "save"
        }
      ],
      uploadFileModal: false,
      formValidate: {
        fileName: "",
        fileDescription: ""
      },
      ruleValidate: {
        fileName: [
          { required: true, message: "Please select type...", trigger: "blur" }
        ],
        fileDescription: [
          { required: false, message: "Drawing tool", trigger: "blur" }
        ]
      },
      // toolStyle 组件
      resources: [],
      dataUrl: "",
      pageParams: { pageId: "", userId: "", userName: "" },
      userInfo: {}
    };
  },
  beforeRouteEnter: (to, from, next) => {
    next(vm => {
      if (!vm.$store.getters.userState || vm.$store.getters.userId == "") {
        vm.$router.push({ name: "Login" });
      } else {
      }
    });
  },
  created() {},
  beforeDestroy() {
    window.removeEventListener("resize", this.initSize);
    this.canvas_bak.removeEventListener("click", this.falseColor);
  },
  mounted() {
    this.initSize();
    this.getStepInfo();
    this.getUserInfo();
    this.initCanvas();
    this.initDrag();
    this.addkeyBoardlistener();
    this.drawType(this.tools[0]);
    this.getResources();
    this.canvas_bak.addEventListener("click", this.falseColor);
    window.addEventListener("resize", this.initSize);
  },
  methods: {
    initSize() {
      $("#app").css("min-width", "0");
      $("#app").css("min-height", "0");
      this.windowHeight = window.innerHeight;
      this.canvasSize = {
        width: window.screen.availWidth - 380,
        height: window.screen.availHeight - 180
      };
    },
    getStepInfo() {
      if (
        this.$route.params.groupID == undefined ||
        this.$route.params.groupID == ""
      ) {
        var href = window.location.href;
        var url = href.split("&");

        for (var i = 0; i < url.length; i++) {
          if (/groupID/.test(url[i])) {
            this.pageParams.pageId = url[i].match(/groupID=(\S*)/)[1];
            continue;
          }

          if (/userID/.test(url[i])) {
            this.pageParams.userId = url[i].match(/userID=(\S*)/)[1];
            continue;
          }

          if (/userName/.test(url[i])) {
            this.pageParams.userName = url[i].match(/userName=(\S*)/)[1];
            continue;
          }
        }
      } else {
        this.pageParams.pageId = this.$route.params.groupID;
        this.pageParams.userId = this.$route.params.userID;
        this.pageParams.userName = this.$route.params.userName;
      }
    },
    getUserInfo() {
      this.userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
      if (this.userInfo == {}) {
        this.axios
          .get(
            "/GeoProblemSolving/user" +
            "?key=userId" +
            "&value=" +
              this.pageParams.userId
          )
          .then(res => {
            if (res.data.code == 0) {
              this.$set(this, "userInfo", res.data.data);
            }
          })
          .catch(err => {});
      }
    },
    initCanvas() {
      this.canvas = document.getElementById("canvas");
      this.canvas.width = this.canvasSize.width;
      this.canvas.height = this.canvasSize.height;
      this.context = this.canvas.getContext("2d");
      this.canvas_bak = document.getElementById("canvas_bak");
      this.canvas_bak.width = this.canvasSize.width;
      this.canvas_bak.height = this.canvasSize.height;
      this.context_bak = this.canvas_bak.getContext("2d");
    },
    setColor() {
      this.chooseColorBtn = this.ischoosecolor ? "Select color" : "Ok";
      this.ischoosecolor = this.ischoosecolor ? false : true;
      //这里选择后需要将属性传递过去
    },
    falseColor() {
      this.ischoosecolor = false;
    },
    drawType(pen) {
      switch (pen.fun) {
        case "pencil":
          this.curcursor =
            "url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAAZ0lEQVR4AdXOrQ2AMBRF4bMc/zOUOSrYoYI5cQQwpAieQDW3qQBO7Xebxx8bWAk5/CASmRHzRHtB+d0Bkw0W5ZiT0SYbFcl6u/2eeJHbxIHOhWO6Er6/y9syXpMul5PLefAGKZ1/rwtTimwbWLpiCgAAAABJRU5ErkJggg==') 3 24,  auto";
          break;
        case "rubber":
          this.curcursor = "pointer";
          break;
        case "circle":
        case "square":
        case "line":
        case "handwriting":
          this.curcursor = "crosshair";
          break;
        default:
          this.curcursor = "auto";
          break;
      }
      this.draw_graph(pen.fun);
      this.chooseImg(pen);
    },
    //选择功能按钮 修改样式
    chooseImg(obj) {
      for (let i = 0; i < this.tools.length; i++) {
        this.tools[i].ischoose = false;
      }
      obj.ischoose = true;
    },
    tabfun(fun) {
      if (fun === "clear") {
        this.clearContext("1");

        this.lines = [];
      } else if (fun === "save") {
        this.downloadImage();
      } else if (fun === "resource") {
        this.uploadFileModal = true;
      }
    },
    getBlobBydataURI(dataurl) {
      var arr = dataurl.split(","),
        mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]),
        n = bstr.length,
        u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      return new Blob([u8arr], { type: mime });
    },
    save2Resource(name) {
      if (this.pageParams.pageId == undefined || this.pageParams.pageId == "") {
        this.$Message.error("Lose the information of current step.");
        return false;
      }

      this.$refs[name].validate(valid => {
        if (valid) {
          //name
          let filename = "";
          if (!/\.(png)$/.test(this.formValidate.fileName.toLowerCase())) {
            filename = this.formValidate.fileName + ".png";
          } else {
            filename = this.formValidate.fileName;
          }

          // description
          let description = "";
          if (this.formValidate.fileDescription == "") {
            description = "from Drawing tool";
          } else {
            description = this.formValidate.fileDescription;
          }

          //thumbnail
          let thumbnail = this.canvas;
          thumbnail.width = 120; // canvas对图片进行缩放
          thumbnail.height = 120;
          let thumbnailUrl = thumbnail.toDataURL();
          let thumbnailBlob = this.getBlobBydataURI(thumbnailUrl);
          var thumbnailBlobFile = new File(
            [thumbnailBlob],
            "thumbnail_" + filename
          );

          //file
          let imageUrl = this.canvas.toDataURL();
          let imageBlob = this.getBlobBydataURI(imageUrl);
          var fileOfBlob = new File([imageBlob], filename);

          let imageForm = new FormData();
          imageForm.append("file", fileOfBlob);
          imageForm.append("description", description);
          imageForm.append("type", "toolData:Sketchpad");
          imageForm.append("uploaderId", this.userInfo.userId);
          imageForm.append("privacy", "private");
          imageForm.append("folderId", this.pageParams.pageId);
          imageForm.append("thumbnail", thumbnailBlobFile);

          this.axios
            .post("/GeoProblemSolving/folder/uploadToFolder", imageForm)
            .then(res => {
              if (res.data.uploaded.length > 0) {
                this.$Notice.open({
                  title: "Upload notification title",
                  desc: "File uploaded successfully",
                  duration: 0
                });

                // 文件列表更新
                let dataName = res.data.uploaded[0].name;
                let dataItem = {
                  name: filename,
                  description: "drawing tool data",
                  pathURL: "/GeoProblemSolving/resource/upload/" + dataName
                };
                this.resources.push(dataItem);

                // 初始化formValidation
                this.formValidate = {
                  fileName: "",
                  fileDescription: ""
                };
              }
            })
            .catch(err => {});
        } else {
          this.$Message.error("Please enter the necessary information!");
        }
      });
    },
    draw_graph(graphType) {
      this.canvas_bak.style.zIndex = 1; //蒙版设置在顶层
      //先画在蒙版上 再复制到画布上
      this.canDraw = false;
      let startX, startY;
      //鼠标按下获取 开始xy开始画图
      let mousedown = e => {
        e = e || window.event;
        startX =
          e.clientX -
          this.canvasLeft +
          (document.body.scrollLeft + document.documentElement.scrollLeft);
        startY =
          e.clientY -
          this.canvasTop +
          (document.body.scrollTop + document.documentElement.scrollTop);

        //记录轨迹 --by mzy
        this.points = [];
        let point = {
          X: startX,
          Y: startY
        };
        this.points.push(point);

        this.drawOnMousedown(
          startX,
          startY,
          graphType,
          this.penSize,
          this.color.hex
        );

        this.isMouseDown = true;
      };
      //鼠标离开 把蒙版canvas的图片生成到canvas中
      let mouseup = e => {
        e = e || window.event;

        if (this.isMouseDown) {
          let x =
            e.clientX -
            this.canvasLeft +
            (document.body.scrollLeft + document.documentElement.scrollLeft);
          let y =
            e.clientY -
            this.canvasTop +
            (document.body.scrollTop + document.documentElement.scrollTop);

          this.drawOnMouseup(x, y, graphType);
        }

        this.isMouseDown = false;

        let line = {
          Graphtype: graphType,
          Linetype: this.lineType,
          Color: this.color.hex,
          Pensize: this.penSize,
          Points: this.points
        };
        this.lines.push(line);
      };
      // 鼠标移动
      let mousemove = e => {
        if (this.isMouseDown) {
          e = e || window.event; //为了使多种浏览器兼容
          let x =
            e.clientX -
            this.canvasLeft +
            (document.body.scrollLeft + document.documentElement.scrollLeft);
          let y =
            e.clientY -
            this.canvasTop +
            (document.body.scrollTop + document.documentElement.scrollTop);

          //记录轨迹 --by mzy
          let point = {
            X: x,
            Y: y
          };
          this.points.push(point);

          this.drawOnMousemove(
            x,
            y,
            startX,
            startY,
            graphType,
            this.lineType,
            this.penSize,
            this.color.hex
          );
        }
      };
      //鼠标离开区域以外 除了涂鸦 都清空
      let mouseout = () => {
        if (graphType != "handwriting") {
          this.clearContext();
        }
      };
      //给蒙布添加各类鼠标事件
      this.canvas_bak.onmousedown = () => mousedown();
      this.canvas_bak.onmousemove = () => mousemove();
      this.canvas_bak.onmouseup = () => mouseup();
      this.canvas_bak.onmouseout = () => mouseout();
    },
    drawOnMousedown(x, y, graphType, penSize, color) {
      this.context.strokeStyle = color;
      this.context_bak.strokeStyle = color;
      this.context_bak.lineWidth = penSize;

      this.context_bak.moveTo(x, y); //蒙布的坐标指向鼠标所在的点位
      this.canDraw = true;
      if (graphType == "pencil") {
        this.context_bak.beginPath(); //开始一条路径
      } else if (graphType == "circle") {
        this.context.beginPath();
        // this.context.moveTo(x, y);
        this.context.lineTo(x + 2, y + 2);
        this.context.stroke();
      } else if (graphType == "rubber") {
        this.context.clearRect(
          x - penSize * 10,
          y - penSize * 10,
          penSize * 20,
          penSize * 20
        );
      }
    },
    drawOnMousemove(x, y, startX, startY, graphType, lineType, penSize, color) {
      this.context_bak.setLineDash(lineType); //设置虚线样式的方法，参数是一个数组
      //方块  即4条直线
      if (graphType == "square") {
        if (this.canDraw) {
          this.context_bak.beginPath();
          this.clearContext();
          this.context_bak.moveTo(startX, startY);
          this.context_bak.lineTo(x, startY);
          this.context_bak.lineTo(x, y);
          this.context_bak.lineTo(startX, y);
          this.context_bak.lineTo(startX, startY);
          this.context_bak.stroke(); //stroke() 方法会实际地绘制出通过 moveTo() 和 lineTo() 方法定义的路径。
        }
        //直线
      } else if (graphType == "line") {
        if (this.canDraw) {
          this.context_bak.beginPath();
          this.clearContext();
          this.context_bak.moveTo(startX, startY);
          this.context_bak.lineTo(x, y);
          this.context_bak.stroke();
        }
        //画笔
      } else if (graphType == "pencil") {
        if (this.canDraw) {
          this.context_bak.lineTo(x, y);
          this.context_bak.stroke();
        }
        //圆 未画得时候 出现一个小圆
      } else if (graphType == "circle") {
        this.clearContext();
        if (this.canDraw) {
          this.context_bak.beginPath();
          let radii = Math.sqrt(
            (startX - x) * (startX - x) + (startY - y) * (startY - y)
          );
          this.context_bak.arc(startX, startY, radii, 0, Math.PI * 2, false);
          this.context_bak.stroke();
        } else {
          this.context_bak.beginPath();
          this.context_bak.arc(x, y, 20, 0, Math.PI * 2, false);
          this.context_bak.stroke();
        }
        //涂鸦 未画得时候 出现一个小圆
      } else if (graphType == "handwriting") {
        if (this.canDraw) {
          this.context_bak.beginPath();
          this.context_bak.strokeStyle = color;
          this.context_bak.fillStyle = color;
          this.context_bak.arc(x, y, penSize * 10, 0, Math.PI * 2, false);
          this.context_bak.fill();
          this.context_bak.stroke();
        } else {
          this.clearContext();
          this.context_bak.beginPath();
          this.context_bak.fillStyle = color;
          this.context_bak.strokeStyle = color;
          this.context_bak.arc(x, y, penSize * 10, 0, Math.PI * 2, false);
          this.context_bak.fill();
          this.context_bak.stroke();
        }
        //橡皮擦 不管有没有在画都出现小方块 按下鼠标 开始清空区域
      } else if (graphType == "rubber") {
        this.context_bak.setLineDash([0, 0]);
        this.context_bak.lineWidth = 1;
        this.clearContext();
        this.context_bak.beginPath();
        this.context_bak.strokeStyle = "#000000";
        this.context_bak.moveTo(x - penSize * 10, y - penSize * 10);
        this.context_bak.lineTo(x + penSize * 10, y - penSize * 10);
        this.context_bak.lineTo(x + penSize * 10, y + penSize * 10);
        this.context_bak.lineTo(x - penSize * 10, y + penSize * 10);
        this.context_bak.lineTo(x - penSize * 10, y - penSize * 10);
        this.context_bak.stroke();
        if (this.canDraw) {
          this.context.clearRect(
            x - penSize * 10,
            y - penSize * 10,
            penSize * 20,
            penSize * 20
          );
        }
        this.context_bak.setLineDash(lineType);
      }
    },
    drawOnMouseup(x, y, graphType) {
      this.canDraw = false;

      let image = new Image(); //创建一个image对象
      if (graphType != "rubber") {
        image.src = this.canvas_bak.toDataURL(); //方法返回一个包含图片展示的data URI.参数是type与encoderOptions，分别表示图片格式与图片质量，0到1之间

        // 相当于给浏览器缓存了一张图片
        // iamge onload事件在图片加载完后立即执行
        image.onload = () => {
          this.context.drawImage(
            image,
            0,
            0,
            image.width,
            image.height,
            0,
            0,
            this.canvasSize.width,
            this.canvasSize.height
          ); //在画布上绘制图像
          this.clearContext();
          this.saveImageToAry();
        };
        this.context.beginPath();
        // this.context.moveTo(x, y);
        this.context.lineTo(x + 2, y + 2);
        this.context.stroke();
      }
    },
    clearContext(type) {
      if (!type) {
        this.context_bak.clearRect(
          0,
          0,
          this.canvasSize.width,
          this.canvasSize.height
        );
      } else {
        this.context.clearRect(
          0,
          0,
          this.canvasSize.width,
          this.canvasSize.height
        );
        this.context_bak.clearRect(
          0,
          0,
          this.canvasSize.width,
          this.canvasSize.height
        );
      }
    },
    downloadImage() {
      this.$refs.download.href = this.canvas.toDataURL();
      this.$refs.download.click();
    },
    cancel() {
      if (this.canvasList.length > 0) {
        this.context.clearRect(
          0,
          0,
          this.canvasSize.width,
          this.canvasSize.height
        );
        this.storeCanvasList.push(this.canvasList.pop());
        let url = this.canvasList[this.canvasList.length - 1];
        let image = new Image();
        image.src = url;
        image.onload = () => {
          this.context.drawImage(
            image,
            0,
            0,
            image.width,
            image.height,
            0,
            0,
            this.canvasSize.width,
            this.canvasSize.height
          );
        };
      }
    },
    next() {
      if (this.storeCanvasList.length > 0) {
        let image = new Image();
        this.context.clearRect(
          0,
          0,
          this.canvasSize.width,
          this.canvasSize.height
        );
        let url = this.storeCanvasList.pop();
        this.canvasList.push(url);
        image.src = url;
        image.onload = () => {
          this.context.drawImage(
            image,
            0,
            0,
            image.width,
            image.height,
            0,
            0,
            this.canvasSize.width,
            this.canvasSize.height
          );
        };
      }
    },
    //保存历史 用于撤销
    saveImageToAry() {
      let dataUrl = this.canvas.toDataURL();
      this.canvasList.push(dataUrl);
      this.storeCanvasList = [];
    },
    // 处理文件拖入事件，防止浏览器默认事件带来的重定向
    handleDragOver(evt) {
      evt.stopPropagation();
      evt.preventDefault();
    },
    isImage(type) {
      switch (type) {
        case "image/jpeg":
        case "image/png":
        case "image/gif":
        case "image/bmp":
        case "image/jpg":
          return true;
        default:
          return false;
      }
    },
    // 处理拖放文件列表
    handleFileSelect(evt) {
      evt.stopPropagation();
      evt.preventDefault();
      let files = evt.dataTransfer.files;
      for (let i = 0, f; (f = files[i]); i++) {
        let t = f.type ? f.type : "n/a";
        let reader = new FileReader();
        let isImg = this.isImage(t);
        // 处理得到的图片
        if (isImg) {
          reader.onload = (() => {
            return e => {
              let image = new Image();
              image.src = e.target.result;
              image.onload = () => {
                this.context.drawImage(
                  image,
                  0,
                  0,
                  image.width,
                  image.height,
                  0,
                  0,
                  this.canvasSize.width,
                  this.canvasSize.height
                );
              };
            };
          })(f);
          reader.readAsDataURL(f);
        }
      }
    },
    //初始化拖入效果
    initDrag() {
      let dragDiv = document.getElementById("canvas_bak");
      dragDiv.addEventListener("dragover", this.handleDragOver, false);
      dragDiv.addEventListener("drop", this.handleFileSelect, false);
    },
    addkeyBoardlistener() {
      document.onkeydown = event => {
        let e = event || window.event || arguments.callee.caller.arguments[0];
        if (e.keyCode === 89 && e.ctrlKey) {
          //ctrl+Y
          this.next();
        }
        if (e.keyCode === 90 && e.ctrlKey) {
          // ctrl+Z
          this.cancel();
        }
      };
    },
    Drawcancel() {
      if (this.lines.length > 0) {
        this.cancel();
        this.storeLines.push(this.lines.pop());
      }
    },
    Drawrestore() {
      if (this.storeLines.length > 0) {
        this.next();
        this.lines.push(this.storeLines.pop());
      }
    },
    canvas_copy() {
      var context = document.getElementById("canvas").getContext("2d");
      var width = context.canvas.width;
      var height = context.canvas.height;
      this.copyimgdata = context.getImageData(0, 0, width, height);
    },
    canvas_paste() {
      var context = document.getElementById("canvas_bak").getContext("2d");
      context.putImageData(this.copyimgdata, 300, 0);
      this.canvas_bak.onmouseup();
    },
    collaDrawLine(line) {
      let pointsLength = line.Points.length;
      let collaGraphType = line.Graphtype;
      let collaPenSize = line.Pensize;
      let collaLineSize = line.Linetype;
      let collaColor = line.Color;
      let sX = line.Points[0].X;
      let sY = line.Points[0].Y;
      // first
      this.drawOnMousedown(sX, sY, collaGraphType, collaPenSize, collaColor);
      // second
      for (let i = 1; i < pointsLength - 1; i++) {
        let x = line.Points[i].X;
        let y = line.Points[i].Y;
        this.drawOnMousemove(
          x,
          y,
          sX,
          sY,
          collaGraphType,
          collaLineSize,
          collaPenSize,
          collaColor
        );
      }
      // third
      let eX = line.Points[pointsLength - 1].X;
      let eY = line.Points[pointsLength - 1].Y;
      this.drawOnMouseup(eX, eY, collaGraphType);
    },
    getResources() {
      if (this.pageParams.pageId == undefined || this.pageParams.pageId == "") {
        this.$Message.error("Lose the information of current step.");
        return false;
      }

      this.resources = [];
      this.axios
        .get(
          "/GeoProblemSolving/folder/inquiry?folderId=" + this.pageParams.pageId
        )
        .then(res => {
          // 写渲染函数，取到所有资源
          if (res.data !== "None") {
            for (let i = 0; i < res.data.files.length; i++) {
              if (
                res.data.files[i].type == "Image" &&
                /\.(jpg|jpeg|png|bmp|gif)$/.test(res.data.files[i].name.toLowerCase())
              ) {
                this.resources.push(res.data.files[i]);
              }
            }
          } else {
            this.resources = [];
          }
        })
        .catch(err => {
          console.log(err.data);
        });
    },
    selecetResource(url) {
      this.dataUrl = url;
      this.viewData();
    },
    viewData() {
      if (/\.(jpg|jpeg|png|bmp|gif)$/.test(this.dataUrl.toLowerCase())) {
        let image = new Image();
        image.src = this.dataUrl;
        image.onload = () => {
          this.context.drawImage(
            image,
            0,
            0,
            image.width,
            image.height,
            0,
            0,
            this.canvasSize.width,
            this.canvasSize.height
          );
        };
      } else {
        this.$Message.error("Worry data format!");
      }
      this.showFile = false;
    }
  }
};
</script>


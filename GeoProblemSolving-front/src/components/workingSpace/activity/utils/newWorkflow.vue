<template>
  <div
    style="display: flex; background-color: #eee; height: calc(100vh - 220px)"
    id="workflow"
  > 
    <div id="drawflow"></div>
    <Card
      dis-hover
      class="mxGraphCard"
      id="mxGraphCard"
      style="height: calc(100vh - 220px)"
    >
      <div style="position: absolute; top: 25px; left: 18px; z-index: 10">
        <info-btn :activityInfo="activityInfo" :projectInfo="projectInfo"></info-btn>
      </div>
      <div style="margin-top: 10px">
        <div
          style="position: absolute; right: 3%; z-index: 1; margin-top: 10px"
        >
          <button @click="zoomActual()" style="cursor: pointer">
            ZoomActual
          </button>
          <button @click="showGraphXml()" style="cursor: pointer">XmlOutput</button>
        </div>
        <div
          style="position: absolute; right: 26%; z-index: 1; margin-top: 10px"
        >
          <Button
            type="primary"
            @click="zoomIn()"
            shape="circle"
            icon="md-add"
            title="Zoom In"
          ></Button>
          <Button
            type="primary"
            @click="zoomOut()"
            shape="circle"
            icon="md-remove"
            title="Zoom Out"
          ></Button>
        </div>
        <div id="graphContainer" ></div>
      </div>
    </Card>
    <Card
      dis-hover
      class="modalCard"
      id="modalCard"
      style="height: calc(100vh - 220px)"
    >
      <div style="width: 430px; border: 1px solid lightgray; position: absolute; top: 10px; right: 10px; z-index: 10">
        <Collapse simple v-model="unfold" class="panel">
            <Panel name="tool">
              Toolbox
              <tool-box slot="content" :activityInfo="activityInfo" :projectInfo="projectInfo" ref="ToolBox"></tool-box>
            </Panel>
            <Panel name="data">
              Resource list
              <res-list slot="content" :activityInfo="activityInfo" ref="ResList"></res-list>
            </Panel>
            <Panel name="introduce">
              Operation Node 
              <Card
                slot="content"
                dis-hover
                style="height: 570px; width: 100%; margin: auto"
                v-if="tempToolInfo.vertex != undefined && tempToolInfo.vertex"
              >
                <h2
                  v-show="tempToolInfo.value != undefined"
                  style="margin-top: 5px; text-align: center;"
                >
                  {{ tempToolInfo.value }}
                </h2>
                <div
                  style="
                    heigth: 74%;
                    width: 90%;
                    float: left;
                    margin-top: 5px;
                    margin-left: 15px;
                  "
                  v-if="tempToolInfo.nodeAttribute.item"
                >
                  <Divider orientation="left">Operation Information</Divider>
                  <div style="margin: 10px 0">
                    <Label>Operation Name:</Label>
                    <span style="margin-left: 10px">{{ tempToolInfo.value }}</span>
                  </div>
                  <div style="margin: 10px 0" >
                    <Label>Description:</Label>
                    <span style="margin-left: 10px">{{ tempToolInfo.nodeAttribute.item.description }}</span>
                  </div>
                  <div style="margin: 10px 0" >
                    <Label>Style:</Label>
                    <span style="margin-left: 10px">{{ tempToolInfo.nodeAttribute.style }}</span>
                  </div>
                  <div v-if="tempToolInfo.nodeAttribute.item.type == 'geo-analysis'">
                    <Divider orientation="left" style="margin: 20px 0"
                      >Tool Information</Divider
                    >
                    <div style="cursor: pointer; margin-top: 15px" v-if="getToolInfoFinish" @click="openTool()" title="Open the tool">
                      <img
                        v-if="
                          selectToolInfo.toolImg != null &&
                          selectToolInfo.toolImg != '' &&
                          selectToolInfo.toolImg != undefined
                        "
                        :src="selectToolInfo.toolImg"
                        :title="selectToolInfo.toolName"
                        style="
                          margin-left: 35%;
                          margin-top: 10px;
                          width: 70px;
                          height: 70px;
                          border-radius: 50%;
                        "
                      />
                      <avatar
                        :size="70"
                        :username="selectToolInfo.toolName"
                        style="margin-top: 10px; margin-left: 35%"
                        v-else
                      />
                    </div>
                    <div style="margin: 10px 0">
                      <Label>Tool Name:</Label>
                      <span style="margin-left: 10px">{{ selectToolInfo.toolName }}</span>
                    </div>
                    <div style="margin: 10px 0">
                      <Label>Privacy:</Label>
                      <span style="margin-left: 10px">{{ selectToolInfo.privacy }}</span>
                    </div>
                    <div style="margin: 10px 0">
                      <Label>Tags:</Label>
                      <span style="margin-left: 10px">{{ toolTags }}</span>
                    </div>
                    <div style="margin: 10px 0">
                      <Label>Created Time:</Label>
                      <span style="margin-left: 10px">{{
                        selectToolInfo.createdTime
                      }}</span>
                    </div>
                    <div style="margin: 10px 0">
                      <Label>Description:</Label>
                      <span style="margin-left: 10px">{{
                        selectToolInfo.description
                      }}</span>
                    </div>
                    <Button 
                      type="primary" 
                      style="cursor: pointer; margin-top: 10px; width: 90px" 
                      v-show="!tempToolInfo.finished"
                      @click="openTool">Open Tool
                    </Button>
                    <Button 
                      type="primary" 
                      style="cursor: pointer; margin-top: 10px; margin-left:15px; width: 90px" 
                      v-show="(userRole == 'manager' ||  userRole == 'coreteam') && !tempToolInfo.finished"
                      @click="operationNodeFinished">Done
                    </Button>
                    <Button 
                      type="primary" 
                      style="cursor: pointer; margin-top: 10px; margin-left:15px; width: 90px" 
                      v-show="(userRole == 'manager' ||  userRole == 'coreteam') && tempToolInfo.finished && tempToolInfo.finished == true"
                      @click="operationNodeRecover">Recover
                    </Button>
                  </div>
                  <div v-else-if="tempToolInfo.nodeAttribute.item.type == 'resource'">
                    <Divider orientation="left" style="margin: 30px 0"
                      >Resource Operation</Divider
                    >
                    <div style="margin: 10px 0">
                      <Label>Behavior:</Label>
                      <span style="margin-left: 10px">{{ tempToolInfo.nodeAttribute.item.behavior }}</span>
                    </div>
                    <div style="margin: 10px 0">
                      <Label>type:</Label>
                      <span style="margin-left: 10px">{{ tempToolInfo.nodeAttribute.item.suffix }}</span>
                    </div>
                    <Button 
                      type="success" 
                      style="cursor: pointer; margin-top: 10px; width: 70px" 
                      v-show="!tempToolInfo.finished"
                      @click="resUpload">Upload
                    </Button>
                    <Button 
                      type="success" 
                      style="cursor: pointer; margin-top: 10px; margin-left:15px; width: 70px" 
                      v-show="(userRole == 'manager' ||  userRole == 'coreteam') && !tempToolInfo.finished"
                      @click="operationNodeFinished">Done
                    </Button>
                    <Button 
                      type="success" 
                      style="cursor: pointer; margin-top: 10px; margin-left:15px; width: 90px" 
                      v-show="(userRole == 'manager' ||  userRole == 'coreteam') && tempToolInfo.finished && tempToolInfo.finished == true"
                      @click="operationNodeRecover">Recover
                    </Button>
                  </div>
                  
                </div>
              </Card>
              <Card
                slot="content"
                dis-hover
                style="height: 350px; width: 100%; margin: auto; text-align: center;"
                v-else
              >
                <h2 style="color: #808695; margin-top: 125px;">No operation</h2>
                <small style="color: #dcdee2" 
                  >*Click on the workflow node to view details.</small
                >
              </Card>
            </Panel>
        </Collapse>
      </div>
      
    </Card>
    <Modal v-model="vertexInfoShow" title="Vertex Infomation">
      <div>
        <span>Id:</span> {{ selectVertex.id }} <br />
        <span>Style:</span> {{ selectVertex.style }} <br />
        <span>Name:</span> {{ selectVertex.value }}
      </div>
      <div slot="footer" style="display: inline-block">
        <Button
          type="primary"
          @click="vertexInfoShow = false"
          style="float: right; height: 30px; width: 70px"
          >OK
        </Button>
        <Button
          @click="vertexInfoShow = false"
          style="float: right; margin-right: 15px; height: 30px; width: 70px"
          >Cancel
        </Button>
      </div>
    </Modal>
    <Modal v-model="showGrapgXml" title="Template Workflow Xml" :footer-hide="true" :draggable="true" width="650">
      <div >
        <textarea :value="graphXml" style="width: 100%; height:400px;"> </textarea>
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>

<script>
import mxgraph from "../../../projects/MxGraph/index";
import Avatar from "vue-avatar";
import resList from "./resList";
import toolBox from "./toolBox";
import infoBtn from "./infoBtn";
import loginModal from "../../../user/userState/loginModal.vue";
import * as socketApi from "../../../../api/socket";
// import resList from "../workingSpace/activity/utils/resList.vue";
// import toolBox from "../workingSpace/activity/utils/toolBox.vue";

const {
  mxGraph,
  mxClient,
  mxEvent,
  mxCodec,
  mxUtils,
  mxConstants,
  mxPerimeter,
  mxHierarchicalLayout,
  mxCompactTreeLayout,
  mxSwimlaneLayout,
} = mxgraph;

export default {
  name: "mxgraph",
  props: ["activityInfo","userInfo","projectInfo"],
  components: {
    Avatar,
    resList,
    toolBox,
    infoBtn,
    loginModal
  },
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      userRole: this.roleIdentity(this.activityInfo),
      unfold: [],
      getToolInfoFinish: false,
      creatorInfo: {},
      participants: [],
      //恢复登录的模态框
      tempLoginModal: false,
      graph: null,
      geoAnalysisList: [],
      selectCell: {},
      inputList: [],
      outputList: [],
      vertexList: [],
      edgeList: [],
      operationList: [
        {
          id: "9585fd8a-4455-4b58-8b26-b4c0f7933094",
          name: "json资源上传",
          description: "json资源上传",
          type: "resource",
          behavior: "upload",
          resId: "9dcf26ad-1f3e-4673-bcb3-7b257745fe3e",
          suffix: ".json",
        },
        {
          id: "10572a76-e6c8-4d08-3d64-b2f779e76af5",
          name: "map tool",
          description: "载入json资源，并进行修改",
          type: "geo-analysis",
          resList: [
            {
              type: "input",
              resId: "9dcf26ad-1f3e-4673-bcb3-7b257745fe3e",
              suffix: ".json",
            },
            {
              type: "output",
              resId: "29935f40-88ab-4303-ae53-13ecbb8aaa2e",
              suffix: ".json",
            },
          ],
          toolInfo: {
              tid: "00678bbf-a0e1-4285-92e5-4f2dd2ea8858",
          }
        },
        {
          id: "951a147c-ec41-4f18-a74b-8572626c961a",
          name: "json 转 xml",
          description: "json 转 xml",
          type: "geo-analysis",
          resList: [
            {
              type: "input",
              resId: "29935f40-88ab-4303-ae53-13ecbb8aaa2e",
              suffix: ".json",
            },
            {
              type: "output",
              resId: "d9f75752-98a2-42e1-93f1-8e5cf5c73560",
              suffix: ".xml",
            },
          ],
          toolInfo: {
              tid: "cc94e0c1-5958-4715-a4e0-4360d5fa6e06",
          }
        },
        
      ],
      selectVertex: {},
      vertexInfoShow: false,
      slctRoleMember: {},
      memberRoleModal: false,
      roleSelected: "",
      tempToolInfo: {},
      selectToolInfo: {},
      operationRecords: [],
      graphXml: "",
      originGraphXml: "",
      showGrapgXml: false,
      taskSocketId: "",
    };
  },
  watch: {
    activityInfo: {
      immediate: true,
      handler(){
          this.taskSocketId = `OperationServer/workflow${this.projectInfo.aid}/${this.activityInfo.aid}`;
      }
    }
  },
  created() {
    // this.getProjectInfo();
    this.operationApi.getActivityDoc(this.activityInfo.aid);
  },
  mounted() {
    // this.operationShow();
    this.initMxGraph();
    this.listenGraphEvent();
    this.initZoomActual();
    this.originGraphXml = this.getXml();
  },
  beforeDestroy() {
    // window.removeEventListener("message", this.toolMsgHandle, false);
  },
  methods: {

    //socket协同
    socketOnMessage(messageJson){
      let behavior = messageJson.behavior;
      let content = messageJson.content;
      if (messageJson.type == "general") {
        if (behavior == "operateNode") {
          if(this.tempToolInfo.nodeAttribute){
            //离开上一个选中的节点
            this.mxgraphOperation("leave");
          }
          this.tempToolInfo = this.getVertexById(content.nodeId);
          this.tempToolInfo.nodeAttribute.style = content.data.style;
          console.log(this.tempToolInfo);
          //进入该节点
          this.mxgraphOperation("enter");

          if(this.tempToolInfo.nodeAttribute.item.type == "geo-analysis"){
            this.getToolInfo();
          }

          this.unfold = [];
          this.unfold.push("introduce");

          //进入节点提醒
          let on = {
            click: () => {
              this.openTool();
            },
          }
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', content.sender),
                  ' has joined the operation-node ',
                  h('a', 
                    {
                      on:on,
                      style:{
                        textDecoration: "underline",
                      },
                    },
                    content.data.item.name
                  ),
                  ' , click to follow.'
                ])
              }
          });
        } else if(behavior == "operateNodeDone"){
          if(this.tempToolInfo.nodeAttribute){
            //离开上一个选中的节点
            this.mxgraphOperation("leave");
          }
          this.tempToolInfo = this.getVertexById(content.nodeId);
          this.tempToolInfo.nodeAttribute.style = content.data.style;
          this.tempToolInfo.finished = true;
          this.mxgraphOperation("finished");

          if(this.tempToolInfo.nodeAttribute.item.type == "geo-analysis"){
            this.getToolInfo();
          }
          this.unfold = [];
          this.unfold.push("introduce");

          //节点完成提醒
          this.$Notice.success({
              title: 'Operation-node changed',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', content.sender),
                  ' has changed the operation-node ',
                  h('a', 
                    content.data.item.name
                  ),
                  ' style: done.'
                ])
              }
          });
        } else if(behavior == "operateNodeRecover"){
          if(this.tempToolInfo.nodeAttribute){
            //离开上一个选中的节点
            this.mxgraphOperation("leave");
          }
          this.tempToolInfo = this.getVertexById(content.nodeId);
          this.tempToolInfo.nodeAttribute.style = content.data.style;
          this.tempToolInfo.finished = false;
          this.mxgraphOperation("recover");

          if(this.tempToolInfo.nodeAttribute.item.type == "geo-analysis"){
            this.getToolInfo();
          }
          this.unfold = [];
          this.unfold.push("introduce");

          //节点恢复提醒
          this.$Notice.success({
              title: 'Operation-node changed',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', content.sender),
                  ' has recovered the operation-node ',
                  h('a', content.data.item.name
                  ),
                  ' style: todo.'
                ])
              }
          });
        }
      }
    },
    roleIdentity(activity) {
      return this.userRoleApi.roleIdentify(
        activity.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, role, operation) {
      if (permission == undefined)
        permission = JSON.stringify(this.userRoleApi.getDefault());
      if (operation == "auto_join") {
        if (JSON.parse(permission).auto_join.visitor == "Yes") return true;
        else if (JSON.parse(permission).auto_join.visitor == "No") return false;
        else {
          return this.getParentPermission();
        }
      } else {
        return this.userRoleApi.permissionIdentity(
          JSON.parse(permission),
          role,
          operation
        );
      }
    },
    initMxGraph() {
      if (!mxClient.isBrowserSupported()) {
        // 判断是否支持mxgraph
        mxUtils.error("Browser is not supported!", 200, false);
      } else {
        // 在容器中创建图表
        let container = document.getElementById("graphContainer");
        let MxGraph = mxGraph;
        let MxCodec = mxCodec;
        var graph = new MxGraph(container);
        this.graph = graph;
        var parent = graph.getDefaultParent();

        let style = this.graph.getStylesheet().getDefaultEdgeStyle();
        console.log(style);

        //定义布局
        var layout = new mxHierarchicalLayout(graph);

        graph.getModel().beginUpdate();
        try {
          this.operationListToGraph();
          
          layout.execute(parent);
        //   graph.center(true, true, 0.1, 0.1);

          // graph.setEnabled(false);//graph只能预览
          graph.setCellsResizable(false); //节点不可改变大小
          mxGraphHandler.prototype.setMoveEnabled(false); //是否可以移动
          graph.setPanning(true); //拖动
          //禁用浏览器默认的右键菜单栏
          mxEvent.disableContextMenu(container);
          graph.setCellsMovable(false); //线是否可移动​
          // 是否可以移动连线，重新连接其他cell，主要用来展现中用
          graph.setCellsLocked(true);
        } finally {
          // Updates the display
          graph.getModel().endUpdate();
        }
        this.graph = graph;
        // this.zoomActual();
        // this.restoreModel();
      }
    },

    operationListToGraph() {
      let list = this.operationList;
      //读取input和output信息，并生成vertex
      list.forEach((item, index) => {
        if (item.type == "geo-analysis") {
          if (item.resList.length > 0) {
            for (let i = 0; i < item.resList.length; i++) {
              if (item.resList[i].type == "input") {
                this.inputList.push({
                  resId: item.resList[i].resId,
                  type: item.resList[i].type,
                  vertexId: item.id,
                });
              } else {
                this.outputList.push({
                  resId: item.resList[i].resId,
                  type: item.resList[i].type,
                  vertexId: item.id,
                });
              }
            }
          }
          let vertex = this.graph.insertVertex(
            this.graph.getDefaultParent(),
            item.id,
            item.name,
            200,
            200,
            150, //width
            40, //height
            "fillColor=#5cadff;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;"
          );

          //绑定相关信息
          vertex.nodeAttribute = {};
          vertex.nodeAttribute.resList = item.resList;
          vertex.nodeAttribute.item = item;
          vertex.nodeAttribute.style = "todo";
          this.vertexList.push(vertex);

        } else if (item.type == "resource" && item.behavior == "upload"){
            this.outputList.push({
                resId: item.resId,
                type: "output",
                vertexId: item.id,
            });
            let vertex = this.graph.insertVertex(
                this.graph.getDefaultParent(),
                item.id,
                item.name,
                200,
                200,
                150, //width
                40, //height
                "fillColor=#3CAEA3;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;"
            );

            //绑定相关信息
            vertex.nodeAttribute = {};
            vertex.nodeAttribute.resList = item.resList;
            vertex.nodeAttribute.item = item;
            vertex.nodeAttribute.style = "todo";
            this.vertexList.push(vertex);
        }
      });

      //利用input和output信息 生成边edge
      for (let i = 0; i < this.inputList.length; i++) {
        for (let j = 0; j < this.outputList.length; j++) {
          if (this.inputList[i].resId == this.outputList[j].resId) {
            let headVertex = this.getVertexById(this.outputList[j].vertexId);
            let tailVertex = this.getVertexById(this.inputList[i].vertexId);

            //判断是否为重复边
            let isNewEdge = true;
            for (let k = 0; k < this.edgeList.length; k++) {
              if (
                this.edgeList[k].headId == headVertex.id &&
                this.edgeList[k].tailId == tailVertex.id
              ) {
                isNewEdge = false;
              }
            }
            if (isNewEdge) {
              let edge = this.graph.insertEdge(
                this.graph.getDefaultParent(),
                null,
                "",
                headVertex,
                tailVertex
              );
              //绑定相关信息
              edge.edgeAttribute = {};
              edge.edgeAttribute.headId = headVertex.id;
              edge.edgeAttribute.tailId = tailVertex.id;
              // console.log(edge); //如果需要  可以将信息补充道edge中
              this.edgeList.push({
                headId: headVertex.id,
                tailId: tailVertex.id,
                resId: this.inputList[i].resId,
              });
            }
          }
        }
      }
    },
    getVertexById(vertexId) {
      for (let i = 0; i < this.vertexList.length; i++) {
        if (this.vertexList[i].id == vertexId) {
          return this.vertexList[i];
        }
      }
    },
    listenGraphEvent() {
      // 监听双击事件
      this.graph.addListener(mxEvent.DOUBLE_CLICK, async (graph, evt) => {
        // DOUBLE_CLICK
        if (evt.properties.cell != undefined) {
          let cell = evt.properties.cell;
          console.log(cell);
          console.log("DOUBLE_CLICK");
          this.selectVertex = this.getVertexById(cell.id);
          this.tempToolInfo = this.getVertexById(cell.id);

          //临时实验
          if(this.selectVertex.nodeAttribute.item.type == "geo-analysis"){
            let toolInfo = {};
            this.axios
              .get("/GeoProblemSolving/tool/" + this.selectVertex.nodeAttribute.item.toolInfo.tid)
              .then((res) => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  // this.$router.push({ name: "Login" });
                  this.tempLoginModal = true;
                } else if (res.data.code == 0) {
                  toolInfo = res.data.data;
                  this.$refs.ToolBox.useTool(toolInfo);
                } else {
                  console.log(res.data.msg);
                }
              })
              .catch((err) => {
                throw err;
              });
          }
          
          // this.vertexInfoShow = true;
    
          this.unfold = [];
          this.unfold.push("introduce");
        }
      });
      // 监听单击事件(只监听点，不监听线)
      this.graph.addListener(mxEvent.CLICK, async (graph, evt) => {
        if (evt.properties.cell != undefined && evt.properties.cell.vertex) {
          if(this.tempToolInfo.nodeAttribute){
            //离开上一个选中的节点
            this.mxgraphOperation("leave");
          }
          let cell = evt.properties.cell;
          this.tempToolInfo = this.getVertexById(cell.id);
          console.log(this.tempToolInfo);
          //进入该节点
          this.mxgraphOperation("enter");

          //临时实验
          if(this.tempToolInfo.nodeAttribute.item.type == "geo-analysis"){
            // this.tempToolInfo.tid = cell.style.toolInfo.tid;
            this.getToolInfo();
          }

          this.unfold = [];
          this.unfold.push("introduce");
        }
      });
    },
    mxgraphOperation(operation){
      //节点样式修改
      let cell = this.tempToolInfo;
      let style = "";
      if(operation == "enter"){
        style = "fillColor=#515a6e;overflow=hidden;shadow=true;strokeColor=white;fontStyle=2;fontColor=white;rounded=1;fontSize=16;"
        let cells = [];
        cells.push(cell);
        this.graph.setCellStyle(style,cells);
      } else if(operation == "leave" && cell != {} && cell != null && cell != undefined){
        if(cell.nodeAttribute.item.type == "resource" && !this.tempToolInfo.finished){
          style = "fillColor=#3CAEA3;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        } else if(cell.nodeAttribute.item.type == "resource" && this.tempToolInfo.finished){
          style = "fillColor=#c5c8ce;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;"
        } else if (cell.nodeAttribute.item.type == "geo-analysis" && !this.tempToolInfo.finished){
          style = "fillColor=#5cadff;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        } else if (cell.nodeAttribute.item.type == "geo-analysis" && this.tempToolInfo.finished){
          style = "fillColor=#c5c8ce;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        } 
        let cells = [];
        cells.push(cell);
        this.graph.setCellStyle(style,cells);
      } else if(operation == "finished"){
        style = "fillColor=#c5c8ce;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        let cells = [];
        cells.push(cell);
        this.graph.setCellStyle(style,cells);
      } else if(operation == "recover"){
        if(cell.nodeAttribute.item.type == "resource"){
          style = "fillColor=#3CAEA3;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        } else if (cell.nodeAttribute.item.type == "geo-analysis"){
          style = "fillColor=#5cadff;overflow=hidden;shadow=true;strokeColor=white;fontStyle=1;fontColor=white;rounded=1;fontSize=14;";
        } 
        let cells = [];
        cells.push(cell);
        this.graph.setCellStyle(style,cells);

      }  
    },
    resUpload(){
      //父子组件通信
      this.$refs.ResList.dataUploadModalShow();
      // this.tempToolInfo.finished = true;
      // this.mxgraphOperation("finished");
    },
    openTool(){
      //父子组件通信，在toolBox中打开工具
      this.$refs.ToolBox.useTool(this.selectToolInfo);

      // 更改operationNode状态
      this.tempToolInfo.nodeAttribute.style = "doing";
      this.unfold = [];
      this.unfold.push("introduce");

      //socket协同
      let sockMsg = {};
      sockMsg["type"] = "general";
      sockMsg["behavior"] = "operateNode";
      sockMsg["content"] = {
        nodeId: this.tempToolInfo.id,
        data: this.tempToolInfo.nodeAttribute,
        sender: this.userInfo.name,
        avatar: this.userInfo.avatar,
      };
      sockMsg["sender"] = this.userInfo.userId;
      socketApi.sendSock(this.taskSocketId, sockMsg, this.socketOnMessage);
    },
    operationNodeFinished(){
      this.tempToolInfo.finished = true;
      this.mxgraphOperation("finished");

      // 更改operationNode状态
      this.tempToolInfo.nodeAttribute.style = "done";
      this.unfold = [];
      this.unfold.push("introduce");

      //socket协同
      let sockMsg = {};
      sockMsg["type"] = "general";
      sockMsg["behavior"] = "operateNodeDone";
      sockMsg["content"] = {
        nodeId: this.tempToolInfo.id,
        data: this.tempToolInfo.nodeAttribute,
        sender: this.userInfo.name,
        avatar: this.userInfo.avatar,
      };
      sockMsg["sender"] = this.userInfo.userId;
      socketApi.sendSock(this.taskSocketId, sockMsg, this.socketOnMessage);
    },
    operationNodeRecover(){
      this.tempToolInfo.finished = false;
      this.mxgraphOperation("recover");

      // 更改operationNode状态
      this.tempToolInfo.nodeAttribute.style = "todo";
      this.unfold = [];
      this.unfold.push("introduce");

      //socket协同
      let sockMsg = {};
      sockMsg["type"] = "general";
      sockMsg["behavior"] = "operateNodeRecover";
      sockMsg["content"] = {
        nodeId: this.tempToolInfo.id,
        data: this.tempToolInfo.nodeAttribute,
        sender: this.userInfo.name,
        avatar: this.userInfo.avatar,
      };
      sockMsg["sender"] = this.userInfo.userId;
      socketApi.sendSock(this.taskSocketId, sockMsg, this.socketOnMessage);
    },
    getToolInfo() {
      let tid = this.tempToolInfo.nodeAttribute.item.toolInfo.tid;
      this.getToolInfoFinish = false;
      this.axios
        .get("/GeoProblemSolving/tool/" + tid)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.selectToolInfo = res.data.data;
            this.getToolInfoFinish = true;
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    heightChange() {
      let count = this.vertexList.length;
      return count * 100 + 100 + "px";
    },
    zoomOut() {
      this.graph.zoomOut();
    },
    zoomIn() {
      this.graph.zoomIn();
    },
    zoomActual() {
      this.graph.zoomActual();
      this.graph.center(true, true, 0.2, 0.3); //将画布放到容器中间
    },
    initZoomActual() {
    //   this.graph.zoomActual();
      this.graph.center(true, true, -0.3, -0.2); //将画布放到容器中间
    },
    getXml() {
      let encoder = new mxCodec();
      let graphXml = encoder.encode(this.graph.getModel());
      let xml = mxUtils.getPrettyXml(graphXml);
      return xml;
    },
    showGraphXml(){
      this.graphXml = this.getXml();
      this.showGrapgXml = true;
    },
    
    avatarUrl(url) {
      let avatarUrl = this.$store.state.UserServer + url;
      return avatarUrl;
    },
  },
  filters: {
    getLength(list) {
      return list.length;
    },
  },
  computed:{
    toolTags: function () {
      let domainTemp = "";
      if (this.selectToolInfo.tags != null && this.selectToolInfo.tags.length > 0 && this.selectToolInfo.tags[0].text == undefined) {
        for (let i = 0; i < this.selectToolInfo.tags.length; i++) {
          domainTemp += this.selectToolInfo.tags[i] + "  ";
        }
      }else if(this.selectToolInfo.tags != null && this.selectToolInfo.tags.length > 0 && this.selectToolInfo.tags[0].text != undefined){
        for (let i = 0; i < this.selectToolInfo.tags.length; i++) {
          domainTemp += this.selectToolInfo.tags[i].text + "  ";
        }
      }
      return domainTemp;
    },
  }
};
</script>

<style scoped>

#drawflow {
  width: 98%;
  height: calc(100vh - 225px);
  background-image: url("../../../projects/MxGraph/images/logogrey.png");
  background-repeat: no-repeat;
  position: absolute;
  
  background-position-x: 27%;
  background-position-y: 40%;
}

.mxGraphCard {
  margin-right: 5px;
  width: 72%;
  overflow-y: auto;
  border: 0;
  opacity: 0.89;
}

.modalCard {
  margin-right: 5px;
  width: 450px;
  overflow-y: auto;
  border: 0;
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
.memberName span {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.memberRole span {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
#graphContainer {
  width: 100%;
  height: calc(100vh - 245px);
  border: 3px solid rgb(194, 185, 185);
  background-image: url("../../../projects/MxGraph/images/grid.gif");
  margin: auto;
  overflow: hidden;
  opacity: 0.85;
}
button {
  width: 80px;
  height: 30px;
  background: rgb(122, 122, 121);
  color: white;
  font-size: 12px;
  outline: none;
  border: none;
  border-radius: 15px;
}
.panel >>> .ivu-collapse-content {
  padding: 0 5px;
}
</style>

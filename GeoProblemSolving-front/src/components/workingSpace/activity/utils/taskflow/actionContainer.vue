<template>
  <div>
    <div id="background1"></div>
    <div id="background2"></div>
    <div id="backmask"></div>
    <div id="drawflow"></div>
    <Button
      class="flow-btn"
      style="top: 100px"
      icon="ios-link"
      @click="linkTask"
      >Link tasks</Button
    >
    <Button
      class="flow-btn"
      style="top: 150px"
      icon="ios-link-outline"
      @click="unlinkTask"
      >Remove links</Button
    >
    <Button class="flow-btn" style="top: 200px" icon="md-add" @click="zoomin"
      >Zoom in</Button
    >
    <Button
      class="flow-btn"
      style="top: 250px"
      icon="md-remove"
      @click="zoomout"
      >Zoom out</Button
    >
    <Modal
      v-model="operationModal"
      title="Bind operation to tasks"
      @on-ok="operationBind"
      ok-text="Bind to task"
      cancel-text="Cancel"
    >
      <div>
        <Label>Bind the operation to the task: </Label>
        <Select v-model="selectedTask" style="width: 200px">
          <Option
            v-for="item in taskList"
            :value="item.taskId"
            :key="item.taskId"
            >{{ item.name }}</Option
          >
        </Select>
      </div>
      <Divider></Divider>
      <div>
        <Button type="warning" @click="operationRemove"
          >Remove this temporary operation</Button
        >
      </div>
    </Modal>
  </div>
</template>
<script>
// drowflow 内容,所有操作都需要响应到此组件中
import Vue from "vue";
import TaskNode from "./taskNode";
import OperationNode from "./operationNode";

export default {
  props: ["activityInfo"],
  components: {},
  data() {
    return {
      editor: null,
      operationFlow: {
        drawflow: {
          Home: {
            data: {},
          },
        },
      },
      taskList: [],
      relations: [],
      operations: [],
      // task link
      taskLinkBtn: false,
      taskUnlinkBtn: false,
      // temp operation
      operationModal: false,
      selectedTask: "",
      slctOperationState: false,
      slctNodeId: "",
      selcetedOid: "",
      selcetedOperation: {},
      // task states
      todo: 0,
      doing: 0,
      done: 0,
    };
  },
  computed: {
    activityTasks() {
      return this.$store.state.activityTaskChange;
    },
    tempOperations() {
      return this.$store.state.tempOperationChange;
    },
  },
  watch: {
    activityTasks(newVal, oldVal) {
      if (newVal.length > 0) {
        for (let i = 0; i < newVal.length; i++) {
          let data = newVal[i];

          if (data.behavior === "add") {
            this.generateTaskNode(data.task);
          } else if (data.behavior === "remove") {
            this.removeTaskNode(data.task);
          } else if (data.behavior === "update") {
            this.removeTaskNode(data.task);
            this.generateTaskNode(data.task);
          }

          this.taskListUpdate(data.behavior, data.task);
        }

        this.$store.commit("clearActivityTasks");
      }
    },
    tempOperations(newVal, oldVal) {
      if (newVal.length > 0) {
        for (let i = 0; i < newVal.length; i++) {
          let data = newVal[i];

          if (data.behavior === "add") {
            this.loadOperationNode(data.operation);
          } else if (data.behavior === "remove") {
            this.removeOperationNode(data.operation);
          } else if (data.behavior === "update") {
            this.removeOperationNode(data.operation);
            this.loadOperationNode(data.operation);
          }

          this.operationsUpdate(data.behavior, data.operation);
        }

        this.$store.commit("clearTempOperations");
      }
    },
  },
  mounted() {
    this.taskList = this.operationApi.getTaskList();
    this.relations = this.operationApi.getTaskDependencies();
    this.operations = this.operationApi.getTempOperations();

    const id = document.getElementById("drawflow");
    this.editor = new Drawflow(id, Vue);
    this.initActionFlow();
  },
  methods: {
    initActionFlow() {
      this.editor.drawflow = this.operationFlow;
      this.editor.start();

      this.generateAllTaskNodes();
      this.loadAllOperationNodes();
      this.addConnections();

      let _this = this;
      // save connection
      this.editor.on("connectionCreated", (connection) => {
        if (_this.taskLinkBtn) {
          let fromTaskId = _this.editor.getNodeFromId(
            connection.output_id
          ).name;
          let toTaskId = _this.editor.getNodeFromId(connection.input_id).name;

          _this.operationApi.taskDependencyRecord(
            _this.activityInfo.aid,
            "link",
            fromTaskId,
            toTaskId
          );
        }
      });
      // save connection remove
      this.editor.on("connectionRemoved", (connection) => {
        if (_this.taskUnlinkBtn) {
          if (confirm("Are you sure to remove this connection?")) {
            let fromTaskId = _this.editor.getNodeFromId(
              connection.output_id
            ).name;
            let toTaskId = _this.editor.getNodeFromId(connection.input_id).name;

            _this.operationApi.taskDependencyRecord(
              _this.activityInfo.aid,
              "break",
              fromTaskId,
              toTaskId
            );
          }
        }
      });
      // save connection remove
      this.editor.on("nodeSelected", (id) => {
        let oid = _this.editor.getNodeFromId(id).name;
        let result = _this.operationApi.getOperationInfo(oid);
        if (result != null) {
          _this.slctNodeId = id;
          _this.slctOperationState = true;
          _this.selcetedOid = oid;
          _this.selcetedOperation = result;
        }
      });
    },
    taskListUpdate(behavior, task) {
      if (behavior === "add") {
        this.taskList.push(task);
      }
      for (let i = 0; i < this.taskList.length; i++) {
        if (this.taskList[i].taskId == task.taskId) {
          if (behavior === "remove") {
            this.taskList.splice(i, 1);
            this.relationUpdate(task.taskId);
          } else if (behavior === "update") {
            this.taskList[i] = task;
          }
        }
      }
    },
    operationsUpdate(behavior, operation) {
      if (behavior === "add") {
        this.operations.push(operation);
      }
      for (let i = 0; i < this.operations.length; i++) {
        if (this.operations[i].id == operation.id) {
          if (behavior === "remove") {
            this.operations.splice(i, 1);
          } else if (behavior === "update") {
            this.operations[i] = operation;
          }
        }
      }
    },
    relationUpdate(taskId) {
      for (let i = 0; i < this.relations.length; i++) {
        if(this.relations[i].from == taskId || this.relations[i].to == taskId){
          this.relations.splice(i, 1);
        }
      }
    },
    generateAllTaskNodes() {
      (this.todo = 0), (this.doing = 0), (this.done = 0);
      for (var i = 0; i < this.taskList.length; i++) {
        let nodeId = this.taskList[i].taskId;
        let nodeName = this.taskList[i].name;
        let pos_x = 0;
        let pos_y = 0;

        // position
        let startTime = new Date(this.taskList[i].startTime);
        let endTime = new Date(this.taskList[i].endTime);
        let today = new Date();
        if (today > endTime && today > startTime) {
          pos_x = 50;
          pos_y = 110 + this.done * 130;
          this.done++;
        } else if (endTime > today && today > startTime) {
          pos_x = 450;
          pos_y = 110 + this.todo * 130;
          this.todo++;
        } else if (endTime > today && startTime > today) {
          pos_x = 850;
          pos_y = 110 + this.doing * 130;
          this.doing++;
        }
        // add node
        let props = {
          taskId: nodeId,
          name: nodeName,
          activityInfo: this.activityInfo,
          parent: this.$parent,
        };
        let data = {};
        this.editor.registerNode("TaskNode", TaskNode, props, {});
        this.editor.addNode(
          nodeId,
          0,
          0,
          pos_x,
          pos_y,
          "task-node",
          data,
          "TaskNode",
          "vue"
        );
      }
    },
    generateTaskNode(task) {
      let nodeId = task.taskId;
      let nodeName = task.name;
      let pos_x = 0;
      let pos_y = 0;

      // position
      let startTime = new Date(task.startTime);
      let endTime = new Date(task.endTime);
      let today = new Date();
      if (today > endTime && today > startTime) {
        pos_x = 50;
        pos_y = 110 + this.done * 130;
        this.done++;
      } else if (endTime > today && today > startTime) {
        pos_x = 450;
        pos_y = 110 + this.todo * 130;
        this.todo++;
      } else if (endTime > today && startTime > today) {
        pos_x = 850;
        pos_y = 110 + this.doing * 130;
        this.doing++;
      }
      // add node
      let props = {
        taskId: nodeId,
        name: nodeName,
        activityInfo: this.activityInfo,
        parent: this.$parent,
        
      };
      let data = {};
      this.editor.registerNode("TaskNode", TaskNode, props, {});
      this.editor.addNode(
        nodeId,
        0,
        0,
        pos_x,
        pos_y,
        "task-node",
        data,
        "TaskNode",
        "vue"
      );

      // this.taskList.push(task);
    },
    removeTaskNode(task) {
      let nodeId = this.editor.getNodesFromName(task.taskId)[0];
      if (nodeId != undefined) {
        this.editor.removeNodeId("node-" + nodeId);
      }
    },
    addConnections() {
      for (var i = 0; i < this.relations.length; i++) {
        // output
        let fromId = this.editor.getNodesFromName(this.relations[i].from)[0];
        let fromNode = this.editor.getNodeFromId(fromId);
        if (JSON.stringify(fromNode.outputs) === "{}") {
          this.editor.addNodeOutput(fromId);
        }

        //input
        let toId = this.editor.getNodesFromName(this.relations[i].to)[0];
        let toNode = this.editor.getNodeFromId(toId);
        if (JSON.stringify(toNode.inputs) === "{}") {
          this.editor.addNodeInput(toId);
        }

        //connection
        this.editor.addConnection(fromId, toId, "output_1", "input_1");
      }
    },
    addInputOutput() {
      for (var i = 0; i < this.taskList.length; i++) {
        let id = this.editor.getNodesFromName(this.taskList[i].taskId)[0];
        let node = this.editor.getNodeFromId(id);
        if (JSON.stringify(node.outputs) === "{}") {
          this.editor.addNodeOutput(id);
        }
        if (JSON.stringify(node.inputs) === "{}") {
          this.editor.addNodeInput(id);
        }
      }
    },
    loadAllOperationNodes() {
      for (var i = 0; i < this.operations.length; i++) {
        // position
        let pos_x = 50 + 220 * i;
        let pos_y = 15;

        // generate node
        let props = {
          taskId: "",
          name: "",
          operation: this.operations[i],
        };
        let data = {};
        this.editor.registerNode("OperationNode", OperationNode, props, {});
        this.editor.addNode(
          this.operations[i].id,
          0,
          0,
          pos_x,
          pos_y,
          "operation-node",
          data,
          "OperationNode",
          "vue"
        );
        let _this = this;
        $(".operation-node").mouseup(function () {
          if (_this.slctOperationState) {
            _this.operationModal = true;
            _this.slctOperationState = false;
          }
        });
      }
    },
    loadOperationNode(operation) {
      // position
      let pos_x = 50 + 220 * this.operations.length;
      let pos_y = 15;

      // generate node
      let props = {
        taskId: "",
        name: "",
        operation: operation,
      };
      let data = {};
      this.editor.registerNode("OperationNode", OperationNode, props, {});
      this.editor.addNode(
        operation.id,
        0,
        0,
        pos_x,
        pos_y,
        "operation-node",
        data,
        "OperationNode",
        "vue"
      );
      let _this = this;
      $(".operation-node").mouseup(function () {
        if (_this.slctOperationState) {
          _this.operationModal = true;
          _this.slctOperationState = false;
        }
      });

      // this.operations.push(operation);
    },
    removeOperationNode(operation) {
      let nodeId = this.editor.getNodesFromName(operation.id)[0];
      if (nodeId != undefined) {
        this.editor.removeNodeId("node-" + nodeId);
      }
    },
    removeInputOutput() {
      for (var i = 0; i < this.taskList.length; i++) {
        let id = this.editor.getNodesFromName(this.taskList[i].taskId)[0];
        let node = this.editor.getNodeFromId(id);
        if (node.outputs.output_1.connections.length === 0) {
          this.editor.removeNodeOutput(id, "output_1");
        }
        if (node.inputs.input_1.connections.length === 0) {
          this.editor.removeNodeInput(id, "input_1");
        }
      }
    },
    linkTask() {
      if (!this.taskLinkBtn) {
        this.addInputOutput();

        this.taskLinkBtn = true;
      } else {
        this.removeInputOutput();

        this.taskLinkBtn = false;
      }
    },
    unlinkTask() {
      if (!this.taskUnlinkBtn) {
        $(".drawflow .connection .main-path").css("stroke", "red");
        this.taskUnlinkBtn = true;
      } else {
        $(".drawflow .connection .main-path").css("stroke", "#4682b4");
        this.taskUnlinkBtn = false;
      }
    },
    zoomin() {
      this.editor.zoom_in();
    },
    zoomout() {
      this.editor.zoom_out();
    },
    operationBind() {
      this.operationModal = false;
      this.operationApi.bindTempOperation2Task(
        this.activityInfo.aid,
        this.selcetedOid,
        this.selectedTask
      );
      this.refreshTempOperations();
    },
    operationRemove() {
      this.operationModal = false;
      this.operationApi.deleteOperation(
        this.activityInfo.aid,
        this.selcetedOid
      );

      this.refreshTempOperations();
    },
    refreshTempOperations() {
      for (var i = 0; i < this.operations.length; i++) {
        this.removeOperationNode(this.operations[i]);
      }
      this.operations = this.operationApi.getTempOperations();
      this.loadAllOperationNodes();
    },
  },
};
</script>
<style scoped>
#background1 {
  width: 100%;
  height: 80px;
  position: absolute;
  background: lightgray;
}

#background2 {
  width: 100%;
  height: calc(100vh - 325px);
  background-image: url("../../../../../../static/Images/logogrey.png");
  background-repeat: no-repeat;
  position: absolute;
  top: 100px;
  background-position-x: 50%;
  background-position-y: 35%;
}

#backmask {
  width: 100%;
  height: calc(100vh - 225px);
  position: absolute;
  background-color: rgba(252, 251, 251, 0.8);
}

#drawflow {
  border-bottom: 1px solid lightgray;
  border-right: 1px solid lightgray;
  width: 100%;
  height: calc(100vh - 225px);

  background: var(--dfBackgroundColor);
  background-size: var(--dfBackgroundSize) var(--dfBackgroundSize);
  background-image: var(--dfBackgroundImage);
}
.flow-btn {
  position: absolute;
  right: 10px;
  background-color: #555555;
  color: white;
  z-index: 5;
  padding: 5px;
  width: 110px;
}
</style>
<style>
:root {
  --dfBackgroundColor: rgba(255, 255, 255, 1);
  --dfBackgroundSize: 20px;
  --dfBackgroundImage: linear-gradient(
      to right,
      rgb(210, 210, 210) 1px,
      transparent 1px
    ),
    linear-gradient(to bottom, rgb(210, 210, 210) 1px, transparent 1px);

  --background-box-title: #f7f7f7;

  --dfNodeType: flex;
  --dfNodeTypeFloat: none;
  --dfNodeBackgroundColor: #ffffff;
  --dfNodeTextColor: #000000;
  --dfNodeBorderSize: 1px;
  --dfNodeBorderColor: rgba(0, 0, 0, 0.74);
  --dfNodeBorderRadius: 3px;
  --dfNodeMinHeight: 30px;
  --dfNodeWidth: 250px;
  --dfNodePaddingTop: 10px;
  --dfNodePaddingBottom: 10px;
  --dfNodeBoxShadowHL: 0px;
  --dfNodeBoxShadowVL: 3px;
  --dfNodeBoxShadowBR: 8px;
  --dfNodeBoxShadowS: 1px;
  --dfNodeBoxShadowColor: rgba(0, 0, 0, 0.56);

  --dfNodeHoverBackgroundColor: #ffffff;
  --dfNodeHoverTextColor: rgba(0, 0, 0, 1);
  --dfNodeHoverBorderSize: 1px;
  --dfNodeHoverBorderColor: rgba(78, 169, 255, 1);
  --dfNodeHoverBorderRadius: 3px;

  --dfNodeHoverBoxShadowHL: 0px;
  --dfNodeHoverBoxShadowVL: 3px;
  --dfNodeHoverBoxShadowBR: 8px;
  --dfNodeHoverBoxShadowS: 1px;
  --dfNodeHoverBoxShadowColor: rgba(78, 169, 255, 0.66);

  --dfInputBackgroundColor: #ffffff;
  --dfInputBorderSize: 2px;
  --dfInputBorderColor: rgba(0, 0, 0, 1);
  --dfInputBorderRadius: 50px;
  --dfInputLeft: -10px;
  --dfInputHeight: 20px;
  --dfInputWidth: 20px;

  --dfInputHoverBackgroundColor: rgba(191, 214, 235, 1);
  --dfInputHoverBorderSize: 2px;
  --dfInputHoverBorderColor: rgba(78, 169, 255, 1);
  --dfInputHoverBorderRadius: 50px;

  --dfOutputBackgroundColor: #999999;
  --dfOutputBorderSize: 2px;
  --dfOutputBorderColor: #000000;
  --dfOutputBorderRadius: 50px;
  --dfOutputRight: 10px;
  --dfOutputHeight: 20px;
  --dfOutputWidth: 20px;

  --dfOutputHoverBackgroundColor: rgba(191, 214, 235, 1);
  --dfOutputHoverBorderSize: 2px;
  --dfOutputHoverBorderColor: rgba(78, 169, 255, 1);
  --dfOutputHoverBorderRadius: 50px;

  --dfLineWidth: 3px;
  --dfLineColor: #4682b4;
  --dfLineHoverColor: #4682b4;
  --dfLineSelectedColor: #43b993;

  --dfRerouteBorderWidth: 1px;
  --dfRerouteBorderColor: #000000;
  --dfRerouteBackgroundColor: #ffffff;

  --dfRerouteHoverBorderWidth: 2px;
  --dfRerouteHoverBorderColor: #000000;
  --dfRerouteHoverBackgroundColor: #ffffff;

  --dfDeleteDisplay: block;
  --dfDeleteColor: #ffffff;
  --dfDeleteBackgroundColor: rgba(0, 0, 0, 1);
  --dfDeleteBorderSize: 2px;
  --dfDeleteBorderColor: #ffffff;
  --dfDeleteBorderRadius: 50px;
  --dfDeleteTop: -20px;

  --dfDeleteHoverColor: #000000;
  --dfDeleteHoverBackgroundColor: #ffffff;
  --dfDeleteHoverBorderSize: 2px;
  --dfDeleteHoverBorderColor: #000000;
  --dfDeleteHoverBorderRadius: 50px;
}

.drawflow-node .drawflow-title-box {
  height: 50px;
  line-height: 50px;
  background: var(--background-box-title);
  border-bottom: 1px solid #e9e9e9;
  border-radius: 4px 4px 0px 0px;
  padding-left: 10px;
  cursor: pointer;
}

.drawflow-node .box {
  padding: 10px 20px 20px 20px;
  font-size: 14px;
  color: #555555;
}

.drawflow .drawflow-node {
  display: var(--dfNodeType);
  background: var(--dfNodeBackgroundColor);
  color: var(--dfNodeTextColor);
  border: var(--dfNodeBorderSize) solid var(--dfNodeBorderColor);
  border-radius: var(--dfNodeBorderRadius);
  min-height: var(--dfNodeMinHeight);
  width: var(--dfNodeWidth);
  /* padding-top: var(--dfNodePaddingTop);
  padding-bottom: var(--dfNodePaddingBottom); */
  -webkit-box-shadow: var(--dfNodeBoxShadowHL) var(--dfNodeBoxShadowVL)
    var(--dfNodeBoxShadowBR) var(--dfNodeBoxShadowS) var(--dfNodeBoxShadowColor);
  box-shadow: var(--dfNodeBoxShadowHL) var(--dfNodeBoxShadowVL)
    var(--dfNodeBoxShadowBR) var(--dfNodeBoxShadowS) var(--dfNodeBoxShadowColor);
  padding: 0px;
}

.operation-node {
  width: 200px !important;
}

.drawflow .drawflow-node:hover {
  background: var(--dfNodeHoverBackgroundColor);
  color: var(--dfNodeHoverTextColor);
  border: var(--dfNodeHoverBorderSize) solid var(--dfNodeHoverBorderColor);
  border-radius: var(--dfNodeHoverBorderRadius);
  -webkit-box-shadow: var(--dfNodeHoverBoxShadowHL)
    var(--dfNodeHoverBoxShadowVL) var(--dfNodeHoverBoxShadowBR)
    var(--dfNodeHoverBoxShadowS) var(--dfNodeHoverBoxShadowColor);
  box-shadow: var(--dfNodeHoverBoxShadowHL) var(--dfNodeHoverBoxShadowVL)
    var(--dfNodeHoverBoxShadowBR) var(--dfNodeHoverBoxShadowS)
    var(--dfNodeHoverBoxShadowColor);
}

.drawflow .drawflow-node.selected {
  background: var(--dfNodeHoverBackgroundColor);
  color: var(--dfNodeHoverTextColor);
  border: var(--dfNodeHoverBorderSize) solid var(--dfNodeHoverBorderColor);
  border-radius: var(--dfNodeHoverBorderRadius);
  -webkit-box-shadow: var(--dfNodeHoverBoxShadowHL)
    var(--dfNodeHoverBoxShadowVL) var(--dfNodeHoverBoxShadowBR)
    var(--dfNodeHoverBoxShadowS) var(--dfNodeHoverBoxShadowColor);
  box-shadow: var(--dfNodeHoverBoxShadowHL) var(--dfNodeHoverBoxShadowVL)
    var(--dfNodeHoverBoxShadowBR) var(--dfNodeHoverBoxShadowS)
    var(--dfNodeHoverBoxShadowColor);
  z-index: 10;
}

.drawflow .drawflow-node .input {
  left: var(--dfInputLeft);
  background: var(--dfInputBackgroundColor);
  border: var(--dfInputBorderSize) solid var(--dfInputBorderColor);
  border-radius: var(--dfInputBorderRadius);
  height: var(--dfInputHeight);
  width: var(--dfInputWidth);
}

.drawflow .drawflow-node .input:hover {
  background: var(--dfInputHoverBackgroundColor);
  border: var(--dfInputHoverBorderSize) solid var(--dfInputHoverBorderColor);
  border-radius: var(--dfInputHoverBorderRadius);
}

.drawflow .drawflow-node .outputs {
  float: var(--dfNodeTypeFloat);
}

.drawflow .drawflow-node .output {
  right: var(--dfOutputRight);
  background: var(--dfOutputBackgroundColor);
  border: var(--dfOutputBorderSize) solid var(--dfOutputBorderColor);
  border-radius: var(--dfOutputBorderRadius);
  height: var(--dfOutputHeight);
  width: var(--dfOutputWidth);
}

.drawflow .drawflow-node .output:hover {
  background: var(--dfOutputHoverBackgroundColor);
  border: var(--dfOutputHoverBorderSize) solid var(--dfOutputHoverBorderColor);
  border-radius: var(--dfOutputHoverBorderRadius);
}

.drawflow .connection .main-path {
  stroke-width: var(--dfLineWidth);
  stroke: var(--dfLineColor);
}

.drawflow .connection .main-path:hover {
  stroke: var(--dfLineHoverColor);
}

.drawflow .connection .main-path.selected {
  stroke: var(--dfLineSelectedColor);
}

.drawflow .connection .point {
  stroke: var(--dfRerouteBorderColor);
  stroke-width: var(--dfRerouteBorderWidth);
  fill: var(--dfRerouteBackgroundColor);
}

.drawflow .connection .point:hover {
  stroke: var(--dfRerouteHoverBorderColor);
  stroke-width: var(--dfRerouteHoverBorderWidth);
  fill: var(--dfRerouteHoverBackgroundColor);
}

.drawflow-delete {
  display: var(--dfDeleteDisplay);
  color: var(--dfDeleteColor);
  background: var(--dfDeleteBackgroundColor);
  border: var(--dfDeleteBorderSize) solid var(--dfDeleteBorderColor);
  border-radius: var(--dfDeleteBorderRadius);
}

.parent-node .drawflow-delete {
  top: var(--dfDeleteTop);
}

.drawflow-delete:hover {
  color: var(--dfDeleteHoverColor);
  background: var(--dfDeleteHoverBackgroundColor);
  border: var(--dfDeleteHoverBorderSize) solid var(--dfDeleteHoverBorderColor);
  border-radius: var(--dfDeleteHoverBorderRadius);
}
</style>

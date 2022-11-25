<template>
  <div class="card-devices">
    <div
      class="content"
      v-if="temOperation != undefined && temOperation.type === 'communication'"
    >
      <span class="behavior">Communication</span>
      <div class="participant">
        <span style="margin-right: 5px">People: </span>
        <avatar
          v-for="person in temOperation.participants"
          :key="person.id"
          class="person"
          :username="person.name"
          :size="16"
          :rounded="true"
          :title="person.name"
        />
      </div>
    </div>
    <div
      class="content"
      v-else-if="
        temOperation != undefined && temOperation.type === 'geo-analysis'
      "
    >
      <span class="behavior"
        >Geo-analysis via the {{ temOperation.tool.name }}</span
      >
      <div class="participant">
        <span style="margin-right: 5px">People: </span>
        <avatar
          v-for="person in temOperation.participants"
          :key="person.id"
          class="person"
          :username="person.name"
          :size="16"
          :rounded="true"
          :title="person.name"
        />
      </div>
    </div>
    <div
      class="content"
      v-else-if="temOperation != undefined && temOperation.type === 'resource'"
    >
      <img
        :src="dataUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-if="temOperation.resource.type == 'data'"
      />
      <img
        :src="modelUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'model'"
      />
      <img
        :src="documentUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'document'"
      />
      <img
        :src="paperUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'paper'"
      />
      <img
        :src="imageUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'iamge'"
      />
      <img
        :src="videoUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'video'"
      />
      <img
        :src="otherUrl"
        height="24px"
        width="24px"
        :title="temOperation.resource.name"
        v-else-if="temOperation.resource.type == 'others'"
      />
      <span class="behavior">Resource {{ temOperation.behavior }}</span>
    </div>
    <div class="content" v-else-if="temOperation != undefined && temOperation.type === 'tool'">
      <img
        :src="toolUrl"
        height="24px"
        width="24px"
        :title="temOperation.tool.name"
      />
      <span class="behavior">Tool {{ temOperation.behavior }}</span>
    </div>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
export default {
  components: {
    Avatar,
  },
  props: ["taskId", "name", "operation"],
  data() {
    return {
      scrollOps: {
        bar: {
          background: "lightgrey",
        },
      },
      unfold: false,
      operationRecords: [],
      temOperation: {},
      // 资源avatar 图片路径
      dataUrl: require("@/assets/images/data.png"),
      modelUrl: require("@/assets/images/model.png"),
      paperUrl: require("@/assets/images/paper.png"),
      documentUrl: require("@/assets/images/document.png"),
      imageUrl: require("@/assets/images/image.png"),
      videoUrl: require("@/assets/images/video.png"),
      otherUrl: require("@/assets/images/otherfile.png"),
      // 工具avatar 图片路径
      toolUrl: require("@/assets/images/toolbox.png"),
      chatUrl: require("@/assets/images/chat.png"),
    };
  },
  mounted() {
    this.operationInit();
  },
  methods: {
    operationInit() {
      let operation = this.operationApi.getOperationInfo(this.operation.id);
      if (operation.type === "communication") {
        // participants
        let participants = [];
        for (var j = 0; j < operation.personRef.length; j++) {
          let personInfo = this.operationApi.getMemberInfo(
            operation.personRef[j]
          );
          if (personInfo == null) return;
          participants.push(personInfo);
        }
        operation.participants = participants;
      } else if (operation.type === "geo-analysis") {
        // geo-analysis
        let toolInfo = this.operationApi.getToolInfo(operation.toolRef);
        if (toolInfo == null) return;
        operation.tool = toolInfo;

        // participants
        let participants = [];
        for (var j = 0; j < operation.personRef.length; j++) {
          let operatorInfo = this.operationApi.getMemberInfo(
            operation.personRef[j]
          );
          if (operatorInfo == null) return;
          participants.push(operatorInfo);
        }
        operation.participants = participants;
      } else if (operation.type === "resource") {
        // resource
        let resInfo = this.operationApi.getResInfo(operation.resRef);
        if (resInfo == null) return;

        operation.resource = resInfo;
        operation.operator = this.operationApi.getMemberInfo(
          operation.operator
        );
      } else if (operation.type === "tool") {
        // tool
        let toolInfo = this.operationApi.getToolInfo(operation.toolRef);
        operation.tool = toolInfo;
        let operatorInfo = this.operationApi.getMemberInfo(operation.operator);
        operation.operator = operatorInfo;
      }
      this.temOperation = operation;
    },
  },
};
</script>
<style scoped>
.time {
  font-size: 14px;
  font-weight: bold;
}
.content {
  width: 200px;
  word-break: break-word;
  padding: 5px;
  font-size: 12px;
}
.behavior {
  vertical-align: super;
}
.tool {
  cursor: pointer;
}
.resource {
  cursor: pointer;
}
.participant {
  display: flex;
  position: relative;
}
.participant .person {
  margin-right: 5px;
  cursor: pointer;
}
.ivu-divider-horizontal {
  margin: 10px 0 0 0;
}
.drawflow-node-title {
  height: 50px;
  line-height: 50px;
  background: #f7f7f7;
  border-bottom: 1px solid #e9e9e9;
  border-radius: 4px 4px 0px 0px;
  padding-left: 15px;
}
.drawflow-node-body {
  font-size: 14px;
  color: #555555;
}
</style>

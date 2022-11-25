<template>
  <div>
    <div
      style="
        width: 100%;
        height: calc(100vh - 225px);
        float: left
      "
    >
      <action-container
        slot="content"
        :activityInfo="activityInfo"
      ></action-container>
    </div>
    <div style="position: absolute; top: 5px; left: 10px; z-index: 10">
      <info-btn :activityInfo="activityInfo" :projectInfo="projectInfo"></info-btn>
    </div>
    <div style="width: 430px; border: 1px solid lightgray; position: absolute; top: 5px; right: 10px; z-index: 10">
      <Collapse simple v-model="unfold" class="panel">
          <Panel name="tool">
            Toolbox
            <tool-box slot="content" :activityInfo="activityInfo" :projectInfo="projectInfo" ref="ToolBox"></tool-box>
          </Panel>
          <Panel name="data">
            Resource list
            <res-list slot="content" :activityInfo="activityInfo"></res-list>
          </Panel>
      </Collapse>
    </div>
  </div>
</template>
<script>
import resList from "../utils/resList";
import toolBox from "../utils/toolBox";
import actionContainer from "../utils/taskflow/actionContainer";
import infoBtn from "../utils/infoBtn";
export default {
  props: ["activityInfo","participants","projectInfo"],
  components: {
    resList,
    toolBox,
    actionContainer,
    infoBtn
  },
  data() {
    return {
      unfold: [],
      ops: {
        bar: {
          background: "#808695",
        },
      },
    };
  },
  mounted() {},
  methods: {
    changeMenuItem(name) {
      this.activeMenu = name;
    },
  },
};
</script>
<style scoped>
.tab >>> .ivu-tabs-bar {
  margin-bottom: 0px;
}
.activityTab >>> .ivu-tabs-bar {
  margin-bottom: 8px;
}
.panel >>> .ivu-collapse-content {
  padding: 0 5px;
}
</style>

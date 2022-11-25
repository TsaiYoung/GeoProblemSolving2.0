<!--  -->
<template>
  <div>
    <el-row>
      <div class="leftContainer">
        <div class="tool_top">
          <div class="tool_title">
            <el-row v-show="switchValue">Public Tools</el-row>
            <el-row v-show="!switchValue">Private Tools</el-row>
          </div>
          <el-switch
            v-model="switchValue"
            active-color="#13ce66"
            inactive-color="#ff4949"
          ></el-switch>
        </div>
        <el-card shadow="never" v-show="switchValue" class="card_contain">
          <vue-scroll :ops="ops" style="height: 480px">
            <el-row :gutter="10">
              <el-col
                :span="6"
                v-for="(tool, index) in filterPublicTools"
                :key="index"
              >
                <div class="choose_tool_contain" @click="getSelectTools(tool)">
                  <tool-card :toolFrom="tool"></tool-card>
                </div>
              </el-col>
            </el-row>
          </vue-scroll>
        </el-card>
        <el-card shadow="never" v-show="!switchValue" class="card_contain">
          <vue-scroll :ops="ops" style="height: 480px">
            <el-row :gutter="10">
              <el-col
                :span="6"
                v-for="(tool, index) in filterPersonalTools"
                :key="index"
              >
                <div class="choose_tool_contain" @click="getSelectTools(tool)">
                  <tool-card :toolFrom="tool"></tool-card>
                </div>
              </el-col>
            </el-row>
          </vue-scroll>
        </el-card>
      </div>
      <div class="rightContainer">
        <div class="tool_top">
          <el-button @click="submitSendTools" size="mini">Send</el-button>
        </div>

        <el-card shadow="never" class="card_contain">
          <div class="container_back">
            Tools that you <br />have sent to <br />the chatroom
          </div>
          <vue-scroll :ops="ops" style="height: 480px">
            <div v-for="(tool, index) in sentTools" :key="tool.index">
              <div>
                <el-card class="select_tools_contain">
                  <div class="ellipsis" style="width: 150px">
                    {{ tool.toolName }}
                  </div>
                  <i
                    class="el-icon-remove changeRedColor"
                    size="small"
                    @click="removeSelectedTools(index)"
                  ></i>
                </el-card>
              </div>
            </div>
          </vue-scroll>
        </el-card>
      </div>
    </el-row>
  </div>
</template>

<script>
import toolCard from "@/components/common/card/simplePublicToolsCard";
import { get, del, post, put } from "@/axios";

export default {
  components: { toolCard },
  data() {
    return {
      projectId: this.$route.params.projectId,
      publicTools: [],
      personalTools: [],
      userId: this.$store.getters.userInfo.userId,
      sentTools: [], //需要发送的工具
      ops: {
        bar: {
          background: "#808695"
        }
      },
      typeSelected: "All",
      typeOptions: [
        "All",
        "Context definition & resource collection",
        "Data processing",
        "Data visualization",
        "Geographic model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Quantitative and qualitative analysis",
        "Decision-making for management"
      ],
      switchValue: true
      // selectTools: []

      // initToolItems: [],
    };
  },

  computed: {
    filterPublicTools() {
      let tools = this.publicTools;
      let type = this.typeSelected;
      this.$set(this, "publicTools", tools);

      if (type == "All") {
        return tools;
      } else {
        return tools.filter(tool => {
          return tool.recomStep.includes(type);
        });
      }
    },
    filterPersonalTools() {
      let tools = this.personalTools;
      let type = this.typeSelected;

      if (type == "All") {
        return tools;
      } else {
        return tools.filter(tool => {
          return tool.recomStep.includes(type);
        });
      }
    }
  },

  mounted() {
    this.init();
  },

  methods: {
    async init() {
      await this.getPublicTools();
      await this.getPersonalTools();
    },

    async getPublicTools() {
      let data = await get(
        "/GeoProblemSolving/tool/privacy/Public"
      );
      this.$set(this, "publicTools", data);
    },

    async getPersonalTools() {
      let data = await get(
        `/GeoProblemSolving/tool/provider/${this.userId}`
      );
      this.$set(this, "personalTools", data);
    },

    getSelectTools(tool) {
      let initTools = this.sentTools;
      if (
        initTools != undefined &&
        initTools.some(item => item.tid == tool.tid)
      ) {
        this.$notify.error({
          title: "Error",
          message: "You have choose this tool"
        });
      } else {
        this.sentTools.push(tool);
      }
    },

    //add需要发送的tools

    removeSelectedTools(index) {
      this.sentTools.splice(index, 1);
    },

    submitSendTools() {
      this.$emit("selectedTools", this.sentTools);
    }
  }
};
</script>
<style lang="scss" scoped>
.ellipsis {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: top;
}
.leftContainer {
  float: left;
  width: 500px;
  // margin: 0 5px;
}
.rightContainer {
  float: left;
  width: 250px;
  margin-left: 5px;
}
.tool_top {
  padding: 5px 0;
  font-size: 16px;
  font-weight: 600;
  height: 40px;
  .tool_title {
    float: left;
    width: 100px;
  }
}
.tool_select {
  margin: 5px 0;
  font-size: 16px;
  font-weight: 600;
}
.changeRedColor {
  font-size: 18px;
  color: red;
  float: right;
}
.changeRedColor:hover {
  cursor: pointer;
}
.card_contain_right {
  height: 500px;
  width: 100%;
  clear: both;
}
.card_contain {
  height: 500px;
  width: 100%;
  clear: both;
  >>> .el-card__body {
    padding: 15px 10px;
  }
  >>> .el-card.is-always-shadow,
  .el-card.is-hover-shadow:focus,
  .el-card.is-hover-shadow:hover {
    box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.1);
  }
  .container_back {
    position: absolute;
    font-weight: 600;
    font-size: 35px;
    color: rgba(153, 153, 153, 0.315);
    user-select: none;
    text-align: center;
    // white-space: normal;
    top: 160px;
    line-height: 55px;
    width: 218px;
  }
}
.choose_tool_contain {
  margin-bottom: 10px;
}
.select_tools_contain {
  width: 210px;
  height: 50px;
  box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.1);
  margin: 0 auto;
  margin-bottom: 10px;
  >>> .el-card__body {
    padding: 15px;
  }
}
</style>

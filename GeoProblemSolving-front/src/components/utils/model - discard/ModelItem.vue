<template>
  <div class="main">
    <el-row class="title">
      <el-col>
        <i class="fa fa-cogs"></i>
        {{modelInstance.name}}
      </el-col>
    </el-row>

    <el-row class="des">
      <el-col>
        <p>{{modelInstance.statusEnum}}</p>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" tab-position="left">
      <el-tab-pane
        v-for="(state,index) in modelItem.stateList"
        :label="state.name"
        :name="state.name"
        :key="index"
      >
        <div class="state-desc">{{state.des}}</div>

        <div class="params-group">
          <el-row class="title">Input</el-row>
          <div class="items">
            <el-row
              v-for="(inEvent,inEventIndex) in inEventList(state)"
              :key="inEventIndex"
              class="item"
            >
              <el-col :span="16">{{inEvent.des}}</el-col>

              <el-col :span="3" :offset="2">
                <el-tooltip :content="inEvent.dataTemplate.tooltip" placement="top" effect="light">
                  <component
                    :disabled="true"
                    :is="inEvent.dataTemplate.type"
                    :initDataTemplate="inEvent.dataTemplate"
                  ></component>
                </el-tooltip>
              </el-col>
            </el-row>
          </div>
        </div>

        <div class="params-group" v-if="outEventList(state).length">
          <el-row class="title">Output</el-row>
          <div class="items">
            <el-row
              v-for="(outEvent,outEventIndex) in outEventList(state)"
              :key="outEventIndex"
              class="item"
            >
              <el-col :span="16">{{outEvent.des}}</el-col>

              <el-col :span="3" :offset="2">
                <div>
                  <el-button
                    plain
                    type="warning"
                    icon="el-icon-download"
                    v-show="outEvent.dataTemplate.value!=''"
                    @click="download(outEvent.dataTemplate.value)"
                  ></el-button>

                  <el-button
                    plain
                    type="success"
                    icon="el-icon-s-marketing"
                    v-show="outEvent.dataTemplate.value!=''"
                    @click="goVisualization(outEvent.dataTemplate.value)"
                  ></el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-tab-pane>

      <el-col :span="2" :offset="18" class="invoke-btn">
        <el-button plain type="primary" @click="invoke">
          <i class="fa fa-save"></i>&nbsp;invoke
        </el-button>
      </el-col>
    </el-tabs>
  </div>
</template>

<script>
// import file from "./../dataTemplate/File";
// import parameter_input from "./../dataTemplate/ParameterInput";
// import parameter_select from "./../dataTemplate/ParameterSelect";
// import parameter_slider from "./../dataTemplate/ParameterSlider";
// import parameter_table from "./../dataTemplate/ParameterTable";
// import parameter_range from "./../dataTemplate/ParameterRange";

export default {
  name: "invoke",
  components: {
    file,
    parameter_input,
    parameter_select,
    parameter_slider,
    parameter_table,
    parameter_range
  },
  data() {
    return {
      id: this.$route.params.id,
      modelInstance: {
        modelitem: {
          stateList: []
        }
      }
    };
  },
  computed: {
    activeTab: {
      get() {
        return this.modelItem.stateList
          ? this.modelItem.stateList[0].name
          : "defaultState";
      },
      set() {}
    },
    modelItem() {
      return this.modelInstance.modelItem;
    }
  },
  methods: {
    goVisualization() {},
    download(value) {
      let url = `/GeoProblemSolving/dataitem/download/${value}`;
      window.open(url);
    },
    inEventList(state) {
      return state.eventList.filter(value => {
        return value.ioFlagEnum === "INPUT";
      });
    },
    outEventList(state) {
      return state.eventList.filter(value => {
        return value.ioFlagEnum === "OUTPUT";
      });
    },
    async invoke() {
      let res = await post(
        "/GeoProblemSolving/modelitem/modelInstance/{id}/invoke",
        this.modelInstance,
        {
          id: this.id
        }
      );
      this.modelInstance = res;
    },
    async getData() {
      let modelInstance = await get("/mc/modelinstance/{id}", null, {
        id: this.id
      });
      this.modelInstance = modelInstance;
    }
  },
  created() {
    this.getData();

  }
};
</script>

<style>
.main {
  position: relative;
}
.title {
  position: relative;
  font-size: 18px;
  padding: 0px 0px 30px 0px;
}
.title i {
  font-size: 30px !important;
}

.el-button {
  padding: 12px;
}
.params-group {
  position: relative;
  padding-bottom: 30px;
}
.params-group > .title {
  font-style: italic;
  font-size: 16px;
  padding-bottom: 10px;
  border-bottom: solid 2px #999;
}
.params-group > .items {
  padding: 10px 0px 6px 50px;
}
.params-group > .items > .item {
  padding: 20px 10px 20px 0px;
  border-bottom: dotted 1px #999999;
  line-height: 2;
}
.invoke-btn {
  margin-top: 20px;
}
.state-desc {
  margin: 0px 0px 35px 0px;
  padding: 4px 0px;
  line-height: 2;
  background-color: #f3f3f3;
  font-size: 16px;
  font-style: italic;
}
</style>

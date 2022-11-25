<!--  -->
<template>
  <div>
    <el-row style="margin: 5px 0; line-height: 40px">
      <div class="switch-label">
        <div v-if="switchValue">Private data</div>
        <div v-else>Public data</div>
      </div>
      <div class="switch-label">
        <el-switch
          v-model="switchValue"
          active-color="#13ce66"
          inactive-color="#409eff"
        >
        </el-switch>
      </div>
      <div style="float: right">
        <!-- <el-button @click="uploadNewData">Upload new data</el-button> -->
        <el-upload
          action
          :auto-upload="true"
          :show-file-list="false"
          ref="upload"
          :http-request="uploadNewData"
        >
          <el-button type="primary" size="mini" style="font-size: 14px"
            >Upload {{ switchValue ? "private" : "public" }} data</el-button
          >
        </el-upload>
      </div>
    </el-row>

    <el-table
      border
      style="width: 100%"
      max-height="400"
      highlight-current-row
      :data="switchValue ? privateData : publicData"
      @row-click="handleCurrentChange"
      @row-dblclick="handleCurrentChangeSubmit"
    >
      <el-table-column
        prop="name"
        label="Name"
        sortable
        width="180"
        :show-overflow-tooltip="true"
      >
      </el-table-column>
      <el-table-column
        prop="suffix"
        label="Suffix"
        sortable
        width="100"
        :show-overflow-tooltip="true"
      >
      </el-table-column>
      <el-table-column
        prop="description"
        label="Description"
        width="209"
        :show-overflow-tooltip="true"
      >
      </el-table-column
      ><el-table-column prop="type" label="Type" width="80"> </el-table-column>
      <!-- <el-table-column prop="privacy" label="Privacy" width="80">
      </el-table-column> -->
      <el-table-column prop="origin" label="Origin" width="120">
      </el-table-column>
      <el-table-column prop="uploaderName" label="Provider" width="120">
      </el-table-column>
      <el-table-column prop="uploadTime" label="Upload time" width="150">
      </el-table-column>
    </el-table>
    <div v-if="Object.keys(selectData).length === 0">
      <div class="data-name select-title" style="width: 300px">
        You haven't selected any data!
      </div>
    </div>
    <div v-else style="line-height: 40px">
      <div class="data-name select-title">Data you have selected:</div>
      <div class="select-data select-data-line">
        <div class="data-name">{{ selectData.name }}</div>
        <i class="el-icon-close" @click="remove(selectData)"></i>
      </div>
      <div style="float: right" class="select-data-line">
        <el-button @click="submit" size="small" type="warning" plain
          >OK</el-button
        >
      </div>
    </div>
    <div style="clear: both"></div>
  </div>
</template>

<script>
import { get, del, post, put, patch } from "@/axios";
export default {
  components: {},

  computed: {},

  data() {
    return {
      switchValue: true,
      privateData: [],
      publicData: [],
      selectData: {},
      // tableData: []
      fileList: [], //el-upload上传的文件列表,
      file: {},
    };
  },

  methods: {
    async init() {
      await this.getUserResources();
      await this.getProjectResources();
    },
    async getUserResources() {
      //个人空间中资源
      let dataInUserSpace = await get(`/GeoProblemSolving/res/file/all`);
      let userInfo = JSON.parse(sessionStorage.userInfo);
      for (let i = 0; i < dataInUserSpace.length; i++) {
        dataInUserSpace[i].uploaderName = userInfo.name;
        dataInUserSpace[i].origin = "Personal Space";
        if (dataInUserSpace[i].privacy == "private") {
          this.privateData.push(dataInUserSpace[i]);
        } else {
          this.publicData.push(dataInUserSpace[i]);
        }
      }
    },
    async getProjectResources() {
      //项目中资源
      let dataInProject = await get(
        `/GeoProblemSolving/rip/file/all/` + activityInfo.aid
      );
      for (let i = 0; i < dataInProject.length; i++) {
        dataInProject[i].origin = "Project";
        if (dataInProject[i].privacy == "private") {
          this.privateData.push(dataInProject[i]);
        } else {
          this.publicData.push(dataInProject[i]);
        }
      }
    },
    handleCurrentChange(val) {
      this.selectData = val;
    },
    handleCurrentChangeSubmit(val) {
      this.selectData = val;
      this.submit();
    },
    remove() {
      this.selectData = {};
    },

    async uploadNewData({ file }) {
      let formData = new FormData();
      formData.append("file", file);
      formData.append("type", "data");
      //默认将上传的资源到根目录下
      formData.append("paths", "0");

      let flag = this.switchValue ? "private" : "public";
      // console.log(flag);

      formData.append("privacy", flag);
      formData.append("description", "");

      let { data } = await this.axios.post(
        `/GeoProblemSolving/res/upload`,
        formData
      );
      this.privateData = [];
      this.publicData = [];
      this.getUserResources();
      this.getProjectResources();
    },

    submit() {
      this.$emit("selectData", this.selectData);
    },
  },

  mounted() {
    this.init();
  },
};
</script>
<style lang="scss" scoped>
.switch-label {
  width: 100px;
  float: left;
  font-weight: 600;
  font-size: 16px;
}
.data-name {
  float: left;
  width: 200px;
  font-weight: 600;
  font-size: 16px;
  // padding-left: 5px;
}
.select-data-line {
  margin: 15px 0 0 0;
  line-height: 30px;
  .el-button {
    width: 138px;
  }
}
.select-data {
  background-color: #f0f9eb;
  display: inline-block;
  height: 32px;
  padding: 0 10px;

  font-size: 12px;
  color: #67c23a;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;

  //   background-color: #f0f9eb;
  // border-color: #e1f3d8;
  // color: #67c23a;
}
.select-data:hover {
  // width: 250px;
  cursor: pointer;
  transition: all 0.2s ease-out;
  color: #60a83c;
  background-color: #c8f3d3;
}
.select-title {
  width: 200px;
  float: left;
  line-height: 30px;
  margin: 18px 0 0 0;
}
</style>

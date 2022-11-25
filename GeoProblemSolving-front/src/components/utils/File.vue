<template>
  <div>
    <!-- <el-button v-show="disabled" plain type="warning" icon="el-icon-download" @click="download()"></el-button> -->
    <!-- <el-button @click="test()"></el-button> -->
    <el-col :span="12">
      <el-upload
        action
        :auto-upload="false"
        :file-list="fileList"
        :on-change="onChange"
        ref="upload"
        :before-upload="onBeforeUpload"
        :http-request="submitUpload"
      >
        <el-button round size="small" type="primary" :disabled="disabled">Choose Files</el-button>
      </el-upload>
    </el-col>
    <el-col :span="12">
      <el-button round size="small" @click="submitUpload" :disabled="disabled">Upload Files</el-button>
    </el-col>
  </div>
</template>
<script>
import X2JS from "x2js"; //xml数据处理插件
export default {
  props: {
    disabled: {
      type: Boolean,
      default: true
    },

    fileIndex: {
      type: Object
    },

    initEventUrl: {
      type: Object //Object&&数组可双向绑定
    },

    initStateList: {
      //传入的没有erl的stateList
      type: Array
    },
    stateIndex: {
      type: Object
    },
    datasetItem: {
      type: Array
    }
  },
  data() {
    return {
      eventUrl: this.initEventUrl,
      response: {},
      length: 0, //文件数量
      uploadFileForm: new FormData(), //上传文件的form
      fileList: [], //el-upload上传的文件列表,
      stateList: this.initStateList,
      stateEventIndex: {
        stateIndex: this.fileIndex.stateIndex,
        eventIndex: this.fileIndex.eventIndex
      },
      datasetItems: this.datasetItem,
      configFile: null
    };
  },

  methods: {
    onSuccess(data) {
      console.log(data);
      this.eventUrl.value = data.id;
      this.dis = true;
    },
    download() {
      let url = `/GeoProblemSolving/dataItem/download/${this.eventUrl.value}`;
      window.open(url);
    },

    //上传文件发生改变时
    onChange(file, fileList) {
      this.fileList = fileList;
    },

    //创建配置文件
    createAndUploadParamFile() {
      let file = this.fileList[0];
      let fileEvent = this.stateList[this.stateEventIndex.stateIndex].Event[
        this.stateEventIndex.eventIndex
      ];
      let datasets = this.datasetItems;
      let itemData = datasets.filter(item => {
        return item.name == fileEvent.ResponseParameter[0].datasetReference;
      });
      let data = itemData[0];
      // this.createXmlFromJosn(data.UdxDeclaration);

      let configContent = "<UDXZip><Name>";
      configContent += "<add value='" + file.name + "' />";
      configContent += "</Name>";
      // let data = event.data[0];
      configContent += "<DataTemplate type='none'>";
      configContent += "</DataTemplate>";
      configContent += "</UDXZip>";
      let configFile = new File([configContent], "config.udxcfg", {
        type: "text/plain"
      });
      this.configFile = configFile;
      this.uploadFileForm.append("files", configFile);
    },

    onBeforeUpload(){
      this.createAndUploadParamFile();
    },

    //上传文件到服务器
    async submitUpload(data) {
      this.uploadFileForm.append("files", this.fileList[0].raw);
      if (this.uploadFileForm.getAll("files").length === 0) {
        this.$message({
          message: "请先选择文件",
          type: "warning"
        });
      } else {
        // this.fileData.append('pathId', fieldData.id);  // 添加机构id
        // this.fileData.append('loginToken', this.loginToken);  // 添加token
        let data = await this.axios.post(
          `/GeoProblemSolving/task/uploadFileForm`,
          this.uploadFileForm
        );
        this.$message({
          message: "Upload Successfully!",
          type: "success"
        });
        let stateIndex = this.stateEventIndex.stateIndex;
        let eventIndex = this.stateEventIndex.eventIndex;
        let resultId = `http://111.229.14.128:8899/data?uid=${data.data.data}`;
        this.$set(
          this.stateList[stateIndex].Event[eventIndex],
          "url",
          resultId
        );
        this.$emit("newStateList", this.stateList);
      }
    }
  },

  watch: {}
};
</script>

<style></style>

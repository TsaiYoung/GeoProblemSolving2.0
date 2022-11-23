<style scoped>
.modelList {
  height: 105px;
  width: 290px;
  margin-bottom: 5px;
  /* border: 1px solid #cfcfcfb6;
  border-radius: 5px; */
  /* padding: 6%; */
}
/* .modelList:hover {
  box-shadow: 0px 0px 3px 1px #909090;
} */

.modelTitle {
  margin-bottom: 5px;
}
.modelName {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.modelDes {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-style: italic;
}
.tipButton >>> .ivu-btn {
  border: 0;
  clear: both;
  /* border: none;
  outline: none; */
}

.searchTitle {
  float: left;
  font-size: 18px;
  font-weight: 600;
}
</style>
<template>
  <div>
    <Row style="margin-top:1%">
      <Col span="22" offset="2">
        <Row>
          <Input
            v-model="value"
            placeholder="Enter something..."
            style="width: 600px"
          />
        </Row>
      </Col>
    </Row>
  </div>
</template>

<script>
import Avatar from "vue-avatar";
export default {
  components: {
    Avatar
  },
  data() {
    return {
      modelList: [],
      inputUrl: {},
      selectModelName: "",
      selectModelDes: ""
    };
  },
  methods: {
    getUrl(url) {
      this.inputUrl = url;
      this.$emit("getUrl", this.inputUrl);
    }
  },
};
</script>

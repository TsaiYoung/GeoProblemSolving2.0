<style scoped>
.main {
  display: flex;
}
.sidebarTree {
  min-width: 250px;
  margin-top: 20px;
  border: 1px solid lightgray;
  z-index: 0;
  height: fit-content;
}
.resourcePanel {
  flex: 1;
}

.btnPanel {
  flex: 1;
}
.btnPanel button {
  margin-left: 20px;
}
.searchPanel {
  display: flex;
  float: right;
}
.ivu-table td,
.ivu-table th {
  height: 40px !important;
}
.demo-spin-icon-load{
    animation: ani-demo-spin 1s linear infinite;
}
@keyframes ani-demo-spin {
    from { transform: rotate(0deg);}
    50%  { transform: rotate(180deg);}
    to   { transform: rotate(360deg);}
}
.demo-spin-col{
    height: 100px;
    position: relative;
    border: 1px solid #eee;
}
</style>
<!--Resources中心-->
<template>
  <Row>
    <Col span="23" offset="1">
      <h2
        style="margin-top: 20px;"
      >Here are public resources shared by users. We will NEVER make your resources public until you share it.</h2>
      <div class="main" :style="{height:contentHeight}">
        <div class="sidebarTree">
          <Menu theme="light" active-name="all" width="auto" @on-select="onMenuSelect">
            <MenuGroup title="File type">
              <MenuItem name="all">
                <Icon type="ios-list-box-outline" />All
              </MenuItem>
              <MenuItem name="image">
                <Icon type="md-image" />Images
              </MenuItem>
              <MenuItem name="video">
                <Icon type="md-videocam" />Videos
              </MenuItem>
              <MenuItem name="data">
                <Icon type="md-analytics" />Data
              </MenuItem>
              <MenuItem name="paper">
                <Icon type="md-paper" />Papers
              </MenuItem>
              <MenuItem name="document">
                <Icon type="md-document" />Documents
              </MenuItem>
              <MenuItem name="model">
                <Icon type="logo-dropbox" />Models
              </MenuItem>
              <MenuItem name="others">
                <Icon type="logo-xbox" />Others
              </MenuItem>
            </MenuGroup>
          </Menu>
        </div>
        <div class="resourcePanel" style="margin-left:-20px">
          <div class="resourcePanel" style="margin-top: 20px;">
            <Row style="height:100%; overflow-y:auto">
              <template v-if="$store.getters.userState">
                <Col span="22" offset="1">
                  <Table :columns="resourceColumn" :data="showList" border no-data-text="No data" v-show="showList.length>0">
                    <template slot-scope="{ index }" slot="action">
                        <Icon type="md-download" :size="20" color="yellowgreen" @click="downFile(showList[index].address)"/>
                      <a @click="show(index)" style="margin-left: 10px" title="Preview">
                        <Icon type="md-eye" :size="20" color="orange" />
                      </a>
                    </template>
                  </Table>
                </Col>
              </template>
              <template v-else>
                <Col span="22" offset="1">
                  <Table :columns="resourceColumn" :data="showList" border size="small" no-data-text="No data"  v-show="showList.length>0">
                    <template slot-scope="{}" slot="action">
                      <a title="Please download after login">
                        <Icon type="md-download" :size="20" color="gray" />
                      </a>
                      <a style="margin-left: 10px" title="Please preview after login">
                        <Icon type="md-eye" :size="20" color="gray" />
                      </a>
                    </template>
                  </Table>
                </Col>
              </template>
            </Row>
          </div>
          <div style="display:flex;justify-content:center;margin-top: 60px;">
            <Page
              :total="dataCount"
              :page-size="pageSize"
              :current="currentPage"
              show-total
              @on-change="changepage"
              show-elevator
              style="position: absolute; bottom:20px;"
            />
          </div>
        </div>
      </div>
    </Col>
    <Modal
      v-model="uploadModal"
      title="Upload resource"
      @on-ok="submitFile()"
      ok-text="Submit"
      cancel-text="Cancel"
      :mask-closable="false"
      width="600px"
    >
      <div style="display:flex;text-align:center;align-items:center;justify-content:center">
        <!-- 这里定义上传的几种资源类型供用户选择 -->
        <span style="width:20%">Type</span>
        <RadioGroup v-model="fileType" style="width:80%">
          <Radio label="data">Data</Radio>
          <Radio label="image">Images</Radio>
          <Radio label="video">Videos</Radio>
          <Radio label="paper">Papers</Radio>
          <Radio label="document">Documents</Radio>
          <Radio label="model">Models</Radio>
          <Radio label="others">Others</Radio>
        </RadioGroup>
        <!-- 结束 -->
      </div>
      <br />
      <div style="display:flex;text-align:center;align-items:center;justify-content:center">
        <span style="width:20%">Description</span>
        <Input type="textarea" :rows="2" v-model="fileDescription" />
      </div>
      <br />
      <input type="file" @change="getFile($event)" style="margin-left:20%" multiple="multiple" />
    </Modal>
  </Row>
</template>
<script>
export default {
  data() {
    return {
      // 侧边栏的颜色主题
      resourceColumn: [
        {
          type: "index",
          maxWidth: 60,
          align: "center"
        },
        {
          title: "Name",
          key: "name",
          minWidth: 10,
          tooltip: true,
          sortable: true
        },
        {
          title: "Type",
          key: "type",
          width: 100,
          sortable: true
        },
        {
          title: "Size",
          key: "fileSize",
          width: 100,
          sortable: true
        },
        {
          title: "Description",
          key: "description",
          minWidth: 30,
          tooltip: true
        },
        {
          title: "Provider",
          key: "uploaderName",
          width: 150,
          tooltip: true,
          align: "center"
        },
        {
          title: "Upload time",
          key: "uploadTime",
          width: 150,
          sortable: true
        },
        {
          title: "Action",
          slot: "action",
          width: 125,
          align: "center"
        }
      ],
      allResourceList: [],
      allSelectedList: [],
      showList: [],
      resourceData:[],
      dataCount: 0,
      pageSize: 12,
      currentPage: 1,
      // 上传文件的模态框
      uploadModal: false,
      file: "",
      fileDescription: "",
      fileType: "data",
      contentHeight: "",
      panel: null,
      resAddress: "",
      resProxy: this.$store.getters.resProxy,
    };
  },
  mounted() {
    this.initLayout();
    this.readResource();
  },
  beforeRouteLeave(to, from, next) {
    if (this.panel != null) {
      this.panel.close();
    }
    next();
  },
  methods: {
    initLayout() {
      this.contentHeight = window.innerHeight - 180 + "px";
    },
    readResource() {
      this.allResourceList = [];
      this.axios.get("/GeoProblemSolving/rip/file/allPublic").then(res => {
        if (res.data.code == 0){
          let tempResourceList = res.data.data;
          tempResourceList.reverse();
          let that = this;
          tempResourceList.forEach(function(list) {
            let time = list.uploadTime;
            let size = list.fileSize;
            list.fileSize = that.filterSizeType(size);
            list.uploadTime = that.filterTimeStyle(time);
          });
          this.$set(this, "allResourceList", tempResourceList);
          this.dataCount = tempResourceList.length;
          this.allSelectedList = tempResourceList;
          this.sliceList();
        }
      });
    },
    sliceList() {
      let tempResourceList = this.allSelectedList;
      if (this.dataCount < this.pageSize) {
        this.$set(this, "showList", tempResourceList);
      } else {
        this.$set(this, "showList", tempResourceList.slice(0, this.pageSize));
      }
    },
    onMenuSelect(name) {
      if (name == "all") {
        this.$set(this, "allSelectedList", this.allResourceList);
      } else {
        this.mapResourceList(name);
      }
      this.dataCount = this.allSelectedList.length;
      this.sliceList();
    },
    mapResourceList(name) {
      var newList = [];
      newList = this.allResourceList.filter(function(e) {
        if (e.type == name) {
          return e;
        }
      });
      this.$set(this, "allSelectedList", newList);
    },
    changepage(index) {
      this.currentPage = 1;
      var _start = (index - 1) * this.pageSize;
      var _end = index * this.pageSize;
      this.showList = this.allSelectedList.slice(_start, _end);
    },
    downFile(address){
      const a = document.createElement("a");
      a.style.display = "none";
      a.target = "_self";
      if (address.indexOf("/resource/") > 0){
        a.href = address;
      }else{
        if (typeof(address) == "string"){
          a.href = this.resProxy + "/data/" + address.slice(-36);
        }
      }
      a.click();
      a.remove();
    },
    show(index) {
      var res = this.showList[index];
      let name = this.showList[index].suffix;
      if (/(doc|docx|xls|xlsx|ppt|pptx|xml|json|md|gif|jpg|png|jpeg|pdf|csv)$/.test(name.toLowerCase())) {
        if (this.panel != null) {
          this.panel.close();
        }
        let url = "";
        if(res.address.indexOf("http://221.226.60.2:8082") != -1){
          url = this.$store.getters.resProxy + this.showList[index].address.split("http://221.226.60.2:8082")[1];
        } else if(res.address.indexOf("/GeoProblemSolving/resource") != -1){
          url = this.$store.getters.resProxy + res.address;
        } else {
          url = this.$store.getters.resProxy + this.showList[index].address;
        }
        let Base64 = require('js-base64').Base64;
        let finalUrl = "https://geomodeling.njnu.edu.cn/dataTransferServer/onlinePreview?url=" + encodeURIComponent(Base64.encode(url)); //Base64.encode(url)
        // let finalUrl = "http://221.226.60.2:8082/onlinePreview?url=" + Base64.encode(url);  // http：
        var toolURL =
          "<iframe src=" +
          finalUrl +
          ' style="width: 100%;height:100%" frameborder="0"></iframe>';
        this.panel = jsPanel.create({
          headerControls: {
            smallify: "remove"
          },
          theme: "primary",
          footerToolbar: '<p style="height:5px"></p>',
          headerTitle: "Preview",
          contentSize: "800 600",
          content: toolURL,
          disableOnMaximized: true,
          dragit: {
            containment: 5
          },
          closeOnEscape: true,
        });
        $(".jsPanel-content").css("font-size", "0");

      } else if (/(mp4)$/.test(name.toLowerCase())) {
        if (this.panel != null) {
          this.panel.close();
        }
        var url = this.showList[index].address;
        var toolURL =
          "<video src=" +
          url +
          ' style="width: 100%;height:100%" controls></video>';
        this.panel = jsPanel.create({
          headerControls: {
            smallify: "remove"
          },
          theme: "primary",
          footerToolbar: '<p style="height:10px"></p>',
          headerTitle: "Preview",
          contentSize: "800 600",
          content: toolURL,
          disableOnMaximized: true,
          dragit: {
            containment: 5
          },
          closeOnEscape: true
        });
        $(".jsPanel-content").css("font-size", "0");
      } else {
        this.$Notice.error({
          title: "Open failed",
          desc: "Not supported file format."
        });
        return false;
      }
    },
    filterSizeType(value) {
      if(/(KB|MB|GB|B)$/.test(value)){
        return value;
      } else {
        if (value === 0) return "0 B";
        let k = 1024;
        let sizes = ["B", "KB", "MB", "GB"];
        let i = Math.floor(Math.log(value) / Math.log(k));
        return (value / Math.pow(k, i)).toPrecision(3) + " " + sizes[i];
      }
    },
    filterTimeStyle(str) {
      let result = str.split('.')[0];
      return result.replace('T', " ");
    }
  },
};
</script>

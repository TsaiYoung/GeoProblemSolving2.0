<style scoped>
.resourceTitle {
  font-size: 18px;
  height: 20px;
  line-height: 20px;
}
.resourceBtnDiv {
  display: flex;
  align-items: center;
  height: 20px;
  padding: 5px;
}
.fileBtnHoverGreen:hover {
  background-color: #19be6b;
  color: white;
}
.fileBtnHoverGray:hover {
  background-color: #808695;
  color: white;
}
</style>
<template>
    <div>
        <Card>
            <div slot="title" class="resourceTitle"></div>
            <div slot="extra" class="resourceBtnDiv">
                <Tooltip content="Download all selected" placement="bottom">
                    <Button
                    class="fileBtnHoverGray"
                    @click="downloadFiles">
                    <Icon size="20" type="md-cloud-download"></Icon>
                    </Button>
                </Tooltip>
            </div>
            <div style="height:500px">
                <vue-scroll :ops="ops">
                <Table :data="userResourceList" :columns="resourceColumn" class="table" @on-selection-change="setSelectedFiles" no-data-text="No data">
                    <template slot-scope="{ row }" slot="name">
                    <strong>{{ row.name }}</strong>
                    </template>
                    <template slot-scope="{  index }" slot="action">
                    <Button
                        class="fileBtnHoverGreen"
                        size="small"
                        title="Download"
                        @click="downloadSingleFile(index)"
                        icon="md-download"
                        shape="circle"
                        type="text"
                    >
                    </Button>
                    <Button
                    @click="fileEditModalShow(index)"
                    shape="circle"
                    icon="md-create"
                    title="Edit"
                    size="small"
                    class="fileBtnHoverBlue"
                    type="text"
                    ></Button>
                    <Button
                    class="fileBtnHoverRed"
                    size="small"
                    shape="circle"
                    type="text"
                    icon="md-close"
                    title="Remove"
                    @click="deleteResourceModalShow(userResourceList[index].resourceId)"
                    >
                    </Button>
                    </template>
                </Table>
                </vue-scroll>
            </div>
        </Card>
        <Modal
        v-model="deleteResourceModal"
        @on-ok="deleteResource"
        ok-text="Yes"
        cancel-text="No"
        >
        <h3>Do you really want to delete this resource?</h3>
        </Modal>
        <Modal v-model="editFileModel" title="Edit file info" width="600">
        <Form
            ref="editFileValidate"
            :model="editFileValidate"
            :rules="editFileRuleValidate"
            :label-width="100"
            label-position="left"
        >
            <FormItem label="Privacy" prop="privacy">
            <RadioGroup v-model="editFileValidate.privacy" style="width:80%">
                <Radio label="private">Private</Radio>
                <Radio label="public">Public</Radio>
            </RadioGroup>
            </FormItem>
            <FormItem label="Type" prop="type">
            <RadioGroup v-model="editFileValidate.type">
                <Radio label="data"></Radio>
                <Radio label="paper"></Radio>
                <Radio label="document"></Radio>
                <Radio label="model"></Radio>
                <Radio label="image"></Radio>
                <Radio label="video"></Radio>
                <Radio label="others"></Radio>
            </RadioGroup>
            </FormItem>
            <FormItem label="Name" prop="name">
            <Input type="text" :rows="4" v-model="editFileValidate.name" />
            </FormItem>
            <FormItem label="Description" prop="description">
            <Input type="textarea" :rows="4" v-model="editFileValidate.description" />
            </FormItem>
        </Form>
        <div slot="footer">
            <Button @click="editFileModel=false">Cancel</Button>
            <Button type="success" @click="changeFileInfo('editFileValidate')">Submit</Button>
        </div>
        </Modal>
    </div>
</template>

<script>
export default {
  mounted() {
    this.getUserResource();
  },
  data() {
    return {
      userResourceList: [],
      deleteResourceModal: false,
      deleteResourceId: "",
      selectResourceIndex: 0,
      editFileValidate: {
        name: "",
        description: "",
        type: "",
        privacy: ""
      },
      ops: {
        bar: {
          background: "#808695"
        }
      },
      editFileModel: false,
      editFileRuleValidate: {
        privacy: [
          {
            required: true,
            message: "file privacy cannot be empty",
            trigger: "blur"
          }
        ],
        type: [
          {
            required: true,
            message: "file type cannot be empty",
            trigger: "blur"
          }
        ],
        name: [
          {
            required: true,
            message: "file description cannot be empty",
            trigger: "blur"
          }
        ],
        description: [
          {
            required: true,
            message: "file description cannot be empty",
            trigger: "blur"
          }
        ]
      },
      resourceColumn: [
        {
          type: "selection",
          width: 60,
          align: "center"
        },
        {
          title: "Name",
          key: "name",
          tooltip: true,
          sortable: true
        },
        {
          title: "Description",
          key: "description",
          tooltip: true,
          sortable: true
        },
        {
          title: "Type",
          key: "type",
          sortable: true,
          width: 90
        },
        {
          title: "Uploader",
          key: "uploaderName",
          sortable: true,
          width: 120
        },
        {
          title: "Time",
          key: "uploadTime",
          width: 160,
          sortable: true
        },
        {
          title: "Action",
          slot: "action",
          width: 150,
          align: "center"
        }
      ],
      filesToPackage:[],
    };
  },
  methods: {
    getUserResource() {
      this.axios
        .get(
          "/GeoProblemSolving/resource/inquiry" +
            "?key=uploaderId" +
            "&value=" +
            this.$store.getters.userId
        )
        .then(res => {
          if (res.data != "None" && res.data != "Fail") {
            this.userResourceList = res.data;
          } else if (res.data == "None") {
            this.userResourceList = [];
          }
        })
        .catch(err => {
          console.log(err.data);
        });
    },
    processResourceModalShow(index) {
      this.selectResourceIndex = index;
    },
    fileEditModalShow(index) {
      this.selectResourceIndex = index;
      var oldFileInfo = this.userResourceList[index];
      this.editFileValidate = {
        name: oldFileInfo.name,
        description: oldFileInfo.description,
        type: oldFileInfo.type,
        privacy: oldFileInfo.privacy
      };
      this.editFileModel = true;
    },
    changeFileInfo(name) {
      this.$refs[name].validate(valid => {
        if (valid) {
          var editFormData = new FormData();
          editFormData.append(
            "resourceId",
            this.userResourceList[this.selectResourceIndex].resourceId
          );
          editFormData.append("name", this.editFileValidate.name);
          editFormData.append("type", this.editFileValidate.type);
          editFormData.append("description", this.editFileValidate.description);
          editFormData.append("privacy", this.editFileValidate.privacy);
          this.axios({
            url: "/GeoProblemSolving/resource/update",
            method: "post",
            data: editFormData
          })
            .then(res => {
              this.editFileModel = false;
              if (res.data != "Fail" && res.data != "None") {
                var newResourceInfo = res.data;
                this.userResourceList.splice(
                  this.selectResourceIndex,
                  1,
                  newResourceInfo
                );
              } else {
                this.$Message.error("Edit fail: " + res.data + ".");
              }
            })
            .catch(err => {
              this.$Message.error("Edit error.");
            });
        }
      });
    },
    setSelectedFiles(selectedFileList) {
      var selectedFileUrls = [];
      for (var i = 0; i < selectedFileList.length; i++) {
        selectedFileUrls.push(selectedFileList[i].pathURL);
      }
      this.$set(this, "filesToPackage", selectedFileUrls);
    },
    downloadSingleFile(index) {
      window.open(this.userResourceList[index].pathURL);
    },
    deleteResourceModalShow(id) {
      this.deleteResourceModal = true;
      this.deleteResourceId = id;
    },
    deleteResource() {
      if (this.deleteResourceId != "") {
        this.axios
          .get(
            "/GeoProblemSolving/resource/delete?" +
              "resourceId=" +
              this.deleteResourceId
          )
          .then(res => {
            if (res.data == "Success") {
              this.$Notice.success({
                title: "Process result",
                desc: "Delete successfully"
              });
              this.getUserResource();
            } else if (res.data == "Fail") {
              this.$Notice.error({
                title: "Process result",
                desc: "Delete fail"
              });
              // this.$Message.info("Failure");
            }
          })
          .catch(err => {
            console.log(err.data);
          });
      }
    },
    downloadFiles() {
      if (this.filesToPackage.length > 0) {
        this.$Spin.show();
        var filesUrlStr = this.filesToPackage.toString();
        this.axios({
          method: "post",
          url: "/GeoProblemSolving/resource/packageZIP?fileURLs=" + filesUrlStr,
          responseType: "blob"
        })
          .then(res => {
            this.$Spin.hide();
            if (res.status == 200) {
              const blobUrl = window.URL.createObjectURL(res.data);
              if (blobUrl != "") {
                this.download(blobUrl);
              }
            }
          })
          .catch(err => {
            confirm("errror");
          });
      } else {
        this.$Message.warning("No file be selected.");
      }
    },
    download(blobUrl) {
      const a = document.createElement("a");
      a.style.display = "none";
      a.download = "package.zip";
      a.href = blobUrl;
      a.click();
      a.remove();
    }
  }
};
</script>

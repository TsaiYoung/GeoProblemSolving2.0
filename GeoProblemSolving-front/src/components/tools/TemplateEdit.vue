<style scoped>
.modelList {
  height: 80px;
  margin-bottom: 5px;
  padding: 0%;
}
.modelList:hover {
  box-shadow: 0px 0px 3px 1px #2d72f1;
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
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  font-style: italic;
}
.createToolBtn {
  text-align: center;
  margin: 15px 45%;
  font-weight: 800;
  height: 50px;
  font-size: 20px;
}
.selectInput {
  /* height: 60px; */
  font-size: 16px;
  /* background-color: rgb(111, 167, 204); */
}
.inline_style {
  display: flex;
}
/* 上传图片 */
.demo-upload-list {
  display: inline-block;
  width: 60px;
  height: 60px;
  text-align: center;
  line-height: 60px;
  border: 1px solid transparent;
  border-radius: 4px;
  /* overflow-x: hidden; */
  /* overflow-y: scroll; */
  background: #fff;
  position: relative;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
  margin-right: 4px;
}
.demo-upload-list img {
  width: 100%;
  height: 100%;
}
.demo-upload-list-cover {
  display: none;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
}
.demo-upload-list:hover .demo-upload-list-cover {
  display: block;
}
.demo-upload-list-cover i {
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  margin: 0 2px;
}
.uploadBox {
  display: inline-block;
  width: 58px;
  height: 58px;
  line-height: 58px;
  overflow: hidden;
  border-width: 0.75px;
  border-style: dashed;
  border-color: lightslategray;
}
.uploadAvatar {
  position: relative;
  width: 58px;
  height: 58px;
  top: 0;
  left: 0;
  outline: none;
  background-color: transparent;
  opacity: 0;
}

/* 结束 */
</style>
<template>
  <div>
    <Row style="margin-bottom: 2%; margin-left: 20%">
      <Steps :current="currentStep">
        <Step title="Basic information"></Step>
        <Step title="Details"></Step>
      </Steps>
    </Row>
    <Row>
      <div v-if="selectTool.toolSet == false">
        <Form
        ref="selectedTool"
        :model="selectedTool"
        :rules="toolInfoRule"
        :label-width="110"
        class="toolForm"
        >
          <div v-show="this.currentStep === 0">
            <FormItem label="Name:" prop="toolName">
              <Input v-model="selectedTool.toolName" disabled></Input>
            </FormItem>

            <FormItem label="Service Type">
              <Input  v-if="selectedTool.backendType == 'modelItem'" value="Computable Model" disabled> </Input>
              <Input v-if="selectedTool.backendType == 'dataMethod'" value="Data Method" disabled> </Input>
              <Input v-if="selectedTool.backendType == 'webTool'" value="Web Tool" disabled> </Input>
            </FormItem>

            <FormItem v-if="selectedTool.backendType != 'webTool'" label="Service Name">
              <Input v-model="selectedTool.backendName" disabled> </Input>
            </FormItem>

            <FormItem v-else label="Url">
              <Input
                v-model="selectedTool.toolUrl"
                placeholder="Enter the url of your tool"
              />
            </FormItem>

            <FormItem label="Step:" prop="recommendation" >
              <Select
                v-model="selectedTool.recommendation"
                multiple
                placeholder="Select the recommended step of your tool"
              >
                <Option
                  v-for="item in stepList"
                  :key="item.index"
                  :value="item"
                  >{{ item }}</Option
                >
              </Select>
            </FormItem>

            <FormItem label="Description:" prop="description" :label-width="110">
              <Input
                v-model="selectedTool.description"
                type="textarea"
                placeholder="Enter description of your tool"
              />
            </FormItem>

          <FormItem prop="scope" label="Scope" :label-width="100">
            <Select
              v-model="selectedTool.scope"
              style="width: 80%; margin-left: 10px"
            >
              <Option value="inner" key="0"
                >Internal tools (*Use this tool on this platform)</Option
              >
              <Option value="outer" key="1"
                >External tools (*Go to another page to use his tool)</Option
              >
            </Select>
          </FormItem>

          <FormItem label="Tag:" prop="tags">
            <Input
              v-model="inputToolTag"
              placeholder="Enter some tag to identify your tools"
              style="width: 400px"
              @keyup.enter.native="addCreateToolTag(inputToolTag)"
            />
            <Button
              icon="ios-add"
              type="dashed"
              size="small"
              @click="addCreateToolTag(inputToolTag)"
              style="margin-left: 2.5%"
              >Add tag</Button
            >
            <div>
              <Tag
                color="primary"
                v-for="(item, index) in this.selectedTool.tags"
                :key="index"
                closable
                @on-close="deleteCreateToolTag(index)"
                >{{ item }}</Tag

                >
              </div>
              <div>
                <span>Example:</span>
                <Tag style="cursor: default" @click.native="addCreateToolTag('vector')">vector</Tag>
                <Tag style="cursor: default" @click.native="addCreateToolTag('raster')">raster</Tag>
                <Tag style="cursor: default" @click.native="addCreateToolTag('evaluation')">evaluation</Tag>
              </div>
            </FormItem>

            <FormItem label="Privacy:" prop="privacy">
              <RadioGroup v-model="selectedTool.privacy">
                <Radio label="Public">Public</Radio>
                <Radio label="Private">Private</Radio>
              </RadioGroup>
            </FormItem>

            <FormItem label="Image:" prop="toolImg">
              <div class="inline_style">
                <div class="demo-upload-list" v-if="selectedTool.toolImg != ''">
                  <template>
                    <img :src="selectedTool.toolImg" />
                    <div class="demo-upload-list-cover">
                      <Icon
                        type="ios-eye-outline"
                        @click.native="handleView()"
                      ></Icon>
                      <Icon
                        type="ios-trash-outline"
                        @click.native="handleRemove()"
                      ></Icon>
                    </div>
                  </template>
                </div>
                <div class="uploadBox">
                  <Icon
                    type="ios-camera"
                    size="20"
                    style="position: absolute; margin: 18px"
                  ></Icon>
                  <input
                    id="choosePicture"
                    @change="uploadPhoto($event)"
                    type="file"
                    class="uploadAvatar"
                    accept="image/*"
                  />
                </div>
                <Modal title="View Image" v-model="visible">
                  <img :src="img" v-if="visible" style="width: 100%" />
                </Modal>
              </div>
            </FormItem>
          </div>
          <div v-show="this.currentStep === 1">
            <FormItem label="Detail:" prop="detail" :label-width="0">
              <tinymce ref="editor" v-model="selectedTool.detail" :height="300" />
            </FormItem>
          </div>
        </Form>
      </div>

      <div v-else>
        <Form
        ref="selectedTool"
        :model="selectedTool"
        :rules="toolsetInfoRule"
        :label-width="110"
        class="toolForm"
        >
          <div v-show="this.currentStep === 0">
            <FormItem label="Name:" prop="toolName">
              <Input v-model="selectedTool.toolName" disabled></Input>
            </FormItem>
            <FormItem label="Description:" prop="description" :label-width="110">
              <Input
                v-model="selectedTool.description"
                type="textarea"
                placeholder="Enter description of your tool"
              />
            </FormItem>
            <FormItem label="Tool List:" prop="toolList" >
              <Card style="height:70px;" :padding="0" dis-hover>
                <vue-scroll :ops="ops" style="height:65px;">
                  <Tag
                    color="#ff9900"
                    style="margin-left: 7px"
                    v-for="(item, index) in toolList"
                    :key="index"
                    >{{ item.toolName }}</Tag
                  >
                </vue-scroll>
              </Card>
            </FormItem>
            <FormItem label="Step:" prop="recommendation" >
              <Select
                v-model="selectedTool.recommendation"
                multiple
                placeholder="Select the recommended step of your tool"
              >
                <Option
                  v-for="item in stepList"
                  :key="item.index"
                  :value="item"
                  >{{ item }}</Option
                >
              </Select>
            </FormItem>

            <FormItem label="tag:" prop="tags">
              <Input
                v-model="inputToolTag"
                placeholder="Enter some tag to classify your tools"
                style="width: 400px"
                @keyup.enter.native="addCreateToolTag(inputToolTag)"
              />
              <Button
                icon="ios-add"
                type="dashed"
                size="small"
                @click="addCreateToolTag(inputToolTag)"
                style="margin-left: 2.5%"
                >Add tag</Button
              >
              <div>
                <Tag
                  color="primary"
                  v-for="(item, index) in this.selectedTool.tags"
                  :key="index"
                  closable
                  @on-close="deleteCreateToolTag(index)"
                  >{{ item }}</Tag
                >
              </div>
              <div>
                <span>Example:</span>
                <Tag style="cursor: default" @click.native="addCreateToolTag('vector')">vector</Tag>
                <Tag style="cursor: default" @click.native="addCreateToolTag('raster')">raster</Tag>
                <Tag style="cursor: default" @click.native="addCreateToolTag('evaluation')">evaluation</Tag>
              </div>
            </FormItem>

            <FormItem label="Privacy:" prop="privacy">
              <RadioGroup v-model="selectedTool.privacy">
                <Radio label="Public">Public</Radio>
                <Radio label="Private">Private</Radio>
              </RadioGroup>
            </FormItem>

            <FormItem label="Image:" prop="toolImg">
              <div class="inline_style">
                <div class="demo-upload-list" v-if="selectedTool.toolImg != ''">
                  <template>
                    <img :src="selectedTool.toolImg" />
                    <div class="demo-upload-list-cover">
                      <Icon
                        type="ios-eye-outline"
                        @click.native="handleView()"
                      ></Icon>
                      <Icon
                        type="ios-trash-outline"
                        @click.native="handleRemove()"
                      ></Icon>
                    </div>
                  </template>
                </div>
                <div class="uploadBox">
                  <Icon
                    type="ios-camera"
                    size="20"
                    style="position: absolute; margin: 18px"
                  ></Icon>
                  <input
                    id="choosePicture"
                    @change="uploadPhoto($event)"
                    type="file"
                    class="uploadAvatar"
                    accept="image/*"
                  />
                </div>
                <Modal title="View Image" v-model="visible">
                  <img :src="img" v-if="visible" style="width: 100%" />
                </Modal>
                  <!-- <Upload
                    ref="upload"
                    :show-upload-list="false"
                    :format="['jpg', 'jpeg', 'png', 'gif']"
                    :max-size="2048"
                    :before-upload="handleBeforeUpload"
                    action
                    style="display: inline-block; width: 58px"
                    type="drag"
                  >
                    <div style="width: 58px; height: 58px; line-height: 58px">
                      <Icon type="ios-camera" size="20"></Icon>
                    </div>
                  </Upload> -->
              </div>
            </FormItem>
          </div>
          <div v-show="this.currentStep === 1">
            <FormItem label="Detail:" prop="detail" :label-width="0">
              <tinymce ref="editor" v-model="selectedTool.detail" :height="300" />
            </FormItem>
          </div>
        </Form>
      </div>
    </Row>
  </div>
</template>
<script>
import tinymce from "./../tinymce";
import Avatar from "vue-avatar";
export default {
  components: {
    tinymce,
    Avatar,
  },
  props: {
    step: {
      type: Number,
    },
    selectTool: {
      type: Object,
    },
  },
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      selectedTool: this.selectTool,
      toolInfoRule: {
        toolName: [
          {
            required: true,
            message: "The name cannot be empty",
            trigger: "blur",
          },
        ],
        toolUrl: [
          {
            required: true,
            message: "The url cannot be empty",
            trigger: "blur",
          },
        ],
        description: [
          {
            trigger: "blur",
          },
        ],
        privacy: [
          {
            trigger: "change",
          },
        ],
      },
      toolsetInfoRule: {
        toolName: [
          {
            required: true,
            message: "The name cannot be empty",
            trigger: "blur",
          },
        ],
        description: [
          {
            trigger: "blur",
          },
        ],
        privacy: [
          {
            trigger: "change",
          },
        ],
      },
      userId: "testUserId",
      inputToolTag: "",
      personalToolList: [],
      toolNameList: [],
      visible: false,
      createToolFlag: null,
      pageParams: { pageId: "", userId: "", userName: "" },
      //step
      currentStep: 0,
      stepList: [
        "All",
        "Context definition & resource collection",
        "Data processing",
        "Data analysis",
        "Data visualization",
        "Geo-analysis model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Decision making",
      ],
    };
  },
  created() {
    this.querryTools();
  },
  methods: {
    addCreateToolTag(tag) {
      if(this.selectedTool.toolSet == false){
        if (tag != "") {
        this.selectedTool.tags.push(tag);
        this.inputToolTag = "";
        }
      } else {
        if (tag != "") {
        this.selectedTool.tags.push(tag);
        this.inputToolTag = "";
        }
      }
    },

    deleteCreateToolTag(index) {
      if(this.selectedTool.toolSet == false){
        this.selectedTool.tags.splice(index, 1);
      } else {
        this.selectedTool.tags.splice(index, 1);
      }
    },
    querryTools: function () {
      this.axios
        .get("/GeoProblemSolving/tool/provider/" + this.$store.getters.userId )
        .then((res) => {
          if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({ name: "Login" });
          } else if (res.data.code == 0){
            let tools =  res.data.data;
            for( let i = 0 ; i < tools.length ; i++){
              if(tools[i].toolSet == false){
                this.personalToolList.push(tools[i]);
              }
            }
          }
        })
        .catch((err) => {
          this.$Message.error("Fail");
        });
    },
    uploadPhoto(e) {
      // 利用fileReader对象获取file
      var file = e.target.files[0];
      var filesize = file.size;
      // 2,621,440   2M

      if (filesize > 2101440) {
        // 图片大于2MB
        this.$Message.error("size > 2MB");
      } else {
        var reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => {
          this.img = e.target.result;
          if(this.selectedTool.toolSet == false){
            this.selectedTool.toolImg = this.img;
          } else {
            this.selectedTool.toolImg = this.img;
          }
        };
      }
    },

    handleView() {
      this.visible = true;
    },

    handleRemove() {
      this.img = "";
      if(this.selectedTool.toolSet == false){
        this.selectedTool.toolImg ="";
      } else {
        this.selectedTool.toolImg ="";
      }
    },

    addEditToolTag(tag) {
      if (tag != "") {
        this.selectedTool.tags.push(tag);
        this.inputToolTag = "";
      }
    },
    deleteEditToolTag(index) {
      this.selectedTool.tags.splice(index, 1);
    },

  },
  computed:{
    toolList(){
      let toolsInToolset = [];
      if(this.selectTool.toolList){
        for(let i = 0 ; i < this.selectedTool.toolList.length ; i++){
          for(let j = 0 ; j < this.personalToolList.length ; j++){
            if(this.selectedTool.toolList[i] == this.personalToolList[j].tid){
              toolsInToolset.push(this.personalToolList[j]);
            }
          }
        }
      }
      return toolsInToolset;
    }
  },
  watch: {
    //监听step切换
    step: {
      handler(val) {
        this.currentStep = val;
      },
      deep: true,
    },
    selectTool: {
      handler(val) {
        this.selectedTool = val;
      },
      deep: true,
    },
    selectedTool: {
      handler(val) {
        this.$emit("generalInfo", this.selectedTool);
      },
      deep: true,
    },
  },
};
</script>

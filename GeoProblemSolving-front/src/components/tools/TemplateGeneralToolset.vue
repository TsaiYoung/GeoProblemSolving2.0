<style scoped xmlns="http://www.w3.org/1999/html">
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
  overflow: hidden;
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
      <Form
        ref="toolsetInfo"
        :model="toolsetInfo"
        :rules="toolsetInfoRule"
        :label-width="110"
        class="toolsetForm"
      >
        <div v-show="this.currentStep === 0">
          <FormItem label="Name:" prop="toolsetName" >
            <Input v-model="toolsetInfo.toolsetName" placeholder="Enter the name of your toolset"></Input>
          </FormItem>
          <FormItem label="Description:" prop="description" >
            <Input
                v-model="toolsetInfo.description"
                type="textarea"
                placeholder="Enter the description of your toolset"
            ></Input>
          </FormItem>
          <FormItem label="Tool List:" prop="toolList" >
            <Select
                value="toolsetInfo.toolList"
                multiple
                placeholder="Select the tools of your toolset"
                @on-change="selectTools"
            >
                <Option v-for="(item,index) in personalToolList" :key="index" :value="index">{{ item.toolName }}</Option>
            </Select>
          </FormItem>
          <FormItem label="Step:" prop="recomStep" >
            <Select
                v-model="toolsetInfo.recomStep"
                multiple
                placeholder="Select the recommended step of your toolset"
            >
                <Option v-for="item in stepList" :key="item.index" :value="item">{{ item }}</Option>
            </Select>
          </FormItem>
          <FormItem label="Tag:" prop="categoryTag">
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
              >Add tag
            </Button>
            <div>
              <Tag
                color="primary"
                v-for="(item, index) in this.toolsetInfo.categoryTag"
                :key="index"
                closable
                @on-close="deleteCreateToolTag(index)"
                >{{ item }}
              </Tag>
            </div>
            <div>
              <span>Example:</span>
              <Tag
                style="cursor: default"
                @click.native="addCreateToolTag('vector')"
                >vector</Tag
              >
              <Tag
                style="cursor: default"
                @click.native="addCreateToolTag('raster')"
                >raster</Tag
              >
              <Tag
                style="cursor: default"
                @click.native="addCreateToolTag('evaluation')"
                >evaluation</Tag
              >
            </div>
          </FormItem>

          <FormItem label="Privacy:" prop="privacy">
            <RadioGroup v-model="toolsetInfo.privacy">
              <Radio label="Public">Public</Radio>
              <Radio label="Private">Private</Radio>
            </RadioGroup>
          </FormItem>

          <FormItem label="Image:" prop="toolImg">
            <div class="inline_style">
              <div class="demo-upload-list" v-if="img != ''">
                <template>
                  <img :src="img" />
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
            <tinymce ref="editor" :value="toolsetInfo.detail" :height="300" />
          </FormItem>
        </div>
      </Form>
    </Row>
  </div>
</template>

<script>
import tinymce from "./../tinymce";
import Avatar from "vue-avatar";
import { post } from "../../axios";
export default {
  components: {
    tinymce,
    Avatar,
  },
  props: {
    step: {
      type: Number,
    },
    info: {
      type: Object,
    },
    editToolInfo: {
      type: Object,
    },
  },
  data() {
    return {
      stepList: [
        "All",
        "Context definition & resource collection",
        "Data processing",
        "Data visualization",
        "Geo-analysis model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Data analysis",
        "Decision making",
      ],
      toolsetInfo: {
        toolsetName: "",
        description: "",
        recomStep: [],
        toolsetImg: "",
        privacy: "Private",
        categoryTag: [],
        detail: "",
        toolSet: true,
        toolList: [],
      },
      toolsetInfoRule: {
        toolsetName: [
          {
            required: true,
            message: "The name cannot be empty",
            trigger: "blur"
          }
        ],
        description: [
          {
            trigger: "blur"
          }
        ],
        privacy: [
          {
            required: true,
            message: "Is this toolset can be used by public or not?",
            trigger: "change"
          }
        ]
      },
      personalToolList: [],
      searchLoading: false,
      inputToolTag: "",
      visible: false,
      createToolFlag: null,
      pageParams: { pageId: "", userId: "", userName: "" },
      //step
      currentStep: this.step,
      //返回的服务基本信息
      backendServices: [],
      img: "",
      pictureUrl: "",
    };
  },
  created() {
    this.querryTools();
  },
  methods: {
    addCreateToolTag(tag) {
      if (tag != "") {
        this.toolsetInfo.categoryTag.push(tag);
        this.inputToolTag = "";
      }
    },

    deleteCreateToolTag(index) {
      this.toolsetInfo.categoryTag.splice(index, 1);
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
          this.toolsetInfo.toolsetImg = this.img;
        };
      }
    },

    handleView() {
      this.visible = true;
    },

    handleRemove() {
      this.img = "";
      this.pictureUrl = "";
      this.toolsetInfo.toolsetImg = this.pictureUrl;
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

    selectTools(indexList){
      this.toolsetInfo.toolList = [];
      for( let i = 0 ; i < indexList.length ; i++){
        this.toolsetInfo.toolList.push(this.personalToolList[indexList[i]]);
      }
      // console.log(this.toolsetInfo.toolList);
    }
  },

  watch: {
    //将toolInfo传出，子传父，在组件中通过定义属性传到父组件
    toolsetInfo: {
      handler(val) {
        this.$emit("generalInfo", this.toolsetInfo);
      },
      deep: true,
    },
    //监听step切换
    step: {
      handler(val) {
        this.currentStep = val;
      },
      deep: true,
    },
    info: {
      handler(val) {
        // console.log(val);
        if (val != "") {
          this.toolsetInfo = val;
        }
      },
      deep: true,
    },
    editToolInfo: {
      handler(val) {
        this.toolsetInfo = val;
      },
      deep: true,
    },
  },
};
</script>

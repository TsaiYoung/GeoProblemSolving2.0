<!-- bubble-left green -->
<template>
  <div class>
    <template>
      <Row>
        <div style="float:left;">
          <Row style="margin:8px 0 0 12px">
            <div class="user_name">{{ list.sender.name }}</div>
          </Row>
          <Row>
            <div class="chat-bubble-l chat-bubble-left">
              <template v-if="list.type === 'message_pic'" class="picOuter">
                <img :src="list.content" class="send_pic" />
              </template>
              <template
                v-else-if="list.type === 'message_tool'"
                class="picOuter"
              >
                <Poptip width="80" trigger="hover">
                  <simple-public-card
                    :toolFrom="list.content"
                    class="toolOuter"
                  ></simple-public-card>
                  <div slot="content">
                    <div class="pop-content" @click="showPreview(list)">
                      Preview
                    </div>
                    <div class="pop-content" @click="saveToolDialog(list)">
                      Save
                    </div>
                  </div>
                </Poptip>
              </template>
              <template v-else>{{ list.content }}</template>
            </div>
          </Row>
        </div>
      </Row>
    </template>
    <div v-show="isOpenTool" title="Tool preview" width="800" class="mask">
      <Icon
        type="md-close"
        @click.native="closeToolPreview"
        class="maskBtn"
        title="Close the tool information"
        size="30"
      />
      <tool-preview :selectTool="selectTool"></tool-preview>
    </div>

    <Modal v-model="saveToolDialogShow" title="Save Tool" width="800">
      <template-general
        @generalInfo="getGeneralInfo"
        ref="toolInfoRef"
        :info="currentToolInfo"
        :step="currentStep"
      ></template-general>
      <div slot="footer">
        <Button
          type="warning"
          @click="previousStep"
          v-show="this.currentStep == 1"
          >Previous</Button
        >
        <Button type="primary" @click="nextStep" v-show="this.currentStep == 0"
          >Next</Button
        >
        <Button
          type="success"
          @click="createTool"
          class="create"
          v-show="this.currentStep == 1"
          >Save</Button
        >
      </div>
    </Modal>
  </div>
</template>

<script>
import simplePublicCard from "@/components/common/card/simplePublicToolsCard";
import toolModal from "@/components/common/tools/toolModal";

import toolPreview from "@/components/common/tools/toolPreview";
import templateGeneral from "@/components/tools/TemplateGeneral";

import { post } from "@/axios";
export default {
  props: {
    message: {
      type: Object
    }
  },
  components: {
    simplePublicCard,
    toolModal,
    toolPreview,
    templateGeneral
  },

  data() {
    return {
      list: this.message,
      // isOpenTool: true,
      isOpenTool: false,
      selectTool: {},
      saveToolDialogShow: false,
      // getGeneralInfo: {},
      currentStep: 0,
      currentToolInfo: {},
      createToolInfo: {}
    };
  },
  watch: {
    message: {
      handler(val) {
        this.list = val;
      },
      deep: true
    }
  },
  methods: {
    showPreview({ content }) {
      console.log(content);
      this.selectTool = content;
      this.isOpenTool = true;
    },
    closeToolPreview() {
      this.isOpenTool = false;
    },
    saveToolDialog({ content }) {
      this.currentToolInfo = content;
      this.saveToolDialogShow = true;
    },

    //创建Tool里面的Step切换
    nextStep() {
      if (this.currentStep != 1) {
        this.currentStep += 1;
      }
    },
    previousStep() {
      if (this.currentStep != 0) {
        this.currentStep -= 1;
      }
    },
    getGeneralInfo(val) {
      this.createToolInfo = val;
      // console.log(val);
    },
    //创建工具
    async createTool() {
      let createToolForm = this.createToolInfo;
      createToolForm["provider"] = this.$store.getters.userId;

      let data = await post("/GeoProblemSolving/tool/create", createToolForm);
      this.saveToolDialogShow = false;
      this.$Notice.info({ desc: "Save the tool successfully" });
    }
  }
};
</script>

<style lang="scss" scoped>
//user
.user_detail {
  height: 60px;
  .user_avatar_outer {
    width: 60px;
    height: 60px;
    .user_avatar {
      width: 100%;
      height: 100%;
    }
    .user_img {
      background-color: lightblue;
      margin-top: 2px;
      margin-left: 5px;
    }
  }
}

.user_name {
  text-align: left;
  /* text-shadow: rgb(75, 75, 75) 0px 0px 2px; */
  color: rgb(63, 63, 63);
  font-size: 10px;
  line-height: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

//bubble green
.chat-bubble-l {
  position: relative;
  margin: 12px;
  padding: 10px;
  word-break: break-word;
  background: #fff;
  border: 1px solid #fff;
  border-radius: 5px;
  font-size: 18px;
  /* min-width: 20%; */
  float: left;

  max-width: 100%;
}
.pop-content {
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
  padding: 5px;
}

.pop-content:hover {
  transition: background-color 0.2s ease;
  background-color: rgb(215, 233, 250);
  cursor: pointer;
}

//send picture
.picOuter {
  max-height: 500px;
  max-width: 400px;
}
.toolOuter {
  max-width: 150px;
  max-height: 110px;
}

.send_pic {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 100%;
}

/* 遮盖罩 */
.mask {
  position: fixed;
  /* 最顶层 */
  z-index: 1;
  left: 0px;
  top: 0px;
  right: 0px;
  bottom: 0px;
  background-color: rgba(247, 241, 241, 0.938);
  padding: 3% 10% 0 10%;
  overflow-y: scroll;
  /* overflow-x:hidden; */

  .maskBtn {
    position: absolute;
    z-index: 2;
    right: 10%;
  }

  .maskBtn:hover {
    cursor: pointer;
  }
}
</style>

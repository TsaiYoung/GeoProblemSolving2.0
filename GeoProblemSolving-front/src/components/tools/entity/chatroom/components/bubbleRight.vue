<!-- bubble-left green -->
<template>
  <div class>
    <template>
      <template>
        <Row>
          <div style="float: right">
            <Row style="margin: 8px 12px 0 0" v-if="list.receivers != undefined && list.receivers.length > 0">
              <div class="user_name">
                {{ list.sender.name }} (Private chat)
              </div>
            </Row>
            <Row style="margin: 8px 12px 0 0" v-else>
              <div class="user_name">{{ list.sender.name }}</div>
            </Row>
            <Row>
              <div class="chat-bubble-r chat-bubble-right">
                <template>
                  <Icon
                    type="ios-pulse"
                    color="red"
                    v-show="list.status === 'sending'"
                    title="Message sending"
                    class="msg-status"
                  />
                  <Icon
                    type="md-alert"
                    color="red"
                    v-show="list.status === 'failed'"
                    title="Failed to send this message"
                    class="msg-status"
                  />
                </template>
                <template v-if="list.type === 'message_pic'" class="picOuter">
                  <img :src="list.content" class="send_pic" />
                </template>
                <template v-else-if="list.type === 'message_tool'">
                  <Dropdown>
                    <a href="javascript:void(0)">
                      <simple-public-card
                        :toolFrom="list.content"
                        class="toolOuter"
                      ></simple-public-card>
                    </a>
                    <DropdownMenu slot="list">
                      <DropdownItem @click.native="showPreview(list)"
                        >Preview</DropdownItem
                      >
                      <DropdownItem>Save</DropdownItem>
                    </DropdownMenu>
                  </Dropdown>
                </template>
                <template v-else>{{ list.content }}</template>
              </div>
            </Row>
          </div>
        </Row>
      </template>
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
  </div>
</template>

<script>
import simplePublicCard from "@/components/common/card/simplePublicToolsCard";
import toolModal from "@/components/common/tools/toolModal";
import toolPreview from "@/components/common/tools/toolPreview";
export default {
  props: {
    message: {
      type: Object,
    },
  },

  components: {
    simplePublicCard,
    toolModal,
    toolPreview,
  },

  data() {
    return {
      list: this.message,
      isOpenTool: false,
      selectTool: {},
    };
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
  },
};
</script>

<style lang="scss" scoped>
//send notice
.chat-notice {
  color: darkgreen;
  width: 90%;
  text-align: center;
}

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
  text-align: right;
  /* text-shadow: rgb(75, 75, 75) 0px 0px 2px; */
  color: rgb(63, 63, 63);
  font-size: 10px;
  line-height: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

//bubble green
.chat-bubble-r {
  position: relative;
  margin: 12px;
  padding: 8px;
  word-break: break-word;
  background: rgb(96, 206, 120);
  border: 1px solid transparent;
  color: black;
  border-radius: 5px;
  font-size: 18px;
  float: right;

  max-width: 100%;
}

// .chat-bubble-right:before {
//   content: "";
//   position: absolute;
//   width: 0;
//   height: 0;
//   right: -20px;
//   top: 5px;
//   border: 10px solid;
//   border-color: transparent transparent transparent rgb(96, 206, 120);
// }

// .chat-bubble-right:after {
//   content: "";
//   position: absolute;
//   width: 0;
//   height: 0;
//   right: -16px;
//   top: 7px;
//   border: 8px solid;
//   border-color: transparent transparent transparent rgb(96, 206, 120);
// }

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

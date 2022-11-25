<template>
  <div>
    <div id="title">
      <h1 style="text-align: center; margin-top: 10px">Tool</h1>
      <h3 style="text-align: center; margin-bottom: 10px">
        You can manage your tools here
      </h3>
    </div>
    <Row>
      <Col span="22" offset="1">
        <!--  Filter 包括 Created by me, Shared with me(项目中内容)，public, privacy-->
        <!--  下移  -->
        <!--  具体显示区，图标加 缩略图 + 名字的显示方案; 先把created tool 拉过来；添加 shared 接口-->
        <Row>
          <Card dis-hover style="position: relative" class="customCard">
            <p slot="title">
              <CheckboxGroup
                v-model="checkedType"
                style="display: inline-block; margin-left: 1%"
              >
                <Checkbox label="public">
                  <label>Public</label>
                  <label class="badge">{{
                    publicTools.length + publicToolsets.length
                  }}</label>
                </Checkbox>
                <Checkbox label="private">
                  <label>Privacy</label>
                  <label class="badge">{{
                    privateTools.length + privateToolsets.length
                  }}</label>
                </Checkbox>
              </CheckboxGroup>
              <Divider type="vertical" style="margin-left: 20px" />
              <span style="margin: 0 10px"></span>
              <CheckboxGroup
                v-model="toolAndToolset"
                style="display: inline-block"
              >
                <Checkbox label="tool">
                  <span>Tool</span>
                  <span class="badge">{{
                    publicTools.length + privateTools.length
                  }}</span>
                </Checkbox>
                <Checkbox label="toolset">
                  <span>Toolset</span>
                  <span class="badge">{{
                    privateToolsets.length + publicToolsets.length
                  }}</span>
                </Checkbox>
              </CheckboxGroup>
            </p>
            <!--              添加一个模态显示判断 -->
            <Button
              slot="extra"
              type="primary"
              icon="md-add"
              @click="chooseCreateModal = true"
              style="
                display: inline-block;
                position: absolute;
                right: 20px;
                top: -4px;
              "
              >Create
            </Button>

            <!--            工具显示内容  -->
            <div
              :style="{ height: contentHeight - 180 + 'px' }"
              style="height: inherit; min-height: fit-content"
            >
              <vue-scroll :ops="ops">
                <Card
                  :bordered="false"
                  v-if="userToolCount.length == 0"
                  dis-hover
                >
                  <div style="display: flex; justify-content: center">
                    <Icon type="md-alert" size="40" color="gray" />
                  </div>
                  <br />
                  <div style="display: flex; justify-content: center">
                    <h3 style="text-align: center; width: 80%">
                      Sorry, you don't have any tools.
                    </h3>
                  </div>
                </Card>

                <div v-if="useToolCSS">
                  <Row>
                    <!--        每一排 1 个-->
                    <Col
                      span="24"
                      v-for="(tool, index) in userToolCount"
                      :key="index"
                    >
                      <div style="margin: 0 5px 15px 5px">
                        <Card class="toolCard" v-if="tool.toolSet == false">
                          <div slot="title" class="toolCardHead">
                            <p
                              class="ellipsis"
                              style="
                                width: 50%;
                                display: inline-block;
                                font-size: 16px;
                              "
                              :title="tool.toolName"
                            >
                              {{ tool.toolName }}
                            </p>
                          </div>
                          <div slot="extra">
                            <Icon
                              type="md-create"
                              title="Edit"
                              size="25"
                              class="icon"
                              @click="editTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="ios-share-alt"
                              title="Share"
                              size="25"
                              class="icon"
                              @click="shareTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="md-close"
                              size="25"
                              class="icon"
                              title="Delete"
                              @click="
                                delTool(
                                  tool.tid,
                                  tool.privacy,
                                  index,
                                  tool.toolName,
                                  tool.toolSet
                                )
                              "
                            />
                          </div>
                          <!--                       内容    -->
                          <div style="cursor: pointer; position: relative">
                            <div style="position: absolute; margin-top: 5px">
                              <img
                                :src="tool.toolImg"
                                v-if="
                                  tool.toolImg != '' && tool.toolImg != null
                                "
                                style="
                                  height: 70px;
                                  width: 70px;
                                  border-radius: 50%;
                                "
                              />
                              <avatar
                                :username="tool.toolName"
                                :size="70"
                                style="margin-bottom: 6px"
                                v-else
                              ></avatar>
                            </div>
                            <div
                              style="
                                margin-left: 10%;
                                width: 85%;
                                display: flex;
                                align-items: center;
                                height: 80px;
                              "
                            >
                              <p class="toolDes">{{ tool.description }}</p>
                            </div>
                          </div>
                        </Card>
                        <Card class="toolCard" v-if="tool.toolSet == true">
                          <div slot="title" class="toolCardHead">
                            <p
                              class="ellipsis"
                              style="
                                width: 50%;
                                display: inline-block;
                                font-size: 16px;
                              "
                              :title="tool.toolName"
                            >
                              {{ tool.toolName }}
                            </p>
                          </div>
                          <div slot="extra">
                            <Icon
                              type="ios-settings"
                              title="Manage tools"
                              size="25"
                              class="icon"
                              @click="manageTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="md-create"
                              title="Edit"
                              size="25"
                              class="icon"
                              @click="editTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="ios-share-alt"
                              title="Share"
                              size="25"
                              class="icon"
                              @click="shareTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="md-close"
                              size="25"
                              class="icon"
                              title="Delete"
                              @click="
                                delTool(
                                  tool.tid,
                                  tool.privacy,
                                  index,
                                  tool.toolName,
                                  tool.toolSet
                                )
                              "
                            />
                          </div>
                          <!--                       内容    -->
                          <div style="cursor: pointer; position: relative">
                            <div style="position: absolute; margin-top: 5px">
                              <img
                                :src="tool.toolImg"
                                v-if="
                                  tool.toolImg != '' && tool.toolImg != null
                                "
                                style="
                                  height: 70px;
                                  width: 70px;
                                  border-radius: 50%;
                                "
                              />
                              <avatar
                                :username="tool.toolName"
                                :size="70"
                                style="margin-bottom: 6px"
                                v-else
                              ></avatar>
                            </div>
                            <div
                              style="
                                margin-left: 10%;
                                width: 85%;
                                display: flex;
                                align-items: center;
                                height: 80px;
                              "
                            >
                              <p class="toolDes">{{ tool.description }}</p>
                            </div>
                          </div>
                        </Card>
                        <Card dis-hover class="toolCardFoot">
                          <div class="toolTags">
                            <div
                              style="position: absolute; top: -5px; left: -10px"
                              v-if="tool.toolSet == false"
                            >
                              <span class="toolTypeSpan">
                                {{ tool.backendType }}
                              </span>
                              <span
                                v-for="(tag, index) in tool.tags"
                                :key="tag"
                                class="toolTagSpan"
                              >
                                {{ tool.tags[index] }}
                              </span>
                            </div>
                            <div
                              style="position: absolute; top: -5px; left: -10px"
                              v-if="tool.toolSet == true"
                            >
                              <span class="toolsetTypeSpan"> Toolset </span>
                              <span
                                v-for="(tag, index) in tool.tags"
                                :key="tag"
                                class="toolTagSpan"
                              >
                                {{ tool.tags[index] }}
                              </span>
                            </div>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </Row>
                </div>

                <!-- v-else -->
                <div v-else>
                  <Row>
                    <!--        每一排 1 个-->
                    <Col
                      span="24"
                      v-for="(tool, index) in userToolCount"
                      :key="index"
                    >
                      <div style="margin: 0 5px 15px 5px">
                        <Card class="toolCard" v-if="tool.toolSet == false">
                          <div slot="title" class="toolCardHead">
                            <p
                              class="ellipsis"
                              style="
                                width: 50%;
                                display: inline-block;
                                font-size: 16px;
                              "
                              :title="tool.toolName"
                            >
                              {{ tool.toolName }}
                            </p>
                          </div>
                          <div slot="extra">
                            <Icon
                              type="md-create"
                              title="Edit"
                              size="25"
                              class="icon"
                              @click="editTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="ios-share-alt"
                              title="Share"
                              size="25"
                              class="icon"
                              @click="shareTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="md-close"
                              size="25"
                              class="icon"
                              title="Delete"
                              @click="
                                delTool(
                                  tool.tid,
                                  tool.privacy,
                                  index,
                                  tool.toolName,
                                  tool.toolSet
                                )
                              "
                            />
                          </div>
                          <!--                       内容    -->
                          <div style="cursor: pointer; position: relative">
                            <div style="position: absolute; margin-top: 5px">
                              <img
                                :src="tool.toolImg"
                                v-if="
                                  tool.toolImg != '' && tool.toolImg != null
                                "
                                style="
                                  height: 70px;
                                  width: 70px;
                                  border-radius: 50%;
                                "
                              />
                              <avatar
                                :username="tool.toolName"
                                :size="70"
                                style="margin-bottom: 6px"
                                v-else
                              ></avatar>
                            </div>
                            <div
                              style="
                                margin-left: 10%;
                                width: 85%;
                                display: flex;
                                align-items: center;
                                height: 80px;
                              "
                            >
                              <p class="toolDes">{{ tool.description }}</p>
                            </div>
                          </div>
                        </Card>
                        <Card class="toolCard" v-if="tool.toolSet == true">
                          <div slot="title" class="toolCardHead">
                            <p
                              class="ellipsis"
                              style="
                                width: 50%;
                                display: inline-block;
                                font-size: 16px;
                              "
                              :title="tool.toolName"
                            >
                              {{ tool.toolName }}
                            </p>
                          </div>
                          <div slot="extra">
                            <Icon
                              type="ios-settings"
                              title="Manage tools"
                              size="25"
                              class="icon"
                              @click="manageTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="ma-create"
                              title="Edit"
                              size="25"
                              class="icon"
                              @click="editTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="ios-share-alt"
                              title="Share"
                              size="25"
                              class="icon"
                              @click="shareTool(tool)"
                              style="margin-right: 3px"
                            />
                            <Icon
                              type="md-close"
                              size="25"
                              class="icon"
                              title="Delete"
                              @click="
                                delTool(
                                  tool.tid,
                                  tool.privacy,
                                  index,
                                  tool.toolName,
                                  tool.toolSet
                                )
                              "
                            />
                          </div>
                          <!--                       内容    -->
                          <div style="cursor: pointer; position: relative">
                            <div style="position: absolute; margin-top: 5px">
                              <img
                                :src="tool.toolImg"
                                v-if="
                                  tool.toolImg != '' && tool.toolImg != null
                                "
                                style="
                                  height: 70px;
                                  width: 70px;
                                  border-radius: 50%;
                                "
                              />
                              <avatar
                                :username="tool.toolName"
                                :size="70"
                                style="margin-bottom: 6px"
                                v-else
                              ></avatar>
                            </div>
                            <div
                              style="
                                margin-left: 10%;
                                width: 85%;
                                display: flex;
                                align-items: center;
                                height: 80px;
                              "
                            >
                              <p class="toolDes">{{ tool.description }}</p>
                            </div>
                          </div>
                        </Card>
                        <Card dis-hover class="toolCardFoot">
                          <div class="toolTags">
                            <div
                              style="position: absolute; top: -5px; left: -10px"
                              v-if="tool.toolSet == false"
                            >
                              <span class="toolTypeSpan">
                                {{ tool.backendType }}
                              </span>
                              <span
                                v-for="(tag, index) in tool.tags"
                                :key="tag"
                                class="toolTagSpan"
                              >
                                {{ tool.tags[index] }}
                              </span>
                            </div>
                            <div
                              style="position: absolute; top: -5px; left: -10px"
                              v-if="tool.toolSet == true"
                            >
                              <span class="toolsetTypeSpan"> Toolset </span>
                              <span
                                v-for="(tag, index) in tool.tags"
                                :key="tag"
                                class="toolTagSpan"
                              >
                                {{ tool.tags[index] }}
                              </span>
                            </div>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </Row>
                </div>
              </vue-scroll>
            </div>
          </Card>
        </Row>
      </Col>
    </Row>
    <!--    Modal 对话框部分 -->
    <Modal
      v-model="chooseCreateModal"
      title="Which one to create"
      width="800"
      footer-hide
    >
      <Row
        type="flex"
        justify="space-around"
        class="code-row-bg"
        style="margin-top: 10px; margin-bottom: 15px"
      >
        <Col span="10">
          <Card style="min-height: 350px">
            <div style="text-align: center">
              <img
                src="../../../assets/images/tool.jpg"
                style="height: 150px"
              />
            </div>
            <div style="margin: 10px 0; text-align: center">
              <h3>Tool</h3>
            </div>
            <div style="min-height: 85px; margin-left: 10%">
              <ul>
                <li>Single-tool type</li>
                <li>Create tool directly</li>
                <li>
                  Flexible combination, you can choose the tool to add to a
                  toolset or a workspace
                </li>
              </ul>
            </div>
            <div
              style="text-align: center; margin-top: 5%; margin-bottom: 10px"
            >
              <h3>
                Select this type ->
                <a @click="createToolModalShow()">Create</a>
              </h3>
            </div>
          </Card>
        </Col>
        <Col span="10">
          <Card style="min-height: 350px">
            <div style="text-align: center">
              <img
                src="../../../assets/images/toolset.jpg"
                style="height: 150px"
              />
            </div>
            <div style="margin: 10px 0; text-align: center">
              <h3>Toolset</h3>
            </div>
            <div style="min-height: 85px; margin-left: 10%">
              <ul>
                <li>Multi-tools type</li>
                <li>Create a toolset to store tools</li>
                <li>
                  Convenient to use, you can directly import the toolset into an
                  activity
                </li>
              </ul>
            </div>
            <div
              style="text-align: center; margin-top: 5%; margin-bottom: 10px"
            >
              <h3>
                Select this type ->
                <a @click="createToolsetModalShow()">Create</a>
              </h3>
            </div>
          </Card>
        </Col>
      </Row>
    </Modal>

    <Modal v-model="createToolsetModal" title="Create toolset" width="800">
      <template-general-toolset
        @generalInfo="getGeneralInfo"
        :step="currentStep"
        :info="toolInfo"
      ></template-general-toolset>
      <div slot="footer">
        <Button
          @click="previousStep"
          v-show="this.currentStep == 1"
          type="warning"
          >Previous
        </Button>
        <Button v-show="this.currentStep == 0" @click="nextStep" type="primary"
          >Next
        </Button>
        <Button
          type="success"
          @click="createTool"
          :loading="loading"
          v-show="this.currentStep == 1"
          >Create
        </Button>
      </div>
    </Modal>

    <Modal v-model="createToolModal" title="Create tool" width="800">
      <template-general
        @generalInfo="getGeneralInfo"
        :step="currentStep"
        :info="toolInfo"
      ></template-general>
      <div slot="footer">
        <Button
          @click="previousStep"
          v-show="this.currentStep == 1"
          type="warning"
          >Previous
        </Button>
        <Button v-show="this.currentStep == 0" @click="nextStep" type="primary"
          >Next
        </Button>
        <Button
          type="success"
          @click="createTool"
          :loading="loading"
          v-show="this.currentStep == 1"
          >Create
        </Button>
      </div>
    </Modal>

    <Modal v-model="shareToolModal" width="800" title="Share tool">
      <Form>
        <FormItem>
          <Select v-model="formItem.select">
            <Option value="selectProject"
              >Share "{{ this.sharedTool.toolName }}" to Project
            </Option>
            <!--            <Option value="selectUser"-->
            <!--            >Share "{{ this.sharedTool.toolName }}" to User-->
            <!--            </Option-->
            <!--            >-->
          </Select>
        </FormItem>
        <FormItem v-if="formItem.select == 'selectProject'">
          <Select
            v-model="formItem.sharedProjectId"
            placeholder="Select Project"
          >
            <Option
              v-for="(item, index) in userProject"
              :value="item.aid"
              :key="index"
              >{{ item.name }}
            </Option>
          </Select>
        </FormItem>
        <FormItem v-if="formItem.select == 'selectUser'">
          <Input
            v-model="formItem.sharedUserEmail"
            placeholder="Enter email address"
          ></Input>
        </FormItem>
      </Form>

      <div slot="footer">
        <Button type="warning" @click="shareToolModal = false">Cancel</Button>
        <Button type="success" @click="sharingTool">Share</Button>
      </div>
    </Modal>

    <Modal
      v-model="editToolModal"
      title="Edit tool"
      width="800"
      :mask-closable="false"
    >
      <template-edit
        @generalInfo="getEditInfo"
        :step="currentStep"
        :selectTool="editToolInfo"
      ></template-edit>
      <div slot="footer">
        <Button
          type="warning"
          @click="previousStep"
          v-show="this.currentStep == 1"
          >Previous
        </Button>
        <Button type="primary" @click="nextStep" v-show="this.currentStep == 0"
          >Next
        </Button>
        <Button
          type="success"
          @click="commitEdit"
          class="create"
          v-show="this.currentStep == 1"
          >Commit
        </Button>
      </div>
    </Modal>

    <Modal
      v-model="manageToolModal"
      title="Manage tools"
      width="800"
      :mask-closable="false"
    >
      <Row>
        <Col span="16">
          <Card dis-hover style="margin-left: 10px">
            <div slot="title">
              <h2>Personal Tools</h2>
            </div>
            <div style="height: 400px">
              <vue-scroll :ops="ops" style="height: 400px">
                <Row>
                  <draggable
                    element="ul"
                    :options="{ group: 'tool' }"
                    v-model="tempPersonalToolsList"
                  >
                    <Col
                      span="6"
                      v-for="tool in tempPersonalToolsList"
                      :key="tool.toolName"
                    >
                      <Card
                        style="
                          background-color: ghostwhite;
                          margin: 0 5px 10px 5px;
                          height: 110px;
                        "
                      >
                        <div style="text-align: center; overflow: hidden">
                          <Tooltip placement="bottom" max-width="600">
                            <img
                              :src="tool.toolImg"
                              v-if="
                                tool.toolImg != undefined && tool.toolImg != ''
                              "
                              style="height: 100%; max-height: 50px"
                            />
                            <avatar
                              :username="tool.toolName"
                              :size="50"
                              style="margin-bottom: 6px"
                              v-else
                            ></avatar>
                            <div slot="content">
                              <span>{{ tool.description }}</span>
                              <template
                                v-if="
                                  tool.tags != undefined && tool.tags.length > 0
                                "
                              >
                                <br />
                                <p>
                                  <i>{{ tool.tags.join(" | ") }}</i>
                                </p>
                              </template>
                            </div>
                          </Tooltip>
                          <h4
                            :title="tool.toolName"
                            style="
                              text-align: left;
                              margin-left: 10px;
                              display: block;
                              width: 90px;
                              overflow: hidden;
                              white-space: nowrap;
                              text-overflow: ellipsis;
                            "
                          >
                            {{ tool.toolName }}
                          </h4>
                        </div>
                      </Card>
                    </Col>
                  </draggable>
                </Row>
              </vue-scroll>
            </div>
          </Card>
        </Col>
        <Col span="8">
          <Card dis-hover style="margin-left: 10px">
            <h2 slot="title">Tools in this Toolset</h2>
            <div style="height: 400px">
              <vue-scroll :ops="ops" style="height: 400px">
                <draggable
                  element="ul"
                  :group="{ name: 'toolset', put: true, pull: false }"
                  v-model="toolsetToolList"
                  @add="addTooltoToolset"
                  style="min-height: 400px"
                >
                  <Card
                    v-for="(tool, index) in toolsetToolList"
                    :key="tool.toolName"
                    class="stepItems"
                    style="margin: 0 0 5px 0"
                  >
                    <div>
                      <span style="font-weight: 600">{{ tool.toolName }}</span>
                      <Button
                        shape="circle"
                        icon="md-remove"
                        class="changeRedColor"
                        size="small"
                        style="float: right"
                        title="Reomve this tool"
                        @click="removeTool(index)"
                      ></Button>
                    </div>
                  </Card>
                </draggable>
              </vue-scroll>
            </div>
          </Card>
        </Col>
      </Row>

      <div slot="footer">
        <Button type="warning" @click="manageToolModal = false">Cancel</Button>
        <Button type="success" @click="confirmManageTool">Ok</Button>
      </div>
    </Modal>

    <!--    确认删除 Modal-->
    <Modal v-model="confirmDelModal" width="300">
      <label style="font-size: 20px">Are you sure to delete </label>
      <h2>{{ this.delToolInfo.delToolName }}</h2>
      <div slot="footer">
        <Button type="warning" @click="confirmDelModal = false">Cancel</Button>
        <Button type="success" @click="confirmDelTool">Ok</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import TemplateGeneral from "../../tools/TemplateGeneral";
import TemplateGeneralToolset from "../../tools/TemplateGeneralToolset";
import { del, post } from "../../../axios";
import Avatar from "vue-avatar";
import TemplateEdit from "../../tools/TemplateEdit";
import todoList from "./todoList";
import draggable from "vuedraggable";

export default {
  name: "tool",
  components: {
    Avatar,
    TemplateGeneral,
    TemplateGeneralToolset,
    TemplateEdit,
    draggable,
  },
  data() {
    return {
      checkedType: ["public", "private"],
      toolAndToolset: ["tool", "toolset"],
      shareToolModal: false,
      chooseCreateModal: false,
      createToolModal: false,
      createToolsetModal: false,
      manageToolModal: false,
      editToolModal: false,
      confirmDelModal: false,
      useToolCSS: true,
      personalToolsList: [],
      tempPersonalToolsList: [],
      toolsetToolList: [],
      personalTools: [],
      publicTools: [],
      privateTools: [],
      publicToolsets: [],
      privateToolsets: [],
      userProject: [],
      ops: {
        bar: {
          background: "#808695",
        },
      },
      formItem: {
        select: "selectProject",
        sharedUserEmail: "",
        sharedProjectId: "",
      },
      toolInfo: {
        toolName: "",
        modelInfo: {
          stateId: "",
          oid: "",
          mdlId: "",
        },
        description: "",
        toolUrl: "",
        recomStep: [],
        categoryTag: [],
        toolImg: "",
        privacy: "Private",
        detail: "",
      },
      editToolInfo: {},
      manageToolInfo: {},
      toolInfoRule: {},
      // create tool step
      currentStep: 0,
      delToolInfo: {
        delToolId: "",
        delToolIndex: undefined,
        delToolType: "",
        delToolName: "",
        delToolSet: Boolean,
      },
      description: "",
      toolUrl: "",
      recomStep: [],
      categoryTag: [],
      toolImg: "",
      privacy: "Private",
      detail: "",
      sharedTool: {},
      contentHeight: "",
      loading: false,
    };
  },
  created() {
    this.getUserProjects();
  },
  mounted() {
    this.getPersonalTools();
    this.resizeContent();
    this.reSize();
    window.addEventListener("resize", this.reSize);
  },
  beforeDestroy: function () {
    window.removeEventListener("resize", this.reSize);
  },
  methods: {
    resizeContent() {
      if (window.innerHeight > 675) {
        this.contentHeight = window.innerHeight - 120;
      } else {
        this.contentHeight = 555;
      }
      window.onresize = () => {
        return (() => {
          this.resizeContent();
        })();
      };
    },
    reSize() {
      // if (window.innerHeight > 675) {
      //   this.contentHeight = window.innerHeight - 120 + "px";
      // } else {
      //   this.contentHeight = 675 - 120 + "px";
      // }
      if (window.innerWidth < 950) {
        this.useToolCSS = false;
      } else {
        this.useToolCSS = true;
      }
    },

    getUserProjects: function () {
      let projectIds = "";
      let userInfo = this.$store.getters.userInfo;
      if (userInfo.createdProjects != null) {
        for (let i = 0; i < userInfo.createdProjects.length; i++) {
          if (userInfo.joinedProjects != null) {
            projectIds += userInfo.createdProjects[i] + ",";
          } else if (userInfo.joinedProjects == null) {
            if (i != userInfo.createdProjects.length - 1) {
              projectIds += userInfo.createdProjects[i] + ",";
            } else {
              projectIds += userInfo.createdProjects[i];
            }
          }
        }
      }
      if (userInfo.joinedProjects != null) {
        for (let j = 0; j < userInfo.joinedProjects.length; j++) {
          if (j != userInfo.joinedProjects.length - 1) {
            projectIds += userInfo.joinedProjects[j] + ",";
          } else {
            projectIds += userInfo.joinedProjects[j];
          }
        }
      }
      this.$axios
        .get("/GeoProblemSolving/project/getProjects?aids=" + projectIds)
        .then((res) => {
          this.$set(this, "userProject", res.data.data);
        })
        .catch((err) => {
          this.$Message.error("Loading project failed.");
        });
    },

    getGeneralInfo: function (form) {
      this.toolInfo = form;
    },
    //与TemplateGeneral 双向绑定，子组件向夫组件传值使用方法
    getEditInfo: function (form) {
      this.editToolInfo = form;
    },
    editTool: function (item) {
      this.editToolModal = true;
      this.editToolInfo = item;
    },
    manageTool(item) {
      this.manageToolInfo = item;
      this.toolsetToolList = this.getToolList(item.toolList);
      let allToolList = JSON.parse(JSON.stringify(this.personalToolsList));
      if (item.toolList) {
        for (let i = 0; i < item.toolList.length; i++) {
          for (let j = 0; j < allToolList.length; j++) {
            if (allToolList[j].tid == item.toolList[i]) {
              allToolList.splice(j, 1);
            }
          }
        }
      }
      this.tempPersonalToolsList = allToolList;
      this.manageToolModal = true;
    },
    getPersonalTools: function () {
      // "/GeoProblemSolving/tool/findByProvider/" + this.$store.getters.userId
      this.$axios
        .get("/GeoProblemSolving/tool/provider/" + this.$store.getters.userId)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
          } else if (res.data.code == 0) {
            let tempTools = res.data.data;

            for (let i = 0; i < tempTools.length; i++) {
              if (
                tempTools[i].privacy == "Public" &&
                tempTools[i].toolSet == false
              ) {
                this.publicTools.push(tempTools[i]);
                this.personalToolsList.push(tempTools[i]);
              } else if (
                tempTools[i].privacy == "Private" &&
                tempTools[i].toolSet == false
              ) {
                this.privateTools.push(tempTools[i]);
                this.personalToolsList.push(tempTools[i]);
              } else if (
                tempTools[i].privacy == "Public" &&
                tempTools[i].toolSet == true
              ) {
                this.publicToolsets.push(tempTools[i]);
              } else if (
                tempTools[i].privacy == "Private" &&
                tempTools[i].toolSet == true
              ) {
                this.privateToolsets.push(tempTools[i]);
              }
            }
          }
        })
        .catch((err) => {
          this.$Message.error("Loading Fail.");
        });
    },

    nextStep: function () {
      if (this.currentStep == 0) {
        this.currentStep = 1;
      }
    },
    previousStep: function () {
      if (this.currentStep == 1) {
        this.currentStep = 0;
      }
    },
    createTool: async function () {
      let createToolForm = this.toolInfo;
      // console.log(createToolForm);
      if (createToolForm.backendType == "" || createToolForm.toolName == "") {
        this.$Notice.error({
          title: "Crate failed",
          desc: "Lack of necessary information",
        });
      } else {
        this.loading = true;
        createToolForm["provider"] = this.$store.getters.userId;
        // let data = await post("/GeoProblemSolving/tool", createToolForm);
        if (createToolForm.toolSet == true) {
          let formData = {};
          formData.toolName = createToolForm.toolsetName;
          formData.toolImg = createToolForm.toolsetImg;
          formData.recommendation = createToolForm.recomStep;
          formData.toolList = [];
          for (let i = 0; i < createToolForm.toolList.length; i++) {
            formData.toolList.push(createToolForm.toolList[i].tid);
          }
          formData.toolSet = true;
          formData.provider = createToolForm.provider;
          formData.privacy = createToolForm.privacy;
          formData.description = createToolForm.description;
          formData.tags = createToolForm.categoryTag;
          this.axios
            .post("/GeoProblemSolving/tool/toolset", formData)
            .then((res) => {
              if (res.data.code == 0) {
                this.createToolsetModal = false;
                this.editToolInfo = {};
                this.toolInfo = {};
                let toolData = res.data.data;
                if (toolData != null) {
                  if (toolData.privacy == "Public") {
                    this.publicToolsets.push(toolData);
                  } else if (toolData.privacy == "Private") {
                    this.privateToolsets.push(toolData);
                  }
                  this.currentStep = 0;
                  this.$Notice.success({ title: "Create successfully" });
                } else {
                  this.$Notice.error({
                    title: "Crate failed",
                    desc: "No Corresponding service",
                  });
                }
              }
              this.loading = false;
            })
            .catch((err) => {
              this.$Notice.error({ title: "Create failed" });
              this.loading = false;
            });
        } else {
          this.axios
            .post("/GeoProblemSolving/tool", createToolForm)
            .then((res) => {
              // ？？？ 判断返回值，进行下一步操作
              if (res.data.code == 0) {
                this.loading = false;
                this.createToolModal = false;
                this.editToolInfo = {};
                this.toolInfo = {};
                let toolData = res.data.data;
                this.personalToolsList.push(toolData);
                if (toolData != null) {
                  if (toolData.privacy == "Public") {
                    this.publicTools.push(toolData);
                  } else if (toolData.privacy == "Private") {
                    this.privateTools.push(toolData);
                  }
                  this.currentStep = 0;
                  this.$Notice.success({ title: "Create successfully" });
                } else {
                  this.$Notice.error({
                    title: "Crate failed",
                    desc: "No Corresponding service",
                  });
                }
              }else {
                this.$Notice.error({ title: "Create failed" });
              }
              this.loading = false;
            })
            .catch((err) => {
              this.$Notice.error({ title: "Create failed" });
              this.loading = false;
            });
        }
      }
    },
    delTool: function (toolId, toolType, index, toolName, toolSet) {
      this.confirmDelModal = true;
      this.delToolInfo.delToolId = toolId;
      this.delToolInfo.delToolIndex = index;
      this.delToolInfo.delToolType = toolType;
      this.delToolInfo.delToolName = toolName;
      this.delToolInfo.delToolSet = toolSet;
    },

    confirmDelTool: function () {
      // "/GeoProblemSolving/tool/delete?tid=" + this.delToolInfo.delToolId
      if (this.delToolInfo.delToolSet == false) {
        this.$axios
          .delete("/GeoProblemSolving/tool/" + this.delToolInfo.delToolId)
          .then((res) => {
            if (res.data.code == 0) {
              this.delCurrentTool();
              this.$Notice.success({ title: "Delete Success." });
              this.confirmDelModal = false;
            } else {
              this.$Message.error("Delete Fail! Try again.");
            }
          })
          .catch((err) => {
            this.$Message.error("Delete Fail.");
          });
      } else if (this.delToolInfo.delToolSet == true) {
        this.$axios
          .delete(
            "/GeoProblemSolving/tool/toolSet/" + this.delToolInfo.delToolId
          )
          .then((res) => {
            if (res.data.code == 0) {
              this.delCurrentTool();
              this.$Notice.success({ title: "Delete Success." });
              this.confirmDelModal = false;
            } else {
              this.$Message.error("Delete Fail! Try again.");
            }
          })
          .catch((err) => {
            this.$Message.error("Delete Fail.");
          });
      }
    },
    delCurrentTool() {
      let id = this.delToolInfo.delToolId;
      let type = this.delToolInfo.delToolType;
      let toolset = this.delToolInfo.delToolSet;
      let index = 0;
      if (type == "Public") {
        if (toolset == true) {
          index = this.getIndex(this.publicToolsets, id);
          this.publicToolsets.splice(index, 1);
        } else if (toolset == false) {
          index = this.getIndex(this.publicTools, id);
          this.publicTools.splice(index, 1);
        }
      } else if (type == "Private") {
        if (toolset == true) {
          index = this.getIndex(this.privateToolsets, id);
          this.privateToolsets.splice(index, 1);
        } else if (toolset == false) {
          index = this.getIndex(this.privateTools, id);
          this.privateTools.splice(index, 1);
        }
      }
    },
    getIndex(list, id) {
      for (let i = 0; i < list.length; i++) {
        if (list[i].tid == id || list[i].tsid == id) {
          return i;
        }
      }
    },
    addTooltoToolset(evt) {
      let addedToolId = this.toolsetToolList[evt.newDraggableIndex].tid;
      for (var i = 0; i < this.tempPersonalToolsList.length; i++) {
        if (this.tempPersonalToolsList[i].tid == addedToolId) {
          this.tempPersonalToolsList.splice(i, 1);
          break;
        }
      }
    },
    removeTool(index) {
      let removeToolInfo = this.toolsetToolList[index];
      this.toolsetToolList.splice(index, 1);
      this.tempPersonalToolsList.push(removeToolInfo);
    },
    confirmManageTool() {
      let toolList = this.toolsetToolList;
      let formData = {};
      formData.tid = this.manageToolInfo.tid;
      formData.toolList = [];
      formData.toolSet = true;
      for (let i = 0; i < toolList.length; i++) {
        formData.toolList.push(toolList[i].tid);
      }
      this.$axios
        .put("/GeoProblemSolving/tool", formData)
        .then((res) => {
          if (res.data.code == 0) {
            let toolsetInfo = res.data.data;
            this.manageToolModal = false;
            if (toolsetInfo.privacy == "Public") {
              for (let i = 0; i < this.publicToolsets.length; i++) {
                if (this.publicToolsets[i].tid == toolsetInfo.tid) {
                  this.publicToolsets[i].toolList = toolsetInfo.toolList;
                }
              }
            } else {
              for (let i = 0; i < this.privateToolsets.length; i++) {
                if (this.privateToolsets[i].tid == toolsetInfo.tid) {
                  this.privateToolsets[i].toolList = toolsetInfo.toolList;
                }
              }
            }
            // this.$Notice.success({ title: "Update successfully." });
          }
        })
        .catch((err) => {
          this.$Message.error("Update Fail.Try again!");
        });
    },
    commitEdit: function () {
      // "/GeoProblemSolving/tool/update/" + this.editToolInfo.tid,
      //   this.editToolInfo
      //...
      //
      //
      //
      //
      //...接口未定
      if (this.editToolInfo.toolSet == false) {
        this.$axios
          .put("/GeoProblemSolving/tool", this.editToolInfo)
          .then((res) => {
            this.$Notice.success({ title: "Update successfully." });
            this.currentStep = 0;
            // this.editToolInfo = {};
            this.editToolModal = false;
          })
          .catch((err) => {
            this.$Message.error("Update Fail.Try again!");
          });
      } else {
        this.$axios
          .put("/GeoProblemSolving/tool", this.editToolInfo)
          .then((res) => {
            this.$Notice.success({ title: "Update successfully." });
            this.currentStep = 0;
            // this.editToolInfo = {};
            this.editToolModal = false;
          })
          .catch((err) => {
            this.$Message.error("Update Fail.Try again!");
          });
      }
    },
    shareTool: function (tool) {
      this.shareToolModal = true;
      this.sharedTool = tool;
    },
    sharingTool: function () {
      let updateProject = {};
      let index = null;
      for (let i = 0; i < this.userProject.length; i++) {
        if (this.userProject[i].aid == this.formItem.sharedProjectId) {
          updateProject.toolList = this.userProject[i].toolList;
          index = i;
        }
      }
      if (updateProject.toolList != null) {
        updateProject.toolList.push(this.sharedTool.tid);
      } else {
        updateProject.toolList = [this.sharedTool.tid];
      }
      this.$axios
        .put(
          "/GeoProblemSolving/project/" + this.formItem.sharedProjectId,
          updateProject
        )
        .then((res) => {
          if (res.data.code == 0) {
            this.$Notice.success({ desc: "Sharing successfully." });
            this.shareToolModal = false;

            this.userProject[index].toolList = updateProject.toolList;
          } else {
            this.$Message.error("Sharing Failed.");
          }
        })
        .catch((err) => {
          this.$Message.error("Update Fail.Try again!");
        });
    },
    createToolModalShow() {
      this.chooseCreateModal = false;
      this.toolInfo = {
        toolName: "",
        modelInfo: {
          stateId: "",
          oid: "",
          mdlId: "",
        },
        description: "",
        toolUrl: "",
        recomStep: [],
        categoryTag: [],
        toolImg: "",
        privacy: "Private",
        detail: "",
      };
      this.createToolModal = true;
    },
    createToolsetModalShow() {
      this.chooseCreateModal = false;
      this.toolInfo = {
        toolName: "",
        modelInfo: {
          stateId: "",
          oid: "",
          mdlId: "",
        },
        description: "",
        toolUrl: "",
        recomStep: [],
        categoryTag: [],
        toolImg: "",
        privacy: "Private",
        detail: "",
      };
      this.createToolsetModal = true;
    },
    getToolList(list) {
      let toolsInToolset = [];
      if (list) {
        for (let i = 0; i < list.length; i++) {
          for (let j = 0; j < this.personalToolsList.length; j++) {
            if (list[i] == this.personalToolsList[j].tid) {
              toolsInToolset.push(this.personalToolsList[j]);
            }
          }
        }
      }
      return toolsInToolset;
    },
  },
  computed: {
    userToolCount: function () {
      if (this.checkedType.length == 0 || this.toolAndToolset.length == 0) {
        this.personalTools = [];
        return this.personalTools;
      } else if (this.checkedType.length == 2) {
        this.personalTools = [];
        if (this.toolAndToolset.length == 0) {
          this.personalTools = [];
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "tool"
        ) {
          this.personalTools = this.publicTools.concat(this.privateTools);
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "toolset"
        ) {
          this.personalTools = this.publicToolsets.concat(this.privateToolsets);
        } else if (this.toolAndToolset.length == 2) {
          this.personalTools = this.publicToolsets
            .concat(this.privateToolsets)
            .concat(this.publicTools)
            .concat(this.privateTools);
        }
        return this.personalTools;
      } else if (
        this.checkedType.length == 1 &&
        this.checkedType[0] == "public"
      ) {
        this.personalTools = [];
        if (this.toolAndToolset.length == 0) {
          this.personalTools = [];
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "tool"
        ) {
          this.personalTools = this.publicTools;
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "toolset"
        ) {
          this.personalTools = this.publicToolsets;
        } else if (this.toolAndToolset.length == 2) {
          this.personalTools = this.publicToolsets.concat(this.publicTools);
        }
        return this.personalTools;
      } else if (
        this.checkedType.length == 1 &&
        this.checkedType[0] == "private"
      ) {
        this.personalTools = [];
        if (this.toolAndToolset.length == 0) {
          this.personalTools = [];
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "tool"
        ) {
          this.personalTools = this.privateTools;
        } else if (
          this.toolAndToolset.length == 1 &&
          this.toolAndToolset[0] == "toolset"
        ) {
          this.personalTools = this.privateToolsets;
        } else if (this.toolAndToolset.length == 2) {
          this.personalTools = this.privateToolsets.concat(this.privateTools);
        }
        return this.personalTools;
      }
    },
  },
};
</script>

<style scoped>
.customCard {
  opacity: 0.95;
}

.toolCard {
  background-color: white;
  border-radius: 3px;
  border: 1px solid #dadce0;
  border-bottom: transparent;
  box-shadow: 0 0.6px 1.8px 0 rgb(0 0 0 / 11%);
  height: 160px;
}

/* .toolCardHead{
    height: 45px;
    width: 100%;
    border-bottom-style: none;
    background-image: linear-gradient(to right, rgb(253, 249, 244) 0%, rgb(216, 244, 249) 100%);
  } */
.changeGreenColor:hover {
  background-color: #19be6b;
  color: white;
}
.changeRedColor:hover {
  background-color: #ed4014;
  color: white;
}

.toolCardFoot {
  height: 40px;
  background-color: rgba(232, 232, 232, 0.5);
  border-radius: 3px;
  border: 1px solid #dadce0;
  border-top: transparent;
}

.toolTags {
  position: relative;
}

.toolTypeSpan {
  background-color: #2db7f5;
  color: white;
  font-size: 15px;
  font-weight: 700;
  margin-left: 10px;
  line-height: 1;
  padding: 3px 7px;
  white-space: nowrap;
  vertical-align: baseline;
  border-radius: 10px;
}

.toolsetTypeSpan {
  background-color: #19be6b;
  color: white;
  font-size: 15px;
  font-weight: 700;
  margin-left: 10px;
  line-height: 1;
  padding: 3px 7px;
  white-space: nowrap;
  vertical-align: baseline;
  border-radius: 10px;
}

.toolTagSpan {
  background-color: #95a0ba;
  color: white;
  font-size: 15px;
  font-weight: 700;
  margin-left: 10px;
  line-height: 1;
  padding: 3px 7px;
  white-space: nowrap;
  vertical-align: baseline;
  border-radius: 10px;
}

.badge {
  display: inline-block;
  min-width: 10px;
  padding: 3px 7px;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
  color: #ffffff;
  text-align: center;
  white-space: nowrap;
  vertical-align: baseline;
  background-color: #999999;
  border-radius: 10px;
}

.icon {
  cursor: pointer;
}

.icon:hover {
  color: #2d8cf0;
}

.ellipsis {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: top;
}

.toolDes {
  /* height: 100px; */
  /* line-height: 20px; */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
}
</style>

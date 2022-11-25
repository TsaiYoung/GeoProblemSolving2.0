<style scoped>
.link-protocol >>> .ivu-modal-body {
  padding: 0 16px 16px 16px;
}

.btnHoverGray:hover {
  background-color: #808695;
  color: white;
}

#steps {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f8f9;
  height: calc(100vh - 300px);
  width: calc(100vw - 400px);
}

.domain >>> .ti-input {
  display: inline-block;
  width: 655px;
  height: 32px;
  line-height: 1.5;
  padding: 4px 7px;
  font-size: 12px;
  border: 1px solid #dcdee2;
  border-radius: 4px;
  color: #515a6e;
  background-color: #fff;
  background-image: none;
  position: relative;
  cursor: text;
}

.domain >>> input {
  opacity: 0.5;
}
.res-protocol-type >>> .ivu-select-selection {
  height: 37px;
}
.res-protocol-type >>> .ivu-select-placeholder {
  height: 35px;
}
.res-protocol >>> .ti-input::-webkit-scrollbar {
  display: none;
}
.res-protocol >>> .ti-input {
  display: inline-block;
  width: 267px;
  height: 39px;
  line-height: 1.5;
  padding: 4px 7px;
  font-size: 12px;
  border: 1px solid #dcdee2;
  border-radius: 4px;
  color: #515a6e;
  background-color: #fff;
  background-image: none;
  position: relative;
  cursor: text;
  overflow-x: scroll;
}
.res-protocol >>> .ti-tags {
  height: 30px;
}
.res-protocol >>> input {
  opacity: 0.5;
}

.inBox {
  position: relative;
  top: 20vh;
  right: 0;
  z-index: 999;
  background-color: red;
  width: 50px;
  height: 100px;
}
.popup {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 901;
}
</style>
<template>
  <Row>
    <Col span="24" style="margin-top: 20px">
      <div v-show="linkBuildModal" class="popup"></div>
      <div>
        <Row type="flex" justify="space-around">
          <div
            style="
              width: 80%;
              height: 25px;
              text-align: center;
              margin-left: 10%;
            "
          >
            <span style="font-weight: bold; font-size: 16px"
              >Procedure of the current activity</span
            >
            <!--            &lt;!&ndash;            todo: 添加头像&ndash;&gt;-->
            <!--            <div style="display: inline-block">-->
            <!--              <avatar-list :list="participants"></avatar-list>-->
            <!--            </div>-->
            <!-- 右侧三个按钮-->
            <template
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  'manage_child_activity'
                )
              "
            >
              <Button
                v-if="nodePositionBtn && procedureDrag"
                type="warning"
                @click="editPosition()"
                size="small"
                icon="md-git-commit"
                title="Adjust the position of nodes"
                style="float: right; margin-left: 10px"
                >Move node
              </Button>
              <Button
                v-else-if="nodePositionBtn && !procedureDrag"
                type="warning"
                @click="editPosition()"
                size="small"
                icon="md-git-network"
                title="Move the position of procedure"
                style="float: right; margin-left: 10px"
                >Move procedure
              </Button>
              <Button
                v-else
                type="default"
                size="small"
                icon="md-git-commit"
                title="Adjust the position of nodes"
                style="float: right; margin-left: 10px; cursor: default"
                >Move node
              </Button>
              <!--link按钮-->
              <Button
                v-if="removeLinkBtn"
                type="error"
                size="small"
                @click="removeLink()"
                icon="md-remove"
                title="Remove links"
                style="float: right; margin-left: 10px"
                >Unlink
              </Button>
              <Button
                v-else
                type="default"
                size="small"
                icon="md-remove"
                title="Please click and select one node (activity)"
                style="float: right; margin-left: 10px; cursor: default"
                >Unlink
              </Button>
              <!--link-->
              <template v-if="childActivities.length > 1">
                <Button
                  v-show="linkStep == 0"
                  type="info"
                  size="small"
                  @click="linkActivities()"
                  icon="md-link"
                  title="Start to link"
                  style="float: right; margin-left: 10px"
                  >Start to link
                </Button>
                <Button
                  v-if="linkBtn"
                  v-show="linkStep == 1"
                  type="success"
                  size="small"
                  @click="linkActivities()"
                  icon="md-link"
                  title="Complete linking"
                  style="float: right; margin-left: 10px"
                  >Complete linking
                </Button>
                <Button
                  v-else
                  v-show="linkStep == 1"
                  type="default"
                  size="small"
                  icon="md-link"
                  title="Complete linking"
                  style="float: right; margin-left: 10px; cursor: default"
                  >Complete linking
                </Button>
              </template>
              <template v-else>
                <Button
                  type="default"
                  size="small"
                  icon="md-link"
                  title="Start to link"
                  style="float: right; margin-left: 10px; cursor: default"
                  id="linkBegin"
                  >Start to link
                </Button>
              </template>
            </template>
          </div>
          <div id="steps"></div>

          <div
            v-if="
              Array.isArray(participants) &&
              participants.length != 0 &&
              collaborating &&
              permissionIdentity(
                activityInfo.permission,
                'manage_child_activity'
              )
            "
            style="margin-top: 20px"
          >
            <strong>Collaborating:</strong>
            <div
              v-for="(item, index) in collaboratingInfoList"
              :key="index"
              style="margin: 20px 0 10px"
            >
              <avatar-list
                v-if="item.collLinkUser.length > 0"
                :list="item.collLinkUser"
                :key="index"
                @click.native="joinLinkCollaboration(index)"
                style="cursor: pointer"
              ></avatar-list>
            </div>
          </div>
        </Row>
      </div>
    </Col>

    <Modal v-model="activityInfoModal" title="Information of the activity">
      <div>
        <label style="margin-left: 20px">Activity name:</label>
        <Input
          v-model="showActivityInfo.name"
          style="width: 300px; margin-left: 10px"
          readonly
        />
      </div>
      <div style="margin-top: 20px">
        <label style="margin-left: 20px">Activity purpose:</label>
        <Input
          v-model="showActivityInfo.purpose"
          style="width: 300px; margin-left: 17px"
          readonly
        />
      </div>
      <div slot="footer">
        <Button type="primary" @click="gotoActivity(showActivityInfo.aid)"
          >Go to this workspace
        </Button>
      </div>
    </Modal>
    <!--    Link Protocol modal -->
    <Modal
      class="link-protocol"
      v-model="linkBuildModal"
      title="Set link protocol"
      width="800"
      style="top: 100px; position: fixed; z-index: 902"
      :mask="false"
      ok-text="Link"
      cancel-text="Cancel"
      @on-visible-change="linkModalStatus"
    >
      <div slot="footer">
        <div style="display: flex; position: absolute; left: 30px">
          <span
            style="margin-right: 10px; font-size: 14px; vertical-align: top"
            v-if="collLinkUser.length > 1"
            >Online collaborating members:
          </span>
          <avatar-list
            :list="collLinkUser"
            v-if="collLinkUser.length > 1"
          ></avatar-list>
        </div>

        <div>
          <Button @click="cancelLink">Cancel</Button>
          <Button type="primary" @click="buildLink">Link</Button>
        </div>
      </div>
      <Divider orientation="left">Activity link</Divider>
      <div v-if="selectedActivities.length >= 2" style="margin: 0 20px">
        <div style="margin-bottom: 15px">
          <label>Type of activity relations:</label>
          <RadioGroup v-model="protocolType" @on-change="changeProtocolType">
            <Radio label="Sequence" style="margin-left: 20px"></Radio>
            <Radio label="Branch" style="margin-left: 20px"></Radio>
            <Radio label="Merger" style="margin-left: 20px"></Radio>
            <Radio label="Loop" style="margin-left: 20px"></Radio>
          </RadioGroup>
        </div>
        <!-- sequence -->
        <div v-if="protocolType == 'Sequence'">
          <template v-for="index in selectedActivities.length - 1">
            <div :key="index" style="margin-bottom: 5px">
              <span style="margin-right: 20px">
                <label>Start activity: </label>

                <Select
                  v-if="index == 1"
                  v-model="activityLinks[index - 1]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index - 1)"
                  clearable
                  @on-clear="activitySelectClear(index - 1)"
                >
                  <template v-for="item in otherNodes"
                    ><Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>

                <Select
                  v-else
                  v-model="activityLinks[index - 1]"
                  style="width: 200px"
                  placeholder="Select activity"
                  disabled
                >
                  <template v-for="item in otherNodes"
                    ><Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>
              </span>
              <Icon type="md-arrow-forward" style="margin-right: 20px" />
              <span>
                <label>End activity: </label>
                <Select
                  v-model="activityLinks[index]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index)"
                  clearable
                  @on-clear="activitySelectClear(index)"
                >
                  <template v-for="item in otherNodes">
                    <Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>
              </span>
              <Icon type="md-arrow-forward" style="margin: 0 20px" />
            </div>
          </template>
        </div>
        <!-- branch -->
        <div v-else-if="protocolType == 'Branch'" style="display: flex">
          <div style="width: 275px">
            <label>Start activity: </label>
            <Select
              v-model="activityLinks[0]"
              style="width: 200px"
              placeholder="Select activity"
              @on-change="activitySelect(0)"
              clearable
              @on-clear="activitySelectClear(0)"
            >
              <template v-for="item in otherNodes">
                <Option
                  :value="item.aid"
                  :key="item.id"
                  :class="item.aid"
                  :disabled="item.selected != undefined && item.selected"
                  >{{ item.name }}
                </Option>
              </template>
            </Select>
          </div>
          <div style="width: 360px">
            <template v-for="index in selectedActivities.length - 1">
              <div :key="index" style="margin-bottom: 5px">
                <Icon type="md-arrow-forward" style="margin: 0 20px" />
                <label>End activity: </label>
                <Select
                  v-model="activityLinks[index]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index)"
                  clearable
                  @on-clear="activitySelectClear(index)"
                >
                  <template v-for="item in otherNodes">
                    <Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}
                    </Option>
                  </template>
                </Select>
              </div>
            </template>
          </div>
        </div>
        <!-- merger -->
        <div v-else-if="protocolType == 'Merger'" style="display: flex">
          <div style="width: 335px">
            <template v-for="index in selectedActivities.length - 1">
              <div :key="index" style="margin-bottom: 5px">
                <label>Start activity: </label>
                <Select
                  v-model="activityLinks[index]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index)"
                  clearable
                  @on-clear="activitySelectClear(index)"
                >
                  <template v-for="item in otherNodes">
                    <Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}
                    </Option>
                  </template>
                </Select>
                <Icon type="md-arrow-forward" style="margin: 0 20px" />
              </div>
            </template>
          </div>
          <div style="width: 360px">
            <label>End activity: </label>
            <Select
              v-model="activityLinks[0]"
              style="width: 200px"
              placeholder="Select activity"
              @on-change="activitySelect(0)"
              clearable
              @on-clear="activitySelectClear(0)"
            >
              <template v-for="item in otherNodes">
                <Option
                  :value="item.aid"
                  :key="item.id"
                  :class="item.aid"
                  :disabled="item.selected != undefined && item.selected"
                  >{{ item.name }}
                </Option>
              </template>
            </Select>
          </div>
        </div>
        <!-- loop -->
        <div v-else-if="protocolType == 'Loop'">
          <template v-for="index in selectedActivities.length - 1">
            <div :key="index" style="margin-bottom: 5px">
              <span style="margin-right: 20px">
                <label>Start activity: </label>
                <Select
                  v-if="index == 1"
                  v-model="activityLinks[index - 1]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index - 1)"
                  clearable
                  @on-clear="activitySelectClear(index - 1)"
                >
                  <template v-for="item in otherNodes"
                    ><Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>
                <Select
                  v-else
                  v-model="activityLinks[index - 1]"
                  style="width: 200px"
                  placeholder="Select activity"
                  disabled
                >
                  <template v-for="item in otherNodes"
                    ><Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>
              </span>
              <Icon type="md-arrow-forward" style="margin-right: 20px" />
              <span>
                <label>End activity: </label>
                <Select
                  v-model="activityLinks[index]"
                  style="width: 200px"
                  placeholder="Select activity"
                  @on-change="activitySelect(index)"
                  clearable
                  @on-clear="activitySelectClear(index)"
                >
                  <template v-for="item in otherNodes">
                    <Option
                      :value="item.aid"
                      :key="item.id"
                      :class="item.aid"
                      :disabled="item.selected != undefined && item.selected"
                      >{{ item.name }}</Option
                    >
                  </template>
                </Select>
              </span>
              <Icon type="md-arrow-forward" style="margin: 0 20px" />
            </div>
          </template>
          <div style="margin-bottom: 5px">
            <span style="margin-right: 20px">
              <label>Start activity: </label>
              <Select
                v-model="activityLinks[selectedActivities.length - 1]"
                style="width: 200px"
                placeholder="Select activity"
                disabled
              >
                <template v-for="item in otherNodes"
                  ><Option :value="item.aid" :key="item.id" :class="item.aid">{{
                    item.name
                  }}</Option>
                </template>
              </Select>
            </span>
            <Icon type="md-arrow-forward" style="margin-right: 20px" />
            <span>
              <label>End activity: </label>
              <Select
                v-model="activityLinks[0]"
                style="width: 200px"
                placeholder="Select activity"
                disabled
              >
                <template v-for="item in otherNodes">
                  <Option :value="item.aid" :key="item.id" :class="item.aid">{{
                    item.name
                  }}</Option>
                </template>
              </Select>
            </span>
          </div>
        </div>
      </div>
      <Divider orientation="left">Person link</Divider>
      <div style="margin: 0 20px">
        <div style="display: flex; margin-bottom: 15px">
          <div style="margin-top: 5px; width: 150px">
            Type of person protocol:
          </div>
          <Select
            v-model="userProtocolForm.roleProtocol"
            style="width: 200px"
            placeholder="Select"
            @on-change="roleProtocolChange"
          >
            <Option value="None">None</Option>
            <Option value="All">All</Option>
            <Option value="Constraints">Constraints</Option>
          </Select>
        </div>
        <template v-if="userProtocolForm.roleProtocol === 'Constraints'">
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Role:</div>
            <Select
              v-model="userProtocolForm.linkRoles"
              multiple
              placeholder="Which roles of participants could join the next activity?"
              @on-change="roleChange"
            >
              <Option value="manager">Manager</Option>
              <Divider style="margin: 5px 0"></Divider>
              <Option value="core" disabled>Core team</Option>
              <Option value="researcher">Researcher</Option>
              <Option value="expert">Expert</Option>
              <Option value="decision-maker">Decision-maker</Option>
              <Option value="core-member">Core-member</Option>
              <Divider style="margin: 5px 0"></Divider>
              <Option value="ordinary" disabled>Ordinary team</Option>
              <Option value="stakeholder">Stakeholder</Option>
              <Option value="consultant">Consultant</Option>
              <Option value="ordinary-member">Ordinary-member</Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Domains:</div>
            <Select
              v-model="userProtocolForm.selectUserDomain"
              multiple
              :max-tag-count="4"
              placeholder="Which domains of participants could join the next activity?"
              @on-change="domainChange"
            >
              <Option
                v-for="(item, index) in userDomain"
                :value="item.name"
                :key="index"
                >{{ item.name }}
              </Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Organizations:</div>
            <Select
              v-model="userProtocolForm.selectUserOrg"
              multiple
              :max-tag-count="4"
              placeholder="Which organizations of participants could join the next activity?"
              @on-change="organizationChange"
            >
              <Option
                v-for="(item, index) in userOrganizations"
                :value="item.name"
                :key="index"
                >{{ item.name }}
              </Option>
            </Select>
          </div>
        </template>
      </div>

      <Divider orientation="left">Resource link</Divider>
      <div style="margin: 0 20px">
        <div style="display: flex; margin-bottom: 15px">
          <div style="width: 150px">Update automatically:</div>
          <i-switch
            v-model="resProtocolForm.autoUpdate"
            @on-change="autoUpdateChange"
          />
        </div>
        <div style="display: flex; margin-bottom: 15px">
          <div style="margin-top: 5px; width: 150px">
            Type of resource protocol:
          </div>
          <Select
            v-model="resProtocolForm.resProtocol"
            style="width: 200px"
            placeholder="Select"
            @on-change="resProtocolChange"
          >
            <Option value="None">None</Option>
            <Option value="All">All</Option>
            <Option value="Constraints">Constraints</Option>
          </Select>
        </div>
        <template v-if="resProtocolForm.resProtocol === 'Constraints'">
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px">Types:</div>
            <Select
              v-model="resProtocolForm.types"
              class="res-protocol-type"
              multiple
              :max-tag-count="2"
              placeholder="Type of resources"
              style="width: 267px; height: 39px"
              @on-change="resTypeChange"
            >
              <Option value="data">Data</Option>
              <Option value="paper">Papers</Option>
              <Option value="document">Documents</Option>
              <Option value="model">Models</Option>
              <Option value="image">Images</Option>
              <Option value="video">Videos</Option>
              <Option value="variable">Variables</Option>
              <Option value="others">Others</Option>
            </Select>
            <div
              style="
                margin-top: 5px;
                margin-left: 50px;
                width: 72px;
                height: 39px;
              "
            >
              Formats:
            </div>
            <vue-tags-input
              class="res-protocol"
              v-model="formats_tag"
              :tags="resProtocolForm.formats"
              :autocomplete-items="formatFilteredItems"
              @tags-changed="formatChange"
            />
          </div>
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px; height: 39px">
              Scales:
            </div>
            <vue-tags-input
              class="res-protocol"
              v-model="scales_tag"
              :tags="resProtocolForm.scales"
              :autocomplete-items="scaleFilteredItems"
              @tags-changed="scaleChange"
            />
            <div
              style="
                margin-top: 5px;
                margin-left: 50px;
                width: 72px;
                height: 39px;
              "
            >
              References:
            </div>
            <vue-tags-input
              class="res-protocol"
              v-model="references_tag"
              :tags="resProtocolForm.references"
              :autocomplete-items="referenceFilteredItems"
              @tags-changed="referenceChange"
            />
          </div>
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px; height: 39px">Units:</div>
            <vue-tags-input
              class="res-protocol"
              v-model="units_tag"
              :tags="resProtocolForm.units"
              :autocomplete-items="unitFilteredItems"
              @tags-changed="unitChange"
            />
            <div
              style="
                margin-top: 5px;
                margin-left: 50px;
                width: 72px;
                height: 39px;
              "
            >
              Concepts:
            </div>
            <vue-tags-input
              class="res-protocol"
              v-model="concepts_tag"
              :tags="resProtocolForm.concepts"
              :autocomplete-items="conceptFilteredItems"
              @tags-changed="conceptChange"
            />
          </div>
        </template>
      </div>
    </Modal>
    <Modal
      class="link-protocol"
      v-model="viewLinkModal"
      title="View link protocol"
      width="800"
      :styles="{ top: '30px' }"
    >
      <div slot="footer">
        <div>
          <Button type="primary" @click="viewLinkModal = false">Ok</Button>
        </div>
      </div>

      <Divider orientation="left">Person link</Divider>
      <div style="margin: 0 20px">
        <div style="display: flex; margin-bottom: 15px">
          <div style="margin-top: 5px; width: 150px">
            Type of person protocol:
          </div>
          <Select
            v-model="viewLinkProtocolForm.roleProtocol"
            style="width: 200px"
            placeholder="Select"
            disabled
          >
            <Option value="None">None</Option>
            <Option value="All">All</Option>
            <Option value="Constraints">Constraints</Option>
          </Select>
        </div>
        <template v-if="viewLinkProtocolForm.roleProtocol === 'Constraints'">
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Role:</div>
            <Select
              v-model="viewLinkProtocolForm.roles"
              multiple
              placeholder="Which roles of participants could join the next activity?"
              disabled
            >
              <Option value="manager">Manager</Option>
              <Divider style="margin: 5px 0"></Divider>
              <Option value="core" disabled>Core team</Option>
              <Option value="researcher">Researcher</Option>
              <Option value="expert">Expert</Option>
              <Option value="decision-maker">Decision-maker</Option>
              <Option value="core-member">Core-member</Option>
              <Divider style="margin: 5px 0"></Divider>
              <Option value="ordinary" disabled>Ordinary team</Option>
              <Option value="stakeholder">Stakeholder</Option>
              <Option value="consultant">Consultant</Option>
              <Option value="ordinary-member">Ordinary-member</Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Domains:</div>
            <Select
              v-model="viewLinkProtocolForm.domains"
              multiple
              :max-tag-count="4"
              placeholder="Which domains of participants could join the next activity?"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.domains"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 10px">
            <div style="margin-top: 10px; width: 100px">Organizations:</div>
            <Select
              v-model="viewLinkProtocolForm.organizations"
              multiple
              :max-tag-count="4"
              placeholder="Which organizations of participants could join the next activity?"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.organizations"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
          </div>
        </template>
      </div>

      <Divider orientation="left">Resource link</Divider>
      <div style="margin: 0 20px">
        <div style="display: flex; margin-bottom: 15px">
          <div style="width: 150px">Update automatically:</div>
          <i-switch v-model="viewLinkProtocolForm.autoUpdate" disabled />
        </div>
        <div style="display: flex; margin-bottom: 15px">
          <div style="margin-top: 5px; width: 150px">
            Type of resource protocol:
          </div>
          <Select
            v-model="viewLinkProtocolForm.resProtocol"
            style="width: 200px"
            placeholder="Select"
            disabled
          >
            <Option value="None">None</Option>
            <Option value="All">All</Option>
            <Option value="Constraints">Constraints</Option>
          </Select>
        </div>
        <template v-if="viewLinkProtocolForm.resProtocol === 'Constraints'">
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px">Types:</div>
            <Select
              v-model="viewLinkProtocolForm.types"
              multiple
              :max-tag-count="2"
              placeholder="Type of resources"
              style="width: 267px"
              disabled
            >
              <Option value="data">Data</Option>
              <Option value="paper">Papers</Option>
              <Option value="document">Documents</Option>
              <Option value="model">Models</Option>
              <Option value="image">Images</Option>
              <Option value="video">Videos</Option>
              <Option value="variable">Variables</Option>
              <Option value="others">Others</Option>
            </Select>
            <div style="margin-top: 5px; margin-left: 50px; width: 72px">
              Formats:
            </div>
            <Select
              v-model="viewLinkProtocolForm.formats"
              multiple
              style="width: 267px"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.formats"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px">Scales:</div>
            <Select
              v-model="viewLinkProtocolForm.scales"
              multiple
              style="width: 267px"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.scales"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
            <div style="margin-top: 5px; margin-left: 50px; width: 72px">
              References:
            </div>
            <Select
              v-model="viewLinkProtocolForm.references"
              multiple
              style="width: 267px"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.references"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
          </div>
          <div style="display: flex; margin-bottom: 15px">
            <div style="margin-top: 5px; width: 72px">Units:</div>
            <Select
              v-model="viewLinkProtocolForm.units"
              multiple
              style="width: 267px"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.units"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
            <div style="margin-top: 5px; margin-left: 50px; width: 72px">
              Concepts:
            </div>
            <Select
              v-model="viewLinkProtocolForm.concepts"
              multiple
              style="width: 267px"
              disabled
            >
              <Option
                v-for="(item, index) in viewLinkProtocolForm.concepts"
                :value="item"
                :key="index"
                >{{ item }}
              </Option>
            </Select>
          </div>
        </template>
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </Row>
</template>


<script>
// import "driver.js/dist/driver.min.css";
import echarts from "echarts";
import VueTagsInput from "@johmun/vue-tags-input";
import loginModal from "../../../user/userState/loginModal.vue";
import * as socketApi from "../../../../api/socket";
import avatarList from "../../../common/AvatarList";
import { sendSock } from "../../../../api/socket";
import { del } from "../../../../axios";
// import Driver from "driver.js";
export default {
  components: { VueTagsInput, loginModal, avatarList },
  props: ["activityInfo", "childActivities", "userInfo", "projectInfo"],
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      userRole: "visitor",
      //button
      linkBtn: false,
      removeLinkBtn: false,
      nodePositionBtn: false,
      //link
      viewLinkModal: false,
      viewLinkProtocolForm: {},
      // relation
      protocolType: "Sequence",
      linkStep: 0,
      otherNodes: [],
      //连接节点的顺序
      activityLinks: [],
      linkBuildModal: false,
      beginNode: {},
      endNode: {},
      //恢复登录的模态框
      tempLoginModal: false,

      userDomain: [],
      userOrganizations: [],
      userProtocolForm: {
        roleProtocol: "None",
        linkRoles: [],
        selectUserDomain: [],
        selectUserOrg: [],
      },

      // resource
      formats_tag: "",
      scales_tag: "",
      references_tag: "",
      units_tag: "",
      concepts_tag: "",
      resProtocolForm: {
        resProtocol: "None",
        autoUpdate: false,
        types: [],
        formats: [],
        scales: [],
        references: [],
        units: [],
        concepts: [],
      },
      //autoComplete 内容
      autoFormats: [{ text: "" }],
      autoScales: [{ text: "" }],
      autoReferences: [{ text: "" }],
      autoUnits: [{ text: "" }],
      autoConcepts: [{ text: "" }],

      // 添加/编辑step
      stepInfo: {
        aid: "",
        name: "",
        description: "",
        purpose: "",
      },

      stepInfoRule: {
        name: [
          { required: true, message: "Please enter name...", trigger: "blur" },
        ],
      },
      processStructure: [],
      workspaceName: "",
      typeList: [
        "Context definition & resource collection",
        "Data processing",
        "Data analysis",
        "Data visualization",
        "Geo-analysis model construction",
        "Model effectiveness evaluation",
        "Geographical simulation",
        "Decision making",
        "Other purpose",
      ],
      // 步骤逻辑图
      stepChart: null,
      // 选择的活动
      selectedActivities: [],
      // 双击展示活动信息
      activityInfoModal: false,
      showActivityInfo: {},
      // 工具
      personalTools: [],
      publicTools: [],
      personalToolsets: [],
      publicToolsets: [],
      selectActivityTools: [],
      selectActivityToolsets: [],
      // activity 结构信息
      procedureDrag: true,
      nodeData: [],

      resTypes: [],
      resSuffixes: [],
      socketId: "",
      restriction: {},

      // 是否有正在协同的内容
      collaborating: false,
      /*
          正在协同 link 的数组, id 由所选活动的aid构成
          linkId, activityLinks, otherNodes, linkRestriction
           */
      collaboratingInfoList: [],
      //协同 Link 的 id 数组，用于判断是否相同[[aid1, .....], [aid2,.....], [] ,[] ]
      collaboratingId: [],
      collaIndex: -1,
      // 正在进行 link 的活动
      linkingId: "",
      //processActivity 页面的成员
      participants: [],
      //正在 link 页面的成员
      collLinkUser: [],
    };
  },
  created() {
    this.init();
  },
  mounted() {
    this.getProcessSteps();
    this.showSteps();
    this.btnEnable();
    this.roleIdentity();
  },
  watch: {
    activityInfo: {
      immediate: true,
      handler() {
        this.socketId = `OperationServer/process${this.projectInfo.aid}/${this.activityInfo.aid}`;
      },
    },
    userProtocolForm: {
      immediate: true,
      deep: true,
      handler() {
        this.restriction = {
          userProtocol: this.userProtocolForm,
          resProtocol: this.resProtocolForm,
        };
      },
    },
    resProtocolForm: {
      immediate: true,
      deep: true,
      handler() {
        this.restriction = {
          userProtocol: this.userProtocolForm,
          resProtocol: this.resProtocolForm,
        };
      },
    },
    collaboratingInfoList: {
      deep: true,
      handler() {
        let linkIdArray = this.collaboratingInfoList.map((item) => {
          return item.linkId;
        });
        //[[aid1, aid2, aid3], [aid4, aid5, aid6], []]
        this.collaboratingId = linkIdArray.map((item) => {
          return item.split(",");
        });
        if (this.collaboratingInfoList.length > 0) {
          this.collaborating = true;
        }
        if (this.collaboratingInfoList.length == 0) {
          this.collaborating = false;
        }
      },
    },
    // linkBuildModal() {
    //   if (this.linkBuildModal) {
    //     this.linkingId = this.selectedActivities
    //       .map((item) => {
    //         return item.aid;
    //       })
    //       .toString();
    //     return;
    //   }
    //   let content = {
    //     behavior: "exitCollLink",
    //     linkingId: this.linkingId,
    //     userId: this.userInfo.userId,
    //   };
    //   this.sendLinkSock(content);
    //   //去除当前页面 collaborationInfoList 中的内容
    //   this.computeCollIndex(this.linkingId);
    //   if (this.collaIndex == -1) {
    //     return;
    //   }
    //   this.collaboratingInfoList[this.collaIndex].collLinkUser =
    //     this.collaboratingInfoList[this.collaIndex].collLinkUser.filter(
    //       (item) => item.userId != tempUserId
    //     );
    //   if (
    //     this.collaboratingInfoList[this.collaIndex].collLinkUser.length == 0
    //   ) {
    //     this.collaboratingInfoList.splice(this.collaIndex, 1);
    //   }
    //   this.initLinkForm();
    // },
  },

  beforeDestroy() {
    if (socketApi.getSocketInfo(this.socketId).linked) {
      socketApi.close(this.socketId);
    }
  },
  computed: {
    formatFilteredItems() {
      return this.autoFormats.filter((i) => {
        return (
          i.text.toLowerCase().indexOf(this.formats_tag.toLowerCase()) !== -1
        );
      });
    },
    scaleFilteredItems() {
      return this.autoScales.filter((i) => {
        return (
          i.text.toLowerCase().indexOf(this.scales_tag.toLowerCase()) !== -1
        );
      });
    },
    referenceFilteredItems() {
      return this.autoReferences.filter((i) => {
        return (
          i.text.toLowerCase().indexOf(this.references_tag.toLowerCase()) !== -1
        );
      });
    },
    unitFilteredItems() {
      return this.autoUnits.filter((i) => {
        return (
          i.text.toLowerCase().indexOf(this.units_tag.toLowerCase()) !== -1
        );
      });
    },
    conceptFilteredItems() {
      return this.autoConcepts.filter((i) => {
        return (
          i.text.toLowerCase().indexOf(this.concepts_tag.toLowerCase()) !== -1
        );
      });
    },
  },
  methods: {
    computeCollIndex(collLinkId) {
      let collLinkIdArr = collLinkId.split(",");
      for (let i = 0; i < this.collaboratingId.length; i++) {
        if (
          collLinkIdArr.every((item) => this.collaboratingId[i].includes(item))
        ) {
          this.collaIndex = i;
          return;
        }
      }
      this.collaIndex = -1;
    },
    //定制化开发内容
    sendLinkSock(content) {
      //为每个正在协同的 link 活动添加 id
      let sockMsg = {
        sender: this.userInfo.userId,
        senderName: this.userInfo.name,
        type: "general",
        content: content,
      };
      socketApi.sendSock(this.socketId, sockMsg, this.socketOnMessage);
    },
    socketOnMessage: function (messageJson) {
      let that = this;
      let content = messageJson.content;
      let type = messageJson.type;
      let senderName = messageJson.senderName;
      if (type == "general") {
        let behavior = content.behavior;
        if (behavior == "inActivities") {
          this.$Notice.info({
            title: `${senderName}  join the page of pathway.`,
          });
          //Send content if an active connection is in progress.
          if (this.linkBuildModal) {
            let senderId = messageJson.sender;
            let initLinkInfo = {
              behavior: "inLinkActivitiesInfo",
              linkingId: this.linkingId,
              activityLinks: this.activityLinks,
              otherNodes: this.otherNodes,
              selectActivities: this.selectedActivities,
              linkRestriction: this.restriction,
              collLinkUser: this.collLinkUser,
            };
            let sockMsg = {
              sender: this.userInfo.userId,
              senderName: this.userInfo.name,
              type: "general",
              receiver: senderId,
              content: initLinkInfo,
            };
            socketApi.sendSock(this.socketId, sockMsg, this.socketOnMessage);
          }
        } else if (behavior == "inLinkActivitiesInfo") {
          let tempLinkId = content.linkingId;
          delete content.linkingId;
          delete content.behavior;
          delete content.receivers;
          content["linkId"] = tempLinkId;

          this.computeCollIndex(tempLinkId);
          if (this.collaIndex == -1) {
            this.collaboratingInfoList.push(content);
            return;
          }
          this.collaboratingInfoList.splice(this.collaIndex, 1, content);
        } else if (behavior == "inLink") {
          //将接收到的 inLink 请求入库
          this.collaboratingInfoList.push(content.linkInfo);
        } else if (behavior == "exitCollLink") {
          let tempUserId = content.userId;
          if (this.linkingId == content.linkingId) {
            this.collLinkUser = this.collLinkUser.filter(
              (item) => item.userId != tempUserId
            );
          }
          this.computeCollIndex(content.linkingId);
          if (this.collaIndex == -1) {
            return;
          }
          this.collaboratingInfoList[this.collaIndex].collLinkUser =
            this.collaboratingInfoList[this.collaIndex].collLinkUser.filter(
              (item) => item.userId != tempUserId
            );
          if (
            this.collaboratingInfoList[this.collaIndex].collLinkUser.length == 0
          ) {
            this.collaboratingInfoList.splice(this.collaIndex, 1);
          }
        } else if (behavior == "joinCollLink") {
          //加入协同
          let tempCollLinker = content.collLinkUser;
          if (this.linkingId == content.linkingId) {
            this.collLinkUser = tempCollLinker;
          }
          this.computeCollIndex(content.linkingId);
          if (this.collaIndex == -1) {
            return;
          }
          this.collaboratingInfoList[this.collaIndex].collLinkUser =
            tempCollLinker;
        } else if (behavior == "bulk") {
          // 正在进行连接的则更新并删除等待队列
          if (this.linkingId == content.linkingId) {
            this.processUpdate();
            this.linkBuildModal = false;
          }
          this.computeCollIndex(content.linkingId);
          if (this.collaIndex == -1) {
            return;
          }
          //删除等待队列
          this.collaboratingInfoList.splice(this.collaIndex, 1);
        } else if (behavior == "removeLink") {
          this.processStructure = content.processStructure;
          this.updateStepChart();
        } else if (behavior == "setLink") {
          let param = content.param;
          switch (param) {
            case "protocolType":
              let tempType = content.protocolType;
              let collLinkId = content.linkingId;
              let tempSel = content.selectedActivities;
              //是正在进行协同的
              if (this.linkingId == collLinkId) {
                this.protocolType = tempType;
                this.otherNodes = JSON.parse(
                  JSON.stringify(this.selectedActivities)
                );
                this.activityLinks = this.selectedActivities.map((item) => {
                  return 0;
                });
              }
              //协同等待
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[this.collaIndex].protocolType =
                tempType;
              this.collaboratingInfoList[this.collaIndex].selectedActivities =
                tempSel;
              this.collaboratingInfoList[this.collaIndex].otherNodes =
                JSON.parse(JSON.stringify(tempSel));
              this.activityLinks = tempSel.map((item) => {
                return 0;
              });
              break;
            case "activityLinks":
              let tempLinks = content.activityLinks;
              let tempOtherNodes = content.otherNodes;
              if (this.linkingId == content.linkingId) {
                this.activityLinks = tempLinks;
                this.otherNodes = tempOtherNodes;
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[this.collaIndex].activityLinks =
                tempLinks;
              this.collaboratingInfoList[this.collaIndex].otherNodes =
                tempOtherNodes;
              break;
            case "roleProtocol":
              let tempRole = content.roleProtocol;
              if (this.linkingId == content.linkingId) {
                if (tempRole == "Constraints") {
                  this.userDomain = content.userDomain;
                  this.userOrganizations = content.userOrganization;
                }
                this.$set(this.userProtocolForm, "roleProtocol", tempRole);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].userProtocolForm.roleProtocol = tempRole;
              if (tempRole == "Constraints") {
                this.collaboratingInfoList[this.collaIndex].userDomains =
                  content.userDomain;
                this.collaboratingInfoList[this.collaIndex].userOrganizations =
                  content.userOrganizations;
              }
              break;
            case "userRole":
              let tempRoles = content.userRole;
              if (this.linkingId == content.linkingId) {
                this.$set(this.userProtocolForm, "linkRoles", tempRoles);
              } else {
                this.computeCollIndex(content.linkingId);
                if (this.collaIndex == -1) {
                  return;
                }
                this.collaboratingInfoList[
                  this.collaIndex
                ].userProtocolForm.linkRoles = tempRoles;
              }
              break;
            case "userDomain":
              let tempSelectDomain = content.selectUserDomain;
              if (this.linkingId == content.linkingId) {
                this.$set(
                  this.userProtocolForm,
                  "selectUserDomain",
                  tempSelectDomain
                );
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].userProtocolForm.selectUserDomain = tempSelectDomain;
              break;
            case "userOrganization":
              let tempSelectOrg = content.userSelectOrganization;
              if (this.linkingId == content.linkingId) {
                this.$set(
                  this.userProtocolForm,
                  "selectUserOrg",
                  tempSelectOrg
                );
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].userProtocolForm.selectUserOrg = tempOrg;
              break;
            case "autoUpdate":
              let tempAuto = content.autoUpdate;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "autoUpdate", tempAuto);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].resProtocolForm.autoUpdate = tempAuto;
              break;
            case "resProtocol":
              let tempProtocol = content.resProtocol;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "resProtocol", tempProtocol);
                let resMetaInfo = content.resMetaInfo;
                if (resMetaInfo !== undefined) {
                  if (resMetaInfo.format !== undefined) {
                    this.autoFormats = resMetaInfo.format;
                  }
                  if (resMetaInfo.scale !== undefined) {
                    this.autoScales = resMetaInfo.scale;
                  }
                  if (resMetaInfo.reference !== undefined) {
                    this.autoReferences = resMetaInfo.reference;
                  }
                  if (resMetaInfo.unit !== undefined) {
                    this.autoUnits = resMetaInfo.unit;
                  }
                  if (resMetaInfo.concept !== undefined) {
                    this.autoConcepts = resMetaInfo.concept;
                  }
                }
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.resProtocol = tempProtocol;
              break;
            case "resType":
              let tempTypes = content.resType;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "types", tempTypes);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.types = tempTypes;
              break;
            case "resFormat":
              let tempFormat = content.resFormat;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "formats", tempFormat);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.formats = tempFormat;
              break;
            case "resScale":
              let tempScale = content.resScale;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "scales", tempScale);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.scales = tempScale;
              break;
            case "resReference":
              let tempReference = content.resReference;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "references", tempReference);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.references = tempReference;
              break;
            case "resUnit":
              let tempUnit = content.resUnit;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "units", tempUnit);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.units = tempUnit;
              break;
            case "resConcept":
              let tempConcept = content.resConcept;
              if (this.linkingId == content.linkingId) {
                this.$set(this.resProtocolForm, "concepts", tempConcept);
              }
              this.computeCollIndex(content.linkingId);
              if (this.collaIndex == -1) {
                return;
              }
              this.collaboratingInfoList[
                this.collaIndex
              ].linkRestriction.resProtocol.concepts = tempConcept;
              break;
          }
        }
      } else if (type == "members") {
        this.participants = messageJson.participants;
      }
    },
    joinLinkCollaboration(index) {
      let collLink = this.collaboratingInfoList[index];
      this.selectedActivities = collLink.selectActivities;
      this.linkingId = this.selectedActivities
        .map((item) => {
          return item.aid;
        })
        .toString();
      this.otherNodes = collLink.otherNodes;
      this.activityLinks = collLink.activityLinks;
      this.resProtocolForm = collLink.linkRestriction.resProtocol;
      this.userProtocolForm = collLink.linkRestriction.userProtocol;
      let joiner = this.participants.filter(
        (item) => item.userId == this.userInfo.userId
      );
      this.collLinkUser = collLink.collLinkUser;
      this.collLinkUser.push(joiner[0]);
      let content = {
        behavior: "joinCollLink",
        linkingId: this.collaboratingInfoList[index].linkId,
        collLinkUser: this.collLinkUser,
      };
      this.sendLinkSock(content);
      this.linkBuildModal = true;
    },
    init() {
      window.addEventListener("resize", this.updateStepChart);
    },
    roleIdentity() {
      this.userRole = this.userRoleApi.roleIdentify(
        this.activityInfo.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, operation) {
      return this.userRoleApi.permissionIdentity(
        JSON.parse(permission),
        this.userRole,
        operation
      );
    },
    //退出协同的界面，当无人则删除
    linkModalStatus() {
      //点开则计算 linkingId
      if (this.linkBuildModal) {
        this.linkingId = this.selectedActivities
          .map((item) => {
            return item.aid;
          })
          .toString();
        return;
      }
      //退出协同
      let content = {
        behavior: "exitCollLink",
        linkingId: this.linkingId,
        userId: this.userInfo.userId,
      };
      this.sendLinkSock(content);
      //去除当前页面 collaborationInfoList 中的内容
      this.computeCollIndex(this.linkingId);
      if (this.collaIndex == -1) {
        return;
      }
      let tempUserId = this.userInfo.userId;
      this.collaboratingInfoList[this.collaIndex].collLinkUser =
        this.collaboratingInfoList[this.collaIndex].collLinkUser.filter(
          (item) => item.userId != tempUserId
        );
      if (
        this.collaboratingInfoList[this.collaIndex].collLinkUser.length == 0
      ) {
        this.collaboratingInfoList.splice(this.collaIndex, 1);
      }
      this.initLinkForm();
    },
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    updateStepChart() {
      // 重新渲染
      this.stepChart.dispose();
      this.stepChart = null;
      this.showSteps();
      this.btnEnable();
    },
    btnEnable() {
      if (
        this.processStructure.length <= 0 ||
        this.processStructure.length == undefined
      ) {
        this.removeLinkBtn = false;
        this.nodePositionBtn = false;
      } else {
        this.nodePositionBtn = true;
        if (this.selectedActivities.length == 0) {
          this.removeLinkBtn = false;
        } else if (this.selectedActivities.length == 1) {
          this.removeLinkBtn = false;
        } else if (this.selectedActivities.length >= 2) {
          if (this.linkStep == 1) {
            this.removeLinkBtn = false;
            this.linkBtn = true;
          } else if (
            this.linkStep == 0 &&
            this.selectedActivities.length == 2
          ) {
            let activity1 = this.selectedActivities[0];
            let activity2 = this.selectedActivities[1];
            this.linkBtn = false;
            if (activity1.next.contains(activity2)) {
              this.removeLinkBtn = true;
            } else {
              this.removeLinkBtn = false;
            }
          }
        }
      }
    },
    getProcessSteps() {
      if (this.activityInfo.pathway == undefined) {
        this.activityInfo.pathway = [];
        for (var i = 0; i < this.childActivities.length; i++) {
          // catagory of node
          var nodeCategory = this.getStepCategory(
            this.childActivities[i].purpose
          );
          // create step node
          let rows = Math.round(Math.sqrt(this.childActivities.length / 2)) * 2;
          var newStepNode = {
            id: i,
            aid: this.childActivities[i].aid,
            name: this.childActivities[i].name,
            category: nodeCategory,
            last: [],
            next: [],
            x: (i % rows) * 100,
            y: Math.floor(i / rows) * 100,
            status: true,
          };
          this.activityInfo.pathway.push(newStepNode);
        }
        this.processStructure = this.activityInfo.pathway;
        this.updatePathway();
      } else if (
        this.activityInfo.pathway.length < this.childActivities.length
      ) {
        for (
          var i = this.activityInfo.pathway.length;
          i < this.childActivities.length;
          i++
        ) {
          // catagory of node
          var nodeCategory = this.getStepCategory(
            this.childActivities[i].purpose
          );
          // create step node
          var newStepNode = {
            id: i,
            aid: this.childActivities[i].aid,
            name: this.childActivities[i].name,
            category: nodeCategory,
            last: [],
            next: [],
            x: (i % 5) * 100,
            y: Math.floor(i / 5) * 100,
            status: true,
          };
          this.activityInfo.pathway.push(newStepNode);
        }
        this.processStructure = this.activityInfo.pathway;
        this.updatePathway();
      } else if (
        this.activityInfo.pathway.length > this.childActivities.length
      ) {
        for (var i = this.activityInfo.pathway.length - 1; i >= 0; i--) {
          let exist = false;
          for (var j = 0; j < this.childActivities.length; j++) {
            if (
              this.activityInfo.pathway[i].aid == this.childActivities[j].aid
            ) {
              exist = true;
            }
          }
          if (!exist) {
            this.removePathwayNode(this.activityInfo.pathway[i].aid);
          }
        }
        this.processStructure = this.activityInfo.pathway;
        this.updatePathway();
      } else {
        this.processStructure = this.activityInfo.pathway;
      }
    },
    getProtocolInfo(last, next) {
      this.axios
        .get(
          `/GeoProblemSolving/activityDriven/${this.activityInfo.aid}/${last}/${next}`
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.viewLinkModal = true;
            this.viewLinkProtocolForm = res.data.data;
          }
        })
        .catch((e) => {});
    },
    showSteps() {
      let option = {
        animationDurationUpdate: 500,
        animationEasingUpdate: "quinticInOut",
        legend: {
          show: true,
          data: [
            {
              name: "Context definition & resource collection",
              icon: "circle",
            },
            {
              name: "Data processing",
              icon: "circle",
            },
            {
              name: "Data visualization",
              icon: "circle",
            },
            {
              name: "Geo-analysis model construction",
              icon: "circle",
            },
            {
              name: "Model effectiveness evaluation",
              icon: "circle",
            },
            {
              name: "Geographical simulation",
              icon: "circle",
            },
            {
              name: "Data analysis",
              icon: "circle",
            },
            {
              name: "Decision making",
              icon: "circle",
            },
            {
              name: "Other purpose",
              icon: "circle",
            },
          ],
        },
        series: [
          {
            id: "procedure",
            type: "graph",
            layout: "none",
            legendHoverLink: true,
            roam: this.procedureDrag,
            label: {
              normal: {
                show: true,
              },
            },
            edgeSymbol: ["circle", "arrow"],
            edgeSymbolSize: [4, 10],
            focusNodeAdjacency: true,
            data: [],
            categories: [
              {
                name: "Context definition & resource collection",
              },
              {
                name: "Data processing",
              },
              {
                name: "Data visualization",
              },
              {
                name: "Geo-analysis model construction",
              },
              {
                name: "Model effectiveness evaluation",
              },
              {
                name: "Geographical simulation",
              },
              {
                name: "Data analysis",
              },
              {
                name: "Decision making",
              },
              {
                name: "Other purpose",
              },
            ],
            links: [],
            lineStyle: {
              normal: {
                opacity: 1,
                width: 5,
                curveness: 0.1,
              },
            },
          },
        ],
      };
      this.nodeData = [];
      if (this.processStructure.length > 0) {
        for (var i = 0; i < this.processStructure.length; i++) {
          //get data
          let datum = {
            name: this.processStructure[i].name,
            index: this.processStructure[i].id,
            aid: this.processStructure[i].aid,
            x: this.processStructure[i].x,
            y: this.processStructure[i].y,
            category: this.processStructure[i].category,
            next: this.processStructure[i].next,
            last: this.processStructure[i].last,
            symbolSize: 45,
          };
          this.nodeData.push(datum);

          //get links
          for (var j = 0; j < this.processStructure[i].next.length; j++) {
            option.series[0].links.push({
              source: this.processStructure[i].name,
              sourceId: this.processStructure[i].aid,
              target: this.processStructure[i].next[j].name,
              targetId: this.processStructure[i].next[j].aid,
            });
          }
        }
      }
      option.series[0].data = this.nodeData;
      if (this.stepChart == null) {
        this.stepChart = echarts.init(document.getElementById("steps"));
      } else {
        this.stepChart.off("click");
        this.stepChart.off("dblclick");
      }
      this.stepChart.setOption(option);

      let _this = this;
      // 单击选择步骤
      this.stepChart.on("click", function (params) {
        if (_this.procedureDrag) {
          if (params.dataType == "edge") {
            let lastNode = params.data.sourceId;
            let nextNode = params.data.targetId;
            _this.getProtocolInfo(lastNode, nextNode);
          } else if (params.dataType == "node") {
            if (option.series[0].data[params.data.index].symbolSize == 45) {
              option.series[0].data[params.data.index].symbolSize = 60;

              // record the selected step nodes
              _this.selectedActivities.push({
                aid: params.data.aid,
                id: params.data.index,
                name: params.data.name,
                category: params.data.category,
                next: params.data.next,
                last: params.data.last,
              });
            } else if (
              option.series[0].data[params.data.index].symbolSize == 60
            ) {
              option.series[0].data[params.data.index].symbolSize = 45;

              // remove these not selected step nodes
              for (var i = 0; i < _this.selectedActivities.length; i++) {
                if (_this.selectedActivities[i].aid == params.data.aid) {
                  _this.selectedActivities.splice(i, 1);
                  break;
                }
              }
            }
            _this.stepChart.setOption(option);
            _this.btnEnable();
          }
        }
      });
      // 双击切换当前步骤
      this.stepChart.on("dblclick", function (params) {
        if (_this.procedureDrag) {
          _this.activityInfoModal = true;
          let purpose = _this.getStepType(params.data.category);
          let activity = {
            aid: params.data.aid,
            name: params.data.name,
            purpose: purpose,
          };
          _this.showActivityInfo = activity;
        }
      });
    },
    getStepType(category) {
      let purpose;
      if (category == 0) {
        purpose = "Context definition & resource collection";
      } else if (category == 1) {
        purpose = "Data processing";
      } else if (category == 2) {
        purpose = "Data visualization";
      } else if (category == 3) {
        purpose = "Geo-analysis model construction";
      } else if (category == 4) {
        purpose = "Model effectiveness evaluation";
      } else if (category == 5) {
        purpose = "Geographical simulation";
      } else if (category == 6) {
        purpose = "Data analysis";
      } else if (category == 7) {
        purpose = "Decision making";
      } else {
        purpose = "Other purpose";
      }
      return purpose;
    },
    getStepCategory(purpose) {
      let category;
      if (purpose == "Context definition & resource collection") {
        category = 0;
      } else if (purpose == "Data processing") {
        category = 1;
      } else if (purpose == "Data visualization") {
        category = 2;
      } else if (purpose == "Geo-analysis model construction") {
        category = 3;
      } else if (purpose == "Model effectiveness evaluation") {
        category = 4;
      } else if (purpose == "Geographical simulation") {
        category = 5;
      } else if (purpose == "Data analysis") {
        category = 6;
      } else if (purpose == "Decision making") {
        category = 7;
      } else {
        category = 8;
      }
      return category;
    },
    // move
    editPosition() {
      this.procedureDrag = !this.procedureDrag;
      // swith off the node dragging fuction
      if (this.procedureDrag) {
        this.updatePathway();
      }

      this.stepChart.setOption({
        animationDurationUpdate: this.procedureDrag ? 500 : 0,
        series: [
          {
            id: "procedure",
            roam: this.procedureDrag,
          },
        ],
      });

      // node的拖拽功能
      let _this = this;
      try {
        this.stepChart.setOption({
          // https://www.echartsjs.com/zh/tutorial.html#小例子：自己实现拖拽
          graphic: echarts.util.map(
            _this.nodeData,
            function (dataItem, dataIndex) {
              let x = dataItem.x;
              let y = dataItem.y;
              let item = [x, y];
              let nodePosition = _this.stepChart.convertToPixel(
                { seriesIndex: 0 },
                item
              );

              return {
                type: "circle",
                shape: {
                  r: 20,
                },
                position: nodePosition,
                invisible: true,
                draggable: !_this.procedureDrag,
                z: 100,
                ondrag: echarts.util.curry(function () {
                  let position = _this.stepChart.convertFromPixel(
                    { seriesIndex: 0 },
                    this.position
                  );
                  _this.nodeData[dataIndex].x = position[0];
                  _this.nodeData[dataIndex].y = position[1];
                  _this.processStructure[dataIndex].x = position[0];
                  _this.processStructure[dataIndex].y = position[1];
                  _this.stepChart.setOption({
                    series: [
                      {
                        id: "procedure",
                        data: _this.nodeData,
                      },
                    ],
                  });
                }, dataIndex),
              };
            }
          ),
        });
      } catch (ex) {
        this.$Notice.info({
          desc: "ERROR!",
        });
        tice;
      }
    },
    updatePathway() {
      let updateurl = "";
      if (this.activityInfo.level == 0) {
        updateurl = "/GeoProblemSolving/project/" + this.activityInfo.aid;
      } else if (this.activityInfo.level == 1) {
        updateurl = "/GeoProblemSolving/subproject/" + this.activityInfo.aid;
      } else if (this.activityInfo.level > 1) {
        updateurl = "/GeoProblemSolving/activity/" + this.activityInfo.aid;
      } else {
        return;
      }
      let data = {
        aid: this.activityInfo.aid,
        pathway: this.processStructure,
      };

      this.axios
        .put(updateurl, data)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.updateStepChart();
            this.selectedActivities = [];
            // this.$Notice.info({
            //   desc: "Reshape pathway successfully!",
            // });
          } else {
            this.$Message.error("Fail to reshape pathway.");
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    // remove
    removePathwayNode(aid) {
      if (this.activityInfo.pathway != undefined) {
        let nodeId = -1;
        for (var i = 0; i < this.activityInfo.pathway.length; i++) {
          if (this.activityInfo.pathway[i].aid == aid) {
            nodeId = this.activityInfo.pathway[i].id;
            // remove mode
            this.activityInfo.pathway.splice(i, 1);
          }
        }
        // normalize
        if (nodeId != -1) {
          for (var i = 0; i < this.activityInfo.pathway.length; i++) {
            let node = this.activityInfo.pathway[i];
            // node
            if (node.id > nodeId) {
              node.id = node.id - 1;
            }
            // last
            for (var j = 0; j < node.last.length; j++) {
              if (node.last[j].id == nodeId) {
                node.last.splice(j, 1);
              } else if (node.last[j].id > nodeId) {
                node.last[j].id = node.last[j].id - 1;
              }
            }
            // next
            for (var j = 0; j < node.next.length; j++) {
              if (node.next[j].id == nodeId) {
                node.next.splice(j, 1);
              } else if (node.next[j].id > nodeId) {
                node.next[j].id = node.next[j].id - 1;
              }
            }
          }
        }
      }
    },
    // link
    linkActivities() {
      if (this.linkStep == 0) {
        this.showSteps();
        this.selectedActivities = [];
        this.linkStep = 1;
        this.$Notice.info({
          desc: "Please select two nodes at least!",
        });
      } else if (this.linkStep == 1) {
        if (this.selectedActivities.length >= 2) {
          //正在进行连接的 id
          this.linkingId = this.selectedActivities
            .map((item) => {
              return item.aid;
            })
            .toString();
          let tempLinkId = this.linkingId;
          this.computeCollIndex(tempLinkId);
          if (this.collaIndex == -1) {
            this.changeProtocolType();
            this.linkBuildModal = true;
            // record and end
            this.linkStep = 0;

            let content = {
              behavior: "inLink",
              linkInfo: {
                linkId: tempLinkId,
                activityLinks: this.activityLinks,
                selectActivities: this.selectedActivities,
                otherNodes: this.otherNodes,
                linkRestriction: this.restriction,
                userDomains: this.userDomain,
                userOrganizations: this.userOrganizations,
                collLinkUser: this.participants.filter(
                  (item) => item.userId == this.userInfo.userId
                ),
              },
            };
            this.sendLinkSock(content);
            this.collLinkUser = this.participants.filter(
              (item) => item.userId == this.userInfo.userId
            );
            this.collaboratingInfoList.push(content.linkInfo);
          } else {
            //此几个选中的活动正在进行连接
            let linkingInfo = this.collaboratingInfoList[this.collaIndex];
            this.selectedActivities = linkingInfo.selectActivities;
            this.activityLinks = linkingInfo.activityLinks;
            this.otherNodes = linkingInfo.otherNodes;
            this.userProtocolForm = linkingInfo.linkRestriction.userProtocol;
            this.resProtocolForm = linkingInfo.linkRestriction.resProtocolForm;
            this.collLinkUser = linkingInfo.collLinkUser;
          }
        } else {
          this.$Notice.info({
            desc: "Please select two nodes and restart to link activities!",
          });
        }
      }
    },
    initLinkForm() {
      this.otherNodes = [];
      this.otherNodes = JSON.parse(JSON.stringify(this.selectedActivities));

      this.activityLinks = [];
      for (let i = 0; i < this.selectedActivities.length; i++) {
        this.activityLinks.push(0);
      }
      this.userDomain = [];
      this.userOrganizations = [];
      this.userProtocolForm = {
        roleProtocol: "None",
        linkRoles: [],
        selectUserDomain: [],
        selectUserOrg: [],
      };
      this.resProtocolForm = {
        resProtocol: "None",
        autoUpdate: false,
        types: [],
        formats: [],
        scales: [],
        references: [],
        units: [],
        concepts: [],
      };
    },
    changeProtocolType() {
      this.otherNodes = [];
      this.otherNodes = JSON.parse(JSON.stringify(this.selectedActivities));

      this.activityLinks = [];
      for (let i = 0; i < this.selectedActivities.length; i++) {
        this.activityLinks.push(0);
      }
      let content = {
        behavior: "setLink",
        param: "protocolType",
        linkingId: this.linkingId,
        protocolType: this.protocolType,
        selectedActivities: this.selectedActivities,
      };
      this.sendLinkSock(content);
    },
    //活动选择
    activitySelect(index) {
      //otherNodes 添加selected
      let slctActivityId = this.activityLinks[index];
      for (let i = 0; i < this.otherNodes.length; i++) {
        if (this.otherNodes[i].aid === slctActivityId) {
          this.otherNodes[i]["selected"] = true;
        }
      }

      let content = {
        behavior: "setLink",
        param: "activityLinks",
        linkingId: this.linkingId,
        activityLinks: this.activityLinks,
        otherNodes: this.otherNodes,
      };
      this.sendLinkSock(content);
    },
    activitySelectClear(index) {
      let slctActivityId = this.activityLinks[index];
      for (let i = 0; i < this.otherNodes.length; i++) {
        if (this.otherNodes[i].aid === slctActivityId) {
          this.otherNodes[i]["selected"] = false;
        }
      }
      let content = {
        behavior: "setLink",
        param: "activityLinks",
        linkingId: this.linkingId,
        activityLinks: this.activityLinks,
        otherNodes: this.otherNodes,
      };
      this.sendLinkSock(content);
    },
    cancelLink() {
      // 重新渲染
      this.updateStepChart();
      // clear
      this.selectedActivities = [];
      this.clearProtocolSetting();
      this.activityLinks = [];
      this.linkBuildModal = false;
    },
    processUpdate() {
      let relations = [];
      // identify the relations
      switch (this.protocolType) {
        case "Sequence": {
          for (let i = 0; i < this.activityLinks.length - 1; i++) {
            let relation = {
              last: this.activityLinks[i],
              next: this.activityLinks[i + 1],
            };
            relations.push(relation);
          }
          break;
        }
        case "Branch": {
          for (let i = 1; i < this.activityLinks.length; i++) {
            let relation = {
              last: this.activityLinks[0],
              next: this.activityLinks[i],
            };
            relations.push(relation);
          }
          break;
        }
        case "Merger": {
          for (let i = 1; i < this.activityLinks.length; i++) {
            let relation = {
              last: this.activityLinks[i],
              next: this.activityLinks[0],
            };
            relations.push(relation);
          }
          break;
        }
        case "Loop": {
          for (let i = 0; i < this.activityLinks.length - 1; i++) {
            let relation = {
              last: this.activityLinks[i],
              next: this.activityLinks[i + 1],
            };
            relations.push(relation);
          }
          let relation = {
            last: this.activityLinks[this.activityLinks.length - 1],
            next: this.activityLinks[0],
          };
          relations.push(relation);
          break;
        }
      }
      //  update
      for (let i = 0; i < relations.length; i++) {
        let lastnode, nextnode;

        for (let j = 0; j < this.selectedActivities.length; j++) {
          if (this.selectedActivities[j].aid == relations[i].last) {
            lastnode = {
              name: this.selectedActivities[j].name,
              aid: this.selectedActivities[j].aid,
              id: this.selectedActivities[j].id,
            };
          }
          if (this.selectedActivities[j].aid == relations[i].next) {
            nextnode = {
              name: this.selectedActivities[j].name,
              aid: this.selectedActivities[j].aid,
              id: this.selectedActivities[j].id,
            };
          }
        }

        for (let k = 0; k < this.processStructure.length; k++) {
          // 后继节点
          if (this.processStructure[k].aid == nextnode.aid) {
            if (!this.processStructure[k].last.contains(lastnode)) {
              this.processStructure[k].last.push(lastnode);
            }
          }
          // 前驱节点
          if (this.processStructure[k].aid == lastnode.aid) {
            if (!this.processStructure[k].next.contains(nextnode)) {
              this.processStructure[k].next.push(nextnode);
            }
          }
        }
      }
      this.updatePathway();
      return relations;
    },
    buildLink: function () {
      //** front link */
      let relations = this.processUpdate();

      //*** save protocol */

      //// normalize protocol
      let relation = {
        graphId: this.activityInfo.aid,
        type: this.protocolType,
        nodes: this.activityLinks,
      };

      let restriction = {
        resProtocol: this.resProtocolForm.resProtocol,
        autoUpdate: this.resProtocolForm.autoUpdate,
        types: this.resProtocolForm.types,
        formats: this.filterTags(this.resProtocolForm.formats),
        concepts: this.filterTags(this.resProtocolForm.concepts),
        scales: this.filterTags(this.resProtocolForm.scales),
        references: this.filterTags(this.resProtocolForm.references),
        units: this.filterTags(this.resProtocolForm.units),

        roleProtocol: this.userProtocolForm.roleProtocol,
        roles: this.userProtocolForm.linkRoles,
        domains: this.userProtocolForm.selectUserDomain,
        organizations: this.userProtocolForm.selectUserOrg,
      };

      let level = window.location.href.split("level=")[1];
      let protocolForm = {
        relation: relation,
        restriction: restriction,
        level: level,
      };
      // save protocol
      this.axios
        .post("/GeoProblemSolving/activityDriven", protocolForm)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // link in the activity document
            let protocolId = res.data.data.protocolId;

            for (let i = 0; i < relations.length; i++) {
              this.operationApi.processRecord(
                this.activityInfo.aid,
                "",
                "link",
                this.userInfo.userId,
                relations[i].last,
                relations[i].next,
                protocolId
              );
            }

            // clear
            this.selectedActivities = [];
            let content = {
              behavior: "bulk",
              linkingId: this.linkingId,
              protocolId: protocolId,
            };
            this.sendLinkSock(content);

            //等待队列，如果有则删除；如果无则不管
            this.computeCollIndex(content.linkingId);
            if (this.collaIndex == -1) {
              return;
            }
            //删除等待队列
            this.collaboratingInfoList.splice(this.collaIndex, 1);
            this.linkBuildModal = false;
          } else {
            this.$Message.error("Fail to link activities.");
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    clearProtocolSetting() {
      this.userProtocolForm = {
        roleProtocol: "None",
        linkRoles: [],
        selectUserDomain: [],
        selectUserOrg: [],
      };

      this.formats_tag = "";
      this.scales_tag = "";
      this.references_tag = "";
      this.units_tag = "";
      this.concepts_tag = "";
      this.resProtocolForm = {
        resProtocol: "",
        autoUpdate: false,
        types: [],
        formats: [],
        scales: [],
        references: [],
        units: [],
        concepts: [],
      };
    },
    filterTags(tags) {
      let newtags = [];
      for (let i = 0; i < tags.length; i++) {
        newtags.push(tags[i].text);
      }
      return newtags;
    },

    // seperate
    removeLink() {
      let beginNode = this.selectedActivities[0];
      let endNode = this.selectedActivities[1];

      for (let i = 0; i < this.processStructure.length; i++) {
        // 前驱节点
        if (this.processStructure[i].aid == beginNode.aid) {
          for (let j = 0; j < this.processStructure[i].next.length; j++) {
            if (this.processStructure[i].next[j].name == endNode.name) {
              this.processStructure[i].next.splice(j, 1);
              break;
            }
          }
        }
        // 后继节点
        if (this.processStructure[i].aid == endNode.aid) {
          for (let j = 0; j < this.processStructure[i].last.length; j++) {
            if (this.processStructure[i].last[j].name == beginNode.name) {
              this.processStructure[i].last.splice(j, 1);
              break;
            }
          }
        }
      }

      // 重新渲染
      this.updateStepChart();
      // 更新图的 pathway
      this.breakLink(beginNode.aid, endNode.aid);
      this.updatePathway();
      //
      let content = {
        behavior: "removeLink",
        processStructure: this.processStructure,
      };
      this.sendLinkSock(content);
      // this.delLinkStore(beginNode.aid, endNode.aid);
      this.selectedActivities = [];
      this.removeLinkBtn = false;
    },
    breakLink(beginNode, endNode) {
      this.axios
        .delete(
          `/GeoProblemSolving/activityDriven/${this.activityInfo.aid}/${beginNode}/${endNode}`
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.$Notice.info({
              desc: "Break the link successfully!",
            });
          } else {
            this.$Message.error("Fail to unlink.");
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          throw err;
        });
    },
    gotoActivity(aid) {
      for (let i = 0; i < this.childActivities.length; i++) {
        if (this.childActivities[i].aid == aid) {
          window.location.href =
            "/GeoProblemSolving/activityInfo/" +
            this.projectInfo.aid +
            "?aid=" +
            this.childActivities[i].aid +
            "&level=" +
            this.childActivities[i].level;
        }
      }
    },
    roleProtocolChange(val) {
      let aidList = [];
      this.selectedActivities.forEach((activity) => {
        aidList.push(activity.aid);
      });
      if (val == "Constraints") {
        let level = window.location.href.split("level=")[1];
        this.axios
          .get(
            `/GeoProblemSolving/activityDriven/user/tag/${level}/${aidList.toString()}`
          )
          .then((res) => {
            let code = res.data.code;
            if (code != 0) {
              this.$Notice.info({
                desc: "ERROR!",
              });
            } else {
              let data = res.data.data;

              this.userDomain = data.domains.map((item) => {
                return { name: item };
              });
              this.userOrganizations = data.organizations.map((item) => {
                return { name: item };
              });

              let content = {
                behavior: "setLink",
                param: "roleProtocol",
                linkingId: this.linkingId,
                userDomain: this.userDomain,
                userOrganization: this.userOrganizations,
                roleProtocol: this.userProtocolForm.roleProtocol,
              };
              this.sendLinkSock(content);
            }
          })
          .catch((e) => {
            this.$Notice.info({
              desc: "ERROR!",
            });
          });
      } else {
        let content = {
          behavior: "setLink",
          param: "roleProtocol",
          linkingId: this.linkingId,
          roleProtocol: this.userProtocolForm.roleProtocol,
        };
        this.sendLinkSock(content);
      }
    },
    roleChange() {
      let content = {
        behavior: "setLink",
        param: "userRole",
        linkingId: this.linkingId,
        userRole: this.userProtocolForm.linkRoles,
      };
      this.sendLinkSock(content);
    },
    domainChange() {
      let content = {
        behavior: "setLink",
        param: "userDomain",
        linkingId: this.linkingId,
        selectUserDomain: this.userProtocolForm.selectUserDomain,
      };
      this.sendLinkSock(content);
    },
    organizationChange() {
      let content = {
        behavior: "setLink",
        param: "userOrganization",
        linkingId: this.linkingId,
        userSelectOrganization: this.userProtocolForm.selectUserOrg,
      };
      this.sendLinkSock(content);
    },
    autoUpdateChange() {
      let content = {
        behavior: "setLink",
        param: "autoUpdate",
        linkingId: this.linkingId,
        autoUpdate: this.resProtocolForm.autoUpdate,
      };
      this.sendLinkSock(content);
    },
    resProtocolChange(val) {
      let aidList = [];
      this.selectedActivities.forEach((activity) => {
        aidList.push(activity.aid);
      });
      let content = {
        behavior: "setLink",
        param: "resProtocol",
        linkingId: this.linkingId,
        resProtocol: this.resProtocolForm.resProtocol,
      };
      if (val == "Constraints") {
        //
        let resMetaInfo = this.operationApi.getResMetaInfo(aidList);
        if (resMetaInfo !== undefined) {
          content["resMetaInfo"] = resMetaInfo;
          if (resMetaInfo.format !== undefined) {
            this.autoFormats = resMetaInfo.format;
          }
          if (resMetaInfo.scale !== undefined) {
            this.autoScales = resMetaInfo.scale;
          }
          if (resMetaInfo.reference !== undefined) {
            this.autoReferences = resMetaInfo.reference;
          }
          if (resMetaInfo.unit !== undefined) {
            this.autoUnits = resMetaInfo.unit;
          }
          if (resMetaInfo.concept !== undefined) {
            this.autoConcepts = resMetaInfo.concept;
          }
        }
      }

      this.sendLinkSock(content);
      // if (val == "Constraints") {
      //   this.axios
      //     .get(
      //       "/GeoProblemSolving/activityDriven/res/tag/" + aidList.toString()
      //     )
      //     .then((res) => {
      //       let code = res.data.code;
      //       if (code != 0) {
      //         this.$Notice.info({
      //           desc: "ERROR!",
      //         });
      //       } else {
      //         let data = res.data.data;
      //         this.resTypes = data.types;
      //         this.resSuffixes = data.suffixes;
      //       }
      //     })
      //     .catch((e) => {
      //       this.$Notice.info({
      //         desc: "ERROR!",
      //       });
      //     });
      // }
    },
    resTypeChange() {
      let content = {
        behavior: "setLink",
        param: "resType",
        linkingId: this.linkingId,
        resType: this.resProtocolForm.types,
      };
      this.sendLinkSock(content);
    },
    formatChange(newTag) {
      this.resProtocolForm.formats = newTag;
      // console.log(newTag);
      let content = {
        behavior: "setLink",
        param: "resFormat",
        linkingId: this.linkingId,
        resFormat: this.resProtocolForm.formats,
      };
      this.sendLinkSock(content);
    },
    scaleChange(newTag) {
      this.resProtocolForm.scales = newTag;
      let content = {
        behavior: "setLink",
        param: "resScale",
        linkingId: this.linkingId,
        resScale: this.resProtocolForm.scales,
      };
      this.sendLinkSock(content);
    },
    referenceChange(newTag) {
      this.resProtocolForm.references = newTag;
      let content = {
        behavior: "setLink",
        param: "resReference",
        linkingId: this.linkingId,
        resReference: this.resProtocolForm.references,
      };
      this.sendLinkSock(content);
    },
    unitChange(newTag) {
      this.resProtocolForm.units = newTag;
      let content = {
        behavior: "setLink",
        param: "resUnit",
        linkingId: this.linkingId,
        resUnit: this.resProtocolForm.units,
      };
      this.sendLinkSock(content);
    },
    conceptChange(newTag) {
      this.resProtocolForm.concepts = newTag;
      let content = {
        behavior: "setLink",
        param: "resConcept",
        linkingId: this.linkingId,
        resConcept: this.resProtocolForm.concepts,
      };
      this.sendLinkSock(content);
    },
  },
};
</script>

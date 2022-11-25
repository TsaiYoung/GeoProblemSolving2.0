<template>
  <div>
    <Card class="resCard" dis-hover>
      <div slot="title">
        <Button
          v-if="folderIdStack.length >= 1"
          icon="md-arrow-round-back"
          shape="circle"
          size="small"
          @click="backforeFolder"
          title="Back to the parent folder"
          style="cursor: pointer"
        />
        <Button
          v-else
          icon="md-arrow-round-back"
          shape="circle"
          size="small"
          title="Back to the parent folder"
          style="color: lightgray; cursor: default"
        />
        <Button
          shape="circle"
          size="small"
          icon="md-refresh"
          @click="refreshResource"
          title="Refresh resources"
          style="margin-left: 5px"
        ></Button>
        <Label style="margin-left: 5px">Select:</Label>
        <Select
          v-model="resouceType"
          size="small"
          @on-change="changeResType"
          style="width: 120px; margin: 5px"
        >
          <Option value="all">All resources</Option>
          <Option value="data">Data</Option>
          <Option value="others">Other resources</Option>
          <!-- <Option value="toolData">Results</Option> -->
        </Select>
        <Poptip placement="bottom">
          <Button
            shape="circle"
            size="small"
            icon="md-shuffle"
            style="margin-right: 7px"
            title="Get resources"
          ></Button>
          <div slot="content" >
            <Button
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'upload_resource'
                )
              "
              @click="shareModalShow()"
              title="Get resources from your personal space"
            > Get resources from your personal space</Button><br>
            <Button
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'upload_resource'
                ) && activityInfo.level > 0
              "
              @click="getPreviousRes()"
              style="margin-top: 7px;"
              title="Get resources from the previous activities"
            > Get resources from the previous activities</Button>
          </div>
        </Poptip>
        <Button
          v-if="
            permissionIdentity(
              activityInfo.permission,
              userRole,
              'upload_resource'
            ) && activityInfo.level > 0
          "
          shape="circle"
          size="small"
          icon="md-cloud-outline"
          @click="shareToParentModalShow()"
          style="margin-right: 7px"
          title="Share resources"
        ></Button>

        <Button
          v-if="
            permissionIdentity(
              activityInfo.permission,
              userRole,
              'upload_resource'
            )
          "
          shape="circle"
          size="small"
          icon="md-cloud-upload"
          style="margin-right: 7px"
          @click="dataUploadModalShow"
          title="Upload resources"
        ></Button>
        <template
          v-if="
            permissionIdentity(
              activityInfo.permission,
              userRole,
              'manage_resource'
            )
          "
        >
          <Button
            v-if="!resEdit"
            shape="circle"
            size="small"
            icon="md-create"
            @click="resEdit = true"
            title="Edit resources"
          >
          </Button>
          <Button
            v-else
            shape="circle"
            size="small"
            icon="md-create"
            @click="resEdit = false"
            title="Edit resources"
          >
          </Button>
        </template>

      </div>
      <div style="display: flex; justify-content: space-between">
        <div style="width: 100%">
          <div style="text-align: center; margin: 70px 0;" v-if="fileList.length == 0">
            <h2 style="color: #808695">No resource</h2>
            <small
              style="color: #dcdee2"
              v-if="
                permissionIdentity(
                  activityInfo.permission,
                  userRole,
                  'upload_resource'
                )
              "
              >*Click the button to add resource.</small
            >
            <small style="color: #dcdee2" v-else
              >*You do not have permission to manage resource.</small
            >
          </div>
          <vue-scroll :ops="ops" style="max-height: calc(50vh - 220px)" v-else>
            <Card class="res-content"
              ><Icon
                type="ios-add"
                size="80"
                title="Create a new file folder"
                @click="addFolderModalShow"
            /></Card>
            <div v-for="(item, index) in fileList" :key="index">
              <Card
                class="res-content"
                v-if="!resEdit && item.fromParents == undefined"
              >
                <div
                  class="res-content-image"
                  v-if="item.folder"
                  @click="enterFolder(item)"
                >
                  <img
                    :src="folderUrl"
                    height="42px"
                    width="42px"
                    title="Folder"
                  />
                </div>
                <div class="res-content-image" @click="checkData(item)" v-else>
                  <template
                    v-if="item.thumbnail == '' || item.thumbnail == undefined"
                  >
                    <img
                      :src="getImageUrl(item.type)"
                      height="42px"
                      width="42px"
                      :title="item.type"
                    />
                  </template>
                  <template v-else>
                    <img :src="item.thumbnail" height="42px" width="42px" />
                  </template>
                </div>
                <div>
                  <div
                    class="toolDataText"
                    :title="item.name"
                    v-if="item.folder"
                  >
                    {{ item.name }}
                  </div>
                  <div
                    class="toolDataText"
                    :title="item.name + item.suffix"
                    v-else
                  >
                    {{ item.name + item.suffix }}
                  </div>
                </div>
              </Card>
              <Card
                class="res-content-parents"
                v-else-if="!resEdit && item.fromParents != undefined"
              >
                <div
                  class="res-content-image"
                  v-if="item.folder"
                  @click="enterFolder(item)"
                >
                  <img
                    :src="folderUrl"
                    height="42px"
                    width="42px"
                    title="Folder"
                  />
                </div>
                <div
                  class="res-content-image"
                  @click="checkData(item)"
                  :title="item.fromParents"
                  v-else
                >
                  <template
                    v-if="item.thumbnail == '' || item.thumbnail == undefined"
                  >
                    <img
                      :src="getImageUrl(item.type)"
                      height="42px"
                      width="42px"
                      :title="item.type + ' | ' + item.fromParents"
                    />
                  </template>
                  <template v-else>
                    <img :src="item.thumbnail" height="42px" width="42px" />
                  </template>
                </div>
                <div>
                  <div
                    class="toolDataText"
                    :title="item.name"
                    v-if="item.folder"
                  >
                    {{ item.name }}
                  </div>
                  <div
                    class="toolDataText"
                    :title="item.name + item.suffix"
                    v-else
                  >
                    {{ item.name + item.suffix }}
                  </div>
                </div>
              </Card>
              <Card class="res-content-edit" v-else-if="resEdit && item.fromParents == undefined">
                <div
                  class="res-content-image"
                  v-if="item.folder"
                  @click="editFolderModalShow(item)"
                >
                  <Badge text="Edit" type="warning">
                    <img
                      :src="folderUrl"
                      height="42px"
                      width="42px"
                      title="Edit (or delete) folder"
                    />
                  </Badge>
                </div>
                <div
                  class="res-content-image"
                  @click="fileEditModelShow(item)"
                  v-else
                >
                  <template
                    v-if="item.thumbnail == '' || item.thumbnail == undefined"
                  >
                    <Badge text="Edit" type="warning">
                      <img
                        :src="getImageUrl(item.type)"
                        height="42px"
                        width="42px"
                        :title="item.type"
                      />
                    </Badge>
                  </template>
                  <template v-else>
                    <img :src="item.thumbnail" height="42px" width="42px" />
                  </template>
                </div>
                <div>
                  <div
                    class="toolDataText"
                    :title="item.name"
                    v-if="item.folder"
                  >
                    {{ item.name }}
                  </div>
                  <div
                    class="toolDataText"
                    :title="item.name + item.suffix"
                    v-else
                  >
                    {{ item.name + item.suffix }}
                  </div>
                </div>
              </Card>
              <Card
                class="res-content-parents"
                v-else
              >
                <div
                  class="res-content-image"
                  v-if="item.folder"
                  @click="enterFolder(item)"
                >
                  <img
                    :src="folderUrl"
                    height="42px"
                    width="42px"
                    title="Folder"
                  />
                </div>
                <div
                  class="res-content-image"
                  @click="checkData(item)"
                  :title="item.fromParents"
                  v-else
                >
                  <template
                    v-if="item.thumbnail == '' || item.thumbnail == undefined"
                  >
                    <img
                      :src="getImageUrl(item.type)"
                      height="42px"
                      width="42px"
                      :title="item.type + ' | ' + item.fromParents"
                    />
                  </template>
                  <template v-else>
                    <img :src="item.thumbnail" height="42px" width="42px" />
                  </template>
                </div>
                <div>
                  <div
                    class="toolDataText"
                    :title="item.name"
                    v-if="item.folder"
                  >
                    {{ item.name }}
                  </div>
                  <div
                    class="toolDataText"
                    :title="item.name + item.suffix"
                    v-else
                  >
                    {{ item.name + item.suffix }}
                  </div>
                </div>
              </Card>
            </div>
          </vue-scroll>
        </div>
      </div>
    </Card>
    <Modal
      v-model="shareModal"
      title="Share file from personal center"
      width="600"
      :mask-closable="false"
    >
      <div>
        <vue-scroll :ops="ops" style="height: 300px">
          <Card dis-hover v-if="userResourceList.length == 0" style="text-align: center; border: transparent; margin-top: 70px">
            <h2 style="color: #808695">No Resource</h2>
            <small style="color: #dcdee2"
              >*You do not have any personal resource.</small
            >
          </Card>
          <CheckboxGroup v-model="selectedFilesToShare">
            <Card dis-hover v-for="file in userResourceList" :key="file.index">
              <Checkbox
                :label="file.uid"
                class="personalFileLabel"
                :title="file.name"
                v-if="canBeShare(file.uid)"
              >
                <Icon
                  v-if="file.type === 'data'"
                  type="ios-podium-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'image'"
                  type="ios-image-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'paper'"
                  type="ios-paper-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'document'"
                  type="ios-document-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'model'"
                  type="ios-construct-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'video'"
                  type="ios-videocam-outline"
                  class="itemIcon"
                  size="25"
                />
                <p
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 100px;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                  "
                >
                  <strong>{{ file.name }}</strong>
                </p>
                <!-- <span ><strong>{{ file.name }}</strong></span> -->
              </Checkbox>
              <Checkbox
                :label="file.uid"
                class="personalFileLabel"
                :title="file.name"
                disabled
                v-else
              >
                <Icon
                  v-if="file.type === 'data'"
                  type="ios-podium-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'image'"
                  type="ios-image-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'paper'"
                  type="ios-paper-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'document'"
                  type="ios-document-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'model'"
                  type="ios-construct-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'video'"
                  type="ios-videocam-outline"
                  class="itemIcon"
                  size="25"
                />
                <p
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 100px;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                  "
                >
                  <strong>{{ file.name }}</strong>
                </p>
                <!-- <strong>{{ file.name }}</strong> -->
              </Checkbox>
              <p
                :title="file.description"
                style="
                  display: inline-block;
                  vertical-align: top;
                  width: 200px;
                  overflow: hidden;
                  white-space: nowrap;
                  text-overflow: ellipsis;
                  margin-left: 50px;
                "
              >
                {{ file.description }}
              </p>
              <!-- <span
                class="personalFileDes"
                style="width: 150px;margin-left:50px"
                :title="file.description"
                >{{ file.description }}</span
              > -->
              <span
                style="display: inline-block; vertical-align: top; float: right"
                >{{ file.fileSize | filterSizeType }}</span
              >
            </Card>
          </CheckboxGroup>
        </vue-scroll>
      </div>
      <div slot="footer" style="display: inline-block">
        <Button type="primary" @click="shareResources()" style="float: right"
          >Submit
        </Button>
        <Button
          @click="shareModal = false"
          style="float: right; margin-right: 15px"
          >Cancel
        </Button>
      </div>
    </Modal>
    <Modal
      v-model="shareToParentModal"
      title="Share files to parent activity"
      width="600"
      :mask-closable="false"
    >
      <div>
        <vue-scroll :ops="ops" style="height: 300px">
          <Card dis-hover v-if="fileListChoosed.length == 0" style="text-align: center; border: transparent; margin-top: 70px">
            <h2 style="color: #808695;">No Resource</h2>
            <small style="color: #dcdee2"
              >*You do not have any resource.</small
            >
          </Card>
          <CheckboxGroup v-model="selctResToShare">
            <Card dis-hover v-for="file in fileListChoosed" :key="file.index">
              <Checkbox
                :label="file.uid"
                class="personalFileLabel"
                :title="file.name"
                v-if="!file.folder && !file.fromParents"
              >
                <Icon
                  v-if="file.type === 'data'"
                  type="ios-podium-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'image'"
                  type="ios-image-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'paper'"
                  type="ios-paper-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'document'"
                  type="ios-document-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'model'"
                  type="ios-construct-outline"
                  class="itemIcon"
                  size="25"
                />
                <Icon
                  v-else-if="file.type === 'video'"
                  type="ios-videocam-outline"
                  class="itemIcon"
                  size="25"
                />
                <p
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 100px;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                  "
                >
                  <strong>{{ file.name }}</strong>
                </p>
                <!-- <span ><strong>{{ file.name }}</strong></span> -->
              </Checkbox>
              <Checkbox
                :label="file.uid"
                class="personalFileLabel"
                :title="file.name"
                v-else-if="!file.fromParents"
              >
                <Icon
                  type="ios-folder-open-outline"
                  class="itemIcon"
                  size="25"
                />
                <p
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 100px;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                  "
                >
                  <strong>{{ file.name }}</strong>
                </p>
              </Checkbox>
              <p
                :title="file.description"
                style="
                  display: inline-block;
                  vertical-align: top;
                  width: 200px;
                  overflow: hidden;
                  white-space: nowrap;
                  text-overflow: ellipsis;
                  margin-left: 50px;
                "
                v-if=" !file.fromParents"
              >
                {{ file.description }}
              </p>
              <!-- <span
                class="personalFileDes"
                style="width: 150px;margin-left:50px"
                :title="file.description"
                >{{ file.description }}</span
              > -->
              <span
                style="display: inline-block; vertical-align: top; float: right" v-if="!file.folder && !file.fromParents"
                >{{ file.fileSize | filterSizeType }}</span
              >
            </Card>
          </CheckboxGroup>
        </vue-scroll>
      </div>

      <div slot="footer" style="display: inline-block">
        <Button type="primary" @click="shareToParent()" style="float: right"
          >Submit
        </Button>
        <Button
          @click="shareToParentModal = false"
          style="float: right; margin-right: 15px"
          >Cancel
        </Button>
      </div>
    </Modal>
    <Modal
      v-model="editFolderModal"
      title="Edit folder"
      ok-text="Assure"
      cancel-text="Cancel"
    >
      <Form
        ref="editFolderValidate"
        :model="editFolderValidate"
        :rules="renameRuleValidate"
        :label-width="80"
      >
        <FormItem label="New name" prop="newName">
          <Input
            v-model="editFolderValidate.newName"
            :rows="4"
            placeholder="Enter the name for folder..."
          />
        </FormItem>
      </Form>
      <Button type="error" @click="deleteFolder">Delete the folder</Button>
      <div slot="footer">
        <Button @click="editFolderModal = false">Cancel</Button>
        <Button type="success" @click="editFolder('editFolderValidate')"
          >Rename
        </Button>
      </div>
    </Modal>
    <Modal v-model="editFileModel" title="Edit file information" width="600">
      <Form
        ref="editFileValidate"
        :model="editFileValidate"
        :rules="editFileRuleValidate"
        label-position="left"
        :label-width="100"
        inline
      >
        <FormItem label="Privacy" prop="privacy">
          <RadioGroup v-model="editFileValidate.privacy">
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
        <FormItem label="Name" prop="name" style="width: 100%">
          <Input
            v-model="editFileValidate.name"
            placeholder="Enter the name for file..."
          />
        </FormItem>
        <FormItem label="Description" prop="description" style="width: 100%">
          <Input
            type="textarea"
            :rows="3"
            v-model="editFileValidate.description"
          />
        </FormItem>
        <Divider orientation="left" v-if="editFileValidate.type == 'data'">Metadata</Divider>
        <FormItem label="Format" prop="format" v-if="editFileValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="editFileValidate.format"
          />
        </FormItem>
        <FormItem label="Scale" prop="scale" v-if="editFileValidate.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="editFileValidate.scale"
          />
        </FormItem>
        <FormItem label="Reference" prop="reference" v-if="editFileValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="editFileValidate.reference"
          />
        </FormItem>
        <FormItem label="Unit" prop="unit" v-if="editFileValidate.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="editFileValidate.unit"
          />
        </FormItem>
        <FormItem label="Concept" prop="concept" v-if="editFileValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="editFileValidate.concept"
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="editFileModel = false">Cancel</Button>
        <Button type="success" @click="editFileInfo('editFileValidate')"
          >Submit
        </Button>
      </div>
    </Modal>
    <Modal v-model="newFolderModal" title="New folder">
      <Form
        ref="newValidate"
        :model="newValidate"
        :rules="newRuleValidate"
        :label-width="80"
        @submit.native.prevent
      >
        <FormItem label="Set name" prop="setName">
          <Input
            v-model="newValidate.setName"
            :rows="4"
            placeholder="Enter the name for folder..."
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="newFolderModal = false">Cancel</Button>
        <Button type="success" @click="addFolder('newValidate')">New</Button>
      </div>
    </Modal>
    <Modal
      v-model="checkDataModal"
      title="Data information"
      width="600"
      footer-hide
    >
      <!-- <Tabs>
        <TabPane label="Information" name="metadata" icon="md-home"> -->
      <div style>
        <div class="dataInfo">
          <Label class="dataLabel">Name:</Label>
          <span class="dataText">{{
            selectData.name + selectData.suffix
          }}</span>
        </div>
        <div class="dataInfo">
          <Label class="dataLabel">Type:</Label>
          <span class="dataContent">{{ selectData.type }}</span>
          <Label class="dataLabel">Provider:</Label>
          <span class="dataContent">{{ selectData.uploaderName }}</span>
        </div>
        <div class="dataInfo">
          <Label class="dataLabel">File size:</Label>
          <span class="dataContent">{{
            selectData.fileSize | filterSizeType
          }}</span>
          <Label class="dataLabel">Created time:</Label>
          <span class="dataContent">{{
            dateFormat(selectData.uploadTime)
          }}</span>
        </div>
        <div class="dataInfo">
          <Label class="dataLabel">Description:</Label>
          <span class="dataText">{{ selectData.description }}</span>
        </div>
        <div class="dataInfo" v-if="selectData.fromParents != undefined">
          <Label class="dataLabel">Source:</Label>
          <span class="dataText">{{ selectData.fromParents }}</span>
        </div>
      </div>
      <!-- </TabPane>
        <TabPane label="UDX Schema" name="udx" icon="md-browsers">
          <pre class="brush: html"></pre>
          <template>...</template>
        </TabPane>
      </Tabs> -->
      <br />
      <div>
        <a
          :href="resProxy + '/data/' + resId"
          :download="selectData.name + selectData.suffix"
          target="_self"
          v-if="
            permissionIdentity(
              activityInfo.permission,
              userRole,
              'use_resource'
            )
          "
        >
          <Button
            type="info"
            size="small"
            title="Download"
            icon="md-download"
            style="margin: 10px 20px 0 0; cursor: pointer; width: 60px"
          ></Button>
        </a>
        <Button
          type="success"
          size="small"
          title="Preview"
          icon="md-eye"
          style="margin: 10px 20px 0 0; cursor: pointer; width: 60px"
          @click="preview(selectData)"
        ></Button>
        <Button
          v-if="
            selectData.fromParents == undefined &&
            (permissionIdentity(
              activityInfo.permission,
              userRole,
              'manage_resource'
            ) ||
            (permissionIdentity(
              activityInfo.permission,
              userRole,
              'upload_resource'
            ) &&
              selectData.uploaderId == userInfo.userId))
          "
          size="small"
          type="warning"
          title="Delete"
          icon="md-close"
          style="margin: 10px 20px 0 0; cursor: pointer; width: 60px"
          @click="deleteResourceModalShow(selectData)"
        ></Button>
      </div>
    </Modal>
    <Modal
      width="800px"
      v-model="inheritResModal"
      title="Get resources from previous activities"
      :styles="{ top: '20px' }"
      @on-ok="saveResources()"
      ok-text="Save"
      cancel-text="Cancel"
    >
      <div style="margin-left: 75px">
        <div style="font-size: 14px">Select the needed data:</div>
        <Transfer
          :data="existingResources"
          :target-keys="targetKeys"
          :list-style="listStyle"
          :render-format="resourceRender"
          :titles="['The previous activities', 'The new activity']"
          filter-placeholder="Enter key words..."
          filterable
          :filter-method="filterMethod"
          @on-change="handleChange"
        ></Transfer>
      </div>
    </Modal>
    <Modal v-model="dataUploadModal" title="Upload data" width="600">
      <Form
        ref="uploadDataInfo"
        :model="uploadDataInfo"
        :rules="uploadDataRule"
        :label-width="100"
        label-position="left"
        inline
      >
        <FormItem label="Privacy" prop="privacy" >
          <RadioGroup v-model="uploadDataInfo.privacy" style="width: 100%">
            <Radio label="private">Private</Radio>
            <Radio label="public">Public</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Type" prop="type">
          <RadioGroup v-model="uploadDataInfo.type">
            <Radio label="data"></Radio>
            <Radio label="paper"></Radio>
            <Radio label="document"></Radio>
            <Radio label="model"></Radio>
            <Radio label="image"></Radio>
            <Radio label="video"></Radio>
            <Radio label="others"></Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Description" prop="description" style="width: 100%">
          <Input
            type="textarea"
            :rows="3"
            v-model="uploadDataInfo.description"
          />
        </FormItem>
        <Divider orientation="left" v-if="uploadDataInfo.type == 'data'">Metadata</Divider>
        <FormItem label="Format" prop="format" v-if="uploadDataInfo.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadDataInfo.format"
          />
        </FormItem>
        <FormItem label="Scale" prop="scale" v-if="uploadDataInfo.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadDataInfo.scale"
          />
        </FormItem>
        <FormItem label="Reference" prop="reference" v-if="uploadDataInfo.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadDataInfo.reference"
          />
        </FormItem>
        <FormItem label="Unit" prop="unit" v-if="uploadDataInfo.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadDataInfo.unit"
          />
        </FormItem>
        <FormItem label="Concept" prop="concept" v-if="uploadDataInfo.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadDataInfo.concept"
          />
        </FormItem>
      </Form>
      <Upload
        :max-size="1024 * 1024"
        multiple
        type="drag"
        :before-upload="gatherFile"
        action="-"
      >
        <div style="padding: 20px 0">
          <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
          <p>
            Click or drag files here to upload (The file size must control in
            <span style="color: red">1GB</span>)
          </p>
        </div>
      </Upload>
      <div style="padding: 0 10px; max-height: 200px; overflow-y: auto">
        <ul v-for="(list, index) in toUploadFiles" :key="index">
          <li style="display: flex">
            File name:
            <span style="font-size: 10px; margin: 0 5px 0 5px"
              >{{ list.name }} ( {{ list.fileSize }} )</span
            >
            <Icon
              type="ios-close"
              size="20"
              @click="delFileList(index)"
              style="display: flex; justify-content: flex-end; cursor: pointer"
            ></Icon>
          </li>
        </ul>
      </div>
      <div slot="footer">
        <Button @click="dataUploadModal = false">Cancel</Button>
        <Button type="success" @click="folderUpload('uploadDataInfo')"
          >Upload</Button
        >
      </div>
    </Modal>
    <Modal
      v-model="progressModalShow"
      title="Upload Progress"
      :mask-closable="false"
      :closable="false"
    >
      <Progress :percent="uploadProgress"></Progress>
      <div slot="footer"></div>
    </Modal>
    <Modal
      v-model="deleteResourceModal"
      @on-ok="removeResource()"
      ok-text="Delete"
      cancel-text="Cancel"
    >
      <h3>Do you really want to delete this resource?</h3>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
import loginModal from "../../../user/userState/loginModal.vue";
export default {
  props: ["activityInfo"],
  components: {
    Avatar,
    loginModal,
  },
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      userInfo: this.$store.getters.userInfo,
      userRole: this.roleIdentity(this.activityInfo),
      resouceType: "all",
      //恢复登录的模态框
      tempLoginModal: false,
      // 文件夹
      newFolderModal: false,
      newValidate: {
        setName: "",
      },
      newRuleValidate: {
        setName: [
          {
            required: true,
            message: "The name can't be null.",
            trigger: "blur",
          },
        ],
      },
      editForeInfo: {},
      editFolderModal: false,
      editFolderValidate: {
        newName: "",
      },
      renameRuleValidate: {
        newName: [
          {
            required: true,
            message: "The name can't be null.",
            trigger: "blur",
          },
        ],
      },
      //资源继承
      preActivities: [],
      existingResources: [],
      targetKeys: [],
      inheritResModal: false,
      listStyle: { width: "280px", height: "375px" },
      // 上传资源
      dataUploadModal: false,
      uploadDataInfo: {
        privacy: "private",
        type: "data",
        description: "",
        format: "",
        scale: "",
        reference: "",
        unit: "",
        concept: "",
      },
      uploadDataRule: {
        privacy: [
          {
            required: true,
            message: "file privacy cannot be empty",
            trigger: "blur",
          },
        ],
        type: [
          {
            required: true,
            message: "file type cannot be empty",
            trigger: "blur",
          },
        ],
        description: [
          {
            message: "file description cannot be empty",
            trigger: "blur",
          },
        ],
      },
      toUploadFiles: [],
      fileCountTimer: null,
      progressModalShow: false,
      uploadProgress: 0,
      folderStack: [{ uid: 0, name: "Home" }], //folder level
      folderIdStack: [], // folder level
      fileList: [], //showed resources
      activityResList: [], //resources in the activity
      activityDataList: [], //data in the activity
      relatedResList: [], //related materials in the activity
      // toolDataList: [], //data from tools in the activity
      parentActivitiesID: [],
      parentActivitiesName: [],
      dataListStyle: false, // 数据列表展示样式
      // 资源avatar 图片路径
      dataUrl: require("@/assets/images/data.png"),
      modelUrl: require("@/assets/images/model.png"),
      paperUrl: require("@/assets/images/paper.png"),
      documentUrl: require("@/assets/images/document.png"),
      imageUrl: require("@/assets/images/image.png"),
      videoUrl: require("@/assets/images/video.png"),
      otherUrl: require("@/assets/images/otherfile.png"),
      folderUrl: require("@/assets/images/folder.png"),
      //
      checkDataModal: false,
      tableColName: [
        {
          title: "Name",
          key: "name",
          minWidth: 30,
          tooltip: true,
          sortable: true,
        },
        // {
        //   title: "Description",
        //   key: "description",
        //   tooltip: true,
        // },
        {
          title: "Action",
          slot: "action",
          width: 120,
          align: "center",
        },
      ],
      selectData: {},
      // 编辑data描述信息
      metaDataEdit: false,
      // 元数据信息，待完善
      metaDataInfo: "",
      // 删除资源
      deleteResourceModal: false,
      deleteResource: {},
      // 共享
      shareModal: false,
      shareToParentModal: false,
      fileListChoosed: [],
      selctResToShare: [],
      userResourceList: [],
      selectedFilesToShare: [],
      //编辑
      oldType: '',
      resEdit: false,
      editFileModel: false,
      oldMetadata: {},
      selectFileInfo: {},
      editFileValidate: {
        name: "",
        type: "",
        description: "",
        format: "",
        scale: "",
        reference: "",
        unit: "",
        concept: "",
      },
      editFileRuleValidate: {
        type: [
          {
            required: true,
            message: "file type cannot be empty",
            trigger: "blur",
          },
        ],
        name: [
          {
            required: true,
            message: "file description cannot be empty",
            trigger: "blur",
          },
        ],
        description: [
          {
            required: false,
            message: "file description cannot be empty",
            trigger: "blur",
          },
        ],
      },
      editForeInfo: {},
      editFolderModal: false,
      editFolderValidate: {
        newName: "",
      },
      renameRuleValidate: {
        newName: [
          {
            required: true,
            message: "The name can't be null.",
            trigger: "blur",
          },
        ],
      },
      // panel
      panel: null,
      resProxy: this.$store.getters.resProxy,
    };
  },
  computed: {
    resId: function () {
      let address = this.selectData.address;
      if (address != "" && typeof(address) == "string"){
        if (address.length == 36){
          return address;
        }else {
          let uidArr = address.split("data/");
          if (uidArr instanceof Array && uidArr.length > 1){
            return uidArr[1];
          }
        }
      }
    }
  },
  watch: {
    checkDataModal(value) {
      // if (!value) {
      //   this.panel.close();
      // }
    },
    activityInfo() {
      this.getResList();
    },
  },
  created() {},
  mounted() {
    this.getResList();
    this.getParentActivities();

    Date.prototype.Format = function (fmt) {
      var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S+": this.getMilliseconds(), //毫毛
      };
      if (/(y+)/.test(fmt))
        fmt = fmt.replace(
          RegExp.$1,
          (this.getFullYear() + "").substr(4 - RegExp.$1.length)
        );
      for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
          fmt = fmt.replace(
            RegExp.$1,
            RegExp.$1.length == 1
              ? o[k]
              : ("00" + o[k]).substr(("" + o[k]).length)
          );
      return fmt;
    };
  },
  methods: {
    roleIdentity(activity) {
      this.userInfo = this.$store.getters.userInfo;
      return this.userRoleApi.roleIdentify(
        activity.members,
        this.userInfo.userId
      );
    },
    permissionIdentity(permission, role, operation) {
      if (permission == undefined)
        permission = JSON.stringify(this.userRoleApi.getDefault());
      if (operation == "auto_join") {
        if (JSON.parse(permission).auto_join.visitor == "Yes") return true;
        else if (JSON.parse(permission).auto_join.visitor == "No") return false;
        else {
          return this.getParentPermission();
        }
      } else {
        return this.userRoleApi.permissionIdentity(
          JSON.parse(permission),
          role,
          operation
        );
      }
    },
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    dateFormat(date) {
      let time = new Date(date);
      return time.Format("yyyy-MM-dd HH:mm:ss");
    },
    getImageUrl(type) {
      let url;
      switch (type) {
        case "data": {
          url = this.dataUrl;
          break;
        }
        case "model": {
          url = this.modelUrl;
          break;
        }
        case "paper": {
          url = this.paperUrl;
          break;
        }
        case "document": {
          url = this.documentUrl;
          break;
        }
        case "image": {
          url = this.imageUrl;
          break;
        }
        case "video": {
          url = this.videoUrl;
          break;
        }
        case "others": {
          url = this.otherUrl;
          break;
        }
      }
      return url;
    },
    getParentActivities() {
      this.parentActivitiesID = [];
      this.parentActivitiesName = [];
      if (this.activityInfo.level > 0 && this.activityInfo.aid != "" && this.activityInfo.aid != undefined) {
        let url = "";
        let aid = this.activityInfo.aid;
        if (this.activityInfo.level == 1) {
          url = "/GeoProblemSolving/subproject/" + aid + "/lineage";
        } else if (this.activityInfo.level > 1) {
          url = "/GeoProblemSolving/activity/" + aid + "/lineage";
        }
        this.$axios
          .get(url)
          .then((res) => {
            if (res.data.code == 0) {
              let list = res.data.data.ancestors;
              for (let i = 1; i < list.length; i++) {
                this.parentActivitiesID.push(list[i].aid);
                this.parentActivitiesName.push("From " + list[i].name);
              }
              this.getParentActivitiesFile();
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            console.log(err.data);
          });
      }
    },
    getParentActivitiesFile() {
      if (
        this.parentActivitiesID != undefined &&
        this.parentActivitiesID.length > 0
      ) {
        this.$axios
          .get(
            "/GeoProblemSolving/rip/file/" + this.parentActivitiesID.toString()
          )
          .then((res) => {
            if (res.data.code == 0) {
              let parentsFilesList = res.data.data;
              let fileList = JSON.parse(JSON.stringify(this.fileList));
              for (let i = 0; i < this.parentActivitiesID.length; i++) {
                for (
                  let j = 0;
                  j < parentsFilesList[this.parentActivitiesID[i]].length;
                  j++
                ) {
                  parentsFilesList[this.parentActivitiesID[i]][j].fromParents =
                    this.parentActivitiesName[i];
                  fileList.push(
                    parentsFilesList[this.parentActivitiesID[i]][j]
                  );
                }
              }
              this.$set(this, "activityResList", fileList);
              this.filterData();
              this.filterRelatedRes();
              // this.filterToolData();

              // show resources
              this.$set(this, "fileList", fileList);
            } else {
              console.log(res.data.msg);
            }
          })
          .catch((err) => {
            console.log(err.data);
          });
      }
    },
    getResList() {
      if (this.activityInfo.aid != "" && this.activityInfo.aid != undefined) {
        this.axios
          .get("/GeoProblemSolving/rip/" + this.activityInfo.aid + "/0")
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let list = res.data.data;
              this.$set(this, "fileList", list);
            }
          })
          .catch((err) => {
            throw err;
          });
      }
    },
    refreshResource(){
      this.folderStack = [{ uid: 0, name: "Home" }], //folder level
      this.folderIdStack = [], // folder level
      this.getResList();
      this.getParentActivities();
    },
    filterData() {
      var filterdata = this.activityResList.filter((item) => {
        return item.folder || item.type === "data";
      });
      this.$set(this, "activityDataList", filterdata);
    },
    filterRelatedRes() {
      var filterdata = this.activityResList.filter((item) => {
        return item.folder || item.type !== "data";
      });
      this.$set(this, "relatedResList", filterdata);
    },
    // filterToolData() {
    //   var filterdata = this.fileList.filter((item) => {
    //     if (item.type.indexOf("toolData") != -1) {
    //       return item;
    //     }
    //   });
    //   this.$set(this, "toolDataList", filterdata);
    // },

    changeResType(value) {
      if (value == "all") {
        this.fileList = this.activityResList;
      } else if (value == "data") {
        this.fileList = this.activityDataList;
      } else if (value == "others") {
        this.fileList = this.relatedResList;
      } else if (value == "toolData") {
        // this.fileList = this.toolDataList;
      }
    },
    gatherFile(file) {
      if (this.toUploadFiles.length >= 5) {
        if (this.fileCountTimer != null) {
          clearTimeout(this.fileCountTimer);
        }
        this.fileCountTimer = setTimeout(() => {
          this.$Message.info("最多只能上传5个文件");
        }, 500);
      } else {
        var fileSize = file.size;
        if (fileSize < 1024) {
          file.fileSize = fileSize + "b";
        } else if (fileSize < 1024 * 1024) {
          file.fileSize = Math.round((fileSize / 1024) * 100) / 100 + "Kb";
        } else {
          file.fileSize =
            Math.round((fileSize / (1024 * 1024)) * 100) / 100 + "Mb";
        }
        this.toUploadFiles.push(file);
      }
      return false;
    },
    delFileList(index) {
      this.toUploadFiles.splice(index, 1);
    },
    dataUploadModalShow() {
      this.uploadDataInfo = {
        privacy: "private",
        type: "data",
        description: "",
        format: "",
        scale: "",
        reference: "",
        unit: "",
        concept: "",
      };
      this.dataUploadModal = true;
    },
    folderUpload(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          var uploadFiles = this.toUploadFiles;
          if (uploadFiles.length > 0) {
            this.dataUploadModal = false;
            var formData = new FormData();
            for (var i = 0; i < uploadFiles.length; i++) {
              formData.append("file", uploadFiles[i]);
            }
            let temp = this.folderIdStack;
            if (temp.length == 0) {
              temp = ["0"];
            }
            formData.append("description", this.uploadDataInfo.description);
            formData.append("type", this.uploadDataInfo.type);
            formData.append("privacy", this.uploadDataInfo.privacy);
            formData.append("aid", this.activityInfo.aid);
            formData.append("paths", temp.toString());
            formData.append("graphId", this.activityInfo.parent);
            if(this.uploadDataInfo.type == "data"){
              formData.append("format", this.uploadDataInfo.format);
              formData.append("scale", this.uploadDataInfo.scale);
              formData.append("reference", this.uploadDataInfo.reference);
              formData.append("unit", this.uploadDataInfo.unit);
              formData.append("concept", this.uploadDataInfo.concept);
            }
            this.progressModalShow = true;

            if (
              this.activityInfo.aid != "" ||
              this.activityInfo.aid != undefined
            ) {
              this.axios({
                url: "/GeoProblemSolving/rip/file/upload",
                method: "post",
                onUploadProgress: (progressEvent) => {
                  this.uploadProgress =
                    ((progressEvent.loaded / progressEvent.total) * 100) | 0;
                },
                data: formData,
              })
                .then((res) => {
                  if (res.data != "Fail") {
                    var uploadedList = res.data.uploaded;
                    var failedList = res.data.failed;
                    var sizeOverList = res.data.sizeOver;
                    let uploadedOperation = res.data.uploadedOperation;
                    let metadata = {};

                    for (var i = 0; i < uploadedList.length; i++) {
                      this.activityResList.push(uploadedList[i]);
                      if (this.uploadDataInfo.type == "data") {
                        this.activityDataList.push(uploadedList[i]);
                        // metadata.format = this.uploadDataInfo.format;
                        // metadata.scale = this.uploadDataInfo.scale;
                        // metadata.reference = this.uploadDataInfo.reference;
                        // metadata.unit = this.uploadDataInfo.unit;
                        // metadata.concept = this.uploadDataInfo.concept;
                      } else {
                        this.relatedResList.push(uploadedList[i]);
                      }
                    }
                    this.operationApi.getActivityDoc(this.activityInfo.aid);
                    for (let i = 0; i < uploadedOperation.length; i++){
                      let resOperation = {
                        id: uploadedOperation[i].oid,
                        type: "resource",
                        resRef: uploadedOperation[i].resRef,
                        operator: this.userInfo.userId,
                      };
                      this.$store.commit("updateTempOperations", {
                        behavior: "add",
                        operation: resOperation,
                      });
                    }

                    if (sizeOverList.length > 0) {
                      this.$Notice.warning({
                        title: "Files too large.",
                        render: (h) => {
                          return h("span", sizeOverList.join(";"));
                        },
                      });
                    }
                    if (failedList.length > 0) {
                      this.$Notice.error({
                        title: "Upload fail.",
                        render: (h) => {
                          return h("span", failedList.join(";"));
                        },
                      });
                    }
                    if (uploadedList.length > 0){
                      let sucFileName = uploadedList.map((item)=>{
                        return item.name + item.suffix;
                      });
                      this.$Notice.success({
                        title: "Upload result",
                        desc: "Upload successfully",
                        render: (h) =>{
                          return h("span", sucFileName.join(";"));
                        }
                      });
                    }

                    // 初始化上传数据列表
                    this.toUploadFiles = [];
                  } else {
                    this.$Message.warning("Upload fail.");
                  }

                  this.progressModalShow = false;
                  this.uploadProgress = 0;
                })
                .catch((err) => {
                  this.progressModalShow = false;
                  this.$Message.warning("Upload fail.");
                  this.uploadProgress = 0;
                });
            } else {
              this.$Notice.info({
                desc: "upload data failed!",
              });
            }
          } else {
            this.$Message.warning("Data is empty.");
          }
        }
      });
    },
    preview(data){
      this.checkDataModal = false;
      var res = data;
      let name = data.suffix;
      if (/(doc|docx|xls|xlsx|ppt|pptx|xml|json|md|gif|jpg|png|jpeg|pdf|txt)$/.test(name.toLowerCase())) {
        // if (this.panel != null) {
        //   this.panel.close();
        // }
        let url = "";
        if(res.address.indexOf("http://221.226.60.2:8082") != -1){
          url = this.$store.getters.resProxy + res.address.split("http://221.226.60.2:8082")[1];
        } else if(res.address.indexOf("/GeoProblemSolving/resource") != -1){
          url = "https://geomodeling.njnu.edu.cn" + res.address;
        } else {
          url = this.$store.getters.resProxy + res.address;
        }
        let Base64 = require('js-base64').Base64;
        // let finalUrl = "http://221.226.60.2:8082/onlinePreview?url=" + Base64.encode(url); // http
        let finalUrl = "https://geomodeling.njnu.edu.cn/dataTransferServer/onlinePreview?url=" + encodeURIComponent(Base64.encode(url)); // https
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
        var url = res.address;
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
      }  else {
        this.$Notice.error({
          title: "Open failed",
          desc: "Not supported file format."
        });
        return false;
      }
      // else if (/(pdf)$/.test(name.toLowerCase())) {
      //   if (this.panel != null) {
      //     this.panel.close();
      //   }
      //   let url = "";
      //   if(res.address.indexOf("http://221.226.60.2:8082") != -1){
      //     url = this.$store.getters.resProxy + res.address.split("http://221.226.60.2:8082")[1];
      //   } else {
      //     url = this.$store.getters.resProxy + res.address;
      //   }
      //   console.log(url);

      //   let that = this;
      //   var xhr = new XMLHttpRequest();
      //   xhr.open("GET", url, true);
      //   xhr.setRequestHeader(
      //     'Content-Type',
      //     'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      //   );
      //   xhr.responseType = 'blob';
      //   this.showLoading = true;
      //   xhr.onload = function (e) {
      //     if (this.status == 200) {
      //       let changeUrl = ''
      //       let file = new Blob([this.response], { type: 'application/pdf' })
      //       if (window.createObjectURL !== undefined) { // basic
      //         changeUrl = window.createObjectURL(file)
      //       } else if (window.webkitURL !== undefined) { // webkit or chrome
      //         try {
      //           changeUrl = window.webkitURL.createObjectURL(file)
      //         } catch (error) {

      //         }
      //       } else if (window.URL !== undefined) { // Mozilla (firefox)
      //         try {
      //           changeUrl = window.URL.createObjectURL(file)
      //         } catch (error) {
      //           console.log(error)
      //         }
      //       }
      //       console.log(file);
      //       console.log(changeUrl);
      //       var toolURL =
      //         "<iframe src=" +
      //         changeUrl +
      //         ' style="width: 100%;height:100%" frameborder="0" controls></iframe>';
      //       this.panel = jsPanel.create({
      //         headerControls: {
      //           smallify: "remove"
      //         },
      //         theme: "primary",
      //         footerToolbar: '<p style="height:10px"></p>',
      //         headerTitle: "Preview",
      //         contentSize: "800 600",
      //         content: toolURL,
      //         disableOnMaximized: true,
      //         dragit: {
      //           containment: 5
      //         },
      //         closeOnEscape: true
      //       });
      //       $(".jsPanel-content").css("font-size", "0");
      //     }
      //   };
      //   xhr.onreadystatechange = function() {
      //     if (xhr.readyState == 4) {
      //       if (xhr.status == 200) {
      //           that.showLoading = false;
      //       } else {
      //           //请求失败
      //       }
      //     }
      //   }
      //   xhr.send();

      // }
    },
    deleteResourceModalShow(resource) {
      this.deleteResourceModal = true;
      this.deleteResource = resource;
    },
    removeResource() {
      if (this.deleteResource.uid != "") {
        let temp = this.folderIdStack;
        if (temp.length == 0) {
          //重新开辟内存空间的temp,如果直接使用push的话，地址还是指向原数据的地址
          temp = ["0"];
        }
        let formData = new FormData();
        formData.append("uids", this.deleteResource.uid);
        formData.append("aid", this.activityInfo.aid);
        formData.append("paths", temp.toString());
        this.axios
          .post("/GeoProblemSolving/rip/del", formData)
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              this.$Notice.success({
                title: "Process result",
                desc: "Delete successfully",
              });

              let operationId = this.operationApi.resOperationRecord(
                this.activityInfo.aid,
                "",
                "",
                "remove",
                this.userInfo.userId,
                this.deleteResource
              );

              // 生成临时操作记录
              let resOperation = {
                id: operationId,
                type: "resource",
                resRef: this.deleteResource.uid,
                operator: this.userInfo.userId,
              };

              this.$store.commit("updateTempOperations", {
                behavior: "add",
                operation: resOperation,
              });

              //从列表中删除
              for (var i = 0; i < this.activityResList.length; i++) {
                if (this.activityResList[i].uid == this.deleteResource.uid) {
                  this.activityResList.splice(i, 1);
                }
              }
              if (this.deleteResource.type == "data") {
                for (var i = 0; i < this.activityDataList.length; i++) {
                  if (this.activityDataList[i].uid == this.deleteResource.uid) {
                    this.activityDataList.splice(i, 1);
                  }
                }
              } else {
                for (var i = 0; i < this.relatedResList.length; i++) {
                  if (this.relatedResList[i].uid == this.deleteResource.uid) {
                    this.relatedResList.splice(i, 1);
                  }
                }
              }
              this.checkDataModal = false;
            }
          })
          .catch((err) => {
            console.log(err.data);
          });
      }
    },
    checkData(item) {
      this.selectData = item;
      this.checkDataModal = true;
    },
    editMetadata() {
      if (this.metaDataEdit) {
        this.metaDataEdit = false;
      } else {
        this.metaDataEdit = true;
      }
    },
    getPreviousRes() {
      // 获取可继承的资源
      this.getPreActivities();
      this.inheritResModal = true;
    },
    getPreActivities() {
      this.preActivities = [];
      // // parent
      // if (
      //   this.activityInfo.parent != undefined &&
      //   this.activityInfo.parent != ""
      // ) {
      //   this.preActivities.push({ aid: this.activityInfo.parent });
      // }
      // last activities
      if (this.activityInfo.last != undefined) {
        for (var i = 0; i < this.activityInfo.last.length; i++) {
          this.preActivities.push(this.activityInfo.last[i]);
        }
      }
      this.getInheritResource();
    },
    getInheritResource() {
      this.existingResources = this.getMockData();
    },
    getMockData() {
      let mockData = [];
      let selectedRes = [];

      // 前驱步骤的资源
      for (var i = 0; i < this.preActivities.length; i++) {
        let activityId = this.preActivities[i].aid;

        this.axios
          .get("/GeoProblemSolving/rip/" + activityId + "/0")
          .then((res) => {
            if (res.data == "Offline") {
              // confirm("You are offline, please login again.");
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              // console.log(res.data.data);
              selectedRes = res.data.data;
              for (var j = 0; j < selectedRes.length; j++) {
                selectedRes[j].key = mockData.length.toString();
                selectedRes[j].source = activityId;
                mockData.push(selectedRes[j]);
              }
            } else {
              selectedRes = [];
            }
          })
          .catch((err) => {
            selectedRes = [];
            throw err;
          });
      }
      return mockData;
    },
    getTargetKeys() {
      let mockData = [];
      if (this.existingResources.length > 0) {
        for (var i = 0; i < this.targetKeys.length; i++) {
          mockData.push(this.existingResources[this.targetKeys[i]]);
        }
      }
      return mockData;
    },
    handleChange(newTargetKeys) {
      this.targetKeys = newTargetKeys;
    },
    resourceRender(item) {
      // return item.type + " - " + item.name;
      return `<span title="${item.type} - ${item.source}">${item.name}</span>`;
    },
    filterMethod(data, query) {
      return data.type.indexOf(query) > -1;
    },
    // 从父级活动拉取资源，属于 flow
    saveResources() {
      let selectResource = this.getTargetKeys();
      let addFileList = [];
      for (var i = 0; i < selectResource.length; i++) {
        addFileList.push(selectResource[i].uid);
      }
      let tempPath = this.folderIdStack;
      if (tempPath.length == 0) {
        tempPath = ["0"];
      }
      console.log(selectResource);
      console.log(addFileList);

      this.axios
        .post(
          "/GeoProblemSolving/rip/bind/" +
            this.activityInfo.aid +
            "/" +
            addFileList.toString() +
            "/" +
            tempPath.toString()
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // console.log(res.data.data);
            this.inheritResModal = false;
            // this.getResList();
            let resList = res.data.data;
            for (let i = 0; i < resList.length; i++) {
              this.activityResList.push(resList[i]);
              this.activityDataList.push(resList[i]);

              let operationId = this.operationApi.resOperationRecord(
                this.activityInfo.aid,
                "",
                "",
                "upload",
                this.userInfo.userId,
                resList[i]
              );
              // 生成临时操作记录
              let resOperation = {
                id: operationId,
                type: "resource",
                resRef: resList[i].uid,
                operator: this.userInfo.userId,
              };
              this.$store.commit("updateTempOperations", {
                behavior: "add",
                operation: resOperation,
              });
            }
            this.$Message.success("Shared file success!");
          } else {
            this.$Message.error(
              "Failed to get resources from previous activities."
            );
          }
        })
        .catch((err) => {
          this.$Message.error(
            "Failed to get resources from previous activities."
          );
        });
    },
    enterFolder(currentFolder) {
      this.folderIdStack.unshift(currentFolder.uid);
      this.changeFolder(currentFolder, "enter");
    },
    backforeFolder() {
      if (this.folderStack.length > 1) {
        //folderStack 倒数第二个就是其的父文件夹
        this.folderIdStack.splice(0, 1);
        this.changeFolder(
          this.folderStack[this.folderStack.length - 2],
          "back"
        );
      } else {
        this.$Message.warning("This is the root folder.");
      }
    },
    changeFolder(folder, operationType) {
      let temp = this.folderIdStack;
      if (temp.length == 0) {
        temp = ["0"];
      }
      if (temp[0] == "0") {
        this.getResList();
        this.getParentActivities();
      } else {
        this.axios
          .get(
            "/GeoProblemSolving/rip/" +
              this.activityInfo.aid +
              "/" +
              temp.toString()
          )
          .then((res) => {
            if (res.data == "Offline") {
              // confirm("You are offline, please login again.");
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let list = res.data.data;

              this.$set(this, "activityResList", list);
              this.filterData();
              this.filterRelatedRes();
              // this.filterToolData();

              // show resources
              this.$set(this, "fileList", list);

              if (operationType == "enter") {
                this.folderStack.push({ uid: folder.uid, name: folder.name });
              } else if (operationType == "back") {
                this.folderStack.pop();
              }
            } else {
              this.$Message.warning("Get folder info fail.");
            }
          })
          .catch((err) => {
            this.$Message.warning("Get folder info fail.");
          });
      }
    },
    addFolderModalShow() {
      this.newValidate.setName = "";
      this.newFolderModal = true;
    },
    addFolder(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let pathArray = this.folderIdStack;
          if (pathArray.length == 0) {
            pathArray = ["0"];
          }
          let formData = new FormData();
          formData.append("folderName", this.newValidate.setName);
          formData.append("paths", pathArray.toString());
          formData.append("aid", this.activityInfo.aid);
          this.axios
            .post("/GeoProblemSolving/rip/folder", formData)
            .then((res) => {
              if (res.data == "Offline") {
                // confirm("You are offline, please login again.");
                this.$store.commit("userLogout");
                // this.$router.push({ name: "Login" });
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                this.activityResList.push(res.data.data);
                this.activityDataList.push(res.data.data);
                this.relatedResList.push(res.data.data);

                this.newFolderModal = false;
              } else {
                this.$Message.warning("New folder fail.");
              }
            })
            .catch((err) => {
              this.$Message.warning("New folder fail.");
            });
        }
      });
    },
    deleteFolder() {
      let folder = this.editForeInfo;
      if (confirm("Are you sure to delete this folder?")) {
        let folderId = folder.uid;
        let temp = this.folderIdStack;
        if (temp.length == 0) {
          //重新开辟内存空间的temp,如果直接使用push的话，地址还是指向原数据的地址
          temp = ["0"];
        }
        let formData = new FormData();
        formData.append("uids", folderId);
        formData.append("aid", this.activityInfo.aid);
        formData.append("paths", temp.toString());
        this.axios
          .post("/GeoProblemSolving/rip/del", formData)
          .then((res) => {
            if (res.data == "Offline") {
              // confirm("You are offline, please login again.");
              this.$store.commit("userLogout");
              // this.$router.push({ name: "Login" });
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              //删除用于显示的数据中对应的内容
              for (let i = 0; i < this.activityResList.length; i++) {
                if (this.activityResList[i].uid == folderId) {
                  this.activityResList.splice(i, 1);
                  break;
                }
              }
              for (let i = 0; i < this.activityDataList.length; i++) {
                if (this.activityDataList[i].uid == folderId) {
                  this.activityDataList.splice(i, 1);
                  break;
                }
              }
              for (let i = 0; i < this.relatedResList.length; i++) {
                if (this.relatedResList[i].uid == folderId) {
                  this.relatedResList.splice(i, 1);
                  break;
                }
              }
            } else {
              this.$Message.warning("Delete folder fail.");
            }
          })
          .catch((err) => {
            this.$Message.warning("Delete folder fail.");
          });
        this.editFolderModal = false;
      }
    },
    editFolderModalShow(folder) {
      this.editForeInfo = folder;
      this.editFolderValidate.newName = "";
      this.editFolderModal = true;
    },
    editFolder(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let aid = this.activityInfo.aid;
          let folderId = this.editForeInfo.uid;
          let newFolderName = this.editFolderValidate.newName;
          let temp = this.folderIdStack;
          if (temp.length == 0) {
            temp = ["0"];
          }
          let formData = new FormData();
          formData.append("aid", aid);
          formData.append("folderId", folderId);
          formData.append("newFolderName", newFolderName);
          formData.append("paths", temp.toString());
          this.axios
            .put("/GeoProblemSolving/rip/folder", formData)
            .then((res) => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                // this.$router.push({ name: "Login" });
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                let newNameFolder = {
                  uid: folderId,
                  name: newFolderName,
                  folder: true,
                };

                for (var i = 0; i < this.activityResList.length; i++) {
                  if (this.activityResList[i].uid == folderId) {
                    this.activityResList.splice(i, 1, newNameFolder);
                    break;
                  }
                }
                for (var i = 0; i < this.activityDataList.length; i++) {
                  if (this.activityDataList[i].uid == folderId) {
                    this.activityDataList.splice(i, 1, newNameFolder);
                    break;
                  }
                }
                for (var i = 0; i < this.relatedResList.length; i++) {
                  if (this.relatedResList[i].uid == folderId) {
                    this.relatedResList.splice(i, 1, newNameFolder);
                    break;
                  }
                }
              } else {
                this.$Message.warning("Rename fail.");
              }
            })
            .catch((err) => {
              this.$Message.warning("Rename fail.");
            });
          this.editFolderModal = false;
        }
      });
    },

    fileEditModelShow(fileInfo) {
      let metadata = {};
      this.selectFileInfo = fileInfo;
      this.editFileValidate.privacy = fileInfo.privacy;
      this.editFileValidate.name = fileInfo.name;
      this.editFileValidate.type = fileInfo.type;
      this.oldType = fileInfo.type;
      this.editFileValidate.description = fileInfo.description;
      if(this.editFileValidate.type =="data"){
        metadata = this.operationApi.getResInfo(fileInfo.uid);
        this.oldMetadata = metadata;
        this.editFileValidate.format = metadata.format;
        this.editFileValidate.scale = metadata.scale;
        this.editFileValidate.reference = metadata.reference;
        this.editFileValidate.unit = metadata.unit;
        this.editFileValidate.concept = metadata.concept;
      }
      this.editFileModel = true;
    },
    editFileInfo(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let formData = new FormData();
          let putResInfo = {
            uid: this.selectFileInfo.uid,
            privacy: this.editFileValidate.privacy,
            name: this.editFileValidate.name,
            type: this.editFileValidate.type,
            description: this.editFileValidate.description,
          };
          formData.append("resInfo",JSON.stringify(putResInfo));
          let temp = this.folderIdStack;
          if (temp.length == 0) {
            temp = ["0"];
          }
          this.axios
            .put(
              "/GeoProblemSolving/rip/file/" +
                this.activityInfo.aid +
                "/" +
                temp.toString(),
              formData
            )
            .then((res) => {
              this.editFileModel = false;
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                // this.$router.push({ name: "Login" });
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                this.selectFileInfo.name = this.editFileValidate.name;
                this.selectFileInfo.type = this.editFileValidate.type;
                this.selectFileInfo.description =this.editFileValidate.description;

                for (var i = 0; i < this.activityResList.length; i++) {
                  if (this.activityResList[i].uid == this.selectFileInfo.uid) {
                    this.activityResList.splice(i, 1, this.selectFileInfo);
                    break;
                  }
                }
                for (var i = 0; i < this.activityDataList.length; i++) {
                  if (this.activityDataList[i].uid == this.selectFileInfo.uid) {
                    this.activityDataList.splice(i, 1, this.selectFileInfo);
                    break;
                  }
                }
                for (var i = 0; i < this.relatedResList.length; i++) {
                  if (this.relatedResList[i].uid == this.selectFileInfo.uid) {
                    this.relatedResList.splice(i, 1, this.selectFileInfo);
                    break;
                  }
                }
                let metadata = {};
                if(this.editFileValidate.type == "data"){
                  metadata.format = this.editFileValidate.format;
                  metadata.scale = this.editFileValidate.scale;
                  metadata.reference = this.editFileValidate.reference;
                  metadata.unit = this.editFileValidate.unit;
                  metadata.concept = this.editFileValidate.concept;
                }
                this.operationApi.getActivityDoc(this.activityInfo.aid);
                let operationId = this.operationApi.resOperationRecord(
                  this.activityInfo.aid,
                  "",
                  "",
                  "update",
                  this.userInfo.userId,
                  this.selectFileInfo,
                  metadata,
                );
                let metadataChanged = false;
                let typeChanged = false;
                let type = this.editFileValidate.type;
                if(type != null && type != this.oldType){
                  typeChanged = true
                }
                if(this.editFileValidate.type == "data"){
                  // 检查元数据发生修改
                  if(
                    this.oldMetadata.format != metadata.format ||
                    this.oldMetadata.scale != metadata.scale ||
                    this.oldMetadata.reference != metadata.reference ||
                    this.oldMetadata.unit != metadata.unit ||
                    this.oldMetadata.concept != metadata.concept
                  ){ metadataChanged = true; }
                }
                if(metadataChanged || typeChanged){
                  this.axios
                  .put(`/GeoProblemSolving/activityDoc/typeOrMeta/${this.activityInfo.parent}/${this.activityInfo.aid}/${this.selectFileInfo.uid}`)
                  .then(res=>{
                    console.log(res.data.data)
                  }).catch(err=>{
                    console.log(err.data)
                  })
                }
                // 生成临时操作记录
                let resOperation = {
                  id: operationId,
                  type: "resource",
                  resRef: this.selectFileInfo.uid,
                  operator: this.userInfo.userId,
                };
                this.$store.commit("updateTempOperations", {
                  behavior: "add",
                  operation: resOperation,
                });
              } else {
                this.$Message.warning("Update fail.");
              }
              this.$Notice.success({
                title: "Edit result",
                desc: "Edit successfully",
              });
            })
            .catch((err) => {
              this.$Message.warning("Update fail.");
            });
          this.editFolderModal = false;
        }
      });
    },
    shareToParentModalShow(){
      this.shareToParentModal = true;
      this.fileListChoosed = [];
      for(let i = 0 ; i < this.fileList.length ; i++){
        if(!this.fileList[i].fromParents){
          this.fileListChoosed.push(this.fileList[i]);
        }
      }
      this.selctResToShare = [];
    },
    shareToParent(){
      let addFileList = [];
      for(let i = 0 ; i < this.selctResToShare.length ; i++){
        for( let j = 0 ; j < this.fileList.length ; j++){
          if(this.selctResToShare[i] == this.fileList[j].uid){
            addFileList.push(this.fileList[j]);
          }
        }
      }

      let toAid = this.activityInfo.parent;
      let fromAid = this.activityInfo.aid;
      this.axios
      .post(`/GeoProblemSolving/rip/file/bind/${fromAid}/${toAid}/0`, addFileList)
      .then(res=>{
        if(res.data == 'Offline'){
          suc = false;
          this.$store.commit('userLogout');
        }else if(res.data.code == 0){
          this.shareToParentModal = false;
          this.$Notice.success({
            title: "Sharing result",
            desc: 'Successfully shring to parent activity.'
          })
        }
      })
      .catch(err=>{
        this.$Notice.error({
          title: 'Sharing failed.',
          desc: ''
        })
      })
    },
    shareModalShow() {
      this.shareModal = true;
      this.axios
        .get("/GeoProblemSolving/res/file/all")
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data != "None" && res.data != "Fail") {
            this.userResourceList = res.data.data;
            this.shareModal = true;
          } else if (res.data == "None") {
            this.userResourceList = [];
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    canBeShare(fileId) {
      //判断项目中是否由此文件，如果有，则不能共享
      let result = true;
      for (let i = 0; i < this.activityResList.length; i++) {
        if (this.activityResList[i].uid == fileId) {
          result = false;
        }
      }
      return result;
    },
    // 从个人空间将数据添加到项目中
    shareResources() {
      let addFileList = this.selectedFilesToShare;
      let tempPath = this.folderIdStack;
      if (tempPath.length == 0) {
        tempPath = ["0"];
      }
      this.axios
        .get(
          "/GeoProblemSolving/rip/shareToProject/" +
            this.activityInfo.aid +
            "/" +
            addFileList.toString() +
            "/" +
            tempPath.toString()
        )
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            this.shareModal = false;
            let sharedResult = res.data.data.sharedResult;
            // let sharedFile = res.data.data.sharedFile;
            // let list = this.fileList;
            // for(let j = 0; j < sharedFile.length; j++){
            //   this.activityResList.push[sharedFile[j]];
            //   this.activityDataList.push[sharedFile[j]];
            //   list.push[sharedFile[j]]
            // }
            // this.$set(this, "fileList", list);
            for(let i = 0; i < sharedResult.length; i++){
              let oid = sharedResult[i].oid
              this.operationApi.getActivityDoc(this.activityInfo.aid);
              let operation = this.operationApi.getOperationInfo(oid);
              this.$store.commit('updateTempOperations',{
                behavior: 'add',
                operation: operation
              });
            }
            this.$Message.success("Shared file success!");
            this.selectedFilesToShare = [];
          } else {
            this.$Message.error("Shared file fail!");
          }
        })
        .catch((err) => {
          this.$Message.error("Shared file fail!!!");
        });
    },
    dataVisualize() {},
  },
  filters: {
    filterSizeType(value) {
      if (value === 0) return "0 B";
      let k = 1024;
      let sizes = ["B", "KB", "MB", "GB"];
      let i = Math.floor(Math.log(value) / Math.log(k));
      return (value / Math.pow(k, i)).toPrecision(3) + " " + sizes[i];
    },
  },
};
</script>
<style scoped>
.res-content {
  width: 85px;
  height: 85px;
  float: left;
  margin: 5px;
  cursor: pointer;
}
.res-content-parents {
  width: 85px;
  height: 85px;
  float: left;
  margin: 5px;
  cursor: pointer;
  background-color: #cceeff;
}
.res-content-edit {
  width: 85px;
  height: 85px;
  float: left;
  margin: 5px;
  cursor: pointer;
  background-color: #e8fde8;
}
.res-content-image {
  margin-left: 18px;
  padding-top: 5px;
}
.fileBtnHoverGreen:hover {
  background-color: #19be6b;
  color: white;
}
.fileBtnHoverBlue:hover {
  background-color: dodgerblue;
  color: white;
}
.fileBtnHoverRed:hover {
  background-color: #ed4014;
  color: white;
}
.dataInfo {
  margin: 5px 0;
}
.dataLabel {
  width: 90px;
  display: inline-block;
  font-size: 13px;
  font-weight: bold;
  vertical-align: top;
  color: dodgerblue;
}
.dataContent {
  width: 180px;
  display: inline-block;
  padding-left: 10px;
}
.dataText {
  padding-left: 10px;
  display: inline-block;
  word-break: break-word;
  width: 470px;
}
.toolDataLabel {
  font-weight: bold;
  vertical-align: top;
  color: dodgerblue;
}
.toolDataText {
  font-size: 12px;
  width: 80px;
  word-break: break-word;
  /* display: inline-block; */
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  text-align: center;
}
#toolDataHeader {
  font-size: 12px;
  font-weight: bold;
  padding: 10px 15px;
  border-bottom: 1px solid #e8eaec;
  background-color: #f8f8f9;
}
.resCard {
  max-height: calc(100vh - 165px);
}
.resCard >>> .ivu-card-head {
  padding: 6px 8px;
}
.resCard >>> .ivu-card-body {
  padding: 5px;
}
</style>

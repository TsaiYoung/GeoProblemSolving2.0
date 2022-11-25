<style scoped>
  .folderContent {
    overflow-y: auto;
    padding: 5px;
    margin: 3px;
  }

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

  .fileBtn {
    margin: 0px 3px;
  }

  .itemIcon {
    margin-right: 5px;
  }

  .fileItemName {
    width: 35%;
    margin-right: 5%;
    display: inline-block;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: top;
    cursor: pointer;
    overflow: hidden;
  }

  .fileItemSize {
    width: 10%;
    margin-right: 5%;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    height: 16px;
  }

  .demo-spin-icon-load {
    animation: ani-demo-spin 1s linear infinite;
  }

  @keyframes ani-demo-spin {
    from {
      transform: rotate(0deg);
    }
    50% {
      transform: rotate(180deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .demo-spin-col {
    height: 100px;
    position: relative;
    /* border: 1px solid #eee; */
  }

  /* .personalFileLabel {
    width: 250px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  } */

  .personalFileDes {
    display: inline-block;
    margin: 0 5px;
    width: 250px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .fileBtnHoverBlue:hover {
    background-color: #2db7f5;
    color: white;
  }

  .fileBtnHoverGreen:hover {
    background-color: #19be6b;
    color: white;
  }

  .fileBtnHoverOrange:hover {
    background-color: #ff9900;
    color: white;
  }

  .fileBtnHoverRed:hover {
    background-color: #ed4014;
    color: white;
  }

  .fileBtnHoverGray:hover {
    background-color: #808695;
    color: white;
  }

  .demo-spin-icon-load {
    animation: ani-demo-spin 1s linear infinite;
  }
</style>
<template>
  <div class="fileSpace">
    <Card :padding="1">
      <div slot="title" class="resourceTitle">
        <strong>Resources</strong>
      </div>
      <div
        slot="extra"
        class="resourceBtnDiv"
        v-show="userRole != 'visitor'"
      ></div>

      <!-- 内容 -->
      <div class="folderContent">
        <Card v-if="folderStack.length > 0" :padding="5" dis-hover>
          <div style="display: flex; align-items: center">
            <div
              style="min-width: 60px"
              v-show="currentFolder.files.length > 0"
            >
              <!--              indeterminate 表示有选中内容
                                checkAll则是全选
              -->
              <Checkbox
                :indeterminate="indeterminate"
                :value="checkAll"
                @click.prevent.native="handleCheckAll"
                v-show="currentFolder.files.length > 0"
                style="align-items: center"
              >All files
              </Checkbox>
            </div>
            <div style="margin-left: 3px">
              <Breadcrumb>
                <BreadcrumbItem
                  v-for="(folder, index) in folderStack"
                  :key="folder.uid"
                  @click.native="switchFolder(folder, index)"
                  style="cursor: pointer"
                >
                  <span v-if="folder.uid == activityInfo.aid"
                  ><Icon type="md-folder" style="color: #2d8cf0"
                  /></span>
                  <span v-else style="color: #2d8cf0">{{ folder.name }}</span>
                </BreadcrumbItem>
              </Breadcrumb>
            </div>
            <Divider type="vertical" style="margin-left: 20px"/>
            <div style="flex: 1; margin-left: 10px">
              <Tooltip content="Back" placement="bottom" class="fileBtn">
                <Icon
                  type="md-arrow-round-back"
                  @click="backforeFolder"
                  style="cursor: pointer; color: #08ab2b"
                />
              </Tooltip>
              <Tooltip content="New folder" placement="bottom" class="fileBtn">
                <Icon
                  type="ios-folder"
                  @click="addFolderModalShow"
                  style="cursor: pointer; color: #f9c245"
                />
              </Tooltip>
            </div>
            <div style="align-items: flex-end" v-if="userRole != 'visitor'">
              <Tooltip content="Download" placement="bottom" class="fileBtn">
                <Button
                  @click="downloadSelectFile"
                  v-show="currentFolder.files.length > 0"
                  shape="circle"
                  icon="md-cloud-download"
                  class="fileBtnHoverGray"
                  v-if="
                    permissionIdentity(
                      activityInfo.permission,
                      userRole,
                      'use_resource'
                    )
                  "
                ></Button>
              </Tooltip>
              <Tooltip
                content="Upload files"
                placement="bottom"
                class="fileBtn"
              >
                <Button
                  @click="uploadModalShow"
                  shape="circle"
                  icon="md-cloud-upload"
                  class="fileBtnHoverGreen"
                  v-if="
                    permissionIdentity(
                      activityInfo.permission,
                      userRole,
                      'upload_resource'
                    )
                  "
                ></Button>
              </Tooltip>
              <Tooltip
                content="Share personal files"
                placement="left"
                class="fileBtn"
              >
                <Button
                  @click="shareModalShow"
                  shape="circle"
                  icon="ios-copy"
                  class="fileBtnHoverOrange"
                ></Button>
              </Tooltip>
            </div>
          </div>
        </Card>
        <div
          v-if="
            currentFolder.folders.length > 0 || currentFolder.files.length > 0
          "
        >
          <vue-scroll :ops="ops" :style="{ height: contentHeight + 'px' }">
            <Card
              v-for="(folder, index) in currentFolder.folders"
              :key="index"
              :padding="5"
            >
              <div>
                <Icon type="ios-folder-open" class="itemIcon" size="25"/>
                <a
                  @click="enterFolder(folder)"
                  class="fileItemName"
                  :title="folder.name"
                >{{ folder.name }}</a
                >

                <!--                文件夹结构-->
                <div style="float: right">
                  <Button
                    @click="renameFolderModalShow(folder)"
                    class="fileBtnHoverBlue"
                    shape="circle"
                    icon="ios-create"
                    title="Rename"
                    size="small"
                    type="text"
                    v-if="
                      permissionIdentity(
                        activityInfo.permission,
                        userRole,
                        'manage_resource'
                      )
                    "
                  ></Button>
                  <Button
                    @click="deleteFolder(folder)"
                    class="fileBtnHoverRed"
                    shape="circle"
                    icon="ios-trash"
                    title="Delete"
                    size="small"
                    style="margin-left: 5px"
                    type="text"
                    v-if="
                      permissionIdentity(
                        activityInfo.permission,
                        userRole,
                        'manage_resource'
                      )
                    "
                  ></Button>
                </div>
              </div>
            </Card>
            <CheckboxGroup
              v-model="chooseFilesArray"
              @on-change="checkAllGroupChange"
            >
              <Card
                v-for="file in currentFolder.files"
                :key="file.uid"
                :padding="5"
              >
                <Checkbox :label="file.address">&nbsp;</Checkbox>
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
                <Icon
                  v-else
                  type="ios-create-outline"
                  class="itemIcon"
                  size="25"
                />
                <span
                  @click="getFileInfo(file)"
                  class="fileItemName"
                  :title="file.name + file.suffix"
                >{{ file.name + file.suffix }}</span
                >
                <span class="fileItemSize">{{
                  file.fileSize | filterSizeType
                }}</span>
                <span style="width: 20%; margin-right: 5%">{{
                  file.uploadTime | filterTimeStyle
                }}</span>

                <!--                使用资源-->
                <div
                  style="float: right"
                  v-if="
                    permissionIdentity(
                      activityInfo.permission,
                      userRole,
                      'use_resource'
                    )
                  "
                >
                  <!--                  <Button-->
                  <!--                    @click="filePreview(file)"-->
                  <!--                    shape="circle"-->
                  <!--                    icon="md-eye"-->
                  <!--                    title="Preview"-->
                  <!--                    size="small"-->
                  <!--                    class="fileBtnHoverGreen"-->
                  <!--                    type="text"-->
                  <!--                  ></Button>-->

                  <Button
                    @click="fileDownload(file)"
                    shape="circle"
                    icon="ios-cloud-download"
                    title="Download"
                    size="small"
                    class="fileBtnHoverGray"
                    type="text"
                  ></Button>
                  <!--                  复制到个人空间-->
                  <Button
                    @click="showCopyFileModel(file)"
                    shape="circle"
                    icon="ios-share-alt"
                    title="Copy to personal center"
                    size="small"
                    class="fileBtnHoverOrange"
                    type="text"
                  ></Button>

                  <!--                  管理资源-->
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
                      @click="fileEditModelShow(file)"
                      shape="circle"
                      icon="md-create"
                      title="Edit info"
                      size="small"
                      class="fileBtnHoverBlue"
                      type="text"
                    ></Button>
                    <Button
                      @click="fileDelete(file)"
                      shape="circle"
                      icon="ios-trash"
                      title="Remove"
                      size="small"
                      class="fileBtnHoverRed"
                      type="text"
                    ></Button>
                  </template>
                </div>
              </Card>
            </CheckboxGroup>
          </vue-scroll>
        </div>
        <div v-else style="text-align: center">
          <div style="color: lightgray; font-size: 2em; font-weight: bold">
            No file or folder
          </div>
        </div>
      </div>
    </Card>
    <Modal
      v-model="renameFolderModal"
      title="Rename folder"
      ok-text="Assure"
      cancel-text="Cancel"
    >
      <Form
        ref="renameValidate"
        :model="renameValidate"
        :rules="renameRuleValidate"
        :label-width="80"
      >
        <FormItem label="New name" prop="newName">
          <Input
            v-model="renameValidate.newName"
            :rows="4"
            placeholder="Enter the name for folder..."
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="renameFolderModal = false">Cancel</Button>
        <Button type="success" @click="renameFolder('renameValidate')"
        >Rename
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

    <Modal v-model="uploadModal" title="Upload file" width="600">
      <Form
        ref="uploadValidate"
        :model="uploadValidate"
        :rules="uploadRuleValidate"
        :label-width="100"
        label-position="left"
        inline
      >
        <FormItem label="Privacy" prop="privacy">
          <RadioGroup v-model="uploadValidate.privacy" style="width: 100%">
            <Radio label="private">Private</Radio>
            <Radio label="public">Public</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Type" prop="type">
          <RadioGroup v-model="uploadValidate.type">
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
            v-model="uploadValidate.description"
          />
        </FormItem>
        <Divider orientation="left" v-if="uploadValidate.type == 'data'">Metadata</Divider>
        <FormItem label="Format" prop="format" v-if="uploadValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadValidate.format"
          />
        </FormItem>
        <FormItem label="Scale" prop="scale" v-if="uploadValidate.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadValidate.scale"
          />
        </FormItem>
        <FormItem label="Reference" prop="reference" v-if="uploadValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadValidate.reference"
          />
        </FormItem>
        <FormItem label="Unit" prop="unit" v-if="uploadValidate.type == 'data'" :label-width="50">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadValidate.unit"
          />
        </FormItem>
        <FormItem label="Concept" prop="concept" v-if="uploadValidate.type == 'data'" :label-width="70">
          <Input
            type="text"
            style="width:200px;"
            v-model="uploadValidate.concept"
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
      <div style="padding: 0 10px 0 10px; max-height: 200px; overflow-y: auto">
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
        <Button @click="uploadModal = false">Cancel</Button>
        <Button type="success" @click="folderUpload('uploadValidate')"
        >Upload
        </Button>
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
    <Modal v-model="fileInfoModal" title="File Info">
      <Table
        :columns="selectedFileColumns"
        :data="selectedFileData"
        stripe
        border
        :show-header="false"
      ></Table>
      <div slot="footer">
        <Button type="primary" @click="fileInfoModal = false">OK</Button>
      </div>
    </Modal>
    <Modal
      v-model="shareModal"
      title="Share file from personal center"
      width="600"
      :mask-closable="false"
    >
      <div>
        <vue-scroll :ops="ops" style="height: 300px">
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
                <Icon
                  v-else
                  type="ios-create-outline"
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
              <span
                style="display: inline-block; vertical-align: top; float: right"
              >{{ file.fileSize | filterSizeType }}</span
              >
            </Card>
          </CheckboxGroup>
        </vue-scroll>
      </div>
      <div slot="footer" style="display: inline-block">
        <i-button type="primary" @click="shareFile()" style="float: right"
        >Submit
        </i-button>
        <i-button
          @click="closeshareModel()"
          style="float: right; margin-right: 15px"
        >Cancel
        </i-button>
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
            :rows="4"
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
    <Modal
      v-model="copyFileModal"
      title="Copy file to personal center"
      width="500"
    >
      <RadioGroup v-model="copyFilePrivacy">
        <Radio label="public">Public</Radio>
        <Radio label="private">Private</Radio>
      </RadioGroup>
      <div slot="footer">
        <Button @click="copyFileModal = false">Cancel</Button>
        <Button type="success" @click="copyFileToCenter">Copy</Button>
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>
<script>
  import loginModal from "../user/userState/loginModal.vue";

  export default {
    components: {
      loginModal,
    },
    props: ["activityInfo"],
    data() {
      return {
        userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
        userRole: "visitor",
        currentFolder: {
          folders: [],
          files: [],
        },
        pathStr: "",
        folderStack: [{uid: 0, name: "Home"}],
        folderIdStack: [],
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
        renameForeInfo: {},
        renameFolderModal: false,
        renameValidate: {
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
        oldMetadata: {},
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
        uploadModal: false,
        uploadValidate: {
          privacy: "private",
          type: "data",
          description: "",
          format: "",
          scale: "",
          reference: "",
          unit: "",
          concept: "",
        },
        uploadRuleValidate: {
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
          // description: [
          //   {
          //     required: false,
          //     message: "file description cannot be empty",
          //     trigger: "blur",
          //   },
          // ],
        },
        toUploadFiles: [],
        fileCountTimer: null,
        progressModalShow: false,
        uploadProgress: 0,
        fileInfoModal: false,
        selectedFileColumns: [
          {
            title: "key",
            key: "key",
            minWidth: 10,
            width: 110,
          },
          {
            title: "value",
            key: "value",
          },
        ],
        selectedFileData: [],
        panel: null,
        // 单选选中的名称数组
        chooseFilesArray: [],
        // 关于单选多选的按钮
        indeterminate: true,
        checkAll: false,
        oldType: '',
        shareModal: false,
        editFileModel: false,
        userResourceList: [],
        selectedFilesToShare: [],
        ops: {
          bar: {
            background: "#808695",
          },
        },
        contentHeight: 100,
        copyFileModal: false,
        copyFilePrivacy: "private",
        selectedFile: {},
        delCount: 0,
        putFileInfo: {},
        tempLoginModal: false,
        downResId: "",
        downResIds: "",
        resProxy: this.$store.getters.resProxy
      };
    },
    mounted() {
      this.initSize();
      this.getResList();
      this.roleIdentity();
      window.addEventListener("resize", this.initSize);
    },
    beforeDestroy: function () {
      window.removeEventListener("resize", this.initSize);
    },
    methods: {
      changeLoginModal(status) {
        this.tempLoginModal = status;
      },
      getResList: function () {
        this.axios
          .get("/GeoProblemSolving/rip/" + this.activityInfo.aid + "/0")
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let rootRes = res.data.data;
              this.resToCurrentFolder(rootRes);
              this.indeterminate = false;
              this.checkAll = false;
            }
          })
          .catch((err) => {
            throw err;
          });
      },
      initSize() {
        this.contentHeight = window.innerHeight - 400;
      },
      roleIdentity() {
        this.userRole = this.userRoleApi.roleIdentify(
          this.activityInfo.members,
          this.userInfo.userId
        );
      },
      permissionIdentity(permission, role, operation) {
        return this.userRoleApi.permissionIdentity(
          JSON.parse(permission),
          role,
          operation
        );
      },
      //工具方法，资源list填充currentFolder
      resToCurrentFolder: function (rootRes) {
        this.currentFolder.folders = [];
        this.currentFolder.files = [];
        for (let i = 0; i < rootRes.length; i++) {
          if (rootRes[i].folder) {
            this.currentFolder.folders.push(rootRes[i]);
          } else {
            this.currentFolder.files.push(rootRes[i]);
          }
        }
        // console.log(this.currentFolder.files)
      },
      enterFolder(currentFolder) {
        this.chooseFilesArray = [];
        this.checkAll = false;
        this.indeterminate = false;
        this.folderIdStack.unshift(currentFolder.uid);
        this.changeFolder(currentFolder, "enter");
      },
      backforeFolder() {
        this.chooseFilesArray = [];
        this.checkAll = false;
        this.indeterminate = false;
        if (this.folderStack.length != 1) {
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
      switchFolder(folder, index) {
        this.delCount = this.folderStack.length - index - 1;
        this.folderIdStack.splice(0, this.delCount);
        this.changeFolder(folder, "switch");
      },
      changeFolder(folder, operationType) {
        let temp = this.folderIdStack;
        if (temp.length == 0) {
          temp = ["0"];
        }
        console.log("paths: " + temp.toString());
        this.axios
          .get(
            "/GeoProblemSolving/rip/" +
            this.activityInfo.aid +
            "/" +
            temp.toString()
          )
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let folderInfo = res.data.data;
              this.resToCurrentFolder(folderInfo);

              if (operationType == "enter") {
                this.folderStack.push({uid: folder.uid, name: folder.name});
                console.log(this.currentFolder);
              } else if (operationType == "back") {
                this.folderStack.pop();
              } else if (operationType == "switch") {
                for (let i = 0; i < this.delCount; i++) {
                  this.folderStack.pop();
                }
              }
            } else {
              this.$Message.warning("Get folder info fail.");
            }
          })
          .catch((err) => {
            this.$Message.warning("Get folder info fail.");
          });
      },
      getFileInfo(file) {
        this.selectedFileData = [
          {
            key: "File name",
            value: file.name,
          },
          {
            key: "Description",
            value: file.description,
          },
          {
            key: "Type",
            value: file.type,
          },
          {
            key: "File size",
            value: this.$options.filters['filterSizeType'](file.fileSize),
          },
          {
            key: "Uploader",
            value: file.uploaderName,
          },
          {
            key: "Upload Time",
            value: this.$options.filters['filterTimeStyle'](file.uploadTime),
          },
        ];
        this.fileInfoModal = true;
      },
      addFolderModalShow() {
        this.newValidate.setName = "";
        this.newFolderModal = true;
      },
      addFolder: function (name) {
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
                  this.$store.commit("userLogout");
                  this.tempLoginModal = true;
                } else if (res.data.code == 0) {
                  this.currentFolder.folders.push(res.data.data);
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
      deleteFolder(folder) {
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
                this.$store.commit("userLogout");
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                //删除用于显示的数据中对应的内容
                for (let i = 0; i < this.currentFolder.folders.length; i++) {
                  if (this.currentFolder.folders[i].uid == folderId) {
                    this.currentFolder.folders.splice(i, 1);
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
        }
      },
      renameFolderModalShow(folder) {
        this.renameForeInfo = folder;
        this.renameValidate.newName = "";
        this.renameFolderModal = true;
      },
      renameFolder(name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            let aid = this.activityInfo.aid;
            let folderId = this.renameForeInfo.uid;
            let newFolderName = this.renameValidate.newName;
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
                  this.tempLoginModal = true;
                } else if (res.data.code == 0) {
                  let newNameFolder = {
                    uid: folderId,
                    name: newFolderName,
                    folder: true,
                  };
                  for (var i = 0; i < this.currentFolder.folders.length; i++) {
                    if (this.currentFolder.folders[i].uid == folderId) {
                      this.currentFolder.folders.splice(i, 1, newNameFolder);
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
            this.renameFolderModal = false;
          }
        });
      },
      uploadModalShow() {
        this.uploadValidate = {
          privacy: "private",
          type: "data",
          description: "",
          format: "",
          scale: "",
          reference: "",
          unit: "",
          concept: "",
        };
        this.toUploadFiles = [];
        this.uploadModal = true;
      },
      gatherFile(file) {
        let that = this;
        if (that.toUploadFiles.length >= 5) {
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
          that.toUploadFiles.push(file);
        }
        return false;
      },
      delFileList(index) {
        this.toUploadFiles.splice(index, 1);
      },
      folderUpload(name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            let uploadFiles = this.toUploadFiles;
            if (uploadFiles.length > 0) {
              this.uploadModal = false;
              let formData = new FormData();
              for (let i = 0; i < uploadFiles.length; i++) {
                formData.append("file", uploadFiles[i]);
              }
              let temp = this.folderIdStack;
              if (temp.length == 0) {
                temp = ["0"];
              }
              formData.append("description", this.uploadValidate.description);
              formData.append("type", this.uploadValidate.type);
              formData.append("privacy", this.uploadValidate.privacy);
              formData.append("aid", this.activityInfo.aid);
              formData.append("paths", temp.toString());
              formData.append("graphId", this.activityInfo.parent);
              // 上传元数据
              if (this.uploadValidate.type == "data"){
                formData.append("format", this.uploadValidate.format);
                formData.append("scale", this.uploadValidate.scale);
                formData.append("reference", this.uploadValidate.reference);
                formData.append("unit", this.uploadValidate.unit);
                formData.append("concept", this.uploadValidate.concept);
              }
              this.progressModalShow = true;
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
                    // multi 无actionContainer, 无需显示操作
                    // let uploadedOperation = res.data.uploadedOperation;
                    this.operationApi.getActivityDoc(this.activityInfo.aid);
                    let metadata = {};
                    for (var i = 0; i < uploadedList.length; i++) {
                      this.currentFolder.files.push(uploadedList[i]);
                      // if (this.uploadValidate.type == "data") {
                      //   metadata.format = this.uploadValidate.format;
                      //   metadata.scale = this.uploadValidate.scale;
                      //   metadata.reference = this.uploadValidate.reference;
                      //   metadata.unit = this.uploadValidate.unit;
                      //   metadata.concept = this.uploadValidate.concept;
                      // }
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
                    if (uploadedList.length > 0) {
                      let sucFileName = uploadedList.map((item) => {
                        return item.name + item.suffix;
                      });
                      this.$Notice.success({
                        title: "Upload result",
                        desc: "Upload successfully",
                        render: (h) => {
                          return h("span", sucFileName.join("; "));
                        }
                      });
                    }
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
              this.$Message.warning("File is not existing.");
            }
          }
        });
      },
      filePreview(fileInfo) {
        let name = fileInfo.name + fileInfo.suffix;
        if (/\.(doc|docx|xls|xlsx|ppt|pptx)$/.test(name.toLowerCase())) {
          this.$Modal.confirm({
            title: "Note",
            content:
              "<p>You selected file will be previewed through</p><p style='font-size:16px;font-weight:bold'>Microsoft office online service</p>",
            onOk: () => {
              if (this.panel != null) {
                this.panel.close();
              }
              var url =
                "http://view.officeapps.live.com/op/view.aspx?src=" +
                "http://" +
                this.$store.state.DataServer +
                fileInfo.address;
              var toolURL =
                "<iframe src=" +
                url +
                ' style="width: 100%;height:100%" frameborder="0"></iframe>';
              this.panel = jsPanel.create({
                headerControls: {
                  smallify: "remove",
                },
                theme: "primary",
                footerToolbar: '<p style="height:5px"></p>',
                headerTitle: "Preview",
                contentSize: "800 600",
                content: toolURL,
                disableOnMaximized: true,
                dragit: {
                  containment: 5,
                },
                closeOnEscape: true,
              });
              $(".jsPanel-content").css("font-size", "0");
              this.$emit("toolPanel", this.panel);
            },
            onCancel: () => {
              return;
            },
          });
        } else if (/\.(mp4)$/.test(name.toLowerCase())) {
          if (this.panel != null) {
            this.panel.close();
          }
          var url = "http://" + this.$store.state.DataServer + fileInfo.address;
          var toolURL =
            "<video src=" +
            url +
            ' style="width: 100%;height:100%" controls></video>';
          this.panel = jsPanel.create({
            headerControls: {
              smallify: "remove",
            },
            theme: "primary",
            footerToolbar: '<p style="height:10px"></p>',
            headerTitle: "Preview",
            contentSize: "800 600",
            content: toolURL,
            disableOnMaximized: true,
            dragit: {
              containment: 5,
            },
            closeOnEscape: true,
          });
          $(".jsPanel-content").css("font-size", "0");
          this.$emit("toolPanel", this.panel);
        } else if (/\.(pdf|json|md|gif|jpg|png)$/.test(name.toLowerCase())) {
          if (this.panel != null) {
            this.panel.close();
          }
          var url = "http://" + this.$store.state.DataServer + fileInfo.address;
          var toolURL =
            "<iframe src=" +
            url +
            ' style="width: 100%;height:100%" frameborder="0" controls></iframe>';
          this.panel = jsPanel.create({
            headerControls: {
              smallify: "remove",
            },
            theme: "primary",
            footerToolbar: '<p style="height:10px"></p>',
            headerTitle: "Preview",
            contentSize: "800 600",
            content: toolURL,
            disableOnMaximized: true,
            dragit: {
              containment: 5,
            },
            closeOnEscape: true,
          });
          $(".jsPanel-content").css("font-size", "0");
          this.$emit("toolPanel", this.panel);
        } else {
          this.$Notice.error({
            title: "Open failed",
            desc: "Sorry. Unsupported file format.",
          });
          return false;
        }
      },
      fileDelete(fileInfo) {
        if (confirm("Are you sure to delete ?")) {
          let fileId = fileInfo.uid;
          let temp = this.folderIdStack;
          if (temp.length == 0) {
            temp = ["0"];
          }
          let formData = new FormData();
          formData.append("uids", fileId);
          formData.append("aid", this.activityInfo.aid);
          formData.append("paths", temp.toString());

          this.axios
            .post("/GeoProblemSolving/rip/del", formData)
            .then((res) => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.tempLoginModal = true;
              } else if (res.data.code == 0) {
                for (let i = 0; i < this.currentFolder.files.length; i++) {
                  if (this.currentFolder.files[i].uid == fileId) {
                    this.currentFolder.files.splice(i, 1);
                    break;
                  }
                }
              } else {
                this.$Message.warning("Delete file fail.");
              }
            })
            .catch((err) => {
              this.$Message.warning("Delete file fail.");
            });
        }
      },
      handleCheckAll() {
        if (this.indeterminate) {
          this.checkAll = false;
        } else {
          this.checkAll = !this.checkAll;
        }
        this.indeterminate = false;
        if (this.checkAll) {
          this.currentFolder.files.forEach((item) => {
            this.chooseFilesArray.push(item.address);
          });
        } else {
          this.chooseFilesArray = [];
        }
      },
      checkAllGroupChange(data) {
        if (data.length == this.currentFolder.files.length) {
          this.indeterminate = false;
          this.checkAll = true;
        } else if (data.length > 0) {
          this.indeterminate = true;
          this.checkAll = false;
        } else {
          this.indeterminate = false;
          this.checkAll = false;
        }
      },
      download(blobUrl) {
        const a = document.createElement("a");
        a.style.display = "none";
        a.download = "package.zip";
        a.href = this.resProxy + "/batchData?oids=" + blobUrl;
        a.target = "_self";
        a.click();
        a.remove();
      },
      downloadSelectFile() {
        let chooseFileUrls = this.chooseFilesArray;
        let temp = [];
        if (chooseFileUrls.length != 0) {
          for (let i = 0; i < chooseFileUrls.length; i++) {
            if (typeof (chooseFileUrls[i]) == "string") {
              temp.push(chooseFileUrls[i].slice(-36));
            }
          }
          this.download(temp.toString());
        }
      },
      shareModalShow() {
        this.shareModal = true;
        this.axios
          .get("/GeoProblemSolving/res/file/all")
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
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
      closeshareModel() {
        this.shareModal = false;
      },
      // 从个人空间分享
      shareFile() {
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
            this.shareModal = false;
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.tempLoginModal = true;
            } else if (res.data.code == 0) {
              let sharedResult = res.data.data.sharedResult;
              let sharedFile = res.data.data.sharedFile;
              for (let i = 0; i < sharedFile.length; i++) {
                this.currentFolder.files.push(sharedFile[i]);
              }
            //   for(let i = 0; i < sharedResult.length; i++){
            //   let oid = sharedResult[i].oid
            //   this.operationApi.getActivityDoc(this.activityInfo.aid);
            //   let operation = this.operationApi.getOperationInfo(oid);
            //   this.$store.commit('updateTempOperations',{
            //     behavior: 'add',
            //     operation: operation
            //   });
            // }
              this.$Message.success("Shared file success!");
              this.selectedFilesToShare = [];
            } else {
              this.$Message.error("Shared file fail!");
            }
          })
          .catch((err) => {
            this.$Message.error("Shared file fail!");
          });
      },
      fileDownload(fileInfo) {
        let address = fileInfo.address;
        let uid = "";
        if (address != "" && typeof (address) == "string") {
          uid = address.slice(-36);
        }
        const a = document.createElement("a");
        a.style.display = "none";
        a.href = this.resProxy + "/data/" + uid;
        a.target = "_self";
        a.click();
        a.remove();
      },
      fileEditModelShow(fileInfo) {
        let metadata = {};
        this.putFileInfo = fileInfo;
        this.editFileValidate.privacy = fileInfo.privacy;
        this.editFileValidate.name = fileInfo.name;
        this.editFileValidate.type = fileInfo.type;
        this.editFileValidate.description = fileInfo.description;
        if (this.editFileValidate.type == "data") {
          metadata = this.operationApi.getResInfo(fileInfo.uid);
          if(metadata == undefined || metadata == null || metadata == ""){
            this.oldMetadata = {};
            this.editFileValidate.format = "";
            this.editFileValidate.scale = "";
            this.editFileValidate.reference = "";
            this.editFileValidate.unit = "";
            this.editFileValidate.concept = "";
          } else {
            this.oldMetadata = metadata;
            this.editFileValidate.format = metadata.format;
            this.editFileValidate.scale = metadata.scale;
            this.editFileValidate.reference = metadata.reference;
            this.editFileValidate.unit = metadata.unit;
            this.editFileValidate.concept = metadata.concept;
          }
        }
        this.editFileModel = true;
      },
      editFileInfo(name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            let formData = new FormData();
            let putResInfo = {
              uid: this.putFileInfo.uid,
              privacy: this.editFileValidate.privacy,
              name: this.editFileValidate.name,
              type: this.editFileValidate.type,
              description: this.editFileValidate.description,
            };
            let temp = this.folderIdStack;
            if (temp.length == 0) {
              temp = ["0"];
            }
            formData.append("resInfo", JSON.stringify(putResInfo));
            formData.append("graphicId", this.activityInfo.parent);
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
                  this.tempLoginModal = true;
                } else if (res.data.code == 0) {
                  this.putFileInfo.privacy = this.editFileValidate.privacy;
                  this.putFileInfo.name = this.editFileValidate.name;
                  this.putFileInfo.type = this.editFileValidate.type;
                  this.putFileInfo.description =this.editFileValidate.description;
                  for (var i = 0; i < this.currentFolder.files.length; i++) {
                    if (
                      this.currentFolder.files[i].resourceId ==
                      this.putFileInfo.uid
                    ) {
                      this.currentFolder.files.splice(i, 1, this.putFileInfo);
                      break;
                    }
                  }
                  let metadata = {};
                  if (this.editFileValidate.type == "data") {
                    metadata.format = this.editFileValidate.format;
                    metadata.scale = this.editFileValidate.scale;
                    metadata.reference = this.editFileValidate.reference;
                    metadata.unit = this.editFileValidate.unit;
                    metadata.concept = this.editFileValidate.concept;
                  }
                  let operationId = this.operationApi.resOperationRecord(
                    this.activityInfo.aid,
                    "",
                    "",
                    "update",
                    this.userInfo.userId,
                    this.putFileInfo,
                    metadata,
                  );
                  let metadataChanged = false;
                  let typeChanged = false;
                  let type = this.editFileValidate.type;
                  if(type != null && type != this.oldType){
                    typeChanged = true;
                  }
                  if (this.editFileValidate.type == "data") {
                    // 检查元数据发生修改
                    if (
                      this.oldMetadata.format != metadata.format ||
                      this.oldMetadata.scale != metadata.scale ||
                      this.oldMetadata.reference != metadata.reference ||
                      this.oldMetadata.unit != metadata.unit ||
                      this.oldMetadata.concept != metadata.concept
                    ) {
                      metadataChanged = true;
                    }
                  }
                  if(typeChanged || metadataChanged){
                    this.axios
                    .put(`/GeoProblemSolving/activityDoc/typeOrMeta/${this.activityInfo.parent}/${this.activityInfo.aid}/${this.putFileInfo.uid}`)
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
                    resRef: this.putFileInfo.uid,
                    operator: this.userInfo.userId,
                  };
                  this.$store.commit("updateTempOperations", {
                    behavior: "add",
                    operation: resOperation,
                  });
                  this.$Message.success("Edit resource success!");
                } else {
                  this.$Message.warning("Update fail.");
                }
              })
              .catch((err) => {
                this.$Message.warning("Update fail.");
              });
            this.renameFolderModal = false;
          }
        });
      },
      canBeShare(fileId) {
        //判断项目中是否由此文件，如果有，则不能共享
        let result = true;
        for (let i = 0; i < this.currentFolder.files.length; i++) {
          if (this.currentFolder.files[i].uid == fileId) {
            result = false;
          }
        }
        return result;
      },
      showCopyFileModel(file) {
        this.copyFileModal = true;
        this.copyFilePrivacy = "private";
        this.selectedFile = file;
      },
      copyFileToCenter() {
        this.$Spin.show();
        this.selectedFile.privacy = this.copyFilePrivacy;
        this.axios
          .post("/GeoProblemSolving/res/file/copyToCenter", this.selectedFile)
          .then((res) => {
            this.$Spin.hide();
            this.copyFileModal = false;
            if (res.data.code == 0) {
              this.$Message.success("Copy file success.");
            } else {
              this.$Message.warning(res.data.data);
            }
          })
          .catch((err) => {
            this.$Message.error("Copy file fail.");
          });
      },
    }
    ,
    filters: {
      filterSizeType(value) {
        if (value === 0) return "0 B";
        let k = 1024;
        let sizes = ["B", "KB", "MB", "GB"];
        let i = Math.floor(Math.log(value) / Math.log(k));
        return (value / Math.pow(k, i)).toPrecision(3) + " " + sizes[i];
      }
      ,
      filterTimeStyle(str) {
        if (str != null) {
          let result = str.split('.')[0];
          return result.replace('T', " ");
        }
      }
    }
    ,
  }
  ;
</script>

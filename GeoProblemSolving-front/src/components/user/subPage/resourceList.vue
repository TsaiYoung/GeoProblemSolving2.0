<template>
  <div class="fileSpace">
    <div id="title">
      <h1 style="text-align: center;margin-top: 10px;">Resource</h1>
      <h3 style="text-align: center;margin-bottom: 10px;">You can manage your resources here</h3>
    </div>
    <Row>
      <Col span="22" offset="1">
        <div>
          <Card :padding="1" dis-hover style="height: calc(100vh - 210px);" class="customCard">
            <!-- <div
              slot="extra"
              class="resourceBtnDiv"
            ></div> -->
            <!-- 内容 -->
            <div class="folderContent">
              <Card v-if="folderStack.length > 0" :padding="5" dis-hover>
                <div style="display: flex; align-items: center">
                  <!--        all file 显示，如果file>0则显示 -->
                  <div
                    style="min-width: 60px"
                    v-show="currentFolder.files.length > 0"
                  >
                    <Checkbox
                      :indeterminate="indeterminate"
                      :value="checkAll"
                      @click.prevent.native="handleCheckAll"
                      v-show="currentFolder.files.length > 0"
                      style="align-items: center"
                    ><strong> All files</strong>
                    </Checkbox
                    >
                  </div>
                  <!--            面包屑显示 -->
                  <div style="margin-left: 3px">
                    <Breadcrumb>
                      <BreadcrumbItem
                        v-for="(folder, index) in folderStack"
                        :key="folder.uid"
                        style="cursor: pointer"
                        @click.native="switchFolder(folder, index)"
                      >
                        <span style="color: #2d8cf0">{{ folder.name }}</span>
                      </BreadcrumbItem>
                    </Breadcrumb>
                  </div>
                  <Divider type="vertical" style="margin-left: 20px"/>
                  <!--           返回及新建文件夹  -->
                  <div style="flex: 1; margin-left: 10px">
                    <Tooltip content="Back" placement="bottom" class="fileBtn">
                      <Icon type="md-arrow-round-back" @click="backforeFolder" style="cursor:pointer; color: #08ab2b"
                            size="20"/>
                    </Tooltip>
                    <Tooltip content="New folder" placement="bottom" class="fileBtn">
                      <Icon type="ios-folder" @click="addFolderModalShow" style="cursor:pointer; color: #f9c245"
                            size="20"/>
                    </Tooltip>
                  </div>
                  <!--            上传及下载内容 -->
                  <div style="align-items: flex-end">
                    <Tooltip content="Download" placement="bottom" class="fileBtn">
                      <Button
                        @click="downloadSelectFile"
                        v-show="currentFolder.files.length > 0"
                        shape="circle"
                        icon="md-cloud-download"
                        class="fileBtnHoverGray"
                      ></Button>
                    </Tooltip>
                    <Tooltip content="Upload files" placement="left" class="fileBtn">
                      <Button
                        @click="uploadModalShow"
                        shape="circle"
                        icon="md-cloud-upload"
                        class="fileBtnHoverGreen"
                      ></Button>
                    </Tooltip>
                    <!--              资源共享-->

                    <Tooltip
                      content="Share personal files"
                      placement="left"
                      class="fileBtn"
                    >
                      <!--                资源分享，后面再来理吧-->
                      <!--                <Button-->
                      <!--                  @click="shareModalShow"-->
                      <!--                  shape="circle"-->
                      <!--                  icon="ios-copy"-->
                      <!--                  class="fileBtnHoverOrange"-->
                      <!--                ></Button>-->
                    </Tooltip>
                  </div>
                </div>
              </Card>

              <!--        资源显示区-->
              <div
                v-if="
                      currentFolder.folders.length > 0 || currentFolder.files.length > 0
                    "
              >
                <vue-scroll :ops="ops" :style="{ height: contentHeight + 'px' }">
                  <!--            folder 内容 -->
                  <Card
                    v-for="folder in currentFolder.folders"
                    :key="folder.uid"
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

                      <!--            -->
                      <div style="float: right">
                        <Button
                          @click="renameFolderModalShow(folder)"
                          class="fileBtnHoverBlue"
                          shape="circle"
                          icon="ios-create"
                          title="Rename"
                          size="small"
                          type="text"
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
                        ></Button>
                      </div>
                    </div>
                  </Card>

                  <!--            文件内容  -->
                  <CheckboxGroup
                    v-model="chooseFilesArray"
                    @on-change="checkAllGroupChange"
                  >
                    <Card
                      v-for="file in currentFolder.files"
                      :key="file.uid"
                      :padding="5"
                      dis-hover
                    >
                      <Checkbox :label="file.address">&nbsp;</Checkbox>
                      <Icon v-if="file.type === 'data'" type="ios-podium-outline" class="itemIcon" size="25"/>
                      <Icon v-else-if="file.type === 'image'" type="ios-image-outline" class="itemIcon" size="25"/>
                      <Icon v-else-if="file.type === 'paper'" type="ios-paper-outline" class="itemIcon" size="25"/>
                      <Icon v-else-if="file.type === 'document'" type="ios-document-outline" class="itemIcon"
                            size="25"/>
                      <Icon v-else-if="file.type === 'model'" type="ios-construct-outline" class="itemIcon" size="25"/>
                      <Icon v-else-if="file.type === 'video'" type="ios-videocam-outline" class="itemIcon" size="25"/>
                      <Icon v-else type="ios-create-outline" class="itemIcon" size="25"/>
                      <span
                        @click="getFileInfo(file)"
                        class="fileItemName"
                        :title="file.name"
                      >{{ file.name }}</span
                      >
                      <span v-if="useFileItemSize" class="fileItemSize">{{file.fileSize | filterSizeType}}</span>
                      <span v-if="useFileItemSize" style="width: 10%; margin-left: 10%">{{
                             file.uploadTime | filterTimeStyle
                          }}</span>

                      <!--                使用资源-->
                      <div style="float: right">
                        <!--                  <Button-->
                        <!--                    @click="filePreview(file)"-->
                        <!--                    shape="circle"-->
                        <!--                    icon="md-eye"-->
                        <!--                    title="Preview"-->
                        <!--                    size="small"-->
                        <!--                    class="fileBtnHoverGreen"-->
                        <!--                    type="text"-->
                        <!--                  ></Button>-->
                        <a
                          :href="resProxy + '/data/' + downResId">
                          <Button
                            @click="fileDownload(file)"
                            shape="circle"
                            icon="ios-cloud-download"
                            title="Download"
                            size="small"
                            class="fileBtnHoverGray"
                            type="text"
                          ></Button>
                        </a>
                        <!--                  <Button-->
                        <!--                    @click="showCopyFileModel(file)"-->
                        <!--                    shape="circle"-->
                        <!--                    icon="ios-share-alt"-->
                        <!--                    title="Copy to personal center"-->
                        <!--                    size="small"-->
                        <!--                    class="fileBtnHoverOrange"-->
                        <!--                    type="text"-->
                        <!--                  ></Button>-->

                        <!--                  管理资源-->
                        <template>
                          <Button
                            @click="fileMoveModalShow(file)"
                            shape="circle"
                            icon="md-locate"
                            title="File move"
                            size="small"
                            class="fileBtnHoverBlue"
                            type="text"
                          ></Button>
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
                            @click="resDelete(file)"
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

        </div>
      </Col>
    </Row>

    <!--    点击文件显示内容-->
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
    <!--    文件夹修改-->
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
        </Button
        >
      </div>
    </Modal>
    <!--    新建文件夹 -->
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
        <Button type="success" @click="createFolder('newValidate')">New</Button>
      </div>
    </Modal>
    <!--    文件上传 modal -->
    <Modal v-model="uploadModal" title="Upload file" width="600">
      <Form
        ref="uploadValidate"
        :model="uploadValidate"
        :rules="uploadRuleValidate"
        :label-width="100"
        label-position="left"
      >
        <FormItem label="Privacy" prop="privacy">
          <RadioGroup v-model="uploadValidate.privacy" style="width: 80%">
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
        <FormItem label="Description" prop="description">
          <Input
            type="textarea"
            :rows="4"
            v-model="uploadValidate.description"
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
        <Button type="success" @click="fileUpload('uploadValidate')"
        >Upload
        </Button
        >
      </div>
    </Modal>
    <!--    文件上传进度条-->
    <Modal
      v-model="progressModalShow"
      title="Upload Progress"
      :mask-closable="false"
      :closable="false"
    >
      <Progress :percent="uploadProgress"></Progress>
      <div slot="footer"></div>
    </Modal>
    <!--    文件修改-->
    <Modal v-model="editFileModel" title="Edit file info" width="600">
      <Form
        ref="editFileValidate"
        :model="editFileValidate"
        :rules="editFileRuleValidate"
        :label-width="80"
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
        <FormItem label="Name" prop="name">
          <Input
            v-model="editFileValidate.name"
            :rows="4"
            placeholder="Enter the name for file..."
          />
        </FormItem>
        <FormItem label="Description" prop="description">
          <Input
            type="textarea"
            :rows="4"
            v-model="editFileValidate.description"
          />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="editFileModel = false">Cancel</Button>
        <Button type="success" @click="editFileInfo('editFileValidate')"
        >Submit
        </Button
        >
      </div>
    </Modal>
    <!--    点击文件移动-->
    <Modal v-model="fileMoveModal" title="File Move">
      <Card style="height:300px;" dis-hover>
        <vue-scroll :ops="scrollOps" style=" height: 270px;">
          <Tree :data="folderTree" :render="renderStyle" style="width:400px"></Tree>
        </vue-scroll>
      </Card>

      <div slot="footer">
        <Button type="warning" icon="md-add" style="float:left;" @click="addNewFolderModalShow()">Add new folder
        </Button>
        <Button @click="fileMoveModalClose()">Cancel</Button>
        <Button type="primary" @click="fileMove()">Move</Button>
      </div>
    </Modal>
    <Modal v-model="addNewFolderModal" title="New folder">
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
        <Button type="success" @click="createNewFolder('newValidate')">New</Button>
      </div>
    </Modal>
    <!--    <Modal v-model="shareModal" width="800" title="Share Project">-->
    <!--      <Form>-->
    <!--        <FormItem>-->
    <!--          <div>Share "{{selectedResName}}" to Project</div>-->
    <!--          &lt;!&ndash;          <Select v-model="shareResFormItems.select">&ndash;&gt;-->
    <!--          &lt;!&ndash;            <Option value="selectProject">Share "{{selectedResName}}" to Project</Option>&ndash;&gt;-->
    <!--          &lt;!&ndash;            <Option value="selectUser">Share "{{selectedResName}}" to User</Option>&ndash;&gt;-->
    <!--          &lt;!&ndash;          </Select>&ndash;&gt;-->
    <!--        </FormItem>-->
    <!--        <FormItem v-if="shareResFormItems.select == 'selectProject'">-->
    <!--          <Select v-model="shareResFormItems.sharedProjectId" placeholder="Select Project">-->
    <!--            <Option v-for="item in userProject" :value="item.aid" :key="item.aid">{{ item.name }}</Option>-->
    <!--          </Select>-->
    <!--        </FormItem>-->
    <!--        <FormItem v-if="shareResFormItems.select == 'selectUser'">-->
    <!--          <Input v-model="shareResFormItems.sharedUserEmail" placeholder="Enter email address"></Input>-->
    <!--        </FormItem>-->
    <!--      </Form>-->
    <!--      <div slot="footer">-->
    <!--        <Button type="warning" @click="shareModal = false">Cancel</Button>-->
    <!--        <Button type="success" @click="shareResources">Share</Button>-->
    <!--      </div>-->
    <!--    </Modal>-->
    <!-- </Card> -->
  </div>
</template>

<script>

  export default {
    name: "resourceList",
    data() {
      return {
        scrollOps: {
          bar: {
            background: "lightgrey",
          },
        },
        userInfo: {
          userState: false,
          name: 'visitor',
          userId: '',
          avatar: '',
        },
        useFileItemSize: true,
        contentHeight: '',
        //用于面包屑显示
        folderStack: [
          //默认路径
          {uid: 0, name: "Home"}
        ],
        /*
        用于转换路径，与folderStack 是相反的
        使用的时候，若路径为空，则填充 ”0“
        */
        folderIdStack: [],
        listContentCSS: "",
        userEmail: "",
        resList: [],
        ops: {
          bar: {
            background: "#808695"
          }
        },
        resourceModel: "data",
        currentFolder: {
          folders: [],
          files: []
        },
        chooseFilesArray: [],
        checkAll: false,
        indeterminate: false,
        selectedFileData: [],
        //文件夹path,存储
        folderPath: [{uid: "0"}],
        pathStr: "",
        //文件移动
        folderTree: [],
        slctFolder: {},
        slctFile: {},
        folderInfo: {},
        //新建文件夹Model
        newFolderModal: false,
        addNewFolderModal: false,
        newValidate: {
          setName: ""
        },
        newRuleValidate: {
          setName: [
            {
              required: true,
              message: "The name can't be null.",
              trigger: "blur"
            }
          ]
        },
        // 重命名文件夹Modal
        renameForeInfo: {},
        renameFolderModal: false,
        renameValidate: {
          newName: ""
        },
        renameRuleValidate: {
          newName: [
            {
              required: true,
              message: "The name can't be null.",
              trigger: "blur"
            }
          ]
        },
        //  上传文件Modal
        uploadModal: false,
        progressModalShow: false,
        uploadProgress: 0,
        uploadValidate: {
          privacy: "private",
          type: "data",
          description: ""
        },
        uploadRuleValidate: {
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
          description: [
            {
              required: false,
              message: "file description cannot be empty",
              trigger: "blur"
            }
          ]
        },
        //编辑文件信息modal
        putFileInfo: {},
        editFileModel: false,
        editFileValidate: {
          name: "",
          type: "",
          privacy: "private",
          description: "",
        },
        editFileRuleValidate: {
          privacy: [
            {
              required: true,
              message: "privacy cannot be empty",
              trigger: "blur"
            }
          ],
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
          // description: [
          //   {
          //     required: false,
          //     message: "file description cannot be empty",
          //     trigger: "blur",
          //   },
          // ],
        },
        toUploadFiles: [],
        //选择的fileInfo内容
        fileInfoModal: false,
        fileMoveModal: false,
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
        switchIndex: 0,
        //shareRes相关
        shareModal: false,
        resProxy: this.$store.getters.resProxy,
        downResId: "",
        downResIds: ""
      }
    },
    methods: {
      /*
      具体实现想法
      显示，根据路径查询这一层内容，然后根据 folder 显示即可
      将文件夹与资源统一起来
       */
      switchFolder(folder, index) {
        this.switchIndex = index;
        this.changeFolder(folder.uid, "switch");
      },
      reSize() {
        // if (window.innerHeight > 675) {
        //   this.contentHeight = window.innerHeight - 120 + "px";
        // } else {
        //   this.contentHeight = 675 - 120 + "px";
        // }
        if (window.innerWidth < 1050) {
          this.useFileItemSize = false;
        } else {
          this.useFileItemSize = true;
        }
      },
      getResList: function () {
        this.axios
          .get("/GeoProblemSolving/res")
          .then(res => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({name: "Login"});
            } else if (res.data.code == 0) {
              let rootRes = res.data.data;
              this.resToCurrentFolder(rootRes);
              this.updataFolderTree(rootRes, "init");
            }
          })
          .catch()
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
      },
      updataFolderTree(rootRes, options) {
        if (options == "init") {

          let allFolder = {
            name: "All Folder",
            folder: true,
            uid: '0',
            children: [],
            path: ['0'],
            level: 0,
            expand: true,
          };
          if (this.folderTree.length == 0) {
            this.folderTree.push(allFolder);
          }
          let folderTreeTemp = rootRes;
          //递归清理其中不是文件夹的child
          this.recursiveTree(folderTreeTemp);
          this.folderTree[0].children = folderTreeTemp;


        } else if (options == "add") {
          //待补充
        }
      },
      recursiveTree(tree) {
        for (let i = 0; i < tree.length; i++) {
          if (tree[i].folder == false) {
            tree.splice(i, 1);
            i--;
          } else {
            if (tree[i].children.length > 0) {
              this.recursiveTree(tree[i].children);
            }
          }
        }
      },
      renderStyle(h, {root, node, data}) {
        let props = {};
        let style = {};
        let on = {};
        let name = data.name;
        if (this.slctFolder.uid !== data.uid && data.folder == true) {
          style = {
            border: "0px",
          };
          on = {
            click: () => {
              this.slctFolder = data;
            },
          };
        } else if (data.folder == true) {
          style = {
            backgroundColor: "lightblue",
            cursor: "default",
          };
        }
        return h('span', {
          style: {
            display: 'inline-block',
            width: '100%'
          },

        }, [
          h('span', [
            h(
              "Button",
              {
                props: props,
                style: style,
                on: on,
                attrs: {title: name},
              }, [
                h('Icon', {
                  props: {
                    type: 'ios-folder-open'
                  },
                  style: {
                    marginRight: '8px',
                  }
                }),
                h("span",
                  {
                    style: {
                      overflow: "hidden",
                      maxWidth: "120px",
                      textOverflow: "ellipsis",
                      fontSize: "15px",
                    },
                  },
                  name
                )
              ])
          ]),
        ]);
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
        //data 为选择的文件的地址
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
      //点选文件信息显示
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
          // {
          //   key: "Uploader",
          //   value: file.uploaderName,
          // },
          {
            key: "Upload Time",
            value: this.$options.filters['filterTimeStyle'](file.uploadTime),
          },
          // {
          //   key: "Path",
          //   value: file.path,
          // }
        ];
        this.fileInfoModal = true;
      },
      resizeContent: function () {
        // if (window.innerHeight > 675) {
        //   this.contentHeight = window.innerHeight - 120 + "px";
        // } else {
        //   this.contentHeight = 555px;
        // }
        if (window.innerHeight > 675) {
          this.contentHeight = window.innerHeight - 120 + "px";
        } else {
          this.contentHeight = 675 - 120 + "px";
        }
      },
      //工具方法，将 pathUid 转换为字符串
      reversePathToStr: function (pathIdArray) {
        let pathStrTemp = "";
        for (let i = 0; i < pathIdArray.length; i++) {
          if (i != pathIdArray.length - 1) {
            pathStrTemp += pathIdArray[i] + ",";
          } else {
            pathStrTemp += pathIdArray[i];
          }
        }
        this.pathStr = pathStrTemp;
      },
      changeFolder: function (folder, operationType) {
        let delCount = this.folderStack.length - this.switchIndex - 1;
        if (operationType == "enter") {
          //folderStack 用于显示
          //folderIdStack 用于计算
          this.folderIdStack.unshift(folder.uid);
        } else if (operationType == "back") {
          this.folderIdStack.splice(0, 1);
        } else if (operationType == "switch") {
          this.folderIdStack.splice(0, delCount);
        }
        this.reversePathToStr(this.folderIdStack);
        this.axios.get("/GeoProblemSolving/res/" + this.pathStr)
          .then(res => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({name: "Login"});
            } else if (res.data.code == 0) {
              let folderInfo = res.data.data;
              this.resToCurrentFolder(folderInfo);
              if (operationType == "enter") {
                this.folderStack.push({uid: folder.uid, name: folder.name});
              } else if (operationType == "back") {
                this.folderStack.pop();
              } else if (operationType == "switch") {
                this.chooseFilesArray = [];
                this.checkAll = false;
                this.indeterminate = false;
                for (let i = 0; i < delCount; i++) {
                  this.folderStack.pop();
                }
              }
            } else {
              this.$Message.warning("Get folder info fail.")
            }
          })
          .catch(err => {
            this.$Message.warning("Get folder info fail.");
          })
      },
      enterFolder: function (currentFolder) {
        this.chooseFilesArray = [];
        this.checkAll = false;
        this.indeterminate = false;
        this.changeFolder(currentFolder, "enter");
      },
      backforeFolder: function () {
        this.chooseFilesArray = [];
        this.checkAll = false;
        this.indeterminate = false;
        if (this.folderStack.length != 1) {
          //folderStack 倒数第二个就是其的父文件夹
          this.changeFolder(this.folderStack[this.folderStack.length - 2], "back");
        } else {
          this.$Message.warning("This is the root folder.");
        }
      },
      //用于显示资源基本信息，先不做处理
      getResInfo: function (file) {
        this.selecterFileData = [];
      },
      addFolderModalShow: function () {
        this.newValidate.setName = "";
        this.newFolderModal = true;
      },
      createFolder: function (name) {
        this.$refs[name].validate(valid => {
          if (valid) {
            let folderObj = {
              name: this.newValidate.setName
            };
            let paths = "";
            let foldersId = this.folderIdStack;
            if (foldersId.length == 0) {
              foldersId = ["0"];
            }
            this.reversePathToStr(foldersId);
            this.axios
              .post("/GeoProblemSolving/res/folder/" + this.pathStr, folderObj)
              .then(res => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.$router.push({name: "Login"});
                } else if (res.data.code == 0) {
                  this.currentFolder.folders.push(res.data.data);
                  // this.updataFolderTree(res.data.data,this.pathStr,"add");
                  this.newFolderModal = false;
                } else {
                  this.$Message.warning("New folder fail.");
                }
              })
              .catch(err => {
                this.$Message.warning("New folder fail.");
              })
          }
        });
      },
      deleteFolder: function (folder) {
        if (confirm("Are you sure to delete this folder?")) {
          let folderId = folder.uid;
          let temp = this.folderIdStack;
          if (temp.length == 0) {
            //重新开辟内存空间的temp,如果直接使用push的话，地址还是指向原数据的地址
            temp = ["0"];
          }
          this.axios.delete("/GeoProblemSolving/res/folder/" + folderId + "/" + temp.toString())
            .then(res => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.$router.push({name: "Login"});
              } else if (res.data.code == 0) {
                //删除用于显示的数据中对应的内容
                for (let i = 0; i < this.currentFolder.folders.length; i++) {
                  if (this.currentFolder.folders[i].uid == folderId) {
                    this.currentFolder.folders.splice(i, 1);
                    break;
                  }
                }
                // this.updataFolderTree(folder,temp.toString(),"del");
              } else {
                this.$Message.warning("Delete folder fail.");
              }
            })
            .catch(err => {
              this.$Message.warning("Delete folder fail.");
            })
        }
      },
      renameFolderModalShow: function (folder) {
        this.renameForeInfo = folder;
        this.renameValidate.newName = "";
        this.renameFolderModal = true;
      },
      renameFolder: function (name) {
        this.$refs[name].validate(valid => {
          if (valid) {
            let foldersPath = this.folderIdStack;
            if (foldersPath.length == 0) {
              foldersPath = ["0"];
            }
            this.reversePathToStr(foldersPath);
            let folderId = this.renameForeInfo.uid;
            let newName = this.renameValidate.newName;
            let folderJson = {
              uid: folderId,
              name: newName
            }
            this.axios
              .put("/GeoProblemSolving/res/folder", folderJson)
              .then(res => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.$router.push({name: "Login"});
                } else if (res.data.code == "0") {
                  //更新成功，替换用于显示的数据 item
                  let newFolder = {
                    uid: folderId,
                    name: newName
                  }
                  for (let i = 0; i < this.currentFolder.folders.length; i++) {
                    if (this.currentFolder.folders[i].uid == folderId) {
                      this.currentFolder.folders.splice(i, 1, newFolder);
                      break;
                    }
                  }
                  // this.updataFolderTree(newFolder,this.pathStr,"rename");
                } else {
                  this.$Message.warning("Rename fail.");
                }
              })
              .catch(err => {
                this.$Message.warning("Rename fail.");
              })
            this.renameFolderModal = false;
          }
        });
      },
      uploadModalShow: function () {
        this.uploadValidate = {
          privacy: "private",
          type: "data",
          description: ""
        };
        this.toUploadFiles = [];
        this.uploadModal = true;
      },
      //资源上传相关内容可能需要更改，以便后台能使用 multipartFile 接收
      gatherFile: function (file) {
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
      delFileList: function (index) {
        this.toUploadFiles.splice(index, 1);
      },
      fileUpload: function (nameValidation) {
        this.$refs[nameValidation].validate((valid) => {
          if (valid) {
            let uploadFiles = this.toUploadFiles;
            if (uploadFiles.length > 0) {
              this.uploadModal = false;
              //上传数据需要用formData
              let formData = new FormData();
              for (let i = 0; i < uploadFiles.length; i++) {
                formData.append("file", uploadFiles[i]);
              }
              formData.append("description", this.uploadValidate.description);
              formData.append("type", this.uploadValidate.type);
              formData.append("privacy", this.uploadValidate.privacy);

              let foldersPath = this.folderIdStack;
              if (foldersPath.length == 0) {
                foldersPath = ["0"];
              }
              this.reversePathToStr(foldersPath)
              formData.append("paths", this.pathStr);
              console.log(formData.get("file"))
              this.progressModalShow = true;
              this.axios({
                url: "/GeoProblemSolving/res/upload",
                method: "post",
                onUploadProgress: (progressEvent) => {
                  this.uploadProgress = ((progressEvent.loaded / progressEvent.total) * 100) | 0;
                },
                data: formData
              }).then(res => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.$router.push({name: "Login"});
                } else if (res.data != "Fail" && res.data != "Offline") {
                  let uploadedList = res.data.uploaded;
                  let failedList = res.data.failed;
                  let sizeOverList = res.data.sizeOver;
                  for (let i = 0; i < uploadedList.length; i++) {
                    this.currentFolder.files.push(uploadedList[i]);
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
                  this.progressModalShow = false;
                  this.uploadProgress = 0;
                } else {
                  this.$Message.warning("Upload fail.");
                  this.progressModalShow = false;
                }
              }).catch(err => {
                this.$Message.warning("Upload fail.");
                this.progressModalShow = false;
              })
            }
          }
        })
      },
      resDelete: function (fileInfo) {
        if (confirm("Are you sure to delete ?")) {
          this.reversePathToStr(this.folderIdStack);
          let fileId = fileInfo.uid;
          let address = fileInfo.address;
          let reqParam = new URLSearchParams();
          reqParam.append("uid", fileId);
          reqParam.append("address", address);
          //path 为 ""则表示不用path进行删除
          reqParam.append("path", "");
          reqParam.append("folder", false);

          let delFileInfo = {
            uid: fileInfo.uid,
            address: fileInfo.address,
            paths: this.pathStr,
            folder: false
          };
          this.axios
            .post("/GeoProblemSolving/res/del", delFileInfo)
            .then(res => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.$router.push({name: "Login"});
              } else if (res.data.code == "0") {
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
            .catch(err => {
              this.$Message.warning("Delete file fail.");
            })
        }
      },
      fileDownload: function (fileInfo) {
        let address = fileInfo.address;
        if (address != "" && typeof (address) == "string") {
          if (address.length == 34) {
            this.downResId = address;
          } else {
            let uidArr = address.split("data/");
            if (uidArr instanceof Array && uidArr.length > 1) {
              this.downResId = uidArr[1];
            }
          }
        }
      },
      download: function (blobUrl) {
        const a = document.createElement("a");
        a.style.display = "none";
        a.download = "package.zip";
        a.href = this.resProxy + "/batchData?oids=" + blobUrl;
        a.click();
        a.remove();
      },
      downloadSelectFile: function () {
        let chooseFileUrls = this.chooseFilesArray;
        let temp = [];
        if (chooseFileUrls.length != 0) {
          for (let i = 0; i < chooseFileUrls.length; i++) {
            for (let i = 0; i < chooseFileUrls.length; i++) {
              if (chooseFileUrls[i].length == 34) {
                temp.push(chooseFileUrls[i]);
              } else {
                temp.push(chooseFileUrls[i].split("/data/")[1]);
              }
            }
            this.download(temp.toString());
          }
        }
        // if (chooseFileUrls != "") {
        //   this.$Spin.show();
        // this.axios({
        //   method: "post",
        //   url:
        //     "/GeoProblemSolving/resource/packageZIP?fileURLs=" + chooseFileUrls,
        //   responseType: "blob"
        // })
        //   .then(res => {
        //     if (res.status == 200) {
        //       this.$Spin.hide();
        //       const blobUrl = window.URL.createObjectURL(res.data);
        //       if (blobUrl != "") {
        //         this.download(blobUrl);
        //       }
        //     }
        //   })
        //   .catch(err => {
        //   });
        // }
      },
      fileMoveModalShow(fileInfo) {
        this.slctFolder = {};
        this.slctFile = {};
        this.fileMoveModal = true;
        this.slctFile = fileInfo;
      },
      fileMoveModalClose() {
        this.fileMoveModal = false;
        this.slctFolder = {};
        this.slctFile = {};
      },
      fileEditModelShow: function (fileInfo) {
        this.putFileInfo = fileInfo;
        this.editFileValidate.name = fileInfo.name;
        this.editFileValidate.type = fileInfo.type;
        this.editFileValidate.description = fileInfo.description;
        this.editFileValidate.privacy = fileInfo.privacy;
        this.editFileModel = true;
      },
      editFileInfo: function (fileInfo) {
        this.$refs[fileInfo].validate(valid => {
          if (valid) {
            let putResInfo = {
              uid: this.putFileInfo.uid,
              name: this.editFileValidate.name,
              privacy: this.editFileValidate.privacy,
              type: this.editFileValidate.type,
              description: this.editFileValidate.description,
              fileSize: this.putFileInfo.fileSize,
            }
            this.reversePathToStr(this.folderIdStack)
            this.axios
              .put("/GeoProblemSolving/res/" + this.pathStr, putResInfo)
              .then(res => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.$router.push({name: "Login"});
                } else if (res.data.code == 0) {
                  this.putFileInfo.name = this.editFileValidate.name;
                  this.putFileInfo.privacy = this.editFileValidate.privacy;
                  this.putFileInfo.type = this.editFileValidate.type;
                  this.putFileInfo.description = this.editFileValidate.description;
                  for (let i = 0; i < this.currentFolder.files.length; i++) {
                    if (this.currentFolder.files[i].uid == this.putFileInfo.uid) {
                      this.currentFolder.files.splice(i, 1, this.putFileInfo);
                      break;
                    }
                  }
                  this.$Message.success("Update success")
                  this.editFileModel = false;
                } else {
                  this.$Message.warning("Update fail.");
                }
              })
              .catch(err => {
                this.$Message.warning("Update fail.");
              })
          }
        })
      },
      fileMove() {
        //fileInfo
        let moveFile = this.slctFile;
        //oldPath
        let oldPath = "";
        let foldersPath = this.folderIdStack;
        if (foldersPath.length == 0) {
          foldersPath = ["0"];
        }
        this.reversePathToStr(foldersPath);
        oldPath = this.pathStr;
        //newPath
        let newPath = [];
        if (this.slctFolder.folder != true) {
          this.$Message.warning("Please select a folder directory.");
        } else {
          this.findFolderPath(this.slctFolder, newPath);
          this.reversePathToStr(newPath);
        }
        this.axios
          .put("/GeoProblemSolving/res/file/" + this.pathStr + '/' + oldPath, moveFile)
          .then(res => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({name: "Login"});
            } else if (res.data.code == 0) {
              this.$Notice.info({title: "File move", desc: "Success!"});
              this.folderStack = [{uid: 0, name: "Home"}];
              this.folderIdStack = [];
              this.resToCurrentFolder(res.data.data);
              this.fileMoveModalClose();
            } else {
              this.$Message.warning("Move fail.");
            }
          })
          .catch(err => {
            this.$Message.warning("Move fail.");
          })
      },
      findFolderPath(slctFolder, path) {
        let tree = this.folderTree[0].children;
        let folder = slctFolder;
        if (slctFolder.uid != "0") {
          path.push(slctFolder.uid);
          while (folder.parent != "0") {
            path.push(folder.parent);
            this.getFolderByUid(tree, folder.parent);
            folder = this.folderInfo;
          }
        } else {
          path.push(slctFolder.uid);
        }

      },
      getFolderByUid(tree, uid) {
        for (let i = 0; i < tree.length; i++) {
          if (tree[i].uid == uid) {
            this.folderInfo = tree[i];
          }
          if (tree[i].children.length > 0) {
            this.getFolderByUid(tree[i].children, uid);
          }
        }
      },
      addNewFolderModalShow: function () {
        this.newValidate.setName = "";
        if (this.slctFolder.folder != true) {
          this.$Message.warning("Please select a folder directory.");
        } else {
          this.addNewFolderModal = true;
        }

      },
      createNewFolder: function (name) {
        this.$refs[name].validate(valid => {
          if (valid) {
            let folderObj = {
              name: this.newValidate.setName
            };
            let newPath = [];
            this.findFolderPath(this.slctFolder, newPath);
            this.reversePathToStr(newPath);
            this.axios
              .post("/GeoProblemSolving/res/folder/" + this.pathStr, folderObj)
              .then(res => {
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.$router.push({name: "Login"});
                } else if (res.data.code == 0) {
                  this.getResList();
                  // this.updataFolderTree(res.data.data,"add");
                  this.addNewFolderModal = false;
                } else {
                  this.$Message.warning("New folder fail.");
                }
              })
              .catch(err => {
                this.$Message.warning("New folder fail.");
              })
          }
        });
      },
    },
    mounted() {
      this.resizeContent();
      this.userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
      this.getResList();
      window.addEventListener("resize", this.resizeContent);
      this.reSize();
      window.addEventListener("resize", this.reSize);
    },
    beforeDestroy: function () {
      window.removeEventListener("resize", this.reSize);
    },
    filters: {
      filterSizeType(value) {
        if (value === 0) return "0 B";
        let k = 1024;
        let sizes = ["B", "KB", "MB", "GB"];
        let i = Math.floor(Math.log(value) / Math.log(k));
        return (value / Math.pow(k, i)).toPrecision(3) + " " + sizes[i];
      },
      filterTimeStyle(str) {
        let result = str.split('.')[0];
        return result.replace('T', " ");
      }
    },
  }
</script>

<style scoped>
  .customCard {
    opacity: 0.95;
  }

  .res-content {
    width: 95px;
    height: 90px;
    float: left;
    margin: 5px;
    cursor: pointer;
  }

  .res-content-image {
    margin-left: 10px;
  }

  .res-content-text {
    width: 60px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    text-align: center;

  }

  .fileSpace {
    padding: 0 20px;
  }

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

  .personalFileLabel {
    width: 250px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

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

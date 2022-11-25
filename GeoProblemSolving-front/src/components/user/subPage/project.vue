<style scoped>
  .projectCard {
    background-color: white;
    border-radius: 3px;
    border: 1px solid #dadce0;
    box-shadow: 0 3.2px 7.2px 0 rgb(0 0 0 / 13%), 0 0.6px 1.8px 0 rgb(0 0 0 / 11%);
    position: relative;
  }

  .customCard {
    background-color: white;
    opacity: 0.95;
  }

  .historyLine >>> .ivu-card-head {
    border-bottom-color: white;
    padding: 4px 6px;
  }

  .historyLine >>> .ivu-card-body {
    padding: 5px;
    height: 100%;
  }

  .timeLineStyle {
    height: 735px;
    margin-left: 5%;
  }

  .authorBtn:hover {
    background-color: #57a3f3;
    color: white;
  }

  .badge {
    display: inline-block;
    min-width: 10px;
    padding: 3px 7px;
    font-size: 12px;
    font-weight: bold;
    line-height: 1;
    color: #fff;
    text-align: center;
    white-space: nowrap;
    vertical-align: baseline;
    background-color: #999;
    border-radius: 10px;
  }
</style>
<template>
  <div>
    <div v-if="useProjectCSS">
      <div id="title">
        <h1 style="text-align: center;margin-top: 10px;">Project</h1>
        <h3 style="text-align: center;margin-bottom: 10px;">You can manage projects that you created and joined
          here</h3>
      </div>
      <Row>
        <Col span="22" offset="1">
          <!--    Filter and Storage  -->
          <Card dis-hover class="customCard">
            <CheckboxGroup v-model="selectedProjectType" style="margin-left: 3.5%">
              <Checkbox label="joinedProject" checked="checked">
                <span>Joined by me</span>
                <span class="badge">{{joinedProjectsList.length}}</span>
              </Checkbox>
              <Checkbox label="createdProject">
                <span>Created by me</span>
                <span class="badge">{{createdProjectList.length}}</span>
              </Checkbox>
            </CheckboxGroup>
            <Button slot="extra" type="primary" to="/newProject">Establish a project</Button>
          </Card>

          <!--    project content     -->
          <div
            :style="{height: contentHeight-140+'px'}"
            style=" padding: 5px; border: #dcdee2 solid 1px;"
            class="customCard"
          >
            <vue-scroll :ops="ops">
              <Card :bordered="false"
                    v-if="createdProjectList.length == 0 && selectedProjectType.includes('createdProject') && joinedProjectsList.length == 0">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, you didn't create any projects.</h3>
                </div>
              </Card>

              <Card :bordered="false"
                    v-if="joinedProjectsList.length == 0 && selectedProjectType.includes('joinedProject') && selectedProjectType.length == 1">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, you didn't participate in any projects.</h3>
                </div>
              </Card>

              <Card :bordered="false" v-if="selectedProjectType.length == 0">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, There are not any projects.</h3>
                </div>
              </Card>


              <!--               项目内容显示        -->
              <div v-show="userProjectCount.length != 0">
                <div v-if="selectedProjectType.indexOf('createdProject') != -1">
                  <div
                    v-for="(mProject,index) in createdProjectList"
                    v-show="createdProjectList!='None'"
                    :key="'create'+index"
                  >
                    <Col span="7" style="margin-left: 3.5%">
                      <div class="projectItem" @click="goSingleProject(mProject)">
                        <Card style="height:320px;margin-top:20px; z-index: 0;" class="projectCard">
                          <p slot="title" class="projectsTitle" :title="mProject.name" style="width: 70%;">{{mProject.name}}</p>
                          <Button
                            class="authorBtn"
                            type="default"
                            slot="extra"
                            title="Privilege change"
                            @click.stop="authorizeModalShow(mProject.aid)"
                            icon="md-happy"
                          ></Button>
                          <Button
                            class="deleteBtn"
                            type="default"
                            slot="extra"
                            style="margin:0 0 0 5px"
                            @click.stop="deleteProjectModalShow(mProject.aid, mProject.name, mProject.members)"
                            icon="md-close"
                            title="remove"
                          ></Button>
                          <!--  @click.stop="deleteProjectModalShow(mProject.projectId)" -->
                          <!-- 表头结束 -->
                          <!--              200px  -->
                          <Row>
                            <Col span="7" >
                              <div style="margin-top: 5px; margin-left:5px;">
                                  <img
                                    :src="mProject.picture"
                                    v-if="mProject.picture != '' && mProject.picture != null"
                                    style="
                                      height: 70px;
                                      width: 70px;
                                      border-radius: 50%;
                                    "
                                  />
                                  <avatar
                                    :username="mProject.name"
                                    :size="70"
                                    style="margin-bottom: 6px"
                                    v-else
                                  ></avatar>
                              </div>
                            </Col>
                            <Col span="17">
                              <div style="height:200px; z-index: 2; position:relative;">
                                <span style="font-weight: bold; position:absolute;">Description：</span>
                                <vue-scroll :ops="ops" style="text-indent:2em;word-break:break-word;white-space: pre-line;">
                                  {{mProject.description}}
                                </vue-scroll>
                              </div>
                            </Col>
                          </Row>

                          <br/>
                          <div>
                            <span style="float:left">Created time:</span>
                            <span style="float:right">{{mProject.createdTime}}</span>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </div>
                </div>


                <div v-if="selectedProjectType.indexOf('joinedProject') != -1">
                  <div
                    v-for="(item,Index) in joinedProjectsList"
                    :key="'join'+ Index"
                    v-show="joinedProjectsList!=[]"
                  >
                    <Col span="7" style="margin-left: 3.5%">
                      <div @click="goSingleProject(item)" class="projectItem">
                        <Card style="height:320px;margin-top:20px;  z-index: 0;" class="projectCard">
                          <p
                            slot="title"
                            class="projectsTitle"
                            :title="item.name"
                            style="width: 70%;"
                          >{{item.name}}</p>
                          <!--                        @click.stop="quitModalShow(item)"-->
                          <Button
                            class="fileBtnHoverRed"
                            slot="extra"
                            @click.stop="quitModalShow(item)"
                          >Quit
                          </Button>
                          <!-- <img :src="item.picture" style="position:absolute; width:98%; height:230px; top:50px; left:1%; opacity: 0.5;z-index: -1;"> -->
                          <Row>
                            <Col span="7">
                              <div style="margin-top: 5px">
                                  <img
                                    :src="item.picture"
                                    v-if="item.picture != '' && item.picture != null"
                                    style="
                                      height: 70px;
                                      width: 70px;
                                      border-radius: 50%;
                                    "
                                  />
                                  <avatar
                                    :username="item.name"
                                    :size="70"
                                    style="margin-bottom: 6px"
                                    v-else
                                  ></avatar>
                              </div>
                            </Col>
                            <Col span="17">
                              <div style="height:200px; z-index: 2; position:relative;">
                                <span style="font-weight: bold; position:absolute;">Description：</span>
                                <vue-scroll :ops="ops" style="text-indent:2em;word-break:break-word;white-space: pre-line;">
                                  {{item.description}}
                                </vue-scroll>
                              </div>
                            </Col>
                          </Row>
                          <br/>
                          <div style="height:40px">
                            <span style="float:left">CreateTime:</span>
                            <span style="float:right">{{item.createdTime}}</span>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </div>
                </div>
              </div>
            </vue-scroll>
          </div>
        </Col>

        <!--    Event History-->
        <!--      <Col span="4">-->
        <!--        <Card dis-hover class="historyLine">-->
        <!--          <p slot="title">Event line</p>-->
        <!--          <div class="timeLineStyle">-->
        <!--            <vue-scroll :ops="ops">-->
        <!--              <Timeline>-->
        <!--                <div v-if="userEventList.length==0">-->
        <!--                  <div style="display:flex;justify-content:center">-->
        <!--                    <Icon type="md-alert" size="40" color="gray"/>-->
        <!--                  </div>-->
        <!--                  <br/>-->
        <!--                  <div style="display:flex;justify-content:center">-->
        <!--                    <h3-->
        <!--                      style="text-align:center;width:80%"-->
        <!--                    >Sorry, there are no events now.</h3>-->
        <!--                  </div>-->
        <!--                </div>-->
        <!--                <TimelineItem-->
        <!--                  v-for="(item,index) in userEventList"-->
        <!--                  :key="index"-->
        <!--                  v-show="userEventList.length>0"-->
        <!--                >-->
        <!--                  <strong>-->
        <!--                    <p class="time">{{item.createTime}}</p>-->
        <!--                  </strong>-->
        <!--                  <p class="content">{{item.description}}</p>-->
        <!--                </TimelineItem>-->
        <!--              </Timeline>-->
        <!--            </vue-scroll>-->
        <!--          </div>-->
        <!--        </Card>-->
        <!--      </Col>-->

        <!--      模态框     -->
        <Modal
          v-model="deleteProjectModal"
          @on-ok="deleteProject"

          ok-text="Assure"
          cancel-text="Cancel"
        >
          <p style="font-family: 'Roboto Light';">Do you really want to delete: <strong>{{delProjectName}}</strong>?</p>
        </Modal>

        <Modal
          title="Quit Project"
          v-model="quitModal"
          @on-ok="quitProject()"

          ok-text="Ok"
          cancel-text="Cancel"
        >
          <p>Once you exit the project: <strong>{{currentProject.name}}</strong> ,you will not be able to participate in
            the collaborative process, confirm the exit?</p>
        </Modal>

        <!--      <Modal-->
        <!--        v-model="authorizeProjectModal"-->
        <!--        @on-ok="authorize()"-->
        <!--        ok-text="Assure"-->
        <!--        cancel-text="Cancel"-->
        <!--      >-->
        <!--        <p style="slot">Hand the project to others</p>-->
        <!--        <div>-->
        <!--          <RadioGroup v-model="selectManagerId">-->
        <!--            <Radio v-for="member in projectMemberList" :key="member.index" :label="member.userId">-->
        <!--              <span>{{member.userName}}</span>-->
        <!--            </Radio>-->
        <!--          </RadioGroup>-->
        <!--          &lt;!&ndash; 用radio将用户表示出来 &ndash;&gt;-->
        <!--          &lt;!&ndash; <tag v-for="(member,index) in projectMemberList" @click="choose(index)" :key="member.index">{{member.userName}}</tag> &ndash;&gt;-->
        <!--        </div>-->
        <!--      </Modal>-->
      </Row>
    </div>

    <!-- v-else -->
    <div v-else>
      <div id="title">
        <h1 style="text-align: center;margin-top: 10px;">Project</h1>
        <h3 style="text-align: center;margin-bottom: 10px;">You can manage projects that you created or joined here</h3>
      </div>
      <Row>
        <Col span="22" offset="1">
          <!--    Filter and Storage  -->
          <Card dis-hover class="customCard">
            <CheckboxGroup v-model="selectedProjectType" style="margin-left: 3.5%">
              <Checkbox label="joinedProject" checked="checked" title="Joined by me">
                <!-- <span>Joined by me</span> -->
                <span class="badge">{{joinedProjectsList.length}}</span>
              </Checkbox>
              <Checkbox label="createdProject" title="Created by me">
                <!-- <span>Created by me</span> -->
                <span class="badge">{{createdProjectList.length}}</span>
              </Checkbox>
            </CheckboxGroup>

            <Button slot="extra" type="primary" to="/newProject">Establish a project</Button>
          </Card>

          <!--    project content     -->
          <div
            :style="{height: contentHeight-140+'px'}"
            style=" padding: 5px; border: #dcdee2 solid 1px;"
            class="customCard"
          >
            <vue-scroll :ops="ops">
              <Card :bordered="false" dis-hover
                    v-if="createdProjectList.length == 0 && selectedProjectType.includes('createdProject') && joinedProjectsList.length == 0">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, you didn't create any projects.</h3>
                </div>
              </Card>

              <Card :bordered="false" dis-hover
                    v-if="joinedProjectsList.length == 0 && selectedProjectType.includes('joinedProject') && selectedProjectType.length == 1">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, you didn't participate in any projects.</h3>
                </div>
              </Card>

              <Card :bordered="false" dis-hover v-if="selectedProjectType.length == 0">
                <div style="display:flex;justify-content:center">
                  <Icon type="md-alert" size="40" color="gray"/>
                </div>
                <br/>
                <div style="display:flex;justify-content:center">
                  <h3
                    style="text-align:center;width:80%"
                  >Sorry, There are not any projects.</h3>
                </div>
              </Card>


              <!--               项目内容显示        -->
              <div v-show="userProjectCount.length != 0">
                <div v-if="selectedProjectType.indexOf('createdProject') != -1">
                  <div
                    v-for="(mProject,index) in createdProjectList"
                    v-show="createdProjectList!='None'"
                    :key="'create'+index"
                  >
                    <Col span="10" style="margin-left: 3.5%">
                      <div class="projectItem" @click="goSingleProject(mProject)">
                        <Card style="height:320px;margin-top:20px;" class="projectCard">
                          <p slot="title" class="projectsTitle" :title="mProject.name" style="width: 70%;">{{mProject.name}}</p>
                          <Button
                            class="authorBtn"
                            type="default"
                            slot="extra"
                            title="Privilege change"
                            @click.stop="authorizeModalShow(mProject.aid)"
                            icon="md-happy"
                          ></Button>
                          <Button
                            class="deleteBtn"
                            type="default"
                            slot="extra"
                            style="margin:0 0 0 5px"
                            @click.stop="deleteProjectModalShow(mProject.aid, mProject.name, mProject.members)"
                            icon="md-close"
                            title="remove"
                          ></Button>

                          <!--  @click.stop="deleteProjectModalShow(mProject.projectId)" -->
                          <!-- 表头结束 -->
                          <!--              200px  -->
                         <Row>
                            <Col span="7">
                              <div style="margin-top: 5px">
                                  <img
                                    :src="mProject.picture"
                                    v-if="mProject.picture != '' && mProject.picture != null"
                                    style="
                                      height: 70px;
                                      width: 70px;
                                      border-radius: 50%;
                                    "
                                  />
                                  <avatar
                                    :username="mProject.name"
                                    :size="70"
                                    style="margin-bottom: 6px"
                                    v-else
                                  ></avatar>
                              </div>
                            </Col>
                            <Col span="17">
                              <div style="height:200px; z-index: 2; position:relative;">
                                <span style="font-weight: bold; position:absolute;">Description：</span>
                                <vue-scroll :ops="ops" style="text-indent:2em;word-break:break-word;white-space: pre-line;">
                                  {{mProject.description}}
                                </vue-scroll>
                              </div>
                            </Col>
                          </Row>
                          <br/>
                          <div>
                            <span style="float:left">CreateTime:</span>
                            <span style="float:right">{{mProject.createdTime}}</span>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </div>

                </div>

                <div v-if="selectedProjectType.indexOf('joinedProject') != -1">
                  <div

                    v-for="(item,Index) in joinedProjectsList"
                    :key="'join'+ Index"
                    v-show="joinedProjectsList!=[]"
                  >
                    <Col span="10" style="margin-left: 3.5%">
                      <div @click="goSingleProject(item)" class="projectItem">
                        <Card style="height:320px;margin-top:20px;" class="projectCard">
                          <p
                            slot="title"
                            class="projectsTitle"
                            :title="item.name"
                            style="width: 70%;"
                          >{{item.name}}</p>
                          <!--                        @click.stop="quitModalShow(item)"-->
                          <Button
                            class="fileBtnHoverRed"
                            slot="extra"
                            @click.stop="quitModalShow(item)"
                          >Quit
                          </Button>
                          <Row>
                            <Col span="7">
                              <div style="margin-top: 5px">
                                  <img
                                    :src="item.picture"
                                    v-if="item.picture != '' && item.picture != null"
                                    style="
                                      height: 70px;
                                      width: 70px;
                                      border-radius: 50%;
                                    "
                                  />
                                  <avatar
                                    :username="item.name"
                                    :size="70"
                                    style="margin-bottom: 6px"
                                    v-else
                                  ></avatar>
                              </div>
                            </Col>
                            <Col span="17">
                              <div style="height:200px; z-index: 2; position:relative;">
                                <span style="font-weight: bold; position:absolute;">Description：</span>
                                <vue-scroll :ops="ops" style="text-indent:2em;word-break:break-word;white-space: pre-line;">
                                  {{item.description}}
                                </vue-scroll>
                              </div>
                            </Col>
                          </Row>
                          <br/>
                          <div style="height:40px">
                            <span style="float:left">CreateTime:</span>
                            <span style="float:right">{{item.createdTime}}</span>
                          </div>
                        </Card>
                      </div>
                    </Col>
                  </div>
                </div>

              </div>
            </vue-scroll>
          </div>
        </Col>

        <!--    Event History-->
        <!--      <Col span="4">-->
        <!--        <Card dis-hover class="historyLine">-->
        <!--          <p slot="title">Event line</p>-->
        <!--          <div class="timeLineStyle">-->
        <!--            <vue-scroll :ops="ops">-->
        <!--              <Timeline>-->
        <!--                <div v-if="userEventList.length==0">-->
        <!--                  <div style="display:flex;justify-content:center">-->
        <!--                    <Icon type="md-alert" size="40" color="gray"/>-->
        <!--                  </div>-->
        <!--                  <br/>-->
        <!--                  <div style="display:flex;justify-content:center">-->
        <!--                    <h3-->
        <!--                      style="text-align:center;width:80%"-->
        <!--                    >Sorry, there are no events now.</h3>-->
        <!--                  </div>-->
        <!--                </div>-->
        <!--                <TimelineItem-->
        <!--                  v-for="(item,index) in userEventList"-->
        <!--                  :key="index"-->
        <!--                  v-show="userEventList.length>0"-->
        <!--                >-->
        <!--                  <strong>-->
        <!--                    <p class="time">{{item.createTime}}</p>-->
        <!--                  </strong>-->
        <!--                  <p class="content">{{item.description}}</p>-->
        <!--                </TimelineItem>-->
        <!--              </Timeline>-->
        <!--            </vue-scroll>-->
        <!--          </div>-->
        <!--        </Card>-->
        <!--      </Col>-->

        <!--      模态框     -->
        <Modal
          v-model="deleteProjectModal"
          @on-ok="deleteProject"

          ok-text="Assure"
          cancel-text="Cancel"
        >
          <p style="font-family: 'Roboto Light';">Do you really want to delete: <strong>{{delProjectName}}</strong>?</p>
        </Modal>

        <Modal
          title="Quit Project"
          v-model="quitModal"
          @on-ok="quitProject()"

          ok-text="Ok"
          cancel-text="Cancel"
        >
          <p>Once you exit the project: <strong>{{currentProject.name}}</strong> ,you will not be able to participate in
            the collaborative process, confirm the exit?</p>
        </Modal>

        <!--      <Modal-->
        <!--        v-model="authorizeProjectModal"-->
        <!--        @on-ok="authorize()"-->
        <!--        ok-text="Assure"-->
        <!--        cancel-text="Cancel"-->
        <!--      >-->
        <!--        <p style="slot">Hand the project to others</p>-->
        <!--        <div>-->
        <!--          <RadioGroup v-model="selectManagerId">-->
        <!--            <Radio v-for="member in projectMemberList" :key="member.index" :label="member.userId">-->
        <!--              <span>{{member.userName}}</span>-->
        <!--            </Radio>-->
        <!--          </RadioGroup>-->
        <!--          &lt;!&ndash; 用radio将用户表示出来 &ndash;&gt;-->
        <!--          &lt;!&ndash; <tag v-for="(member,index) in projectMemberList" @click="choose(index)" :key="member.index">{{member.userName}}</tag> &ndash;&gt;-->
        <!--        </div>-->
        <!--      </Modal>-->
      </Row>

    </div>
  </div>
</template>

<script>
import Avatar from "vue-avatar";

  export default {
    name: "project",
    components: {Avatar,},
    data() {
      return {
        joinedProjectsList: [],
        createdProjectList: [],
        // 用于显示的project
        userProjectList: [],
        deleteProjectModal: false,
        quitModal: false,
        useProjectCSS: true,
        projectMemberList: [],
        deleteProjectId: "",
        delProjectName: "",
        delProjectMembers: [],
        currentProject: {},
        userEventList: [],
        selectedProjectType: ['joinedProject', 'createdProject'],
        deleteProjectId: "",
        member: {},
        contentHeight: "",
        ops: {
          bar: {
            background: "#808695"
          }
        },
      }
    },
    mounted() {
      this.getUserProject();
      // this.readPersonalEvent();
      this.resizeContent();
      this.reSize();
      window.addEventListener("resize", this.reSize);
    },
    beforeDestroy: function () {
      window.removeEventListener("resize", this.reSize);
    },
    computed: {
      userProjectCount: function () {
        if (this.selectedProjectType.length == 0) {
          this.userProjectList = [];
          return this.userProjectList;
        } else if (this.selectedProjectType.length == 2) {
          this.userProjectList = [];
          this.userProjectList = this.joinedProjectsList.concat(this.createdProjectList);
          return this.userProjectList;
        } else if (this.selectedProjectType.length == 1 && this.selectedProjectType[0] == "joinedProject") {
          this.userProjectList = [];
          this.userProjectList = this.joinedProjectsList;
          return this.userProjectList;
        } else if (this.selectedProjectType.length == 1 && this.selectedProjectType[0] == "createdProject") {
          this.userProjectList = [];
          this.userProjectList = this.createdProjectList;
          return this.userProjectList;
        }
      },
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
        if (window.innerWidth < 1050) {
          this.useProjectCSS = false;
        } else {
          this.useProjectCSS = true;
        }
      },
      getUserProject: function () {
        let userInfo = this.$store.getters.userInfo;
        if (userInfo.createdProjects != null) {
          let createdProjectIds = userInfo.createdProjects.toString();
          // project/getProjects?aids=  用户获取项目
          this.$axios.get("/GeoProblemSolving/project/getProjects?aids=" + createdProjectIds)
            .then(res => {
              if(res.data.code == 0) {
                this.$set(this, "createdProjectList", res.data.data)
              }else {
                this.$Message.error("Failed to load the created project.");
              }
            })
            .catch(err => {
              this.$Message.error("Failed to load the created project.");
            })
        }

        if (userInfo.joinedProjects != null) {
          let joinedProjectIds = userInfo.joinedProjects.toString();
          //projectIds 获取所有项目详情
          this.$axios.get("/GeoProblemSolving/project/getProjects?aids=" + joinedProjectIds)
            .then(res => {
              if (res.data.code == 0) {
                this.$set(this, "joinedProjectsList", res.data.data);
              }else {
                this.$Message.error("Failed to load the joined project.");
              }
            })
            .catch(err => {
              this.$Message.error("Loading project failed.")
            })
        }
      },
      deleteProjectModalShow(pid, pName, pMembers) {
        this.deleteProjectId = pid;
        this.delProjectName = pName;
        this.delProjectMembers = pMembers;
        this.deleteProjectModal = true;
      },
      deleteProject() {
        if (this.deleteProjectId != "") {
          this.axios
            .delete(
              "/GeoProblemSolving/project?" +
              "aid=" + this.deleteProjectId
            )
            .then(res => {
              if (res.data.data == "Offline") {
                this.$store.commit("userLogout");
                this.$router.push({name: "Login"});
              } else if (res.data.data == "Success") {
                if (this.createdProjectList != null) {
                  for (let i = 0; i < this.createdProjectList.length; i++) {
                    if (this.deleteProjectId == this.createdProjectList[i].aid) {
                      this.createdProjectList.splice(i, 1);
                      break;
                    }
                  }
                  //  项目删除成功，需要给每一个成员都发送通知
                  let notice = {};
                  notice["type"] = "notice";
                  notice["content"] = {
                    title: "Project Deleted",
                    description:
                      this.delProjectName + " has been deleted by manager " + this.$store.getters.userName + " !",
                    projectId: this.deleteProjectId,
                  };


                  this.delProjectMembers.forEach((item) => {
                    notice["recipientId"] = item.userId;
                    this.axios
                      .post("/GeoProblemSolving/notice/save", notice)
                      .then(res => {
                        if (res.data == "Success") {
                          this.$emit("sendNotice", {"type": "Notice", "recipientId": item.userId});
                        }
                      })
                      .catch(err => {
                        console.log("失败原因：" + err.data);
                      });
                  })
                }
                //更改store中的信息
                this.$store.commit("updateProject", this.deleteProjectId);
                this.$Notice.success({title: "Delete Success."})
              } else {
                this.$Notice.error({
                  title: "Error",
                  desc: "Delete project fail."
                });
              }
            })
            .catch(err => {
              console.log(err.data);
            });
        }
      },
      //点击跳转到指定项目的函数
      goSingleProject(projectInfo) {
        // sessionStorage.setItem("projectInfo", JSON.stringify(projectInfo));
        // sessionStorage.setItem("projectInfo", this.encrypto(projectInfo));
        // console.log(JSON.parse(projectInfo.permission).auto_join.visitor);
        window.location.href =
          "/GeoProblemSolving/projectInfo/" + projectInfo.aid;
      },
      authorizeModalShow: function (aid) {
        let url = window.location.href;
        let permissionUrl = url.split("newPersonalPage")[0] + "permission/0/" + aid
        window.open(permissionUrl, "_self")
      },


      //读取用户对项目的操作
      // readPersonalEvent() {
      //   this.axios.get(
      //     "/GeoProblemSolving/history/inquiry?" +
      //     "eventType=project" +
      //     "&key=userId" +
      //     "&value=" +
      //     this.$store.getters.userId
      //   )
      //     .then(res => {
      //       if (res.data == "offline") {
      //         this.$store.commit("userLogout");
      //       } else if (res.data != "None" && res.data != "Fail") {
      //         this.userEventList = res.data.reverse();
      //       }
      //     })
      // },
      authorize() {
        this.axios
          .get(
            "/GeoProblemSolving/project/" + this.currentProject.projectId + "/user" +
            "?userId=" + this.selectManagerId +
            "&role=manager"
          )
          .then(res => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({name: "Login"});
            } else if (res.data != "Fail") {
              let projectInfo = res.data;
              projectInfo.projectId = this.currentProject.projectId;
              this.projectManageToJoin(projectInfo);
              let notice = {};
              let recipientId = this.selectManagerId;
              notice["recipientId"] = recipientId;
              notice["type"] = "notice";
              notice["content"] = {
                title: "Manager change",
                description:
                  "You have been the manager of project " +
                  this.currentProject.title +
                  " !",
                projectId: this.currentProject.projectId,
              };
              this.axios
                .post("/GeoProblemSolving/notice/save", notice)
                .then(res => {
                  if (res.data == "Success") {
                    this.$emit("sendNotice", {"type": "Notice", "recipientId": recipientId});
                  }
                })
                .catch(err => {
                  console.log("申请失败的原因是：" + err.data);
                });
            }
          })
          .catch(err => {
          });
      },
      quitModalShow(project) {
        this.quitModal = true;
        this.currentProject = project;
        console.log(project)
        this.delProjectName = project.name;
      },
      quitProject: function () {
        let quitProjectId = this.currentProject.aid;
        let quitUserId = this.$store.getters.userId;
        let members = this.currentProject.members;
        let creatorId = "";
        for (let i = 0; i < members.length; i++) {
          if (members[i].role == "manager") {
            creatorId = members[i].userId;
          }
        }
        let formData = new URLSearchParams();
        formData.append("userId", quitUserId);
        formData.append("projectId", quitProjectId);

        this.axios
          .put("/GeoProblemSolving/user/quitProject", formData)
          .then(res => {
            if (res.data.code == 0) {
              this.$Notice.success({title: "Quit Success"})
              this.removeQuitProject(quitProjectId);
              let notice = {};
              // let recipientId = this.currentProject.members[0].userId;
              notice["recipientId"] = creatorId;
              notice["type"] = "notice";
              notice["content"] = {
                title: "Project member change",
                description:
                  "The member called " +
                  this.$store.getters.userInfo.name +
                  " had quited from your project " +
                  this.currentProject.name +
                  "!",
                projectId: quitProjectId,
                projectName: this.currentProject.name,

              };
              this.axios
                .post("/GeoProblemSolving/notice/save", notice)
                .then(res => {
                  if (res.data == "Success") {
                    //调用父组件（Navigation.vue）websocket推送信息给对象
                    this.$emit("sendNotice", {"type": "Notice", "recipientId": creatorId});
                  }
                })
                .catch(err => {
                  console.log("申请失败的原因是：" + err.data);
                });
            } else if (res.data.code == -2) {
              this.$Message.error("Quit Failed.")
            }
          })
          .catch(err => {
            this.$Message.error("Quit Fail.");
          })
        // this.axios
        //   .get(
        //     "/GeoProblemSolving/project/" + quitProjectId + "/user" +
        //     "?userId=" +
        //     this.$store.getters.userId
        //   )
        //   .then(res => {
        //     if (res.data == "Offline") {
        //       this.$store.commit("userLogout");
        //       this.$router.push({ name: "Login" });
        //     } else if (res.data != "Fail" && res.data != "None") {
        //
        //       this.$Message.success("Quit Success");
        //       this.removeQuitProject(quitProjectId);
        //       let notice = {};
        //       let recipientId = this.currentProject.members[0].userId;
        //       notice["recipientId"] = recipientId;
        //       notice["type"] = "notice";
        //       notice["content"] = {
        //         title: "Manager change",
        //         description:
        //           "The member called " +
        //           this.$store.getters.userInfo.name +
        //           " had quited from your project " +
        //           this.currentProject.name +
        //           "!"
        //       };
        //       this.axios
        //         .post("/GeoProblemSolving/notice/save", notice)
        //         .then(res => {
        //           if (res.data == "Success") {
        //             this.$emit("sendNotice", recipientId);
        //           }
        //         })
        //         .catch(err => {
        //           console.log("申请失败的原因是：" + err.data);
        //         });
        //     } else {
        //       this.$Message.error("Quit Fail.");
        //       console.log("Quit fail: " + res.data);
        //     }
        //   })
        //   .catch(err => {});
      },
      removeQuitProject(projectId) {
        for (var i = 0; i < this.joinedProjectsList.length; i++) {
          if (this.joinedProjectsList[i].aid == projectId) {
            this.joinedProjectsList.splice(i, 1);
          }
        }
      },
    }
  }
</script>

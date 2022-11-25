<template>
  <div>
    <div id="title">
      <h1 style="text-align: center;margin-top: 10px;">Todo List</h1>
      <h3 style="text-align: center;margin-bottom: 10px;">You can manage your tasks here</h3>
    </div>
    <Row>
      <Col span="22" offset="1">
        <Card dis-hover style="position: relative; z-index: 3;" class="customCard">
          <CheckboxGroup v-model="selectedTaskType" style="margin-left: 1%">
            <Checkbox label="Doing" title="Doing">
              <span >Doing</span>
              <span class="badge">{{doingList.length}}</span>
            </Checkbox>
            <Checkbox label="Done" title="Done">
              <span >Done</span>
              <span class="badge">{{doneList.length}}</span>
            </Checkbox>
<!--            <Checkbox label="Assigned">-->
<!--              <span>Assigned to you</span>-->
<!--              <span class="badge">10</span>-->
<!--            </Checkbox>-->
            <Checkbox label="Importance" :disabled="true" title="Importance">
              <span >Importance</span>
              <span class="badge">{{importanceNum}}</span>
            </Checkbox>
          </CheckboxGroup>
          <!--         新建任务   -->
          <div v-if="useTodoListCSS">
            <DatePicker
              type="date"
              style="position: absolute;top: 20%; right: 4%; z-index: 3"
              class="customDatePicker custom"
              title="Select Endtime"
              @on-change="handleChange"
            >
            </DatePicker>
            <Input
              v-model="newTask.content"
              prefix="ios-radio-button-off"
              placeholder="Add Todo"
              size="large"
              @on-enter="addTask()"
              style="display: inline-block;position: absolute;width: 35%;margin-left: 60%;top: 10px;"
              class="customIcon"
            />
          </div>
        </Card>
        <!-- v-else -->
        <Card dis-hover v-if="!useTodoListCSS" style="position: relative;z-index: 3;" class="customCard">
          <DatePicker
            type="date"
            style="position: absolute;right: 5%;top: 25%; z-index: 3;"
            class="customDatePicker custom"
            title="select endTime"
            @on-change="handleChange"
          >
          </DatePicker>
          <Input
            v-model="newTask.content"
            prefix="ios-radio-button-off"
            placeholder="Add Todo"
            size="large"
            @on-enter="addTask()"
            style="display: inline-block;"
            class="customIcon"
          />
        </Card>

        <Card dis-hover style="margin-bottom: 10px; z-index: 1;" class="customCard">
          <!--      未完成            -->
          <div >
            <Card
              dis-hover
              v-if="selectedTaskType.includes('Doing')"
            >
              <p slot="title"><strong>Doing</strong></p>
              <div :style="{height: contentHeight-contentHeightCompute-60+'px'}">
                <vue-scroll :ops="ops">
                  <div
                    v-for="(item, doingIndex) in doingList"
                    :key="doingIndex"
                  >
                    <Card
                      class="customIcon"
                      dis-hover
                    >
                      <Icon type="ios-radio-button-off"
                            size="20"
                            style="cursor:pointer;"
                            title="Change State"
                            @click="changeState(item, doingIndex)"
                      />
                      <Icon type="ios-close" size="31"
                            style="float: right;color: #ff7800;cursor: pointer; line-height: .8"
                            title="Delete"
                            @click="delTask(item.ptId, doingIndex, 'doing')"
                      />
                      <Icon v-if="item.importance == 0"
                            size="20" type="ios-star-outline"
                            style="float: right;cursor: pointer;margin-right: 20px"
                            title="Mark Important"
                            @click="changeImportance(item, doingIndex, 'doing')"
                      />
                      <Icon v-else size="20"
                            type="ios-star"
                            style="float: right;cursor:pointer;margin-right: 20px"
                            title="Unmark"
                            @click="changeImportance(item, doingIndex, 'doing')"
                      />
                      <!-- 考虑增加详情页 类似resource的详情页 -->
                      <label
                        style="margin-left: 10px;"
                        @click="getTodoInfo(item)"
                        class="todoItemInfo"
                        :title="item.content"
                      >
                        {{item.content}}
                      </label>
                      <label
                        style="float: right;" title="End Time" v-if="useTodoListCSS && item.endTime != ''"
                      >Deadline：{{item.endTime}}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
                    </Card>
                  </div>
                </vue-scroll>
              </div>

            </Card>
          </div>
          <!--        已完成      -->
          <div>
            <Card
              dis-hover
              v-if="selectedTaskType.includes('Done')"
            >
              <p slot="title"><strong>Done</strong></p>
              <!--            显示内容                -->
              <div :style="{height: contentHeight-contentHeightCompute-60+'px'}">
                <vue-scroll :ops="ops">
                  <div
                    v-for="(item, doneIndex) in doneList"
                    :key="doneIndex"
                  >
                    <Card class="customIcon" dis-hover>
                      <Icon type="ios-checkmark-circle-outline" size="20" style="line-height: .8; cursor: pointer"
                            @click="changeState(item, doneIndex)"/>
                      <Icon type="ios-close" size="31"
                            style="float: right;color: #ff7800;cursor: pointer; line-height: .8"
                            @click="delTask(item.ptId, doneIndex, 'done')"
                      />
                      <Icon v-if="item.importance == 1"
                            size="20"
                            type="ios-star"
                            style="float: right;cursor:pointer;margin-right: 20px"
                            @click="changeImportance(item, doneIndex, 'done')"
                      />
                      <Icon v-else
                            size="20"
                            type="ios-star-outline"
                            style="float: right;cursor: pointer;margin-right: 20px"
                            @click="changeImportance(item, doneIndex, 'done')"
                      />
                      <label
                        style="margin-left: 10px;"
                        @click="getTodoInfo(item)"
                        class="todoItemInfo"
                        :title="item.content"
                      >
                        {{item.content}}
                      </label>
                    </Card>

                  </div>
                </vue-scroll>
              </div>

            </Card>
          </div>

        </Card>
      </Col>
    </Row>
    <!--    点击文件显示内容-->
    <Modal v-model="todoInfoModal" title="Todo Info">
      <Table
        :columns="selectedTodoColumns"
        :data="selectedTodoData"
        stripe
        border
        :show-header="false"
      ></Table>
      <div slot="footer">
        <Button type="primary" @click="todoInfoModal = false">OK</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
  export default {
    name: "todoList",
    data() {
      return {
        selectedTaskType: ["Doing",  "Importance"],
        doneList: [],
        doingList: [],
        selectedTodoData: [],
        importanceNum: 0,
        useTodoListCSS: true,
        newTask: {
          userId: "",
          content: "",
          endTime: "",
          importance: "0",
          state: "doing"
        },
        contentHeight: "",
        ops: {
          bar: {
            background: "#808695"
          }
        },
        //选择的todoInfo内容
        todoInfoModal: false,
        selectedTodoColumns: [
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
      }
    },
    mounted() {
      this.initTodoList();
      this.resizeContent();
      this.reSize();
      window.addEventListener("resize", this.reSize);
    },
    methods: {
      resizeContent() {
        if (window.innerHeight > 800) {
          this.contentHeight = window.innerHeight - 120;
        } else {
          this.contentHeight = 800;
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
        if (window.innerWidth < 1000) {
          this.useTodoListCSS = false;
        } else {
          this.useTodoListCSS = true;
        }
      },
      //点选文件信息显示
      getTodoInfo(file) {
        this.selectedTodoData = [
          {
            key: "Todo Content",
            value: file.content,
          },
          {
            key: "End Time",
            value: file.endTime,
          },
          {
            key: "Important",
            value: (file.importance==1?"yes":"no"),
          },
          {
            key: "State",
            value: file.state,
          },
          // {
          //   key: "Uploader",
          //   value: file.uploaderName,
          // },
          // {
          //   key: "Upload Time",
          //   value: file.uploadTime,
          // },
        ];
        this.todoInfoModal = true;
      },
      initTodoList: function () {
        this.axios
          .get("/GeoProblemSolving/user/todo/" + this.$store.getters.userId)
          .then(res => {
            let doingListTemp = [];
            let doneListTemp = [];
            if (res.data.code == 0) {
              let todoList = res.data.data;
              for (let index = 0; index < todoList.length; index++) {
                if (todoList[index].state == "doing" && todoList[index].importance == 1) {
                  doingListTemp.unshift(todoList[index]);
                  this.importanceNum++;
                } else if (todoList[index].state == "doing" && todoList[index].importance == 0) {
                  doingListTemp.push(todoList[index]);
                } else if (todoList[index].state == "done" && todoList[index].importance == 1) {
                  doneListTemp.unshift(todoList[index]);
                  this.importanceNum++;
                } else if (todoList[index].state == "done" && todoList[index].importance == 0) {
                  doneListTemp.push(todoList[index]);
                }
              }
              this.$set(this, "doneList", doneListTemp);
              this.$set(this, "doingList", doingListTemp);
            } else if (res.code == -2) {
              this.$Message.error("Todo Task Loading Fail.")
            }
          })
          .catch(err => {
            this.$Message.error("Todo Task Loading Fail.")
          })
      },
      addTask: function () {
        this.newTask.userId = this.$store.getters.userId;
        if (this.newTask.content !== "") {
          this.axios
            .post("/GeoProblemSolving/user/todo", this.newTask)
            .then(res => {
              console.log(res);
              if (res.data.code == 0) {
                this.doingList.push(res.data.data);
                this.newTask.content = "";
                this.newTask.endTime = "";
              }
            })
            .catch(err => {
              this.$Message.error("Failed to create todo task")
            })
        }
      },
      // 选择日期
      handleChange: function (date) {
        this.newTask.endTime = date;
      },
      changeState: function (item, index) {
        let changeType = 0;
        if (item.state == "doing") {
          item.state = "done";
          changeType = 1;
        } else if (item.state == "done") {
          item.state = "doing";
          changeType = 2;
        }
        this.axios
          .put("/GeoProblemSolving/user/todo", item)
          .then(res => {
            if (res.data.code == 0) {
              //
              if (changeType == 1) {
                this.doneList.push(item);
                this.doingList.splice(index, 1);
              } else if (changeType == 2) {
                this.doingList.push(item);
                this.doneList.splice(index, 1);
              }
            }
          })
          .catch(err => {
            this.$Message.error("Change task state Fail.")
          })
        // this.initTodoList();

      },
      changeImportance: function (item, index, state) {
        if (item.importance == 0) {
          item.importance = 1;
        } else if (item.importance == 1) {
          item.importance = 0;
          this.importanceNum--;
        }
        this.axios
          .put("/GeoProblemSolving/user/todo", item)
          .then(res => {
            if (res.data.code == 0) {
              if (state == "doing" && item.importance == 1) {
                this.doingList.unshift(this.doingList.splice(index, 1)[0]);
                this.importanceNum++;
              } else if (state == "done" && item.importance == 1) {
                this.doneList.unshift(this.doneList.splice(index, 1)[0]);
                this.importanceNum++;
              }
            }
          })
          .catch()
          // this.initTodoList();
      },
      delTask: function (ptId, index, state) {
        this.axios
          .delete("/GeoProblemSolving/user/todo/" + ptId)
          .then(res => {
            if (res.data.code == 0) {
              if (state == "doing") {
                this.doingList.splice(index, 1);
                this.$Notice.success({title: "Delete Success"})
              } else if (state == "done") {
                this.$Notice.success({title: "Delete Success"})
                this.doneList.splice(index, 1);
              }
            } else {
              this.$Message.error("Delete Fail.")
            }
          })
          .catch(err => {
            this.$Message.error("Delete Fail.")
          })
      },
      //使用计算属性还是方法更好？？？
      timeCompare: function (time) {
        let nowTime = new Date();
        let formatTime = Date.parse(time);
        let secondsNum = nowTime - formatTime - 86400000;
        if (secondsNum > 0) {
          return true;
        } else {
          return false;
        }
      },
      test: function () {
        console.log("123131")
      }
    },
    //用一个计算属性动态捕捉doing/done List 的变化
    computed: {
      contentHeightCompute: function () {
        let tempHeight = "";
        if (this.selectedTaskType.length == 2){
          tempHeight = 280;
        }else if (this.selectedTaskType.length == 3){
          tempHeight = 570;
        }
        return tempHeight;
      }
    },
  }
</script>

<style scoped>
  .customCard{
    opacity: 0.95;
  }

  .todoCard{
    background-color: white;
    border-radius: 3px;
    border:1px solid #dadce0;
    box-shadow:  0 3.2px 7.2px 0 rgb(0 0 0 / 13%), 0 0.6px 1.8px 0 rgb(0 0 0 / 11%);
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

 .todoItemInfo {
    width: 35%;
    margin-right: 5%;
    display: inline-block;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: top;
    cursor: pointer;
    overflow: hidden;
  }

  .customCardHead >>> .ivu-card-head {
    border-bottom-color: white;
    padding: 4px 6px;
  }

  .customDatePicker >>> .ivu-input {
    display: inline-block;
    width: 100%;
    height: 32px;
    line-height: 1.5;
    padding: 4px 7px;
    font-size: 12px;
    border: 1px solid white;
    text-align: center;
    border-radius: 0px;
    color: #515a6e;
    background-color: #fff;
    background-image: none;
    position: relative;
    cursor: text;
    transition: border .2s ease-in-out, background .2s ease-in-out, box-shadow .2s ease-in-out;
  }

  .customDatePicker:hover >>> .ivu-input {
    cursor: pointer;
  }

  .customIcon >>> .ivu-icon {
    font-weight: 700;
  }

  .custom >>> .ivu-input-suffix i {
    font-size: 22px;
    line-height: 30px;
    font-weight: 700;
  }
</style>

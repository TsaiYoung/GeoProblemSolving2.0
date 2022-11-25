<template>
  <div>
    <Row>
      <div style="margin-top: 8px; margin-right: 60px; float: right">
        <span
          id="todoPanel"
          style="cursor: pointer; color: #57a3f3"
          @click="switch2Manager"
          >Task manager</span
        >
        <Divider type="vertical" />
        <span id="ganttPanel" style="cursor: pointer" @click="switch2Gantt"
          >Gantt chart</span
        >
      </div>
      <Col id="taskPage" span="24">
        <div id="taskContainer" :style="{ height: contentHeight - 120 + 'px' }">
          <template v-if="!chartSwitch">
            <Row type="flex" justify="space-around">
              <Col span="7">
                <Card :padding="0" :border="false" dis-hover>
                  <h3 slot="title">Todo</h3>
                  <Button
                    slot="extra"
                    type="default"
                    class="createTaskBtn"
                    style="margin-top: -8px"
                    v-if="
                      permissionIdentity(
                        activityInfo.permission,
                        userRole,
                        'create_task'
                      )
                    "
                    @click="createTaskModalShow()"
                    >Add</Button
                  >
                  <vue-scroll
                    :ops="ops"
                    :style="{ height: contentHeight - 180 + 'px' }"
                  >
                    <draggable
                      :disabled="
                        !permissionIdentity(
                          activityInfo.permission,
                          userRole,
                          'manage_task'
                        )
                      "
                      class="taskList"
                      element="ul"
                      :options="{ group: 'task' }"
                      v-model="taskTodo"
                      :style="{ height: contentHeight - 180 + 'px' }"
                      @start="setMoveCount()"
                      @update="updateMoveTask(taskTodo, 'todo')"
                      @add="addMoveTask(taskTodo, 'todo')"
                      @remove="removeMoveTask(taskTodo, 'todo')"
                    >
                      <div
                        v-for="(item, index) in taskTodo"
                        :key="index.taskId"
                        :padding="3"
                      >
                        <Card
                          style="margin: 5px; background-color: lightyellow"
                          v-if="item.type == 'activity'"
                        >
                          <div title="Activity task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon type="ios-list" color="gray" :size="20" />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div style="float: right">
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskTodo)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskTodo)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                            <p
                              style="
                                word-break: break-word;
                                padding: 5px;
                                cursor: pointer;
                              "
                              @click="showTask(index, taskTodo)"
                            >
                              {{ item.description }}
                            </p>
                            <div
                              style="display: flex; justify-content: flex-end"
                            >
                              <Tag
                                color="default"
                                style="cursor: default"
                                title="Creator"
                                >{{ item.creatorName }}</Tag
                              >
                            </div>
                          </div>
                        </Card>
                        <Card style="margin: 5px" v-else>
                          <div title="Simple task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon type="ios-list" color="gray" :size="20" />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div style="float: right">
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskTodo)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskTodo)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                            <p
                              style="
                                word-break: break-word;
                                padding: 5px;
                                cursor: pointer;
                              "
                              @click="showTask(index, taskTodo)"
                            >
                              {{ item.description }}
                            </p>
                            <div
                              style="display: flex; justify-content: flex-end"
                            >
                              <Tag
                                color="default"
                                style="cursor: default"
                                title="Creator"
                                >{{ item.creatorName }}</Tag
                              >
                            </div>
                          </div>
                        </Card>
                      </div>
                      <Spin size="large" fix v-if="todoLoading"></Spin>
                    </draggable>
                  </vue-scroll>
                </Card>
              </Col>
              <Col span="7">
                <Card :padding="0" :border="false" dis-hover>
                  <h3 slot="title">Doing</h3>
                  <vue-scroll
                    :ops="ops"
                    :style="{ height: contentHeight - 180 + 'px' }"
                  >
                    <draggable
                      :disabled="
                        !permissionIdentity(
                          activityInfo.permission,
                          userRole,
                          'manage_task'
                        )
                      "
                      class="taskList"
                      element="ul"
                      :options="{ group: 'task' }"
                      v-model="taskDoing"
                      :style="{ height: contentHeight - 180 + 'px' }"
                      @start="setMoveCount()"
                      @update="updateMoveTask(taskDoing, 'doing')"
                      @add="addMoveTask(taskDoing, 'doing')"
                      @remove="removeMoveTask(taskDoing, 'doing')"
                    >
                      <div
                        v-for="(item, index) in taskDoing"
                        :key="index.taskId"
                        :padding="3"
                      >
                        <Card
                          style="margin: 5px; background-color: lightyellow"
                          v-if="item.type == 'activity'"
                        >
                          <div title="Activity task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon
                                type="ios-information-circle-outline"
                                color="gray"
                                :size="20"
                              />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div
                              style="float: right"
                              v-show="userRole != 'visitor'"
                            >
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskDoing)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskDoing)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                          </div>
                          <p
                            style="
                              word-break: break-word;
                              padding: 5px;
                              cursor: pointer;
                            "
                            @click="showTask(index, taskDoing)"
                          >
                            {{ item.description }}
                          </p>
                          <div style="display: flex; justify-content: flex-end">
                            <Tag
                              color="default"
                              style="cursor: default"
                              title="Executor"
                              >{{ item.managerName }}</Tag
                            >
                          </div>
                        </Card>
                        <Card style="margin: 5px" v-else>
                          <div title="Simple task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon
                                type="ios-information-circle-outline"
                                color="gray"
                                :size="20"
                              />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div
                              style="float: right"
                              v-show="userRole != 'visitor'"
                            >
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskDoing)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskDoing)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                          </div>
                          <p
                            style="
                              word-break: break-word;
                              padding: 5px;
                              cursor: pointer;
                            "
                            @click="showTask(index, taskDoing)"
                          >
                            {{ item.description }}
                          </p>
                          <div style="display: flex; justify-content: flex-end">
                            <Tag
                              color="default"
                              style="cursor: default"
                              title="Executor"
                              >{{ item.managerName }}</Tag
                            >
                          </div>
                        </Card>
                      </div>
                      <Spin size="large" fix v-if="doingLoading"></Spin>
                    </draggable>
                  </vue-scroll>
                </Card>
              </Col>
              <Col span="7">
                <Card :padding="0" :border="false" dis-hover>
                  <h3 slot="title">Done</h3>
                  <vue-scroll
                    :ops="ops"
                    :style="{ height: contentHeight - 180 + 'px' }"
                  >
                    <draggable
                      :disabled="
                        !permissionIdentity(
                          activityInfo.permission,
                          userRole,
                          'manage_task'
                        )
                      "
                      class="taskList"
                      element="ul"
                      :options="{ group: 'task' }"
                      v-model="taskDone"
                      :style="{ height: contentHeight - 180 + 'px' }"
                      @start="setMoveCount()"
                      @update="updateMoveTask(taskDone, 'done')"
                      @add="addMoveTask(taskDone, 'done')"
                      @remove="removeMoveTask(taskDone, 'done')"
                    >
                      <div
                        v-for="(item, index) in taskDone"
                        :key="index.taskId"
                        :padding="3"
                      >
                        <Card
                          style="margin: 5px; background-color: lightyellow"
                          v-if="item.type == 'activity'"
                        >
                          <div title="Activity task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon type="md-checkmark-circle-outline" />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div
                              style="float: right"
                              v-show="userRole != 'visitor'"
                            >
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskDone)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskDone)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                            <p
                              style="
                                word-break: break-word;
                                padding: 5px;
                                cursor: pointer;
                              "
                              @click="showTask(index, taskDone)"
                            >
                              {{ item.description }}
                            </p>
                            <div
                              style="display: flex; justify-content: flex-end"
                            >
                              <Tag
                                color="default"
                                style="cursor: default"
                                title="Executor"
                                >{{ item.managerName }}</Tag
                              >
                            </div>
                          </div>
                        </Card>
                        <Card style="margin: 5px" v-else>
                          <div title="Simple task">
                            <span style="float: left; padding: 0 2.5px">
                              <Icon type="md-checkmark-circle-outline" />
                            </span>
                            <span style="padding: 5px">
                              <strong
                                style="color: #57a3f3"
                                class="name"
                                :title="item.name"
                                >{{ item.name }}</strong
                              >
                            </span>
                            <div
                              style="float: right"
                              v-show="userRole != 'visitor'"
                            >
                              <Rate
                                :disabled="
                                  !(
                                    permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'manage_task'
                                    ) ||
                                    (permissionIdentity(
                                      activityInfo.permission,
                                      userRole,
                                      'create_task'
                                    ) &&
                                      item.creatorId == userInfo.userId)
                                  )
                                "
                                v-model="item.importance"
                                :count="1"
                                clearable
                                title="Importance"
                                @on-change="changeImportance(item)"
                              />
                              <template
                                v-if="
                                  permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'manage_task'
                                  ) ||
                                  (permissionIdentity(
                                    activityInfo.permission,
                                    userRole,
                                    'create_task'
                                  ) &&
                                    item.creatorId == userInfo.userId)
                                "
                              >
                                <span title="Edit">
                                  <Icon
                                    type="ios-create"
                                    color="gray"
                                    :size="20"
                                    style="cursor: pointer"
                                    @click="editOneTask(index, taskDone)"
                                  />
                                </span>
                                <span
                                  style="
                                    margin-left: 5px;
                                    margin-right: 3px;
                                    cursor: pointer;
                                    color: gray;
                                  "
                                  title="Delete"
                                  @click="taskRemoveAssure(index, taskDone)"
                                >
                                  <Icon
                                    type="ios-trash"
                                    :size="20"
                                    color="gray"
                                  />
                                </span>
                              </template>
                            </div>
                            <p
                              style="
                                word-break: break-word;
                                padding: 5px;
                                cursor: pointer;
                              "
                              @click="showTask(index, taskDone)"
                            >
                              {{ item.description }}
                            </p>
                            <div
                              style="display: flex; justify-content: flex-end"
                            >
                              <Tag
                                color="default"
                                style="cursor: default"
                                title="Executor"
                                >{{ item.managerName }}</Tag
                              >
                            </div>
                          </div>
                        </Card>
                      </div>
                      <Spin size="large" fix v-if="doneLoading"></Spin>
                    </draggable>
                  </vue-scroll>
                </Card>
              </Col>
            </Row>
          </template>
          <div v-show="chartSwitch">
            <vue-scroll
              :ops="scrollOps"
              :style="{ height: contentHeight - 15 + 'px' }"
            >
              <gantt-elastic
                :tasks="ganttTasks"
                :options="ganttOptions"
              ></gantt-elastic>
            </vue-scroll>
          </div>
        </div>
      </Col>
    </Row>
    <Modal
      v-model="taskDeleteModal"
      title="Delete task"
      @on-ok="taskRemove()"
      ok-text="OK"
      cancel-text="Cancel"
    >
      <p>Do yout want to delete this task?</p>
    </Modal>
    <Modal
      v-model="createTaskModal"
      title="Create task"
      width="800px"
      :closable="false"
    >
      <Form
        ref="formValidate"
        :model="formValidate"
        :rules="ruleValidate"
        :label-width="100"
        style="margin-left: 30px"
      >
        <FormItem label="Task type">
          <Row>
            <Col span="8">
              <RadioGroup v-model="formValidate.type">
                <Radio label="simple">Simple task</Radio>
                <!-- <Radio
                  label="activity"
                  style="margin-left: 20px"
                  v-show="
                    childActivities != undefined && childActivities.length > 0
                  "
                  >Activity task</Radio
                > -->
              </RadioGroup></Col
            >
            <Col span="13" v-show="formValidate.type == 'activity'">
              <Select
                v-model="formValidate.activity"
                placeholder="In the activity of ..."
              >
                <Option
                  v-for="activity in childActivities"
                  :key="activity.aid"
                  :value="activity.aid"
                  :title="activity.description"
                  >{{ activity.name }}</Option
                >
              </Select>
            </Col></Row
          >
        </FormItem>
        <FormItem label="Name" prop="name">
          <Input
            v-model="formValidate.name"
            placeholder="Fill in the name of task..."
            style="width: 560px"
          />
        </FormItem>
        <FormItem label="Description" prop="description">
          <Input
            v-model="formValidate.description"
            type="textarea"
            placeholder="Fill in the description of task..."
            style="width: 560px"
            :autosize="{ minRows: 3 }"
          />
        </FormItem>
        <FormItem label="Time range" prop="timeRange">
          <DatePicker
            v-model="formValidate.timeRange"
            type="daterange"
            format="yyyy-MM-dd"
            placeholder="Select start time..."
            style="width: 560px"
          ></DatePicker>
        </FormItem>
        <FormItem label prop="importance">
          <Checkbox v-model="formValidate.importance">Important task</Checkbox>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="createTaskModal = false">Cancel</Button>
        <Button type="primary" @click="createTask('formValidate')"
          >Create</Button
        >
      </div>
    </Modal>
    <Modal
      v-model="editTaskModal"
      title="Edit task"
      @on-ok="updateTask('formValidate')"
      ok-text="Ok"
      cancel-text="Cancel"
      width="800px"
      :closable="false"
    >
      <Form
        ref="formValidate"
        :model="formValidate"
        :rules="ruleValidate"
        :label-width="100"
        style="margin-left: 30px"
      >
        <FormItem label="Name" prop="name">
          <Input
            v-model="formValidate.name"
            placeholder="Fill in the name of task..."
            style="width: 560px"
          />
        </FormItem>
        <FormItem label="Description" prop="description">
          <Input
            v-model="formValidate.description"
            type="textarea"
            placeholder="Fill in the description of task..."
            style="width: 560px"
            :autosize="{ minRows: 4 }"
          />
        </FormItem>
        <FormItem label="Time range" prop="timeRange">
          <DatePicker
            v-model="formValidate.timeRange"
            type="daterange"
            format="yyyy-MM-dd"
            placeholder="Select start time..."
            style="width: 560px"
          ></DatePicker>
        </FormItem>
        <FormItem label prop="importance">
          <Checkbox v-model="formValidate.importance">Important Task</Checkbox>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="editTaskModal = false">Cancel</Button>
        <Button type="primary" @click="updateTask('formValidate')"
          >Update</Button
        >
      </div>
    </Modal>
    <Modal
      v-model="taskDetailModal"
      :title="taskInfo.type == 'activity' ? 'Activity task' : 'Simple task'"
      width="800px"
    >
      <div class="taskFormItem">
        <span style="width: 15%">Task name</span>
        <Input style="width: 600px" v-model="taskInfo.name" readonly />
      </div>
      <div class="taskFormItem">
        <span style="width: 15%">Description</span>
        <Input
          style="width: 600px"
          type="textarea"
          v-model="taskInfo.description"
          :autosize="{ minRows: 4 }"
          readonly
        />
      </div>
      <div class="taskFormItem" style="margin-bottom: 10px">
        <span style="width: 15%">Time range</span>
        <DatePicker
          v-model="taskInfo.timeRange"
          type="daterange"
          format="yyyy-MM-dd"
          style="width: 600px"
          readonly
        ></DatePicker>
      </div>
      <div slot="footer">
        <Button
          type="info"
          @click="go2Activity(taskInfo.taskActivityId)"
          v-if="taskInfo.type == 'activity'"
          >Performing the task</Button
        >
      </div>
    </Modal>
    <login-modal
      :tempLoginModal="tempLoginModal"
      @changeLoginModal="changeLoginModal"
    ></login-modal>
  </div>
</template>
<script>
import dayjs from "dayjs";
import draggable from "vuedraggable";
import GanttElastic from "gantt-elastic";
import loginModal from "../../../user/userState/loginModal.vue";
import * as socketApi from "../../../../api/socket";
export default {
  components: {
    draggable,
    dayjs,
    ganttElastic: GanttElastic,
    loginModal,
  },
  props: ["activityInfo", "projectInfo", "childActivities", "userInfo"],
  data() {
    return {
      // userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
      userRole: "visitor",
      todoLoading: true,
      doingLoading: true,
      editTaskState: "",
      removeTaskState: "",
      socketId: "",
      doneLoading: true,
      ops: {
        bar: {
          background: "#808695",
        },
      },
      scrollOps: {
        bar: {
          background: "#808080",
          keepShow: true,
          size: "8px",
        },
        rail: {
          background: "#d7d7d7",
          opacity: 0.8,
          size: "10px",
        },
      },
      // 后台获取的subproject下的task列表
      taskList: [],
      selectTaskIndex: 0,
      taskDeleteModal: false,
      // 创建任务的模态框
      createTaskModal: false,
      // 编辑任务的模态框
      editTaskModal: false,
      taskDetailModal: false,
      //恢复登录的模态框
      tempLoginModal: false,
      //task相关
      taskInfo: {},
      taskTodo: [],
      taskDoing: [],
      taskDone: [],
      MoveCount: 0,
      // 动态记录相关
      formValidate: {
        name: "",
        description: "",
        type: "simple",
        activity: "",
        timeRange: "",
        importance: false,
      },
      ruleValidate: {
        name: [
          { required: true, message: "Please enter name...", trigger: "blur" },
        ],
        description: [
          { required: true, message: "Please select type...", trigger: "blur" },
        ],
        timeRange: [
          {
            type: "array",
            required: true,
            trigger: "blur",
            fields: {
              0: { required: true, type: "date" },
              1: { required: true, type: "date" },
            },
          },
        ],
      },
      // style
      contentHeight: "",
      chartSwitch: false, // 切换至甘特图
      // gantt 图
      ganttTasks: [
        {
          id: 1,
          name: "",
          user: "",
          start: new Date(),
          end: new Date(),
          progress: 0,
          type: "task",
        },
      ],
      ganttOptions: {
        maxRows: 100,
        maxHeight: this.contentHeight - 60,
        row: {
          height: 24,
        },
        calendar: {
          hour: {
            display: false,
          },
        },
        chart: {
          progress: {
            bar: false,
          },
          expander: {
            display: true,
          },
        },
        taskList: {
          expander: {
            straight: false,
          },
          columns: [
            {
              id: 1,
              label: "ID",
              value: "id",
              width: 40,
            },
            {
              id: 2,
              label: "Name",
              value: "name",
              width: 200,
              expander: true,
            },
            {
              id: 3,
              label: "Assigned to",
              value: "user",
              width: 100,
            },
            {
              id: 4,
              label: "Start",
              value: (task) => dayjs(task.start).format("YYYY-MM-DD"),
              width: 90,
            },
            {
              id: 5,
              label: "End",
              value: (task) => dayjs(task.end).format("YYYY-MM-DD"),
              width: 90,
            },
          ],
        },
      },
    };
  },
  created() {
    this.initSize();
    this.roleIdentity();
  },
  watch:{
    activityInfo: {
      immediate: true,
      handler(){
        this.socketId = `OperationServer/task${this.projectInfo.aid}/${this.activityInfo.aid}`;
      }
    }
  },
  mounted() {
    this.inquiryTask();
    window.addEventListener("resize", this.initSize);
  },
  beforeRouteLeave(to, from, next) {
    next();
  },
  beforeDestroy: function () {
    window.removeEventListener("resize", this.initSize);
  },
  methods: {
    initSize() {
      this.contentHeight = window.innerHeight - 140;
    },
    socketOnMessage(messageJson){
      let behavior = messageJson.behavior;
      let content = messageJson.content;
      if (messageJson.type == "task") {
        if (behavior == "create") {
          let task = content.newTask;
          this.taskTodo.push(task);
        } else if (behavior == "importance") {
          let taskState = content.state;
          let taskId = content.taskId;
          let importance = content.importance;
          switch (taskState) {
            case "todo":
              this.taskTodo.forEach((item) => {
                if (item.taskId == taskId) {
                  item.importance = importance;
                }
              });
              break;
            case "doing":
              this.taskDoing.forEach((item) => {
                if (item.taskId == taskId) {
                  item.importance = importance;
                }
              });
              break;
            case "done":
              this.taskDone.forEach((item) => {
                if (item.taskId == taskId) {
                  item.importance = importance;
                }
              });
              break;
          }
        } else if (behavior == "updateTask") {
          let taskState = content.state;
          let putTask = content.editedTask;
          let taskId = putTask.taskId;
          switch (taskState) {
            case "todo":
              this.taskTodo.forEach((item) => {
                if (item.taskId == taskId) {
                  Object.assign(item, putTask);
                }
              });
              break;
            case "doing":
              this.taskDoing.forEach((item) => {
                if (item.taskId == taskId) {
                  Object.assign(item, putTask);
                }
              });
              break;
            case "done":
              this.taskDone.forEach((item) => {
                if (item.taskId == taskId) {
                  Object.assign(item, putTask);
                }
              });
              break;
          }
        } else if (behavior == "order") {
          let taskState = content.state;
          let taskList = content.taskList;
          switch (taskState) {
            case "todo":
              this.$set(this, "taskTodo", taskList);
              break;
            case "doing":
              this.$set(this, "taskDoing", taskList);
              break;
            case "done":
              this.$set(this, "taskDone", taskList);
              break;
          }
        } else if (behavior == "remove") {
          let taskState = content.state;
          let index = content.removeIndex;
          switch (taskState) {
            case "todo":
              this.taskTodo.splice(index, 1);
              break;
            case "doing":
              this.taskDoing.splice(index, 1);
              break;
            case "done":
              this.taskDone.splice(index, 1);
              break;
          }
        }
      }
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
    changeLoginModal(status) {
      this.tempLoginModal = status;
    },
    //创建任务
    createTaskModalShow() {
      let taskDefult = {
        name: "",
        description: "",
        type: "simple",
        timeRange: [],
        activity: "",
        state: "todo",
      };
      this.$set(this, "taskInfo", taskDefult);
      this.$set(this, "formValidate", taskDefult);
      this.createTaskModal = true;
    },
    createTask(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let taskForm = {};
          taskForm["name"] = this.formValidate.name;
          taskForm["description"] = this.formValidate.description;
          taskForm["startTime"] = new Date(this.formValidate.timeRange[0]);
          taskForm["endTime"] = new Date(this.formValidate.timeRange[1]);
          taskForm["type"] = this.formValidate.type;
          taskForm["importance"] = this.formValidate.importance ? 1 : 0;
          taskForm["taskActivityId"] = this.formValidate.activity;
          taskForm["creatorId"] = this.$store.getters.userId;
          taskForm["creatorName"] = this.$store.getters.userName;
          taskForm["managerName"] = this.$store.getters.userName;
          taskForm["aid"] = this.activityInfo.aid;
          taskForm["state"] = "todo";
          taskForm["order"] = this.taskTodo.length;
          this.axios
            .post("/GeoProblemSolving/task/save", taskForm)
            .then((res) => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                this.tempLoginModal = true;
              } else if (res.data != "Fail") {
                this.addNewTask(res.data);

                // update activity doc
                this.operationApi.taskUpdate(
                  this.activityInfo.aid,
                  "create",
                  res.data
                );
                this.$store.commit("updateActivityTasks", {
                  behavior: "add",
                  task: res.data,
                });

                this.createTaskModal = false;

                let sockMsg = {};
                sockMsg["type"] = "task";
                sockMsg["behavior"] = "create";
                sockMsg["content"] = {
                  newTask: res.data,
                };
                sockMsg["sender"] = this.userInfo.userId;
                socketApi.sendSock(
                  this.socketId,
                  sockMsg,
                  this.socketOnMessage
                );
              }
            })
            .catch((err) => {});
        } else {
          this.$Message.error("Please enter the necessary information!");
        }
      });
    },
    addNewTask(newTaskObject) {
      this.taskTodo.push(newTaskObject);
    },
    changeImportance(task) {
      let taskForm = new URLSearchParams();
      taskForm.append("taskId", task.taskId);
      taskForm.append("importance", task.importance);
      this.axios
        .post("/GeoProblemSolving/task/update", taskForm)
        .then((res) => {
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data != "None" && res.data != "Fail") {
            this.$Message.info("Changed the importance of one task.");
            let sockMsg = {};
            sockMsg["type"] = "task";
            sockMsg["behavior"] = "importance";
            sockMsg["content"] = {
              taskId: task.taskId,
              state: task.state,
              importance: task.importance,
            };
            sockMsg["sender"] = this.userInfo.userId;
            socketApi.sendSock(this.socketId, sockMsg, this.socketOnMessage);
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    //打开task编辑器
    editOneTask(index, taskList) {
      this.axios
        .get(
          "/GeoProblemSolving/task/inquiry?" +
            "key=taskId" +
            "&value=" +
            taskList[index]["taskId"]
        )
        .then((res) => {
          if (res.data != "Fail") {
            let taskInfoRes = res.data[0];
            taskInfoRes["timeRange"] = [
              new Date(taskInfoRes.startTime),
              new Date(taskInfoRes.endTime),
            ];
            taskInfoRes.importance = taskInfoRes.importance ? true : false;
            this.$set(this, "formValidate", taskInfoRes);
            this.editTaskModal = true;
            this.editTaskState = taskInfoRes.state;
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.$Message.error("Fail!");
        });
    },
    showTask(index, taskList) {
      this.axios
        .get(
          "/GeoProblemSolving/task/inquiry?" +
            "key=taskId" +
            "&value=" +
            taskList[index]["taskId"]
        )
        .then((res) => {
          if (res.data != "Fail") {
            let taskInfoRes = res.data[0];
            taskInfoRes["timeRange"] = [
              new Date(taskInfoRes.startTime),
              new Date(taskInfoRes.endTime),
            ];
            this.$set(this, "taskInfo", taskInfoRes);
            this.taskDetailModal = true;
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.$Message.error("Fail!");
        });
    },
    updateTaskList(taskObject) {
      taskObject.importance = taskObject.importance ? 1 : 0;
      switch (taskObject.state) {
        case "todo": {
          let taskList = this.taskTodo;
          for (var i = 0; i < taskList.length; i++) {
            if (taskList[i].taskId == taskObject.taskId) {
              this.$set(this.taskTodo, i, taskObject);
              break;
            }
          }
          break;
        }
        case "doing": {
          let taskList = this.taskDoing;
          for (var i = 0; i < taskList.length; i++) {
            if (taskList[i].taskId == taskObject.taskId) {
              this.$set(this.taskDoing, i, taskObject);
              break;
            }
          }
          break;
        }
        case "done": {
          let taskList = this.taskDone;
          for (var i = 0; i < taskList.length; i++) {
            if (taskList[i].taskId == taskObject.taskId) {
              this.$set(this.taskDone, i, taskObject);
              break;
            }
          }
          break;
        }
      }
    },
    //更新某个task
    updateTask(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let importance = this.formValidate.importance ? 1 : 0;
          let taskForm = new URLSearchParams();
          taskForm.append("taskId", this.formValidate.taskId);
          taskForm.append("name", this.formValidate.name);
          taskForm.append("description", this.formValidate.description);
          taskForm.append("startTime", new Date(this.formValidate.timeRange[0]));
          taskForm.append("endTime", new Date(this.formValidate.timeRange[1]));
          taskForm.append("importance", importance);

          let tempTaskForm = {
            "taskId": this.formValidate.taskId,
            "name": this.formValidate.name,
            "description": this.formValidate.description,
            "startTime": new Date(this.formValidate.timeRange[0]),
            "endTime": new Date(this.formValidate.timeRange[1]),
            "importance": importance
          };
          this.axios
            .post("/GeoProblemSolving/task/update", taskForm)
            .then((res) => {
              if (res.data == "Offline") {
                this.$store.commit("userLogout");
                // this.$router.push({ name: "Login" });
                this.tempLoginModal = true;
              } else if (res.data != "None" && res.data != "Fail") {
                this.editTaskModal = false;
                // update activity doc
                this.operationApi.taskUpdate(
                  this.activityInfo.aid,
                  "update",
                  taskForm
                );
                this.$store.commit("updateActivityTasks", {
                  behavior: "update",
                  task: taskForm,
                });

                this.updateTaskList(res.data); // 只更新单个任务
                let sockMsg = {};
                sockMsg["type"] = "task";
                sockMsg["behavior"] = "updateTask";
                sockMsg["content"] = {
                  "state": this.editTaskState,
                  "editedTask": tempTaskForm
                };
                sockMsg["sender"] = this.userInfo.userId;
                socketApi.sendSock(
                  this.socketId,
                  sockMsg,
                  this.socketOnMessage
                );


              } else {
                this.$Message.error("Fail!");
              }
            })
            .catch((err) => {
              console.log(err.data);
            });
        } else {
          this.$Message.error("Please enter the necessary information!");
        }
      });
    },
    //查询task
    inquiryTask() {
      this.inquiryTodoTask();
      this.inquiryDoingTask();
      this.inquiryDoneTask();
    },
    inquiryTodoTask() {
      this.axios
        .get(
          "/GeoProblemSolving/task/inquiryTodo?" +
            "aid=" +
            this.activityInfo.aid
        )
        .then((res) => {
          this.todoLoading = false;
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
          } else if (res.data != "None" && res.data != "Fail") {
            this.$set(this, "taskTodo", res.data);
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.todoLoading = false;
        });
    },
    inquiryDoingTask() {
      this.axios
        .get(
          "/GeoProblemSolving/task/inquiryDoing?" +
            "aid=" +
            this.activityInfo.aid
        )
        .then((res) => {
          this.doingLoading = false;
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
          } else if (res.data != "None" && res.data != "Fail") {
            this.$set(this, "taskDoing", res.data);
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.doingLoading = false;
        });
    },
    inquiryDoneTask() {
      this.axios
        .get(
          "/GeoProblemSolving/task/inquiryDone?" +
            "aid=" +
            this.activityInfo.aid
        )
        .then((res) => {
          this.doneLoading = false;
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
          } else if (res.data != "None" && res.data != "Fail") {
            this.$set(this, "taskDone", res.data);
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.doneLoading = false;
          console.log(err.data);
        });
    },
    setMoveCount() {
      this.MoveCount = 2;
    },
    addMoveTask(taskList, type) {
      this.MoveCount--;
      this.taskOrderUpdate(taskList, type);
    },
    removeMoveTask(taskList, type) {
      this.MoveCount--;
      this.taskOrderUpdate(taskList, type);
    },
    updateMoveTask(taskList, type) {
      this.MoveCount -= 2;
      this.taskOrderUpdate(taskList, type);
    },
    taskOrderUpdate(taskList, type) {
      let thisUserName = this.$store.getters.userName;
      let stateChangeIndex = 0;
      let count = taskList.length;
      for (let i = 0; i < taskList.length; i++) {
        let thisTask = taskList[i];
        if (thisTask.order != i || thisTask.state != type) {
          if (thisTask.state != type) {
            stateChangeIndex = i;
            let taskUpdateObj = new URLSearchParams();
            taskUpdateObj.append("taskId", taskList[i]["taskId"]);
            taskUpdateObj.append("order", i);
            taskUpdateObj.append("state", type);
            taskUpdateObj.append("managerName", thisUserName);
            taskList[stateChangeIndex].order = stateChangeIndex;
            taskList[stateChangeIndex].managerName = thisUserName;
            taskList[stateChangeIndex].state = type;
            this.axios
              .post("/GeoProblemSolving/task/update", taskUpdateObj)
              .then((res) => {
                count--;
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  this.tempLoginModal = true;
                } else if (res.data != "Fail") {
                  //更新数组
                  // taskList[stateChangeIndex].order = stateChangeIndex;
                  // taskList[stateChangeIndex].managerName = thisUserName;
                  // taskList[stateChangeIndex].state = type;
                }
              })
              .catch((err) => {
                console.log(err.data);
              });
          } else {
            let taskUpdateObj = new URLSearchParams();
            taskUpdateObj.append("taskId", taskList[i]["taskId"]);
            taskUpdateObj.append("order", i);
            taskUpdateObj.append("state", type);
            this.axios
              .post("/GeoProblemSolving/task/update", taskUpdateObj)
              .then((res) => {
                count--;
                if (res.data == "Offline") {
                  this.$store.commit("userLogout");
                  // this.$router.push({ name: "Login" });
                  this.tempLoginModal = true;
                } else if (res.data != "Fail") {
                  taskList[i].order = i;
                }
              })
              .catch((err) => {
                console.log(err.data);
              });
          }
        }
      }
      let sockMsg = {};
      sockMsg["type"] = "task";
      sockMsg["behavior"] = "order";
      sockMsg["content"] = {
        state: type,
        taskList: taskList,
      };
      sockMsg["sender"] = this.userInfo.userId;
      socketApi.sendSock(this.socketId, sockMsg, this.socketOnMessage);
    },
    taskRemoveAssure(index, taskList) {
      this.taskDeleteModal = true;
      this.selectTaskIndex = index;
      this.removeTaskState = taskList[index].state;
      this.taskList = taskList;
    },
    taskRemove() {
      this.$axios
        .get(
          "/GeoProblemSolving/task/delete" +
            "?taskId=" +
            this.taskList[this.selectTaskIndex]["taskId"]
        )
        .then((res) => {
          // console.log(res);
          if (res.data == "Offline") {
            this.$store.commit("userLogout");
            // this.$router.push({ name: "Login" });
            this.tempLoginModal = true;
          } else if (res.data == "Success") {

            // update activity doc
            this.operationApi.taskUpdate(
              this.activityInfo.aid,
              "remove",
              this.taskList[this.selectTaskIndex]
            );
            this.$store.commit("updateActivityTasks", {
              behavior: "remove",
              task: this.taskList.splice(this.selectTaskIndex, 1)[0],
            });

            // 协同
            let sockMsg = {};
            sockMsg["type"] = "task";
            sockMsg["behavior"] = "remove";
            sockMsg["content"] = {
              state: this.removeTaskState,
              removeIndex: this.selectTaskIndex,
            };
            sockMsg["sender"] = this.userInfo.userId;
            socketApi.sendSock(this.socketId, sockMsg, this.socketOnMessage);
          } else {
            this.$Message.error("Fail!");
          }
        })
        .catch((err) => {
          this.$Message.error("Fail!");
        });
    },
    go2Activity(aid) {
      for (let i = 0; i < this.childActivities.length; i++) {
        if (this.childActivities[i].aid == aid) {
          window.location.href =
            "/activityInfo/" +
            this.projectInfo.aid +
            "?content=workspace&aid=" +
            this.childActivities[i].aid +
            "&level=" +
            this.childActivities[i].level;
        }
      }
    },
    initGantt() {
      let taskNum =
        this.taskDoing.length + this.taskTodo.length + this.taskDone.length;
      if (taskNum > 0) {
        this.ganttTasks = [];
      } else {
        this.ganttTasks = [
          {
            id: 1,
            name: "",
            user: "",
            start: new Date(),
            end: new Date(),
            progress: 0,
            type: "task",
          },
        ];
      }
      for (let i = 0; i < this.taskDoing.length; i++) {
        let gantttask = {
          id: i + 1,
          name: this.taskDoing[i].name,
          user: this.taskDoing[i].managerName,
          start: new Date(this.taskDoing[i].startTime),
          end: new Date(this.taskDoing[i].endTime),
          type: "task",
          progress: 100,
          style: {
            base: {
              fill: "#1EBC61",
            },
          },
        };
        this.ganttTasks.push(gantttask);
      }
      for (let i = 0; i < this.taskTodo.length; i++) {
        let gantttask = {
          id: this.taskDoing.length + i + 1,
          name: this.taskTodo[i].name,
          user: this.taskTodo[i].creatorName,
          start: new Date(this.taskTodo[i].startTime),
          end: new Date(this.taskTodo[i].endTime),
          type: "task",
          progress: 100,
          style: {
            base: {
              fill: "#F90",
            },
          },
        };
        this.ganttTasks.push(gantttask);
      }
      for (let i = 0; i < this.taskDone.length; i++) {
        let gantttask = {
          id: this.taskDoing.length + this.taskTodo.length + i + 1,
          name: this.taskDone[i].name,
          user: this.taskDone[i].creatorName,
          start: new Date(this.taskDone[i].startTime),
          end: new Date(this.taskDone[i].endTime),
          type: "task",
          progress: 100,
          style: {
            base: {
              fill: "#57A3F3",
            },
          },
        };
        this.ganttTasks.push(gantttask);
      }
    },
    switch2Manager() {
      this.chartSwitch = false;
      $("#todoPanel").css("color", "#57a3f3");
      $("#ganttPanel").css("color", "black");
    },
    switch2Gantt() {
      $("#todoPanel").css("color", "black");
      $("#ganttPanel").css("color", "#57a3f3");
      this.chartSwitch = true;

      this.initGantt();
    },
  },
};
</script>
<style scoped>
.createTaskBtn {
  font-size: 10px;
}
.createTaskBtn:hover {
  color: white;
  background: #47cb89;
}
.title {
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid lightgray;
}

.taskFormItem {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.taskFormItem span {
  text-align: center;
}
#taskContainer {
  padding: 10px;
  background-color: white;
}
.taskList {
  min-height: 60px;
  background: #f7f7f7;
}
.name {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 120px;
}
</style>

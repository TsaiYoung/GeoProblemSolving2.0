<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <!-- coding for your tools // begin-->
      <vue-scroll :ops="ops" style="height: calc(100vh - 40px)">
      <div style="padding: 10px">
        <div style="margin-bottom: 5px">
          <Button @click="add">Add</Button>
          <Button @click="edit" :disabled="this.selectSpecies.length != 1"
            >Edit</Button
          >
          <Button @click="beforeRemove" :disabled="this.selectSpecies.length != 1">Remove</Button>
          <Button @click="beforeSubmit" type="success" style="float: right"
            >Submit</Button
          >
        </div>
        <Table
          border
          ref="selection"
          :columns="fields"
          :data="species"
          @on-selection-change="beforeSelect"
          class="selectionTable"
        >
          <template slot-scope="{ row }" slot="name">
            <strong>{{ row.name }}</strong>
          </template>
        </Table>
        <div style="margin-top: 5px">
          <Button @click="beforeHandleSelectAll(true)">Set all selected</Button>
          <Button @click="beforeHandleSelectAll(false)">Cancel all selected</Button>
        </div>
      </div>
      </vue-scroll>
      <Modal
        v-model="addSpeciesModal"
        width="800"
        title="Add a tree species information"
      >
        <Form
          ref="speciesValidate"
          :model="speciesValidate"
          :rules="ruleValidate"
          :label-width="210"
        >
          <div style="display: flex">
            <FormItem label="Name" prop="name">
              <Input
                v-model="speciesValidate.name"
                placeholder="Enter the name"
                @on-focus="sendInputTyping('name', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.name,
                    'name'
                  )
                "
                @on-blur="sendInputTyping('name', 'out')"
                id="input_name"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Longevity"
              prop="longevity"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.longevity"
                placeholder="Enter the longevity"
                @on-focus="sendInputTyping('longevity', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.longevity,
                    'longevity'
                  )
                "
                @on-blur="sendInputTyping('longevity', 'out')"
                id="input_longevity"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Sexual maturity" prop="maturity">
              <Input
                v-model="speciesValidate.maturity"
                placeholder="Enter the sexual maturity"
                @on-focus="sendInputTyping('maturity', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maturity,
                    'maturity'
                  )
                "
                @on-blur="sendInputTyping('maturity', 'out')"
                id="input_maturity"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Shade tolerance"
              prop="s_tolerance"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.s_tolerance"
                placeholder="Enter the shade tolerance"
                @on-focus="sendInputTyping('s_tolerance', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.s_tolerance,
                    's_tolerance'
                  )
                "
                @on-blur="sendInputTyping('s_tolerance', 'out')"
                id="input_s_tolerance"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Fire tolerance" prop="f_tolerance">
              <Input
                v-model="speciesValidate.f_tolerance"
                placeholder="Enter the fire tolerance"
                @on-focus="sendInputTyping('f_tolerance', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.f_tolerance,
                    'f_tolerance'
                  )
                "
                @on-blur="sendInputTyping('f_tolerance', 'out')"
                id="input_f_tolerance"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Effective seeding distance"
              prop="e_seeding"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.e_seeding"
                placeholder="Enter the effective seeding distance"
                @on-focus="sendInputTyping('e_seeding', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.e_seeding,
                    'e_seeding'
                  )
                "
                @on-blur="sendInputTyping('e_seeding', 'out')"
                id="input_e_seeding"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Maximum seeding distance" prop="m_seeding">
              <Input
                v-model="speciesValidate.m_seeding"
                placeholder="Enter the maximum seeding distance"
                @on-focus="sendInputTyping('m_seeding', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.m_seeding,
                    'm_seeding'
                  )
                "
                @on-blur="sendInputTyping('m_seeding', 'out')"
                id="input_m_seeding"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Vegetative reproduction probability"
              prop="reproduction"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.reproduction"
                placeholder="Enter the vegetative reproduction probability"
                @on-focus="sendInputTyping('reproduction', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.reproduction,
                    'reproduction'
                  )
                "
                @on-blur="sendInputTyping('reproduction', 'out')"
                id="input_reproduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Minimum resprouting age" prop="min_resprouting">
              <Input
                v-model="speciesValidate.min_resprouting"
                placeholder="Enter the minimum resprouting age"
                @on-focus="sendInputTyping('min_resprouting', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.min_resprouting,
                    'min_resprouting'
                  )
                "
                @on-blur="sendInputTyping('min_resprouting', 'out')"
                id="input_min_resprouting"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Maximum resprouting age"
              prop="max_resprouting"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.max_resprouting"
                placeholder="Enter the maximum resprouting age"
                @on-focus="sendInputTyping('max_resprouting', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.max_resprouting,
                    'max_resprouting'
                  )
                "
                @on-blur="sendInputTyping('max_resprouting', 'out')"
                id="input_max_resprouting"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <FormItem label="Post-fire regeneration" prop="post_fire">
            <Input
              v-model="speciesValidate.post_fire"
              placeholder="Enter the post-fire regeneration"
              @on-focus="sendInputTyping('post_fire', 'in')"
              @on-change="
                sendInputParams(
                  speciesValidate.post_fire,
                  'post_fire'
                )
              "
              @on-blur="sendInputTyping('post_fire', 'out')"
              id="input_post_fire"
              class="addOrEditInputs"
            ></Input>
          </FormItem>
        </Form>
        <div slot="footer">
          <Button type="primary" @click="beforeHandleSubmit('add', 'speciesValidate')"
            >Submit</Button
          >
          <Button
            @click="beforeHandleReset('speciesValidate')"
            style="margin-left: 8px"
            >Reset</Button
          >
        </div>
      </Modal>
      <Modal
        v-model="editSpeciesModal"
        width="800"
        title="Edit a tree species information"
      >
        <Form
          ref="speciesValidate"
          :model="speciesValidate"
          :rules="ruleValidate"
          :label-width="210"
        >
          <div style="display: flex">
            <FormItem label="Name" prop="name">
              <Input
                v-model="speciesValidate.name"
                placeholder="Enter the name"
                readonly
              ></Input>
            </FormItem>
            <FormItem
              label="Longevity"
              prop="longevity"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.longevity"
                placeholder="Enter the longevity"
                @on-focus="sendInputTyping('longevity', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.longevity,
                    'longevity'
                  )
                "
                @on-blur="sendInputTyping('longevity', 'out')"
                id="input__longevity"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Sexual maturity" prop="maturity">
              <Input
                v-model="speciesValidate.maturity"
                placeholder="Enter the sexual maturity"
                @on-focus="sendInputTyping('maturity', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maturity,
                    'maturity'
                  )
                "
                @on-blur="sendInputTyping('maturity', 'out')"
                id="input__maturity"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Shade tolerance"
              prop="s_tolerance"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.s_tolerance"
                placeholder="Enter the shade tolerance"
                @on-focus="sendInputTyping('s_tolerance', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.s_tolerance,
                    's_tolerance'
                  )
                "
                @on-blur="sendInputTyping('s_tolerance', 'out')"
                id="input__s_tolerance"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Fire tolerance" prop="f_tolerance">
              <Input
                v-model="speciesValidate.f_tolerance"
                placeholder="Enter the fire tolerance"
                @on-focus="sendInputTyping('f_tolerance', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.f_tolerance,
                    'f_tolerance'
                  )
                "
                @on-blur="sendInputTyping('f_tolerance', 'out')"
                id="input__f_tolerance"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Effective seeding distance"
              prop="e_seeding"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.e_seeding"
                placeholder="Enter the effective seeding distance"
                @on-focus="sendInputTyping('e_seeding', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.e_seeding,
                    'e_seeding'
                  )
                "
                @on-blur="sendInputTyping('e_seeding', 'out')"
                id="input__e_seeding"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Maximum seeding distance" prop="m_seeding">
              <Input
                v-model="speciesValidate.m_seeding"
                placeholder="Enter the maximum seeding distance"
                @on-focus="sendInputTyping('m_seeding', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.m_seeding,
                    'm_seeding'
                  )
                "
                @on-blur="sendInputTyping('m_seeding', 'out')"
                id="input__m_seeding"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Vegetative reproduction probability"
              prop="reproduction"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.reproduction"
                placeholder="Enter the vegetative reproduction probability"
                @on-focus="sendInputTyping('reproduction', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.reproduction,
                    'reproduction'
                  )
                "
                @on-blur="sendInputTyping('reproduction', 'out')"
                id="input__reproduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Minimum resprouting age" prop="min_resprouting">
              <Input
                v-model="speciesValidate.min_resprouting"
                placeholder="Enter the minimum resprouting age"
                @on-focus="sendInputTyping('min_resprouting', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.min_resprouting,
                    'min_resprouting'
                  )
                "
                @on-blur="sendInputTyping('min_resprouting', 'out')"
                id="input__min_resprouting"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Maximum resprouting age"
              prop="max_resprouting"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.max_resprouting"
                placeholder="Enter the maximum resprouting age"
                @on-focus="sendInputTyping('max_resprouting', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.max_resprouting,
                    'max_resprouting'
                  )
                "
                @on-blur="sendInputTyping('max_resprouting', 'out')"
                id="input__max_resprouting"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <FormItem label="Post-fire regeneration" prop="post_fire">
            <Input
              v-model="speciesValidate.post_fire"
              placeholder="Enter the post-fire regeneration"
              @on-focus="sendInputTyping('post_fire', 'in')"
              @on-change="
                sendInputParams(
                  speciesValidate.post_fire,
                  'post_fire'
                )
              "
              @on-blur="sendInputTyping('post_fire', 'out')"
              id="input__post_fire"
              class="addOrEditInputs"
            ></Input>
          </FormItem>
        </Form>
        <div slot="footer">
          <Button
            type="primary"
            @click="beforeHandleSubmit('edit', 'speciesValidate')"
            >Submit</Button
          >
        </div>
      </Modal>
      <!-- // end -->
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      ops: {
        bar: {
          background: "#808695",
        },
      },
      // basic info
      activityInfo: {},
      toolId: "",
      participants: [],
      userInfo: {},
      resources: [],
      //
      selectSpecies: [],
      fields: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Name",
          slot: "name",
        },
        {
          title: "Longevity",
          key: "longevity",
        },
        {
          title: "Sexual maturity",
          key: "maturity",
        },
        {
          title: "Shade tolerance",
          key: "s_tolerance",
        },
        {
          title: "Fire tolerance",
          key: "f_tolerance",
        },
        {
          title: "Effective seeding distance",
          key: "e_seeding",
        },
        {
          title: "Maximum seeding distance",
          key: "m_seeding",
        },
        {
          title: "Vegetative reproduction probability",
          key: "reproduction",
        },
        {
          title: "Minimum resprouting age",
          key: "min_resprouting",
        },
        {
          title: "Maximum resprouting age",
          key: "max_resprouting",
        },
        {
          title: "Post-fire regeneration",
          key: "post_fire",
        },
      ],
      species: [
        {
          name: "Example",
          longevity: 200,
          maturity: 25,
          s_tolerance: 5,
          f_tolerance: 1,
          e_seeding: 130,
          m_seeding: 160,
          reproduction: 0.0,
          min_resprouting: 0,
          max_resprouting: 0,
          post_fire: "none",
          _disabled: true,
        },
      ],
      addSpeciesModal: false,
      editSpeciesModal: false,
      speciesValidate: {
        name: "",
        longevity: "",
        maturity: "",
        s_tolerance: "",
        f_tolerance: "",
        e_seeding: "",
        m_seeding: "",
        reproduction: "",
        min_resprouting: "",
        max_resprouting: "",
        post_fire: "",
      },
      ruleValidate: {
        name: [
          {
            required: true,
            message: "The name cannot be empty",
            trigger: "blur",
          },
        ],
        longevity: [
          {
            required: true,
            message: "The longevity cannot be empty",
            trigger: "blur",
          },
        ],
        maturity: [
          {
            required: true,
            message: "The sexual maturity cannot be empty",
            trigger: "blur",
          },
        ],
        s_tolerance: [
          {
            required: true,
            message: "The shade tolerance cannot be empty",
            trigger: "blur",
          },
        ],
        f_tolerance: [
          {
            required: true,
            message: "The fire tolerance cannot be empty",
            trigger: "blur",
          },
        ],
        e_seeding: [
          {
            required: true,
            message: "The effective seeding distance cannot be empty",
            trigger: "blur",
          },
        ],
        m_seeding: [
          {
            required: true,
            message: "The maximum seeding distance cannot be empty",
            trigger: "blur",
          },
        ],
        reproduction: [
          {
            required: true,
            message: "The vegetative reproduction probability cannot be empty",
            trigger: "blur",
          },
        ],
        min_resprouting: [
          {
            required: true,
            message: "The minimum resprouting age cannot be empty",
            trigger: "blur",
          },
        ],
        max_resprouting: [
          {
            required: true,
            message: "The maximum resprouting age cannot be empty",
            trigger: "blur",
          },
        ],
        post_fire: [
          {
            required: true,
            message: "The post-fire regeneration cannot be empty",
            trigger: "blur",
          },
        ],
      },
    };
  },
  created() {},
  mounted() {
    // 加载协同组件
    loadCollabComponent();
    this.getStepInfo();
  },
  methods: {
    getStepInfo() {
      if (componentStatus) {
        // 获取数据
        this.activityInfo = activityInfo;
        this.toolId = toolId;
        this.participants = onlineMembers;
        this.userInfo = userInfo;
        this.resources = resources;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
      } else {
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },
    getSocketComputation(data) {

    },
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "open"){
        //  点击add\edit按钮协同
        if(content.type == "add"){
          this.speciesValidate = {
            name: "",
            longevity: "",
            maturity: "",
            s_tolerance: "",
            f_tolerance: "",
            e_seeding: "",
            m_seeding: "",
            reproduction: "",
            min_resprouting: "",
            max_resprouting: "",
            post_fire: "",
          };
          this.addSpeciesModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is adding a tree species information '
                ])
              }
          });
        } else if (content.type == "edit"){
          this.speciesValidate = this.selectSpecies[0];
          this.editSpeciesModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is editing a tree species information '
                ])
              }
          });
        }

      } else if (behavior == "message"){
        //编辑信息协同1 add-input  获取和失去焦点
        let index = content.inputNum;
        if (content.inOrOut == "in") {
          document.getElementById('input_' + index).children[0].children[1].style.borderColor = "red";
          document.getElementById('input__' + index).children[0].children[1].style.borderColor = "red";
        } else {
          document.getElementById('input_' + index).children[0].children[1].style.borderColor = "#4caf50";
          document.getElementById('input__' + index).children[0].children[1].style.borderColor = "#4caf50";
        }

      } else if (behavior == "params"){
        //编辑信息协同2 add-input  输入参数
        let index = content.stateIndex;
        // document.getElementById('input_' + index).children[0].children[1].value = content.inputs;
        this.speciesValidate[index] = content.inputs;
      } else if (behavior == "submit") {
        // add\edit表单提交协同
        let behavior = content.behavior;
        let name = content.name;
        this.handleSubmit(behavior, name);
        this.addSpeciesModal = false;
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has submitted the tree species information '
                ])
              }
          });
      } else if (behavior == "reset") {
        // add表单reset协同
        let name = content.name;
        this.handleReset(name);
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has reset the tree species information '
                ])
              }
          });
      } else if (behavior == "select") {
        // 选择协同 type: selectChange、selectAll、cancelSelect
        if (content.type == "select-change"){
          let selection = content.data;
          // this.selectSpecies = Object.assign([], selection);
          this.select(selection);
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has updated the list of selected tree species information '
                ])
              }
          });
        } else if (content.type == "select-all"){
          this.handleSelectAll(true);
        } else if (content.type == "cancel-select"){
          this.handleSelectAll(false);
        }
      } else if (behavior == "remove"){
        this.remove();
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has removed the list of selected tree species information '
                ])
              }
          });
      } else if (behavior == "final-submit"){
        // this.submit();
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has submit the list of selected tree species information '
                ])
              }
          });
      } else if (behavior == "success"){
        // success
        this.$Notice.success({
          title: 'Submit successfully',
          duration: 10,});
      }
    },
    getSocketData(data) {},
    add() {
      this.speciesValidate = {
        name: "",
        longevity: "",
        maturity: "",
        s_tolerance: "",
        f_tolerance: "",
        e_seeding: "",
        m_seeding: "",
        reproduction: "",
        min_resprouting: "",
        max_resprouting: "",
        post_fire: "",
      };
      this.addSpeciesModal = true;

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "add",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    edit() {
      this.speciesValidate = this.selectSpecies[0];
      this.editSpeciesModal = true;

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "edit",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    beforeRemove(){
      this.remove();

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "remove",
        content: {
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    remove() {
      for (let i = 0; i < this.selectSpecies.length; i++) {
        for (let j = 0; j < this.species.length; j++) {
          if(this.selectSpecies[i].name === this.species[j].name){
            this.species.splice(j, 1);
            this.selectSpecies.splice(i, 1);
            break;
          }
        }
      }
    },
    beforeSubmit(){
      this.submit();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "final-submit",
        content: {
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    submit() {
      let aid = this.activityInfo.aid;
      let graphId = this.activityInfo.parent;
      let toolId = this.toolId;
      let participant = this.participants;
      let species = this.species;

      let submitInfo = {};
      submitInfo.aid = aid;
      submitInfo.graphId = graphId;
      submitInfo.toolId = toolId;
      submitInfo.participant = participant;
      submitInfo.species = [];

      for( let i = 1 ; i < species.length; i++) {
        submitInfo.species.push(species[i]);
      }

      this.axios
        .post("/GeoProblemSolving/landis/species",submitInfo)
        .then((res) => {
          console.log(res);
          if (res.data.data == "Offline") {
            this.$store.commit("userLogout");
            this.$router.push({ name: "Login" });
            // this.tempLoginModal = true;
          } else if (res.data.code == 0) {
            // success
            this.$Notice.success({
              title: 'Submit successfully',
              duration: 10,});
            // websocket
            let paramsMsg = {
              type: "operation",
              behavior: "success",
              content: {
              },
              sender: this.userInfo.userId,
            };
            sendCustomOperation(paramsMsg);
            let operationResult = res.data.data;
            let oid = operationResult.operationId
            loadingBackendOperation(oid)
          } else {
            console.log(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err.data);
        });
    },
    beforeSelect(selection){
      this.select(selection);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "select-change",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    select(selection) {
      this.selectSpecies = Object.assign([], selection);

      //修改Table的样式
      let table = document.getElementsByClassName("selectionTable");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 1 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].children[0].children[0].innerText == selection[i].name){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAll(status){
      this.handleSelectAll(status);

      // websocket
      let selectType = status ? "select-all" : "cancel-select"
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: selectType,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAll(status) {
      this.$refs.selection.selectAll(status);
      if (status) {
        // this.selectSpecies = Object.assign([], this.species);
        this.select(this.species);
      } else {
        // this.selectSpecies = [];
        this.select([]);
      }
    },
    beforeHandleSubmit(behavior, name) {
      this.handleSubmit(behavior, name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          behavior: behavior,
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmit(behavior, name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(_this.species, _this.speciesValidate);
          if (behavior === "add") {
            if (row >= 0) {
              this.$Message.error("Duplicate tree species information!");
            } else {
              _this.species.push(_this.speciesValidate);
              _this.addSpeciesModal = false;
            }
          } else if (behavior === "edit") {
            if (row >= 0) {
              _this.species.splice(row, 1, _this.speciesValidate);
              _this.editSpeciesModal = false;
              _this.select([]);
            } else {
              this.$Message.error("Information error!");
            }
          }
        } else {
          this.$Message.error(
            "Please provide complete information of species!"
          );
        }
      });
      this.afterHandleSubmit();
    },
    afterHandleSubmit(){
      // 清除css样式
      let doms = document.getElementsByClassName('addOrEditInputs');
      for( let i = 0 ; i < doms.length ; i++){
        doms[i].children[0].children[1].style.borderColor = "";
      }
    },

    beforeHandleReset(name) {
      this.handleReset(name);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "reset",
        content: {
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },

    handleReset(name) {
      this.$refs[name].resetFields();
      this.afterHandleSubmit();
    },


    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
    sendInputParams: function (modelInEvent, stateIndex) {
      sendInputParams(modelInEvent, stateIndex);
    },


    arrayContains(array, data) {
      var i = array.length;
      while (i--) {
        if (array[i].name != undefined && array[i].name === data.name) {
          return i;
        }
      }
      return i;
    },
  },
};
</script>
<style scoped>
</style>

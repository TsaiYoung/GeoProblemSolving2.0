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
            <FormItem label="Year" prop="year">
              <Input
                v-model="speciesValidate.year"
                placeholder="Enter the year"
                @on-focus="sendInputTyping('year', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.year,
                    'year'
                  )
                "
                @on-blur="sendInputTyping('year', 'out')"
                id="input_year"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Ecoregion"
              prop="ecoregion"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.ecoregion"
                placeholder="Enter the ecoregion"
                @on-focus="sendInputTyping('ecoregion', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.ecoregion,
                    'ecoregion'
                  )
                "
                @on-blur="sendInputTyping('ecoregion', 'out')"
                id="input_ecoregion"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Species" prop="species">
              <Input
                v-model="speciesValidate.species"
                placeholder="Enter the species"
                @on-focus="sendInputTyping('species', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.species,
                    'species'
                  )
                "
                @on-blur="sendInputTyping('species', 'out')"
                id="input_species"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="ProbEst"
              prop="probEst"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.probEst"
                placeholder="Enter the probEst"
                @on-focus="sendInputTyping('probEst', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.probEst,
                    'probEst'
                  )
                "
                @on-blur="sendInputTyping('probEst', 'out')"
                id="input_probEst"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="MaxANPP" prop="maxANPP">
              <Input
                v-model="speciesValidate.maxANPP"
                placeholder="Enter the maxANPP"
                @on-focus="sendInputTyping('maxANPP', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maxANPP,
                    'maxANPP'
                  )
                "
                @on-blur="sendInputTyping('maxANPP', 'out')"
                id="input_maxANPP"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="MaxB"
              prop="maxB"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.maxB"
                placeholder="Enter the maxB"
                @on-focus="sendInputTyping('maxB', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maxB,
                    'maxB'
                  )
                "
                @on-blur="sendInputTyping('maxB', 'out')"
                id="input_maxB"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
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
            <FormItem label="Year" prop="year">
              <Input
                v-model="speciesValidate.year"
                placeholder="Enter the year"
                @on-focus="sendInputTyping('year', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.year,
                    'year'
                  )
                "
                @on-blur="sendInputTyping('year', 'out')"
                id="input__year"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Ecoregion"
              prop="ecoregion"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.ecoregion"
                placeholder="Enter the ecoregion"
                @on-focus="sendInputTyping('ecoregion', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.ecoregion,
                    'ecoregion'
                  )
                "
                @on-blur="sendInputTyping('ecoregion', 'out')"
                id="input__ecoregion"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Species" prop="species">
              <Input
                v-model="speciesValidate.species"
                placeholder="Enter the species"
                readonly
              ></Input>
            </FormItem>
            <FormItem
              label="ProbEst"
              prop="probEst"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.probEst"
                placeholder="Enter the probEst"
                @on-focus="sendInputTyping('probEst', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.probEst,
                    'probEst'
                  )
                "
                @on-blur="sendInputTyping('probEst', 'out')"
                id="input__probEst"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="MaxANPP" prop="maxANPP">
              <Input
                v-model="speciesValidate.maxANPP"
                placeholder="Enter the maxANPP"
                @on-focus="sendInputTyping('maxANPP', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maxANPP,
                    'maxANPP'
                  )
                "
                @on-blur="sendInputTyping('maxANPP', 'out')"
                id="input__maxANPP"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="MaxB"
              prop="maxB"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesValidate.maxB"
                placeholder="Enter the maxB"
                @on-focus="sendInputTyping('maxB', 'in')"
                @on-change="
                  sendInputParams(
                    speciesValidate.maxB,
                    'maxB'
                  )
                "
                @on-blur="sendInputTyping('maxB', 'out')"
                id="input__maxB"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
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
          title: "Year",
          key: "year",
        },
        {
          title: "Ecoregion",
          key: "ecoregion",
        },
        {
          title: "Species",
          key: "species",
        },
        {
          title: "ProbEst",
          key: "probEst",
        },
        {
          title: "MaxANPP",
          key: "maxANPP",
        },
        {
          title: "MaxB",
          key: "maxB",
        },
      ],
      species: [
        {
          year: 2020,
          ecoregion: 101,
          species: "Example",
          probEst: 0.9,
          maxANPP: 886,
          maxB: 26000,
          _disabled: true,
        },
      ],
      addSpeciesModal: false,
      editSpeciesModal: false,
      speciesValidate: {
        year: "",
        ecoregion: "",
        species: "",
        probEst: "",
        maxANPP: "",
        maxB: "",
      },
      ruleValidate: {
        year: [
          {
            required: true,
            message: "The year cannot be empty",
            trigger: "blur",
          },
        ],
        ecoregion: [
          {
            required: true,
            message: "The ecoregion cannot be empty",
            trigger: "blur",
          },
        ],
        species: [
          {
            required: true,
            message: "The species cannot be empty",
            trigger: "blur",
          },
        ],
        probEst: [
          {
            required: true,
            message: "The probEst cannot be empty",
            trigger: "blur",
          },
        ],
        maxANPP: [
          {
            required: true,
            message: "The maxANPP cannot be empty",
            trigger: "blur",
          },
        ],
        maxB: [
          {
            required: true,
            message: "The maxB cannot be empty",
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
    getSocketComputation(data) {},
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "open"){
        //  点击add\edit按钮协同
        if(content.type == "add"){
          this.speciesValidate = {
            year: "",
            ecoregion: "",
            species: "",
            probEst: "",
            maxANPP: "",
            maxB: "",
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
      }else if (behavior == "final-submit"){
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
        year: "",
        ecoregion: "",
        species: "",
        probEst: "",
        maxANPP: "",
        maxB: "",
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
      let dynamicInput = this.species;

      let submitInfo = {};
      submitInfo.aid = aid;
      submitInfo.graphId = graphId;
      submitInfo.toolId = toolId;
      submitInfo.participant = participant;
      submitInfo.dynamicInput = dynamicInput;

      console.log(submitInfo);
      this.axios
        .post("/GeoProblemSolving/landis/dynamicInput",submitInfo)
        .then((res) => {
          console.log(res);
          if (res.data == "Offline") {
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
          if( tableRow.children[j].children[2].children[0].children[0].innerText == selection[i].species){
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
              _this.selectSpecies = [];
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
        if (array[i].species != undefined && array[i].species === data.species) {
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

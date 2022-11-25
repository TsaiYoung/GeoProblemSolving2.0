<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <vue-scroll :ops="ops" style="height: calc(100vh - 40px)">
        <!-- coding for your tools // begin-->
        <Divider orientation="left">REQUIRED INPUTS</Divider>
        <!-- Timestep -->
        <div style="margin: 10px 20px">
          <Label class="labelTile">Timestep:</Label>
          <Input
            v-model="timeStep"
            placeholder="Enter the time step..."
            style="width: 100px"
            @on-focus="sendInputTyping('timeStep', 'in')"
            @on-change="
              sendInputParams(
                timeStep,
                'timeStep'
              )
            "
            @on-blur="sendInputTyping('timeStep', 'out')"
            id="input_timeStep"
            class="addOrEditInputs"
          />
        </div>
        <!-- SeedingAlgorithm  -->
        <div style="margin: 10px 20px">
          <Label class="labelTile">SeedingAlgorithm:</Label>
          <Select v-model="seedingAlgorithm" style="width: 200px" @on-change="seedingAlgorithmChange">
            <Option value="WardSeedDispersal">WardSeedDispersal</Option>
            <Option value="NoDispersal">NoDispersal</Option>
            <Option value="UniversalDispersal">UniversalDispersal</Option>
          </Select>
        </div>
        <!-- InitialCommunities and InitialCommunitiesMap -->
        <div style="margin: 10px 20px">
          <div>
            <Button
              type="info"
              style="width: 200px"
              :loading="boolInitialCommunities"
              @click="selectInitialCommunities"
            >
              <span v-if="!boolInitialCommunities"
                >Select initial communities</span
              >
              <span v-else>Importing data...</span>
            </Button>
            <Label class="labelTile" style="margin-left: 20px"
              >InitialCommunities:
              {{ initialCommunities.name + initialCommunities.suffix }}</Label
            >
            <Label class="labelTile" style="margin-left: 20px"
              >InitialCommunitiesMap:
              {{
                initialCommunitiesMap.name + initialCommunitiesMap.suffix
              }}</Label
            >
          </div>
        </div>
        <!-- ClimateConfigFile -->
        <div style="margin: 10px 20px">
          <Button
            type="info"
            style="width: 200px"
            :loading="boolClimateConfigFile"
            @click="selectClimateConfigFile"
          >
            <span v-if="!boolClimateConfigFile"
              >Select climate config file</span
            >
            <span v-else>Importing data...</span>
          </Button>
          <Label class="labelTile" style="margin-left: 20px"
            >ClimateConfigFile:
            {{ climateConfigFile.name + climateConfigFile.suffix }}</Label
          >
        </div>
        <!-- CalibrateMode  -->
        <div style="margin: 10px 20px">
          <Label class="labelTile">CalibrateMode:</Label>
          <i-switch @on-change="calibrateModeChange" v-model="boolCalibrateMode">
            <span slot="open">Yes</span>
            <span slot="close">No</span>
          </i-switch>
        </div>
        <!-- SpinUpMortalityFraction  -->
        <div style="margin: 10px 20px">
          <Label class="labelTile">SpinUpMortalityFraction:</Label>
          <Input
            v-model="spinUpMortalityFraction"
            placeholder="Enter the spin up mortality fraction..."
            style="width: 100px"
            @on-focus="sendInputTyping('spinUpMortalityFraction', 'in')"
            @on-change="
              sendInputParams(
                spinUpMortalityFraction,
                'spinUpMortalityFraction'
              )
            "
            @on-blur="sendInputTyping('spinUpMortalityFraction', 'out')"
            id="input_spinUpMortalityFraction"
            class="addOrEditInputs"
          />
        </div>

        <Divider orientation="left">LIFE HISTORY PARAMETERS</Divider>
        <!-- MinRelativeBiomass -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">MinRelativeBiomass:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button @click="addMRB()">Add</Button>
              <Button @click="beforeRemoveMRB()" :disabled="this.selectedMRB.length != 1">Remove</Button>
            </div>
          </div>
          <Table
            border
            ref="selection0"
            :columns="shadeClasses"
            :data="ecoregionsMRB"
            @on-selection-change="beforeSelectMRB"
            class="selectionTableMRB"
          >
            <template slot-scope="{ row }" slot="name">
              <strong>{{ row.name }}</strong>
            </template>
          </Table>
          <div style="margin-top: 5px">
            <Button @click="beforeHandleSelectAllMRB(true)">Set all selected</Button>
            <Button @click="beforeHandleSelectAllMRB(false)"
              >Cancel all selected</Button
            >
          </div>
        </div>
        <!-- SufficientLight -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">SufficientLight:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button @click="beforeEditSL()" :disabled="this.selectedSL.length != 1"
                >Edit</Button
              >
            </div>
          </div>
          <Table
            border
            ref="selection1"
            :columns="lightConditions"
            :data="SEP"
            @on-selection-change="beforeSelectSL"
            class="selectionTableSL"
          >
          </Table>
        </div>
        <!-- SpeciesParameters -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">SpeciesParameters:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button @click="addSP()">Add</Button>
              <Button @click="beforeRemoveSP()" :disabled="this.selectedSP.length != 1">Remove</Button>
            </div>
          </div>
          <Table
            border
            ref="selection2"
            :columns="speciesInfo"
            :data="speciesSP"
            @on-selection-change="beforeSelectSP"
            class="selectionTableSP"
          >
          </Table>
          <div style="margin-top: 5px">
            <Button @click="beforeHandleSelectAllSP(true)">Set all selected</Button>
            <Button @click="beforeHandleSelectAllSP(false)"
              >Cancel all selected</Button
            >
          </div>
        </div>
        <!-- EcoregionParameters -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">EcoregionParameters:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button @click="addEP()">Add</Button>
              <Button @click="beforeRemoveEP()">Remove</Button>
            </div>
          </div>
          <Table
            border
            ref="selection3"
            :columns="ecoregionParameters"
            :data="AET"
            @on-selection-change="beforeSelectEP"
            class="selectionTableEP"
          >
          </Table>
          <div style="margin-top: 5px">
            <Button @click="beforeHandleSelectAllEP(true)">Set all selected</Button>
            <Button @click="beforeHandleSelectAllEP(false)"
              >Cancel all selected</Button
            >
          </div>
        </div>
        <!-- DynamicInputFile   -->
        <div style="margin: 10px 20px">
          <Button
            type="info"
            style="width: 250px"
            :loading="boolDynamicInputFile"
            @click="selectDynamicInputFile"
          >
            <span v-if="!boolDynamicInputFile">Select dynamic input file</span>
            <span v-else>Importing data...</span>
          </Button>
          <Label class="labelTile" style="margin-left: 20px"
            >DynamicInputFile:
            {{ dynamicInputFile.name + dynamicInputFile.suffix }}</Label
          >
        </div>
        <!-- FireReductionParameters -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">FireReductionParameters:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button
                @click="beforeEditFRP()"
                :disabled="this.selectedFRP.length != 1"
                >Edit</Button
              >
            </div>
          </div>
          <Table
            border
            ref="selection4"
            :columns="fireReductionParams"
            :data="fireRows"
            @on-selection-change="beforeSelectFRP"
            class="selectionTableFRP"
          >
          </Table>
        </div>
        <!-- HarvestReductionParameters -->
        <div style="margin: 10px 20px">
          <div style="display: flex">
            <Label class="labelTile">HarvestReductionParameters:</Label>
            <div style="margin-bottom: 5px; margin-left: 10px">
              <Button @click="addHRP()">Add</Button>
              <Button @click="beforeRemoveHRP()" :disabled="this.selectedHRP.length != 1">Remove</Button>
            </div>
          </div>
          <Table
            border
            ref="selection5"
            :columns="harvestReductionParams"
            :data="harvestRows"
            @on-selection-change="beforeSelectHRP"
            class="selectionTableHRP"
          >
          </Table>
          <div style="margin-top: 5px">
            <Button @click="beforeHandleSelectAllHRP(true)">Set all selected</Button>
            <Button @click="beforeHandleSelectAllHRP(false)"
              >Cancel all selected</Button
            >
          </div>
        </div>
        <Divider></Divider>
        <Button
          @click="beforeSubmit()"
          type="success"
          style="margin: 0 20px 20px; width: 200px"
          >Submit</Button
        >
        <Button
          @click="beforeResetAll()"
          type="warning"
          style="margin: 0 20px 20px; width: 200px"
          >Clear All</Button
        >
      </vue-scroll>
      <!-- // end -->
      <!-- MinRelativeBiomass Modal -->
      <Modal
        v-model="addMinRelativeBiomassModal"
        width="600"
        title="Add the minimum relative biomass for shade classes 1 - 5"
      >
        <Form
          ref="minRelativeBiomassVal"
          :model="minRelativeBiomassVal"
          :rules="ruleValidate1"
          :label-width="100"
        >
          <div style="display: flex">
            <FormItem label="Name" prop="ecoregionName">
              <Input
                v-model="minRelativeBiomassVal.ecoregionName"
                placeholder="Enter the ecoregion name"
                @on-focus="sendInputTyping('MRB_ecoregionName', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.ecoregionName,
                    'MRB_ecoregionName'
                  )
                "
                @on-blur="sendInputTyping('MRB_ecoregionName', 'out')"
                id="input_MRB_ecoregionName"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="Class 1" prop="class1" style="margin-left: 20px">
              <Input
                v-model="minRelativeBiomassVal.class1"
                placeholder="Enter the minimum relative biomass for shade class 1"
                @on-focus="sendInputTyping('MRB_class1', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.class1,
                    'MRB_class1'
                  )
                "
                @on-blur="sendInputTyping('MRB_class1', 'out')"
                id="input_MRB_class1"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Class 2" prop="class2">
              <Input
                v-model="minRelativeBiomassVal.class2"
                placeholder="Enter the minimum relative biomass for shade class 2"
                @on-focus="sendInputTyping('MRB_class2', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.class2,
                    'MRB_class2'
                  )
                "
                @on-blur="sendInputTyping('MRB_class2', 'out')"
                id="input_MRB_class2"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="Class 3" prop="class3" style="margin-left: 20px">
              <Input
                v-model="minRelativeBiomassVal.class3"
                placeholder="Enter the minimum relative biomass for shade class 3"
                @on-focus="sendInputTyping('MRB_class3', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.class3,
                    'MRB_class3'
                  )
                "
                @on-blur="sendInputTyping('MRB_class3', 'out')"
                id="input_MRB_class3"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Class 4" prop="class4">
              <Input
                v-model="minRelativeBiomassVal.class4"
                placeholder="Enter the minimum relative biomass for shade class 4"
                @on-focus="sendInputTyping('MRB_class4', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.class4,
                    'MRB_class4'
                  )
                "
                @on-blur="sendInputTyping('MRB_class4', 'out')"
                id="input_MRB_class4"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="Class 5" prop="class5" style="margin-left: 20px">
              <Input
                v-model="minRelativeBiomassVal.class5"
                placeholder="Enter the minimum relative biomass for shade class 5"
                @on-focus="sendInputTyping('MRB_class5', 'in')"
                @on-change="
                  sendInputParams(
                    minRelativeBiomassVal.class5,
                    'MRB_class5'
                  )
                "
                @on-blur="sendInputTyping('MRB_class5', 'out')"
                id="input_MRB_class5"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
        </Form>
        <div slot="footer">
          <Button
            type="primary"
            @click="beforeHandleSubmitMRB('add', 'minRelativeBiomassVal')"
            >Submit</Button
          >
          <Button
            @click="beforeHandleReset('minRelativeBiomassVal')"
            style="margin-left: 8px"
            >Reset</Button
          >
        </div>
      </Modal>
      <!-- SufficientLight -->
      <Modal
        v-model="editSufficientLightModal"
        width="750"
        title="Edit the probability of establishment for each species shade tolerance class"
      >
        <Form
          ref="sufficientLightVal"
          :model="sufficientLightVal"
          :rules="ruleValidate2"
          :label-width="150"
        >
          <div style="display: flex">
            <FormItem label="Shade class" prop="shadeclass">
              <Input v-model="sufficientLightVal.shadeclass" readonly></Input>
            </FormItem>
            <FormItem
              label="Light condition 0"
              prop="light0"
              style="margin-left: 20px"
            >
              <Input
                v-model="sufficientLightVal.light0"
                placeholder="Enter the light condition 0"
                @on-focus="sendInputTyping('SL_light0', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light0,
                    'SL_light0'
                  )
                "
                @on-blur="sendInputTyping('SL_light0', 'out')"
                id="input_SL_light0"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Light condition 1" prop="light1">
              <Input
                v-model="sufficientLightVal.light1"
                placeholder="Enter the light condition 1"
                @on-focus="sendInputTyping('SL_light1', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light1,
                    'SL_light1'
                  )
                "
                @on-blur="sendInputTyping('SL_light1', 'out')"
                id="input_SL_light1"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Light condition 2"
              prop="light2"
              style="margin-left: 20px"
            >
              <Input
                v-model="sufficientLightVal.light2"
                placeholder="Enter the light condition 2"
                @on-focus="sendInputTyping('SL_light2', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light2,
                    'SL_light2'
                  )
                "
                @on-blur="sendInputTyping('SL_light2', 'out')"
                id="input_SL_light2"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Light condition 3" prop="light3">
              <Input
                v-model="sufficientLightVal.light3"
                placeholder="Enter the light condition 3"
                @on-focus="sendInputTyping('SL_light3', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light3,
                    'SL_light3'
                  )
                "
                @on-blur="sendInputTyping('SL_light3', 'out')"
                id="input_SL_light3"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Light condition 4"
              prop="light4"
              style="margin-left: 20px"
            >
              <Input
                v-model="sufficientLightVal.light4"
                placeholder="Enter the light condition 4"
                @on-focus="sendInputTyping('SL_light4', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light4,
                    'SL_light4'
                  )
                "
                @on-blur="sendInputTyping('SL_light4', 'out')"
                id="input_SL_light4"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Light condition 5" prop="light5">
              <Input
                v-model="sufficientLightVal.light5"
                placeholder="Enter the light condition 5"
                @on-focus="sendInputTyping('SL_light5', 'in')"
                @on-change="
                  sendInputParams(
                    sufficientLightVal.light5,
                    'SL_light5'
                  )
                "
                @on-blur="sendInputTyping('SL_light5', 'out')"
                id="input_SL_light5"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
        </Form>
        <div slot="footer">
          <Button type="primary" @click="beforeHandleSubmitSL('sufficientLightVal')"
            >Submit</Button
          >
        </div>
      </Modal>
      <!-- SpeciesParameters -->
      <Modal
        v-model="addSpeciesParametersModal"
        width="800"
        title="Add the species’ biomass parameters"
      >
        <Form
          ref="speciesParametersVal"
          :model="speciesParametersVal"
          :rules="ruleValidate3"
          :label-width="150"
        >
          <div style="display: flex">
            <FormItem label="Name" prop="Name">
              <Input
                v-model="speciesParametersVal.name"
                placeholder="Enter the species name"
                @on-focus="sendInputTyping('SP_name', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.name,
                    'SP_name'
                  )
                "
                @on-blur="sendInputTyping('SP_name', 'out')"
                id="input_SP_name"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Leaf longevity"
              prop="leaf_longevity"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesParametersVal.leaf_longevity"
                placeholder="Enter the leaf longevity"
                @on-focus="sendInputTyping('SP_leaf_longevity', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.leaf_longevity,
                    'SP_leaf_longevity'
                  )
                "
                @on-blur="sendInputTyping('SP_leaf_longevity', 'out')"
                id="input_SP_leaf_longevity"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Woody decay rate" prop="woody_decay_rate">
              <Input
                v-model="speciesParametersVal.woody_decay_rate"
                placeholder="Enter the woody decay rate"
                @on-focus="sendInputTyping('SP_woody_decay_rate', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.woody_decay_rate,
                    'SP_woody_decay_rate'
                  )
                "
                @on-blur="sendInputTyping('SP_woody_decay_rate', 'out')"
                id="input_SP_woody_decay_rate"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Mortality"
              prop="mortality"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesParametersVal.mortality"
                placeholder="Enter the mortality curve – shape parameter"
                @on-focus="sendInputTyping('SP_mortality', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.mortality,
                    'SP_mortality'
                  )
                "
                @on-blur="sendInputTyping('SP_mortality', 'out')"
                id="input_SP_mortality"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Growth" prop="growth">
              <Input
                v-model="speciesParametersVal.growth"
                placeholder="Enter the growth curve – shape parameter"
                @on-focus="sendInputTyping('SP_growth', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.growth,
                    'SP_growth'
                  )
                "
                @on-blur="sendInputTyping('SP_growth', 'out')"
                id="input_SP_growth"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Leaf lignin"
              prop="leaf_lignin"
              style="margin-left: 20px"
            >
              <Input
                v-model="speciesParametersVal.leaf_lignin"
                placeholder="Enter the leaf lignin"
                @on-focus="sendInputTyping('SP_leaf_lignin', 'in')"
                @on-change="
                  sendInputParams(
                    speciesParametersVal.leaf_lignin,
                    'SP_leaf_lignin'
                  )
                "
                @on-blur="sendInputTyping('SP_leaf_lignin', 'out')"
                id="input_SP_leaf_lignin"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
        </Form>
        <div slot="footer">
          <Button type="primary" @click="beforeHandleSubmitSP('speciesParametersVal')"
            >Submit</Button
          >
          <Button
            @click="beforeHandleReset('speciesParametersVal')"
            style="margin-left: 8px"
            >Reset</Button
          >
        </div>
      </Modal>
      <!-- EcoregionParameters -->
      <Modal
        v-model="addEcoregionParametersModal"
        width="600"
        title="Add the ecoregion parameters"
      >
        <Form
          ref="ecoregionParametersVal"
          :model="ecoregionParametersVal"
          :rules="ruleValidate4"
          :label-width="100"
        >
          <div style="display: flex">
            <FormItem label="Ecoregions" prop="ecoregion">
              <Input
                v-model="ecoregionParametersVal.ecoregion"
                placeholder="Enter the ecoregion name"
                @on-focus="sendInputTyping('EP_ecoregion', 'in')"
                @on-change="
                  sendInputParams(
                    ecoregionParametersVal.ecoregion,
                    'EP_ecoregion'
                  )
                "
                @on-blur="sendInputTyping('EP_ecoregion', 'out')"
                id="input_EP_ecoregion"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="AET" prop="aet" style="margin-left: 20px">
              <Input
                v-model="ecoregionParametersVal.aet"
                placeholder="Enter the actual evapotranspiration (AET)"
                @on-focus="sendInputTyping('EP_aet', 'in')"
                @on-change="
                  sendInputParams(
                    ecoregionParametersVal.aet,
                    'EP_aet'
                  )
                "
                @on-blur="sendInputTyping('EP_aet', 'out')"
                id="input_EP_aet"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
        </Form>
        <div slot="footer">
          <Button
            type="primary"
            @click="beforeHandleSubmitEP('ecoregionParametersVal')"
            >Submit</Button
          >
          <Button
            @click="beforeHandleReset('ecoregionParametersVal')"
            style="margin-left: 8px"
            >Reset</Button
          >
        </div>
      </Modal>
      <!-- FireReductionParameters -->
      <Modal
        v-model="editFireReductionParametersModal"
        width="750"
        title="Edit the fire reduction parameters"
      >
        <Form
          ref="fireReductionParametersVal"
          :model="fireReductionParametersVal"
          :rules="ruleValidate5"
          :label-width="150"
        >
          <div style="display: flex">
            <FormItem label="Fire severity" prop="severity">
              <Input v-model="fireReductionParametersVal.severity" readonly></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Wood reduction" prop="wood_reduction">
              <Input
                v-model="fireReductionParametersVal.wood_reduction"
                placeholder="Enter the wood reduction"
                @on-focus="sendInputTyping('FRP_wood_reduction', 'in')"
                @on-change="
                  sendInputParams(
                    fireReductionParametersVal.wood_reduction,
                    'FRP_wood_reduction'
                  )
                "
                @on-blur="sendInputTyping('FRP_wood_reduction', 'out')"
                id="input_FRP_wood_reduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem
              label="Litter reduction"
              prop="litter_reduction"
              style="margin-left: 20px"
            >
              <Input
                v-model="fireReductionParametersVal.litter_reduction"
                placeholder="Enter the litter reduction"
                @on-focus="sendInputTyping('FRP_litter_reduction', 'in')"
                @on-change="
                  sendInputParams(
                    fireReductionParametersVal.litter_reduction,
                    'FRP_litter_reduction'
                  )
                "
                @on-blur="sendInputTyping('FRP_litter_reduction', 'out')"
                id="input_FRP_litter_reduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>          
        </Form>
        <div slot="footer">
          <Button type="primary" @click="beforeHandleSubmitFRP('fireReductionParametersVal')"
            >Submit</Button
          >
        </div>
      </Modal>
      <!-- HarvestReductionParameters -->
      <Modal
        v-model="addHarvestReductionParametersModal"
        width="800"
        title="Add the harvest reduction parameters"
      >
        <Form
          ref="harvestReductionParametersVal"
          :model="harvestReductionParametersVal"
          :rules="ruleValidate6"
          :label-width="150"
        >
          <div style="display: flex">
            <FormItem label="Prescription name" prop="name">
              <Input
                v-model="harvestReductionParametersVal.name"
                placeholder="Enter the prescription name"
                @on-focus="sendInputTyping('HRP_name', 'in')"
                @on-change="
                  sendInputParams(
                    harvestReductionParametersVal.name,
                    'HRP_name'
                  )
                "
                @on-blur="sendInputTyping('HRP_name', 'out')"
                id="input_HRP_name"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="Dead wood reduction" prop="wood_reduction" style="margin-left: 20px">
              <Input
                v-model="harvestReductionParametersVal.wood_reduction"
                placeholder="Enter the proportion (0.0 – 1.0) of removed dead wood biomass"
                @on-focus="sendInputTyping('HRP_wood_reduction', 'in')"
                @on-change="
                  sendInputParams(
                    harvestReductionParametersVal.wood_reduction,
                    'HRP_wood_reduction'
                  )
                "
                @on-blur="sendInputTyping('HRP_wood_reduction', 'out')"
                id="input_HRP_wood_reduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Dead litter reduction" prop="litter_reduction">
              <Input
                v-model="harvestReductionParametersVal.litter_reduction"
                placeholder="Enter the proportion (0.0 – 1.0) of removed dead litter biomass"
                @on-focus="sendInputTyping('HRP_litter_reduction', 'in')"
                @on-change="
                  sendInputParams(
                    harvestReductionParametersVal.litter_reduction,
                    'HRP_litter_reduction'
                  )
                "
                @on-blur="sendInputTyping('HRP_litter_reduction', 'out')"
                id="input_HRP_litter_reduction"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
            <FormItem label="Cohort wood removal" prop="wood_removal" style="margin-left: 20px">
              <Input
                v-model="harvestReductionParametersVal.wood_removal"
                placeholder="Enter the the proportion (0.0 – 1.0) of harvested cohort live wood biomass"
                @on-focus="sendInputTyping('HRP_wood_removal', 'in')"
                @on-change="
                  sendInputParams(
                    harvestReductionParametersVal.wood_removal,
                    'HRP_wood_removal'
                  )
                "
                @on-blur="sendInputTyping('HRP_wood_removal', 'out')"
                id="input_HRP_wood_removal"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
          <div style="display: flex">
            <FormItem label="Cohort leaf removal" prop="leaf_removal">
              <Input
                v-model="harvestReductionParametersVal.leaf_removal"
                placeholder="Enter the proportion (0.0 – 1.0) of harvested cohort live leaf biomass"
                @on-focus="sendInputTyping('HRP_leaf_removal', 'in')"
                @on-change="
                  sendInputParams(
                    harvestReductionParametersVal.leaf_removal,
                    'HRP_leaf_removal'
                  )
                "
                @on-blur="sendInputTyping('HRP_leaf_removal', 'out')"
                id="input_HRP_leaf_removal"
                class="addOrEditInputs"
              ></Input>
            </FormItem>
          </div>
        </Form>
        <div slot="footer">
          <Button
            type="primary"
            @click="beforeHandleSubmitHRP('harvestReductionParametersVal')"
            >Submit</Button
          >
          <Button
            @click="beforeHandleReset('harvestReductionParametersVal')"
            style="margin-left: 8px"
            >Reset</Button
          >
        </div>
      </Modal>
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
      userInfo: {},
      toolId: "",
      participants: [],
      resources: [],
      //
      timeStep: 10,
      seedingAlgorithm: "WardSeedDispersal",
      boolInitialCommunities: false,
      initialCommunities: {},
      initialCommunitiesMap: {},
      boolClimateConfigFile: false,
      climateConfigFile: {},
      calibrateMode: "No",
      boolCalibrateMode: false,
      spinUpMortalityFraction: 0.0,

      // MinRelativeBiomass data
      shadeClasses: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Ecoregions\\Shade",
          key: "ecoregionName",
        },
        {
          title: "1",
          key: "class1",
        },
        {
          title: "2",
          key: "class2",
        },
        {
          title: "3",
          key: "class3",
        },
        {
          title: "4",
          key: "class4",
        },
        {
          title: "5",
          key: "class5",
        },
      ],
      ecoregionsMRB: [],
      minRelativeBiomassVal: {
        ecoregionName: "",
        class1: "",
        class2: "",
        class3: "",
        class4: "",
        class5: "",
      },
      ruleValidate1: {
        ecoregionName: [
          {
            required: true,
            message: "The ecoregion name cannot be empty",
            trigger: "blur",
          },
        ],
        class1: [
          {
            required: true,
            message:
              "The minimum relative biomass for shade class 1 cannot be empty",
            trigger: "blur",
          },
        ],
        class2: [
          {
            required: true,
            message:
              "The minimum relative biomass for shade class 2 cannot be empty",
            trigger: "blur",
          },
        ],
        class3: [
          {
            required: true,
            message:
              "The minimum relative biomass for shade class 3 cannot be empty",
            trigger: "blur",
          },
        ],
        class4: [
          {
            required: true,
            message:
              "The minimum relative biomass for shade class 4 cannot be empty",
            trigger: "blur",
          },
        ],
        class5: [
          {
            required: true,
            message:
              "The minimum relative biomass for shade class 5 cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedMRB: [],
      addMinRelativeBiomassModal: false,
      // SufficientLight data
      lightConditions: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Shade\\SEP",
          key: "shadeclass",
        },
        {
          title: "0",
          key: "light0",
        },
        {
          title: "1",
          key: "light1",
        },
        {
          title: "2",
          key: "light2",
        },
        {
          title: "3",
          key: "light3",
        },
        {
          title: "4",
          key: "light4",
        },
        {
          title: "5",
          key: "light5",
        },
      ],
      SEP: [
        {
          shadeclass: 1,
          light0: 0.0,
          light1: 0.0,
          light2: 0.0,
          light3: 0.0,
          light4: 0.0,
          light5: 0.0,
        },
        {
          shadeclass: 2,
          light0: 0.0,
          light1: 0.0,
          light2: 0.0,
          light3: 0.0,
          light4: 0.0,
          light5: 0.0,
        },
        {
          shadeclass: 3,
          light0: 0.0,
          light1: 0.0,
          light2: 0.0,
          light3: 0.0,
          light4: 0.0,
          light5: 0.0,
        },
        {
          shadeclass: 4,
          light0: 0.0,
          light1: 0.0,
          light2: 0.0,
          light3: 0.0,
          light4: 0.0,
          light5: 0.0,
        },
        {
          shadeclass: 5,
          light0: 0.0,
          light1: 0.0,
          light2: 0.0,
          light3: 0.0,
          light4: 0.0,
          light5: 0.0,
        },
      ],
      sufficientLightVal: {
        shadeclass: 1,
        light0: 0.0,
        light1: 0.0,
        light2: 0.0,
        light3: 0.0,
        light4: 0.0,
        light5: 0.0,
      },
      ruleValidate2: {
        light0: [
          {
            required: true,
            message: "The SEP at site-level light condition 0 cannot be empty",
            trigger: "blur",
          },
        ],
        light1: [
          {
            required: true,
            message: "The SEP at site-level light condition 1 cannot be empty",
            trigger: "blur",
          },
        ],
        light2: [
          {
            required: true,
            message: "The SEP at site-level light condition 2 cannot be empty",
            trigger: "blur",
          },
        ],
        light3: [
          {
            required: true,
            message: "The SEP at site-level light condition 3 cannot be empty",
            trigger: "blur",
          },
        ],
        light4: [
          {
            required: true,
            message: "The SEP at site-level light condition 4 cannot be empty",
            trigger: "blur",
          },
        ],
        light5: [
          {
            required: true,
            message: "The SEP at site-level light condition 5 cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedSL: {},
      editSufficientLightModal: false,
      // SpeciesParameters data
      speciesInfo: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Species",
          key: "name",
        },
        {
          title: "Leaf Longevity",
          key: "leaf_longevity",
        },
        {
          title: "Woody Decay Rate",
          key: "woody_decay_rate",
        },
        {
          title: "Mortality",
          key: "mortality",
        },
        {
          title: "Growth",
          key: "growth",
        },
        {
          title: "Leaf Lignin",
          key: "leaf_lignin",
        },
      ],
      speciesSP: [
        {
          name: "Example",
          leaf_longevity: 3,
          woody_decay_rate: 0.1,
          mortality: 10,
          growth: 0.25,
          leaf_lignin: 0.252,
          _disabled: true,
        },
      ],
      speciesParametersVal: {
        name: "",
        leaf_longevity: 0,
        woody_decay_rate: 0,
        mortality: 0,
        growth: 0,
        leaf_lignin: 0,
      },
      ruleValidate3: {
        name: [
          {
            required: true,
            message: "The species name cannot be empty",
            trigger: "blur",
          },
        ],
        leaf_longevity: [
          {
            required: true,
            message: "The leaf longevity cannot be empty",
            trigger: "blur",
          },
        ],
        woody_decay_rate: [
          {
            required: true,
            message: "The woody decay rate cannot be empty",
            trigger: "blur",
          },
        ],
        mortality: [
          {
            required: true,
            message: "The mortality parameter cannot be empty",
            trigger: "blur",
          },
        ],
        growth: [
          {
            required: true,
            message: "The growth parameter cannot be empty",
            trigger: "blur",
          },
        ],
        leaf_lignin: [
          {
            required: true,
            message: "The leaf lignin cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedSP: [],
      addSpeciesParametersModal: false,
      // EcoregionParameters data
      ecoregionParameters: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Ecoregions\\AET",
          key: "ecoregion",
        },
        {
          title: "AET",
          key: "aet",
        },
      ],
      AET: [],
      ecoregionParametersVal: {
        ecoregion: "",
        aet: 0,
      },
      ruleValidate4: {
        ecoregion: [
          {
            required: true,
            message: "The ecoregion name cannot be empty",
            trigger: "blur",
          },
        ],
        aet: [
          {
            required: true,
            message: "The AET cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedEP: [],
      addEcoregionParametersModal: false,
      //
      boolDynamicInputFile: false,
      dynamicInputFile: {},
      // FireReductionParameters data
      fireReductionParams: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Fire Severity",
          key: "severity",
        },
        {
          title: "Wood Reduction",
          key: "wood_reduction",
        },
        {
          title: "Litter Reduction",
          key: "litter_reduction",
        },
      ],
      fireRows: [
        {
          severity: 1,
          wood_reduction: 0.0,
          litter_reduction: 0.0,
        },
        {
          severity: 2,
          wood_reduction: 0.0,
          litter_reduction: 0.0,
        },
        {
          severity: 3,
          wood_reduction: 0.0,
          litter_reduction: 0.0,
        },
        {
          severity: 4,
          wood_reduction: 0.0,
          litter_reduction: 0.0,
        },
        {
          severity: 5,
          wood_reduction: 0.0,
          litter_reduction: 0.0,
        },
      ],
      fireReductionParametersVal: {
        severity: 0,
        wood_reduction: 0.0,
        litter_reduction: 0.0,
      },
      ruleValidate5: {
        wood_reduction: [
          {
            required: true,
            message: "The wood reduction cannot be empty",
            trigger: "blur",
          },
        ],
        litter_reduction: [
          {
            required: true,
            message: "The litter reduction cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedFRP: {},
      editFireReductionParametersModal: false,
      // HarvestReductionParameters data
      harvestReductionParams: [
        {
          type: "selection",
          width: 60,
          align: "center",
        },
        {
          title: "Prescription Name",
          key: "name",
        },
        {
          title: "Dead Wood Reduction",
          key: "wood_reduction",
        },
        {
          title: "Dead Litter Reduction",
          key: "litter_reduction",
        },
        {
          title: "Cohort Wood Removal",
          key: "wood_removal",
        },
        {
          title: "Cohort Leaf Removal",
          key: "leaf_removal",
        },
      ],
      harvestRows: [],
      harvestReductionParametersVal: {
        name: "",
        wood_reduction: 0,
        litter_reduction: 0,
        wood_removal: 0,
        leaf_removal: 0,
      },
      ruleValidate6: {
        name: [
          {
            required: true,
            message: "The prescription name cannot be empty",
            trigger: "blur",
          },
        ],
        wood_reduction: [
          {
            required: true,
            message: "The dead wood reduction cannot be empty",
            trigger: "blur",
          },
        ],
        litter_reduction: [
          {
            required: true,
            message: "The dead litter reduction cannot be empty",
            trigger: "blur",
          },
        ],
        wood_removal: [
          {
            required: true,
            message: "The cohort wood removal cannot be empty",
            trigger: "blur",
          },
        ],
        leaf_removal: [
          {
            required: true,
            message: "The cohort leaf removal cannot be empty",
            trigger: "blur",
          },
        ],
      },
      selectedHRP: [],
      addHarvestReductionParametersModal: false,
      selectData: [],
    };
  },
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
        this.userInfo = userInfo;
        this.toolId = toolId;
        this.participants = onlineMembers;
        this.resources = resources;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
        loadResChannel = this.loadResources;
      } else {
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },
    getSocketOperation(data) {
      // 接受socket指令、进行相应操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "open"){
        //  点击add按钮协同
        if(content.type == "addMRB"){
          this.minRelativeBiomassVal = {
            ecoregionName: "",
            class1: "",
            class2: "",
            class3: "",
            class4: "",
            class5: "",
          };
          this.addMinRelativeBiomassModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is adding a min-relative-biomass '
                ])
              }
          });
        } else if (content.type == "addSP"){
          this.speciesParametersVal = {
            name: "",
            leaf_longevity: 0,
            woody_decay_rate: 0,
            mortality: 0,
            growth: 0,
            leaf_lignin: 0,
          };
          this.addSpeciesParametersModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is adding a species parameters form '
                ])
              }
          });
        } else if (content.type == "addEP"){
          this.ecoregionParametersVal = {
            ecoregion: "",
            aet: 0,
          };
          this.addEcoregionParametersModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is adding a ecoregion parameters form '
                ])
              }
          });
        } else if (content.type == "addHRP"){
          this.harvestReductionParametersVal = {
            name: "",
            wood_reduction: 0,
            litter_reduction: 0,
            wood_removal: 0,
            leaf_removal: 0,
          };
          this.addHarvestReductionParametersModal = true;
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is adding a harvest reduction parameters form '
                ])
              }
          });
        } else if (content.type == "edit"){
          // this.speciesValidate = this.selectSpecies[0];
          // this.editSpeciesModal = true;
          if(content.index == "SL"){
            this.editSL();
          } else if (content.index == "FRP"){
            this.editFRP();
          }
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' is editing a form '
                ])
              }
          });
        }
      } else if (behavior == "message"){
        //编辑信息协同1 add-input  获取和失去焦点
        let index = content.inputNum;
        if (content.inOrOut == "in") {
          document.getElementById('input_' + index).children[0].children[1].style.borderColor = "red";
          // document.getElementById('input__' + index).children[0].children[1].style.borderColor = "red";
        } else {
          document.getElementById('input_' + index).children[0].children[1].style.borderColor = "#4caf50";
          // document.getElementById('input__' + index).children[0].children[1].style.borderColor = "#4caf50";
        }

      } else if (behavior == "params"){
        //编辑信息协同2 add-input  输入参数
        let index = content.stateIndex;
        let index2 = content.stateIndex.split("_")[0];
        if (index == "timeStep"){
          this.timeStep = content.inputs;
        } else if (index == "spinUpMortalityFraction"){
          this.spinUpMortalityFraction = content.inputs;
        } else if (index2 == "MRB") {
          this.minRelativeBiomassVal[index.split("_")[1]] = content.inputs;
        } else if (index2 == "SP") {
          this.speciesParametersVal[index.split("SP_")[1]] = content.inputs;
        } else if (index2 == "EP") {
          this.ecoregionParametersVal[index.split("EP_")[1]] = content.inputs;
        } else if (index2 == "HRP") {
          this.harvestReductionParametersVal[index.split("HRP_")[1]] = content.inputs;
        } else if (index2 == "SL") {
          this.sufficientLightVal[index.split("SL_")[1]] = content.inputs;
        } else if (index2 == "FRP") {
          this.fireReductionParametersVal[index.split("FRP_")[1]] = content.inputs;
        }
        // this.speciesValidate[index] = content.inputs;
      } else if (behavior == "switch") {
        if (content.status) {
          this.calibrateMode = "Yes";
          this.boolCalibrateMode = true;
        } else {
          this.calibrateMode = "No";
          this.boolCalibrateMode = false;
        }
      } else if (behavior == "submit") {
        // add\edit表单提交协同
        let behavior = content.behavior;
        let name = content.name;
        if (content.type == "MRB"){
          this.handleSubmitMRB(behavior,name);
        } else if (content.type == "SP") {
          this.handleSubmitSP(name);
        } else if (content.type == "EP") {
          this.handleSubmitEP(name);
        } else if (content.type == "HRP") {
          this.handleSubmitHRP(name);
        } else if (content.type == "SL") {
          this.handleSubmitSL(name);
        } else if (content.type == "FRP") {
          this.handleSubmitFRP(name);
        }
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has submitted the form '
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
                  ' has reset the form '
                ])
              }
          });
      } else if (behavior == "select") {
        this[content.type] = content.value;
      } else if (behavior == "table-select") {
        // 选择协同 type: selectChange、selectAll、cancelSelect 
        if (content.type == "select-change"){
          let selection = content.data;
          // this.selectSpecies = Object.assign([], selection);
          if(content.index == "MRB"){
            this.selectMRB(selection);
          } else if (content.index == "SP") {
            this.selectSP(selection);
          } else if (content.index == "EP") {
            this.selectEP(selection);
          } else if (content.index == "HRP") {
            this.selectHRP(selection);
          } else if (content.index == "SL") {
            this.selectSL(selection);
          } else if (content.index == "FRP") {
            this.selectFRP(selection);
          }
          // this.select(selection);
          this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has updated the list of selected information '
                ])
              }
          });
        } else if (content.type == "select-all"){
          if(content.index == "MRB"){
            this.handleSelectAllMRB(true);
          } else if (content.index == "SP") {
            this.handleSelectAllSP(selection);
          } else if (content.index == "EP") {
            this.handleSelectAllEP(selection);
          } else if (content.index == "HRP") {
            this.handleSelectAllHRP(selection);
          }
          // this.handleSelectAll(true);
        } else if (content.type == "cancel-select"){
          if(content.index == "MRB"){
            this.handleSelectAllMRB(false);
          } else if (content.index == "SP") {
            this.handleSelectAllSP(selection);
          } else if (content.index == "EP") {
            this.handleSelectAllEP(selection);
          } else if (content.index == "HRP") {
            this.handleSelectAllHRP(selection);
          }
          // this.handleSelectAll(false);
        }
      } else if (behavior == "remove"){
        if (content.index == "MRB"){
          this.removeMRB();
        } else if (content.index == "SP") {
          this.removeSP();
        } else if (content.index == "EP") {
          this.removeEP();
        } else if (content.index == "HRP") {
          this.removeHRP();
        }
        // this.remove();
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has removed the list of information '
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
                  ' has submit the list of selected information '
                ])
              }
          });
      } else if (behavior == "reset-all"){
        this.resetAll();
        this.$Notice.success({
              title: 'Operation notice',
              duration: 10,
              render: h => {
                return h('span', [
                  'The member ',
                  h('a', sender.name),
                  ' has reset all of the information '
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
    getSocketComputation(data) {},
    getClimateConfigFile(data) {},
    getSocketData(data) {
      // socket数据操作
      let behavior = data.behavior;
      let content = JSON.parse(data.content);
      let sender = data.sender;
      if (behavior == "select"){
      } else if(behavior == "file") {
        if(content.type != "" && content.type == "initialCommunities"){
          this.initialCommunities = content.data[0];
          this.initialCommunitiesMap = content.data[1];
          this.boolInitialCommunities = false;
        } else if (content.type != "" && content.type == "climateConfigFile") {
          this.climateConfigFile = content.data[0];
          this.boolClimateConfigFile = false;
        } else if (content.type != "" && content.type == "dynamicInputFile") {
          this.dynamicInputFile = content.data[0];
          this.boolDynamicInputFile = false;
        }
      }
    },
    loadResources(resList) {
      let type = "";
      for (let i = 0; i < resList.length; i++) {
        // your function
        if (this.boolInitialCommunities) {
          this.initialCommunities = resList[0];
          this.initialCommunitiesMap = resList[1];
          type = "initialCommunities";
          this.selectData = [];
          this.selectData.push(resList[0]);
          this.selectData.push(resList[1]);
          this.boolInitialCommunities = false;
        } else if (this.boolClimateConfigFile) {
          this.climateConfigFile = resList[0];
          type = "climateConfigFile";
          this.selectData = [];
          this.selectData.push(resList[0]);
          this.boolClimateConfigFile = false;
        } else if (this.boolDynamicInputFile) {
          this.dynamicInputFile = resList[0];
          type = "dynamicInputFile";
          this.selectData = [];
          this.selectData.push(resList[0]);
          this.boolDynamicInputFile = false;
        }
      }
      //
      let send_content = {
        type: "resource",
        sender: this.userInfo.userId,
        behavior: "file",
        content: {
          type: type,
          data: this.selectData,
        }
      };
      sendCustomOperation(send_content);
    },
    selectInitialCommunities() {
      this.boolInitialCommunities = !this.boolInitialCommunities;
      this.boolClimateConfigFile = false;
      this.boolDynamicInputFile = false;
    },
    selectClimateConfigFile() {
      this.boolClimateConfigFile = !this.boolClimateConfigFile;
      this.boolInitialCommunities = false;
      this.boolDynamicInputFile = false;
    },
    selectDynamicInputFile() {
      this.boolDynamicInputFile = !this.boolDynamicInputFile;
      this.boolInitialCommunities = false;
      this.boolClimateConfigFile = false;
    },
    calibrateModeChange(status) {
      if (status) {
        this.calibrateMode = "Yes";
        this.boolCalibrateMode = true;
      } else {
        this.calibrateMode = "No";
        this.boolCalibrateMode = false;
      }

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "switch",
        content: {
          status: status,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
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
    submit(){
      let aid = this.activityInfo.aid;
      let graphId = this.activityInfo.parent;
      let toolId = this.toolId;
      let participant = this.participants;
      let requiredInput = {};
      let lifeParameter = {};
      let biomassSuccession = {};

      requiredInput.timeStep = this.timeStep;
      requiredInput.seedingAlgorithm = this.seedingAlgorithm;
      requiredInput.initialCommunities = this.initialCommunities.address;
      requiredInput.initialCommunitiesMap = this.initialCommunitiesMap.address;
      requiredInput.climateConfigFile = this.climateConfigFile.address;
      requiredInput.calibrateMode = this.calibrateMode;
      requiredInput.spinUpMortalityFraction = this.spinUpMortalityFraction;
      //
      lifeParameter.minRelativeBiomassVal = this.ecoregionsMRB;
      lifeParameter.sufficientLightVal = this.SEP;
      lifeParameter.speciesParametersVal = [];
      for( let i = 1 ; i< this.speciesSP.length ; i++){
        lifeParameter.speciesParametersVal.push(this.speciesSP[i]);
      }
      lifeParameter.ecoregionParametersVal = this.AET;
      lifeParameter.dynamicInputFile = this.dynamicInputFile.address;
      lifeParameter.fireReductionParametersVal = this.fireRows;
      lifeParameter.harvestReductionParametersVal = this.harvestRows;

      let submitInfo = {};
      biomassSuccession.requiredInput = requiredInput;
      biomassSuccession.lifeParameter = lifeParameter;
      submitInfo.aid = aid;
      submitInfo.graphId = graphId;
      submitInfo.toolId = toolId;
      submitInfo.participant = participant;
      submitInfo.biomassSuccession = biomassSuccession;

      console.log(submitInfo);
      this.axios
        .post("/GeoProblemSolving/landis/biomass",submitInfo)
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
    beforeResetAll(){
      this.resetAll();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "reset-all",
        content: {
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    resetAll(){
      location.reload();
    },

    // MinRelativeBiomass method
    addMRB() {
      this.minRelativeBiomassVal = {
        ecoregionName: "",
        class1: "",
        class2: "",
        class3: "",
        class4: "",
        class5: "",
      };
      this.addMinRelativeBiomassModal = true;

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "addMRB",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    beforeRemoveMRB() {
      this.removeMRB();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "remove",
        content: {
          index: "MRB",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    removeMRB() {
      for (let i = 0; i < this.selectedMRB.length; i++) {
        for (let j = 0; j < this.ecoregionsMRB.length; j++) {
          if (this.selectedMRB[i].name === this.ecoregionsMRB[j].name) {
            this.ecoregionsMRB.splice(j, 1);
            break;
          }
        }
      }
    },
    beforeSelectMRB(selection){
      this.selectMRB(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "MRB",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectMRB(selection) {
      this.selectedMRB = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableMRB");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].ecoregionName){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllMRB(status){
      this.handleSelectAllMRB(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "MRB",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllMRB(status) {
      this.$refs.selection0.selectAll(status);
      if (status) {
        this.selectedMRB = Object.assign([], this.ecoregionsMRB);
      } else {
        this.selectedMRB = [];
      }
    },
    beforeHandleSubmitMRB(behavior,name){
      this.handleSubmitMRB(behavior,name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "MRB",
          behavior: behavior,
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitMRB(behavior, name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(
            _this.ecoregionsMRB,
            _this.minRelativeBiomassVal
          );
          if (behavior === "add") {
            if (row >= 0) {
              this.$Message.error("Duplicate ecoregion information!");
            } else {
              _this.ecoregionsMRB.push(_this.minRelativeBiomassVal);
              _this.addMinRelativeBiomassModal = false;
            }
          }
        } else {
          this.$Message.error(
            "Please provide complete the minimum relative biomass for shade classes 1 - 5."
          );
        }
      });
    },

    // SufficientLight method
    beforeEditSL(){
      this.editSL();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "edit",
          index: "SL",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    editSL() {
      this.sufficientLightVal = this.selectedSL[0];
      this.editSufficientLightModal = true;
    },
    beforeSelectSL(selection){
      this.selectSL(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "SL",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectSL(selection) {
      this.selectedSL = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableSL");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].shadeclass){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllSL(status){
      this.handleSelectAllSL(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "SL",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllSL(status) {
      this.$refs.selection1.selectAll(status);
      if (status) {
        this.selectedSL = Object.assign([], this.SEP);
      } else {
        this.selectedSL = [];
      }
    },
    beforeHandleSubmitSL(name){
      this.handleSubmitSL(name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "SL",
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitSL(name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(_this.SEP, _this.sufficientLightVal);
          if (row >= 0) {
            _this.SEP.splice(row, 1, _this.sufficientLightVal);
            _this.editSufficientLightModal = false;
          } else {
            this.$Message.error("Information error!");
          }
        } else {
          this.$Message.error(
            "Please provide complete the probability of establishment for each species shade tolerance class."
          );
        }
      });
    },

    // SpeciesParameters method
    addSP() {
      this.speciesParametersVal = {
        name: "",
        leaf_longevity: 0,
        woody_decay_rate: 0,
        mortality: 0,
        growth: 0,
        leaf_lignin: 0,
      };
      this.addSpeciesParametersModal = true;
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "addSP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    beforeRemoveSP(){
      this.removeSP();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "remove",
        content: {
          index: "SP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    removeSP() {
      for (let i = 0; i < this.selectedSP.length; i++) {
        for (let j = 0; j < this.speciesSP.length; j++) {
          if (this.selectedSP[i].name === this.speciesSP[j].name) {
            this.speciesSP.splice(j, 1);
            break;
          }
        }
      }
    },
    beforeSelectSP(selection){
      this.selectSP(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "SP",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectSP(selection) {
      this.selectedSP = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableSP");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].name){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllSP(status){
      this.handleSelectAllSP(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "SP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllSP(status) {
      this.$refs.selection2.selectAll(status);
      if (status) {
        this.selectedSP = Object.assign([], this.speciesSP);
      } else {
        this.selectedSP = [];
      }
    },
    beforeHandleSubmitSP(name){
      this.handleSubmitSP(name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "SP",
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitSP(name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(
            _this.speciesSP,
            _this.speciesParametersVal
          );
          if (row >= 0) {
            this.$Message.error("Duplicate species information!");
          } else {
            _this.speciesSP.push(_this.speciesParametersVal);
            _this.addSpeciesParametersModal = false;
          }
        } else {
          this.$Message.error("Please provide complete the form.");
        }
      });
    },

    // EcoregionParameters method
    addEP() {
      this.ecoregionParametersVal = {
        ecoregion: "",
        aet: 0,
      };
      this.addEcoregionParametersModal = true;
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "addEP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    beforeRemoveEP(){
      this.removeEP();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "remove",
        content: {
          index: "EP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    removeEP() {
      for (let i = 0; i < this.selectedEP.length; i++) {
        for (let j = 0; j < this.AET.length; j++) {
          if (this.selectedEP[i].name === this.AET[j].name) {
            this.AET.splice(j, 1);
            break;
          }
        }
      }
    },
    beforeSelectEP(selection){
      this.selectEP(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "EP",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectEP(selection) {
      this.selectedEP = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableEP");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].ecoregion){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllEP(status){
      this.handleSelectAllEP(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "EP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllEP(status) {
      this.$refs.selection3.selectAll(status);
      if (status) {
        this.selectedEP = Object.assign([], this.AET);
      } else {
        this.selectedEP = [];
      }
    },
    beforeHandleSubmitEP(name){
      this.handleSubmitEP(name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "EP",
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitEP(name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(_this.AET, _this.ecoregionParametersVal);
          if (row >= 0) {
            this.$Message.error("Duplicate ecoregion information!");
          } else {
            _this.AET.push(_this.ecoregionParametersVal);
            _this.addEcoregionParametersModal = false;
          }
        } else {
          this.$Message.error("Please provide complete the form.");
        }
      });
    },

    // FireReductionParameters method
    beforeEditFRP(){
      this.editFRP();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "edit",
          index: "FRP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    editFRP() {
      this.fireReductionParametersVal = this.selectedFRP[0];
      this.editFireReductionParametersModal = true
    },
    beforeSelectFRP(selection){
      this.selectFRP(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "FRP",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectFRP(selection) {
      this.selectedFRP = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableFRP");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].severity){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllFRP(status){
      this.handleSelectAllFRP(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "FRP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllFRP(status) {
      this.$refs.selection4.selectAll(status);
      if (status) {
        this.selectedFRP = Object.assign([], this.fireRows);
      } else {
        this.selectedFRP = [];
      }
    },
    beforeHandleSubmitFRP(name){
      this.handleSubmitFRP(name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "FRP",
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitFRP(name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(_this.fireRows, _this.fireReductionParametersVal);
          if (row >= 0) {
            _this.fireRows.splice(row, 1, _this.fireReductionParametersVal);
            _this.editFireReductionParametersModal = false;
          } else {
            this.$Message.error("Information error!");
          }
        } else {
          this.$Message.error(
            "Please provide complete the form."
          );
        }
      });
    },

    // HarvestReductionParameters method
    addHRP() {
      this.harvestReductionParametersVal = {
        name: "",
        wood_reduction: 0,
        litter_reduction: 0,
        wood_removal: 0,
        leaf_removal: 0,
      };
      this.addHarvestReductionParametersModal = true;
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "open",
        content: {
          type: "addHRP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    beforeRemoveHRP(){
      this.removeHRP();
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "remove",
        content: {
          index: "HRP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    removeHRP() {
      for (let i = 0; i < this.selectedHRP.length; i++) {
        for (let j = 0; j < this.harvestRows.length; j++) {
          if (this.selectedHRP[i].name === this.harvestRows[j].name) {
            this.harvestRows.splice(j, 1);
            break;
          }
        }
      }
    },
    beforeSelectHRP(selection){
      this.selectHRP(selection);
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: "select-change",
          index: "HRP",
          data: selection,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    selectHRP(selection) {
      this.selectedHRP = Object.assign([], selection);
      //修改Table的样式
      let table = document.getElementsByClassName("selectionTableHRP");
      let tableRow = table[0].children[0].children[1].children[0].children[1];
      for( let j = 0 ; j < tableRow.children.length ; j++){
        // 先取消选中，在判断是否被选中
        tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox";
        for( let i = 0 ; i < selection.length ; i++){
          if( tableRow.children[j].children[1].children[0].innerText == selection[i].name){
            // 应该被选中
            tableRow.children[j].children[0].children[0].children[0].children[0].className = "ivu-checkbox ivu-checkbox-checked";
          }
        }
      }
    },
    beforeHandleSelectAllHRP(status){
      this.handleSelectAllHRP(status);
      // websocket
      let selectType = status ? "select-all" : "cancle-select"
      let paramsMsg = {
        type: "operation",
        behavior: "table-select",
        content: {
          type: selectType,
          index: "HRP",
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSelectAllHRP(status) {
      this.$refs.selection5.selectAll(status);
      if (status) {
        this.selectedHRP = Object.assign([], this.harvestRows);
      } else {
        this.selectedHRP = [];
      }
    },
    beforeHandleSubmitHRP(name){
      this.handleSubmitHRP(name);

      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "submit",
        content: {
          type: "HRP",
          name: name,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
    handleSubmitHRP(name) {
      let _this = this;
      this.$refs[name].validate((valid) => {
        if (valid) {
          let row = this.arrayContains(_this.harvestRows, _this.harvestReductionParametersVal);
          if (row >= 0) {
            this.$Message.error("Duplicate prescription information!");
          } else {
            _this.harvestRows.push(_this.harvestReductionParametersVal);
            _this.addHarvestReductionParametersModal = false;
          }
        } else {
          this.$Message.error("Please provide complete the form.");
        }
      });
    },

    //common
    beforeHandleReset(name){
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
    afterHandleSubmit(){
      // 清除css样式
      let doms = document.getElementsByClassName('addOrEditInputs');
      for( let i = 0 ; i < doms.length ; i++){
        doms[i].children[0].children[1].style.borderColor = "";
      }
    },
    arrayContains(array, data) {
      var i = array.length;
      while (i--) {
        if (
          (array[i].shadeclass != undefined &&
            array[i].shadeclass === data.shadeclass) ||
          (array[i].ecoregionName != undefined &&
            array[i].ecoregionName === data.ecoregionName) ||
          (array[i].name != undefined && array[i].name === data.name) ||
          (array[i].ecoregion != undefined &&
            array[i].ecoregion === data.ecoregion) ||
          (array[i].severity != undefined &&
            array[i].severity === data.severity)
        ) {
          return i;
        }
      }
      return i;
    },

    sendInputTyping: function (index, inOrOut) {
      sendTypingInfo(index, inOrOut);
    },
    sendInputParams: function (modelInEvent, stateIndex) {
      sendInputParams(modelInEvent, stateIndex);
    },
    seedingAlgorithmChange(value){
      // websocket
      let paramsMsg = {
        type: "operation",
        behavior: "select",
        content: {
          type: "seedingAlgorithm",
          value: value,
        },
        sender: this.userInfo.userId,
      };
      sendCustomOperation(paramsMsg);
    },
  },
};
</script>
<style scoped>
.labelTile {
  font-size: larger;
  font-weight: bold;
  margin: 5px 0;
}
</style>
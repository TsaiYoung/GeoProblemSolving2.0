<template>
  <div class="panel-body" id="concept-panel">
    <vue-scroll :ops="ops" style="height:calc(79vh)">
      <Collapse simple v-for="(map, indexMap) in conceptMap" :key="indexMap">
        <Panel>
          {{ map.name }}
          <div slot="content">
            <Collapse accordion v-model="collapseValue">
              <Panel name="MainMap" v-show="map.pathUrl != ''">
                Main Map
                <div slot="content">
                  <Col>
                    <img
                      :src="
                        'http://' +
                          windowHost +
                          '/GeoProblemSolving' +
                          map.pathUrl
                      "
                      class="pic"
                    />
                  </Col>
                </div>
              </Panel>
              <Panel name="Concepts" v-show="map.concept[0].relateImages != ''">
                Concepts
                <div slot="content">
                  <Row>
                    <Col
                      :span="24"
                      v-for="(item, index) in map.concept[0].relateImages"
                      :key="indexMap + 'Concepts' + index"
                    >
                      <img
                        :src="
                          'http://' +
                            windowHost +
                            '/GeoProblemSolving' +
                            item.pathUrl
                        "
                        class="itemPic"
                      />
                    </Col>
                  </Row>
                </div>
              </Panel>
              <Panel
                name="Positions"
                v-show="map.spacePosition.relateImages != ''"
              >
                Positions
                <div slot="content">
                  <Row>
                    <Col
                      :span="24"
                      v-for="(item, index) in map.spacePosition.relateImages"
                      :key="indexMap + 'Positions' + index"
                    >
                      <img
                        :src="
                          'http://' +
                            windowHost +
                            '/GeoProblemSolving' +
                            item.pathUrl
                        "
                        class="itemPic"
                      />
                    </Col>
                  </Row>
                </div>
              </Panel>
              <Panel
                name="Property"
                v-show="map.properties[0].relateImages != ''"
              >
                Property
                <div slot="content">
                  <Row>
                    <Col
                      :span="24"
                      v-for="(item, index) in map.properties[0].relateImages"
                      :key="indexMap + 'property' + index"
                    >
                      <img
                        :src="
                          'http://' +
                            windowHost +
                            '/GeoProblemSolving' +
                            item.pathUrl
                        "
                        class="itemPic"
                      />
                    </Col>
                  </Row>
                </div>
              </Panel>
              <Panel
                name="Process"
                v-show="map.processes[0].relateImages != ''"
              >
                Process
                <div slot="content">
                  <Row>
                    <Col
                      :span="24"
                      v-for="(item, index) in map.processes[0].relateImages"
                      :key="indexMap + 'process' + index"
                    >
                      <img
                        :src="
                          'http://' +
                            windowHost +
                            '/GeoProblemSolving' +
                            item.pathUrl
                        "
                        class="itemPic"
                      />
                    </Col>
                  </Row>
                </div>
              </Panel>
              <Panel name="Shape" v-show="map.shapeInfo.relateImages != ''">
                Shape
                <div slot="content">
                  <Row>
                    <Col
                      :span="24"
                      v-for="(item, index) in map.shapeInfo.relateImages"
                      :key="indexMap + 'shape' + index"
                    >
                      <img
                        :src="
                          'http://' +
                            windowHost +
                            '/GeoProblemSolving' +
                            item.pathUrl
                        "
                        class="itemPic"
                      />
                    </Col>
                  </Row>
                </div>
              </Panel>
            </Collapse>
          </div>
        </Panel>
      </Collapse>
    </vue-scroll>
  </div>
</template>

<script>
export default {
  props: {
    msgConceptMap: {
      type: Array
    },
    panelHeight: {
      type: Number
    }
  },
  watch: {
    msgConceptMap: {
      handler(val) {
        this.conceptMap = val;
      },
      deep: true
    },
    panelHeight: {
      handler(val) {
        this.height = val;
      },
      deep: true
    }
  },
  computed: {},
  components: {},
  data() {
    return {
      conceptMap: this.msgConceptMap,
      activeNames: [
        "concept",
        "property",
        "shape",
        "position",
        "process",
        "relation"
      ],
      windowHost: window.location.host,
      collapseValue: "MainMap",
      height: this.panelHeight,
      ops: {
        bar: {
          background: "#808695"
        }
      }
    };
  },
  methods: {}
};
</script>

<style lang="scss" scoped>
.panel-body {
  width: 100%;
  margin: 0;
}
.pic {
  max-width: 100%;
  height: auto;
}
.itemPic {
  width: 100%;
  height: auto;
  text-align: center;
}
</style>

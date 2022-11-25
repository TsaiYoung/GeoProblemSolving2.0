<template>
  <div class>
    <h2 class="title">{{conceptMap.name}}</h2>
    <div class="imageContainer">
      <img class="conceptImage" :src="conceptMap.pathUrl" alt="概念图" />
    </div>

    <Collapse simple v-for="(item,index) in activeNames" :key="index">
      <Panel :name="item">
          <div ></div>
      </Panel>
    </Collapse>
  </div>
</template>

<script>
export default {
  props: {

  },
  components: {},
  data() {
    return {
      activeNames: [
        "concept",
        "property",
        "shape",
        "position",
        "process",
        "relation"
      ]
    };
  },
  methods: {},
  created() {},
  mounted() {}
};
</script>
<style lang='scss' scoped>
.title {
  text-align: center;
}
.el-collapse-item__header {
  font-size: medium !important;
  font-weight: bold;
}
span,
p {
  font-size: 16px;
}
#app {
  width: 80%;
  margin-left: 10%;
}
.imageContainer {
  display: flex;
  justify-content: center;
  align-items: center;
}
.conceptImage {
  width: 30%;
  margin: 10px;
  border: 2px solid #bab3b3;
  border-radius: 10px;
}
</style>
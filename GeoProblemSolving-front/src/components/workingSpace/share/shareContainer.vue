<style scoped>
</style>
<template>
  <iframe :src="pageUrl" style="width:100%" :style="{height:contentHeight+'px'}" frameborder="0"></iframe>
</template>
<script>
export default {
  data() {
    return {
      pageUrl: "",
      contentHeight: window.innerHeight - 125,
      userInfo: this.$store.getters.userInfo
    };
  },
  mounted() {
    this.verifyToken();
    window.addEventListener("resize", this.initSize);
  },
  methods: {
    initSize() {
      this.contentHeight = window.innerHeight - 125;
    },
    verifyToken() {
      let tokenStr = this.$route.query.token;
      this.axios
        .get("/GeoProblemSolving/token/checkShareToken?shareToken=" + tokenStr)
        .then(res => {
          if (Object.prototype.toString.call(res.data)=="[object Object]") {
            let groupId = res.data.groupId;
            let resourceId = res.data.resourceId;
            this.getSharedUrl(groupId, resourceId);
          } else {
            this.pageUrl = "";
          }
        })
        .catch(err => {
            throw err;
        });
    },
    getSharedUrl(groupId, resourceId) {
      let userInfo = this.userInfo;
      if (
        userInfo.userId != undefined &&
        userInfo.userId != "" &&
        userInfo.userName != undefined &&
        userInfo.userName != ""
      ) {
        this.pageUrl =
          "/GeoProblemSolving/Collaborative/Mindmap/version/mindmap.html?userName=" +
          userInfo.userName +
          "&userID=" +
          userInfo.userId +
          "&groupID=" +
          groupId +
          "&resourceID=" +
          resourceId;
      } else {
        this.pageUrl =
          "/GeoProblemSolving/Collaborative/Mindmap/share/mindmap.html?userName=&userID=&groupID=" +
          groupId +
          "&resourceID=" +
          resourceId;
      }
    }
  }
};
</script>

<template>
  <div class="avatarList">
    <span class="avatar" v-for="(item,index) in avatarList" :key="index" :title="item.name">
      <img
        v-if="item.avatar != '' && item.avatar != undefined"
        :src="avatarUrl(item.avatar)"
        style="width: 30px; height: 30px; border-radius: 50%;"
      />
      <avatar
        v-else
        :username="item.name"
        :size="30"
        :rounded="true"
      />
    </span>
  </div>
</template>

<script>
  import Avatar from "vue-avatar";

  export default {
    name: "AvatarList",
    props: {
      list: Array
    },
    components: {
      Avatar,
    },
    watch: {
      list: {
        deep: true,
        handler(val, oldVal){
          let newVal = JSON.parse(JSON.stringify(val));
          this.avatarList = newVal;
        }
      }
    },
    data() {
      return {
        avatarList: []
      }
    },
    mounted() {
      this.avatarList = this.list;
      console.log(this.avatarList);
    },
    methods: {
      avatarUrl(url) {
        let avatarUrl = this.$store.state.UserServer + url;
        return avatarUrl;
      },
    },
  }
</script>

<style scoped>
  .avatarList {
    display: flex;
  }
  .avatar {
    margin-right: -12px;
  }

</style>

<!--  -->
<template>
  <div class="cardBody">
    <Card style="margin:2.5%" :padding="4" class="card_hover">
      <div style="display:flex;align-items:center">
        <div class="memberImg">
          <img
            v-if="participant.avatar != '' && participant.avatar!= undefined"
            :src="participant.avatar"
            style="width:55px;height:55px"
          />
          <avatar v-else :username="participant.userName" :size="55" :rounded="false"></avatar>
          <div class="onlinecircle"></div>
        </div>
        <div class="memberDetail">
          <div class="memberName">
            <span>{{participant.userName}}</span>
            <div class="managerIconMask"></div>
          </div>
          <div class="memberOrganization">
            <span>{{participant.organization}}</span>
          </div>
        </div>
      </div>
      <div class="card_hover_btn">
        <Icon
          type="md-chatbubbles"
          class="icon"
          :size="28"
          @click.native="sendToParticipant(participant)"
        />
      </div>
    </Card>
  </div>
</template>

<script>
import Avatar from "vue-avatar";
export default {
  props: {
    item: {
      type: Object
    }
  },
  components: { Avatar },
  data() {
    return {
      participant: this.item
    };
  },
  computed: {},

  watch: {
    //监听step切换
    item: {
      handler(val) {
        this.participant = val;
      },
      deep: true
    }
  },
  methods: {
    sendToParticipant(participant) {
      this.$emit("sendSingle", participant);
    }
  },
  created() {},
  mounted() {}
};
</script>
<style lang='scss' scoped>
//@import url(); 引入公共css类

.memberImg {
  width: 55px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.memberDetail {
  height: 100%;
  width: 100%;
  overflow: hidden;
  padding-left: 10px;

  .memberName {
    height: 25px;
    width: 100%;
    span {
      font-size: 19px;
      font-weight: 600;
    }
    .managerIconMask {
      position: absolute;
      right: 29px;
      top: 5px;
      height: 30px;
      width: 30px;
      background-image: linear-gradient(to right, rgba(255, 0, 0, 0), white);
    }
  }

  .memberOrganization {
    height: 30px;
    width: 100%;
    span {
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 15px;
      max-width: 80%;
      font-style: italic;
      color: gray;
    }
  }
}

.onlinecircle {
  width: 14px;
  height: 14px;
  background-color: rgb(93, 243, 79);
  border: 2px solid rgb(221, 245, 221);
  border-radius: 50%;
  -moz-border-radius: 50%;
  -webkit-border-radius: 50%;
  position: absolute;
  bottom: -2px;
  right: -2px;
}

.card_hover {
  height: 65px;
}
.card_hover_btn {
  display: none;
}

.card_hover:hover .card_hover_btn {
  display: block;
  position: absolute;
  right: 5px;
  top: 20px;
  color: rgb(45, 54, 92);
  //   transition:  0.5s, color 0.5s;
}

.icon:hover {
  cursor: pointer;
  -webkit-transform: translateY(-4px);
  -ms-transform: translateY(-4px);
  transform: translateY(-4px);
  -webkit-transition: all 0.2s ease-out;
  transition: all 0.2s ease-out;
  color: #69ca63;
}
</style>
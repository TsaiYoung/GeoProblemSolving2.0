<template>
  <!-- 应当设置每页10条。且带有创建的按钮 -->
  <div class="community">
    <h1>{{ msg }}</h1>
    <Row class="search_part">
      <!-- <Col span="9" offset="3"> -->
      <Col span="6" offset="3">
        <Input search enter-button placeholder="Enter something..." class="search"/>
      </Col>
      <Col :md="12" :lg="12">
        <ButtonGroup size="large" class="order">
          <Button>Default</Button>
          <Button>Time</Button>
          <Button>Category</Button>
          <Button>Hot</Button>
        </ButtonGroup>
      </Col>
    </Row>
    <Row>
      <Col :md="18" :lg="18" offset="3">
        <div class="topic_list" v-for="(item,index) in topics" :key="item.index">
          <div class="topic_title">
            <Card>
              <p slot="title" @click="navigateTo(item.title)">{{item.title}}</p>
              <div style="height:auto">
                <p>{{item.cotent}}</p>
              </div>
              <div class="footer" style="display:flex">
                <!-- <div class="belong"></div> -->
                <div class="tags">
                  <span>
                    <Tag
                      color="primary"
                      v-for="(tag,idx) in item.tags"
                      :key="idx"
                      class="tag"
                    >{{tag}}</Tag>
                  </span>
                </div>
                <div class="detail" style="display:flex">
                  <!-- <Icon type="ios-happy" /><span>{{item.comment}}</span> -->
                  <!-- <span @click="test" class="comment" :data-index="index"> -->
                  <span @click="Support(index)" class="support" :key="item.index">
                    <Icon type="md-thumbs-up" color="green" class="up"/>
                    {{item.support}}
                  </span>
                  <span @click="noSupport(index)" class="nosupport" :key="item.index">
                    <Icon type="md-thumbs-down" color="red"/>
                    {{item.noSupport}}
                  </span>
                  <span class="comment" @click="modal1=true">
                    <Icon type="md-chatboxes"/>
                    {{item.comment}}
                  </span>
                  <Modal
                    v-model="modal1"
                    title="Leave your comment about this topic"
                  >
                    <Input
                      v-model="commentContent"
                      type="textarea"
                      :rows="4"
                      placeholder="please input your comment"
                    />
                  </Modal>
                  <span class="time">
                    <Icon type="md-clock"/>
                    {{item.time}}
                  </span>
                </div>
                <!-- Modal regison -->
              </div>
            </Card>
          </div>
        </div>
      </Col>
      <Col :md="1" :lg="1" offset="1">
        <div style="height:60px">
          <Button
            class="createTopic"
            icon="md-add"
            type="success"
            shape="circle"
            title="create a new topic"
            size="large"
          ></Button>
        </div>
      </Col>
    </Row>
  </div>
</template>
<script>
export default {
  name: "Community",
  methods: {
    Support(index) {
      if (!this.liked) this.topics[index].support++;
      else this.topics[index].support--;
      this.liked = !this.liked;

    },

    noSupport(index) {
      if (!this.liked) this.topics[index].noSupport++;
      //此时将此数据传递到后台进行更新
      else this.topics[index].noSupport--;
      //此时将此数据传递到后台进行更新
      this.liked = !this.liked;

    },
    submit() {
      this.$Message.info("reply success");
    },
    cancel() {
      this.$Message.info("reply error");
    },
    navigateTo(parameter) {
      this.$router.push({ path: `community/${parameter}` });
    }
  },
  mounted() {
    // this.$Notice.info({
    //   title: "Notification title",
    //   desc:
    //     "Here is the notification description. Here is the notification description. "
    // });
  },
  data() {
    return {
      msg: "Welcome to Community",
      //初始设定未点赞
      liked: false,
      commentContent: "",
      //评论的模态框
      modal1: false,
      topics: [
        {
          title: "US abuses legal procedure to stifle Huawei",
          cotent:
            "Samsung and Apple no longer made up 50 percent of the world’s active smartphones in September, compared with 52.3 percent in September of 2017 and 56.5 percent in 2016, according to research firm Newzoo.",
          tags: ["water", "pollutions"],
          time: "2018-10-11",
          comment: 12,
          support: 4,
          noSupport: 2
        },
        {
          title:
            "Commemoration ceremony held for late Nanjing Massacre survivors",
          cotent:
            "It is clear that Washington is maliciously finding fault with Huawei and trying to put the company in jeopardy with US laws. Washington is attempting to damage Huawei's international reputation and taking aim at the tech giant's global market in the name of law.",
          tags: ["water", "pollution", "sea"],
          time: "2018-10-11",
          comment: 1,
          support: 3,
          noSupport: 1
        },
        {
          title: "13th Global Confucius Institute Conference opens in SW China",
          cotent:
            "Zhao Min, daughter of the late Nanjing Massacre survivor Zhao Jinhua, mourns her mother's passing at the Memorial Hall of the Victims in Nanjing Massacre by Japanese Invaders in Nanjing, capital of east China's Jiangsu Province, Dec. 6, 2018.",
          tags: ["water", "sea"],
          time: "2018-10-11",
          comment: 4,
          support: 8,
          noSupport: 5
        },
        {
          title: "Brexit: legal advice stiffens opposition",
          cotent:
            "The event welcomed over 1,500 delegates from 154 countries and regions, including university presidents, scholars, Confucius Institute directors and education personnel.",
          tags: ["pollution", "sea"],
          time: "2018-10-11",
          comment: 2,
          support: 5,
          noSupport: 0
        },
        {
          title:
            "Firms welcome China-US trade consensus, but negotiations pose uncertainties",
          cotent:
            "Prime Minister Theresa May's Brexit deal came under fire from allies and opponents.Prime Minister Theresa May's Brexit deal came under fire from allies and opponents.",
          tags: ["water"],
          time: "2018-10-11",
          comment: 10,
          support: 7,
          noSupport: 4
        },
        {
          title:
            "Theater-goers in the spotlight, even though they cannot see it",
          cotent:
            "Seeing-eye dogs were there for a concert featuring the Shanghai Disabled People’s Choir as it celebrated its 10th anniversary. In what was probably a first, seeing-eye dogs were allowed in with their blind owners.",
          tags: ["pollution"],
          time: "2018-10-11",
          comment: 8,
          support: 12,
          noSupport: 3
        },
        {
          title:
            "Chinese elementary school cancels math class for first and second graders",
          cotent:
            "Seeing-eye dogs were there for a concert featuring the Shanghai Disabled People’s Choir as it celebrated its 10th anniversary. In what was probably a first, seeing-eye dogs were allowed in with their",
          tags: ["sea"],
          time: "2018-10-11",
          comment: 13,
          support: 9,
          noSupport: 1
        },
        {
          title: "Changes in countryside owing to China's reform, opening-up",
          cotent:
            "When most parents and schools try cramming arithmetic and algebra into their children's brains as early as possible, an elementary school in central China's Hubei Province, however, made an unconventional decision to cancel math lessons for its first and second graders.",
          tags: ["water", "pollution", "sea"],
          time: "2018-10-11",
          comment: 15,
          support: 5,
          noSupport: 2
        }
      ]
    };
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1 {
  font-weight: normal;
  text-align: center;
}
.search_part {
  margin-top: 2.5%;
}
.topic_search {
  width: 80%;
  margin-left: 10%;
  margin-right: 10%;
}
.search {
  width: 100%;
}
.order {
  float: right;
}
.topic_list {
  margin-top: 1%;
}
.tags {
  width: 60%;
  /* background-color: lightgreen; */
}
.tag {
  margin-left: 2%;
  margin-right: 2%;
}
.detail {
  width: 40%;
  float: right;
  /* background-color: lightblue; */
}
.time {
  width: 40%;
  margin-left: 1%;
  margin-right: 1%;
}
.comment {
  width: 20%;
  margin-left: 1%;
  margin-right: 1%;
}
.support,
.nosupport {
  width: 20%;
  margin-left: 1%;
  margin-right: 1%;
  transition: 0.75s;
}
.create_topic {
  height: 60px;
  float: right;
}
.support:hover,
.nosupport:hover {
  width: 20%;
  margin-left: 1%;
  margin-right: 1%;
  zoom: 1;
  transform: scale(1.2);
  transition: 0.75s;
  /* font-size: 16px;
  transition:  0.5s; */
}
.createTopic {
  transition: 0.75s;
}
.createTopic:hover {
  /* width: 60px;
    height: 60px;
    transition:  0.5s;
     padding: 0;
    font-size: 32px;
    border-radius: 50%; */
  zoom: 1;
  transform: scale(1.5);
  transition: 0.75s;
}
/* icon :hover{
  color:black;
  size:20;
} */
</style>

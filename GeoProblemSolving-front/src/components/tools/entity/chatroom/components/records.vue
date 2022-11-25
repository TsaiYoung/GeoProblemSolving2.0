<!-- records -->
<template>
  <div class="record-contain">
    <!-- <Row style="margin-left: 20px;">
      <Button type="info" ghost @click="getAll" class="btn">All</Button>
      <Button type="info" ghost @click="getPictures" class="btn"
        >Pictures</Button
      >
      <Button type="info" ghost @click="getTools" class="btn">Tools</Button>
    </Row> -->

    <Row>
      <DatePicker
        type="date"
        placeholder="Select date"
        class="date_pick"
        format="yyyy-MM-dd"
        :value="query_date"
        @on-change="queryTimeLike"
      ></DatePicker>
    </Row>
    <!-- 搜索框 -->
    <Row class="search_msg">
      <Input
        search
        placeholder="Enter something..."
        enter-button
        @on-search="queryContentLike"
        class="searchKey"
      />
    </Row>
    <!-- 聊天记录列表 -->
    <Row class="record_list" v-show="queryType == 'all'">
      <vue-scroll :ops="ops" style="height: calc(100vh - 290px)">
        <div
          class="box"
          v-infinite-scroll="load"
          infinite-scroll-disabled="disabled"
        >
          <div
            v-for="(list, index) in currentPageData"
            :key="index"
            class="record"
          >
            <Row style="margin-bottom:2.5px">
              <Row>
                <div class="message_from">
                  <div class="src" v-if="list.sender.userId == userId">
                    {{ list.sender.name }}
                  </div>
                  <div class="target" v-else>{{ list.sender.name }}</div>
                </div>
                <div class="create_time">{{ list.time }}</div>
              </Row>
              <Row>
                <template v-if="list.type === 'message_pic'" class="picOuter">
                  <img :src="list.content" class="send_pic" />
                </template>
                <template v-else-if="list.type === 'message_tool'">
                  <simple-public-card
                    :toolFrom="JSON.parse(list.content)"
                    :isOpenTool="isOpenTool"
                    class="toolOuter"
                  ></simple-public-card>
                </template>
                <template v-else class="content">{{ list.content }}</template>
              </Row>
            </Row>
          </div>
          <p v-if="loading" class="loading_icon">
            <Icon class="el-icon-loading" :size="30" />
          </p>
          <p v-if="noMore" class="no_records">No More Reocrds</p>
        </div>
      </vue-scroll>
    </Row>
    <Row class="record_list" v-show="queryType != 'all'">
      <vue-scroll :ops="ops" style="height: calc(100vh - 290px)">
        <div v-for="(list, index) in msg_guodu" :key="index" class="record">
          <Row style="margin-bottom:2.5px">
            <Row>
              <div class="message_from">
                <div class="src" v-if="list.sender.userId == userId">
                  {{ list.sender.name }}
                </div>
                <div class="target" v-else>{{ list.sender.name }}</div>
              </div>
              <div class="create_time">{{ list.time }}</div>
            </Row>
            <Row>
              <template v-if="list.type === 'message_pic'" class="picOuter">
                <img :src="list.content" class="send_pic" />
              </template>
              <template v-else-if="list.type === 'message_tool'">
                <simple-public-card
                  :toolFrom="JSON.parse(list.content)"
                  :isOpenTool="isOpenTool"
                  class="toolOuter"
                ></simple-public-card>
              </template>
              <template v-else class="content">{{ list.content }}</template>
            </Row>
          </Row>
        </div>
        <p v-if="loading" class="loading_icon">
          <Icon class="el-icon-loading" :size="30" />
        </p>
        <p v-if="noMore" class="no_records">No More Reocrds</p>
      </vue-scroll>
    </Row>
    <!-- 分页 -->
    <!-- <Row class="record_pages">
      <Page
        :current="currentPage"
        :total="totalMsg"
        :page-size="pageSize"
        @on-change="changeRecordPage"
        simple
        ref="page"
      />
    </Row>-->
  </div>
</template>

<script>
import { get, del, post, put } from "@/axios";
import simplePublicCard from "@/components/common/card/simplePublicToolsCard";
export default {
  props: {
    groupId: {
      type: String
    },
    userId: {
      type: String
    }
  },
  components: { simplePublicCard },

  data() {
    return {
      query_date: "",
      // 聊天记录
      msgRecords: [],
      totalMsg: 0, //数据总条数
      totalPages: 1,
      currentPage: 1, //当前页数 ，默认为1
      pageSize: 15, // 每页显示数量
      currentPageData: [], //当前页显示内容
      msg_guodu: [],
      ops: {
        vueScroll: {},
        scrollPanel: {},
        rail: {
          opacity: "0.1",
          border: "1px solid #f2f2f2",
          size: "6px"
        },
        bar: {
          size: "6px",
          background: "#999",
          keepShow: true
        }
      },
      loading: false,
      //日期选择器选择的index
      msgR_datepicker: [], //日期选择器的第一条记录
      msgindex: 0,
      msgR_prev: [],
      msgR_next: [],

      //height
      panelHeight: 0,
      queryType: "all",

      isOpenTool: true,
      // msgType: "All"
    };
  },
  computed: {
    noMore() {
      return this.currentPage >= this.totalPages - 1;
    },
    disabled() {
      return this.loading || this.noMore;
    }
  },
  watch: {
    // msgType: {
    //   handler(val) {
    //     this.queryType = "all";
    //     this.currentPage = 1;
    //     this.currentPageData = [];
    //     if (val == "All") {
    //       this.loadMessageRecords();
    //     } else {
    //       this.loadMessageRecordsByType();
    //     }
    //   },
    //   deep: true
    // }
  },
  created() {
    this.getTotalCount();
    this.loadMessageRecords();
  },
  mounted() {
    this.initSize();
  },
  methods: {
    // getAll() {
    //   this.msgType = "All";
    // },

    // getPictures() {
    //   this.msgType = "message_pic";
    // },

    // getTools() {
    //   this.msgType = "message_tool";
    // },

    //查找total currentPage
    async getTotalCount() {
      let data = await get(`/GeoProblemSolving/chatmessage/totalCount`);
      if (data != 0) {
        this.totalMsg = data;
        this.totalPages = Math.ceil(this.totalMsg / this.pageSize);
      } else {
        this.totalMsg = data;
        // this.$Message.warning("There is no record here!");
      }
    },
    //inifte scroll
    load() {
      this.loading = true;
      setTimeout(() => {
        this.currentPage += 1; //页数+1
        if (this.queryType == "all") {
          this.loadMessageRecords(); //调用接口，此时页数+1，查询下一页数据
        } else if (this.queryType == "date") {
          this.queryContentLike();
        } else if (this.queryType == "content") {
          this.queryTimeLike();
        }

        this.loading = false;
      }, 2000);
    },

    //按页码查询
    async loadMessageRecords() {
      this.queryType = "all";
      let data = await get(
        `/GeoProblemSolving/chatmessage/inquiryByPage/${this.groupId}/${this.currentPage}/${this.pageSize}`
      );
      this.currentPageData = [];
      for(let i=0; i < data.length; i++){
        let message = data[i];
        let date = new Date(message.time);
        message.time = date.Format("yyyy-MM-dd HH:mm:ss");
        this.currentPageData.push(message);
      }
    },

    // async loadMessageRecordsByType() {
    //   let data = await get(
    //     `/GeoProblemSolving/chatmessage/inquiryByType/${this.groupId}/${this.msgType}`
    //   );
    //   this.currentPageData.push(...data);
    // },

    //聊天记录分页,没有按日期查找，倒序排列
    // changeRecordPage(index) {
    //   this.currentPage = index;
    //   this.loadMessageRecords();
    // },
    async queryContentLike(val) {
      this.queryType = "content";
      this.msg_guodu = [];
      this.currentPageData = [];
      let data = await get(
        `/GeoProblemSolving/chatmessage/contentLike/${this.groupId}/${val}/${this.currentPage}/${this.pageSize}`
      );
      this.msg_guodu.push(...data);
    },

    //日期选择器
    async queryTimeLike(seledate) {
      this.msg_guodu = [];
      if (seledate == "") {
        this.queryType = "all";
        this.currentPage = 1;
        this.loadMessageRecords();
      } else {
        this.queryType = "date";
        this.currentPageData = [];
        let data = await get(
          `/GeoProblemSolving/chatmessage/timeLike/${this.groupId}/${seledate}/${this.currentPage}/${this.pageSize}`
        );
        this.msg_guodu.push(...data);
      }
    },

    timeClear() {},

    initSize() {
      this.panelHeight = window.innerHeight;
      // console.log(this.panelHeight);
    }
  }
};
</script>
<style lang="scss" scoped>
.record-contain {
  width: 100%;
}
.date_pick {
  padding: 10px;
  width: 60%;
  margin-left: 2.5%;
  margin-right: 2.5%;
}

.date_pick_btn {
  width: 20%;
  margin-left: 5%;
  margin-right: 5%;
  height: 32px;
  margin-top: 10px;
}

.search_msg {
  padding: 5px;
  /* background-color: lightblue; */
  width: 90%;
  z-index: 0;
  // margin-left: 2.5%;
}

.record_board {
  padding: 0 15px;
}

.record {
  padding: 5px 25px;
}
// col left
.message_from {
  float: left;
  width: 150px;
  font-size:13px
}
//col right
.create_time {
  color: lightgray;
  float: left;
  width: 150px;
  font-size: 13px;
}

.src {
  color: #37881f;
}
.target {
  color: #2d8cf0;
  float: left;
  width: 300px;
  font-size: 13px;
}

.content {
  color: gray;
  font-size: 13px;
  word-wrap: break-word;
  width: 100%;
}
.box {
  width: 100%;
  height: 100%;
  position: absolute;
  overflow-y: auto;
}
.loading_icon {
  text-align: center;
  margin: 10px 0;
}
.no_records {
  margin-top: 10px;
  font-size: 18px;
  color: #ccc;
  text-align: center;
  font-style: italic;
  font-weight: 600;
  margin: 10px 0;
}
.searchKey {
  width: 220px;
  margin: 0 10px;
}
.picOuter {
  max-height: 300px;
  max-width: 400px;
}
.send_pic {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 100%;
}
.toolOuter {
  width: 120px;
}
.btn {
  line-height: 15px;
}
</style>

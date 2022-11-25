<template>
  <div>
    <Row>
      <Form ref="postForm" :model="postForm" label-position="left">
        <FormItem :label-width="70" label="name:">
          <Input v-model="postForm.name"></Input>
        </FormItem>

        <FormItem :label-width="70" label="Des:">
          <Input
            v-model="postForm.des"
            :rows="1"
            type="textarea"
            autosize
            placeholder="Please enter the des"
          />
        </FormItem>

        <!-- <FormItem :label-width="70" label="Process Id:">
        <Input v-model="postForm.pid" :disabled="pidDisabled" placeholder="Please enter the pid" />
        </FormItem>-->

        <!-- <FormItem :label-width="70" label="activityEnum:">
        <Select v-model="postForm.activityEnum" placeholder="请选择" disabled>
          <Option
            v-for="item in activityEnums"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></Option>
        </Select>
        </FormItem>-->

        <!-- <FormItem :label-width="70" label="categoryEnum:">
        <Select v-model="postForm.categoryEnum" placeholder="请选择">
          <Option
            v-for="item in categoryEnums"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></Option>
        </Select>
        </FormItem>-->

        <FormItem :label-width="70" label="topicList:">
          <Select v-model="postForm.topicList" multiple placeholder="请选择">
            <Option
              v-for="item in topicList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></Option>
          </Select>
        </FormItem>

        <!-- <el-form-item style="margin-bottom: 40px;" label-width="70px">
          <Tinymce ref="editor" v-model="postForm.content" :height="400" />
        </el-form-item>-->
      </Form>
    </Row>
    <Row>
        <Col span="2" offset="20">
      <Button @click="submitForm"  type="primary" >POST</Button>
    </Col>
    </Row>
  </div>
</template>

<script>
export default {
  //   components: { Tinymce,tinymceVue },
  data() {
    return {
      pidDisabled: false,
      activityEnums: [
        {
          value: "SIMULATION",
          label: "Simulation"
        },
        {
          value: "DATAPROCESS",
          label: "DataProcess"
        },
        {
          value: "VISUALIZATION",
          label: "Visualization"
        },
        {
          value: "DATAEXPLORE",
          label: "DataExplore"
        }
      ],
      categoryEnums: [
        {
          value: "PRACTICE",
          label: "Practice"
        },
        {
          value: "TUTORIAL",
          label: "Tutorial"
        },
        {
          value: "OTHER",
          label: "Other"
        }
      ],
      topicList: [
        {
          value: "Geo Problem",
          label: "Geo Problem"
        },
        {
          value: "General",
          label: "General"
        },
        {
          value: "GIS & RS",
          label: "GIS & RS"
        },
        {
          value: "Human-Activity",
          label: "Human-Activity"
        },
        {
          value: "Geological System",
          label: "Geological System"
        },
        {
          value: "Ecological System",
          label: "Ecological System"
        },
        {
          value: "Climate System",
          label: "Climate System"
        },
        {
          value: "Marine System",
          label: "Marine System"
        },
        {
          value: "Coastal System",
          label: "Coastal System"
        },

        {
          value: "Terrestrial System",
          label: "Terrestrial System"
        }
      ],
      postForm: {
        id: undefined,
        name: "",
        des: "",
        topicList: [],
        content: "",

        type: "activity",
        categoryEnum: "PRACTICE",

        memberName: "zhuzhiyi",
        activityEnum: "SIMULATION",
        pid: this.instanceId
      },
    };
  },
  methods: {
    async submitForm() {
      if (this.postForm.name == "") {
        this.$Notice.success({
          title: "Error",
          desc: "Please enter the name !"
        });
      } else if (this.postForm.des == "") {
        this.$Notice.success({
          title: "Error",
          desc: "Please enter the description !"
        });
      } else {
        //form表单都填写完成之后
        let data = await this.axios.post(
          "/GeoProblemSolving/modelItem/activity",
          this.postForm
        );
        if (data.status == 200) {
          this.$Notice.success({
            title: "Success",
            desc: "Post the model successfully "
          });
        } else {
          this.$Notice.success({
            title: "Fail",
            desc: "Post the model failed "
          });
        }
        console.log(data);
        //   this.openModal = false;
        let modalOpen = false;
        this.$emit("changeModalState", modalOpen);
      }
    }
  },

  created() {
    if (this.postForm.pid != "") {
      this.pidDisabled = true;
    }
    // this.postForm.memberName = this.$store.userInfo.username;
    this.postForm.memberName = "zhuzhiyi";
    // this.postForm.activityEnum = this.$route.query.activityEnum;
  },

  props: {
    instanceId: {
      type: String
    },
    openModal: {
      type: Boolean
    }
  },
  watch: {
    instanceId(val) {
      this.postForm.pid = val;
      // console.log(val);
    }
  }
};
</script>

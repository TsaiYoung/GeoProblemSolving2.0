<style scoped>
img {
  width: 100%;
  height: auto;
}
.helpTree{
  margin-top: 30px;
  margin-right: 10px;
  background-color: #f7f7f7;
}
.helpTitle{
  position: relative;
  height: 120px;
}
.bg{
  background-image: url("../../assets/images/helpTitle.png");
  position: absolute;
  width: 101%;
  height: 120%;
  top: -10px;
  left: -18px;
}
.content{
  top:50%; 
  left:50%;
  margin-top:-35px;
  margin-left:-140px;
  position: absolute;
}
</style>
<template>
  <div>
    <Card dis-hover class="helpTitle">
      <div class="bg"></div>
      <div class="content">
        <h1 style="color:white;">Learn and Support</h1>
        <h2 style="color:white; text-align: center; margin-left:-140px;">Here can help you quickly get started with the platform!</h2>
      </div>
    </Card>
    <Row>
        <Col span="5" offset="0">
          <Affix :offset-top="50">
              <Card dis-hover class="helpTree">
                <div style="font-size: 20px; font-weight:700; margin-left:30px; color:#2d8cf0">
                  <a href="javascript:window.scrollTo(0,0)">
                    Help Contents
                  </a>
                </div>
                <template>
                  <Tree :data="helpTree" :render="renderStyle" style="margin-left: 15px; margin-top:20px;"></Tree>
                </template>
              </Card>
          </Affix>
          <BackTop title="Back to top"></BackTop>
        </Col>
        <Col span="19" offset="0">
          <Card v-for="(img,index) in imageList" :key="img.index" dis-hover :id="newTitleList[index]"  style="margin-top:30px;">
            <h2 slot="title">{{newTitleList[index]}}</h2>
            <div>
              <img :src="img">
            </div>
          </Card>
        </Col>
      </Row>
  </div>
</template>
<script>
export default {
  created() {
    this.getHelpImg();
  },
  data() {
    return {
      imageList: [],
      titleList: [
        "Home page",
        "Projects page",
        "Project information",
        "Personal page",
        "Personal projects",
        "Notifications center",
        "Project detail page",
        "Project resources",
        "Project subprojects",
        "Subproject detail page",
        "Subproject tasks"
      ],
      newTitleList: [
        "Home page",
        "Projects page",
        "Project information",
        "Project detail page - Introduction",
        "Project detail page - Task",
        "Project detail page - Task",
        "Project detail page - Resource",
        "Workspace page - Type choose",
        "Workspace page - Multi activity",
        "Multi activity - Introduction",
        "Multi activity - Task",
        "Multi activity - Pathway",
        "Multi activity - Resource",
        "Workspace page - Single activity",
        "Single activity - Work panel",
        "Single activity - Work panel",
        "Single activity - Work panel",
        "Single activity - Work panel",
        "Single activity - Task",
        "Personal page",
        "Personal page - Project",
        "Personal page - Resource",
        "Personal page - Tool",
        "Personal page - Todo list",
      ],
      helpTree: [
        {
          title: 'Home page',
          value: 'Home page',
          expand: false,
        },
        {
          title: 'Projects page',
          value: 'Projects page',
          expand: false,
          children: [
            {
              title: 'Project information',
              value: 'Project information',
              expand: false,
            },
            {
              title: 'Project detail page',
              value: 'Project detail page - Introduction',
              expand: false,
              children: [
                {
                  title: 'Introduction',
                  value: 'Project detail page - Introduction',
                },
                {
                  title: 'Task',
                  value: 'Project detail page - Task',
                },
                {
                  title: 'Resource',
                  value: 'Project detail page - Resource',
                }
              ]
            }
          ]
        },
        {
          title: 'Workspace page',
          value: 'Workspace page - Type choose',
          expand: false,
          children: [
            {
              title: 'Type choose',
              value: 'Workspace page - Type choose',
              expand: false,
            },
            {
              title: 'Multi activity',
              value: 'Workspace page - Multi activity',
              expand: false,
              children: [
                {
                  title: 'Introduction',
                  value: 'Multi activity - Introduction',
                },
                {
                  title: 'Task',
                  value: 'Multi activity - Task',
                },
                {
                  title: 'Pathway',
                  value: 'Multi activity - Pathway',
                },
                {
                  title: 'Resource',
                  value: 'Multi activity - Resource',
                }
              ]
            },
            {
              title: 'Single activity',
              value: 'Workspace page - Single activity',
              expand: false,
              children: [
                {
                  title: 'Work panel',
                  value: 'Single activity - Work panel',
                },
                {
                  title: 'Task',
                  value: 'Single activity - Task',
                }
              ]
            }
          ]
        },
        {
          title: 'Personal page',
          value: 'Personal page',
          expand: false,
          children: [
            {
              title: 'Project',
              value: 'Personal page - Project',
              expand: false,
            },
            {
              title: 'Resource',
              value: 'Personal page - Resource',
              expand: false,
            },
            {
              title: 'Tool',
              value: 'Personal page - Tool',
              expand: false,
            },
            {
              title: 'Todo list',
              value: 'Personal page - Todo list',
              expand: false,
            }
          ]
        },
      ],
    };
  },
  methods: {
    getHelpImg() {
      for (var i = 1; i < 25; i++) {
        var imgUrl = require("@/assets/images/Help/" + "help" + i + ".png");
        this.imageList.push(imgUrl);
      }
    },

    //设置限制内容
    renderStyle(h, { root, node, data }) {
      let props = {};
      let style = {};
      let on = {};
      let value = data.value;
      let name = data.title;

      on = {
        click: () => {
          //跳转到相应内容
          if(data.expand != null && data.expand != undefined){
            data.expand = !data.expand;
          }
          document.getElementById(value).scrollIntoView({behavior: "smooth", block: "end", inline: "nearest"});
        },
      };
      style = {
        width: "100%",
        border: "transparent",
        cursor: "pointer",
        fontSize: "15px",
      };

      return h(
        "span",
        {
          style: {
            display: "inline-block",
            width: "100%",
          },
        },
        [
          h("span", [
            h(
              "span",
              {
                props: props,
                style: style,
                on: on,
                attrs: { title: name },
              },
              [
                h(
                  "span",
                  {
                    style: {
                      overflow: "hidden",
                      maxWidth: "120px",
                      textOverflow: "ellipsis",
                      margin: "0 10px",
                    },
                  },
                  name
                ),
              ]
            ),
          ]),
        ]
      );
    },
  }
};
</script>

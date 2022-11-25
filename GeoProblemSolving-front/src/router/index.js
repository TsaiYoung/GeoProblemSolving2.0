import Vue from "vue";
import Router from "vue-router";

const originalPush = Router.prototype.push
Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
Vue.use(Router);

const routes = [
  {
    path: "/",
    name: "Navigation",
    component: resolve => require(["@/components/navigation"], resolve),
    children: [
      { path: "", redirect: "staticPage" },
      {
        path: "staticPage",
        name: "StaticPage",
        component: resolve =>
          require(["@/components/navigationContent/staticPage"], resolve)
      },
      //add level
      {
        path: "permission/:level/:id",
        name: "permission",
        component: resolve =>
          require(["@/components/workingSpace/permissionManager"], resolve)
      },
      {
        path: "newPersonalPage",
        name: "newPersonalPage",
        component: resolve =>
          require(["@/components/user/userPage/newPersonalPage"], resolve),
        children: [
          {
            path: "overView",
            name: "overView",
            component: resolve => require(["@/components/user/subPage/overView"], resolve),
          },
          //新的resource 页面，含文件夹结构
          {
            path: "resourceList",
            name: "resourceList",
            component: resolve => require(["@/components/user/subPage/resourceList"], resolve)
          },
          {
            path: "notification",
            name: "notification",
            component: resolve => require(["@/components/user/subPage/notification"], resolve)
          },
          {
            path: "tool",
            name: "tool",
            component: resolve => require(["@/components/user/subPage/tool"], resolve)
          },
          {
            path: "project",
            name: "project",
            component: resolve => require(["@/components/user/subPage/project"], resolve)
          },
          {
            path: "todoList",
            name: "todoList",
            component: resolve => require(["@/components/user/subPage/todoList"], resolve)
          },
          {
            path: "userInfo",
            name: "userInfo",
            component: resolve => require(["@/components/user/subPage/userInfo"], resolve)
          }

        ]
      },
      {
        path: "newProject",
        name: "NewProject",
        component: resolve =>
          require(["@/components/projects/newProject"], resolve)
      },
      {
        path: "workflow/:projectId",
        name: "Workflow",
        component: resolve =>
          require(["@/components/projects/workflow"], resolve)
      },
      {
        path: "community",
        name: "Community",
        component: resolve =>
          require(["@/components/community/community"], resolve)
      },
      {
        path: "community/:id",
        name: "Communityreply",
        component: resolve =>
          require(["@/components/community/communityReply"], resolve)
      },
      {
        path: "help",
        name: "Help",
        component: resolve =>
          require(["@/components/navigationContent/help"], resolve)
      },
      // {
      //   path: "personalPage",
      //   name: "PersonalPage",
      //   component: resolve =>
      //     require(["@/components/user/userPage/personalPage"], resolve)
      // },
      // {
      //   path: "notifications",
      //   name: "Notifications",
      //   component: resolve =>
      //     require(["@/components/user/userState/notifications"], resolve)
      // },
      {
        path: "memberPage/:id",
        name: "MemberDetailPage",
        component: resolve =>
          require(["@/components/user/userPage/memberDetailPage"], resolve)
      },
      // { path: 'resourceList', name: 'resourceList', component: resolve => (require(["@/components/resources/resourceList"], resolve)) },
      {
        path: "publicResource",
        name: "PublicResource",
        component: resolve =>
          require(["@/components/resources/publicResourceList"], resolve)
      },
      {
        path: "join/:id/:email",
        name: "joinProject",
        component: resolve =>
          require(["@/components/projects/joinNewProject"], resolve)
      },
      {
        path: "login",
        name: "Login",
        component: resolve =>
          require(["@/components/user/userState/login"], resolve)
      },
      {
        path: "register",
        name: "Register",
        component: resolve =>
          require(["@/components/user/userState/register"], resolve)
      },
      {
        path: "resetPassword/:email",
        name: "resetPassword",
        component: resolve =>
          require(["@/components/user/userState/resetPwd"], resolve)
      },
      {
        path: "resourceCenter",
        name: "resourceCenter",
        component: resolve =>
          require(["@/components/resources/resourceCenter"], resolve)
      },
      {
        path: "toolsCenter",
        name: "toolsCenter",
        component: resolve =>
          require(["@/components/tools/toolsCenter"], resolve)
      },
      {
        path: "share",
        name: "shareContainer",
        component: resolve =>
          require(["@/components/workingSpace/share/shareContainer"], resolve)
      },//share
      {
        path: "/activityInfo/:projectId",
        name: "workspaceContent",
        component: resolve =>
          require(["@/components/workingSpace/workspaceFrame.vue"], resolve),
        children: [
          {
            path: "show",
            name: "activityShow",
            component: resolve =>
              require(["@/components/workingSpace/activity/activityShow"], resolve)
          },
          {
            path: "type",
            name: "activityType",
            component: resolve =>
              require(["@/components/workingSpace/activity/typeChoose"], resolve)
          },
          {
            path: "work",
            name: "workSpace",
            component: resolve =>
              require([
                "@/components/workingSpace/activity/singleActivity"
              ], resolve),
          },
          {
            path: "info",
            name: "activityInfo",
            component: resolve =>
              require(["@/components/workingSpace/activity/multiActivity"], resolve)
          }
        ]
      },
    ]
  },

  //之前的工具页面
  // {
  //   path: "/dataService/:id/:token/:type",
  //   name: "dataService",
  //   component: resolve =>
  //     require(["@/components/utils/data/dataService"], resolve)
  // },
  {
    path: "/chat",
    name: "chatUtil",
    component: resolve =>
      require(["@/components/tools/entity/chatroom/chatroom.vue"], resolve)
  },
  {
    path: "/draw",
    name: "drawUtil",
    component: resolve =>
      require(["@/components/tools/entity/drawBoard"], resolve)
  },
  {
    path: "/map",
    name: "mapTool",
    component: resolve =>
      require(["@/components/tools/entity/mapTool"], resolve)
  },
  {
    path: "/computeModel",
    name: "computeModel",
    component: resolve =>
      require(["@/components/utils/ToolModel"], resolve)
  },
  {
    path: "/dataMethod",
    name: "dataMethod",
    component: resolve =>
      require(["@/components/utils/ToolDataMethod"], resolve)
  },
  {
    path: "/lineChart",
    name: "lineChart",
    component: resolve =>
      require(["@/components/tools/entity/charts/lineChart"], resolve)
  },
  {
    path: "/basicScatter",
    name: "basicScatter",
    component: resolve =>
      require(["@/components/tools/entity/charts/basicScatter"], resolve)
  },
  {
    path: "/mapScatter",
    name: "mapScatter",
    component: resolve =>
      require(["@/components/tools/entity/charts/mapScatter"], resolve)
  },
  {
    path: "/histogram",
    name: "histogram",
    component: resolve =>
      require(["@/components/tools/entity/charts/histogram"], resolve)
  },
  {
    path: "/pieChart",
    name: "pieChart",
    component: resolve =>
      require(["@/components/tools/entity/charts/pieChart"], resolve)
  },
  {
    path: "/radarChart",
    name: "radarChart",
    component: resolve =>
      require(["@/components/tools/entity/charts/radarChart"], resolve)
  },
  {
    path: "/funnelChart",
    name: "funnelChart",
    component: resolve =>
      require(["@/components/tools/entity/charts/funnelChart"], resolve)
  },
  {
    path: "/nc/draw",
    name: "drawUtilNC",
    component: resolve =>
      require(["@/components/tools/entity/singleUtils/ncDrawBoard"], resolve)
  },
  {
    path: "/nc/map",
    name: "mapToolNC",
    component: resolve =>
      require(["@/components/tools/entity/singleUtils/ncMapTool"], resolve)
  },
  {
    path: "/nc/charts",
    name: "dataChartsNC",
    component: resolve =>
      require(["@/components/tools/entity/singleUtils/ncCharts"], resolve)
  },
  // {
  //   path: "/nc/taskManager",
  //   name: "taskManager",
  //   component: resolve =>
  //     require(["@/components/tools/entity/singleUtils/taskManager"], resolve)
  // },
  {
    path: "/video",
    name: "videoViewer",
    component: resolve =>
      require(["@/components/tools/entity/videoViewer"], resolve)
  },
  {
    path: "/preview",
    name: "pdfViewer",
    component: resolve =>
      require(["@/components/tools/entity/filePreview"], resolve)
  },
  {
    path: "/abseir",
    name: "abSeir",
    component: resolve =>
      require(["@/components/tools/entity/ABM-SEIR"], resolve)
  },
  {
    path: "/tinymce",
    name: "tinymce",
    component: resolve =>
      require(["@/components/tools/entity/singleUtils/tinymce"], resolve)
  },
  {
    path: "/toolShow",
    name: "toolTemplate",
    component: resolve => require(["@/components/tools/toolPreview"], resolve)
  },
  {
    path: "/landisii/species",
    name: "species",
    component: resolve => require(["@/components/tools/entity/LandisII-tools/species"], resolve)
  },
  {
    path: "/landisii/bioSuccession",
    name: "bioSuccession",
    component: resolve => require(["@/components/tools/entity/LandisII-tools/biomass-succession"], resolve)
  },
  {
    path: "/landisii/climateConfig",
    name: "climateConfig",
    component: resolve => require(["@/components/tools/entity/LandisII-tools/climateConfig"], resolve)
  },
  {
    path: "/landisii/dynamicInputData",
    name: "dynamicInputData",
    component: resolve => require(["@/components/tools/entity/LandisII-tools/dynamicInputData"], resolve)
  },
  {
    path: "/editor/markDown",
    name: "markDown",
    component: resolve => require(["@/components/tools/entity/editor/markDown"], resolve)
  },
];

export default new Router({
  routes,
  mode: "history",
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { x: 0, y: 0 };
    }
  }
});

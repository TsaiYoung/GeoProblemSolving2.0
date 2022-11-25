<template>
  <div>
    <div style="margin-top: 70px;"></div>
    <Row>
      <Col span="9" offset="2">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-cube" size="25"/>
            <big>Project</big>
           </p>
          <div slot="extra">
            <router-link to="project">
              <!-- 1：<Icon type="ios-arrow-dropright" size="30" color="#2d8cf0" style="cursor:pointer;"/> -->
              <!-- 2：<button shape="circle" style="background-color: transparent;border: transparent;" class="extrabtn">
                <Icon type="ios-arrow-dropright" size="30" color="#2d8cf0" style="cursor:pointer;"/>
              </button> -->
              <!-- 3：蓝色或者灰色 -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span class="customFont">Manage project created and joined by yourself</span>

        </Card>
      </Col>
      <Col span="9" offset="1">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-cloud" size="25"/>
            <big>Resource</big>
          </p>
          <div slot="extra">
            <router-link to="resourceList">
              <!-- <Icon type="ios-arrow-dropright" size="20" color="blue" style="cursor:pointer;"/> -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span>You can manage resources upload by yourself</span>
        </Card>
      </Col>
    </Row>

    <Row style="margin-top: 40px">
      <Col span="9" offset="2">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-cog" size="25"/>
            <big>Tool</big>
          </p>
          <div slot="extra">
            <router-link to="tool">
              <!-- <Icon type="ios-arrow-dropright" size="20" color="blue" style="cursor:pointer;"/> -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span>You can manage tools created by yourself</span>
        </Card>
      </Col>
      <Col span="9" offset="1">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-clipboard" size="25"/>
            <big>Todo List</big>
          </p>
          <div slot="extra">
            <router-link to="todoList">
              <!-- <Icon type="ios-arrow-dropright" size="20" color="blue" style="cursor:pointer;"/> -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span>You can manage your task list</span>
        </Card>
      </Col>
    </Row>

    <Row style="margin-top: 40px">
      <Col span="9" offset="2">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-contact" size="25"/>
            <big>My Account</big>
          </p>
          <div slot="extra">
            <router-link to="userInfo">
              <!-- <Icon type="ios-arrow-dropright" size="20" color="blue" style="cursor:pointer;"/> -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span>You can manage tools created by yourself</span>
        </Card>
      </Col>
      <Col span="9" offset="1">
        <Card class="customCard">
          <p slot="title">
            <Icon type="ios-mail-open" size="25"/>
            <big>Message</big>
          </p>
          <div slot="extra">
            <router-link to="notification">
              <!-- <Icon type="ios-arrow-dropright" size="20" color="blue" style="cursor:pointer;"/> -->
              <Button shape="circle" icon="ios-arrow-forward" class="ovBtnHoverGray"></Button>
            </router-link>
          </div>
          <span>You can manage your messages</span>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<script>
  export default {
    name: "overView",
    methods: {
      test: function () {
        this.$router.push({name: 'tool'})
      }
    }
  }
</script>

<style scoped>
  .customCard {
    /*width: 335px;*/
    height: 130px;
    background-color: white;
    border-radius: 3px;
    border:1px solid #dadce0;
    box-shadow:  0 3.2px 7.2px 0 rgb(0 0 0 / 13%), 0 0.6px 1.8px 0 rgb(0 0 0 / 11%);
    opacity: 0.99;
  }


  .customFont {
    font-family: 'Lato', sans-serif;
  }
  .ovBtnHoverBlue:hover {
    background-color: #2db7f5;
    color: white;
  }
  .ovBtnHoverGray:hover {
    background-color: #808695;
    color: white;
  }
</style>

let websockets = {};
let callbacks = {};
let websockLinked = {};
//linking people
let connectorNum = {};

let socketSites = [];

let timer = null;


function initWebSocket(para) { //初始化websocket
  let IP_Port = window.location.host;
  var wsurl = `${window.location.protocol === 'https:' ? 'wss://' : 'ws://'}${IP_Port}/GeoProblemSolving/${para}`;
  if (IP_Port == "localhost:8080") {
    wsurl = `ws://localhost:8081/GeoProblemSolving/${para}`;
  }
  //switch 使用时提供一个参数type
  let websock = new WebSocket(wsurl);
  websock.onmessage = function (e) {
    let url = e.target.url;
    let para = url.substring(url.indexOf('/GeoProblemSolving/')+"/GeoProblemSolving/".length);
    websocketonmessage(e, para);
    websockLinked[para] = true;
  }
  websock.onclose = function (e) {
    let url = e.target.url;
    let para = url.substring(url.indexOf('/GeoProblemSolving/')+"/GeoProblemSolving/".length);

    websockLinked[para] = false;
    connectorNum[para] = -1;
    let index = socketSites.indexOf(para);
    if (index > -1){
      socketSites.splice(index, 1);
    }
    if (socketSites.length == 0){
      removeTimer();
    }

    websocketclose(e, para);
  }
  //连接成功的回调函数
  websock.onopen = function () {
    websocketOpen();
    if(timer == null){
      setTimer();
    }
    websockLinked[para] = true;
    connectorNum[para] = 1;
    socketSites.push(para);
  }

  //连接发生错误的回调方法
  websock.onerror = function (e) {
    console.log("WebSocket error");
    console.log(e);

    // let url = e.target.url;
    // let para = url.substring(url.indexOf('/GeoProblemSolving/')+"/GeoProblemSolving/".length);

    // websockLinked[para] = false;
    // connectorNum[para] = -1;
    // let index = socketSites.indexOf(para);
    // if (index > -1){
    //   socketSites.splice(index, 1);
    // }
    // if (socketSites.length == 0 ){
    //   removeTimer();
    // }
  }

  websockets[para] = websock;
}

function close(param) {
  websockets[param].close();
  websockets[param] = null;
}

function getSocketInfo(param) {
  return {
    linked: websockLinked[param],
    num: connectorNum[param]
  }
}

// 实际调用的方法
function sendSock(param, agentData, callback) {
  callbacks[param] = callback;
  if (websockets[param].readyState === websockets[param].OPEN) {
    // 若是ws开启状态
    if (connectorNum[param] > 1 || agentData.type == "test") {
      websocketsend(param, agentData);
    }
  } else if (websockets[param].readyState === websockets[param].CONNECTING) {
    // 若是 正在开启状态，则等待1s后重新调用
    setTimeout(function () {
      sendSock(param, agentData, callback);
    }, 1000);
  } else {
    // 若未开启 ，则等待1s后重新调用
    setTimeout(function () {
      sendSock(param, agentData, callback);
    }, 1000);
  }
}

//

//数据接收
function websocketonmessage(e, param) {
  try {
    var data = JSON.parse(e.data);
    if (data.type == "members") {
      connectorNum[param] = data.participants.length;
    }
    if (data.type != "ping") {
      let socketCallback = callbacks[param];
      if (socketCallback != null && socketCallback != "" && socketCallback != undefined) {
        socketCallback(data);
      }
    } else {
      return;
    }
  } catch (err) {
    console.log(err);
  };
}

//数据发送
function websocketsend(param, agentData) {
  websockets[param].send(JSON.stringify(agentData));
}

//关闭
function websocketclose(e, para) {
  // error
  if(e.code === 1006){
    initWebSocket(para);
  }
  console.log("Connection closed (" + e.code + ")");
}

function websocketOpen(e) {
  console.log("Connect successfully");
}

//keep beating
function setTimer() {
  timer = setInterval(() => {
    var messageJson = { type: "ping" };
    for (let i = 0; i < socketSites.length; i++) {
      websocketsend(socketSites[i], messageJson);
    }
  }, 20000);
}

function removeTimer() {
  clearInterval(timer);
  timer = null;
}


// initWebSocket();

export { initWebSocket, sendSock, close, getSocketInfo }

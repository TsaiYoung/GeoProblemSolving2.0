$(function () {
    // ready - event
    // create tags
    // ...
    // style
    $("#people-btn").on("click", function () {
        $("#people-btn").addClass("active");
        $("#resource-btn").removeClass("active");
        $("#operation-btn").removeClass("active");

        $("#people-list").show();
        $("#resource-list").hide();
        $("#operation-list").hide();

    })
    $("#resource-btn").on("click", function () {
        $("#people-btn").removeClass("active");
        $("#resource-btn").addClass("active");
        $("#operation-btn").removeClass("active");

        $("#people-list").hide();
        $("#resource-list").show();
        $("#operation-list").hide();
    })
    $("#operation-btn").on("click", function () {
        $("#people-btn").removeClass("active");
        $("#resource-btn").removeClass("active");
        $("#operation-btn").addClass("active");

        $("#people-list").hide();
        $("#resource-list").hide();
        $("#operation-list").show();

    })

    // message    
    window.addEventListener("message", getActivityInfo, false);
});

function getActivityInfo(event) {
    if (event.data.type === "activity") {
        console.log(event.data);
        getResources(event.data.aid);
    }

    // socket
    initWebSocket(event.data.aid, event.data.tid)
}

/**
 * Resources
 */
function getResources(aid) {

}

/**
 * Joined members
 */
function getParticipants() {

}

/**
 * Operations
 */
function a() {

}

/**
 * Socket
 */
var websock = null;
var global_callback = null;
var timer = null;
var websockLinked = false;

function initWebSocket(aid, toolId) { //初始化websocket
    let IP_Port = window.location.host;
    var wsurl = `${window.location.protocol === 'https:' ? 'wss://' : 'ws://'}${IP_Port}/GeoProblemSolving/OperationServer/${toolId}/${aid}`;
    if (IP_Port == "localhost:8080") {
        wsurl = `ws://localhost:8081/GeoProblemSolving/OperationServer/${toolId}/${aid}`;
    }
    //switch 使用时提供一个参数type
    websock = new WebSocket(wsurl);
    websock.onopen = function () {
        websocketOpen();
        setTimer();
        websockLinked = true;
    }
    websock.onmessage = function (e) {
        websocketonmessage(e);
        websockLinked = true;
    }
    websock.onclose = function (e) {
        websocketclose(e);
        removeTimer();
        websockLinked = false;
    }

    //连接发生错误的回调方法
    websock.onerror = function () {
        console.log("WebSocket error!");
        removeTimer();
        websockLinked = false;
    }

}

function close() {
    websock.close();
}

function getSocketInfo() {
    return {
        linked: websockLinked
    }
}

// 实际调用的方法
function sendSock(agentData, callback) {
    global_callback = callback;
    if (websock.readyState === websock.OPEN) {
        // 若是ws开启状态
        websocketsend(agentData)
    } else if (websock.readyState === websock.CONNECTING) {
        // 若是 正在开启状态，则等待1s后重新调用
        setTimeout(function () {
            sendSock(agentData, callback);
        }, 1000);
    } else {
        // 若未开启 ，则等待1s后重新调用
        setTimeout(function () {
            sendSock(agentData, callback);
        }, 1000);
    }
}

//数据接收
function websocketonmessage(e) {
    try {
        var data = JSON.parse(e.data);
        if (data.type != "ping") {
            if (global_callback != null && global_callback != "" && global_callback != undefined) {
                global_callback(data);
            }
        }
    } catch (err) { throw err };
}

function websocketOpen(e) {
    console.log("Connect successfully!");
}

//数据发送
function websocketsend(agentData) {
    websock.send(JSON.stringify(agentData));
}

//关闭
function websocketclose(e) {
    console.log("Connection closed (" + e.code + ")");
}

function setTimer() {
    timer = setInterval(() => {
        var messageJson = {type: "ping"};
        websocketsend(messageJson);
    }, 20000);
}

function removeTimer() {
    clearInterval(timer);
}
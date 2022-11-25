Array.prototype.contains = function (obj) {
    var i = this.length;
    while (i--) {
        if (this[i] != undefined && this[i] === obj ||
            this[i].userId != undefined && this[i].userId === obj) {
            return true;
        }
    }
    return false;
};

//guid
function guid() {
    function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    return (S4() + S4() + "-" + S4() + "-4" + S4().substr(0, 3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
}

let resProxy = "https://geomodeling.njnu.edu.cn/dataTransferServer";

// basic information
var toolId = "";
var activityInfo = null;
var userInfo = null;
var taskList = [];


///////////
/// init/page
//////////
{
    // the loading status of the collaboration component
    var componentStatus = false;
    var panelType = "people";

    function initComponent() {
        $("#collab-tool-head").append(`<li><span class="head-logo" id="tool-logo" style="cursor: pointer; width: 120px; display: inline-block;"></span></li>`);
        $("#collab-tool-sidebar").append(
            `<ul class="nav flex-column" style="width: 46px">
            <li class="nav-item">
                <button type="button" class="btn btn-dark active" id="people-btn" title="People">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                        class="bi bi-people-fill" viewBox="0 0 16 16">
                        <path d="M7 14s-1 0-1-1 1-4 5-4 5 3 5 4-1 1-1 1H7zm4-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
                        <path fill-rule="evenodd"
                            d="M5.216 14A2.238 2.238 0 0 1 5 13c0-1.355.68-2.75 1.936-3.72A6.325 6.325 0 0 0 5 9c-4 0-5 3-5 4s1 1 1 1h4.216z" />
                        <path d="M4.5 8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" />
                    </svg>
                </button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-dark" id="resource-btn" title="Resources">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                        class="bi bi-folder-fill" viewBox="0 0 16 16">
                        <path
                            d="M9.828 3h3.982a2 2 0 0 1 1.992 2.181l-.637 7A2 2 0 0 1 13.174 14H2.825a2 2 0 0 1-1.991-1.819l-.637-7a1.99 1.99 0 0 1 .342-1.31L.5 3a2 2 0 0 1 2-2h3.672a2 2 0 0 1 1.414.586l.828.828A2 2 0 0 0 9.828 3zm-8.322.12C1.72 3.042 1.95 3 2.19 3h5.396l-.707-.707A1 1 0 0 0 6.172 2H2.5a1 1 0 0 0-1 .981l.006.139z" />
                    </svg>
                </button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-dark" id="operation-btn" title="Operations">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                        class="bi bi-ui-checks" viewBox="0 0 16 16">
                        <path
                            d="M7 2.5a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-7a.5.5 0 0 1-.5-.5v-1zM2 1a2 2 0 0 0-2 2v2a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2H2zm0 8a2 2 0 0 0-2 2v2a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2v-2a2 2 0 0 0-2-2H2zm.854-3.646a.5.5 0 0 1-.708 0l-1-1a.5.5 0 1 1 .708-.708l.646.647 1.646-1.647a.5.5 0 1 1 .708.708l-2 2zm0 8a.5.5 0 0 1-.708 0l-1-1a.5.5 0 0 1 .708-.708l.646.647 1.646-1.647a.5.5 0 0 1 .708.708l-2 2zM7 10.5a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-7a.5.5 0 0 1-.5-.5v-1zm0-5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0 8a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5z" />
                    </svg>
                </button>
            </li>
        </ul>
        <div class="nav-content">
            <div class="sidebar-content scrollbar" id="people-list">
            </div>
            <div class="sidebar-content" id="resource-panel" style="display: none;">
                <div class="resource-search form-inline">
                    <div class="resource-input">
                        <input class="form-control form-control-sm" id="resource-search-form" style="width: 100%;"
                            placeholder="Key word">
                    </div>
                    <button class="btn btn-primary btn-sm">Filter</button>
                </div>
                <div class="resource-control">
                    Resource list
                    <div class="resource-control-btn" id="folder-back" title="Back to parent folder" style="display: none;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                            class="bi bi-arrow-left" viewBox="0 0 16 16">
                            <path fill-rule="evenodd"
                                d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z" />
                        </svg>
                    </div>
                    <div class="resource-control-btn" id="resource-update" title="Refresh">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                            class="bi bi-arrow-repeat" viewBox="0 0 16 16">
                            <path
                                d="M11.534 7h3.932a.25.25 0 0 1 .192.41l-1.966 2.36a.25.25 0 0 1-.384 0l-1.966-2.36a.25.25 0 0 1 .192-.41zm-11 2h3.932a.25.25 0 0 0 .192-.41L2.692 6.23a.25.25 0 0 0-.384 0L.342 8.59A.25.25 0 0 0 .534 9z" />
                            <path fill-rule="evenodd"
                                d="M8 3c-1.552 0-2.94.707-3.857 1.818a.5.5 0 1 1-.771-.636A6.002 6.002 0 0 1 13.917 7H12.9A5.002 5.002 0 0 0 8 3zM3.1 9a5.002 5.002 0 0 0 8.757 2.182.5.5 0 1 1 .771.636A6.002 6.002 0 0 1 2.083 9H3.1z" />
                        </svg>
                    </div>
                </div>
                <div class="resource-list scrollbar" id="resource-list"></div>
                <div class="resource-load">
                    <button class="btn btn-primary btn-sm" id="resource-load">Load resource</button>
                </div>
            </div>
            <div class="sidebar-content" id="operation-panel" style="display: none;">
                <div class="sync-state card" style="height: 40px">
                    <div class="custom-control custom-switch" title="Enable collaboration">
                        <input type="checkbox" class="custom-control-input" id="collaboration-switch" style="cursor: pointer">
                        <label class="custom-control-label" for="collaboration-switch" style="cursor: pointer">Collaboration</label>
                    </div>
                </div>
                <div class="sync-state card">
                    <select class="custom-select custom-select-sm" title="Collaboration mode" id="collaboration-mode" disabled >
                        <option selected value="Free">Free</option>
                        <option value="SemiFree_Occupy">Occupy</option>
                        <option value="SemiFree_Apply">Apply</option>
                    </select>
                </div>
                <div class="operation-control-apply" style="display: none;">
                    <div class="operation-apply">
                        <div>Operation</div>
                        <button class="btn btn-success btn-sm apply" style="background-color: green;" id="operation-apply">Apply</button>
                        <button class="btn btn-info btn-sm stop" style="display: none;" id="operation-stop">Stop</button>
                    </div>
                    <div class="operator" id="apply-operator">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-joystick" viewBox="0 0 16 16" style="margin-top: -3px; margin-right:5px">
                            <path d="M10 2a2 2 0 0 1-1.5 1.937v5.087c.863.083 1.5.377 1.5.726 0 .414-.895.75-2 .75s-2-.336-2-.75c0-.35.637-.643 1.5-.726V3.937A2 2 0 1 1 10 2z"/>
                            <path d="M0 9.665v1.717a1 1 0 0 0 .553.894l6.553 3.277a2 2 0 0 0 1.788 0l6.553-3.277a1 1 0 0 0 .553-.894V9.665c0-.1-.06-.19-.152-.23L9.5 6.715v.993l5.227 2.178a.125.125 0 0 1 .001.23l-5.94 2.546a2 2 0 0 1-1.576 0l-5.94-2.546a.125.125 0 0 1 .001-.23L6.5 7.708l-.013-.988L.152 9.435a.25.25 0 0 0-.152.23z"/>
                        </svg> Operator:
                    </div>
                    <div class="operation-waiting" id="operation-waiting"></div>
                </div>
                <div class="operation-control-occupy" style="display: none;">
                    <div class="operator" id="occupy-operator">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-joystick" viewBox="0 0 16 16" style="margin-top: -3px; margin-right:5px">
                            <path d="M10 2a2 2 0 0 1-1.5 1.937v5.087c.863.083 1.5.377 1.5.726 0 .414-.895.75-2 .75s-2-.336-2-.75c0-.35.637-.643 1.5-.726V3.937A2 2 0 1 1 10 2z"/>
                            <path d="M0 9.665v1.717a1 1 0 0 0 .553.894l6.553 3.277a2 2 0 0 0 1.788 0l6.553-3.277a1 1 0 0 0 .553-.894V9.665c0-.1-.06-.19-.152-.23L9.5 6.715v.993l5.227 2.178a.125.125 0 0 1 .001.23l-5.94 2.546a2 2 0 0 1-1.576 0l-5.94-2.546a.125.125 0 0 1 .001-.23L6.5 7.708l-.013-.988L.152 9.435a.25.25 0 0 0-.152.23z"/>
                        </svg> Operator:
                    </div>
                </div>
                <div class="operation-list scrollbar">
                    <span style="margin-left: 10px; font-size: 14px; font-weight:bold">Current operations</span>
                    <div id="operation-list"></div>
                </div>
                <div class="operation-bind">
                    <button class="btn btn-primary btn-sm" id="bind-tasks" data-toggle="modal" data-target="#bind-tasks-modal">Bind to task</button>
                    <div class="modal fade" id="bind-tasks-modal" tabindex="-1" role="dialog" aria-labelledby="bind-tasks-modal-title" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title" id="bind-tasks-modal-title">Bind operations to tasks</h5>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                    <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body" id="bind-tasks-modal-content">
                                    <h3>There is no operations needed to bind to tasks.</h3>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                                    <button type="button" class="btn btn-primary" id="bind-tasks-modal-ok">Bind</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`);

        // events
        addEvents();
    }

    function initBasicComponent() {

        window.addEventListener("message", getActivityInfo, false);
    }

    function getActivityInfo(event) {
        if (event.data.type === "activity") {

            activityInfo = event.data.activity;
            userInfo = event.data.user;
            onlineMembers = [userInfo];
            toolId = event.data.tid;
            taskList = event.data.tasks;

            componentStatus = true;

            getParticipants();
            getParentActivities();
            getResources();

            // socket
            CollabSocket.initWebSocket(activityInfo.aid, toolId);
        }
    }

    function addEvents() {
        // message
        // The event occurs when a message is received through the event source
        window.addEventListener("message", getActivityInfo, false);

        $("#tool-logo").on("click", function () {
            location.reload();
        });

        $("#people-btn").on("click", function () {

            if (panelType === "people") {
                $(".nav-content").hide();
                $("#collab-tool-sidebar").css("width", "46px");
                $("#collab-tool-content").css("left", "46px");
                $("#collab-tool-content").css("width", "calc(100vw - 46px)");

                $("#people-btn").removeClass("active");
                panelType = "";
            } else {
                $("#collab-tool-sidebar").css("width", "250px");
                $("#collab-tool-content").css("left", "250px");
                $(".nav-content").show();

                $("#people-btn").addClass("active");
                $("#resource-btn").removeClass("active");
                $("#operation-btn").removeClass("active");

                $("#people-list").show();
                $("#resource-panel").hide();
                $("#operation-panel").hide();

                $("#collab-tool-content").css("width", "calc(100vw - 250px)");
                panelType = "people";
            }
        })
        $("#resource-btn").on("click", function () {
            if (panelType === "resource") {
                $(".nav-content").hide();
                $("#collab-tool-sidebar").css("width", "46px");
                $("#collab-tool-content").css("left", "46px");
                $("#collab-tool-content").css("width", "calc(100vw - 46px)");

                $("#resource-btn").removeClass("active");
                panelType = "";
            } else {
                $("#collab-tool-sidebar").css("width", "250px");
                $("#collab-tool-content").css("left", "250px");
                $(".nav-content").show();

                $("#people-btn").removeClass("active");
                $("#resource-btn").addClass("active");
                $("#operation-btn").removeClass("active");

                $("#people-list").hide();
                $("#resource-panel").show();
                $("#operation-panel").hide();

                $("#collab-tool-content").css("width", "calc(100vw - 250px)");
                panelType = "resource";
            }
        })
        $("#operation-btn").on("click", function () {
            if (panelType === "operation") {
                $(".nav-content").hide();
                $("#collab-tool-sidebar").css("width", "46px");
                $("#collab-tool-content").css("left", "46px");
                $("#collab-tool-content").css("width", "calc(100vw - 46px)");

                $("#operation-btn").removeClass("active");
                panelType = "";
            } else {
                $("#collab-tool-sidebar").css("width", "250px");
                $("#collab-tool-content").css("left", "250px");
                $(".nav-content").show();

                $("#people-btn").removeClass("active");
                $("#resource-btn").removeClass("active");
                $("#operation-btn").addClass("active");

                $("#people-list").hide();
                $("#resource-panel").hide();
                $("#operation-panel").show();

                $("#collab-tool-content").css("width", "calc(100vw - 250px)");
                panelType = "operation";

            }
        })

        $("#folder-back").on("click", function () {
            getFolderRes({}, "back");
        });
        $("#resource-update").on("click", function () {
            getResources();
        });
        $("#resource-load").on("click", function () {
            if (loadResChannel != undefined && typeof loadResChannel == "function") {
                loadResChannel(selectedResources);
            }
        });

        $("#operation-apply").on("click", operationApply);
        $("#operation-stop").on("click", operationStop);
        $("#bind-tasks-modal-ok").on("click", function () {
            if (selectedTask != undefined && selectedTask != "" && selectedTask != "Select one task") {
                let data = {
                    "type": "task",
                    "behavior": "bind",
                    "operations": selectedOperations,
                    "task": selectedTask
                }
                postIframeMsg(data);
                $('#bind-tasks-modal').modal('hide');
            }
        });

        $("#collaboration-switch").on("change", () => {
            if ($("#collaboration-switch").is(':checked')) {
                startCollaboration();
            } else {
                CollabSocket.socketClose();
            }
        });

        $("#collaboration-mode").on("change", function () {
            let value = $("#collaboration-mode option:checked").val();
            syncCollabMode(value);
            setCollaborationMode(value);
        });
    }

    // post message to parent page
    function postIframeMsg(data) {
        window.parent.postMessage(data, '*');
    }
}

///////////
/// people
//////////
{
    // user related
    var participants = [];
    var onlineMembers = [];
    let UserServer = "/userServer";
    if (window.location.hostname == "localhost") {
        UserServer = "http://172.21.213.245:8088/userServer";
    }

    // data
    function getParticipants() {

        let reqUrl = "";
        if (activityInfo.level == 0) {
            reqUrl = "/GeoProblemSolving/project/" + activityInfo.aid + "/user";
        } else if (activityInfo.level == 1) {
            reqUrl = "/GeoProblemSolving/subproject/" + activityInfo.aid + "/user";
        } else if (activityInfo.level > 1) {
            reqUrl = "/GeoProblemSolving/activity/" + activityInfo.aid + "/user";
        }
        $.ajax({
            url: reqUrl,
            type: "GET",
            async: false,
            success: function (result) {
                if (result.code == 0) {
                    // creatorInfo = result.data.creator;
                    participants = result.data.members;
                    showParticipants();
                    initCollaborationMode();
                } else {
                    console.log(result.msg);
                }
            },
            error: function (err) {
                throw err;
            }
        });
    }

    function showParticipants() {
        for (let i = 0; i < participants.length; i++) {
            let avatar = ""
            if (participants[i].avatar == undefined || participants[i].avatar == "") {
                avatar = "./static/collabTemplate/img/icon_avatar.png";
            } else {
                avatar = UserServer + participants[i].avatar;
            }
            let peopleElement = `<div class="card participants" id="${participants[i].userId}">
                                <img src="${avatar}" class="participant-avatar" onerror="src='./static/collabTemplate/img/icon_avatar.png'"/>
                                <div class="participant-info">
                                    <div class="participant-info-name">${participants[i].name}</div>
                                    <div class="participant-info-role">${participants[i].role}</div>
                                </div>
                            </div>`;
            $("#people-list").append(peopleElement);
            if (participants[i].userId != userInfo.userId) {
                $(`#${participants[i].userId}`).css("background-color", "lightgrey");
            }
        }
    }

    function personOnline(members) {
        for (let i = 0; i < members.length; i++) {
            $(`#${members[i].userId}`).css("background-color", "white");
        }
        onlineMembers = members;
    }

    function personOffline(member) {
        $(`#${member.userId}`).css("background-color", "lightgrey");
        for (let i = 0; i < onlineMembers.length; i++) {
            if (onlineMembers[i].userId === member.userId) {
                onlineMembers.splice(i, 1);
            }
        }
    }
}


///////////
/// Resources
//////////
{
    // resource related
    var folderIdStack = [];
    var resources = [];
    var selectedResources = [];
    var loadResChannel = null;
    var parentResources = [];

    //data
    function getResources() {
        let temp = folderIdStack;
        if (temp.length == 0) {
            temp = ["0"];
            $("#folder-back").hide();
        }
        let paths = temp.toString();


        $.ajax({
            url: "/GeoProblemSolving/rip/" + activityInfo.aid + "/" + paths,
            type: "GET",
            async: false,
            success: function (result) {
                if (result == "Offline") {
                    confirm("You are offline, please login.");
                } else if (result.code == 0) {
                    let rootRes = result.data;
                    resources = resToCurrentFolder(rootRes);
                    importParentRes();
                    showResList();

                }
            },
            error: function (err) {
                throw err;
            }
        });
    }

    function getParentActivities() {
        let parents = [];
        if (activityInfo.level > 0 && activityInfo.aid != "" && activityInfo.aid != undefined) {
            let url = "";
            if (activityInfo.level == 1) {
                url = "/GeoProblemSolving/subproject/" + activityInfo.aid + "/lineage";
            } else if (activityInfo.level > 1) {
                url = "/GeoProblemSolving/activity/" + activityInfo.aid + "/lineage";
            }

            $.ajax({
                url: url,
                type: "GET",
                async: false,
                success: function (result) {
                    if (result == "Offline") {
                        confirm("You are offline, please login.");
                    } else if (result.code == 0) {
                        let list = result.data.ancestors;
                        for (let i = 1; i < list.length; i++) {
                            parents.push(list[i].aid);
                        }
                        getParentActivitiesFile(parents);
                    } else {
                        console.log(result.msg);
                    }
                },
                error: function (err) {
                    throw err;
                }
            });

        }
    }

    function getParentActivitiesFile(parents) {
        if (parents != undefined && parents.length > 0) {
            $.ajax({
                url: "/GeoProblemSolving/rip/file/" + parents.toString(),
                type: "GET",
                async: false,
                success: function (result) {
                    if (result == "Offline") {
                        confirm("You are offline, please login.");
                    } else if (result.code == 0) {
                        let fileList = result.data;
                        parentResources = [];
                        for (let i = 0; i < parents.length; i++) {
                            parentResources.push(fileList[parents[i]]);
                        }
                    } else {
                        console.log(result.msg);
                    }
                },
                error: function (err) {
                    throw err;
                }
            });
        }
    }

    // page
    function showResList() {
        $("#resource-list").empty();
        if (resources != undefined) {
            for (let i = 0; i < resources.folders.length; i++) {
                addfolder(resources.folders[i]);
            }
            for (let j = 0; j < resources.files.length; j++) {
                addfile(resources.files[j]);
            }
        }
    }

    function addfolder(folder) {
        let resElement = `<div class="card resource" title="${folder.name}">
                            <input class="form-check-input" type="checkbox" id="${folder.uid}">
                            <img src="./static/collabTemplate/img/folder.png" class="folder-${folder.uid} res-icon"/>
                            <div class="folder-${folder.uid} res-name">${folder.name}</div>
                        </div>`
        $("#resource-list").append(resElement);
        $(`.folder-${folder.uid}`).on("click", function () {
            getFolderRes(folder, "enter")

        });
        $(`#${folder.uid}`).on("change", function () {
            selectFile(folder);

            let message = {
                type: "resource",
                sender: userInfo.userId,
                behavior: "select",
                content: {
                    uid: file.uid,
                    name: file.name,
                    description: file.description,
                    address: file.address,
                }
            }
            CollabSocket.websocketSend(message);
        });
    }

    function addfile(file) {
        let resElement = "";
        let fileName = file.name + file.suffix;
        switch (file.type) {
            case "data": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}" >
                            <img src="./static/collabTemplate/img/data.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "model": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}" >
                            <img src="./static/collabTemplate/img/model.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "paper": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}" >
                            <img src="./static/collabTemplate/img/paper.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "document": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}">
                            <img src="./static/collabTemplate/img/document.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "image": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}" >
                            <img src="./static/collabTemplate/img/image.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "video": {
                resElement = `<div class="card resource" title="${fileName}">
                            <input class="form-check-input" type="checkbox" id="${file.uid}">
                            <img src="./static/collabTemplate/img/video.png" class="res-icon" />
                            <div class="res-name">${fileName}</div>
                        </div>`
                break;
            }
            case "others": {
                resElement = `<div class="card resource" title="${fileName}">
                    <input class="form-check-input" type="checkbox" id="${file.uid}">
                        <img src="./static/collabTemplate/img/otherfile.png" class="res-icon" />
                        <div class="res-name">${fileName}</div>
                        </>`
                break;
            }
        }
        $("#resource-list").append(resElement);
        $(`#${file.uid} `).on("change", function () {
            selectFile(file);

            let message = {
                type: "resource",
                sender: userInfo.userId,
                behavior: "select",
                content: {
                    uid: file.uid,
                    name: file.name,
                    description: file.description,
                    address: file.address,
                }
            }
            CollabSocket.websocketSend(message);
        });
    }

    function getFolderRes(folder, behavior) {
        if (behavior === "back" && folderIdStack.length > 1) {
            folderIdStack.splice(0, 1);
        } else if (behavior === "back" && folderIdStack.length === 1) {
            $("#folder-back").hide();
            folderIdStack.splice(0, 1);
        } else if (behavior === "enter") {
            $("#folder-back").show();
            folderIdStack.unshift(folder.uid)
        } else {
            return;
        }

        let temp = folderIdStack;
        if (temp.length == 0) {
            temp = ["0"];
        }
        $.ajax({
            url: "/GeoProblemSolving/rip/" +
                activityInfo.aid +
                "/" +
                temp.toString(),
            type: "GET",
            async: false,
            success: function (result) {
                if (result == "Offline") {
                    confirm("You are offline, please login.")
                } else if (result.code == 0) {
                    let rootRes = result.data;
                    resources = resToCurrentFolder(rootRes);
                    showResList();
                }
            },
            error: function (err) {
                throw err;
            }
        });

    }

    function resToCurrentFolder(rootRes) {
        let currentFolder = {
            folders: [],
            files: []
        }

        for (let i = 0; i < rootRes.length; i++) {
            if (rootRes[i].folder) {
                currentFolder.folders.push(rootRes[i]);
            } else {
                let address = rootRes[i].address;
                if (typeof (address) == "string") {
                    address = address.slice(-36);
                }
                rootRes[i].address = resProxy + "/data/" + address;
                currentFolder.files.push(rootRes[i]);
            }
        }
        return currentFolder;
    }

    function importParentRes() {
        for (let i = 0; i < parentResources.length; i++) {
            resources.files.push.apply(resources.files, parentResources[i]);
        }
    }

    // resource related operations
    function selectFile(file) {
        if ($(`#${file.uid} `).is(":checked")) {
            selectedResources.push(file);
        } else {
            for (let i = 0; i < selectedResources.length; i++) {
                if (selectedResources[i].uid == file.uid) {
                    selectedResources.splice(i, 1);
                }
            }
        }
    }

    function uploadResList(uploadFiles, description, type, privacy) {

        let temp = folderIdStack;
        if (temp.length == 0) {
            temp = ["0"];
        }
        let paths = temp.toString();

        var formData = new FormData();
        for (var i = 0; i < uploadFiles.length; i++) {
            formData.append("file", uploadFiles[i]);
        }
        formData.append("description", description);
        formData.append("type", type);
        formData.append("privacy", privacy);
        formData.append("aid", activityInfo.aid);
        formData.append("paths", paths);

        let uploadedList = fileUpload(formData);

        resourceChanged(uploadedList, "upload")

        for (let i = 0; i < uploadedList.length; i++) {
            let message = {
                type: "resource",
                behavior: "upload",
                sender: userInfo.userId,
                content: {
                    uid: uploadedList[i].uid,
                    name: uploadedList[i].name,
                    type: "data",
                    suffix: uploadedList[i].suffix,
                    provider: userInfo.userId,
                    description: uploadedList[i].description,
                    address: uploadedList[i].address,
                }
            }
            // collaboration message
            CollabSocket.websocketSend(message);
            // record
            addOperations(userInfo, message, "origin");
        }
        return uploadedList;
    }

    function saveResList(uploadFiles, description, type, privacy, thumbnail) {

        let temp = folderIdStack;
        if (temp.length == 0) {
            temp = ["0"];
        }
        let paths = temp.toString();

        var formData = new FormData();
        for (var i = 0; i < uploadFiles.length; i++) {
            formData.append("file", uploadFiles[i]);
        }
        formData.append("description", description);
        formData.append("type", type);
        formData.append("privacy", privacy);
        formData.append("thumbnail", thumbnail);
        formData.append("aid", activityInfo.aid);
        formData.append("paths", paths);
        formData.append("editToolInfo", toolId);

        let uploadedList = fileUpload(formData);

        resourceChanged(uploadedList, "save")

        for (let i = 0; i < uploadedList.length; i++) {
            let message = {
                type: "geo-analysis",
                purpose: "Data processing",
                sender: userInfo.userId,
                toolId: toolId,
                inputs: [],
                outputs: [
                    {
                        uid: uploadedList[i].uid,
                        name: uploadedList[i].name,
                        type: "data",
                        suffix: uploadedList[i].suffix,
                        provider: userInfo.userId,
                        description: uploadedList[i].description,
                        address: uploadedList[i].address,
                        thumbnail: uploadedList[i].thumbnail,
                        editToolInfo: toolId
                    }
                ],
                params: [],
                participants: onlineMembers
            }

            // collaboration message
            CollabSocket.websocketSend(message);
            // record
            addOperations(userInfo, message, "origin");
        }
        return uploadedList;
    }

    function fileUpload(formData) {
        let uploadedList = null;
        $.ajax({
            url: "/GeoProblemSolving/rip/file/upload",
            type: "POST",
            data: formData,
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            cache: false,
            async: false,
            success: function (data) {
                if (data != "Fail") {
                    uploadedList = JSON.parse(data).uploaded;
                } else {
                    alert("Upload fail.");
                }
            },
            error: function (err) {
                throw err;
            }
        });
        return uploadedList;
    }

    function resaveFile(file, info) {

        var formData = new FormData();
        formData.append("file", file);
        formData.append("resInfo", info);

        let temp = folderIdStack;
        if (temp.length == 0) {
            temp = ["0"];
        }
        let paths = temp.toString();

        $.ajax({
            url: `/GeoProblemSolving/rip/file/${activityInfo.aid}/${paths}`,
            type: "PUT",
            data: formData,
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            cache: false,
            async: false,
            success: function (data) {
                let result;
                try {
                    result = JSON.parse(data);
                } catch (e) {
                    result = data;
                }
                if (result.code == 0) {
                    resultData = result.data;
                } else {
                    resultData = "Error";
                }
            },
            error: function (err) {
                throw err;
            }
        });
        return "success";
    }

    function deleteResource() {
    }

    /**
     * when resources changed
     */
    function resourceChanged(resources, behavior) {
        switch (behavior) {
            case "upload":
            case "save": {
                for (let i = 0; i < resources.length; i++) {
                    if (resources[i].folder) {
                        addfolder(resources[i]);
                    } else {
                        addfile(resources[i]);
                    }
                }
                break;
            }
            case "delete": {

                break;
            }
        }
    }
}


///////////
/// Operations
//////////
{
    // operation related
    var selectedOperations = [];

    // task related
    var selectedTask = "";

    // collaboration mode
    var collabMode = "Free";

    // style
    function initCollaborationMode() {
        for (let i = 0; i < participants.length; i++) {
            if (participants[i].userId === userInfo.userId && participants[i].role === "manager") {
                $("#collaboration-mode").attr("disabled", false);
            }
        }
    }

    function setCollaborationMode(mode) {
        if (mode != undefined && mode !== "") {
            if (mode === "Free") {
                $("#collaboration-mode").val("Free");
                $(".operation-control-apply").hide();
                $(".operation-control-occupy").hide();
                $(".operation-list").css("height", "calc(100vh - 200px)");
                collabMode = "Free";
            } else if (mode === "SemiFree_Apply") {
                $("#collaboration-mode").val("SemiFree_Apply");
                $(".operation-control-apply").show();
                $(".operation-control-occupy").hide();
                $(".operation-list").css("height", "calc(100vh - 305px)");
                collabMode = "SemiFree_Apply";
            } else if (mode === "SemiFree_Occupy") {
                $("#collaboration-mode").val("SemiFree_Occupy");
                $(".operation-control-apply").hide();
                $(".operation-control-occupy").show();
                $(".operation-list").css("height", "calc(100vh - 250px)");
                collabMode = "SemiFree_Occupy";
            }
        }
    }

    function setOperator(operator) {
        if (collabMode == "Free") {
            $("#edit-mask").hide();
        } else if (collabMode == "SemiFree_Occupy") {
            $("#edit-mask").hide();

            if (operator != undefined && operator != {} && operator != "") {
                $("#occupy-operator-name").remove();
                $("#occupy-operator").append(`<span class="operator-name" id="occupy-operator-name" title="${operator.name}">${operator.name}</span>`);
            }
        } else if (collabMode == "SemiFree_Apply") {
            let user = null;
            if (Object.prototype.toString.call(operator) == "[object Object]") {
                user = Object.assign({}, operator);
                operator = operator.userId
            } else {
                for (let i = 0; i < participants.length; i++) {
                    if (participants[i].userId == operator) {
                        user = participants[i];
                    }
                }
            }

            if (operator != userInfo.userId) {
                $("#operation-apply").show();
                $("#operation-stop").hide();
                $("#edit-mask").show();
            } else {
                $("#operation-apply").hide();
                $("#operation-stop").show();
                $("#edit-mask").hide();
            }

            if (operator != undefined && operator != "") {
                if (user != null) {
                    $("#apply-operator-name").remove();
                    $("#apply-operator").append(`<span class="operator-name" id="apply-operator-name" title="${user.name}">${user.name}</span>`);
                }
            } else if (operator == "") {
                $("#apply-operator-name").remove();
                $("#apply-operator").append(`<span class="operator-name" id="apply-operator-name" title="no operator">${operator}</span>`);
            }
        }
    }

    function setWaitingLine(count) {
        if (count != undefined) {
            if (count > 0) {
                $("#operation-waiting").empty();
                $("#operation-waiting").append(`<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person-lines-fill" viewBox="0 0 16 16" style="margin-top: -3px;">
                                                <path d="M6 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm-5 6s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H1zM11 3.5a.5.5 0 0 1 .5-.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 1-.5-.5zm.5 2.5a.5.5 0 0 0 0 1h4a.5.5 0 0 0 0-1h-4zm2 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2zm0 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2z"/>
                                            </svg>
                                            <span style="margin-left: 5px; color: #007bff;" title="Waiting for operation">Waiting for ${count} people</span>`);
            } else if (count == 0) {
                $("#operation-waiting").empty();
                $("#operation-waiting").append(`<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person-lines-fill" viewBox="0 0 16 16" style="margin-top: -3px;">
                                                <path d="M6 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm-5 6s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H1zM11 3.5a.5.5 0 0 1 .5-.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 1-.5-.5zm.5 2.5a.5.5 0 0 0 0 1h4a.5.5 0 0 0 0-1h-4zm2 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2zm0 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2z"/>
                                            </svg>
                                            <span style="margin-left: 5px; color: #007bff;" title="Waiting for operation">Apply to operate</span>`);
            } else if (count < 0) {
                $("#operation-waiting").empty();
                $("#operation-waiting").append(`<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-person-lines-fill" viewBox="0 0 16 16" style="margin-top: -3px;">
                                                <path d="M6 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm-5 6s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H1zM11 3.5a.5.5 0 0 1 .5-.5h4a.5.5 0 0 1 0 1h-4a.5.5 0 0 1-.5-.5zm.5 2.5a.5.5 0 0 0 0 1h4a.5.5 0 0 0 0-1h-4zm2 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2zm0 3a.5.5 0 0 0 0 1h2a.5.5 0 0 0 0-1h-2z"/>
                                            </svg>
                                            <span style="margin-left: 5px; color: #007bff;" title="Waiting for operation">Current operator</span>`);
            }
        }
    }

    /**
     * 给操作列表增加记录
     * @param {*} user
     * @param {*} operation
     */
    function addOperations(user, operation, from) {
        // set operation id
        let operationId = guid();
        operation["id"] = operationId;

        if (operation != undefined) {

            switch (operation.type) {
                case "resource": {
                    if (operation.behavior === "upload") {
                        let element = "";
                        if (user.userId === userInfo.userId) {
                            element = `<div class="operation-item">
                                        <div class="operation-title">Data - upload
                                            <input class="form-check-input operation-bind-check" type="checkbox" id="${operation.id}" title="Bind operations to the task">
                                        </div>
                                        <div class="operation-divider"></div>
                                        <div class="operation-content" title="You uploaded the data - ${operation.content.name}">You uploaded the data - ${operation.content.name}</div>
                                    </div>`;
                        } else {
                            element = `<div class="operation-item">
                                        <div class="operation-title">Data - upload</div>
                                        <div class="operation-divider"></div>
                                        <div class="operation-content" title="${user.name} uploaded the data - ${operation.content.name}">${user.name} uploaded the data - ${operation.content.name}</div>
                                    </div>`;
                        }

                        $("#operation-list").append(element);
                        $(`#${operation.id}`).on("change", function () {
                            selectOperations(operation);
                        });


                    } else if (operation.behavior === "update") {

                    }
                    break;
                }
                case "communication": {
                    break;
                }
                case "geo-analysis": {

                    if (operation.purpose === "Data processing") {
                        let element = "";
                        if (operation.participants.contains(userInfo.userId)) {
                            element = `<div class="operation-item">
                                        <div class="operation-title">Geoanalysis - processing
                                            <input class="form-check-input operation-bind-check" type="checkbox" id="${operation.id}" title="Bind operations to the task">
                                        </div>
                                        <div class="operation-divider"></div>
                                        <div class="operation-content" title="You were processing the data - ${operation.outputs[0].name}">You were processing the data - ${operation.outputs[0].name}.</div>
                                    </div>`;
                        } else {
                            element = `<div class="operation-item">
                                        <div class="operation-title">Geoanalysis - processing</div>
                                        <div class="operation-divider"></div>
                                        <div class="operation-content" title="The data - ${operation.content.name} were processing">The data - ${operation.content.name} were processing.</div>
                                    </div>`;
                        }

                        $("#operation-list").append(element);
                        $(`#${operation.id}`).on("change", function () {
                            selectOperations(operation);
                        });
                    } else if (operation.purpose === "execute") {

                    } else if (operation.purpose === "modify") {

                    }
                    break;
                }
            }

            // save the temporary operation
            if (from === "origin") {
                postIframeMsg({
                    "type": "task",
                    "behavior": "record",
                    "operations": [operation],
                    "task": ""
                });
            } else if (from === "transfer") {
            }
        }
    }

    function refreshOperation(oid) {
        let message = {
            type: "task-record-backend",
            oid: oid
        };
        postIframeMsg(message)
    }

    // Synchronize
    function startCollaboration() {
        CollabSocket.initWebSocket(activityInfo.aid, toolId);
    }

    function syncCollabMode(mode) {
        let message = {
            type: "mode",
            sender: userInfo.userId,
            content: mode
        }
        CollabSocket.websocketSend(message);
    }

    function operationApply() {
        let message = {
            type: "control-apply",
            sender: userInfo.userId,
        }
        CollabSocket.websocketSend(message);
    }

    function operationStop() {
        let message = {
            type: "control-stop",
            sender: userInfo.userId,
        }
        CollabSocket.websocketSend(message);
    }

    function selectOperations(operation) {
        if ($(`#${operation.id}`).is(":checked")) {
            selectedOperations.push(operation);
        } else {
            for (let i = 0; i < selectedOperations.length; i++) {
                if (selectedOperations[i].id == operation.id) {
                    selectedOperations.splice(i, 1);
                }
            }
        }
        // 操作绑定模态框
        $("#bind-tasks-modal-content").empty();
        if (selectedOperations.length === 0) {
            $("#bind-tasks-modal-content").append(`<h3>There is no operations needed to bind to tasks.</h3>`);

        } else if (selectedOperations.length > 0) {
            if (taskList.length === 0) {
                $("#bind-tasks-modal-content").append(`<h3>There is no existing task.</h3>`);
            } else if (taskList.length > 0) {
                $("#bind-tasks-modal-content").append(`<select class="custom-select" id="task-list">
                                                            <option selected>Select one task</option>
                                                        </select>`);
                for (let i = 0; i < taskList.length; i++) {
                    let elem = `<option value="${taskList[i].taskId}" title="${taskList[i].purpose}">${taskList[i].name}</option>`
                    $("#task-list").append(elem);
                }
                $("#task-list").on("change", function () {
                    selectedTask = $("#task-list option:checked").val();
                });
            }
        }
    }

}


///////////
/// Socket
//////////
{
    var operationChannel = null;
    var dataChannel = null;
    var computationChannel = null;
    var participantChannel = null;

    var CollabSocket = {
        websock: null,
        timer: null,
        websockLinked: false,

        initSocketChannel: function (opeChannel, resChannel, compChannel, peopleChannel) {
            operationChannel = opeChannel;
            dataChannel = resChannel;
            computationChannel = compChannel;
            participantChannel = peopleChannel;
        },

        initWebSocket: function (aid, toolId) { //初始化websocket
            let IP_Port = window.location.host;
            var wsurl = `${window.location.protocol === 'https:' ? 'wss://' : 'ws://'}${IP_Port}/GeoProblemSolving/OperationServer/${toolId}/${aid}`;
            if (IP_Port == "localhost:8080") {
                wsurl = `ws://localhost:8081/GeoProblemSolving/OperationServer/${toolId}/${aid}`;
            }
            //switch 使用时提供一个参数type
            this.websock = new WebSocket(wsurl);
            this.websock.onopen = () => {
                console.log("Connect successfully!");
                this.setTimer();
                this.websockLinked = true;

                this.websocketSend({
                    type: "test",
                    sender: userInfo.userId
                });

                $("#collaboration-switch").attr('checked', true);
            }
            this.websock.onmessage = (e) => {
                this.websocketonmessage(e);
                this.websockLinked = true;
            }
            this.websock.onclose = (e) => {
                console.log("Connection closed (" + e.code + ")");
                this.removeTimer();
                this.websockLinked = false;

                if (e.code == 1006) {
                    this.initWebSocket(aid, toolId);
                }
            }

            //连接发生错误的回调方法
            this.websock.onerror = () => {
                console.log("WebSocket error!");
                // this.removeTimer();
                // this.websockLinked = false;
            }

        },

        setTimer: function () {
            this.timer = setInterval(() => {
                var messageJson = {type: "ping"};
                this.websocketSend(messageJson);
            }, 20000);
        },

        removeTimer: function () {
            clearInterval(this.timer);
        },

        //数据接收
        websocketonmessage: function (e) {
            try {
                let data = JSON.parse(e.data);
                switch (data.type) {
                    case "members": {
                        if (data.behavior == "on") {
                            personOnline(data.participants);
                        } else if (data.behavior == "off") {
                            personOffline(data.activeUser);
                        }

                        if (participantChannel != undefined && typeof participantChannel == "function") {
                            participantChannel(data);
                        }
                        break;
                    }
                    case "collaboration-init": {
                        setCollaborationMode(data.content);
                        setOperator(data.operator);
                        setWaitingLine(data.waiting);
                        break;
                    }
                    case "mode": {
                        if (data.operator !== userInfo.userId) {
                            setCollaborationMode(data.content);
                        }
                        setOperator("");
                        setWaitingLine(0);
                        break;
                    }
                    case "control-apply": {
                        if (data.operator == userInfo.userId) {
                            $("#operation-apply").hide();
                            $("#operation-stop").show();
                        }
                        setOperator(data.operator);
                        setWaitingLine(data.waiting);
                        break;
                    }
                    case "control-stop": {
                        setOperator(data.operator);
                        setWaitingLine(data.waiting);
                        break;
                    }
                    case "operation": {
                        setOperator(data.sender);
                        if (data.behavior != undefined && data.behavior == "Refuse") {
                            alert("Please wait for a while.")
                            break;
                        }
                        if (operationChannel != undefined && typeof operationChannel == "function") {
                            if (data.sender.userId !== userInfo.userId) {
                                operationChannel(data);
                            }
                        }
                        break;
                    }
                    case "resource": {
                        if (dataChannel != undefined && typeof dataChannel == "function") {
                            if (data.sender.userId !== userInfo.userId) {
                                if (data.behavior == "select") {
                                    selectFile(data.content);
                                }
                                dataChannel(data);
                                // record
                                // addOperations(message.sender, message, "transfer");
                            }
                        }
                        break;
                    }
                    case "geo-analysis": {
                        // addOperations(message.sender, message, "transfer");
                    }
                    case "computation": {
                        if (computationChannel != undefined && typeof computationChannel == "function") {
                            computationChannel(data);
                            if (data.computeSuc) {
                                let computationResult = {
                                    type: "task-record-backend",
                                    oid: data.operationId
                                }
                                postIframeMsg(computationResult);
                            }
                        }
                        break;
                    }
                    case "test": {
                        try {
                            content = JSON.parse(data.content);
                            setCollaborationMode(content.mode);
                            setOperator(content.operator);
                            setWaitingLine(content.waiting);
                        } catch (err) {
                            console.log(err);
                        }
                        break;
                    }
                }
            } catch (err) {
                throw err
            }
        },


        //数据发送
        websocketSend: function (agentData) {
            this.websock.send(JSON.stringify(agentData));
        },

        /**
         * collaboration end
         */
        socketClose: function () {
            this.websock.close();
        },

        reciveCustomOperations: function (agentData) {
            // operationChannel = callback;
            if (this.websock.readyState === this.websock.OPEN) {
                // 若是ws开启状态
                this.websocketSend(agentData)
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                // 若是 正在开启状态，则等待1s后重新调用
                setTimeout(function () {
                    this.reciveCustomOperations(agentData);
                }, 1000);
            } else {
                // 若未开启 ，则等待1s后重新调用
                setTimeout(function () {
                    this.reciveCustomOperations(agentData);
                }, 1000);
            }
        },

        /*
        这个层次的工具并不是面向高级用户（开发者）
        通用工具是使用模型部署包与数据服务包为后台
        开发者只需要写封装代码即可
        至于工具生成、调用这一块内容对其为透明的
        为此
        为了简化代码
        没必要将两类工具 Invoke 强行拧在一起

        面向开发者定制开发工具
        若他在定制工具中使用到了通用工具
        如果要进行自定义的话
        那我最好还是统一接口
        但是既然工具后台服务读取都使用不同的接口
        那肯定是能区分是模型服务还是数据服务
        这时使用强行拧在一起的接口
        开发者更不能很好的理解参数含义
        如：dataService 时 servicePort 置空
        serviceIp 为 Token
         */
        reciveModelOperation: function (aid, serviceMd5, serviceIp, servicePort, inputs, outputs) {
            // computationChannel = callback;
            //若是数据方法的话，则直接将部分参数置空即可
            let invokeForm = {
                serviceMd5: serviceMd5,
                serviceIp: serviceIp,
                servicePort: servicePort,
                computeAbleModel: true,
                type: "computation",
                inputs: inputs,
                outputs: outputs,
                sender: userInfo.userId,
                graphId: activityInfo.parent
            };
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(invokeForm);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.reciveModelOperation(aid, serviceId, serviceIp, servicePort, inputs, outputs);
                }, 1000)
            } else {
                //未开启，等待 1s
                setTimeout(function () {
                    this.reciveModelOperation(aid, serviceId, serviceIp, servicePort, inputs, outputs);
                }, 1000)
            }
        },

        receiveDataComputation: function (aid, serviceId, serviceToken, inputData, inputs, params) {
            // computationChannel = callback;
            let invokeMsg = {
                type: "computation",
                tid: serviceId,
                token: serviceToken,
                urls: inputs,
                inputs: inputData,
                params: params,
                computeAbleModel: false,
                sender: userInfo.userId,
                graphId: activityInfo.parent
            };
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(invokeMsg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.receiveDataComputation(aid, serviceId, serviceToken, inputData, inputs, params);
                }, 1000)
            } else {
                setTimeout(function () {
                    this.receiveDataComputation(aid, serviceId, serviceToken, inputData, inputs, params);
                }, 1000)
            }
        },

        /*
        协同工具输入或输出协同显示
        输入某个文件或参数
        都是对mdl 文档的修改
        先采用每次都全部更新
        vue 视图更新总是会刷新的
        msg.content 用于存储传输内容
         */
        //协同工具---正在输入提示
        receiveTypingOperation: function (index, inOrOut) {
            //inputNum 表示DOM 元素编号
            //importer 表示正在输入的用户
            let typingMsg = {
                type: "operation",
                behavior: "message",
                content: {
                    "inputNum": index,
                    "importer": userInfo.name,
                    "inOrOut": inOrOut
                },
                sender: userInfo.userId
            };
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(typingMsg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.receiveTypingOperation(index, inOrOut);
                }, 1000)
            } else {
                setTimeout(function () {
                    this.receiveTypingOperation(index, inOrOut);
                }, 1000)
            }
        },
        //协同工具---输入文件
        receiveDataInputDataOperation: function (inputMdl, addOrRemove) {
            let msg = {
                type: "operation",
                behavior: "data",
                content: {
                    addOrRemove: addOrRemove,
                    inputs: inputMdl
                },
                sender: userInfo.userId
            };
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(msg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.receiveDataInputDataOperation(inputMdl, addOrRemove);
                }, 1000)
            } else {
                setTimeout(function () {
                    this.receiveDataInputDataOperation(inputMdl, addOrRemove);
                }, 1000)
            }
        },

        /*
        协同工具---输入参数
         */
        receiveParamsOperation: function (inputParams, stateIndex) {
            let paramsMsg = {
                type: "operation",
                behavior: "params",
                content: {
                    inputs: inputParams,
                    stateIndex: stateIndex
                },
                sender: userInfo.userId
            };

            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(paramsMsg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.receiveParamsOperation(inputParams, stateIndex);
                }, 1000)
            } else {
                setTimeout(function () {
                    this.receiveParamsOperation(inputParams, stateIndex);
                }, 1000)
            }
        },

        reciveElementChangeOperation(paramsMsg) {
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(paramsMsg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.reciveElementChangeOperation(inputParams);
                }, 1000)
            } else {
                setTimeout(function () {
                    this.reciveElementChangeOperation(inputParams);
                }, 1000)
            }
        },

        receiveRunToolOperation: function () {
            let msg = {
                type: "operation",
                behavior: "run",
                content: {
                    toolRun: "run"
                },
                sender: userInfo.userId
            };
            if (this.websock.readyState === this.websock.OPEN) {
                this.websocketSend(msg);
            } else if (this.websock.readyState === this.websock.CONNECTING) {
                setTimeout(function () {
                    this.receiveRunToolOperation();
                }, 1000)
            } else {
                setTimeout(function () {
                    this.receiveRunToolOperation();
                }, 1000)
            }
        },


        socketInfo: function () {
            return {
                linked: this.websockLinked
            }
        }

    }
}


///////////
/// public methods
//////////
{
    /**
     * 加载协同模板组件
     */
    function loadCollabComponent() {
        initComponent();
    }


    /**
     * 使用组件基础功能：获取基本信息;协同功能
     */
    function basicCollabComponent() {
        initBasicComponent();
    }

    /**
     * 按文件夹，获取最新的资源
     * @param {*} paths
     */
    function refreshResources() {
        getResources();
    }

    /**
     * 切换文件夹
     * @param {*} folder
     * @param {*} behavior
     */
    function switchFolder(folder, behavior) {
        getFolderRes(folder, behavior);
    }

    /**
     * upload files
     * 上传
     * @param {*} uploadFiles 文件
     * @param {*} description 描述
     * @param {*} type 文件类型
     * @param {*} privacy 获取权限
     */
    function uploadResources(uploadFiles, description, type, privacy) {
        return uploadResList(uploadFiles, description, type, privacy);
    }

    /**
     * save as new files
     * 另存为
     * @param {*} uploadFiles
     * @param {*} description
     * @param {*} type
     * @param {*} privacy
     * @returns
     */
    function saveResources(uploadFiles, description, type, privacy, thumbnail) {
        return saveResList(uploadFiles, description, type, privacy, thumbnail);
    }

    /**
     * save file
     * 保存
     * @param {*} file
     * @param {*} info
     */
    function resaveResource(file, info) {
        return resaveFile(file, JSON.stringify(info));
    }


    function loadingBackendOperation(oid) {
        refreshOperation(oid)
    }


    /**
     * 活动socket转态信息
     * get socket status
     */
    function getSocketInfo() {
        return CollabSocket.socketInfo();
    }

    /**
     * 自定义操作回调函数
     * send custom operations
     */
    function sendCustomOperation(agentData) {
        CollabSocket.reciveCustomOperations(agentData);
    }

    /**
     * 模型计算操作回调函数
     * send custom operations
     */
    function sendModelOperation(aid, serviceMd5, serviceIp, servicePort, inputs, outputs) {
        CollabSocket.reciveModelOperation(aid, serviceMd5, serviceIp, servicePort, inputs, outputs);
    }

    function sendSelectDataOperation() {
    }

    function sendTypingInfo(index, inOrOut) {
        CollabSocket.receiveTypingOperation(index, inOrOut);
    }

    /**
     * Collaboratively input parameters
     * 参数协同输入方法
     * @param domId
     * @param inputType
     * @param style
     * @param value
     * @param attributes
     */
    function sendElementChangeOperation(elemId, behavior, type, value, style, attributes) {
        var change = {
            type: "operation",
            behavior: behavior,
            content: {
                id: elemId,
                type: type,
                value: value,
                style: style,
                attributes: attributes
            },
            sender: userInfo.userId
        }
        CollabSocket.reciveElementChangeOperation(change);
    }

    function sendInputParams(inputParams, stateIndex) {
        CollabSocket.receiveParamsOperation(inputParams, stateIndex);
    }

    /**
     * 数据处理操作回调函数
     * data service callback method
     * @param aid
     * @param serviceId
     * @param serviceToken
     * @param inputData
     * @param inputs
     * @param params
     * @param callback
     */
    function sendDataOperation(aid, serviceId, serviceToken, inputData, inputs, params) {
        CollabSocket.receiveDataComputation(aid, serviceId, serviceToken, inputData, inputs, params);
    }

    /**
     * 建立协同数据通道
     * build call back channel
     */
    function buildSocketChannel(opeChannel, resChannel, compChannel, peopleChannel) {
        CollabSocket.initSocketChannel(opeChannel, resChannel, compChannel, peopleChannel);
    }

    //
    function selectDataOperation(value, addOrRemove) {
        CollabSocket.receiveDataInputDataOperation(value, addOrRemove);
    }

    function runTool() {
        CollabSocket.receiveRunToolOperation();
    }
}

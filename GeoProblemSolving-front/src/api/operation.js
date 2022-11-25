/**
 * 提供操作记录的存储、删除、查询方法
 * @ 2021/3/29
 * @ mzy
 */
var xmlDoc = null;
var strDoc = null;

//修改时间格式使其统一
Date.prototype.Format = function (fmt) {
  var o = {
    "M+": this.getMonth() + 1, //月份
    "d+": this.getDate(), //日
    "H+": this.getHours(), //小时
    "m+": this.getMinutes(), //分
    "s+": this.getSeconds(), //秒
    "q+": Math.floor((this.getMonth() + 3) / 3), //季度
    "S+": this.getMilliseconds(), //毫毛
  };
  if (/(y+)/.test(fmt))
    fmt = fmt.replace(
      RegExp.$1,
      (this.getFullYear() + "").substr(4 - RegExp.$1.length)
    );
  for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt))
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length == 1
          ? o[k]
          : ("00" + o[k]).substr(("" + o[k]).length)
      );
  return fmt;
};

// 加载活动文档
function loadActivityDoc() {

  try //Internet Explorer
  {
    xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
    xmlDoc.async = "false";
    xmlDoc.loadXML(strDoc);
  } catch (e) {
    try //Firefox, Mozilla, Opera, etc.
    {
      let parser = new DOMParser();
      xmlDoc = parser.parseFromString(strDoc, "text/xml");
    } catch (e) {
      alert(e.message)
    }
  }
}


//guid
function guid() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }

  return (S4() + S4() + "-" + S4() + "-4" + S4().substr(0, 3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
}

// 创建活动时的活动文档初始化
export function activityDocInit(activityInfo, user) {

  strDoc = `<Activity></Activity>`;
  loadActivityDoc();
  let Activity = xmlDoc.getElementsByTagName("Activity")[0];
  if (Activity == undefined) return;

  // attr
  Activity.setAttribute("id", activityInfo.aid);
  Activity.setAttribute("name", activityInfo.name);
  Activity.setAttribute("description", activityInfo.description);
  Activity.setAttribute("type", activityInfo.type);

  // children
  let Participants = xmlDoc.createElement('Participants');
  let Person = xmlDoc.createElement('Person');
  Person.setAttribute("id", user.userId);
  Person.setAttribute("name", user.name);
  Person.setAttribute("role", "Manager");
  Participants.appendChild(Person);
  Activity.appendChild(Participants);

  let ResourceCollection = xmlDoc.createElement('ResourceCollection');
  Activity.appendChild(ResourceCollection);

  if (activityInfo.type === "Activity_Unit") {

    let ToolBox = xmlDoc.createElement('ToolBox');
    Activity.appendChild(ToolBox);

    // Operation
    let OperationRecords = xmlDoc.createElement('OperationRecords');
    Activity.appendChild(OperationRecords);

    let TaskList = xmlDoc.createElement('TaskList');
    Activity.appendChild(TaskList);

    let TaskDependency = xmlDoc.createElement('TaskDependency');
    Activity.appendChild(TaskDependency);

  } else if (activityInfo.type === "Activity_Group") {

    let ChildActivities = xmlDoc.createElement('ChildActivities');
    Activity.appendChild(ChildActivities);

    let ActivityDependencies = xmlDoc.createElement('ActivityDependencies');
    Activity.appendChild(ActivityDependencies);

    // Operation
    let OperationRecords = xmlDoc.createElement('OperationRecords');
    Activity.appendChild(OperationRecords);
  }

  saveActivityDoc(activityInfo.aid);
}

// 重建活动文档
export function rebuildActivityDoc(activityInfo) {

  strDoc = `<Activity></Activity>`;
  loadActivityDoc();
  let Activity = xmlDoc.getElementsByTagName("Activity")[0];
  if (Activity == undefined) return;

  // attr
  Activity.setAttribute("id", activityInfo.aid);
  Activity.setAttribute("name", activityInfo.name);
  Activity.setAttribute("description", activityInfo.description);
  Activity.setAttribute("type", activityInfo.type);

  // Participants
  let Participants = xmlDoc.createElement('Participants');
  for (var i = 0; i < activityInfo.members.length; i++) {
    let Person = xmlDoc.createElement('Person');
    Person.setAttribute("id", activityInfo.members[i].userId);
    Person.setAttribute("name", activityInfo.members[i].name);
    Person.setAttribute("role", "");
    Person.setAttribute("state", "in");
    Participants.appendChild(Person);
  }
  Activity.appendChild(Participants);

  let ResourceCollection = xmlDoc.createElement('ResourceCollection');
  Activity.appendChild(ResourceCollection);

  if (activityInfo.type === "Activity_Unit") {


    let ToolBox = xmlDoc.createElement('ToolBox');
    Activity.appendChild(ToolBox);

    // Operation
    let OperationRecords = xmlDoc.createElement('OperationRecords');
    Activity.appendChild(OperationRecords);

    let TaskList = xmlDoc.createElement('TaskList');
    Activity.appendChild(TaskList);

    let TaskDependency = xmlDoc.createElement('TaskDependency');
    Activity.appendChild(TaskDependency);

  } else if (activityInfo.type === "Activity_Group") {

    // Child activity
    let ChildActivities = xmlDoc.createElement('ChildActivities');
    for (var i = 0; i < activityInfo.children.length; i++) {
      let Child = xmlDoc.createElement('Child');
      Child.setAttribute("id", activityInfo.children[i].aid);
      Child.setAttribute("name", activityInfo.children[i].aid);
      Child.setAttribute("creator", "");
      Child.setAttribute("state", "accessible");
      ChildActivities.appendChild(Child);
    }
    Activity.appendChild(ChildActivities);

    var ActivityDependencies = xmlDoc.createElement('ActivityDependencies');
    Activity.appendChild(ActivityDependencies);

    var OperationRecords = xmlDoc.createElement('OperationRecords');
    Activity.appendChild(OperationRecords);
  }

  updateActivityDoc(activityInfo.aid);
}

// inquiry——打开activity时
export function getActivityDoc(aid) {
  let result;
  $.ajax({
    url: "/GeoProblemSolving/activityDoc?aid=" + aid,
    type: "GET",
    async: false,
    success: function (data) {
      result = data;
    },
    error: function (err) {
      throw err;
    }
  });

  if (result.code === 0) {
    strDoc = result.data;
    loadActivityDoc();
    clearTempOperations(aid);
    return "success";
  } else if (result.code === -1) {
    return "empty";
  } else if (result.code === -2) {
    throw new Error(result.msg);
  }
}

export function getResInfo(resId) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let resNode = xmlDoc.getElementById(resId);
  if (resNode == null || resNode.localName != "Resource") return null;

  let resource = {}
  resource["id"] = resNode.getAttribute("id");
  resource["name"] = resNode.getAttribute("name") + resNode.getAttribute("format");
  resource["type"] = resNode.getAttribute("type");
  resource["description"] = resNode.getAttribute("description");
  resource["provider"] = resNode.getAttribute("provider");
  resource["href"] = resNode.getAttribute("href");

  //Metadata
  let MetadataList = resNode.childNodes;
  for (let i = 0; i < MetadataList.length; i++) {
    let type = MetadataList[i].getAttribute("type");
    if (type === "format"){
      resource["format"] = MetadataList[i].getAttribute("description");
    } else if (type === "scale") {
      resource["scale"] = MetadataList[i].getAttribute("description");
    } else if (type === "reference") {
      resource["reference"] = MetadataList[i].getAttribute("description");
    } else if (type === "unit") {
      resource["unit"] = MetadataList[i].getAttribute("description");
    } else if (type === "concept") {
      resource["concept"] = MetadataList[i].getAttribute("description");
    }
  }

  return resource;
}

//tempera
export function getResMetaInfo(aids) {
  let result;
  $.ajax({
    url: "/GeoProblemSolving/activityDoc/" + aids.toString(),
    type: "GET",
    async: false,
    success: function (data) {
      result = data;
    },
    error: function (err) {
      throw err;
    }
  })

  if (result.code == 0) {
    let docs = result.data;
    let formatTemp = [];
    let scaleTemp = [];
    let referenceTemp = [];
    let unitTemp = [];
    let conceptTemp = [];
    let resMetaInfo = {};
    let parser = new DOMParser();
    for (let i = 0; i < docs.length; i++) {
      let xmlDocument = parser.parseFromString(docs[i].document, "text/xml");
      let resNode = xmlDocument.getElementsByTagName("Resource");
      if (resNode == null || resNode.length == 0) {
        continue;
      }
      for (let j = 0; j < resNode.length; j++){
        let metaDtaList = resNode[j].childNodes;
        for (let k = 0; k < metaDtaList.length; k++) {
          let type = metaDtaList[k].getAttribute("type");
          let description = metaDtaList[k].getAttribute("description");
          if (type === "scale") {
            scaleTemp.push(description);
          } else if (type === "format") {
            formatTemp.push(description);
          } else if (type === "reference") {
            referenceTemp.push(description);
          } else if (type === "unit") {
            unitTemp.push(description);
          } else if (type === "concept") {
            conceptTemp.push(description);
          }
        }
      }
    }
    let format = Array.from(new Set(formatTemp)).filter(item=>{return item != ""});
    let scale = Array.from(new Set(scaleTemp)).filter(item=>{return item != ""});
    let reference = Array.from(new Set(referenceTemp)).filter(item=>{return item != ""});
    let unit = Array.from(new Set(unitTemp)).filter(item=>{return item != ""});
    let concept = Array.from(new Set(conceptTemp)).filter(item=>{return item != ""});
    resMetaInfo = {
      format: format.map(item=>{return {text: item}}),
      scale: scale.map(item=>{return {text: item}}),
      reference: reference.map(item=>{return {text: item}}),
      unit: unit.map(item=>{return {text: item}}),
      concept: concept.map(item=>{return {text: item}})
    };

    return resMetaInfo;
  } else {
    throw new Error(result.msg)
  }
}

export function getMemberInfo(pid) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let personNode = xmlDoc.getElementById(pid);
  if (personNode == null || personNode.localName != "Person") return null;

  let member = {}
  member["id"] = personNode.getAttribute("id");
  member["email"] = personNode.getAttribute("email");
  member["name"] = personNode.getAttribute("name");
  member["role"] = personNode.getAttribute("role");

  return member;
}

export function getToolInfo(tid) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let toolNode = xmlDoc.getElementById(tid);
  if (toolNode == null || toolNode.localName != "Tool") return null;

  let tool = {}
  tool["id"] = toolNode.getAttribute("id");
  tool["name"] = toolNode.getAttribute("name");
  tool["type"] = toolNode.getAttribute("type");
  tool["function"] = toolNode.getAttribute("function");
  tool["provider"] = toolNode.getAttribute("provider");
  tool["href"] = toolNode.getAttribute("href");

  return tool;
}

export function getOperationInfo(oid) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let operationNode = xmlDoc.getElementById(oid);
  if (operationNode == null || operationNode.localName != "Operation") return null;

  let operation = {};
  operation["id"] = operationNode.getAttribute("id");
  operation["type"] = operationNode.getAttribute("type");

  if (operationNode.getAttribute("type") === "resource") {

    operation["behavior"] = operationNode.getAttribute("behavior");
    operation["resRef"] = operationNode.getAttribute("resRef");
    operation["operator"] = operationNode.getAttribute("operator");
    operation["time"] = operationNode.getAttribute("time");

  } else if (operationNode.getAttribute("type") === "tool") {

    operation["behavior"] = operationNode.getAttribute("behavior");
    operation["toolRef"] = operationNode.getAttribute("toolRef");
    operation["operator"] = operationNode.getAttribute("operator");
    operation["time"] = operationNode.getAttribute("time");

  } else if (operationNode.getAttribute("type") === "communication") {

    operation["toolRef"] = operationNode.getAttribute("toolRef");
    operation["resRef"] = operationNode.getAttribute("resRef");
    operation["time"] = operationNode.getAttribute("time");

    // participants
    let participants = [];
    for (var j = 0; j < operationNode.childNodes.length; j++) {
      let person = operationNode.childNodes[j].getAttribute("idRef");
      participants.push(person);
    }
    operation["personRef"] = participants;

  } else if (operationNode.getAttribute("type") === "geo-analysis") {

    operation["toolRef"] = operationNode.getAttribute("toolRef");
    operation["operator"] = operationNode.getAttribute("operator");
    operation["time"] = operationNode.getAttribute("time");

    // resources
    let resources = {
      inputs: [],
      outputs: [],
      params: []
    };
    let participants = [];

    for (var j = 0; j < operationNode.childNodes.length; j++) {
      let children = operationNode.childNodes[j];
      if (children.getAttribute("type") === "input") {
        resources.inputs.push(children.getAttribute("idRef"));
      } else if (children.getAttribute("type") === "param") {
        resources.params.push(children.getAttribute("idRef"));
      } else if (children.getAttribute("type") === "output") {
        resources.outputs.push(children.getAttribute("idRef"));
      } else if (children.getAttribute("type") === "participant") {
        participants.push(children.getAttribute("idRef"));
      }
    }
    operation["resesRef"] = resources;
    operation["personRef"] = participants;
  }

  return operation;
}

export function getTaskInfo(taskId) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let taskDoc = xmlDoc.getElementById(taskId);
  if (taskDoc == undefined || taskDoc.localName != "Task") return null;

  let time = taskDoc.getAttribute("timerange").split(" - ");
  let taskInfo = {
    taskId: taskDoc.getAttribute("id"),
    name: taskDoc.getAttribute("name"),
    purpose: taskDoc.getAttribute("purpose"),
    startTime: time[0],
    endTime: time[1],
    operations: []
  }
  let operations = taskDoc.childNodes;
  for (var i = 0; i < operations.length; i++) {
    taskInfo.operations.push(operations[i].getAttribute("idRef"));
  }
  return taskInfo;
}

export function getTaskList() {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let taskDoc = xmlDoc.getElementsByTagName("Task");
  if (taskDoc == undefined) return [];

  let tasks = [];
  for (var i = 0; i < taskDoc.length; i++) {
    let time = taskDoc[i].getAttribute("timerange").split(" - ");

    let taskInfo = {
      taskId: taskDoc[i].getAttribute("id"),
      name: taskDoc[i].getAttribute("name"),
      purpose: taskDoc[i].getAttribute("purpose"),
      startTime: time[0],
      endTime: time[1],
      operations: []
    }
    if (taskDoc[i].getAttribute("state") === "removed") {
      continue;
    }
    let operations = taskDoc[i].childNodes;
    for (var j = 0; j < operations.length; j++) {
      taskInfo.operations.push(operations[j].getAttribute("idRef"));
    }
    tasks.push(taskInfo);
  }
  return tasks;
}

export function getTaskDependencies() {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let dependencyDoc = xmlDoc.getElementsByTagName("Relation");
  if (dependencyDoc == undefined) return [];

  let relations = [];
  for (var i = 0; i < dependencyDoc.length; i++) {
    let relation = {
      id: dependencyDoc[i].getAttribute("id"),
      name: dependencyDoc[i].getAttribute("name"),
      from: "",
      to: ""
    };
    for (var j = 0; j < dependencyDoc[i].childNodes.length; j++) {
      let linkNode = dependencyDoc[i].childNodes[j];
      if (linkNode.tagName == "From") {
        relation.from = linkNode.getAttribute("taskRef");
      } else if (linkNode.tagName == "To") {
        relation.to = linkNode.getAttribute("taskRef");
      }
    }
    relations.push(relation);
  }
  return relations;
}

export function getToollist() {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let toolsDoc = xmlDoc.getElementsByTagName("Tool");
  if (toolsDoc == undefined) return [];

  let tools = [];
  for (var i = 0; i < toolsDoc.length; i++) {
    if (toolsDoc[i].getAttribute("state") == "accessible") {
      tools.push(toolsDoc[i].getAttribute("id"));
    }
  }
  return tools;
}

export function getReslist() {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let resDoc = xmlDoc.getElementsByTagName("Resource");
  if (resDoc == undefined) return [];

  let resources = [];
  for (var i = 0; i < resDoc.length; i++) {
    resources.push(resDoc[i].getAttribute("id"));
  }
  return resources;
}

// independent operations(Temporary operations)
export function getTempOperations() {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let Operations = xmlDoc.getElementsByTagName("Operation");
  if (Operations == undefined) return [];

  let tempOperations = [];
  for (var i = 0; i < Operations.length; i++) {

    let operationNode = Operations[i];
    if (operationNode.getAttribute("task") === "") {

      let operation = {};
      operation["id"] = operationNode.getAttribute("id");
      operation["type"] = operationNode.getAttribute("type");

      if (operationNode.getAttribute("type") === "communication") {

        operation["toolRef"] = operationNode.getAttribute("toolRef");
        operation["resRef"] = operationNode.getAttribute("resRef");
        operation["time"] = operationNode.getAttribute("time");

        // participants
        let participants = [];
        for (var j = 0; j < operationNode.childNodes.length; j++) {
          let person = operationNode.childNodes[j].getAttribute("idRef");
          participants.push(person);
        }
        operation["personRef"] = participants;

      } else if (operationNode.getAttribute("type") === "geo-analysis") {

        operation["toolRef"] = operationNode.getAttribute("toolRef");
        operation["operator"] = operationNode.getAttribute("operator");
        operation["time"] = operationNode.getAttribute("time");

        // resources
        let resources = {
          inputs: [],
          outputs: [],
          params: []
        };
        let participants = [];

        for (var j = 0; j < operationNode.childNodes.length; j++) {
          let children = operationNode.childNodes[j];
          if (children.getAttribute("type") === "input") {
            resources.inputs.push(children.getAttribute("idRef"));
          } else if (children.getAttribute("type") === "param") {
            resources.params.push(children.getAttribute("idRef"));
          } else if (children.getAttribute("type") === "output") {
            resources.outputs.push(children.getAttribute("idRef"));
          } else if (children.getAttribute("type") === "participant") {
            participants.push(children.getAttribute("idRef"));
          }
        }
        operation["resesRef"] = resources;
        operation["personRef"] = participants;
      } else if (operationNode.getAttribute("type") === "resource") {

        operation["behavior"] = operationNode.getAttribute("behavior");
        operation["resRef"] = operationNode.getAttribute("resRef");
        operation["operator"] = operationNode.getAttribute("operator");
        operation["time"] = operationNode.getAttribute("time");
      } else if (operationNode.getAttribute("type") === "tool") {

        operation["behavior"] = operationNode.getAttribute("behavior");
        operation["toolRef"] = operationNode.getAttribute("toolRef");
        operation["operator"] = operationNode.getAttribute("operator");
        operation["time"] = operationNode.getAttribute("time");
      } else {
        continue;
      }
      tempOperations.push(operation);
    }
  }
  return tempOperations;
}

// save
function saveActivityDoc(id) {

  let serializer = new XMLSerializer();
  strDoc = serializer.serializeToString(xmlDoc);
  let jsonData = {
    "aid": id,
    "document": strDoc
  }

  $.ajax({
    url: "/GeoProblemSolving/activityDoc",
    type: "POST",
    dataType: "JSON",
    contentType: "application/json",
    data: JSON.stringify(jsonData),
    async: false,
    success: function (result) {
      if (result.code === 0) {
        return "success";
      } else {
        alert(result.msg);
      }
    },
    error: function (err) {
      throw err;
    }
  });
}

// update
// update activity document
function updateActivityDoc(aid) {

  let serializer = new XMLSerializer();
  strDoc = serializer.serializeToString(xmlDoc);
  let jsonData = {
    "aid": aid,
    "document": strDoc
  }

  $.ajax({
    url: "/GeoProblemSolving/activityDoc",
    type: "PUT",
    dataType: "JSON",
    contentType: "application/json",
    data: JSON.stringify(jsonData),
    async: false,
    success: function (result) {
      if (result.code === 0) {
        return "success";
      } else if (result.code === -1) {
        alert(result.msg);
      }
    },
    error: function (err) {
      throw err;
    }
  });
}

// acitivity——type: type/other
export function activityUpdate(updateType, activityInfo) {
  if (xmlDoc === null) {
    if (xmlDoc === null){
      alert("Failed to record operation. Please try it again!");
      return;
    }
  }

  let Activity = xmlDoc.getElementsByTagName('Activity')[0];
  Activity.setAttribute("name", activityInfo.name);
  Activity.setAttribute("description", activityInfo.description);
  Activity.setAttribute("type", activityInfo.type);

  if (updateType === "type") {
    try {
      // clear
      if (xmlDoc.getElementsByTagName('ChildActivities').length > 0) {

        let node = xmlDoc.getElementsByTagName('ChildActivities')[0];
        xmlDoc.documentElement.removeChild(node);

        node = xmlDoc.getElementsByTagName('ActivityDependencies')[0];
        xmlDoc.documentElement.removeChild(node);

        node = xmlDoc.getElementsByTagName('OperationRecords')[0];
        xmlDoc.documentElement.removeChild(node);

      } else if (xmlDoc.getElementsByTagName('ToolBox').length > 0) {

        let node = xmlDoc.getElementsByTagName('ToolBox')[0];
        xmlDoc.documentElement.removeChild(node);

        node = xmlDoc.getElementsByTagName('OperationRecords')[0];
        xmlDoc.documentElement.removeChild(node);

        node = xmlDoc.getElementsByTagName('TaskList')[0];
        xmlDoc.documentElement.removeChild(node);

        node = xmlDoc.getElementsByTagName('TaskDependency')[0];
        xmlDoc.documentElement.removeChild(node);
      }
      // add
      if (activityInfo.type === "Activity_Unit") {

        let ToolBox = xmlDoc.createElement('ToolBox');
        Activity.appendChild(ToolBox);

        let OperationRecords = xmlDoc.createElement('OperationRecords');
        Activity.appendChild(OperationRecords);

        let TaskList = xmlDoc.createElement('TaskList');
        Activity.appendChild(TaskList);

        let TaskDependency = xmlDoc.createElement('TaskDependency');
        Activity.appendChild(TaskDependency);

      } else if (activityInfo.type === "Activity_Group") {

        let ChildActivities = xmlDoc.createElement('ChildActivities');
        Activity.appendChild(ChildActivities);

        let ActivityDependencies = xmlDoc.createElement('ActivityDependencies');
        Activity.appendChild(ActivityDependencies);

        let OperationRecords = xmlDoc.createElement('OperationRecords');
        Activity.appendChild(OperationRecords);
      }

    } catch (err) {
      throw err;
    }

  }
  updateActivityDoc(activityInfo.aid);
}

export function taskUpdate(aid, behavior, taskInfo) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }
  let time1 = new Date(taskInfo.startTime);
  let time2 = new Date(taskInfo.endTime);
  let timerange = time1.Format("yyyy-MM-dd HH:mm:ss") + " - " + time2.Format("yyyy-MM-dd HH:mm:ss");

  // TaskList
  if (behavior === "create") {
    let Task = xmlDoc.getElementById(taskInfo.taskId);
    if (Task !== null && Task.localName == "Task") {
      Task.setAttribute("name", taskInfo.name);
      Task.setAttribute("purpose", taskInfo.description);
      Task.setAttribute("timerange", timerange);
      Task.setAttribute("state", "available");

    } else {
      Task = xmlDoc.createElement('Task');
      Task.setAttribute("id", taskInfo.taskId);
      Task.setAttribute("name", taskInfo.name);
      Task.setAttribute("purpose", taskInfo.description);
      Task.setAttribute("timerange", timerange);
      Task.setAttribute("state", "available");

      let TaskList = xmlDoc.getElementsByTagName('TaskList')[0];
      if (TaskList == undefined) return;
      TaskList.appendChild(Task);
    }
  } else if (behavior === "remove") {
    let Task = xmlDoc.getElementById(taskInfo.taskId);
    if (Task !== null && Task.localName == "Task") {
      Task.setAttribute("state", "removed");
    }

    // remove dependency
    let relations = xmlDoc.getElementsByTagName("Relation");
    for(var i = 0; i < relations.length; i++) {
      let Relation = relations[i];
      let relationId = Relation.getAttribute("id");
      if(relationId.indexOf(taskInfo.taskId) == 0 || relationId.indexOf(taskInfo.taskId) == 36){
        Relation.parentNode.removeChild(Relation);
      }
    }

    // remove operation in this task
    let operations = Task.childNodes;
    for (var i = 0; i < operations.length; i++) {

      let oid = operations[i].getAttribute("idRef");
      let operationNode = xmlDoc.getElementById(oid);
      if (operationNode != null && operationNode.localName != "Operation") {
        operationNode.setAttribute("task", "");
      }
    }

  } else if (behavior === "update") {
    let Task = xmlDoc.getElementById(taskInfo.taskId);
    if (Task != null && Task.localName == "Task") {
      Task.setAttribute("name", taskInfo.name);
      Task.setAttribute("purpose", taskInfo.description);
      Task.setAttribute("timerange", timerange);
    }
  }

  updateActivityDoc(aid);
}


export function taskDependencyRecord(aid, behavior, last, next) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let relationId = last + next;

  // TaskDependency
  if (behavior === "link") {

    let Relation = xmlDoc.createElement('Relation');
    Relation.setAttribute("id", relationId);
    Relation.setAttribute("name", "Task Dependency");
    Relation.setAttribute("state", "used");

    let From = xmlDoc.createElement('From');
    From.setAttribute("taskRef", last);
    Relation.appendChild(From);

    let To = xmlDoc.createElement('To');
    To.setAttribute("taskRef", next);
    Relation.appendChild(To);

    let TaskDependency = xmlDoc.getElementsByTagName("TaskDependency")[0];
    if (TaskDependency == undefined) return;
    TaskDependency.appendChild(Relation);

  } else if (behavior === "break") {
    let Relation = xmlDoc.getElementById(relationId);
    if (Relation !== null && Relation.localName == "Relation") {
      Relation.parentNode.removeChild(Relation);
    }
  }

  updateActivityDoc(aid);
}

export function participantUpdate(aid, behavior, userId, name, role, domains) {
  getActivityDoc(aid)
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  // Participants
  if (behavior === "join") {

    let Person = xmlDoc.getElementById(userId);
    if (Person !== null && Person.localName == "Person") {
      Person.setAttribute("name", name);
      Person.setAttribute("role", role);
      Person.setAttribute("state", "in");

      let Domains = Person.childNodes;
      for (let i = 0; i < Domains.length; i++) {
        Person.removeChild(Domains[i]);
      }
    } else {

      Person = xmlDoc.createElement('Person');
      Person.setAttribute("id", userId);
      Person.setAttribute("name", name);
      Person.setAttribute("role", role);
      Person.setAttribute("state", "in");

      let Participants = xmlDoc.getElementsByTagName('Participants')[0];
      if (Participants == undefined) return;
      Participants.appendChild(Person);
    }

    // add domains
    for (let j = 0; j < domains.length; j++) {
      let Domain = xmlDoc.createElement('Domain');
      Domain.setAttribute("description", domains[j]);
      Person.appendChild(Domain);
    }

  } else if (behavior === "remove") {
    let Person = xmlDoc.getElementById(userId);
    if (Person !== null && Person.localName == "Person") {
      Person.setAttribute("state", "out");
    }
  } else if (behavior === "role") {
    let Person = xmlDoc.getElementById(userId);
    if (Person !== null && Person.localName == "Person") {
      Person.setAttribute("role", role);
    }
  } else if (behavior === "add-domain") {
    for (let i = 0; i < domains.length; i++) {
      let Domain = xmlDoc.createElement('Domain');
      Domain.setAttribute("description", domains[i]);
      Person.appendChild(Domain);
    }
  } else if (behavior === "remove-domain") {
    let Domains = Person.childNodes;

    for (let i = 0; i < domains.length; i++) {
      for (let j = 0; j < Domains.length; j++) {
        if (Domains[j].getAttribute("description") === domains[i]) {
          Person.removeChild(Domains[j]);
        }
      }
    }
  }

  updateActivityDoc(aid);
}

// operation

export function bindTempOperation2Task(aid, operationId, taskId) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let Operation = xmlDoc.getElementById(operationId);

  if (Operation !== null && Operation.localName == "Operation") {
    Operation.setAttribute("task", taskId);
  } else {
    alert("Wrong operation id");
    return;
  }


  // TaskList
  let Task = xmlDoc.getElementById(taskId);
  if (Task !== null && Task.localName == "Task") {
    let OperationRef = xmlDoc.createElement('OperationRef');
    OperationRef.setAttribute("idRef", operationId);
    Task.appendChild(OperationRef);
    updateActivityDoc(aid);

  } else {
    alert("Wrong task id");
    return;
  }
}

export function resOperationRecord(aid, oid, taskId, behavior, userId, resInfo, metadata) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  //ResourceCollection
  if (behavior === "upload") {

    let Resource = xmlDoc.getElementById(resInfo.uid);
    if (Resource !== null && Resource.localName == "Resource") {
      Resource.setAttribute("name", resInfo.name);
      Resource.setAttribute("type", resInfo.type);
      // Resource.setAttribute("format", resInfo.suffix);
      Resource.setAttribute("description", resInfo.description);
      Resource.setAttribute("provider", resInfo.uploaderId);
      Resource.setAttribute("href", resInfo.address);
      Resource.setAttribute("state", "accessible");

      let MatedataList = Resource.childNodes;
      for (let i = 0; i < MatedataList.length; i++) {
        Resource.removeChild(MatedataList[i]);
      }

    } else {
      Resource = xmlDoc.createElement('Resource');
      Resource.setAttribute("id", resInfo.uid);
      Resource.setAttribute("name", resInfo.name);
      Resource.setAttribute("type", resInfo.type);
      Resource.setAttribute("format", resInfo.suffix);
      Resource.setAttribute("description", resInfo.description);
      Resource.setAttribute("provider", resInfo.provider);
      Resource.setAttribute("href", resInfo.address);
      Resource.setAttribute("state", "accessible");

      let ResourceCollection = xmlDoc.getElementsByTagName("ResourceCollection")[0];
      if (ResourceCollection == undefined) return;
      ResourceCollection.appendChild(Resource);
    }
    // matedata
    if (metadata != undefined && metadata != {}) {
      if (metadata.format != undefined && metadata.format != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "format");
        Metadata.setAttribute("description", metadata.format);
        Resource.appendChild(Metadata);

      }
      if (metadata.scale != undefined && metadata.scale != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "scale");
        Metadata.setAttribute("description", metadata.scale);
        Resource.appendChild(Metadata);

      }
      if (metadata.reference != undefined && metadata.reference != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "reference");
        Metadata.setAttribute("description", metadata.reference);
        Resource.appendChild(Metadata);

      }
      if (metadata.unit != undefined && metadata.unit != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "unit");
        Metadata.setAttribute("description", metadata.unit);
        Resource.appendChild(Metadata);

      }
      if (metadata.concept != undefined && metadata.concept != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "concept");
        Metadata.setAttribute("description", metadata.concept);
        Resource.appendChild(Metadata);
      }
    }
  } else if (behavior === "update") {
    let Resource = xmlDoc.getElementById(resInfo.uid);
    if (Resource == undefined || Resource.localName != "Resource") {
      Resource = xmlDoc.createElement("Resource");
      Resource.setAttribute("id", resInfo.uid);
    }
    Resource.setAttribute("name", resInfo.name);
    Resource.setAttribute("type", resInfo.type);
    // Resource.setAttribute("format", resInfo.suffix);
    Resource.setAttribute("description", resInfo.description);
    Resource.setAttribute("provider", resInfo.uploaderId);
    Resource.setAttribute("href", resInfo.address);
    Resource.setAttribute("state", "accessible");
    if (metadata != undefined && metadata != {}) {
      let MatedataList = Resource.childNodes;
      for (let i = 0; i < MatedataList.length; i++) {
        Resource.removeChild(MatedataList[i]);
      }
      if (metadata.format != undefined && metadata.format != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "format");
        Metadata.setAttribute("description", metadata.format);
        Resource.appendChild(Metadata);
      }
      if (metadata.scale != undefined && metadata.scale != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "scale");
        Metadata.setAttribute("description", metadata.scale);
        Resource.appendChild(Metadata);
      }
      if (metadata.reference != undefined && metadata.reference != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "reference");
        Metadata.setAttribute("description", metadata.reference);
        Resource.appendChild(Metadata);
      }
      if (metadata.unit != undefined && metadata.unit != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "unit");
        Metadata.setAttribute("description", metadata.unit);
        Resource.appendChild(Metadata);
      }
      if (metadata.concept != undefined && metadata.concept != "") {
        let Metadata = xmlDoc.createElement('Metadata');
        Metadata.setAttribute("type", "concept");
        Metadata.setAttribute("description", metadata.concept);
        Resource.appendChild(Metadata);
      }
    }

  } else if (behavior === "remove") {
    let Resource = xmlDoc.getElementById(resInfo.uid);
    if (Resource !== null && Resource.localName == "Resource") {
      Resource.setAttribute("state", "removed");
    }
  }

  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  Operation = xmlDoc.createElement('Operation');
  Operation.setAttribute("id", operationId);
  Operation.setAttribute("type", "resource");
  Operation.setAttribute("behavior", behavior);
  Operation.setAttribute("resRef", resInfo.uid);
  Operation.setAttribute("operator", userId);
  Operation.setAttribute("task", taskId);
  Operation.setAttribute("time", new Date().Format("yyyy-MM-dd HH:mm:ss"));
  OperationRecords.appendChild(Operation);

  // TaskList
  let Task = xmlDoc.getElementById(taskId);
  if (Task !== null && Task.localName == "Task") {
    let OperationRef = xmlDoc.createElement('OperationRef');
    OperationRef.setAttribute("idRef", operationId);
    Task.appendChild(OperationRef);
  }

  updateActivityDoc(aid);
  return operationId;
}

export function toolOperationRecord(aid, oid, taskId, behavior, userId, toolInfo) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  //ToolBox
  if (behavior === "add") {
    let Tool = xmlDoc.getElementById(toolInfo.tid);
    if (Tool === null || Tool.localName != "Tool") {
      Tool = xmlDoc.createElement('Tool');
      Tool.setAttribute("id", toolInfo.tid);
      Tool.setAttribute("name", toolInfo.toolName);
      Tool.setAttribute("toolSet", toolInfo.toolSet);
      if (!toolInfo.toolSet && toolInfo.toolSetId != undefined) {
        Tool.setAttribute("toolSetRef", toolInfo.toolSetId)
      }
      Tool.setAttribute("function", toolInfo.description);
      Tool.setAttribute("provider", toolInfo.provider);
      Tool.setAttribute("href", toolInfo.toolUrl);
      Tool.setAttribute("state", "accessible");

      let ToolBox = xmlDoc.getElementsByTagName("ToolBox")[0];
      if (ToolBox == undefined) {
        let activityElement = xmlDoc.getElementById(aid);
        let activityType = activityElement.getAttribute("type");
        if (activityType == "Activity_Unit") {
          ToolBox = xmlDoc.createElement("ToolBox");
          activityElement.appendChild(ToolBox);
        }
      }
      ToolBox.appendChild(Tool);
    } else {
      //修改
      Tool.setAttribute("name", toolInfo.toolName);
      Tool.setAttribute("toolSet", toolInfo.toolSet);
      if (!toolInfo.toolSet && toolInfo.toolSetId != undefined) {
        Tool.setAttribute("toolSetRef", toolInfo.toolSetId)
      }
      Tool.setAttribute("function", toolInfo.description);
      Tool.setAttribute("provider", toolInfo.provider);
      Tool.setAttribute("href", toolInfo.toolUrl);
      Tool.setAttribute("state", "accessible");
    }
  } else if (behavior === "remove") {
    let Tool = xmlDoc.getElementById(toolInfo.tid);
    if (Tool !== null && Tool.localName == "Tool") {
      Tool.setAttribute("state", "removed");
    }
  }

  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) {
    let activityElement = xmlDoc.getElementById("Activity");
    OperationRecords = xmlDoc.createElement("OperationRecords");
    activityElement.appendChild(OperationRecords);
  }
  ;

  //生成 operation
  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  Operation = xmlDoc.createElement('Operation');
  Operation.setAttribute("id", operationId);
  Operation.setAttribute("type", "tool");
  Operation.setAttribute("behavior", behavior);
  Operation.setAttribute("toolRef", toolInfo.tid);
  Operation.setAttribute("operator", userId);
  Operation.setAttribute("task", taskId);
  Operation.setAttribute("time", new Date().Format("yyyy-MM-dd HH:mm:ss"));
  OperationRecords.appendChild(Operation);

  // TaskList
  if (taskId != "") {
    let Task = xmlDoc.getElementById(taskId);
    if (Task !== null && Task.localName == "Task") {
      let OperationRef = xmlDoc.createElement('OperationRef');
      OperationRef.setAttribute("idRef", operationId);
      Task.appendChild(OperationRef);
    }
  }

  updateActivityDoc(aid);
  return operationId;
}

export function communicationRecord(activity, oid, taskId, toolId, resId, time, onlineMembers) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let aid = activity.aid;

  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  if (activity.type === "Activity_Group") {

    Operation = xmlDoc.createElement('Operation');
    Operation.setAttribute("id", operationId);
    Operation.setAttribute("type", "communication");
    Operation.setAttribute("resRef", resId);
    let date = new Date(time);
    Operation.setAttribute("time", date.Format("yyyy-MM-dd HH:mm:ss"));
    for (var i = 0; i < onlineMembers.length; i++) {
      let PersonRef = xmlDoc.createElement('PersonRef');
      PersonRef.setAttribute("idRef", onlineMembers[i]);
      Operation.appendChild(PersonRef);
    }
    OperationRecords.appendChild(Operation);

  } else if (activity.type === "Activity_Unit") {

    Operation = xmlDoc.createElement('Operation');
    Operation.setAttribute("id", operationId);
    Operation.setAttribute("type", "communication");
    Operation.setAttribute("toolRef", toolId);
    Operation.setAttribute("resRef", resId);
    Operation.setAttribute("task", taskId);
    let date = new Date(time);
    Operation.setAttribute("time", date.Format("yyyy-MM-dd HH:mm:ss"));
    for (var i = 0; i < onlineMembers.length; i++) {
      let PersonRef = xmlDoc.createElement('PersonRef');
      PersonRef.setAttribute("idRef", onlineMembers[i]);
      Operation.appendChild(PersonRef);
    }
    OperationRecords.appendChild(Operation);

    // TaskList
    let Task = xmlDoc.getElementById(taskId);
    if (Task !== null && Task.localName == "Task") {
      let OperationRef = xmlDoc.createElement('OperationRef');
      OperationRef.setAttribute("idRef", operationId);
      Task.appendChild(OperationRef);
    }
  }
  updateActivityDoc(aid);
  return operationId;
}

export function analysisRecord(aid, oid, taskId, userId, toolId, purpose, inputs, outputs, params, participants) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  // save output data
  let ResourceCollection = xmlDoc.getElementsByTagName("ResourceCollection")[0];
  if (ResourceCollection == undefined) return;

  for (var i = 0; i < outputs.length; i++) {
    let Resource = xmlDoc.createElement('Resource');
    Resource.setAttribute("id", outputs[i].uid);
    Resource.setAttribute("name", outputs[i].name);
    Resource.setAttribute("type", outputs[i].type);
    Resource.setAttribute("format", outputs[i].suffix);
    Resource.setAttribute("description", outputs[i].description);
    Resource.setAttribute("provider", outputs[i].provider);
    Resource.setAttribute("href", outputs[i].address);
    Resource.setAttribute("state", "accessible");

    ResourceCollection.appendChild(Resource);
  }


  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  Operation = xmlDoc.createElement('Operation');
  Operation.setAttribute("id", operationId);
  Operation.setAttribute("type", "geo-analysis");
  Operation.setAttribute("toolRef", toolId);
  Operation.setAttribute("operator", userId);
  Operation.setAttribute("task", taskId);
  Operation.setAttribute("purpose", purpose);
  Operation.setAttribute("time", new Date().Format("yyyy-MM-dd HH:mm:ss"));
  for (var i = 0; i < inputs.length; i++) {
    let ResRef = xmlDoc.createElement('ResRef');
    ResRef.setAttribute("idRef", inputs[i].uid);
    ResRef.setAttribute("type", "input");
    Operation.appendChild(ResRef);
  }
  for (var i = 0; i < outputs.length; i++) {
    let ResRef = xmlDoc.createElement('ResRef');
    ResRef.setAttribute("idRef", outputs[i].uid);
    ResRef.setAttribute("type", "output");
    Operation.appendChild(ResRef);
  }
  for (var i = 0; i < params.length; i++) {
    let ResRef = xmlDoc.createElement('ResRef');
    ResRef.setAttribute("idRef", params[i].uid);
    ResRef.setAttribute("type", "param");
    Operation.appendChild(ResRef);
  }
  for (var i = 0; i < participants.length; i++) {
    let PersonRef = xmlDoc.createElement('PersonRef');
    PersonRef.setAttribute("idRef", participants[i].userId);
    PersonRef.setAttribute("type", "participant");
    Operation.appendChild(PersonRef);
  }
  OperationRecords.appendChild(Operation);

  // TaskList
  let Task = xmlDoc.getElementById(taskId);
  if (Task !== null && Task.localName == "Task") {
    let OperationRef = xmlDoc.createElement('OperationRef');
    OperationRef.setAttribute("idRef", operationId);
    Task.appendChild(OperationRef);
  }

  updateActivityDoc(aid);
  return operationId;
}

export function analysisRecordUpdate(aid, oid, outputs) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }


  // save output data
  let ResourceCollection = xmlDoc.getElementsByTagName("ResourceCollection")[0];
  if (ResourceCollection == undefined) return;

  for (var i = 0; i < outputs.length; i++) {
    let Resource = xmlDoc.createElement('Resource');
    Resource.setAttribute("id", outputs[i].uid);
    Resource.setAttribute("name", outputs[i].name);
    Resource.setAttribute("type", outputs[i].type);
    Resource.setAttribute("format", outputs[i].suffix);
    Resource.setAttribute("description", outputs[i].description);
    Resource.setAttribute("provider", outputs[i].provider);
    Resource.setAttribute("href", outputs[i].address);
    Resource.setAttribute("state", "accessible");

    ResourceCollection.appendChild(Resource);
  }


  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation == undefined) return;

  // update operation record
  for (var i = 0; i < outputs.length; i++) {
    let ResRef = xmlDoc.createElement('ResRef');
    ResRef.setAttribute("idRef", outputs[i].uid);
    ResRef.setAttribute("type", "output");
    Operation.appendChild(ResRef);
  }
  OperationRecords.appendChild(Operation);


  updateActivityDoc(aid);
  return operationId;
}

// Child Activity
export function activityRecord(oid, behavior, userId, childInfo) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  // ChildActivities
  if (behavior === "create") {

    let Child = xmlDoc.getElementById(childInfo.aid);
    if (Child === null || Child.localName != "Child") {

      Child = xmlDoc.createElement('Child');
      Child.setAttribute("id", childInfo.aid);
      Child.setAttribute("name", childInfo.name);
      Child.setAttribute("creator", childInfo.creator);
      Child.setAttribute("state", "accessible");

      let ChildActivities = xmlDoc.getElementsByTagName("ChildActivities")[0];
      if (ChildActivities == undefined) return;
      ChildActivities.appendChild(Child);
    } else {
      Child.setAttribute("name", childInfo.name);
      Child.setAttribute("creator", childInfo.creator);
      Child.setAttribute("state", "accessible");
    }

  } else if (behavior === "remove") {
    ////////
    // save parent activity document
    getActivityDoc(childInfo.parent);

    let Child = xmlDoc.getElementById(childInfo.aid);
    if (Child !== null && Child.localName == "Child") {
      Child.setAttribute("state", "removed");
    }
  }

  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  Operation = xmlDoc.createElement('Operation');
  Operation.setAttribute("id", operationId);
  Operation.setAttribute("type", "activity");
  Operation.setAttribute("behavior", behavior);
  Operation.setAttribute("operator", userId);
  Operation.setAttribute("time", new Date().Format("yyyy-MM-dd HH:mm:ss"));
  OperationRecords.appendChild(Operation);


  updateActivityDoc(childInfo.parent);
}

export function processRecord(aid, oid, behavior, userId, last, next, protocalId) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  // ActivityDependencies
  let relationId = guid();

  if (behavior === "link") {

    let Relation = xmlDoc.createElement('Relation');
    Relation.setAttribute("id", relationId);
    Relation.setAttribute("name", "ActivityDependency");
    Relation.setAttribute("protocol", protocalId);
    Relation.setAttribute("state", "used");
    let From = xmlDoc.createElement('From');
    From.setAttribute("childRef", last);
    Relation.appendChild(From);
    let To = xmlDoc.createElement('To');
    To.setAttribute("childRef", next);
    Relation.appendChild(To);

    let ActivityDependencies = xmlDoc.getElementsByTagName("ActivityDependencies")[0];
    if (ActivityDependencies == undefined) return;
    ActivityDependencies.appendChild(Relation);

  } else if (behavior === "break") {
    let Relation = xmlDoc.getElementById(relationId);
    if (Relation !== null && Relation.localName == "Relation") {
      Relation.setAttribute("state", "removed");
    }
  }

  //OperationRecords
  let OperationRecords = xmlDoc.getElementsByTagName("OperationRecords")[0];
  if (OperationRecords == undefined) return;

  let operationId = (oid === "") ? guid() : oid;
  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") return;

  // create operation record
  Operation = xmlDoc.createElement('Operation');
  Operation.setAttribute("id", operationId);
  Operation.setAttribute("type", "process");
  Operation.setAttribute("behavior", behavior);
  Operation.setAttribute("dependency", relationId);
  Operation.setAttribute("operator", userId);
  Operation.setAttribute("time", new Date().Format("yyyy-MM-dd HH:mm:ss"));
  OperationRecords.appendChild(Operation);


  updateActivityDoc(aid);
}

// delete
export function deleteActivityDoc(aid) {
  $.ajax({
    url: "/GeoProblemSolving/activityDoc?aid=" + aid,
    type: "DELETE",
    async: false,
    success: function (result) {
      if (result.code == 0) {
        return "success";
      } else {
        alert(result.msg);
      }
    },
    error: function (err) {
      throw err;
    }
  });
}

export function deleteOperation(aid, operationId) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let Operation = xmlDoc.getElementById(operationId);
  if (Operation !== null && Operation.localName == "Operation") {
    Operation.parentNode.removeChild(Operation);
  }

  updateActivityDoc(aid);
}

// remove independent operations(Temporary operations) > 48h
function clearTempOperations(aid) {
  if (xmlDoc === null) {
    alert("Failed to record operation. Please try it again!");
    return;
  }

  let Operations = xmlDoc.getElementsByTagName("Operation");
  if (Operations == undefined) return [];

  let isRemoved = false;
  for (var i = Operations.length - 1; i >= 0; i--) {

    let operationNode = Operations[i];
    if (operationNode.getAttribute("task") === "") {

      let time = new Date(operationNode.getAttribute("time"));
      let current = new Date();
      let threshold = 1000 * 60 * 60 * 48;
      if (current - time > threshold) {
        operationNode.parentNode.removeChild(operationNode);
        isRemoved = true;
      }
    }
  }
  if (isRemoved) {
    updateActivityDoc(aid);
  }
}



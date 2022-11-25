package cn.edu.njnu.geoproblemsolving.business.collaboration.service;

import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServe;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.CommunicationCache;
import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.ComputeTasks;
import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.OperationQueue;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ChatMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.CollaborationUser;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ComputeMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.collaboration.enums.CollaborationMode;
import cn.edu.njnu.geoproblemsolving.business.collaboration.utils.CollaborationBehavior;
import cn.edu.njnu.geoproblemsolving.business.collaboration.utils.CollaborationConfig;
import cn.edu.njnu.geoproblemsolving.business.resource.dao.ActivityResDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.AnsjSegService;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Service
public class CollaborationService {

    @Autowired
    UserDao iUserDao;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    AnsjSegService ansjSegService;

    @Autowired
    CollaborationBehavior collaborationBehavior;

    @Autowired
    CommunicationCache communicationCache;

    @Autowired
    OperationQueue operationQueue;

    @Autowired
    ComputeTasks computeTasks;

    @Autowired
    ActivityResDaoImpl ripDao;

    @Autowired
    GeoAnalysisProcess geoAnalysisProcess;

    @Autowired
    NodeService nodeService;


    @Autowired
    DocParseServe docParseServe;

    @Value("${managerServerIpAndPort}")
    String mangeServiceLocation;

    @Value("${dataMethodProxyServerLocation}")
    String dataProxyServer;

    //global static
    private static final Map<String, CollaborationConfig> groups = new ConcurrentHashMap<>(); // collaboration groups

    public void msgStart(String groupKey, Session session, EndpointConfig config) {
        CollaborationConfig collaborationConfig;
        try {
            //Check if the session existence
            if (groups.containsKey(groupKey)) {
                collaborationConfig = groups.get(groupKey);
            } else {
                collaborationConfig = new CollaborationConfig(groupKey);
                groups.put(groupKey, collaborationConfig);
            }

            // current sender
            String userId = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("userId").toString();
            CollaborationUser collaborationUser = collaborationBehavior.getMemberInfo(userId, session);

            // current participants
            HashMap<String, CollaborationUser> participants;
            if (collaborationConfig.getParticipants() == null) {
                participants = new HashMap<>();
            } else {
                participants = collaborationConfig.getParticipants();
            }
            participants.put(userId, collaborationUser);
            collaborationConfig.setParticipants(participants);
            groups.put(groupKey, collaborationConfig);

            // 发布缓存信息
            if (participants.size() > 1) {
                if (communicationCache.getCache(groupKey) != null && communicationCache.getCache(groupKey).size() > 0) {
                    collaborationBehavior.sendMessageCache(communicationCache.getCache(groupKey), session);
                }
                // 通知新成员上线，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "on");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void operationStart(String groupKey, Session session, EndpointConfig config) {
        CollaborationConfig collaborationConfig;
        // System.out.println("0---Operation On Open----In user------: " + ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("name"));
        // System.out.println("1-----Operation on open-----groupKey-------:" + groupKey);
        try {
            //Check if the session existence.
            if (groups.containsKey(groupKey)) {
                collaborationConfig = groups.get(groupKey);
                // System.out.println("2.1-Operation On Open---Exists Group------:" + collaborationConfig.getAid());
            } else {
                collaborationConfig = new CollaborationConfig(groupKey);
                groups.put(groupKey, collaborationConfig);
                // System.out.println("2.1--Operation On Open--New Group------:" + collaborationConfig.getAid());
            }


            // current operator
            String userId = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("userId").toString();
            CollaborationUser collaborationUser = collaborationBehavior.getMemberInfo(userId, session);

            // current participants
            HashMap<String, CollaborationUser> participants;
            if (collaborationConfig.getParticipants() == null) {
                participants = new HashMap<>();
                // System.out.println("3.1--Operation On Open---This is no people. -----");
            } else {
                participants = collaborationConfig.getParticipants();

                // String name = "";
                // for (Map.Entry<String, CollaborationUser> item : participants.entrySet()){
                //     CollaborationUser value = item.getValue();
                //     name += ":" + value.getName();
                // }
                // System.out.println("3.1--Operation On Open--Exists participants----" + name);
            }
            participants.put(userId, collaborationUser);
            collaborationConfig.setParticipants(participants);
            // groups.put(groupKey, collaborationConfig);

            if (participants.size() > 1) {
                // 通知新成员上线，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "on");

                // 发布当前协同操作模式
                collaborationBehavior.sendCollaborationStatus(collaborationConfig, session);
            } else if (participants.size() == 1) {
                // 初始化协同操作模式
                collaborationConfig.setMode(CollaborationMode.Free);
                collaborationConfig.setOperator("");
                collaborationConfig.setApplyQueue(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void msgTransfer(String groupKey, String message) {
        CollaborationConfig collaborationConfig;
        try {
            collaborationConfig = groups.get(groupKey);

            JSONObject messageObject = JSONObject.parseObject(message);
            String messageType = messageObject.getString("type");
            if (messageType.equals("ping")) return;

            String user = messageObject.getString("sender");
            CollaborationUser sender = collaborationBehavior.getMemberInfo(user, null);

            String time = messageObject.getString("time");

            List<String> receivers;
            try {
                receivers = messageObject.getJSONArray("receivers").toJavaList(String.class);
            } catch (NullPointerException ex) {
                receivers = new ArrayList<>();
            }

            switch (messageType) {
                case "test": {
                    collaborationConfig.setMode(CollaborationMode.Free);
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.transferMessage(messageType, collaborationConfig.getParticipants(), sender, receivers, "test", time);
                    break;
                }
                case "message":
                case "message_pic":
                case "message_tool": {
                    String text = messageObject.getString("content");
                    collaborationBehavior.transferMessage(messageType, collaborationConfig.getParticipants(), sender, receivers, text, time);

                    Boolean concepts = messageObject.getBooleanValue("geoConcepts");
                    String relateConceptSet = "";
                    if (concepts != null && concepts) {
                        String result = ansjSegService.processInfo(text);
                        relateConceptSet = ansjSegService.elasticSearch(result);
                    }
                    if (!relateConceptSet.equals("")) {
                        collaborationBehavior.transferConceptMessage(collaborationConfig.getParticipants(), sender, receivers, relateConceptSet, time);
                    }

                    // 添加消息至缓存
                    if (collaborationConfig.getParticipants().size() > 1) {
                        ChatMsg chatMsg = new ChatMsg();
                        ArrayList<ChatMsg> chatMsgRecords = communicationCache.getCache(groupKey);
                        if (chatMsgRecords == null) chatMsgRecords = new ArrayList<>();

                        chatMsg.setMessageId(UUID.randomUUID().toString());
                        chatMsg.setAid(groupKey);
                        chatMsg.setSender(sender);
                        chatMsg.setReceiver(receivers);
                        chatMsg.setContent(text);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        chatMsg.setTime(dateFormat.parse(time));
                        chatMsgRecords.add(chatMsg);
                        communicationCache.putCache(groupKey, chatMsgRecords);
                    }

                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Message Websocket Error_Transfer");
            ex.printStackTrace();
        }
    }

    public void operationTransfer(String toolId, String aid, String message) {
        CollaborationConfig collaborationConfig;
        try {
            String groupKey = toolId + aid;


            // System.out.println("1---transfer---groupId: " +  groupKey);
            collaborationConfig = groups.get(groupKey);


            // String name = "";
            // for (Map.Entry<String, CollaborationUser> item : collaborationConfig.getParticipants().entrySet()){
            //     CollaborationUser value = item.getValue();
            //     name += ":" + value.getName();
            // }
            //
            // System.out.println("1---Operation Transfer---Exists participant: " +  name);
            // System.out.println("1---Operation Transfer---Participant num: " +  collaborationConfig.getParticipants().size());

            JSONObject messageObject = JSONObject.parseObject(message);
            String messageType = messageObject.getString("type");

            // System.out.println("3---transfer---messageType:" + messageType);
            // System.out.println("4---transfer---sender: " +  messageObject.getString("sender"));

            if (messageType.equals("ping")) return;

            String user = messageObject.getString("sender");
            CollaborationUser sender =  collaborationBehavior.getMemberInfo(user, null);

            List<String> receivers;
            try {
                receivers = messageObject.getJSONArray("receivers").toJavaList(String.class);
            } catch (NullPointerException ex) {
                receivers = new ArrayList<>();
            }

            switch (messageType) {
                case "test": {
                    JSONObject collaboration = new JSONObject();
                    if(collaborationConfig.getMode() == null) {
                        collaborationConfig.setMode(CollaborationMode.Free);
                        collaborationConfig.setOperator("");
                        collaborationConfig.setApplyQueue(new ArrayList<>());
                        groups.put(groupKey, collaborationConfig);
                    }
                    collaboration.put("mode", collaborationConfig.getMode());
                    collaboration.put("operator", collaborationConfig.getOperator());
                    collaboration.put("waiting", collaborationConfig.getApplyQueue().size());

                    collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), messageType, sender, receivers, "test", collaboration.toJSONString());
                    break;
                }
                case "mode": {
                    String mode = messageObject.getString("content");
                    collaborationConfig.setMode(CollaborationMode.valueOf(mode));
                    collaborationConfig.setOperator("");
                    collaborationConfig.setApplyQueue(new ArrayList<>());
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.sendModeType(collaborationConfig.getParticipants(), mode);
                    break;
                }
                case "control-apply": {
                    List<String> applyQueue = collaborationConfig.getApplyQueue();
                    if(applyQueue == null) applyQueue = new ArrayList<>();

                    if(collaborationConfig.getOperator().equals("")) {
                        collaborationConfig.setOperator(sender.getUserId());
                    } else {
                        if(!applyQueue.contains(sender.getUserId())){
                            applyQueue.add(sender.getUserId());
                            collaborationConfig.setApplyQueue(applyQueue);
                        }
                    }

                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.sendControlInfo(collaborationConfig, applyQueue, sender, messageType);

                    break;
                }
                case "control-stop": {
                    collaborationCtrChange(collaborationConfig, groupKey, sender, messageType);
                    break;
                }
                case "resource":
                //    operation content字段用于存储需要转发的信息，统一使用String
                case "operation": {
                    String behavior = messageObject.getString("behavior");
                    String object = messageObject.getString("content");
                    // 操作权限判断，冲突判断
                    CollaborationMode mode = collaborationConfig.getMode();
                    if (mode.equals(CollaborationMode.SemiFree_Apply)) {
                        // 判断操作权限
                        if(collaborationConfig.getOperator().equals(sender.getUserId())){
                            collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), messageType, sender, receivers, behavior, object);
                        } else {
                            collaborationBehavior.operationRefuse(collaborationConfig.getParticipants(), messageType, sender.getUserId());
                        }
                    } else if (mode.equals(CollaborationMode.SemiFree_Occupy)) {
                        // 抢占操作权
                        if(collaborationConfig.getOperator().equals("")){
                            collaborationConfig.setOperator(sender.getUserId());
                            groups.put(groupKey, collaborationConfig);
                        }
                        // 判断操作权限
                        if(collaborationConfig.getOperator().equals(sender.getUserId())){
                            collaborationConfig.resetTime();
                            collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), messageType, sender, receivers, behavior, object);
                        } else {
                            if(collaborationConfig.resetOperator(sender.getUserId())){
                                // reset operator successfully
                                collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), messageType, sender, receivers, behavior, object);
                            } else {
                                // fail to reset operator
                                collaborationBehavior.operationRefuse(collaborationConfig.getParticipants(), messageType, sender.getUserId());
                            }
                        }
                    } else if (mode.equals(CollaborationMode.Free)) {
                        collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), messageType, sender, receivers, behavior, object);
                    }

                    break;
                }
                case "computation": {
                    //初始化一个计算信息
                    ComputeMsg computeMsg = new ComputeMsg();
                    //读取当前 group 下的计算队列
                    HashMap<String, ComputeMsg> computeRecords = computeTasks.getCache(groupKey);
                    if (computeRecords == null) computeRecords = new HashMap<>();

                    Boolean isComputeModel = messageObject.getBooleanValue("computeAbleModel");
                    // computeMsg.setOutputs(outputs);

                    computeMsg.setAid(aid);
                    computeMsg.setToolId(toolId);
                    //此时的 receivers 是当前参与协作的人，当他们体现后 websocket下是否能将通知信息发送到他们账号上？
                    computeMsg.setReceivers(collaborationConfig.getParticipants());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    computeMsg.setTime(dateFormat.format(new Date()));


                    RestTemplate restTemplate = new RestTemplate();
                    String graphId = messageObject.getString("graphId");

                    HashSet<String> outResIds = new HashSet<>();
                    HashSet<String> inResIds = new HashSet<>();
                    ArrayList<ResourceEntity> outputRes = new ArrayList<>();
                    if (isComputeModel) {
                    /*
                     1. 根据模型 md5 获取合适的 taskService
                     2. invoke
                     3. 获取 taskId, 将 computeMsg 存入缓存
                     4. 采用短轮询方式获取模型运行状态
                     5. 模型运行成功或失败，taskId 读取缓存中的计算任务
                        a. 运行成功将输出存入项目资源
                        b. 运行失败，将错误信息通知到参与协同的人（使用message接口）
                     6. 从当前在线的人中寻找 invoke 时的人，将模型运行结果通知到在线用户
                     */
                        // "http://172.21.212.167:8084/GeoModeling/computableModel/invoke"
                        //设置 invoke 相关参数然后，invoke 起来
                        //从 message 获取所需参数 ip:port, inputs, outputs
                        String serviceIp = messageObject.getString("serviceIp");
                        String servicePort = messageObject.getString("servicePort");
                        String serviceMd5 = messageObject.getString("serviceMd5");
                        JSONArray inputs = messageObject.getJSONArray("inputs");

                        computeMsg.setServiceIp(serviceIp);
                        computeMsg.setServicePort(servicePort);
                        computeMsg.setServiceId(serviceMd5);
                        computeMsg.setInputs(inputs);

                        String invokeUrl = "http://" + mangeServiceLocation + "/GeoModeling/computableModel/invoke";
                        JSONObject invokeJson = new JSONObject();
                        invokeJson.put("inputs", inputs);
                        invokeJson.put("ip", serviceIp);
                        invokeJson.put("port", servicePort);
                        //这个参数必须要就离谱
                        invokeJson.put("outputs", Lists.newArrayList());
                        //md5 值，直接前端传过来好了
                        invokeJson.put("pid", serviceMd5);
                        //invokeName
                        invokeJson.put("username", sender.getName());

                        //invoke 起来获得taskId，由此可将计算任务存入队列
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(APPLICATION_JSON);
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(invokeJson, headers);
                        ResponseEntity<JSONObject> exchange = restTemplate.exchange(invokeUrl, HttpMethod.POST, httpEntity, JSONObject.class);
                        String taskId = exchange.getBody().getJSONObject("data").getString("tid");

                        computeMsg.setTaskId(taskId);
                        //将计算任务存入，存入队列
                        computeRecords.put(taskId, computeMsg);
                        /*
                        这里的处理应该是如果有这个groupKey = toolId + aid;
                        就去除 computeRecord，然后将新的任务添加进去
                        groupKey 这样设计的话，每个 computeRecords 存储的是单个活动的单个工具的调用情况
                         */
                        /*
                         计算任务存入与groupKey 绑定
                         似乎有点多此一举，但从这里来看
                         将groupKey 计算任务的field就行了
                         也不存在
                         */
                        computeTasks.putCache(groupKey, computeRecords);

                        //轮询查询结果
                        //如果有状态变化，改动cache中内容
                        //准备 /refresh 的参数
                        JSONObject refreshJson = new JSONObject();
                        refreshJson.put("ip", serviceIp);
                        refreshJson.put("port", servicePort);
                        refreshJson.put("tid", taskId);
                        System.out.println("start: " + groupKey);
                        System.out.println(collaborationConfig);
                        /*
                        新开 Callable 线程轮询获取模型运行状态
                        数据方法的话，新开线程阻塞等待计算结果即可
                        维护一个线程池，避免频繁的创建、删除线程
                         */
                        Callable<JSONObject> callable = new Callable<JSONObject>() {
                            //一个线程执行完毕之后会自动结束，如果在运行过程中发生异常也会提前结束。
                            //保证线程读取时标志位是最新数据
                            private volatile int index = 0;
                            //带返回值的 call 方法
                            @Override
                            public JSONObject call() throws Exception {
                                //default value = 0
                                int refreshStatus;
                                JSONObject resJson = new JSONObject();
                                RestTemplate restTemplate = new RestTemplate();
                                JSONObject dataJson;
                                do {
                                    Thread.sleep(3000);
                                    String refreshUrl = "http://" + mangeServiceLocation + "/GeoModeling/computableModel/refreshTaskRecord";
                                    Object dataObject = restTemplate.postForEntity(refreshUrl, refreshJson, JSONObject.class).getBody().get("data");
                                    dataJson = JSONObject.parseObject(JSONObject.toJSONString(dataObject));
                                    refreshStatus = dataJson.getInteger("status");
                                    if (refreshStatus == 2) {
                                        JSONArray outputs = dataJson.getJSONArray("outputs");
                                        resJson.put("outputs", outputs);
                                        resJson.put("tid", dataJson.getString("tid"));
                                    } else {
                                        if (index > 60 && refreshStatus == 0) {
                                            Thread.currentThread().interrupt();
                                            return null;
                                        }
                                        System.out.println("Waiting for computation: "+ (++index) + "+++status: " + refreshStatus);
                                    }

                                } while (refreshStatus != 2 && refreshStatus != -1);
                                return dataJson;
                            }
                        };
                        //感觉上好像行，但是又有问题，取出来的值到底是哪一个的喃？
                        /*
                        返回值通过 Future 封装
                        异步获取执行结果或取消执行任务的情景。
                        当一个计算任务需要执行很长时间，那么就可以用 FutureTask 来封装这个任务
                        主线程在完成自己的任务之后再去获取结果
                         */
                        FutureTask<JSONObject> ft = new FutureTask<>(callable);
                        Thread thread = new Thread(ft);
                        thread.start();
                        JSONObject resJson = ft.get();
                        //外部为线程 A,Callable 为线程 B
                        // thread.interrupt();
                        /*
                        每个 websocket 对应一个主线程
                        每个主线程再对应多个子线程，子线程是在主线程下开的
                        子线程返回结果也是返回给主线程，主线程内存池只有一个（ 针对单个websocket 线程而言）
                        在同一个 websocket 下，每个子线程对应一个高速缓存进行自己计算
                        返回结果到主线程的话，主线程（单个websocket）对应一个的内存池
                         */
                        System.out.println("end: " + groupKey);
                        if (resJson == null) {
                            computeMsg.setOutputs("Fail");
                            collaborationBehavior.sendComputeResult(collaborationConfig.getParticipants(), computeMsg.getReceivers(), computeMsg);
                            return;
                        }
                        // String sucTaskTid = resJson.getString("tid");
                        // HashMap<String, ComputeMsg> computeList = computeTasks.getCache(groupKey);
                        // ComputeMsg sucMessage = computeList.get(sucTaskTid);
                        computeMsg.setComputeSuc(true);
                        JSONArray sucOutputs = resJson.getJSONArray("outputs");
                        computeMsg.setOutputs(sucOutputs);
                        //将所有返回内容存入项目中,不做持久化暂时没用
                        for (int i = 0; i < sucOutputs.size(); i++) {
                            JSONObject outItem = sucOutputs.getJSONObject(i);
                            String address = outItem.getString("url").split("\\?")[0];
                            String fileName = outItem.getString("event");
                            String suffix = outItem.getString("suffix");
                            ResourceEntity resourceEntity = new ResourceEntity();
                            resourceEntity.setUserUpload(false);
                            resourceEntity.setUploaderId(user);
                            resourceEntity.setUploaderName(sender.getName());
                            resourceEntity.setSuffix("." + suffix);
                            String uid = UUID.randomUUID().toString();
                            resourceEntity.setUid(uid);
                            resourceEntity.setUploadTime(new Date());
                            resourceEntity.setName(fileName);
                            resourceEntity.setAddress(address);
                            resourceEntity.setDescription(fileName);
                            //这个需要确定
                            resourceEntity.setType("data");
                            resourceEntity.setPrivacy("private");
                            resourceEntity.setActivityId(aid);
                            resourceEntity.setFolder(false);
                            //已经将数据存入资源中心
                            ripDao.addResource(resourceEntity);
                            outputRes.add(resourceEntity);
                            outResIds.add(uid);
                        }

                        //更新文档
                        for (Object item : inputs){
                            JSONObject input = JSONObject.parseObject(JSONObject.toJSONString(item));
                            inResIds.add(input.getString("uid"));
                        }

                    } else {
                    /*
                    从缓存中获得数据处理任务
                    1. 从 message 中读取 token， id
                    2. java异步运行，以免阻塞程序
                    3. 等待运行结果
                     */
                        String token = messageObject.getString("token");
                        String serviceId = messageObject.getString("tid");
                        //前端整理好的输入格式
                        JSONObject urls = messageObject.getJSONObject("urls");
                        String params = messageObject.getString("params");
                        //输入数据
                        JSONArray inputs = messageObject.getJSONArray("inputs");
                        JSONArray paramsArray = new JSONArray();
                        if (!params.equals("")){
                            String[] split = params.split(",");
                            for (String param: split){
                                paramsArray.add(param);
                            }
                        }

                        String dataMethodUrl = "http://"+ dataProxyServer +"/invokeUrlsDataPcsWithKey";
                        JSONObject invokeJson = new JSONObject();
                        invokeJson.put("token", URLEncoder.encode(token));
                        invokeJson.put("pcsId", serviceId);
                        invokeJson.put("urls", urls);
                        invokeJson.put("params", paramsArray);
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(invokeJson);
                        //新开线程处理
                        Callable<JSONObject> dataComputeCallable = ()-> restTemplate.exchange(dataMethodUrl, HttpMethod.POST, httpEntity, JSONObject.class).getBody();
                        FutureTask<JSONObject> futureTask = new FutureTask<>(dataComputeCallable);
                        Thread dataComputeThread = new Thread(futureTask);
                        dataComputeThread.start();
                        JSONObject jsonObject = futureTask.get();
                        // 后续做持久化可能会有用
                        Integer code = jsonObject.getInteger("code");
                        if (code != 0){
                            //Send error info
                            computeMsg.setOutputs("Fail");
                            collaborationBehavior.sendComputeResult(collaborationConfig.getParticipants(), computeMsg.getReceivers(), computeMsg);
                        }else {
                            computeMsg.setComputeSuc(true);
                            Map<String, String> outputs = jsonObject.getObject("urls", HashMap.class);
                            for (Map.Entry item: outputs.entrySet()){
                                String outputName = (String)item.getKey();
                                if (!outputName.equals("undefined")){
                                    //将资源存入项目
                                    String outputUrl = (String)item.getValue();
                                    String[] strings = outputName.split("\\.");
                                    String prefix = strings[0];
                                    String suffix = strings[1];
                                    ResourceEntity outputEntity = new ResourceEntity();
                                    String uid = UUID.randomUUID().toString();
                                    outputEntity.setUid(uid);
                                    outputEntity.setName(prefix);
                                    outputEntity.setSuffix("." + suffix);
                                    outputEntity.setType("data");
                                    outputEntity.setPrivacy("private");
                                    outputEntity.setAddress(outputUrl);
                                    outputEntity.setUploadTime(new Date());
                                    outputEntity.setActivityId(aid);
                                    outputEntity.setFolder(false);
                                    outputEntity.setUserUpload(false);
                                    outputEntity.setUploaderId(user);
                                    outputEntity.setUploaderName(sender.getName());
                                    ripDao.addResource(outputEntity);
                                    outputRes.add(outputEntity);
                                    outResIds.add(uid);
                                }
                            }
                            computeMsg.setOutputs(outputs);

                            //输入数据
                            for (Object item : inputs){
                                JSONObject input = JSONObject.parseObject(JSONObject.toJSONString(item));
                                inResIds.add(input.getString("uid"));
                            }
                        }

                    }

                    //更新文档
                    // String oid = docParser.geoAnalysis(aid, toolId, inResIds, outputRes, computeMsg.getParticipants());
                    String oid = docParseServe.geoAnalysis(aid, toolId, inResIds, outputRes, computeMsg.getParticipants());
                    computeMsg.setOid(oid);

                    //更新当前节点
                    nodeService.addResToNodeBatch(aid, outResIds);
                    //资源自动更新
                    if(graphId != null && graphId != ""){
                        geoAnalysisProcess.batchResFlowAutoUpdate(graphId, aid, outResIds);
                    }

                    HashMap<String, CollaborationUser> oldParticipants = computeMsg.getReceivers();
                    HashMap<String, CollaborationUser> participants = collaborationConfig.getParticipants();
                    collaborationBehavior.sendComputeResult(participants, oldParticipants, computeMsg);
                    break;
                }
                case "task":{
                    //做消息转发
                    HashMap<String, CollaborationUser> participants = collaborationConfig.getParticipants();
                    collaborationBehavior.sendTasKAssignment(participants, sender, messageObject, null);
                    break;
                }
                case "general": {
                    //message transmission
                    HashMap<String, CollaborationUser> participants = collaborationConfig.getParticipants();
                    String receiver = messageObject.getString("receiver");
                    collaborationBehavior.sendTasKAssignment(participants, sender, messageObject, receiver != null && !receiver.equals("") ? receiver : null);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 协同操作控制
    private void collaborationCtrChange(CollaborationConfig collaborationConfig, String groupKey, CollaborationUser sender, String messageType){
        String ctrUser = "";
        if (collaborationConfig.getMode().equals(CollaborationMode.SemiFree_Apply)) {
            List<String> applyQueue = collaborationConfig.getApplyQueue();
            if (applyQueue != null && applyQueue.size() > 0) {
                ctrUser = applyQueue.remove(0);
                collaborationConfig.setOperator(ctrUser);
                collaborationConfig.setApplyQueue(applyQueue);
            } else {
                collaborationConfig.setOperator("");
            }
            groups.put(groupKey, collaborationConfig);

            collaborationBehavior.sendControlInfo(collaborationConfig, applyQueue, sender, messageType);
        } else if(collaborationConfig.getMode().equals(CollaborationMode.SemiFree_Occupy)){
            collaborationConfig.setOperator("");
            groups.put(groupKey, collaborationConfig);
            collaborationBehavior.sendControlInfo(collaborationConfig, new ArrayList<>(), null, messageType);
        }
    }

    public void communicationClose(String groupKey, Session session, String toolId) {
        CollaborationConfig collaborationConfig;
        try {
            collaborationConfig = groups.get(groupKey);
            HashMap<String, CollaborationUser> participants = collaborationConfig.getParticipants();

            // remove people
            CollaborationUser collaborationUser = null;
            for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                if (user.getValue().getSession().equals(session)) {
                    collaborationUser = user.getValue();

                    participants.remove(user.getKey());
                    collaborationConfig.setParticipants(participants);
                    groups.put(groupKey, collaborationConfig);
                    break;
                }
            }

            if (collaborationConfig.getParticipants().size() == 1) {
                // 持久化存储
                if (communicationCache.getCache(groupKey) != null && communicationCache.getCache(groupKey).size() > 0) {
                    MsgRecords msgRecords = collaborationBehavior.msgCacheStore(groupKey, communicationCache.getCache(groupKey));
                    //聊天记录存入文档
                    String oid = "";
                    if (toolId != null){
                        oid = docParseServe.messageRecord(toolId, msgRecords);
                    }else {
                        oid = docParseServe.messageRecord("Multi-activity", msgRecords);
                    }

                    //将 oid 发送给前端
                    collaborationBehavior.sendStoredMsgRecords(collaborationConfig.getParticipants(), oid);
                }
                // 通知成员退出，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "off");

            } else if (collaborationConfig.getParticipants().size() < 1) {
                // 通讯结束，清除缓存
                groups.remove(groupKey);
                communicationCache.removeCache(groupKey);
            } else {
                // 通知成员退出，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "off");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void operationClose(String groupKey, Session session) {
        CollaborationConfig collaborationConfig;
        try {
            // System.out.println("0---Operation close---groupId: " +  groupKey);
            collaborationConfig = groups.get(groupKey);

            // String name = "";
            // for (Map.Entry<String, CollaborationUser> item : collaborationConfig.getParticipants().entrySet()){
            //     CollaborationUser value = item.getValue();
            //     name += ":" + value.getName();
            // }
            //
            // System.out.println("1---Operation close---Exist Participants: " +  name);
            HashMap<String, CollaborationUser> participants = collaborationConfig.getParticipants();
            // remove people
            CollaborationUser collaborationUser = null;
            for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                if (user.getValue().getSession().equals(session)) {
                    collaborationUser = user.getValue();
                    participants.remove(user.getKey());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String nowDate = dateFormat.format(new Date());
                    System.out.println("Operation已断开连接："+"用户名-"+user.getValue().getName() + "-----"+ nowDate);
                    //在同一地址内进行操作，不用再做set
                    // collaborationConfig.setParticipants(participants);
                    // groups.put(groupKey, collaborationConfig);
                    break;
                }
            }

            // collaboration mode
            collaborationCtrChange(collaborationConfig, groupKey, collaborationUser, "control-stop");

            // 通知成员退出，发布新的成员列表
            collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "off");

            // 通讯结束，清除缓存
            if (groups.get(groupKey).getParticipants().size() < 1) {
                groups.remove(groupKey);
                operationQueue.removeCache(groupKey);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}




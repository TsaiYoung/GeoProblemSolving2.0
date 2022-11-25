package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.impl;

import cn.edu.njnu.geoproblemsolving.business.activity.dao.Impl.ActivityLinkProtocolDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServe;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ResProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.RoleProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityGraph;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityLinkProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityNode;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.LinkRestriction;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.TagUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityGraphRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityLinkProtocolRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityNodeRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.resource.dao.ActivityResDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.Impl.ActivityResServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @ClassName GeoAnalysisProcess
 * @Description 进行大图维护以及对外提供一系列接口
 * 后续设计的东西为了能向上兼容的话，最好是以类似于 AOP 的方式进行
 * 这样就不必对以前的代码进行大量的修改
 * @Author zhngzhng
 * @Date 2021/7/21
 **/
@Aspect
@Service
public class GeoAnalysisProcessImpl implements GeoAnalysisProcess {
    @Autowired
    ActivityLinkProtocolDaoImpl protocolDao;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ActivityLinkProtocolRepository protocolRepository;

    @Autowired
    ActivityGraphRepository graphRepository;

    @Autowired
    ActivityResServiceImpl activityResService;

    @Autowired
    ActivityNodeRepository nodeRepository;

    @Autowired
    ActivityResDaoImpl activityResDao;

    @Autowired
    NodeService nodeService;

    @Autowired
    DocParseServe docParseServe;

    @Value("${userServerLocation}")
    String userServer;

    /**
     * 初始化 graph
     * 应用场景：
     * 将项目设定为 MultiActivity 时调用
     * （不需要在设定项目的时候调用，这样徒增消耗）
     * 在新建协议的时候检测是否有此图，无的话调用即可
     *
     * @param rootAid
     * @return
     */
    @Override
    public ActivityGraph initActivityGraph(String rootAid) {
        ActivityGraph activityGraph = new ActivityGraph();
        activityGraph.setId(rootAid);
        activityGraph.setAdjacencyMap(new HashMap<>());
        activityGraph.setCreateDate(new Date());
        return graphRepository.save(activityGraph);
    }

    /**
     * 新建协议
     * 1. 新建协议
     * 2. 维护图
     * 新建协议需要做的
     * 判断节点是否存在，不存在则创建
     * 将协议添加到图中去[实质就是在图中添加边（由于关系的存在所以是简化了用户添加边的困难，还有就是是否可以通过边来简化遍历的内容），判断重复边]
     * 根据协议进行资源流动，先进行关系内资源流动，在节点上进行操作，然后再更新到 activity 上去
     * 遍历图找到关联的节点
     * （由于关系的存在，可以简化操作，如1个分散为3个，可以直接先遍历分散点，完成上层的关联点的判断然后根据关系完成流动，但是这只是简化了）
     * (可能还并不使用)还可以使用关系进行简化存储，现在的邻接表存储的是0->(1,constraint) 可以简化为 => 0 ->
     * todo 集合框架的传值的问题
     *
     * @param rootAid      协议所依附于的活动
     * @param linkProtocol 链接协议，包含如下内容
     *                     type            协议的关系
     *                     nodeIdList      协议中节点的id,即相关联的活动 id
     *                     linkRestriction 协议设置的资源与用户的限制条件
     * @return 参数很多 restFul 风格是 hold 不住的
     */
    @Override
    public ActivityLinkProtocol setLinkProtocol(String rootAid, ActivityLinkProtocol linkProtocol, Integer level) {
        String type = linkProtocol.getType();
        ArrayList<String> nodeIdList = linkProtocol.getNodes();
        LinkRestriction linkRestriction = linkProtocol.getRestriction();
        Optional<ActivityGraph> graphOptional = graphRepository.findById(rootAid);
        ActivityGraph activityGraph = graphOptional.isPresent() ? graphOptional.get() : initActivityGraph(rootAid);
        // Acquiring the graph's adjacency list.
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = activityGraph.getAdjacencyMap();
        // 生成协议
        HashMap<String, HashMap<String, LinkRestriction>> protocolByRelation = setProtocolByRelation(adjacencyMap, nodeIdList, type, linkRestriction, level);
        // 更新邻接表
        adjacencyMap.putAll(protocolByRelation);
        activityGraph.setAdjacencyMap(adjacencyMap);
        graphRepository.save(activityGraph);

        /*
        获得协议的上层节点，用于协议内部的资源流动
        需要完成将协议添加到图中的操作,才能实现此步骤
         */
        // HashMap<String, HashMap<String, LinkRestriction>> relevantUpperNode = getRelevantUpperNode(adjacencyMap, nodeIdList);
        //完成关系内资源流动
        resFlowInProtocol(null, type, nodeIdList, linkRestriction);
        //关系下层资源流动
        resFlowInLowerNode(rootAid, nodeIdList);

        // 将添加后的图给用户
        linkProtocol.setProtocolId(UUID.randomUUID().toString());
        protocolRepository.save(linkProtocol);
        return linkProtocol;
    }

    /**
     * 生成协议，包含与协议内节点相关联的其他节点（邻接表结构）
     *
     * @param adjacencyMap
     * @param nodeIdList
     * @param type
     * @param linkRestriction
     * @return
     */
    private HashMap<String, HashMap<String, LinkRestriction>> setProtocolByRelation(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap, ArrayList<String> nodeIdList, String type, LinkRestriction linkRestriction, Integer level) {
        //取出第一个节点（关键节点，在排序规则中第一个会汇聚点，分散点，起点）
        String keyNodeId = nodeIdList.get(0);
        //判断协议中节点是否存在，若不存在则新建
        for (String nodeId : nodeIdList) {
            if (nodeService.nodeIsPresent(nodeId) == null) {
                nodeService.createActivityNode(nodeId, level);
            }
        }
        //初始化终点数据结构
        HashMap<String, LinkRestriction> endNodeAndRestriction = new HashMap<>();
        //初始化协议数据结构
        HashMap<String, HashMap<String, LinkRestriction>> linkProtocol = new HashMap<>();
        //将节点转换为邻接表---由于有关系类型的存在，简化了部分关系内部的操作
        /*
        1. 先新建节点
        2. 再将关系添加到图中
        新建关系内节点的邻接表
        有个注意点，需要将终点加在起点已有的邻接表中
        如果此起点无其他终点，则可以为其新建邻接表并将其放在图中
        上述步骤被封装在 generateEdgeEndNode 中
        */
        switch (type) {
            case "Merger":
                for (int i = 1; i < nodeIdList.size(); i++) {
                    String startNodeId = nodeIdList.get(i);
                    //将终点放入起点的邻接表中
                    HashMap<String, LinkRestriction> edgeEndNode = generateEdgeEndNode(adjacencyMap, startNodeId, keyNodeId, linkRestriction);
                    linkProtocol.put(startNodeId, edgeEndNode);
                }
                break;
            case "Branch":
                for (int i = 1; i < nodeIdList.size(); i++) {
                    String endNodeId = nodeIdList.get(i);
                    /*
                    同一起点，只需要判断一次起点是否有多边连接
                    如果有的话，其他所有终点都已经被存入
                    后续几个终点只需要存入即可
                     */
                    if (i == 1) {
                        endNodeAndRestriction = generateEdgeEndNode(adjacencyMap, keyNodeId, endNodeId, linkRestriction);
                        continue;
                    }
                    LinkRestriction oldRestriction = edgeIsPresent(adjacencyMap, keyNodeId, endNodeId);
                    LinkRestriction newRestriction = linkRestriction;
                    if (oldRestriction != null) {
                        newRestriction = mergeLinkProperties(linkRestriction, oldRestriction);
                    }
                    endNodeAndRestriction.put(endNodeId, newRestriction);
                }
                linkProtocol.put(keyNodeId, endNodeAndRestriction);
                break;
            case "Loop":
                //单向循环模式
                for (int i = 0; i < nodeIdList.size(); i++) {
                    String startNodeId = nodeIdList.get(i);
                    String endNodeId;
                    // 起点为最后列表中最后一个则起点为 keyNode
                    endNodeId = (i == nodeIdList.size() - 1) ? keyNodeId : nodeIdList.get(i + 1);
                    HashMap<String, LinkRestriction> edgeEndNode = generateEdgeEndNode(adjacencyMap, startNodeId, endNodeId, linkRestriction);
                    linkProtocol.put(startNodeId, edgeEndNode);
                }
                break;
            case "Sequence":
                //串联模式
                for (int i = 0; i < nodeIdList.size() - 1; i++) {
                    String startNodeId = nodeIdList.get(i);
                    String endNodeId = nodeIdList.get(i + 1);
                    HashMap<String, LinkRestriction> edgeEndNode = generateEdgeEndNode(adjacencyMap, startNodeId, endNodeId, linkRestriction);
                    linkProtocol.put(startNodeId, edgeEndNode);
                }
                break;
        }
        return linkProtocol;
    }

    /**
     * 关系上层及关系内资源的流动
     * 1. 关系上层到关系连接点的连接
     * 2. 关系内部的流动
     * ps: 可能需要注意资源的去重，去除 id 相同的即可
     *
     * @param upperNodeLinkList 关系上层节点关系
     * @param nodeIds           关系
     * @return
     */
    private void resFlowInProtocol(HashMap<String, HashMap<String, LinkRestriction>> upperNodeLinkList, String type, ArrayList<String> nodeIds, LinkRestriction linkRestriction) {
        //关系上层到关系连接点的流动，内部包含活动层的更新
        // for (Map.Entry<String, HashMap<String, LinkRestriction>> linkedEdge : upperNodeLinkList.entrySet()) {
        //     //起终点是倒置的，所以起点是存储在 value 中的，有多个起点
        //     String endNodeId = linkedEdge.getKey();
        //     ActivityNode endNode = nodeRepository.findById(endNodeId).get();
        //     HashMap<String, LinkRestriction> startNodeAndLinkRestriction = linkedEdge.getValue();
        //     for (Map.Entry<String, LinkRestriction> endNodeItem : startNodeAndLinkRestriction.entrySet()) {
        //         //获取流动的资源，并且判断那些资源可以通过限制
        //         String startNodeId = endNodeItem.getKey();
        //         LinkRestriction edgeRestriction = endNodeItem.getValue();
        //         ActivityNode flowNode = nodeRepository.findById(startNodeId).get();
        //         HashMap<String, String> flowNodeResources = flowNode.getResources();
        //         HashSet<String> approvedRes = resFlowApprove(flowNodeResources, edgeRestriction);
        //         //无可流动资源直接就不做处理了
        //         if (approvedRes.size() == 0) continue;
        //         //将资源流动到节点中
        //         HashMap<String, String> nodeResourceTagMap = addResourceToNode(startNodeId, endNodeId, approvedRes);
        //         endNode.setResources(nodeResourceTagMap);
        //         nodeRepository.save(endNode);
        //     }
        // }

        //内部的流动 协议内部的限制性条件是相同的
        String keyNodeId = nodeIds.get(0);
        ActivityNode keyNode = nodeRepository.findById(keyNodeId).get();
        HashMap<String, String> keyNodeResources = keyNode.getResources();
        switch (type) {
            case "Merger":
                for (int i = 1; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    ActivityNode startNode = nodeRepository.findById(startId).get();
                    HashMap<String, String> startNodeResourceMap = startNode.getResources();
                    HashSet<String> approvedRes = resFlowApprove(startNodeResourceMap, linkRestriction);
                    HashMap<String, String> endNodeResourceTagMap = addResourceToNode(startId, keyNodeId, approvedRes);
                    keyNode.setResources(endNodeResourceTagMap);
                    nodeRepository.save(keyNode);
                }
                break;
            case "Branch":
                HashSet<String> approvedRes = resFlowApprove(keyNodeResources, linkRestriction);
                if (approvedRes.size() == 0) break;
                for (int i = 1; i < nodeIds.size(); i++) {
                    String endNodeId = nodeIds.get(i);
                    HashMap<String, String> endNodeResourceTagMap = addResourceToNode(keyNodeId, endNodeId, approvedRes);
                    ActivityNode endNode = nodeRepository.findById(endNodeId).get();
                    endNode.setResources(endNodeResourceTagMap);
                    nodeRepository.save(endNode);
                }
                break;
            case "Loop":
                for (int i = 0; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    String endNodeId = (i == nodeIds.size() - 1) ? keyNodeId : nodeIds.get(i + 1);
                    ActivityNode endNode = nodeRepository.findById(endNodeId).get();
                    HashSet<String> approvedResId = resFlowApprove(startId, linkRestriction);
                    HashMap<String, String> endNodeResourceTag = addResourceToNode(startId, endNodeId, approvedResId);
                    endNode.setResources(endNodeResourceTag);
                    nodeRepository.save(endNode);
                }
                break;
            case "Sequence":
                for (int i = 0; i < nodeIds.size() - 1; i++) {
                    String startId = nodeIds.get(i);
                    String endId = nodeIds.get(i + 1);
                    HashSet<String> flowApprove = resFlowApprove(startId, linkRestriction);
                    HashMap<String, String> endNodeResourceTag = addResourceToNode(startId, endId, flowApprove);
                    ActivityNode endNode = nodeRepository.findById(endId).get();
                    endNode.setResources(endNodeResourceTag);
                    nodeRepository.save(endNode);
                }
                break;
        }
    }


    /**
     * 实现关系下层的流动
     * <p>
     * 1. 获取节点下层路径
     * 2. 根据路径获取资源可到达的节点
     * 3. 根据节点及资源可到达的节点将资源流动过去
     *
     * @param graphId
     * @param nodeIdList 关系类型
     * @return
     */
    private void resFlowInLowerNode(String graphId, ArrayList<String> nodeIdList) {
        for (String nodeId : nodeIdList) {
            //获取节点所有下层路径 todo 优化点：关系内存在下层关系的，会导致有很多无用路径
            Stack<Stack<HashMap<String, LinkRestriction>>> relevantNodeRoute = getRelevantNodeRoute(graphId, nodeId);
            //将此方法移植到深度遍历中，即可去掉重复
            for (int i = relevantNodeRoute.size() - 1; i >= 0; i--) {
                Stack<HashMap<String, LinkRestriction>> path = relevantNodeRoute.get(i);
                //路径中的第一个节点，因为关系只能是一层，所以能用于判断第一个
                HashMap<String, LinkRestriction> edgeOne = path.get(0);
                //实际上就一个item，这个可能是在数据结构上可以优化的点；连续内存地址与不连续内存地址
                for (String id : edgeOne.keySet()) {
                    if (nodeIdList.contains(id)) {
                        //说明该条路径第一个节点是关系内的，关系内资源流动已经完成，可以直接去掉
                        //todo：为什么需要将关系内部与下部进行划分喃？直接放在一起不好吗？
                        relevantNodeRoute.remove(i);
                    }
                }
            }
            if (relevantNodeRoute.size() == 0) continue;
            ActivityNode node = nodeRepository.findById(nodeId).get();
            HashMap<String, String> resourceTagMap = node.getResources();
            //获取当前节点中资源所能到达的节点
            HashMap<String, HashSet<String>> resFlowNode = getFlowNode(relevantNodeRoute, resourceTagMap);
            //将资源流动到各点中
            resFlow(nodeId, resFlowNode);
        }
    }


    /**
     * 暂时取这个名字
     * 资源的流动
     *
     * @param resNodeId   资源流出的节点，用于获取资源字段
     * @param resFlowNode 由路径生成的每个资源可以流动到的节点
     */
    private void resFlow(String resNodeId, HashMap<String, HashSet<String>> resFlowNode){
        for (Map.Entry<String, HashSet<String>> item : resFlowNode.entrySet()) {
            /*
            1. 拿到资源字段
            2. 根据路径依次存入activity
            3. 存入节点
             */
            String resId = item.getKey();
            HashSet<String> nodeId = item.getValue();
            if (nodeId.isEmpty()) {
                continue;
            }
            Iterator<String> iterator = nodeId.iterator();
            //遍历这些节点将资源存入
            while (iterator.hasNext()) {
                String nextNodeId = iterator.next();
                //将资源添加到对应的活动中，并返回节点的资源标签
                addResource(resNodeId, nextNodeId, resId);
            }
        }
    }

    /**
     * 两个节点间的资源流动
     *
     * @param startNodeId     流动节点 id
     * @param endNodeId       接收流动资源节点的 id
     * @param linkRestriction 节点间的资源限定条件
     * @return
     */
    private HashMap<String, String> resFlowInNode(String startNodeId, String endNodeId, LinkRestriction linkRestriction) {
        ActivityNode startNode = nodeRepository.findById(startNodeId).get();
        HashMap<String, String> startNodeResourceTagMap = startNode.getResources();

        HashSet<String> approveRes = resFlowApprove(startNodeResourceTagMap, linkRestriction);
        // 没有可流动资源，则直接返回原来的即可
        if (approveRes.size() == 0) {
            return null;
        }
        //判断在当前协议下哪些资源能够得到授权
        return addResourceToNode(startNodeId, endNodeId, approveRes);
    }


    /**
     * 获取当前关系的上级节点及对应的边
     * 这个起终点是的反向的，终点->[起点，限制性条件]
     * 起终点倒置 end->[{start, link} .... {start, link}]
     *
     * @param adjacencyMap 邻接表，图
     * @param nodeList     关系中的节点 id 列表
     * @return key: 关系中节点
     * value: 与该节点先关联的上层节点
     * 约定大于配置的话
     * 存储顺序还是的采用同关系中节点相同的存储顺序
     * 每个节点的都可以有多个上层节点（父级）
     * 这里相当于把起终点顺序给倒置了
     * 还有一个问题：是否一同返回边的限制性条件
     * 是，方便直接验证，不用再根据起终点来拿限制性条件
     * 同key的map，直接value覆盖
     */
    private HashMap<String, HashMap<String, LinkRestriction>> getRelevantUpperNode(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap, ArrayList<String> nodeList) {
        //初始化用于存储返回值,key:终点, value.key 起点
        HashMap<String, HashMap<String, LinkRestriction>> relevantNode = new HashMap<>();
        //取出邻接表中的终点，通过终点来获取起点
        for (Map.Entry<String, HashMap<String, LinkRestriction>> item : adjacencyMap.entrySet()) {
            HashMap<String, LinkRestriction> endNodeList = item.getValue();
            //遍历协议节点列表，如果是边的终点，则将存入
            for (String nodeId : nodeList) {
                /*
                判断此终点 List 中是否含有当前节点;
                若有则该起点连接着该节点
                 */
                if (endNodeList.containsKey(nodeId)) {
                    // 获取起始节点的 NodeId, 并构造<start, Restriction>结构存入返回值中 <End, <Start, Restriction>>
                    String startNodeId = item.getKey();
                    //去掉关系内部节点为上层节点的情况
                    if (nodeList.contains(startNodeId)) continue;
                    LinkRestriction linkRestriction = endNodeList.get(nodeId);

                    /*
                    若返回值中已经有以该节点为终点的边了，则将其取出，并将新边放进去
                     */
                    HashMap<String, LinkRestriction> startAndLinkRestriction =
                            (relevantNode.containsKey(nodeId)) ? relevantNode.get(nodeId) : new HashMap<>();
                    startAndLinkRestriction.put(startNodeId, linkRestriction);
                    relevantNode.put(nodeId, startAndLinkRestriction);
                }
            }
        }
        return relevantNode;
    }

    /**
     * 判断边是否存在
     * 判断图中是否有此边
     * 若有则返回此边的限制性条件
     * 判断重复边
     *
     * @param adjacencyMap
     * @param start
     * @param end
     * @return
     */
    private LinkRestriction edgeIsPresent(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap, String start, String end) {
        if (adjacencyMap.keySet().contains(start)) {
            HashMap<String, LinkRestriction> endNodeAndLinkRestriction = adjacencyMap.get(start);
            if (endNodeAndLinkRestriction.keySet().contains(end)) {
                return endNodeAndLinkRestriction.get(end);
            }
        }
        return null;
    }

    /**
     * 生成边
     * 单独写一个通用方法主要是为了判断重复边
     * 若有重复边则将限制性条件进行合并即可
     * <p>
     * 单个边的生成因为采用邻接表，还有个注意点
     * 同起点的处理
     *
     * @param adjacencyMap
     * @param start
     * @param end
     * @param linkRestriction
     * @return 返回起点的邻接表，如果有相同起点的情况，需要合并结束点(在generateEdgeEndNode方法中执行)
     */
    private HashMap<String, HashMap<String, LinkRestriction>> generateEdge(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap, String start, String end, LinkRestriction linkRestriction) {
        //生成该地点的邻接表的尾部节点
        HashMap<String, LinkRestriction> edgeEndNodeAndRestriction = generateEdgeEndNode(adjacencyMap, start, end, linkRestriction);
        HashMap<String, HashMap<String, LinkRestriction>> linkList = new HashMap<>();
        linkList.put(start, edgeEndNodeAndRestriction);
        return linkList;
    }

    /**
     * 生成起点邻接表
     * 0->[{1,weight},{3, weight}......]
     *
     * @param adjacencyMap    图的邻接表
     * @param start
     * @param end
     * @param linkRestriction
     * @return 该起点的尾节点们
     */
    private HashMap<String, LinkRestriction> generateEdgeEndNode(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap, String start, String end, LinkRestriction linkRestriction) {
        //判断边是否存在，如果边重复，则合并 linkRestriction 即可
        LinkRestriction oldRestriction = edgeIsPresent(adjacencyMap, start, end);
        LinkRestriction newLinkRestriction = linkRestriction;
        if (oldRestriction != null) {
            //边已经存在，合并限制性条件
            newLinkRestriction = mergeLinkProperties(linkRestriction, oldRestriction);
        }
        /*
        判断是否起点是否连接多个点
         */
        HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(start) == null ? new HashMap<>() : adjacencyMap.get(start);
        endNodeAndRestriction.put(end, newLinkRestriction);
        return endNodeAndRestriction;
    }


    /**
     * 节点间资源流动判断
     * 判断哪些资源可以流动
     * 这个方法可能还有些麻烦，就感觉
     * 限制性条件中的内容是放行条件，只需要满足其中一个条件即可放行
     * 资源因为字段不明，用限制性条件去 indexOf 可能会更好一一些
     *
     * @param activityResource
     * @param linkRestriction
     * @return 能流动的资源的id list,采用set, set 中元素不能重复
     */
    private HashSet<String> resFlowApprove(HashMap<String, String> activityResource, LinkRestriction linkRestriction) {
        ResProtocol resProtocol = linkRestriction.getResProtocol();
        if (activityResource.size() == 0 || resProtocol.toString().equals("None")) return new HashSet<>();
        if (resProtocol.toString().equals("All"))
            return JSONObject.parseObject(JSONObject.toJSONString(activityResource.keySet()), HashSet.class);
        //Constrains 部分代码
        HashSet<String> types = linkRestriction.getTypes();
        HashSet<String> approvedResIdList = new HashSet<>();
        for (Map.Entry<String, String> res : activityResource.entrySet()) {
            String resId = res.getKey();
            String resTagStr = res.getValue();
            HashMap<String, String> resTagMap = TagUtil.recoveryResTag(resTagStr);
            String type = resTagMap.get("type");
            boolean flag = false;
            if (types != null && !type.equals("data") && types.contains(type)) flag = true;
            //判断 data 类型数据
            if (type.equals("data") && types.contains(type)){
                HashSet<String> formats = linkRestriction.getFormats();
                String format = resTagMap.get("format");
                if (formats == null || formats.isEmpty() || (formats != null && formats.contains(format))) {
                    flag = true;
                } else {
                    flag = false;
                    continue;
                }
                HashSet<String> scales = linkRestriction.getScales();
                String scale = resTagMap.get("scale");
                if (scales == null || scales.isEmpty() ||(scales != null && scales.contains(scale))) {
                    flag = true;
                } else {
                    flag = false;
                    continue;
                }
                HashSet<String> references = linkRestriction.getReferences();
                String reference = resTagMap.get("reference");
                if (references == null || references.isEmpty() || (references != null && references.contains(reference))) {
                    flag = true;
                } else {
                    flag = false;
                    continue;
                }
                HashSet<String> units = linkRestriction.getUnits();
                String unit = resTagMap.get("unit");
                if (units == null || units.isEmpty() || (units != null && units.contains(unit))) {
                    flag = true;
                } else {
                    flag = false;
                    continue;
                }
                HashSet<String> concepts = linkRestriction.getConcepts();
                String concept = resTagMap.get("concept");
                if (concepts == null || concepts.isEmpty() || (concepts != null && concepts.contains(concept))) {
                    flag = true;
                } else {
                    flag = false;
                    continue;
                }
            }
            if (flag) approvedResIdList.add(resId);
        }
        return approvedResIdList;
    }

    private HashSet<String> resFlowApprove(String flowNodeId, LinkRestriction linkRestriction) {
        ActivityNode flowNode = nodeRepository.findById(flowNodeId).get();
        HashMap<String, String> activityResourceMap = flowNode.getResources();

        return resFlowApprove(activityResourceMap, linkRestriction);
    }



    /**
     * 判断用户或资源是否符合条件(适用于Resource)
     *
     * @param tagStr 资源判断字符串
     * @param linkRestriction
     * @return
     */
    private boolean resFlowApproveByTagStr(String tagStr, LinkRestriction linkRestriction) {
        if (linkRestriction.getResProtocol().equals(ResProtocol.All)){
            return true;
        }else if (linkRestriction.getResProtocol().equals(ResProtocol.None)){
            return false;
        }else {
            HashMap<String, String> resTagMap = TagUtil.recoveryResTag(tagStr);
            String type = resTagMap.get("type");
            HashSet<String> types = linkRestriction.getTypes();
            // 默认是无法流动的
            boolean flag = false;
            if (types != null && !type.equals("data") && types.contains(type)) flag = true;
            //判断 data 类型数据,全都满足才能通过
            if (type.equals("data") && types.contains(type)){
                HashSet<String> formats = linkRestriction.getFormats();
                String format = resTagMap.get("format");
                if (formats == null || formats.isEmpty() || (formats != null && formats.contains(format))) flag = true;
                else return false;
                HashSet<String> scales = linkRestriction.getScales();
                String scale = resTagMap.get("scale");
                if (scales == null || scales.isEmpty()|| (scales != null && scales.contains(scale))) flag = true;
                else return false;
                HashSet<String> references = linkRestriction.getReferences();
                String reference = resTagMap.get("reference");
                if (references == null || references.isEmpty() || (references != null && references.contains(reference))) flag = true;
                else return false;
                HashSet<String> units = linkRestriction.getUnits();
                String unit = resTagMap.get("unit");
                if (units == null || units.isEmpty() || (units != null && units.contains(unit))) flag = true;
                else return false;
                HashSet<String> concepts = linkRestriction.getConcepts();
                String concept = resTagMap.get("concept");
                if (concepts == null || concepts.isEmpty() || (concepts != null && concepts.contains(concept))) flag = true;
                else return false;
            }
            return flag;
        }
    }

    /**
     * 节点间用户流动判断
     *
     * @param activityUser
     * @param linkRestriction
     * @return
     */
    private HashSet<String> userFlowApprove(HashMap<String, String> activityUser, LinkRestriction linkRestriction) {
        HashSet<String> approvedUserIdList = new HashSet<>();
        RoleProtocol roleProtocol = linkRestriction.getRoleProtocol();
        if (roleProtocol.toString().equals("All")) return (HashSet<String>) activityUser.keySet();
        if (roleProtocol.toString().equals("None")) return new HashSet<>();
        HashSet<String> roles = linkRestriction.getRoles();
        HashSet<String> organizations = linkRestriction.getOrganizations();
        HashSet<String> domains = linkRestriction.getDomains();

        for (Map.Entry<String, String> user : activityUser.entrySet()) {
            String userId = user.getKey();
            String userTagStr = user.getValue();
            HashMap<String, HashSet<String>> userTagMap = TagUtil.recoveryUserTag(userTagStr);
            HashSet<String> role = userTagMap.get("role");
            if (TagUtil.checkTagContain(role, roles)) {
                approvedUserIdList.add(userId);
                continue;
            }
            HashSet<String> domain = userTagMap.get("domain");
            if (TagUtil.checkTagContain(domain, domains)) {
                approvedUserIdList.add(userId);
                continue;
            }
            HashSet<String> org = userTagMap.get("org");
            if (TagUtil.checkTagContain(org, organizations)) {
                approvedUserIdList.add(userId);
                continue;
            }
        }
        return approvedUserIdList;
    }


    /**
     * 单个用户流动判断
     * 拆分tag字符串
     * 如果不把构造的时候写好，读取的时候肯定会出错
     *
     * @param tagStr
     * @param linkRestriction
     * @return
     */
    private boolean userFlowApprove(String tagStr, LinkRestriction linkRestriction) {
        //添加 All & None 情况的判断
        String roleProtocol = linkRestriction.getRoleProtocol().toString();
        if (roleProtocol.equals("All")) return true;
        if (roleProtocol.equals("None")) return false;
        HashMap<String, HashSet<String>> userTagMap = TagUtil.recoveryUserTag(tagStr);
        HashSet<String> role = userTagMap.get("role");
        HashSet<String> roles = linkRestriction.getRoles();
        if (TagUtil.checkTagContain(roles, role)) {
            return true;
        }
        HashSet<String> domain = userTagMap.get("domain");
        HashSet<String> domains = linkRestriction.getDomains();
        if (TagUtil.checkTagContain(domains, domain)) {
            return true;
        }
        HashSet<String> org = userTagMap.get("organization");
        HashSet<String> organizations = linkRestriction.getOrganizations();
        if (TagUtil.checkTagContain(organizations, org)) {
            return true;
        }
        return false;
    }


    /**
     * 判断用户是否可以流动到此节点
     * 实现逻辑
     * 1. 查询有此用户的节点（写成一个方法，尽量将操作都拆分成原子操作，万一后面就要多线程分布式了喃）
     * 2. 查找有此用户的节点到当前节点的路径(写成一个方法，可能这个相对麻烦一点，但也不存在嘛)
     * 3. 根据此路径走嘛，看走得到不嘛
     * 如果走得通喃，就直接可以直接加入
     * 走不通喃，就申请加入嘛
     *
     * @param nodeId
     * @param userId
     * @param graphId
     * @return
     */
    private boolean userIsBeRefuse(String nodeId, String userId, String graphId) {
        return true;
    }


    /**
     * 文件上传的自动更新
     * 实现逻辑
     * 1. 查询与此节点来连通的节点（深度/广度搜索？？？或者我直接递归进去就完事了可能还简单一些）
     * 2. 依次判断每条边是否拦得住这个资源，限制不住就放行
     *
     * @param nodeId
     * @param graphId
     * @param uid
     * @return
     */
    @Override
    public void resFlowAutoUpdate(String graphId, String nodeId, String uid) {
        //获取该节点往下的路径
        Stack<Stack<HashMap<String, LinkRestriction>>> relevantNodeRoute = getRelevantNodeRoute(graphId, nodeId);
        /*
        获取当前节点中的该资源
        需要判断它是否接受 AutoUpdate
         */
        ResourceEntity file = activityResService.getFileById(nodeId, uid);
        String resTags = TagUtil.setResourceTag(file);
        HashMap<String, String> resTagMap = new HashMap<>();
        resTagMap.put(uid, resTags);
        //获取该资源可以流动的的到的节点
        HashMap<String, HashSet<String>> flowNode = getFlowNode(relevantNodeRoute, resTagMap);
        resFlow(nodeId, flowNode);
    }

    /**
     * 自动更新
     *
     * @param graphId
     * @param nodeId
     * @param resTag
     */
    @Override
    public void batchResFlowAutoUpdate(String graphId, String nodeId, HashMap<String, String> resTag) {
        if (graphId == null || graphId.equals("") || graphId.equals("null")) return;
        //Get the connection node with this node.
        Stack<Stack<HashMap<String, LinkRestriction>>> relevantNodeRoute = getRelevantNodeRoute(graphId, nodeId);
        //Get the point where resources can flow to.
        HashMap<String, HashSet<String>> flowNode = getFlowNode(relevantNodeRoute, resTag);
        if (flowNode != null){
            resFlow(nodeId, flowNode);
        }
    }

    @Override
    public void batchResFlowAutoUpdate(String graphId, String nodeId, HashSet<String> uids) {
        if (graphId == null || graphId.equals("")) return;
        if (uids == null || uids.size() == 0) return;
        Optional<ActivityNode> byId = nodeRepository.findById(nodeId);
        if (!byId.isPresent()) return;
        ActivityNode node = byId.get();
        HashMap<String, String> resources = node.getResources();
        HashMap<String, String> resTagMap = new HashMap<>();
        for (String uid : uids){
            String tagStr = resources.get(uid);
            if (tagStr != null){
                resTagMap.put(uid, tagStr);
            }
        }
        batchResFlowAutoUpdate(graphId, nodeId, resTagMap);
    }

    @Override
    public void batchResFlowAutoUpdate(String graphId, String nodeId, String uid) {
        Optional<ActivityNode> byId = nodeRepository.findById(nodeId);
        if (!byId.isPresent()) return;
        ActivityNode node = byId.get();
        HashMap<String, String> resources = node.getResources();
        HashMap<String, String> tagMap = new HashMap<>();
        String tagStr = resources.get(uid);
        if (tagStr == null || tagStr.equals("")) return;
        tagMap.put(uid, tagStr);
        batchResFlowAutoUpdate(graphId, nodeId, tagMap);
    }

    /*
            基于 WFS(width first search)
            走不过的节点就直接放弃，这样效率会更高相较于DFS而言😊
            然后直接返回能到达的nodeId，岂不是不亦乐乎😂
            return-> key: resId, value: 资源能到达的节点
             */
    private HashMap<String, HashSet<String>> resAutoUpdate(String graphId, String nodeId, HashMap<String, String> resTag) {
        Optional<ActivityGraph> byId = graphRepository.findById(graphId);
        if (!byId.isPresent()) return null;
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = byId.get().getAdjacencyMap();
        HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(nodeId);
        HashMap<String, HashSet<String>> flowNode = new HashMap<>();
        HashSet<HashMap<String, LinkRestriction>> endNodes;
        while (true) {
            for (Map.Entry<String, LinkRestriction> entry : endNodeAndRestriction.entrySet()) {
                LinkRestriction restriction = entry.getValue();
                for (Map.Entry<String, String> item : resTag.entrySet()) {

                }

            }
            break;
        }

        return null;
    }

    private HashMap<String, HashSet<String>> rAutoUpdate(HashMap<String, LinkRestriction> linkRestriction,
                                                         HashMap<String, String> resTag,
                                                         HashMap<String, HashSet<String>> flowNode) {


        return flowNode;
    }

    /**
     * 合并重复边的限制性条件
     * 先使用笨方法进行处理
     * 后续改用反射吧。。。
     * 终于有能用到反射的地方了，giao
     * <p>
     * 为什么要使用并喃？有点忘了😀
     * 这里还可以进行拓展，再在外面封装一层可以输入字段参数的部分
     * <p>
     * 以旧的规则为基准，将 source 内容合并到target 中
     *
     * @param source
     * @param target
     * @return
     */
    private LinkRestriction mergeLinkProperties(LinkRestriction source, LinkRestriction target) {
        // 获取用户限制性操作模式
        String sourceRoleProtocol = source.getRoleProtocol().toString();
        String targetRoleProtocol = target.getRoleProtocol().toString();

        /*
        有以下几种情况
        1. target 为 None
         */
        if (targetRoleProtocol.equals("All") && sourceRoleProtocol.equals("None")) {
            target.setRoleProtocol(RoleProtocol.valueOf("None"));
        } else if (targetRoleProtocol.equals("Constraints") && (sourceRoleProtocol.equals("None") || sourceRoleProtocol.equals("All"))) {
            target.setRoleProtocol(RoleProtocol.valueOf(sourceRoleProtocol));
        } else if (targetRoleProtocol.equals("Constraints") && sourceRoleProtocol.equals("Constraints")) {
            //用户三个部分合并
            HashSet<String> sourceDomains = source.getDomains();
            HashSet<String> sourceRoles = source.getRoles();
            HashSet<String> sourceOrganizations = source.getOrganizations();

            HashSet<String> targetDomains = target.getDomains();
            HashSet<String> targetRoles = target.getRoles();
            HashSet<String> targetOrganizations = target.getOrganizations();

            HashSet<String> domains = mergeStringList(sourceDomains, targetDomains);
            HashSet<String> roles = mergeStringList(sourceRoles, targetRoles);
            HashSet<String> organizations = mergeStringList(sourceOrganizations, targetOrganizations);

            //  资源部分限制条件合并
            HashSet<String> sourceFormats = source.getFormats();
            HashSet<String> sourceReferences = source.getReferences();
            HashSet<String> sourceScales = source.getScales();

            HashSet<String> targetFormats = target.getFormats();
            HashSet<String> targetReferences = target.getReferences();
            HashSet<String> targetScales = target.getScales();

            HashSet<String> format = mergeStringList(sourceFormats, targetFormats);
            HashSet<String> references = mergeStringList(sourceReferences, targetReferences);
            HashSet<String> scales = mergeStringList(sourceScales, targetScales);

            target.setDomains(domains);
            target.setRoles(roles);
            target.setOrganizations(organizations);
            target.setFormats(format);
            target.setReferences(references);
            target.setScales(scales);
        }
        return target;
    }

    /**
     * 合并两个List
     * 并且去除其中重复的部分
     *
     * @param source
     * @param target
     * @return
     */
    private HashSet<String> mergeStringList(HashSet<String> source, HashSet<String> target) {
        if (source == null) {
            source = new HashSet<>();
        }
        if (target == null) {
            target = new HashSet<>();
        }
        //去掉重复项
        target.removeAll(source);
        // 合并
        target.addAll(source);
        return target;
    }

// ======================节点层面上的操作结束===============================================================================

    /**
     * 更新节点
     * 只有更新用户或者资源
     * 应用场景 todo 完成转移---> NodeServer
     * 只要是活动资源或用户有所变化的时候都需要进行处理
     *
     * @param aid           节点 id
     * @param userId        userId or uid
     * @param oldTag        旧的标签值(put操作时使用)
     * @param newTag        新的标签值（put操作时使用）
     * @param operationType insert, del and put
     * @return 节点的 member
     */
    private HashMap<String, String> putActivityNodeUser(String aid, String userId, String userRole, String operationType, String oldTag, String newTag) {
        //初始化用于存储用户标签,value 存userId
        Optional<ActivityNode> nodeRepositoryById = nodeRepository.findById(aid);
        ActivityNode node = nodeRepositoryById.get();
        if (!nodeRepositoryById.isPresent()) {
            //如果无此节点则新建此节点
            node = nodeService.createActivityNode(aid, 1);
        }
        //不可能为空
        HashMap<String, String> members = node.getMembers();

        if (operationType.equals("insert")) {
            //无需判断原本有无此用户，map 数据结构决定后续 push 的会覆盖前面同key的值
            String tagStr = nodeService.getUserTag(userId, userRole);
            members.put(userId, tagStr);
        } else if (operationType.equals("del")) {
            members.remove(userId);
        } else if (operationType.equals("put")) {
            //更新，先验证是否有此用户，若无此用户则 warning
            if (!members.containsKey(userId)) {
                return null;
            }
            String userTag = members.get(userId);
            /*
            可能会出现问题
            不该替换的地方被替换掉
             */
            String replacedStr = userTag.replace(oldTag, newTag);
            members.put(userId, replacedStr);

        }
        return members;
    }

    /**
     * 节点中资源变化的修改
     * 只针对节点层面的操作
     * 不处理活动层面的内容
     * <p> todo 完成转移---> NodeServer
     * 资源流动的处理真挺麻烦的
     *
     * @param aid               节点id
     * @param res               资源
     * @param putUid            put 操作时仅需要传 uid 过来即可
     * @param oldTag(ArrayList) put 操作时会使用到旧的标签
     * @param newTag(ArrayList) put 操作时会使用到新的标签(两者一一对应)
     * @param operationType
     * @return 节点更新后的 resource 字段
     */
    private HashMap<String, String> putActivityNodeResource(String aid, ResourceEntity res, String operationType, String putUid, ArrayList<String> oldTag, ArrayList<String> newTag) {
        //初始化用于存储用户标签,value 存userId
        Optional<ActivityNode> nodeRepositoryById = nodeRepository.findById(aid);
        ActivityNode node = nodeRepositoryById.get();
        if (!nodeRepositoryById.isPresent()) {
            //如果无此节点则新建此节点
            node = nodeService.createActivityNode(aid, 1);
        }
        // 资源的流动，实际上是对 node 中 resource 字段的处理
        HashMap<String, String> resourceTagMap = node.getResources();
        if (operationType.equals("insert")) {
            /*
            addResource 需要的参数,针对节点层面的操作
            resourceEntity(uid,限制性标签内容)
            附加在活动层面的操作放在更高层次一点的方法中
             */
            String uid = res.getUid();
            HashMap<String, String> resInfo = new HashMap<>();
            resInfo.put("uid", uid);
            resInfo.put("type", res.getType());
            String resTag = TagUtil.setResTag(resInfo);
            resourceTagMap.put(uid, resTag);
        } else if (operationType.equals("del")) {
            /*
            是否需要处理活动层面的内容？ 不需要
            活动中的资源自有自己的一套管理方法，节点的处理，就不用管活动中的逻辑了
            整个节点的操作过程更相对更加底层一点，相对于活动的操作
            只需要 uid 将条目删除即可
             */
            resourceTagMap.remove(putUid);
        } else if (operationType.equals("put")) {
            /*
            将原限制条目取出来，进行修改
            uid,(oldTag,newTag)一一对应
             */
            String resTagStr = resourceTagMap.get(putUid);
            /*
            这样写的话仅仅支持单个条件的修改，当一次性修改多个的时候会有问题
            这样的话，可能需要我再上层进行封装，或者修改此处的代码
            这里的问题等待和前端进行调试的时候再来确认
             */
            String replacedResTagStr = resTagStr;
            for (int i = 0; i < oldTag.size(); i++) {
                replacedResTagStr = replacedResTagStr.replace(oldTag.get(i), newTag.get(i));
            }
            resourceTagMap.put(putUid, replacedResTagStr);
        }
        return resourceTagMap;
    }


    /**
     * 流动资源的添加
     * 将资源添加到对应的活动中，并返回节点的资源标签列表
     * 资源是流动过去的
     * 所以也需要操作活动层的内容
     * 完成将资源存入活动层
     * 返回节点的资源标签
     * todo 这不是节点层面操作，这是活动流动的内容
     *
     * @param startAid 资源来源
     * @param endAid   当前节点 id
     * @param uid      资源id(资源来源节点出获取)
     * @return 当前节点的资源标签
     */
    private HashMap<String, String> addResource(String startAid, String endAid, String uid) {
        //1.在源节点中查询得到文件信息
        ResourceEntity res = activityResService.getFileById(startAid, uid);
        //2.将资源放在当前节点中
        ResourceEntity flowFolder = activityResService.getFlowFolder(endAid, "Flowing Resources");
        String flowFolderId;
        //若无此文件夹则生成此文件夹
        if (flowFolder == null) {
            flowFolder = new ResourceEntity();
            flowFolderId = UUID.randomUUID().toString();
            flowFolder.setUid(flowFolderId);
            flowFolder.setName("Flowing Resources");
            flowFolder.setPrivacy("public");
            flowFolder.setParent("0");
            flowFolder.setFolder(true);
            flowFolder.setActivityId(endAid);
            flowFolder.setChildren(new ArrayList<>());
        }
        flowFolderId = flowFolder.getUid();
        ArrayList<ResourceEntity> flowFolderChildren = flowFolder.getChildren();
        //将资源放入 Flowing Resources 中
        res.setParent(flowFolderId);
        if (flowFolderChildren == null) flowFolderChildren = new ArrayList<>();
        //重复资源处理
        if (flowFolderChildren.size() > 0){
            ArrayList<String> flowFolderFileIds = new ArrayList<>();
            for (ResourceEntity item : flowFolderChildren){
                flowFolderFileIds.add(item.getUid());
            }
            int index = flowFolderFileIds.indexOf(uid);
            if (index != -1){
                flowFolderChildren.remove(index);
            }
        }
        flowFolderChildren.add(res);
        flowFolder.setChildren(flowFolderChildren);
        activityResDao.addResource(flowFolder);
        //第二步：更新文档
        HashMap<String, String> resInfo = docParseServe.resFlow(startAid, endAid, uid);
        if (resInfo == null) return null;
        //第三步：更新节点
        return nodeService.addResToNodeBatch(endAid, resInfo);
    }

    /**
     * 节点间多资源流动
     * ps: uids.size() 等于 0
     * 说明无资源可以流动，则直接将endNodeResourceTagMap 返回来
     * 可优化的地方：
     * 如果没有资源需要流动，那就没必要更新了
     * 代码整洁点更好，这一次 put 就不节约了
     *
     * @param startAid 流动的节点
     * @param endAid   接收资源的点
     * @param uids     流动资源的 id
     * @return 终点的资源map
     * tips
     * 重复资源去重处理，使用覆盖
     */
    private HashMap<String, String> addResourceToNode(String startAid, String endAid, HashSet<String> uids) {
        // uids 为空，直接将 endNode 的 resourceTagMap返回回来即可
        ActivityNode endNode = nodeRepository.findById(endAid).get();
        HashMap<String, String> endNodeResourcesTagMap = endNode.getResources();
        if (uids.size() == 0) return endNodeResourcesTagMap;
        //更新活动层内容
        ArrayList<ResourceEntity> approvedFilesEntity = activityResService.getFilesByIds(startAid, uids);
        //获取流动资源文件夹，该文件夹位于项目的根目录，可以直接 query
        ResourceEntity flowFolder = activityResService.getFlowFolder(endAid, "Flowing Resources");
        if (flowFolder == null) {
            flowFolder = new ResourceEntity();
            flowFolder.setUid(UUID.randomUUID().toString());
            flowFolder.setName("Flowing Resources");
            flowFolder.setPrivacy("public");
            flowFolder.setParent("0");
            flowFolder.setFolder(true);
            flowFolder.setActivityId(endAid);
            flowFolder.setChildren(new ArrayList<>());
        }
        // 资源的parentId
        String flowFolderUid = flowFolder.getUid();
        ArrayList<ResourceEntity> flowFolderChildren = flowFolder.getChildren();
        if (flowFolderChildren == null) flowFolderChildren = new ArrayList<>();
        // key 存 id, value 存对应的tag
        HashMap<String, String> flowResourceTagMap = new HashMap<>();
        ArrayList<String> flowFolderFileIds = new ArrayList<>();
        //重复资源的处理,本来就无流动资源所以就不必做处理
        for (ResourceEntity item : flowFolderChildren) {
            flowFolderFileIds.add(item.getUid());
        }
        for (ResourceEntity approvedFile : approvedFilesEntity) {
            String uid = approvedFile.getUid();
            /*
            节约一个循环
            在这里获取用户资源的 resourceTagMap
             */
            HashMap<String, String> resInfo = docParseServe.getResInfo(startAid, uid);
            if (resInfo != null){
                String resTag = TagUtil.setResTag(resInfo);
                flowResourceTagMap.put(uid, resTag);
            }

            approvedFile.setParent(flowFolderUid);
            //不用去重，直接覆盖，后来的覆盖掉先来的
            int index = flowFolderFileIds.indexOf(uid);
            if (index != -1) {
                //则说明有此资源，则用后来的资源替换掉
                flowFolderChildren.remove(index);
                flowFolderChildren.add(approvedFile);
                continue;
            }
            flowFolderChildren.add(approvedFile);
        }
        flowFolder.setChildren(flowFolderChildren);
        //活动层内容更新成功
        activityResDao.addResource(flowFolder);

        //更新文档
        docParseServe.resFlow(startAid, endAid, uids);
        //putAll 同 key 去掉重复的
        endNodeResourcesTagMap.putAll(flowResourceTagMap);
        return endNodeResourcesTagMap;
    }


    private HashMap<String, String> delResource(String aid, String uid) {
        return putActivityNodeResource(aid, null, "del", uid, null, null);
    }

    private HashMap<String, String> putResource(String aid, JSONObject putInfo) {
        //在 controller 层面使用resourceDto 进行接收，然后处理成这边需要的结构即可
        String putUid = putInfo.getString("uid");
        JSONArray oldTag = putInfo.getJSONArray("oldTag");
        JSONArray newTag = putInfo.getJSONArray("newTag");
        ArrayList<String> oldTagList = Lists.newArrayList();
        ArrayList<String> newTagList = Lists.newArrayList();
        oldTag.forEach(item -> {
            String tempTag = (String) item;
            oldTagList.add(tempTag);
        });
        newTag.forEach(item -> {
            String tempTag = (String) item;
            newTagList.add(tempTag);
        });
        return putActivityNodeResource(aid, null, "put", putUid, oldTagList, newTagList);
    }

    /**
     * 流动用户加入
     * 1. 更新活动 member
     * 2. 更新节点
     * 用户流动过来是什么 role？
     * 采用默认的 role
     *
     * @param aid
     * @param userId
     * @return
     */
    private HashMap<String, String> addUser(String aid, String userId) {
        //更新活动中的 member
        Activity activity = activityRepository.findById(aid).get();
        JSONArray members = activity.getMembers();
        JSONObject member = new JSONObject();
        member.put("role", "ordinary-member");
        member.put("userId", userId);
        members.add(member);
        activity.setMembers(members);
        activityRepository.save(activity);
        //肯定是有关系才有流动，有关系，肯定就有这个节点
        return putActivityNodeUser(aid, userId, "ordinary-member", "insert", null, null);
    }


// ======================节点层面上的操作结束===============================================================================

    /**
     * 获取指定节点的所有下层路径，用于资源流动
     * 资源流动不会往上层流动
     * <p>
     * 直接递归进去
     * 返回结果的组织形式
     * 按层次结构存储
     * 0->[{1,a},{2,b}]
     * 2->[{4,c},{5,d}]
     * 还得添加一个资源流动函数
     * 返回能到达的节点列表
     * DFS 算法
     *
     * @param graphId
     * @param nodeId
     * @return
     */
    private Stack<Stack<HashMap<String, LinkRestriction>>> getRelevantNodeRoute(String graphId, String nodeId) {
        Optional<ActivityGraph> byId = graphRepository.findById(graphId);
        ActivityGraph activityGraph = byId.isPresent() ? byId.get() : null;
        if (activityGraph == null) return null;
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = activityGraph.getAdjacencyMap();
        /*
        将第一层拼接好后，然后再进去递归完后拼接，每一层都往endNode的hashMap中去存
        0->[{1,a},{2,b},{3,c},{5,b}] 这就是从0到5的第一条路径
        不能同 key，所以把起点放在 value  中，然后进行存储即可
         */
        //初始化用于存储路径
        Stack<Stack<HashMap<String, LinkRestriction>>> pathStack = new Stack<>();
        //用于存储遍历层次
        Stack<HashMap<String, LinkRestriction>> nodeStack = new Stack<>();
        //用于标记访问过的节点
        Stack<String> visitedNode = new Stack<>();
        return depthFirstSearch(adjacencyMap, nodeId, nodeStack, pathStack, visitedNode);
    }


    /**
     * 深度遍历该节点能到达的所有路
     *
     * @param adjacencyMap
     * @param startId
     * @param pathStackTemp 存储节点层次
     * @param path          存储路径
     * @return 有多条路径 key路径
     * 环的处理， visited是对于当前正在进行查找的路径而言的
     */
    private Stack<Stack<HashMap<String, LinkRestriction>>> depthFirstSearch(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap,
                                                                            String startId,
                                                                            Stack<HashMap<String, LinkRestriction>> pathStackTemp,
                                                                            Stack<Stack<HashMap<String, LinkRestriction>>> path,
                                                                            Stack<String> visitedNode
    ) {
        //起点不用管,有肯定就不为空
        HashMap<String, LinkRestriction> endNode = adjacencyMap.get(startId);
        visitedNode.add(startId);
        if (endNode != null && !endNode.isEmpty()) {
            //节点遍历完，都没下一步则走完了，退回上一层
            int size = endNode.size();
            for (Map.Entry<String, LinkRestriction> item : endNode.entrySet()) {
                String nextStartId = item.getKey();
                if (visitedNode.contains(nextStartId)) {
                    /*
                    fix bug
                    添加环路径的入栈机制
                    1. 该节点仅有一个后继节点
                    2. 该后继节点为已访问过的节点
                     */
                    if (size == 1){
                        if (pathStackTemp.size() == 0) return path;
                        Stack<HashMap<String, LinkRestriction>> pathStack = new Stack<>();
                        pathStack.addAll(pathStackTemp);
                        path.push(pathStack);
                    }
                    continue;
                }
                LinkRestriction linkRestriction = item.getValue();
                //到这一层了，入栈
                HashMap<String, LinkRestriction> endNodeAndRestriction = new HashMap<>();
                endNodeAndRestriction.put(nextStartId, linkRestriction);
                //pathStack 是相互影响的，用于传值的 new, 用于存值的就
                pathStackTemp.push(endNodeAndRestriction);
                depthFirstSearch(adjacencyMap, nextStartId, pathStackTemp, path, visitedNode);
            }
        } else {
            /*
            路径入库
            达到一条路径的尽头
            ps: 有个特殊情况，该即是尽头节点
             */
            if (pathStackTemp.size() == 0) return path;
            Stack<HashMap<String, LinkRestriction>> pathStack = new Stack<>();
            pathStack.addAll(pathStackTemp);
            path.push(pathStack);
        }
        /*
        完成遍历
        返回上层，将此层节点出栈
        ps: 当退回到出发节点的时候，nodeStack 为里面就没内容了，可以直接将path返回
         */
        if (pathStackTemp.size() == 0) return path;
        pathStackTemp.pop();
        visitedNode.pop();
        return path;
    }

    /**
     * 获取两点之间的所有路径
     *
     * @param graphId
     * @param startId
     * @param endId
     * @return
     */
    private Stack<Stack<HashMap<String, LinkRestriction>>> getRelevantNodeRoute(String graphId, String startId, String endId) {
        Stack<Stack<HashMap<String, LinkRestriction>>> stacks = new Stack<>();
        Optional<ActivityGraph> byId = graphRepository.findById(graphId);
        if (!byId.isPresent()) {
            return null;
        }
        ActivityGraph activityGraph = byId.get();
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = activityGraph.getAdjacencyMap();
        Stack<Stack<HashMap<String, LinkRestriction>>> pathStack = new Stack<>();
        Stack<HashMap<String, LinkRestriction>> nodeStack = new Stack<>();
        return gRelevantNode(adjacencyMap, startId, endId, nodeStack, pathStack);
    }

    private Stack<Stack<HashMap<String, LinkRestriction>>> gRelevantNode(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap,
                                                                         String startId,
                                                                         String endId,
                                                                         Stack<HashMap<String, LinkRestriction>> pathStack,
                                                                         Stack<Stack<HashMap<String, LinkRestriction>>> route
    ) {
        HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(startId);
        if (endNodeAndRestriction != null) {
            //先遍历尾节点，看这一层是否有终点
            Set<String> endNodeIdSet = endNodeAndRestriction.keySet();
            for (String endNodeId : endNodeIdSet) {
                if (endNodeId.equals(endId)) {
                    //终点
                    LinkRestriction linkRestriction = endNodeAndRestriction.get(endNodeId);
                    HashMap<String, LinkRestriction> hashMap = new HashMap<>();
                    hashMap.put(endNodeId, linkRestriction);
                    pathStack.push(hashMap);
                    route.push(pathStack);
                    pathStack.pop();
                    return route;
                }
            }
            //还未达到终点
            for (Map.Entry<String, LinkRestriction> item : endNodeAndRestriction.entrySet()) {
                LinkRestriction linkRestriction = item.getValue();
                String startNodeId = item.getKey();
                // 设置好了，将其返回
                gRelevantNode(adjacencyMap, startNodeId, endId, pathStack, route);
            }
        } else {
            //退出，进入下一层的下一个
            pathStack.pop();
        }
        return route;
    }


    /**
     * 获得可以流动的节点
     * <p>
     * @param paths
     * @param resApproveMap
     * @return
     */
    private HashMap<String, HashSet<String>> getFlowNode(Stack<Stack<HashMap<String, LinkRestriction>>> paths, HashMap<String, String> resApproveMap) {
        if (paths == null || paths.size() == 0) return null;
        HashMap<String, HashSet<String>> flowNodeList = new HashMap<>();
        /*
        初始化资源可到达的数据结构，用于存储 key 为resId, value为可以到达的节点
        key 在resId 前面添加是否能继续向深处走的标记
        wx(yes),xw(no)
        T(yes), F(no)
         */
        Set<String> resIds = resApproveMap.keySet();
        //生成标记字符串
        int pathNum = paths.size();
        int pathNumTemp = paths.size();
        String flags = "";
        while (pathNumTemp != 0) {
            pathNumTemp--;
            flags += "T";
        }
        //将标记字符串存入资源id前
        for (String resId : resIds) {
            //初始化的时候当然是所有的资源都是yes状态, flowNodeList存储能达到的节点
            resId = flags + resId;
            flowNodeList.put(resId, new HashSet<>());
        }
        /*
        开始遍历每一条路径，判断能到达的节点，需要去重
        优化点：不用走相同的路
         */
        for (int i = 0; i < pathNum; i++) {
            //获得第 i 条路径
            Stack<HashMap<String, LinkRestriction>> path = paths.pop();
            for (int j = 0; j < path.size(); j++) {
                //第 i 条路径的第 j 个节点
                HashMap<String, LinkRestriction> nodeInPath = path.get(j);
                LinkRestriction edgeRestriction = new LinkRestriction();
                String flowNodeId = "";
                //获取该边的节点以及限制性条件
                for (Map.Entry<String, LinkRestriction> item : nodeInPath.entrySet()) {
                    flowNodeId = item.getKey();
                    edgeRestriction = item.getValue();
                }
                //遍历所有资源，判断是能够通过
                for (Map.Entry<String, String> restriction : resApproveMap.entrySet()) {
                    String resId = restriction.getKey();
                    //获取带标签的资源 Id
                    Set<String> resIdAndFlags = flowNodeList.keySet();
                    String resIdAndFlag = "";
                    String flag = "";
                    String tempFlags = "";
                    for (String item : resIdAndFlags) {
                        //获取该资源的 id 中的标记
                        String tempResId = item.substring(pathNum);
                        if (tempResId.equals(resId)) {
                            //获取此资源 此条路径的通路情况
                            flag = item.substring(i, i + 1);
                            //将该资源的标记字符串取出
                            tempFlags = item.substring(0, pathNum);
                            //获取此资源含有通路标记对的resId
                            resIdAndFlag = item;
                        }
                    }
                    //该资源的此路径已断
                    if (flag.equals("F")) {
                        continue;
                    }
                    HashSet<String> canFlowNodeSet = flowNodeList.get(resIdAndFlag);
                    //已经到达过该节点，不再判断
                    if (canFlowNodeSet.contains(flowNodeId)) {
                        continue;
                    }
                    String resTagStr = restriction.getValue();
                    //若能通过，则将节点id，放到对应的资源的节点中（注意去重）
                    if (resFlowApproveByTagStr(resTagStr, edgeRestriction)) {
                        canFlowNodeSet.add(flowNodeId);
                    } else {
                        //不能通过，将此路径标记替换为 F todo 这里有点问题
                        flowNodeList.remove(resIdAndFlag);
                        StringBuilder builder = new StringBuilder();
                        builder.append(resIdAndFlag);
                        builder.replace(i, i + 1, "F");
                        flowNodeList.put(builder.toString(), canFlowNodeSet);
                    }
               }
            }
        }
        HashMap<String, HashSet<String>> newFlowNodeList = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> item : flowNodeList.entrySet()) {
            String key = item.getKey();
            HashSet<String> value = item.getValue();
            // Remove path tag.
            String resId = key.substring(pathNum);
            if (value != null && value.size() != 0){
                newFlowNodeList.put(resId, value);
            }
            // newFlowNodeList.put(resId, value);
            // flowNodeList.remove(key);
            // flowNodeList.put(resId, value);
        }
        return newFlowNodeList;
    }


    /**
     * 获取该节点上层的所有节点
     *
     * @param graphId
     * @param nodeId
     * @return
     */
    public Stack<Stack<HashMap<String, LinkRestriction>>> getUpperNodePath(String graphId, String nodeId) {
        Optional<ActivityGraph> byId = graphRepository.findById(graphId);
        if (!byId.isPresent()){
            return null;
        }
        ActivityGraph activityGraph = byId.get();
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = activityGraph.getAdjacencyMap();
        Stack<Stack<HashMap<String, LinkRestriction>>> path = new Stack<>();
        Stack<HashMap<String, LinkRestriction>> nodeStack = new Stack<>();
        //初始化节点栈，用于解决循环问题
        Stack<String> nodeIdStack = new Stack<>();
        gUpperNodePath(adjacencyMap, nodeId, nodeIdStack, path, nodeStack);
        return path;

    }


    private Stack<Stack<HashMap<String, LinkRestriction>>> gUpperNodePath(HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap,
                                                                          String nodeId,
                                                                          Stack<String> nodeIdStack,
                                                                          Stack<Stack<HashMap<String, LinkRestriction>>> path,
                                                                          Stack<HashMap<String, LinkRestriction>> nodeStackTemp) {
        //是否进入下一层的标记，如果能进入下一层则说明当前这一层不是该条路径的尽头，此机制还是有问题
        boolean depthIndex = true;
        //节点访问标记点
        nodeIdStack.add(nodeId);
        for (Map.Entry<String, HashMap<String, LinkRestriction>> edge : adjacencyMap.entrySet()) {
            HashMap<String, LinkRestriction> edgeEndNode = edge.getValue();
            if (edgeEndNode.containsKey(nodeId)) {
                //终点列表中包含此节点，则说明这是一条可行的路径，则将此条边入栈
                String startId = edge.getKey();
                /*
                这个机制有问题，应该使用路线节点栈中的内容来判断是否为循环
                if (visitedNode.contains(startId)) continue;
                节点访问机制会有一定问题
                当两条路径尽头都为同一节点的时候，第二条路径就会因为尽头节点被第一条路径访问过了，而不能被放入路径中
                使用节点栈来判断是否循环即可，环的问题需要处理一下
                 */
                if (nodeIdStack.contains(startId)) continue;
                //获取这条边的限制性条件
                LinkRestriction linkRestriction = edgeEndNode.get(nodeId);
                HashMap<String, LinkRestriction> startNodeAndRestriction = new HashMap<>();
                /*
                可能会有问题，注意看 linkRestriction 的地址，可能会出现所有的 linkRestriction 都是一个的问题
                没有问题，linkRestriction 在下一轮会指向另一个内存地址，然后被push到startNodeAndRestriction中
                如果在重新指向之前，改的话就会影响到startNodeAndRestriction中的内容
                 */
                startNodeAndRestriction.put(startId, linkRestriction);
                nodeStackTemp.push(startNodeAndRestriction);
                //进入下一层,则说明这一层不是
                depthIndex = false;
                gUpperNodePath(adjacencyMap, startId, nodeIdStack, path, nodeStackTemp);
            }
        }
        //所有连接表都走完了，这条路径到头了可以存起来了
        //同名引用类型指向同一个地址
        //
        Stack<HashMap<String, LinkRestriction>> nodeStack = new Stack<>();
        nodeStack.addAll(nodeStackTemp);
        //无上层节点 or 回退到起始节点了
        if (nodeStackTemp.size() == 0) return path;
        if (depthIndex) path.push(nodeStack);
        nodeStackTemp.pop();
        nodeIdStack.pop();
        return path;
    }


    /**
     * 采用广度遍历方式，获取该用户上层节点路径中最近的可以流动过来的节点
     *
     * @param upperPath
     * @param userId
     * @return 为 null 则无法通过 有值则是最近可以流动过来的节点
     */
    public String cUserIsApprovedUsingWFS(Stack<Stack<HashMap<String, LinkRestriction>>> upperPath, String userId) {
        if (upperPath.size() == 0) return null;
        int longestPath = 0;
        //用于标记深度
        int depthIndex = 0;
        while (true) {
            //达到最深深度
            if (depthIndex > longestPath) break;
            //一个 iterator 游标到底后就没用了
            //遍历每条路径
            for (int i = 0; i < upperPath.size(); i++) {
                Stack<HashMap<String, LinkRestriction>> path = upperPath.get(i);
                //获取最长路径
                longestPath = path.size() > longestPath ? path.size() : longestPath;
                //当层数＞= size 的时候，则说明此条路径已经到达尽头
                if (depthIndex >= path.size()) continue;
                HashMap<String, LinkRestriction> edge = path.get(depthIndex);
                /*
                1. 取出起始节点及限制性条件
                2. 判断是否有此用户，无的话就continue
                3. 有此用户(拿到用户tag)，判断当前边能够走通, 不能走通就 over
                4. 能走通就切路径，然后判断路径能否走通，不能走通 over, 能走通就
                 */
                String startId = "";
                //将起始节点取出来
                for (Map.Entry<String, LinkRestriction> item : edge.entrySet()) {
                    startId = item.getKey();
                }
                ActivityNode node = nodeRepository.findById(startId).get();
                HashMap<String, String> members = node.getMembers();
                //该节点都没此用户可以切了
                if (members == null || !members.containsKey(userId)) continue;
                //该节点有此用户，则切一下路径 tips: depthIndex = 0 的情况
                //能通过此路径则说明能行，不单单返回内容，将节点一并返回好了
                String userTag = members.get(userId);
                String latestUserTag = nodeService.updateNodeUserTag(node.getId(), userId);
                if (latestUserTag != null) userTag = latestUserTag;
                if (cUserIsApprovedInPath(path.subList(0, depthIndex + 1), userTag)) {
                    return startId;
                }
            }
            //进入下一层
            depthIndex++;
        }
        return null;
    }

    //判断这个用户是否能通过这条路径,应该使用 pop 从最上层开始算起来
    public boolean cUserIsApprovedInPath(List<HashMap<String, LinkRestriction>> pathNodeStack, String userTag) {
        for (int i = pathNodeStack.size() - 1; i >= 0; i--) {
            HashMap<String, LinkRestriction> edgeAndRestriction = pathNodeStack.get(i);
            for (Map.Entry<String, LinkRestriction> item : edgeAndRestriction.entrySet()) {
                LinkRestriction linkRestriction = item.getValue();
                if (!userFlowApprove(userTag, linkRestriction)) return false;
            }
        }
        //全部走通，则能走通
        return true;
    }


    /**
     * 判断这个此用户是否可以从其他地方流动到该节点
     * 调用该接口的时机：Join this activity 的时候，如果可以就直接加入，不可以的话就申请
     *
     * @param graphId
     * @param nodeId
     * @param userId
     * @return
     */
    public String checkUserIsApprovedService(String graphId, String nodeId, String userId) {
        /*
        1.查找 nodeId 的所有上层路径
        2.采用广度遍历，查询是否能按照路径依次是否可以过去
        3.如果能过去就添加就行了
         */
        Stack<Stack<HashMap<String, LinkRestriction>>> upperPath = getUpperNodePath(graphId, nodeId);
        if (upperPath == null) return null;
        //最近的可流动过来的点
        String flowId = cUserIsApprovedUsingWFS(upperPath, userId);
        return flowId;
    }


    /**
     * 删除单条边
     *
     * @param graphId
     * @param startId
     * @param endId
     * @return
     */
    @Override
    public HashMap<String, HashMap<String, LinkRestriction>> deleteEdge(String graphId, String startId, String endId) {
        Optional<ActivityGraph> graphRepositoryById = graphRepository.findById(graphId);
        if (!graphRepositoryById.isPresent()) return null;
        ActivityGraph activityGraph = graphRepositoryById.get();
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = activityGraph.getAdjacencyMap();
        //adjacencyMap.get 出来的 item 和 adjacencyMap 中的一个地址，直接remove会影响到adjacencyMap中的内容
        adjacencyMap.get(startId).remove(endId);
        return graphRepository.save(activityGraph).getAdjacencyMap();
    }

    /**
     * 删除多条边
     * 看前端如何设计吧
     *
     * @param graphId
     * @param nodeIdSet
     * @return
     */
    @Override
    public HashMap<String, HashMap<String, LinkRestriction>> deleteEdges(String graphId, HashSet<String> nodeIdSet) {
        return null;
    }


    //==================================================================================================================


    @Override
    public void updateGraphByProtocol(String rootId, ActivityLinkProtocol protocol) {
        Optional<ActivityGraph> byId = graphRepository.findById(rootId);
        if (byId.get() == null) {
            return;
        }
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = byId.get().getAdjacencyMap();
        String relationType = protocol.getType();
        ArrayList<String> nodeIds = protocol.getNodes();
        LinkRestriction restriction = protocol.getRestriction();
        String keyNodeId = nodeIds.get(0);
        switch (relationType) {
            case "Merger":
                for (int i = 1; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(startId);
                    //替换新的限制性条件
                    endNodeAndRestriction.put(keyNodeId, restriction);
                }
                break;
            case "Branch":
                HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(keyNodeId);
                for (int i = 1; i < nodeIds.size(); i++) {
                    endNodeAndRestriction.put(keyNodeId, restriction);
                }
                break;
            case "Loop":
                for (int i = 0; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    HashMap<String, LinkRestriction> endNodeAndRestriction1 = adjacencyMap.get(startId);
                    String endNodeId = i == nodeIds.size() - 1 ? keyNodeId : nodeIds.get(i + 1);
                    endNodeAndRestriction1.put(endNodeId, restriction);
                }
                break;
            case "Sequence":
                for (int i = 0; i < nodeIds.size() - 1; i++) {
                    String startId = nodeIds.get(i);
                    String endId = nodeIds.get(i + 1);
                    HashMap<String, LinkRestriction> endNodeAndRestriction2 = adjacencyMap.get(startId);
                    endNodeAndRestriction2.put(endId, restriction);
                }
                break;
        }

    }

    @Override
    public void delGraphByProtocol(String rootId, String protocolId) {
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap = graphRepository.findById(rootId).get().getAdjacencyMap();
        ActivityLinkProtocol activityLinkProtocol = protocolRepository.findById(protocolId).get();
        String relationType = activityLinkProtocol.getType();
        ArrayList<String> nodeIds = activityLinkProtocol.getNodes();
        String keyNodeId = nodeIds.get(0);
        switch (relationType) {
            case "Merger":
                for (int i = 1; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(startId);
                    //替换新的限制性条件
                    endNodeAndRestriction.remove(keyNodeId);
                }
                break;
            case "Branch":
                HashMap<String, LinkRestriction> endNodeAndRestriction = adjacencyMap.get(keyNodeId);
                for (int i = 1; i < nodeIds.size(); i++) {
                    endNodeAndRestriction.remove(keyNodeId);
                }
                break;
            case "Loop":
                for (int i = 0; i < nodeIds.size(); i++) {
                    String startId = nodeIds.get(i);
                    HashMap<String, LinkRestriction> endNodeAndRestriction1 = adjacencyMap.get(startId);
                    String endNodeId = i == nodeIds.size() - 1 ? keyNodeId : nodeIds.get(i + 1);
                    endNodeAndRestriction1.remove(endNodeId);
                }
                break;
            case "Sequence":
                for (int i = 0; i < nodeIds.size() - 1; i++) {
                    String startId = nodeIds.get(i);
                    String endId = nodeIds.get(i + 1);
                    HashMap<String, LinkRestriction> endNodeAndRestriction2 = adjacencyMap.get(startId);
                    endNodeAndRestriction2.remove(endId);
                }
                break;
        }

    }

    @Override
    public LinkRestriction getLinkRestriction(String graphId, String startId, String endId) {
        Optional<ActivityGraph> byId = graphRepository.findById(graphId);
        if (!byId.isPresent()){
            return null;
        }
        ActivityGraph activityGraph = byId.get();
        HashMap<String, HashMap<String, LinkRestriction>> adjacencyMap =
                activityGraph.getAdjacencyMap();
        try {
            HashMap<String, LinkRestriction> endNodeAndLinkRestriction = adjacencyMap.get(startId);
            LinkRestriction linkRestriction = endNodeAndLinkRestriction.get(endId);
            return linkRestriction;
        }catch (NullPointerException e){
            return null;
        }
    }
}

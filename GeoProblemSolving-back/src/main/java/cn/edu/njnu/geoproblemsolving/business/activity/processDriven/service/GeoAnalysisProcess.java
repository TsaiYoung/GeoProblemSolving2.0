package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service;

import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityGraph;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityLinkProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.LinkRestriction;
import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface GeoAnalysisProcess {
    ActivityGraph initActivityGraph(String rootAid);
    /**
     * 新建协议
     * @param graphId 根活动的 aid
     * @return
     */
    ActivityLinkProtocol setLinkProtocol(String graphId, ActivityLinkProtocol linkProtocol, Integer level) throws DocumentException;

    HashMap<String, HashMap<String, LinkRestriction>> deleteEdge(String graphId, String startId, String endId);

    HashMap<String, HashMap<String, LinkRestriction>> deleteEdges(String graphId, HashSet<String> nodeIdSet);

    //判断该用户是否可以到此节点中
    String checkUserIsApprovedService(String graphId, String nodeId, String userId);

    //要不要用AOP方式切入到上传资源的函数中去
    void resFlowAutoUpdate(String graphId, String nodeId, String uid) throws DocumentException;

    void batchResFlowAutoUpdate(String graphId, String nodeId, HashMap<String, String> resTag);

    void batchResFlowAutoUpdate(String graphId, String nodeId, HashSet<String> uids);

    void batchResFlowAutoUpdate(String graphId, String nodeId, String uid);

    void updateGraphByProtocol(String rootId, ActivityLinkProtocol protocol);

    void delGraphByProtocol(String rootId, String protocolId);

    LinkRestriction getLinkRestriction(String graphId, String startId, String endId);


    //协议已经变成了一系列的字符串


    // void addRelationHelper(String start, String end);
    //
    //
    // //采用广度遍历或深度遍历完成连通的查询
    //
    //
    // Object insertSubgraph(String rootActivityId, ArrayList<String> nodes, ArrayList<String> linkType);
    //
    // Object delSubgraph(String protocolId);
    //
    // Object addScatter(String aid);
    //
    // Object checkConnectivity();
    //
    // Object hasEdge(String start, String end);
    //
    // /*
    // 在实现中写一个究极多参数的查找相关联的节点的函数，然后疯狂重载😄 也没想的那么简单哈。。。。
    // 可能需要重载，主要还是限定条件的注入，两大类（user,resource）以及下面的小类：
    // 1. user
    //     domain
    //     organization
    // 2. resource
    //     ......
    //  */
    // Object relationNode(String nodeId, String type, String tag);
    //
    // Object relationNodeUser(String nodeId, String tag);
    //
    // Object relationNode(String node);
    //
    // //遍历图中
    // ArrayList<String> whereUser(String userId);

}

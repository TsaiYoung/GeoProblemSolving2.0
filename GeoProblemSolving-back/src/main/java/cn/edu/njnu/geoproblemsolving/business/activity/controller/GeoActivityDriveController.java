package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityGraph;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityLinkProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.LinkRestriction;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.ProtocolService;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.impl.GeoAnalysisProcessImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.impl.ResourceDispatchImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.impl.UserDispatchImpl;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @ClassName GeoActivityDriveController
 * @Description 地理活动驱动接口
 * 将这里面的内容整个分成三块
 * 然后再在下面按 restful 风格来写就好了
 * @Author zhngzhng
 * @Date 2021/8/3
 **/
@RequestMapping(value = "/activityDriven")
@RestController
public class GeoActivityDriveController {
    private final GeoAnalysisProcessImpl geoAnalysisProcess;

    private final UserDispatchImpl userDispatch;

    private final ResourceDispatchImpl resDispatch;

    private final ProtocolService protocolService;


    public GeoActivityDriveController(GeoAnalysisProcessImpl geoAnalysisProcess,
                                      UserDispatchImpl userDispatch,
                                      ResourceDispatchImpl resourceDispatch,
                                      ProtocolService protocolService) {
        this.geoAnalysisProcess = geoAnalysisProcess;
        this.userDispatch = userDispatch;
        this.resDispatch = resourceDispatch;
        this.protocolService = protocolService;
    }

    @RequestMapping(value = "/graph/{rootId}", method = RequestMethod.GET)
    public JsonResult initGraph(@PathVariable String rootId) {
        ActivityGraph activityGraph = geoAnalysisProcess.initActivityGraph(rootId);
        return ResultUtils.success(activityGraph);
    }



//=================================用户相关==============================================================================

    /**
     * 获取节点中所有的用户标签
     * 包含 domain & org
     *
     * @param nodeIds
     * @return
     */
    @RequestMapping(value = "/user/tag/{level}/{nodeIds}", method = RequestMethod.GET)
    public JsonResult getUsersTag(@PathVariable Integer level, @PathVariable HashSet<String> nodeIds){
        return userDispatch.getNodeUserTag(level, nodeIds);
    }

    /**
     * 查看该用户是否可以流动到该节点
     * @param graphId
     * @param nodeId
     * @param req
     * @return
     */
    @RequestMapping(value = "/user/{graphId}/{nodeId}", method = RequestMethod.GET)
    public JsonResult checkUserCanJoinThsNode(@PathVariable String graphId,
                                              @PathVariable String nodeId,
                                              HttpServletRequest req){
        String userId =  (String)req.getSession().getAttribute("userId");
        return ResultUtils.success(userDispatch.checkUserIsApproved(graphId, nodeId, userId));
    }


//=================================资源相关==============================================================================

    /**
     * 获取节点中资源列表
     * 暂时只包括的 type
     * @param nodeIds
     * @return
     */
    @RequestMapping(value = "/res/tag/{nodeIds}", method = RequestMethod.GET)
    public JsonResult getResourceTag(@PathVariable HashSet<String> nodeIds){
        return ResultUtils.success(resDispatch.getResourceTagInActivity(nodeIds));
    }

//=================================节点操作相关===========================================================================

//=================================活动驱动相关===========================================================================
    /**
     * 新建协议
     * @param protocolJson
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public JsonResult setActivityRelation(@RequestBody JSONObject protocolJson) {
        JSONObject relation = protocolJson.getJSONObject("relation");
        JSONObject restriction = protocolJson.getJSONObject("restriction");
        Integer level = protocolJson.getInteger("level");
        ActivityLinkProtocol activityRelation = JSONObject.parseObject(JSONObject.toJSONString(relation), ActivityLinkProtocol.class);
        LinkRestriction linkRestriction = JSONObject.parseObject(JSONObject.toJSONString(restriction), LinkRestriction.class);
        String id = activityRelation.getGraphId();
        activityRelation.setRestriction(linkRestriction);
        ActivityLinkProtocol linkProtocol = geoAnalysisProcess.setLinkProtocol(id, activityRelation, level);
        return ResultUtils.success(linkProtocol);
    }

    /**
     * 删除连接
     * @param graphId
     * @param startNodeId
     * @param endNodeId
     * @return
     */
    @RequestMapping(value = "/{graphId}/{startNodeId}/{endNodeId}", method = RequestMethod.DELETE)
    public JsonResult delEdge(@PathVariable String graphId,
                              @PathVariable String startNodeId,
                              @PathVariable String endNodeId){
        HashMap<String, HashMap<String, LinkRestriction>> delResult = geoAnalysisProcess.deleteEdge(graphId, startNodeId, endNodeId);
        if (delResult != null){
            return ResultUtils.success(delResult);
        }
        return ResultUtils.error(-2, "No such graph, Check you activity id.");
    }

//=================================节点操作相关===========================================================================
    @RequestMapping(value = "/protocol/{graphId}", method = RequestMethod.PUT)
    public JsonResult updateProtocol(@PathVariable String graphId, @RequestBody ActivityLinkProtocol protocol){
        ActivityLinkProtocol activityLinkProtocol = protocolService.putProtocol(graphId, protocol);
        if (activityLinkProtocol != null){
            return ResultUtils.success(activityLinkProtocol);
        }
        return ResultUtils.error(-2, "Fail.");
    }


    @RequestMapping(value = "/{graphId}/{startNodeId}/{endNodeId}", method = RequestMethod.GET)
    public JsonResult getLinkRestriction(@PathVariable String graphId,
                                         @PathVariable String startNodeId,
                                         @PathVariable String endNodeId){
        LinkRestriction linkRestriction = geoAnalysisProcess.getLinkRestriction(graphId, startNodeId, endNodeId);
        if (linkRestriction == null){
            return ResultUtils.error(-1, "Fail");
        }
        return ResultUtils.success(linkRestriction);
    }



}

package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityDocService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/activityDoc")
public class ActivityDocController {

    private final ActivityDocService activityDocService;
    private final NodeService nodeService;
    public ActivityDocController(ActivityDocService activityDocService, NodeService nodeService) {
        this.activityDocService = activityDocService;
        this.nodeService = nodeService;
    }

    /**
     * inquiry activity document
     */
    @RequestMapping(method = RequestMethod.GET)
    public JsonResult inquiryActivityDoc(@RequestParam("aid") String aid){
        return activityDocService.findDocument(aid);
    }

    /**
     * create activity document
     */
    @RequestMapping(method = RequestMethod.POST)
    public JsonResult createActivityDoc(@RequestBody ActivityDoc activityDoc){
        return activityDocService.saveDocument(activityDoc);
    }

    /**
     * delete activity document
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResult removeActivityDoc(@RequestParam("aid") String aid){
        return activityDocService.deleteDocument(aid);
    }

    /**
     * update activity document
     */
    @RequestMapping(method = RequestMethod.PUT)
    public JsonResult updateActivityDoc(@RequestBody  ActivityDoc activityDoc)
    {
        return activityDocService.updateDocument(activityDoc.getAid(), activityDoc.getDocument());
    }

    @RequestMapping(value = "/{aids}",method = RequestMethod.GET)
    public JsonResult inquiryActivityDocs(@PathVariable HashSet<String> aids){
        return activityDocService.findDocuments(aids);
    }

    //资源元数据发生修改
    @RequestMapping(value = "/typeOrMeta/{graphicId}/{aid}/{uid}", method = RequestMethod.PUT)
    public void putDataMeta(@PathVariable String graphicId, @PathVariable String aid, @PathVariable String uid){
        if (graphicId == null) return;
        nodeService.putResMetaOrType(graphicId, aid, uid);
    }

    /**
     *  查询一个工具的操作记录
     */
    @RequestMapping(value = "/getGeoAnalysisInTool/{aid}/{tid}",method = RequestMethod.GET)
    public JsonResult inquiryGeoAnalysisOperation(@PathVariable("aid") String aid, @PathVariable("tid") String tid){
        return activityDocService.findGeoAnalysisOperation(aid, tid);
    }
}

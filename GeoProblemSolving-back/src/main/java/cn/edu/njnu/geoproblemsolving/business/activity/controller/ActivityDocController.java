package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityDocService;
import cn.edu.njnu.geoproblemsolving.business.activity.service.DocInterpret;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/activityDoc")
public class ActivityDocController {

    private final ActivityDocService activityDocService;
    @Autowired
    DocInterpret docParse;

    public ActivityDocController(ActivityDocService activityDocService) {
        this.activityDocService = activityDocService;
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
    public JsonResult updateAcitivityDoc(@RequestBody  ActivityDoc activityDoc)
    {
        return activityDocService.updateDocument(activityDoc.getAid(), activityDoc.getDocument());
    }

    @RequestMapping(value = "/{aids}",method = RequestMethod.GET)
    public JsonResult inquiryActivityDocs(@PathVariable HashSet<String> aids){
        return activityDocService.findDocuments(aids);
    }

    @RequestMapping(value = "/workflow/{aid}",method = RequestMethod.GET)
    public JsonResult activityDoc2WorkflowTemplate(@PathVariable String aid) throws DocumentException {
        docParse.activity2WorkflowTemplate(aid);
        return null;
    }
}

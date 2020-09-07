package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.LinkProtocol;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/activity")
public class ActivityController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    private final ActivityService activityService;

    @Autowired
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }


    @RequestMapping(method = RequestMethod.POST)
    public JsonResult createActivity(@RequestBody Activity activity) {
        logger.info("createActivity");
        return activityService.createActivity(activity);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/children")
    public JsonResult getChildren(@PathVariable("aid") String aid) {
        logger.info("getChildren");
        return activityService.findChildren(aid);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{aid}/children")
    public JsonResult createChildActivity(@PathVariable("aid") String aid, @RequestParam("childId") String childId) {
        logger.info("createChildActivity");
        return activityService.createChild(aid, childId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/next")
    public JsonResult getNextActivities(@PathVariable("aid") String aid) {
        logger.info("getNextActivities");
        return activityService.findNext(aid);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{aid}/next")
    public JsonResult createNextActivity(@PathVariable("aid") String aid, @RequestParam("nextId") String nextId, @RequestBody LinkProtocol protocol) {
        logger.info("createNextActivity");
        return activityService.createNext(aid, nextId, protocol);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/last")
    public JsonResult getLastActivities(@PathVariable("aid") String aid) {
        logger.info("getLastActivities");
        return activityService.findLast(aid);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{aid}/last")
    public JsonResult createLastActivity(@PathVariable("aid") String aid, @RequestParam("lastId") String lastId, @RequestBody LinkProtocol protocol) {
        logger.info("createLastActivity");
        return activityService.createLast(aid, lastId, protocol);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/link/{aid1}/{aid2}")
    public JsonResult linkActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestBody LinkProtocol protocol) {
        logger.info("linkActivities");
        return activityService.linkActivities(aid1, aid2, protocol);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/separate/{aid1}/{aid2}")
    public JsonResult separateActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2) {
        logger.info("separateActivities");
        return activityService.separateActivities(aid1, aid2);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/join")
    public JsonResult joinActivity(@RequestParam("aid") String aid, @RequestParam("userId") String userId) {
        logger.info("joinActivity");
        return activityService.joinActivity(aid, userId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/quit")
    public JsonResult quitActivity(@RequestParam("aid") String aid, @RequestParam("userId") String userId) {
        logger.info("quitActivity");
        return activityService.quitActivity(aid, userId);
    }
}

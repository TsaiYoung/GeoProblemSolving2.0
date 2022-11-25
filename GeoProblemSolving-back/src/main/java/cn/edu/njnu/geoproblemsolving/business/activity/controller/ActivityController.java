package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

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

    /**
     * Inquiry activity
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public JsonResult inquiryActivity(@RequestParam("aid") String aid) {
        logger.info("createActivity");
        return activityService.findActivity(aid);
    }

    /**
     * Create activity
     * @param activity
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public JsonResult createActivity(@RequestBody Activity activity) {
        logger.info("createActivity");
        return activityService.createActivity(activity);
    }

    /**
     * Update activity
     * @param activity
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{aid}")
    public JsonResult updateActivity(@PathVariable("aid") String aid, @RequestBody UpdateActivityDTO activity) {
        logger.info("createActivity");
        return activityService.updateActivity(aid, activity);
    }

    /**
     * get activity
     * @param aid
     * @return
     */
    @RequestMapping(produces = "application/json;charset=UTF-8", method = RequestMethod.GET, value = "/{aid}")
    public JsonResult getActivity(@PathVariable("aid") String aid){
        return activityService.findActivity(aid);
    }

    /**
     * Delete activity
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResult deleteActivity(@RequestParam("aid") String aid) {
        logger.info("createActivity");
        return activityService.deleteActivity(aid);
    }

    /**
     * Get child activities
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/children")
    public JsonResult getChildren(@PathVariable("aid") String aid) {
        logger.info("getChildren");
        return activityService.findChildren(aid);
    }

    /**
     * get participants of one activity
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.GET)
    public JsonResult getSubprojectParticipants(@PathVariable("aid") String aid){
        return activityService.findParticipants(aid);
    }

    /**
     * join an activity
     * @param aid
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.POST)
    public JsonResult joinSubproject(@PathVariable("aid") String aid, @RequestParam("userId") String userId){
        return activityService.joinActivity(aid, userId);
    }

    /**
     * join an activity
     * @param aid
     * @param userIds
     * @return
     */
    @RequestMapping(value = "/{aid}/userBatch", method = RequestMethod.POST)
    public JsonResult joinProjectBatch(@PathVariable String aid, @RequestParam HashSet<String> userIds) {
        return activityService.joinActivity(aid, userIds);
    }

    /**
     * change the role of member
     * @param aid
     * @param userId
     * @param role
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.PUT)
    public JsonResult changeUserRole(@PathVariable("aid") String aid, @RequestParam("userId") String userId, @RequestParam("role") String role){
        return activityService.updateMemberRole(aid, userId, role);
    }

    /**
     * Exit an activity
     * @param aid
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.DELETE)
    public JsonResult quitSubproject(@PathVariable("aid") String aid, @RequestParam("userId") String userId){
        return activityService.quitActivity(aid, userId);
    }

    /**
     * get ancestors of one activity
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/lineage", method = RequestMethod.GET)
    public JsonResult getLineage(@PathVariable("aid") String aid){
        return activityService.findLineage(aid);
    }

//    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/next")
//    public JsonResult getNextActivities(@PathVariable("aid") String aid) {
//        logger.info("getNextActivities");
//        return activityService.findNext(aid);
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/{aid}/last")
//    public JsonResult getLastActivities(@PathVariable("aid") String aid) {
//        logger.info("getLastActivities");
//        return activityService.findLast(aid);
//    }

//    /**
//     * Link two activity
//     * @param aid1
//     * @param aid2
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/link/{aid1}/{aid2}")
//    public JsonResult linkActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestParam("pid") String pid, @RequestBody UpdateActivityDTO update) {
//        logger.info("linkActivities");
//        return activityService.linkActivities(update, aid1, aid2, pid);
//    }
//
//    /**
//     * Separate two activities
//     * @param aid1
//     * @param aid2
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/separate/{aid1}/{aid2}")
//    public JsonResult separateActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestBody UpdateActivityDTO update) {
//        logger.info("separateActivities");
//        return activityService.separateActivities(update, aid1, aid2);
//    }
}

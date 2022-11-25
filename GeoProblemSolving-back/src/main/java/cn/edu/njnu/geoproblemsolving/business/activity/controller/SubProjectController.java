package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.service.SubprojectService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


@CrossOrigin(origins = "*",allowCredentials = "true")
@RestController
@RequestMapping("/subproject")
public class SubProjectController {

    private final SubprojectService subprojectService;

    public SubProjectController(SubprojectService subprojectService) {
        this.subprojectService = subprojectService;
    }

    /**
     * create subproject
     * @param subproject
     * @return
     */
    @RequestMapping(produces = {"application/json;charset=UTF-8"},method = RequestMethod.POST)
    public JsonResult createSubproject(@RequestBody Subproject subproject){
        return subprojectService.createSubproject(subproject);
    }

    /**
     * inquiry subproject
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public JsonResult readSubproject(@RequestParam("aid") String aid){
        return subprojectService.inquirySubproject(aid);
    }

    /**
     * zhengzhong 2020/11/26
     * find subProject
     * @param aid
     * @return
     */
    @RequestMapping(produces = {"application/json;charset=UTF-8"},method = RequestMethod.GET, value = "/{aid}")
    public JsonResult getSubProject(@PathVariable("aid") String aid){
        return subprojectService.findSubProject(aid);
    }
    /**
     * update subproject
     * @param subproject
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{aid}")
    public JsonResult updateSubproject(@PathVariable("aid") String aid, @RequestBody UpdateActivityDTO subproject){
        return subprojectService.updateSubproject(aid, subproject);
    }

    /**
     * delete subproject
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResult deleteSubproject(@RequestParam("aid") String aid){
        return subprojectService.deleteSubproject(aid);
    }

    @RequestMapping(value = "/{aid}/children", method = RequestMethod.GET)
    public JsonResult getAllActivities(@PathVariable("aid") String aid){
        JsonResult result = subprojectService.findChildren(aid);
        return result;
    }

    /**
     * get participants of one subproject
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.GET)
    public JsonResult getSubprojectParticipants(@PathVariable("aid") String aid){
        return subprojectService.findParticipants(aid);
    }

    /**
     * join a subproject
     * @param aid
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.POST)
    public JsonResult joinSubproject(@PathVariable("aid") String aid, @RequestParam("userId") String userId){
        return subprojectService.joinSubproject(aid, userId);
    }

    @RequestMapping(value = "/{aid}/userBatch", method = RequestMethod.POST)
    public JsonResult joinProjectBatch(@PathVariable String aid, @RequestParam HashSet<String> userIds) {
        return subprojectService.joinSubproject(aid, userIds);
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
        return subprojectService.updateMemberRole(aid, userId, role);
    }

    /**
     * Exit a subproject
     * @param aid
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.DELETE)
    public JsonResult quitSubproject(@PathVariable("aid") String aid, @RequestParam("userId") String userId){
        return subprojectService.quitSubproject(aid, userId);
    }

    /**
     * get ancestors of one subproject
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/lineage", method = RequestMethod.GET)
    public JsonResult getLineage(@PathVariable("aid") String aid){
        return subprojectService.findLineage(aid);
    }

//    /**
//     * Link two activity
//     * @param aid1
//     * @param aid2
//     * @param pid
//     * @param update
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/link/{aid1}/{aid2}")
//    public JsonResult linkActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestParam("pid") String pid, @RequestBody UpdateActivityDTO update) {
//        return subprojectService.linkActivities(update, aid1, aid2, pid);
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
//        return subprojectService.separateActivities(update, aid1, aid2);
//    }
}

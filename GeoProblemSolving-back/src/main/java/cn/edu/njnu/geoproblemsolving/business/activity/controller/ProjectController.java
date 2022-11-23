package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.Entity.EmailEntity;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateProjectDTO;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ProjectService;
import cn.edu.njnu.geoproblemsolving.View.StaticPagesBuilder;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/project")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final StaticPagesBuilder staticPagesBuilder;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
        this.staticPagesBuilder = new StaticPagesBuilder();
    }

    /**
     * Conditional query
     * @param category
     * @param tag
     * @param keyword
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public JsonResult inquiryByPage(@RequestParam("category") String category,
                                @RequestParam("tag") String tag,
                                @RequestParam("keyword") String keyword,
                                @RequestParam("userId") String userId,
                                @RequestParam("page") int page,
                                @RequestParam("pageSize") int pageSize){
        JsonResult result = projectService.inquiryByConditions(category, tag, keyword, userId, page, pageSize);
        return result;
    }

    /**
     * Create project
     * @param project
     * @return
     * @throws IOException
     */
    @RequestMapping(produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    public JsonResult createProject(@RequestBody Project project) throws IOException {
        logger.info("createProject");
        JsonResult result = projectService.createProject(project);

        // Thymeleaf
        if(result.getCode() == 0){
            //将内容同步内容放到这个部分
            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
            staticPagesBuilder.projectListPageBuilder(projectService.findProjectsByPage(1,18));
        }

        return result;
    }

    /**
     * Updata project
     * @param project
     * @return
     * @throws IOException
     */
    @RequestMapping(produces = {"application/json;charset=UTF-8"}, method = RequestMethod.PUT, value = "/{aid}")
    public JsonResult updateProject(@PathVariable("aid") String aid, @RequestBody UpdateProjectDTO project) throws IOException {
        JsonResult result = projectService.updateProject(aid, project);
        // Thymeleaf
        if(result.getCode() == 0) {
            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
        }
        return result;
    }

    /**
     * get project
     * @param aid
     * @return
     */
    @RequestMapping(produces = {"application/json;charset=UTF-8"}, method = RequestMethod.GET, value = "/{aid}")
    public JsonResult getProject(@PathVariable("aid") String aid){
        JsonResult project = projectService.findProject(aid);
        return project;
    }

    /**
     * get projects
     * @param projectIds
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getProjects")
    public JsonResult getProjects(@RequestParam("aids") ArrayList<String> projectIds){
        try {
            ArrayList<Project> projects = new ArrayList<>();
            for (String projectId: projectIds){{
                JsonResult findResult = projectService.findProject(projectId);
                Project project = (Project)findResult.getData();
                projects.add(project);
            }}
            return ResultUtils.success(projects);
        } catch (Exception ex){
            return ResultUtils.error(-1, ex.toString());
        }
    }

    /**
     * Delete project
     * @param aid
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResult deleteProject(@RequestParam("aid") String aid) {
        JsonResult result = projectService.deleteProject(aid);
        // Thymeleaf
        if(result.getCode() == 0) {
            staticPagesBuilder.projectDetailPageRemove(aid);
            staticPagesBuilder.projectListPageBuilder(projectService.findProjectsByPage(1, 18));
        }
        return result;
    }

    /**
     * Get children of project
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/children", method = RequestMethod.GET)
    public JsonResult getAllActivities(@PathVariable("aid") String aid){
        JsonResult result = projectService.findChildren(aid);
        return result;
    }

    /**
     * Inquiry the creator and members of one project
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.GET)
    public JsonResult getParticipants(@PathVariable("aid") String aid){
        JsonResult result = projectService.findParticipants(aid);
        return result;
    }

    /**
     * Add a member to a project
     * @param aid
     * @param userId
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.POST)
    public JsonResult joinProject(@PathVariable("aid") String aid, @RequestParam("userId") String userId) throws IOException {
        JsonResult result = projectService.joinProject(aid, userId);

        if(result.getCode() == 0) {
            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
            staticPagesBuilder.projectListPageBuilder(projectService.findProjectsByPage(1, 18));
        }
        return result;
    }

    /**
     * Delete a member of a project
     * @param aid
     * @param userId
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.DELETE)
    public JsonResult quitProject(@PathVariable("aid") String aid, @RequestParam("userId") String userId) throws IOException {
        JsonResult result = projectService.quitProject(aid, userId);
        if(result.getCode() == 0) {
            JsonResult project = projectService.findProject(aid);
            staticPagesBuilder.projectDetailPageBuilder((Project) project.getData());
            staticPagesBuilder.projectListPageBuilder(projectService.findProjectsByPage(1, 18));
        }
        return result;
    }

    /**
     * Update a member role
     * @param aid
     * @param userId
     * @param role
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{aid}/user", method = RequestMethod.PUT)
    public JsonResult changeUserRole(@PathVariable("aid") String aid, @RequestParam("userId") String userId, @RequestParam("role") String role) throws IOException {
        JsonResult result = projectService.updateMemberRole(aid, userId, role);

        if(result.getCode() == 0) {
            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
        }
        return result;
    }

    /**
     * join a project via email
     * @param aid
     * @param email
     * @param password
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{aid}/invitation", method = RequestMethod.POST)
    public JsonResult joinByMail(@PathVariable("aid") String aid, @RequestParam("email") String email, @RequestParam("password") String password) throws IOException {
        JsonResult result = projectService.invitedParticipants(aid, email, password);

        if(result.getCode() == 0) {
            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
        }
        return result;
    }

    /**
     * send emails to apply to join a project
     * @param aid
     * @param emailEntity
     * @return
     */
    @RequestMapping(value = "/{aid}/application", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    public JsonResult sendEmail(@PathVariable("aid") String aid, @RequestBody EmailEntity emailEntity) {
        return projectService.applyJoinProject(aid, emailEntity);
    }


//    /**
//     * Link two activity
//     * @param aid1
//     * @param aid2
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/link/{aid1}/{aid2}")
//    public JsonResult linkActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestParam("pid") String pid, @RequestBody UpdateActivityDTO update) throws IOException {
//        logger.info("linkActivities");
//        JsonResult result =  projectService.linkActivities(update, aid1, aid2, pid);
//
//        if(result.getCode() == 0) {
//            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
//        }
//        return result;
//    }
//
//    /**
//     * Separate two activities
//     * @param aid1
//     * @param aid2
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/separate/{aid1}/{aid2}")
//    public JsonResult separateActivities(@PathVariable("aid1") String aid1, @PathVariable("aid2") String aid2, @RequestBody UpdateActivityDTO update) throws IOException {
//        logger.info("separateActivities");
//        JsonResult result = projectService.separateActivities(update, aid1, aid2);
//
//        if(result.getCode() == 0) {
//            staticPagesBuilder.projectDetailPageBuilder((Project) result.getData());
//        }
//        return result;
//    }

}

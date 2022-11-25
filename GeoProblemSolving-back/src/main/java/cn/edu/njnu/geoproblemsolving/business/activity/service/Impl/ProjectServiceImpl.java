package cn.edu.njnu.geoproblemsolving.business.activity.service.Impl;

import cn.edu.njnu.geoproblemsolving.Dao.Email.EmailDaoImpl;
import cn.edu.njnu.geoproblemsolving.Dao.Folder.FolderDaoImpl;
import cn.edu.njnu.geoproblemsolving.View.StaticPagesBuilder;
import cn.edu.njnu.geoproblemsolving.business.activity.ProjectUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateProjectDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.service.ToolService;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.Entity.EmailEntity;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ProjectService;
import cn.edu.njnu.geoproblemsolving.business.user.repository.UserRepository;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SubprojectRepository subprojectRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FolderDaoImpl folderDao;
    private final ProjectUtil projectUtil;
    private final ToolService toolService;
    private final NodeService nodeService;
    private final DocParseServiceImpl docParseService;

    public ProjectServiceImpl(ProjectRepository projectRepository, SubprojectRepository subprojectRepository, ActivityRepository activityRepository, UserRepository userRepository, FolderDaoImpl folderDao, ProjectUtil projectUtil, ToolService toolService, NodeService nodeService, DocParseServiceImpl docParseService) {
        this.projectRepository = projectRepository;
        this.subprojectRepository = subprojectRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.folderDao = folderDao;
        this.projectUtil = projectUtil;
        this.toolService = toolService;
        this.nodeService = nodeService;
        this.docParseService = docParseService;
    }

    private UserEntity findByUserId(String userId) {
        Optional optional = userRepository.findById(userId);
        if (optional.isPresent()) {
            Object user = optional.get();
            return (UserEntity) user;
        } else {
            return new UserEntity();
        }
    }

    @Override
    public JsonResult findProject(String aid) {
        Optional optional = projectRepository.findById(aid);

        if (optional.isPresent()) {
            Project project = (Project) optional.get();

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            project.setActiveTime(dateFormat.format(new Date()));
            projectRepository.save(project);

            return ResultUtils.success(project);
        } else {
            return ResultUtils.error(-1, "None");
        }
    }


    @Override
    public JSONObject findProjectsByPage(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Project> projects = projectRepository.findAll(pageable);
            JSONObject result = new JSONObject();
            result.put("totalPage", projects.getTotalPages());
            result.put("count", projects.getTotalElements());
            result.put("projectList", projects.getContent());
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public JsonResult inquiryByConditions(String category, String tag, String keyword, String userId, int page, int size) {
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Page<Project> projects = null;
            if (keyword.equals("")) {
                if (userId.equals("")) {
                    if (category.equals("All")) {
                        projects = projectRepository.findProjectsByPrivacyIsNot("Private", pageable);
                    } else {
                        if (tag.equals("")) {
                            projects = projectRepository.findProjectsByPrivacyIsNotAndCategoryEquals("Private", category, pageable);
                        } else {
                            projects = projectRepository.findProjectsByPrivacyIsNotAndCategoryEqualsAndTagContaining("Private", category, tag, pageable);
                        }
                    }
                } else {
                    if (category.equals("All")) {
                        projects = projectRepository.findProjectsByPrivacyIsAndCreatorIsOrPrivacyIsNot("Private", userId, "Private", pageable);
                    } else {
                        if (tag.equals("")) {
                            projects = projectRepository.findProjectsByPrivacyIsAndCreatorIsOrPrivacyIsNotAndCategoryEquals("Private", userId, "Private", category, pageable);
                        } else {
                            projects = projectRepository.findProjectsByPrivacyIsAndCreatorIsOrPrivacyIsNotAndCategoryEqualsAndTagContaining("Private", userId, "Private", category, tag, pageable);
                        }
                    }
                }
            } else {
                if (userId.equals("")) {
                    projects = projectRepository.findProjectsByNameLikeOrDescriptionLikeAndPrivacyIsNot(keyword, keyword, "Private", pageable);
                } else {
                    projects = projectRepository.findProjectsByNameLikeOrDescriptionLikeAndPrivacyIsAndCreatorIsOrPrivacyIsNot(keyword, keyword, "Private", userId, "Private", pageable);
                }
            }

            JSONObject result = new JSONObject();
            result.put("totalPage", projects.getTotalPages());
            result.put("count", projects.getTotalElements());
            result.put("projectList", projects.getContent());

            if (projects.isEmpty())
                return ResultUtils.error(-1, "None");
            else
                return ResultUtils.success(result);

        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult createProject(Project project) {
        try {
            /**
             * Save project info
             */
            // Created time and active time
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            project.setCreatedTime(dateFormat.format(date));
            project.setActiveTime(dateFormat.format(date));

            // Aid
            String projectId = UUID.randomUUID().toString();
            project.setAid(projectId);

            // Set project member
            String creatorId = project.getCreator();
            JSONArray members = new JSONArray();
            JSONObject creator = new JSONObject();

            creator.put("userId", creatorId);
            creator.put("role", "manager");
            members.add(creator);

            project.setMembers(members);

            // set type
            project.setType(project.getType());
            if (project.getType().equals(ActivityType.Activity_Group)) {
                project.setChildren(new ArrayList<>());
            }

            // children
            ArrayList<String> children = new ArrayList<>();
            project.setChildren(children);

            /**
             * Update user info
             */
            // Update user information
            UserEntity user = findByUserId(creator.getString("userId"));
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");

            ArrayList<String> createdProjects = user.getCreatedProjects();
            if (createdProjects == null) {
                createdProjects = new ArrayList<>();
            }
            createdProjects.add(projectId);
            user.setCreatedProjects(createdProjects);
            /**
             * Resource folder
             */
            folderDao.createFolder(project.getName(), "", projectId);

            projectRepository.save(project);
            userRepository.save(user);


            return ResultUtils.success(project);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    public JsonResult updateProject(String aid, UpdateProjectDTO update) {
        try {
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent())
                return ResultUtils.error(-1, "Fail: no this project");

            Project project = (Project) optional.get();

            // if original project's privacy is private
            Boolean updateProjectListPage = false;
            if (update.getPrivacy() != null && update.getPrivacy().equals(project.getPrivacy())) {
                updateProjectListPage = isUpdateProjectListStaticPage(aid, !project.getPrivacy().equals("Private"));
            }


            //补充绑定对于 purpose 的工具
            //mongoTemplate 底层实现为 arrayList
            List<Tool> relevantPurposeTool = new ArrayList<>();
            if (
                    update.getType() != null && update.getPurpose() != null &&
                    (update.getType().equals(ActivityType.Activity_Unit)
                    && !project.getType().equals(ActivityType.Activity_Unit)
                    || !project.getPurpose().equals(update.getPurpose()))) {
                HashSet<String> toolSet = new HashSet<>();
                relevantPurposeTool = toolService.getRelevantPurposeTool(update.getPurpose());
                if (!relevantPurposeTool.isEmpty()){
                    for (Tool tool : relevantPurposeTool) {
                        toolSet.add(tool.getTid());
                    }
                }
                update.setToolList(toolSet);
            }

            ActivityType oldType = project.getType();
            String oldName = project.getName();
            String oldDesc = project.getDescription();
            update.updateTo(project);
            projectRepository.save(project);

            //activityType 发生改变
            if (update.getType() != null && !oldType.equals(update.getType())){
                docParseService.changeActivityType(aid, project);
            }

            if (!relevantPurposeTool.isEmpty()){
                //单活动类型发生变化,需要文档中工具部分内容
                docParseService.refreshTool(aid, relevantPurposeTool);
            }
            if ((update.getName() != null && !oldName.equals(update.getName())) || (update.getDescription() != null && !oldDesc.equals(update.getDescription()))){
                HashMap<String, String> updateInfo = new HashMap<>();
                updateInfo.put("name", update.getName());
                updateInfo.put("description", update.getDescription());
                docParseService.updateRoot(aid, updateInfo);
            }

            StaticPagesBuilder staticPagesBuilder = new StaticPagesBuilder();
            if (updateProjectListPage) {
                staticPagesBuilder.projectListPageBuilder(findProjectsByPage(1, 18));
            }



            return ResultUtils.success(project);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    private Boolean isUpdateProjectListStaticPage(String projectId, Boolean noPrivate) {
        JsonResult result = inquiryByConditions("", "", "", "", 1, 18);
        try {
            List<Project> projects = (List<Project>) ((JSONObject) result.getData()).get("projectList");
            long count = projects.size();
            if (count < 18 && noPrivate) { //如果首页数量少于页面数量规格且有新的可见条目，则更新
                return true;
            }
            for (Project project : projects) {
                if (projectId.equals(project.getAid())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            return false;
        }

        return false;
    }

    @Override
    public JsonResult deleteProject(String aid) {
        try {
            // Update user information
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: does not exist");
            Project project = (Project) optional.get();

            // joined project
            JSONArray members = project.getMembers();
            for (Object member : members) {
                String userId = (String) ((HashMap) member).get("userId");
                quitProject(aid, userId);
            }

            // created project
            String userId = project.getCreator();
            UserEntity user = findByUserId(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");
            ArrayList<String> projectIds = user.getCreatedProjects();
            projectIds.removeIf(projectId -> projectId.equals(aid));
            user.setCreatedProjects(projectIds);
            userRepository.save(user);

            // delete children
            if (project.getChildren() != null) {
                deleteChildren(project);
            }

            // delete project
            projectRepository.deleteById(aid);
            docParseService.deleteDoc(aid);

            return ResultUtils.success("Success");

        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    private void deleteChildren(Activity activity) {
        for (String childId : activity.getChildren()) {
            if (activity.getLevel() == 1) {
                Optional optional = activity.getLevel() > 1 ? activityRepository.findById(childId) : subprojectRepository.findById(childId);
                if (optional.isPresent()) {
                    Activity child = (Activity) optional.get();
                    if (child.getChildren() == null) {
                        if (activity.getLevel() > 1) {
                            activityRepository.deleteById(childId);
                        } else {
                            subprojectRepository.deleteById(childId);
                        }
                    } else {
                        deleteChildren(child);
                    }
                }
            }
        }
    }

    @Override
    public JsonResult findChildren(String aid) {
        try {
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            JSONArray children = new JSONArray();
            if (project.getChildren() != null) {
                for (String childId : project.getChildren()) {
                    optional = subprojectRepository.findById(childId);
                    if (optional.isPresent()) {
                        Subproject childActivity = (Subproject) optional.get();
                        children.add(childActivity);
                    }
                }
            }

            return ResultUtils.success(children);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult findParticipants(String aid) {
        try {
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            JSONObject participants = new JSONObject();
            UserEntity creator = findByUserId(project.getCreator());
            participants.put("creator", creator);

            JSONArray members = project.getMembers();
            if (members == null) members = new JSONArray();
            JSONArray memberInfos = new JSONArray();
            for (Object member : members) {
                String userId = (String) ((HashMap) member).get("userId");
                UserEntity user = findByUserId(userId);
                JSONObject userInfo = new JSONObject();
                userInfo.put("userId", userId);
                userInfo.put("role", ((HashMap) member).get("role"));
                userInfo.put("name", user.getName());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("email", user.getEmail());
                userInfo.put("title", user.getTitle());
                userInfo.put("domain", user.getDomain());
                memberInfos.add(userInfo);
            }
            participants.put("members", memberInfos);

            return ResultUtils.success(participants);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult joinProject(String aid, String userId) {
        try {
            // confirm
            UserEntity user = findByUserId(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            // add user info to project
            // if user exist in project?
            JSONArray members = project.getMembers();
            if (members == null) members = new JSONArray();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    return ResultUtils.error(-3, "Fail: member already exists in the project");
                }
            }

            JSONObject newUser = new JSONObject();
            newUser.put("userId", userId);
            newUser.put("role", "ordinary-member");
            members.add(newUser);
            project.setMembers(members);

            // update user info
            // if user exist in project?
            ArrayList<String> joinedProjects = user.getJoinedProjects();
            if (joinedProjects == null) joinedProjects = new ArrayList<>();
            for (String projectId : joinedProjects) {
                if (projectId.equals(aid))
                    return ResultUtils.error(-3, "Fail: project already exists in personal joined projects");
            }

            joinedProjects.add(aid);
            user.setJoinedProjects(joinedProjects);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            project.setActiveTime(dateFormat.format(new Date()));

            //save
            projectRepository.save(project);
            userRepository.save(user);



            StaticPagesBuilder staticPagesBuilder = new StaticPagesBuilder();
            staticPagesBuilder.projectDetailPageBuilder(project);

            //更新文档
            docParseService.userJoin(aid, userId);
            // docParser.userJoin(aid, userId);
            //update node
            nodeService.addOrPutUserToNode(aid, userId, "ordinary-member");

            return ResultUtils.success(project);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult joinProject(String aid, HashSet<String> userIds) throws IOException {
        Optional<Project> optional = projectRepository.findById(aid);
        if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project( "+ aid + ")does not exist.");
        Project project = optional.get();
        JSONArray members = project.getMembers();
        if (members == null) members = new JSONArray();
        try {
            //Do not process if the user exists.
            for (Object member : members){
                String userId =  (String)((HashMap) member).get("userId");
                for (Iterator<String> it = userIds.iterator(); it.hasNext();){
                    String uid = it.next();
                    if (uid.equals(userId)){
                        it.remove();
                    }
                }
            }
            for (String uid : userIds){
                Optional<UserEntity> byId = userRepository.findById(uid);
                if (!byId.isPresent()) continue;
                UserEntity userEntity = byId.get();
                JSONObject newMember = new JSONObject();
                newMember.put("userId", uid);
                newMember.put("role", "ordinary-member");
                members.add(newMember);
                ArrayList<String> joinedProjects = userEntity.getJoinedProjects();
                joinedProjects.remove(uid);
                joinedProjects.add(uid);
                //update user
                userRepository.save(userEntity);
            }
            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            project.setActiveTime(dateFormat.format(new Date()));
            projectRepository.save(project);


            //更新文档
            docParseService.userJoin(aid, userIds);
            // docParser.userJoin(aid, userIds);

            //update node
            nodeService.addUserToNodeBatch(aid, userIds);

            StaticPagesBuilder staticPagesBuilder = new StaticPagesBuilder();
            staticPagesBuilder.projectDetailPageBuilder(project);

            return ResultUtils.success(project);
        }catch (Exception e){
            return ResultUtils.error(-2, e.toString());
        }
    }

    @Override
    public JsonResult updateMemberRole(String aid, String userId, String role) {
        try {
            // check
            UserEntity user = findByUserId(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");

            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            // Update roles
            JSONArray members = project.getMembers();
            JSONArray newMembers = new JSONArray();
            if (members == null) members = new JSONArray();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    JSONObject userInfo = new JSONObject();
                    userInfo.put("userId", userId);
                    userInfo.put("role", role);
                    newMembers.add(userInfo);
                } else {
                    ObjectMapper personMap = new ObjectMapper();
                    String personStr = personMap.writeValueAsString(member);
                    JSONObject userInfo = JSONObject.parseObject(personStr);
                    newMembers.add(userInfo);
                }
            }
            project.setMembers(newMembers);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            project.setActiveTime(dateFormat.format(new Date()));

            projectRepository.save(project);
            nodeService.addOrPutUserToNode(aid, userId, role);

            return ResultUtils.success(project);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult quitProject(String aid, String userId) {
        try {
            // check
            UserEntity user = findByUserId(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");

            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            // remove the user from the project
            JSONArray members = project.getMembers();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    members.remove(member);
                    break;
                }
            }
            project.setMembers(members);

            // update user info
            ArrayList<String> joinedProjects = user.getJoinedProjects();
            if (joinedProjects != null) {
                joinedProjects.removeIf(projectId -> projectId.equals(aid));
                user.setJoinedProjects(joinedProjects);
            }

            //save
            projectRepository.save(project);
            userRepository.save(user);
            nodeService.userExitActivity(aid, userId);
            //判断是否为临时用户，若是则删除，临时用户的id长度为两个uuid
            if (userId.length() > 40){
                userRepository.deleteById(userId);
            }

            //退出子活动
            projectUtil.quitSubProject(aid, userId, 0);

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult invitedParticipants(String aid, String email, String password) {
        try {
            UserEntity user = userRepository.findByEmail(email);
            if (user == null) {
                user = new UserEntity();
                user.setUserId(UUID.randomUUID().toString());
                user.setEmail(email);
                user.setPassword(password);
                userRepository.save(user);
                return joinProject(aid, user.getUserId());
            } else {
                if (user.getPassword().equals(password)) {
                    return joinProject(aid, user.getUserId());
                } else {
                    return ResultUtils.error(-2, "Fail: Wrong password");
                }
            }
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult applyJoinProject(String aid, EmailEntity emailEntity) {
        try {
            // check
            Optional optional = projectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist");
            Project project = (Project) optional.get();

            String addresses = "";
            JSONArray members = project.getMembers();
            for (Object member : members)
                if (((HashMap) member).get("role").equals("manager")) {
                    String userId = ((JSONObject) member).getString("userId");

                    UserEntity user = findByUserId(userId);
                    if (user == null) return ResultUtils.error(-1, "Fail: user does not exist");
                    addresses += user.getEmail();
                    addresses += ",";
                }
            addresses = addresses.substring(0, addresses.length() - 1);
            emailEntity.setRecipient(addresses);
            EmailDaoImpl emailDao = new EmailDaoImpl();
            emailDao.sendEmail(emailEntity);

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

//    @Override
//    public JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid) {
//        try {
//            // Confirm aid
//            Optional optional = projectRepository.findById(update.getAid());
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
//            Project project = (Project) optional.get();
//            optional = subprojectRepository.findById(aid1);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Subproject activity1 = (Subproject) optional.get();
//            optional = subprojectRepository.findById(aid2);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Subproject activity2 = (Subproject) optional.get();
//
//            // Save the pathway
//            update.updateTo(project);
//
//            // Save the protocol------------------------------------------------待完善----------------------------------
//            // String pid = protocolRepository.save(protocol).getPid();
//
//            // Save activities
//            subprojectRepository.save(saveLastActivityInfo(aid2, pid, activity1));
//            subprojectRepository.save(saveNextActivityInfo(aid1, pid, activity2));
//            projectRepository.save(project);
//
//            return ResultUtils.success(project);
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }

//    @Override
//    public JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid) {
//        try {
//            // Confirm aid
//            Optional optional = projectRepository.findById(update.getAid());
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
//            Project project = (Project) optional.get();
//            optional = subprojectRepository.findById(lastAid);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Subproject activity1 = (Subproject) optional.get();
//            optional = subprojectRepository.findById(nextAid);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Subproject activity2 = (Subproject) optional.get();
//
//            // Save the pathway
//            update.updateTo(project);
//
//            // Save the last activity
//            JSONArray nextActivities = activity1.getNext();
//
//            IntStream.range(0, nextActivities.size()).forEach(i -> {
//                String aid = nextActivities.getJSONObject(i).getString("aid");
//                if (aid.equals(nextAid)) {
//                    nextActivities.remove(i);
//                }
//            });
//            activity1.setNext(nextActivities);
//
//            // Save the next activity
//            JSONArray lastActivities = activity2.getLast();
//
//            IntStream.range(0, lastActivities.size()).forEach(i -> {
//                String aid = lastActivities.getJSONObject(i).getString("aid");
//                if (aid.equals(lastAid)) {
//                    lastActivities.remove(i);
//                }
//            });
//            activity2.setLast(lastActivities);
//
//            // Save activities
//            projectRepository.save(project);
//            subprojectRepository.save(activity1);
//            subprojectRepository.save(activity2);
//
//            return ResultUtils.success(project);
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }

//    private Subproject saveLastActivityInfo(String aid, String pid, Subproject last) {
//        JSONObject nextInfo = new JSONObject();
//        nextInfo.put("aid", aid);
//        nextInfo.put("protocolId", pid);
//
//        JSONArray nextActivities = last.getNext();
//        if (nextActivities == null) nextActivities = new JSONArray();
//        nextActivities.add(nextInfo);
//        last.setNext(nextActivities);
//
//        return last;
//    }

//    private Subproject saveNextActivityInfo(String aid, String pid, Subproject next) {
//        JSONObject lastInfo = new JSONObject();
//        lastInfo.put("aid", aid);
//        lastInfo.put("protocolId", pid);
//
//        JSONArray lastActivities = next.getLast();
//        if (lastActivities == null) lastActivities = new JSONArray();
//        lastActivities.add(lastInfo);
//        next.setLast(lastActivities);
//
//        return next;
//    }
}

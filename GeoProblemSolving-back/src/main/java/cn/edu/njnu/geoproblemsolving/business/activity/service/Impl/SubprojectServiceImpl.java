package cn.edu.njnu.geoproblemsolving.business.activity.service.Impl;

import cn.edu.njnu.geoproblemsolving.Dao.Folder.FolderDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.ProjectUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.service.ToolService;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.service.SubprojectService;
import cn.edu.njnu.geoproblemsolving.business.user.repository.UserRepository;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class SubprojectServiceImpl implements SubprojectService {

    private final SubprojectRepository subprojectRepository;
    private final ProjectRepository projectRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FolderDaoImpl folderDao;
    private final ProjectUtil projectUtil;
    private final ToolService toolService;
    private final NodeService nodeService;
    private final DocParseServiceImpl docParseService;

    public SubprojectServiceImpl(ToolService toolService, SubprojectRepository subprojectRepository, ProjectRepository projectRepository, ActivityRepository activityRepository, UserRepository userRepository, FolderDaoImpl folderDao, ProjectUtil projectUtil, NodeService nodeService, DocParseServiceImpl docParseService) {
        this.subprojectRepository = subprojectRepository;
        this.projectRepository = projectRepository;
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

    private void updateActiveTime(Subproject subproject) {
        // Update active time
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        subproject.setActiveTime(dateFormat.format(date));

        subprojectRepository.save(subproject);
    }

    @Override
    public JsonResult createSubproject(Subproject subproject) {
        try {
            Date data = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String subprojectId = UUID.randomUUID().toString();

            // Created time
            subproject.setCreatedTime(dateFormat.format(data));
            subproject.setActiveTime(dateFormat.format(data));

            // Aid
            subproject.setAid(subprojectId);

            // members
            String creatorId = subproject.getCreator();
            JSONObject creator = new JSONObject();
            JSONArray members = new JSONArray();

            creator.put("userId", creatorId);
            creator.put("role", "manager");
            members.add(creator);

            subproject.setMembers(members);

            // set type
            subproject.setType(subproject.getType());
            // 1-初始化文档
            docParseService.initActivityDoc(subproject);
            // docParser.initActivityDoc(subproject);
            if (subproject.getType().equals(ActivityType.Activity_Group)) {
                subproject.setChildren(new ArrayList<>());
            }else if (subproject.getType().equals(ActivityType.Activity_Unit)){
                //Bind the relevant tool
                String purpose = subproject.getPurpose();
                List<Tool> relevantPurposeTool = toolService.getRelevantPurposeTool(purpose);
                HashSet<String> toolSet = new HashSet<>();
                if (relevantPurposeTool != null && !relevantPurposeTool.isEmpty()){
                    for (Tool tool : relevantPurposeTool){
                        toolSet.add(tool.getTid());
                    }
                    subproject.setToolList(toolSet);
                }

                //若是 Activity_Unit, 则绑定工具
                //2-绑定工具
                docParseService.refreshTool(subprojectId, relevantPurposeTool);
                // docParser.addTools(subprojectId, relevantPurposeTool);
            }

            // folder
            folderDao.createFolder(subproject.getName(), "", subprojectId);
            // update project info
            String projectId = subproject.getParent();
            Project project = projectRepository.findById(projectId).get();
            if (project == null) return ResultUtils.error(-1, "Fail: project does not exist.");
            ArrayList<String> children;
            if (project.getChildren() == null) {
                children = new ArrayList<>();
            } else {
                children = project.getChildren();
            }
            children.add(subprojectId);
            project.setChildren(children);

            // Save
            subprojectRepository.save(subproject);
            projectRepository.save(project);
            /*
            activity doc operation
            1. init subproject document
            2. append child activity
            3. add tool, if need
             */
            //3-添加依赖
            docParseService.appendChildActivity(projectId, subprojectId, subproject.getName(), creatorId);
            // docParser.appendChildActivity(projectId, subprojectId, subproject.getName(), creatorId);


            return ResultUtils.success(subproject);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult inquirySubproject(String aid) {
        try {
            Optional result = subprojectRepository.findById(aid);
            if (result.isPresent()) {
                Subproject subproject = (Subproject) result.get();
                // Update active time
                updateActiveTime(subproject);
                return ResultUtils.success(subproject);
            } else {
                return ResultUtils.error(-1, "Fail: subproject does not exist.");
            }
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult updateSubproject(String aid, UpdateActivityDTO update) {
        try {
            // confirm
            Optional result = subprojectRepository.findById(aid);
            if (!result.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");

            Subproject subproject = (Subproject) result.get();

            String purpose = update.getPurpose();
            List<Tool> relevantPurposeTool = new ArrayList<>();
            if (
                    update.getType() != null && update.getPurpose() != null &&
                    (update.getType().equals(ActivityType.Activity_Unit) &&
                    !subproject.getType().equals(ActivityType.Activity_Unit) ||
                    !subproject.getPurpose().equals(purpose)
            )){
                relevantPurposeTool = toolService.getRelevantPurposeTool(purpose);
                HashSet<String> toolSet = new HashSet<>();
                if (!relevantPurposeTool.isEmpty()){
                    for (Tool tool : relevantPurposeTool){
                        toolSet.add(tool.getTid());
                    }
                }
                update.setToolList(toolSet);
            }

            ActivityType oldType = subproject.getType();
            String oldName = subproject.getName();
            String oldDesc = subproject.getDescription();
            update.updateTo(subproject);

            //activityType 发生改变
            if (update.getType() != null && !oldType.equals(update.getType())){
                // docParser.changeActivityType(aid, subproject);
                docParseService.changeActivityType(aid, subproject);
            }

            if (!relevantPurposeTool.isEmpty()){
                //更新内容
                docParseService.refreshTool(aid, relevantPurposeTool);
                // docParser.putTools(aid, relevantPurposeTool);
            }

            if (update.getName() != null && !oldName.equals(update.getName())){
                HashMap<String, String> updateInfo = new HashMap<>();
                updateInfo.put("name", update.getName());
                docParseService.updateRoot(aid, updateInfo);
                //name 改变, 需要改变 父级的child标签
                docParseService.updateChild(subproject.getParent(), aid, update.getName());
            }
            if (update.getDescription() != null && !oldDesc.equals(update.getDescription())){
                HashMap<String, String> updateInfo = new HashMap<>();
                updateInfo.put("description", update.getDescription());
                docParseService.updateRoot(aid, updateInfo);
            }
            /*
            文档更新分为三块
            1. 类型改变
            2. purpose 改变
            3. 文档名字发生改变就更改父文档
             */


            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(dateFormat.format(new Date()));

            return ResultUtils.success(subprojectRepository.save(subproject));
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult deleteSubproject(String aid) {
        try {
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Subproject subproject = (Subproject) optional.get();
            optional = projectRepository.findById(subproject.getParent());
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Project project = (Project) optional.get();

            // delete from parent
            if (project.getChildren().contains(aid))
                project.getChildren().remove(aid);

            // delete children
            if (subproject.getChildren() != null) {
                deleteChildren(subproject);
            }

            projectRepository.save(project);
            subprojectRepository.deleteById(aid);

            //文档删除
            docParseService.deleteDoc(aid, subproject.getParent());
            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    private void deleteChildren(Activity activity) {
        for (String childId : activity.getChildren()) {
            Optional optional = activityRepository.findById(childId);
            if (optional.isPresent()) {
                Activity child = (Activity) optional.get();
                if (child.getChildren() == null) {
                    activityRepository.deleteById(childId);
                } else {
                    deleteChildren(child);
                }
            }
        }
    }

    @Override
    public JsonResult findChildren(String aid) {
        try {
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist");
            Subproject subproject = (Subproject) optional.get();

            ArrayList<Activity> children = getChildActivities(subproject);

            // Update active time
            updateActiveTime(subproject);

            return ResultUtils.success(children);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    private ArrayList getChildActivities(Activity activity) {
        ArrayList<Activity> activities = new ArrayList<>();

        if (activity.getChildren() != null) {
            for (String childId : activity.getChildren()) {
                Optional optional = activity.getLevel() > 0 ? activityRepository.findById(childId) : subprojectRepository.findById(childId);
                if (optional.isPresent()) {
                    Activity childActivity = (Activity) optional.get();
                    activities.add(childActivity);
                }
            }
        }
        return activities;
    }

    @Override
    public JsonResult findParticipants(String aid) {
        try {
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Subproject subproject = (Subproject) optional.get();

            // creator
            JSONObject participants = new JSONObject();
            UserEntity creator = findByUserId(subproject.getCreator());
            participants.put("creator", creator);

            // members
            JSONArray members = subproject.getMembers();
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

            // Update active time
            updateActiveTime(subproject);

            return ResultUtils.success(participants);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult findLineage(String aid) {
        try {
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Activity activity = (Activity) optional.get();

            // Update active time
            updateActiveTime((Subproject) optional.get());

            // children
            ArrayList<Activity> children = getChildActivities(activity);

            // ancestors
            ArrayList<Activity> ancestors = new ArrayList<>();
            ancestors.add(activity);

            optional = projectRepository.findById(activity.getParent());
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist.");
            activity = (Activity) optional.get();
            ancestors.add(activity);

            // brothers
            ArrayList<Activity> brothers = getChildActivities(ancestors.get(1));

            // result
            JSONObject lineage = new JSONObject();
            lineage.put("ancestors", ancestors);
            lineage.put("brothers", brothers);
            lineage.put("children", children);

            return ResultUtils.success(lineage);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult findSubProject(String aid) {
        Optional<Subproject> optional = subprojectRepository.findById(aid);
        if (optional.isPresent()) {
            Subproject subproject = optional.get();

            //Update active time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(simpleDateFormat.format(new Date()));
            subprojectRepository.save(subproject);

            return ResultUtils.success(subproject);
        } else {
            return ResultUtils.error(-1, "None");
        }
    }

    @Override
    public JsonResult joinSubproject(String aid, String userId) {
        try {
            // confirm
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            UserEntity user = findByUserId(userId);
            // UserEntity user = projectUtil.getUserInfoFromUserServer(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist.");

            // add user info to subproject
            // if user exist in subproject?
            Subproject subproject = (Subproject) optional.get();
            JSONArray members = subproject.getMembers();
            if (members == null) members = new JSONArray();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    return ResultUtils.error(-3, "Fail: member already exists in the subproject");
                }
            }

            JSONObject newUser = new JSONObject();
            newUser.put("userId", userId);
            newUser.put("role", "ordinary-member");
            members.add(newUser);
            subproject.setMembers(members);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(dateFormat.format(new Date()));

            subprojectRepository.save(subproject);

            //更新文档
            docParseService.userJoin(aid, userId);
            //update node
            nodeService.addOrPutUserToNode(aid, userId,"ordinary-member");

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult joinSubproject(String aid, HashSet<String> userIds) {
        Optional<Subproject> optional = subprojectRepository.findById(aid);
        if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project( "+ aid + ")does not exist.");
        Subproject subproject = optional.get();
        JSONArray members = subproject.getMembers();
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
                JSONObject newMember = new JSONObject();
                newMember.put("userId", uid);
                newMember.put("role", "ordinary-member");
                members.add(newMember);
            }

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(dateFormat.format(new Date()));

            subprojectRepository.save(subproject);

            //update doc
            docParseService.userJoin(aid, userIds);
            //update node
            nodeService.addUserToNodeBatch(aid, userIds);

            return ResultUtils.success(members);
        }catch (Exception e){
            return ResultUtils.error(-2, e.toString());
        }
    }

    @Override
    public JsonResult updateMemberRole(String aid, String userId, String role) {
        try {
            // check
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Subproject subproject = (Subproject) optional.get();

            // Update roles
            JSONArray members = subproject.getMembers();
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
            subproject.setMembers(newMembers);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(dateFormat.format(new Date()));

            subprojectRepository.save(subproject);
            nodeService.addOrPutUserToNode(aid, userId, role);

            return ResultUtils.success(subproject);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult quitSubproject(String aid, String userId) {
        try {
            // check
            Optional optional = subprojectRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
            Subproject subproject = (Subproject) optional.get();

            // remove the user from the project
            JSONArray members = subproject.getMembers();
            if (members == null) members = new JSONArray();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    members.remove(member);
                    break;
                }
            }
            subproject.setMembers(members);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            subproject.setActiveTime(dateFormat.format(new Date()));

            subprojectRepository.save(subproject);
            //删除临时用户
            if (userId.length() > 40){
                userRepository.deleteById(userId);
            }
            //update node
            nodeService.userExitActivity(aid, userId);

            //退出activity
            projectUtil.quitSubProject(aid, userId, 1);

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }


//    @Override
//    public JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid){
//        try {
//            // Confirm aid
//            Optional optional = subprojectRepository.findById(update.getAid());
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
//            Subproject subproject = (Subproject) optional.get();
//            optional = activityRepository.findById(aid1);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity1 = (Activity) optional.get();
//            optional = activityRepository.findById(aid2);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity2 = (Activity) optional.get();
//
//            // Save the pathway
//            update.updateTo(subproject);
//
//            // Save the protocol------------------------------------------------待完善----------------------------------
//            // String pid = protocolRepository.save(protocol).getPid();
//
//            // Save activities
//            activityRepository.save(saveLastActivityInfo(aid2, pid, activity1));
//            activityRepository.save(saveNextActivityInfo(aid1, pid, activity2));
//            subprojectRepository.save(subproject);
//
//            return ResultUtils.success("Success");
//        } catch (Exception ex){
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }

//    @Override
//    public JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid){
//        try {
//            // Confirm aid
//            Optional optional = subprojectRepository.findById(update.getAid());
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
//            Subproject subproject = (Subproject) optional.get();
//            optional = activityRepository.findById(lastAid);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity1 = (Activity) optional.get();
//            optional = activityRepository.findById(nextAid);
//            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity2 = (Activity) optional.get();
//
//            // Save the pathway
//            update.updateTo(subproject);
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
//            subprojectRepository.save(subproject);
//            activityRepository.save(activity1);
//            activityRepository.save(activity2);
//
//            return ResultUtils.success("Success");
//        } catch (Exception ex){
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }
//
//    private Activity saveLastActivityInfo(String aid, String pid, Activity last) {
//        JSONObject nextInfo = new JSONObject();
//        nextInfo.put("aid", aid);
//        nextInfo.put("protocolId", pid);
//
//        JSONArray nextActivities = last.getNext();
//        if(nextActivities == null) nextActivities = new JSONArray();
//        nextActivities.add(nextInfo);
//        last.setNext(nextActivities);
//
//        return last;
//    }
//
//    private Activity saveNextActivityInfo(String aid, String pid, Activity next) {
//        JSONObject lastInfo = new JSONObject();
//        lastInfo.put("aid", aid);
//        lastInfo.put("protocolId", pid);
//
//        JSONArray lastActivities = next.getLast();
//        if(lastActivities == null) lastActivities = new JSONArray();
//        lastActivities.add(lastInfo);
//        next.setLast(lastActivities);
//
//        return next;
//    }
}

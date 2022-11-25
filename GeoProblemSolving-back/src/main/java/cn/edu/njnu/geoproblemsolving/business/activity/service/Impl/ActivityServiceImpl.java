package cn.edu.njnu.geoproblemsolving.business.activity.service.Impl;

import cn.edu.njnu.geoproblemsolving.Dao.Folder.FolderDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.ProjectUtil;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.service.ToolService;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ActivityService;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.user.repository.UserRepository;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    //    private final MongoTemplate mongoTemplate;
    private final FolderDaoImpl folderDao;
    private final SubprojectRepository subprojectRepository;
    private final ProjectRepository projectRepository;

    private final ProjectUtil projectUtil;
    private final ToolService toolService;
    private final NodeService nodeService;
    private final DocParseServiceImpl docParseService;

    @Autowired
    public ActivityServiceImpl(ActivityRepository activityRepository,
                               UserRepository userRepository,
                               SubprojectRepository subprojectRepository,
                               MongoTemplate mongoTemplate,
                               FolderDaoImpl folderDao,
                               ProjectRepository projectRepository,
                               ProjectUtil httpUtil,
                               ToolService toolService,
                               NodeService nodeService, DocParseServiceImpl docParseService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.folderDao = folderDao;
        this.subprojectRepository = subprojectRepository;
        this.projectRepository = projectRepository;
        this.projectUtil = httpUtil;
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

    public Activity findActivityById(String aid) {
        Optional optional = activityRepository.findById(aid);
        if (optional.isPresent()) {
            Object activity = optional.get();
            return (Activity) activity;
        } else {
            return null;
        }
    }

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

    private void updateActiveTime(Activity activity) {
        // Update active time
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        activity.setActiveTime(dateFormat.format(date));

        activityRepository.save(activity);
    }

    @Override
    public JsonResult findActivity(String aid) {
        try {
            Activity activity = findActivityById(aid);
            if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");

            // Update active time
            updateActiveTime(activity);

            return ResultUtils.success(activity);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult createActivity(Activity activity) {
        try {
            // aid
            String aid = UUID.randomUUID().toString();
            activity.setAid(aid);

            // created time
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setCreatedTime(dateFormat.format(date));
            activity.setActiveTime(dateFormat.format(date));

            // members
            String creatorId = activity.getCreator();
            JSONObject creator = new JSONObject();
            JSONArray members = new JSONArray();

            creator.put("userId", creatorId);
            creator.put("role", "manager");
            members.add(creator);

            activity.setMembers(members);

            // set type
            activity.setType(activity.getType());
            // docParser.initActivityDoc(activity);
            docParseService.initActivityDoc(activity);
            if (activity.getType().equals(ActivityType.Activity_Group)) {
                activity.setChildren(new ArrayList<>());
            } else if (activity.getType().equals(ActivityType.Activity_Unit)) {
                String purpose = activity.getPurpose();
                List<Tool> relevantPurposeTool = toolService.getRelevantPurposeTool(purpose);
                HashSet<String> toolSet = new HashSet<>();
                if (relevantPurposeTool != null && !relevantPurposeTool.isEmpty()) {
                    for (Tool tool : relevantPurposeTool) {
                        toolSet.add(tool.getTid());
                    }
                    activity.setToolList(toolSet);
                }

                //更新文档
                // docParser.addTools(aid, relevantPurposeTool);
                docParseService.refreshTool(aid, relevantPurposeTool);

            }

            // tools and toolsets
//            Query queryPublic = new Query(Criteria.where("privacy").is("Public"));
//            List<ToolEntity> toolEntities = mongoTemplate.find(queryPublic, ToolEntity.class);
//            ArrayList<String> tools = new ArrayList<>();
//            for (ToolEntity toolEntity : toolEntities) {
//                tools.add(toolEntity.getTid());
//            }
//            activity.setToolList(tools);
//
//            List<ToolsetEntity> toolsetEntities = mongoTemplate.find(queryPublic, ToolsetEntity.class);
//            ArrayList<String> toolsets = new ArrayList<>();
//            for (ToolsetEntity toolsetEntity : toolsetEntities) {
//                toolsets.add(toolsetEntity.getTsid());
//            }
//            activity.setToolsetList(toolsets);

            //folder
            folderDao.createFolder(activity.getName(), "", aid);

            // update parent activity info
            if (activity.getLevel() == 2) {
                String subprojectId = activity.getParent();
                Optional optional = subprojectRepository.findById(subprojectId);
                if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: parent activity does not exist.");
                Subproject parent = (Subproject) optional.get();

                ArrayList<String> children;
                if (parent.getChildren() != null) {
                    children = parent.getChildren();
                } else {
                    children = new ArrayList<>();
                }
                children.add(aid);
                parent.setChildren(children);

                subprojectRepository.save(parent);
            } else {
                String activityId = activity.getParent();
                Optional optional = activityRepository.findById(activityId);
                if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: parent activity does not exist.");
                Activity parent = (Activity) optional.get();

                ArrayList<String> children;
                if (parent.getChildren() != null) {
                    children = parent.getChildren();
                } else {
                    children = new ArrayList<>();
                }
                children.add(aid);
                parent.setChildren(children);

                activityRepository.save(parent);
            }

            //save
            activityRepository.save(activity);

            docParseService.appendChildActivity(activity.getParent(), aid, activity.getName(), creatorId);
            // docParser.appendChildActivity(activity.getParent(), aid, activity.getName(), creatorId);

            return ResultUtils.success(activity);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult updateActivity(String aid, UpdateActivityDTO update) {
        try {
            // confirm
            Optional result = activityRepository.findById(aid);
            if (!result.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) result.get();

            String purpose = update.getPurpose();
            List<Tool> relevantPurposeTool = new ArrayList<>();
            if (
                    update.getType() != null && update.getPurpose() != null &&
                            (update.getType().equals(ActivityType.Activity_Unit) &&
                                    !activity.getType().equals(ActivityType.Activity_Unit) ||
                                    !activity.getPurpose().equals(purpose))) {
                relevantPurposeTool = toolService.getRelevantPurposeTool(purpose);
                HashSet<String> toolSet = new HashSet<>();
                if (!relevantPurposeTool.isEmpty()) {
                    for (Tool tool : relevantPurposeTool) {
                        toolSet.add(tool.getTid());
                    }
                }
                update.setToolList(toolSet);
            }

            ActivityType oldType = activity.getType();
            update.updateTo(activity);

            //activityType 发生改变
            if (update.getType() != null && !oldType.equals(update.getType())) {
                docParseService.changeActivityType(aid, activity);
                // docParser.changeActivityType(aid, activity);
            }

            //更新工具箱内容
            if (!relevantPurposeTool.isEmpty()) {
                docParseService.refreshTool(aid, relevantPurposeTool);
            }
            //名字发生改变，更新父文档
            if (update.getName() != null && !activity.getName().equals(update.getName())) {
                docParseService.updateChild(activity.getParent(), activity.getAid(), update.getName());
            }

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setActiveTime(dateFormat.format(new Date()));

            return ResultUtils.success(activityRepository.save(activity));
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult deleteActivity(String aid) {
        try {
            // confirm
            Optional optional = activityRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) optional.get();

            // delete from parent
            if (activity.getLevel() == 2) {
                optional = subprojectRepository.findById(activity.getParent());
                if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: parent activity does not exist.");
                Subproject parent = (Subproject) optional.get();

                if (parent.getChildren().contains(aid))
                    parent.getChildren().remove(aid);
                subprojectRepository.save(parent);
            } else {
                optional = activityRepository.findById(activity.getParent());
                if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: parent activity does not exist.");
                Activity parent = (Activity) optional.get();

                if (parent.getChildren().contains(aid))
                    parent.getChildren().remove(aid);
                activityRepository.save(parent);
            }

            // delete children
            if (activity.getChildren() != null) {
                deleteChildren(activity);
            }

            activityRepository.deleteById(aid);

            //文档删除
            docParseService.deleteDoc(aid, activity.getParent());

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
            // confirm
            Optional optional = activityRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) optional.get();

            ArrayList<Activity> activities = getChildActivities(activity);

            // Update active time
            updateActiveTime(activity);

            return ResultUtils.success(activities);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    private ArrayList getChildActivities(Activity activity) {
        if (activity.getLevel() >= 0) {
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
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public JsonResult findParticipants(String aid) {
        try {
            Optional optional = activityRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) optional.get();

            // creator
            JSONObject participants = new JSONObject();
            UserEntity creator = findByUserId(activity.getCreator());
            participants.put("creator", creator);

            // members
            JSONArray members = activity.getMembers();
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
            updateActiveTime(activity);

            return ResultUtils.success(participants);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult findLineage(String aid) {
        try {

            Optional optional = activityRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) optional.get();

            // children
            ArrayList<Activity> children = getChildActivities(activity);

            // ancestors
            ArrayList<Activity> ancestors = new ArrayList<>();
            ancestors.add(activity);

            int level = activity.getLevel();
            for (int i = level - 1; i >= 0; i--) {
                aid = activity.getParent();
                if (i == 0) {
                    optional = projectRepository.findById(aid);
                    if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: project does not exist.");
                } else if (i == 1) {
                    optional = subprojectRepository.findById(aid);
                    if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: subproject does not exist.");
                } else {
                    optional = activityRepository.findById(aid);
                    if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
                }
                activity = (Activity) optional.get();
                ancestors.add(activity);
            }

            // brothers
            ArrayList<Activity> brothers = getChildActivities(ancestors.get(1));

            JSONObject lineage = new JSONObject();
            lineage.put("ancestors", ancestors);
            lineage.put("brothers", brothers);
            lineage.put("children", children);

            // Update active time
            updateActiveTime(activity);

            return ResultUtils.success(lineage);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

//    @Override
//    public JsonResult findLast(String aid) {
//        try {
//            List<Activity> activities = new ArrayList();
//            Activity current = findActivityById(aid);
//            if (current == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//
//            JSONArray lastActivities = current.getLast();
//            for (Object last : lastActivities) {
//                if (last instanceof JSONObject) {
//                    Activity lastActivity = findActivityById(((JSONObject) last).getString("aid"));
//                    if (lastActivity == null) continue;
//                    activities.add(lastActivity);
//                }
//            }
//
//            // Update active time
//            updateActiveTime(current);
//
//            return ResultUtils.success(activities);
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }
//
//    @Override
//    public JsonResult findNext(String aid) {
//        try {
//            List<Activity> activities = new ArrayList();
//            Activity current = findActivityById(aid);
//            if (current == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//
//            JSONArray nextActivities = current.getNext();
//            for (Object next : nextActivities) {
//                if (next instanceof JSONObject) {
//
//                    Activity nextActivity = findActivityById(((JSONObject) next).getString("aid"));
//                    if (nextActivity == null) continue;
//                    activities.add(nextActivity);
//                }
//            }
//
//            // Update active time
//            updateActiveTime(current);
//
//            return ResultUtils.success(activities);
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//
//    }


//    @Override
//    public JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid) {
//        try {
//            // Confirm aid
//            Activity activity1 = findActivityById(aid1);
//            if (activity1 == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity2 = findActivityById(aid2);
//            if (activity2 == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity = findActivityById(update.getAid());
//            if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//
//            // Save the pathway
//            update.updateTo(activity);
//
//            // Save the protocol------------------------------------------------待完善----------------------------------
//            // String pid = protocolRepository.save(protocol).getPid();
//
//            // Save activities
//            activityRepository.save(saveLastActivityInfo(aid2, pid, activity1));
//            activityRepository.save(saveNextActivityInfo(aid1, pid, activity2));
//            activityRepository.save(activity);
//
//            return ResultUtils.success("Success");
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }

//    @Override
//    public JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid) {
//        try {
//            // Confirm aid
//            Activity activity1 = findActivityById(lastAid);
//            if (activity1 == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity2 = findActivityById(nextAid);
//            if (activity2 == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//            Activity activity = findActivityById(update.getAid());
//            if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
//
//            // Save the pathway
//            update.updateTo(activity);
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
//            activityRepository.save(activity);
//            activityRepository.save(activity1);
//            activityRepository.save(activity2);
//
//            return ResultUtils.success("Success");
//        } catch (Exception ex) {
//            return ResultUtils.error(-2, ex.toString());
//        }
//    }

    @Override
    public JsonResult joinActivity(String aid, String userId) {
        try {
            // confirm
            Activity activity = findActivityById(aid);
            if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
            //从用户服务器读取该用户，还需将他注册到这参与式平台
            UserEntity user = findByUserId(userId);
            if (user == null) return ResultUtils.error(-1, "Fail: user does not exist.");

            // add user info to subproject
            // if user exist in subproject?
            JSONArray members = activity.getMembers();
            if (members == null) members = new JSONArray();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    return ResultUtils.error(-3, "Fail: member already exists in the subproject");
                }
            }

            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", user.getUserId());
            userInfo.put("role", "ordinary-member");
            members.add(userInfo);
            // activity.setMembers(members);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setActiveTime(dateFormat.format(new Date()));

            activityRepository.save(activity);


            //update doc
            docParseService.userJoin(aid, userId);
            //update node
            nodeService.addOrPutUserToNode(aid, userId, "ordinary-member");

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    @Override
    public JsonResult joinActivity(String aid, HashSet<String> userIds) {
        Activity activity = findActivityById(aid);
        if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");
        JSONArray members = activity.getMembers();
        if (members == null) members = new JSONArray();
        try {
            //Do not process if the user exists.
            for (Object member : members) {
                String userId = (String) ((HashMap) member).get("userId");
                for (Iterator<String> it = userIds.iterator(); it.hasNext(); ) {
                    String uid = it.next();
                    if (uid.equals(userId)) {
                        it.remove();
                    }
                }
            }
            for (String uid : userIds) {
                Optional<UserEntity> byId = userRepository.findById(uid);
                if (!byId.isPresent()) continue;
                JSONObject newMember = new JSONObject();
                newMember.put("userId", uid);
                newMember.put("role", "ordinary-member");
                members.add(newMember);
            }
            //update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setActiveTime(dateFormat.format(new Date()));
            activityRepository.save(activity);

            //update doc
            docParseService.userJoin(aid, userIds);
            //update node
            nodeService.addUserToNodeBatch(aid, userIds);
            return ResultUtils.success(members);
        } catch (Exception e) {
            return ResultUtils.error(-2, e.toString());
        }
    }

    @Override
    public JsonResult quitActivity(String aid, String userId) {
        try {
            Activity activity = findActivityById(aid);
            if (activity == null) return ResultUtils.error(-1, "Fail: activity does not exist.");

            JSONArray members = activity.getMembers();
            for (Object member : members) {
                if (((HashMap) member).get("userId").equals(userId)) {
                    members.remove(member);
                    break;
                }
            }
            // activity.setMembers(members);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setActiveTime(dateFormat.format(new Date()));

            activityRepository.save(activity);

            //删除临时用户
            if (userId.length() > 40) {
                userRepository.deleteById(userId);
            }

            //update node
            nodeService.userExitActivity(aid, userId);

            //完成当前项目的退出，需要将子项目推出
            projectUtil.quitSubProject(aid, userId, 2);

            return ResultUtils.success("Success");
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }


    @Override
    public JsonResult updateMemberRole(String aid, String userId, String role) {
        try {
            // check
            Optional optional = activityRepository.findById(aid);
            if (!optional.isPresent()) return ResultUtils.error(-1, "Fail: activity does not exist.");
            Activity activity = (Activity) optional.get();

            // Update roles
            JSONArray members = activity.getMembers();
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
            activity.setMembers(newMembers);

            // Update active time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activity.setActiveTime(dateFormat.format(new Date()));

            activityRepository.save(activity);
            nodeService.addOrPutUserToNode(aid, userId, role);

            return ResultUtils.success(activity);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }
}

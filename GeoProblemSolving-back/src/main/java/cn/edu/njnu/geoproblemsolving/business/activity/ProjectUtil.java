package cn.edu.njnu.geoproblemsolving.business.activity;

import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * @ClassName HttpUtil
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/5/13
 **/
@Service
public class ProjectUtil {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    SubprojectRepository subprojectRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Value("${userServerLocation}")
    String userServer;

    @Autowired
    NodeService nodeService;

    @Autowired
    DocParseServiceImpl docParseService;

    @Value("${client_id}")
    String clientId;

    @Value("${client_secret}")
    String clientSecret;

    public UserEntity getUserInfoFromUserServer(String userId) {
        String userUrl = "http://" + userServer + "/user/" + userId + "/" + clientId + "/" + clientSecret;
        try {
            JSONObject forObject = restTemplate.getForObject(userUrl, JSONObject.class);
            if (forObject.getInteger("code") != 0) {
                //包含两种情况，-1无此用户，-2出错(几乎不会存在)
                return null;
            }
            return forObject.getObject("data", UserEntity.class);
        } catch (Exception e) {
            throw e;
        }
    }

    //退出子项目
    public void userQuitSubActivity(JsonResult subProject, String userId) {
        ArrayList arrayList = JSONObject.parseObject(JSONObject.toJSONString(subProject.getData()), ArrayList.class);
        arrayList.forEach(item -> {
            Activity activity = JSONObject.parseObject(JSONObject.toJSONString(item), Activity.class);
            JSONArray members = activity.getMembers();
            members.forEach(member -> {
                JSONObject memberJson = JSONObject.parseObject(JSONObject.toJSONString(member));
                String memberId = memberJson.getString("userId");
                if (userId.equals(memberId)) {
                    members.remove(member);
                }
            });
        });
    }

    /**
     * 获取当前用户所在的所有下层活动
     *
     * @param aid
     * @param userId
     * @param level  当前的 level
     * @return
     */
    public void quitSubProject(String aid, String userId, Integer level) {
        ArrayList<String> childIds;
        //quit current level
        docParseService.userOut(aid,userId);
        if (level == 0) {
            Project project = projectRepository.findById(aid).get();
            childIds = project.getChildren();
            level++;
        } else if (level == 1) {
            Subproject subproject = subprojectRepository.findById(aid).get();
            childIds = subproject.getChildren();
            level++;
        } else {
            Activity activity = activityRepository.findById(aid).get();
            childIds = activity.getChildren();
            level++;
        }

        if (childIds == null || childIds.size() <= 0) {
            return;
        }
        gSubProject(childIds,userId, level);
    }

    private void gSubProject(ArrayList<String> childIds, String userId, Integer level) {
        if (level == 1) {
            for (int i = 0; i < childIds.size(); i++) {
                String aid = childIds.get(i);
                Subproject subproject = subprojectRepository.findById(aid).get();
                JSONArray members = subproject.getMembers();
                members.forEach(item -> {
                    JSONObject member = JSONObject.parseObject(JSONObject.toJSONString(item));
                    String memberId = member.getString("userId");
                    if (userId.equals(memberId)) {
                        members.remove(item);
                        subprojectRepository.save(subproject);
                        //update document
                        docParseService.userOut(aid, userId);
                        //update node
                        nodeService.userExitActivity(aid, userId);
                    }
                });

                ArrayList<String> children = subproject.getChildren();
                if (children != null && children.size() > 0) {
                    level++;
                    gSubProject(children, userId, level);
                }
            }
        } else {
            for (int i = 0; i < childIds.size(); i++) {
                String aid = childIds.get(i);
                Activity activity = activityRepository.findById(aid).get();
                JSONArray members = activity.getMembers();
                members.forEach(item -> {
                    JSONObject member = JSONObject.parseObject(JSONObject.toJSONString(item));
                    String memberId = member.getString("userId");
                    if (userId.equals(memberId)) {
                        members.remove(item);
                        activityRepository.save(activity);
                        docParseService.userOut(aid, userId);
                        //update node
                        nodeService.userExitActivity(aid, userId);
                    }
                });
                ArrayList<String> children = activity.getChildren();
                if (children != null && children.size() > 0) {
                    level++;
                    gSubProject(children, userId, level);
                }
            }
        }
    }

}

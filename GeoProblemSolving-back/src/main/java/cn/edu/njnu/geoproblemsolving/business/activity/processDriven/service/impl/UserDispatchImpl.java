package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.impl;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.UserDispatch;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.user.dao.Impl.UserDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import cn.hutool.http.HttpException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * @ClassName UserDispatch
 * @Description 活动间用户调度中心
 * @Author zhngzhng
 * @Date 2021/7/21
 **/
@Service
public class UserDispatchImpl implements UserDispatch {
    private final UserDaoImpl userDao;

    private final GeoAnalysisProcessImpl geoAnalysisProcess;

    private final RestTemplate restTemplate;

    private final ActivityRepository activityDao;

    @Autowired
    SubprojectRepository subprojectDao;

    @Value("${userServerLocation}")
    String userServerLocation;

    public UserDispatchImpl(RestTemplate restTemplate, UserDaoImpl userDao, GeoAnalysisProcessImpl geoAnalysisProcess, ActivityRepository activityDao) {
        this.restTemplate = restTemplate;
        this.userDao = userDao;
        this.geoAnalysisProcess = geoAnalysisProcess;
        this.activityDao = activityDao;
    }

    @Override
    public Object updateActivityMember(String aid, JSONArray members) {
        for (int i = 0; i < members.size(); i++){
            JSONObject member = JSONObject.parseObject(JSONObject.toJSONString(members.get(i)));
            String userId = member.getString("userId");
            member.getString("role");
        }
        return null;
    }

    /**
     * 获取节点中用于的标签
     * @param activityIds
     * @return
     */
    @Override
    public JsonResult getNodeUserTag(Integer level, HashSet<String> activityIds) {
        if (activityIds.isEmpty()){
            return null;
        }
        Iterator<String> iterator = activityIds.iterator();
        HashSet<String> memberIds = new HashSet<>();
        while (iterator.hasNext()){
            //需要去读取 member 所以不应该叫 nodeId，应该叫 activityId
            String activityId = iterator.next();
            //肯定会有重复的成员，最好是先将成员 id 取出来，然后再去贴换
            Activity activity;
            if (level == 0){
                activity = subprojectDao.findById(activityId).get();
            }else{
                activity = activityDao.findById(activityId).get();
            }
            JSONArray members = activity.getMembers();
            members.forEach(member->{
                JSONObject memberJson = JSONObject.parseObject(JSONObject.toJSONString(member));
                String userId = memberJson.getString("userId");
                memberIds.add(userId);
            });
        }

        String memberIdStr = memberIds.stream().collect(Collectors.joining(","));
        //一次性拿到所有用户的标签
        String getUsersTagUrl = "http://" + userServerLocation + "/user/tag/batch/" + memberIdStr;
        try {
            JSONObject getResult = restTemplate.getForObject(getUsersTagUrl, JSONObject.class);
            if (getResult.getInteger("code") != 0)  return ResultUtils.error(-2, "Fail: Acquiring user's tag from userServer.");
            return ResultUtils.success(getResult.getJSONObject("data"));
        }catch (HttpException exception){
            return ResultUtils.error(-2, "Fail: Acquiring user's tag from userServer.");
        }
    }

    @Override
    public JSONObject getUserTag(String userId) {
        String getUserTagUrl = "http://" + userServerLocation + "/user/tag/" + userId;
        try {
            JSONObject getResult = restTemplate.getForObject(getUserTagUrl, JSONObject.class);
            if (getResult.getInteger("code") != 0){
                System.out.println("Fail: Acquiring user's tag from userServer.");
                //增加临时用户的处理
                UserEntity userById = userDao.findUserById(userId);
                if (userById != null) return new JSONObject();
                return null;
            }
            return getResult.getJSONObject("data");
        }catch (HttpException e){
            System.out.println("getUserTag" + e.toString());
            return null;
        }
    }

    @Override
    public JSONObject getUsersTag(HashSet<String> userIds) {
        String userIdStr = userIds.stream().collect(Collectors.joining(","));
        String getUsersTagUrl = "http://" + userServerLocation + "/user/tags/" + userIdStr;
        try {
            JSONObject getResult = restTemplate.getForObject(getUsersTagUrl, JSONObject.class);
            if (getResult.getInteger("code") != 0)  return null;
            return getResult.getJSONObject("data");
        }catch (HttpException exception){
            return null;
        }
    }

    @Override
    public boolean checkUserIsApproved(String graphId, String nodeId, String userId) {
        //更新节点中的用户信息
        String flowId = geoAnalysisProcess.checkUserIsApprovedService(graphId, nodeId, userId);
        return flowId != null;
    }
}

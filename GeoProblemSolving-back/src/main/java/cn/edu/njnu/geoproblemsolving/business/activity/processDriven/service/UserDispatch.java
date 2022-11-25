package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;

public interface UserDispatch {
    // ArrayList<UserEntity> getRelationUser(ArrayList<String> aids);
    //
    // ArrayList<String> getUserTag(ArrayList<UserEntity> users, String type);

    Object updateActivityMember(String aid, JSONArray members);

    Object getNodeUserTag(Integer level, HashSet<String> nodeIds);

    JSONObject getUserTag(String userId);

    JSONObject getUsersTag(HashSet<String> userIds);

    boolean checkUserIsApproved(String graphId, String nodeId, String userId);

}

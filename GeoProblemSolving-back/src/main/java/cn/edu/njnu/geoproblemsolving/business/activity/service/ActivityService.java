package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;

import java.io.IOException;
import java.util.HashSet;

public interface ActivityService {

    JsonResult findActivity(String aid);

    JsonResult createActivity(Activity activity);

    JsonResult updateActivity(String aid, UpdateActivityDTO update);

    JsonResult deleteActivity(String aid);

    JsonResult findChildren(String aid);

    JsonResult findParticipants(String aid);

    JsonResult updateMemberRole(String aid, String userId, String role);

    JsonResult joinActivity(String aid, String userId);

    JsonResult joinActivity(String aid, HashSet<String> userId);

    JsonResult quitActivity(String aid, String userId);

    JsonResult findLineage(String aid);

//    JsonResult findLast(String aid);
//
//    JsonResult findNext(String aid);
//
//    JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid);
//
//    JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid);
}

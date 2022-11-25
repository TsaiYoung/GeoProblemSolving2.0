package cn.edu.njnu.geoproblemsolving.business.activity.service;


import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;

import java.io.IOException;
import java.util.HashSet;


public interface SubprojectService{
    JsonResult createSubproject(Subproject subproject);

    JsonResult inquirySubproject(String aid);

    JsonResult updateSubproject(String aid, UpdateActivityDTO update);

    JsonResult deleteSubproject(String aid);

    JsonResult findChildren(String aid);

    JsonResult findParticipants(String aid);

    JsonResult findLineage(String aid);

    JsonResult findSubProject(String aid);

    JsonResult joinSubproject(String aid, String userId);

    JsonResult joinSubproject(String aid, HashSet<String> userId);

    JsonResult updateMemberRole(String aid, String userId, String role);

    JsonResult quitSubproject(String aid, String userId);

//    JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid);
//
//    JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid);
}

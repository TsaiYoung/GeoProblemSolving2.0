package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateActivityDTO;
import cn.edu.njnu.geoproblemsolving.business.activity.dto.UpdateProjectDTO;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.Entity.EmailEntity;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public interface ProjectService {
    JsonResult createProject(Project project);

    JsonResult findProject(String aid);

    JSONObject findProjectsByPage(int page, int size);

    JsonResult deleteProject(String aid);

    JsonResult updateProject(String aid, UpdateProjectDTO project);

    JsonResult findParticipants(String aid);

    JsonResult findChildren(String aid);

    JsonResult joinProject(String aid, String userId);

    JsonResult joinProject(String aid, HashSet<String> userId) throws IOException;

    JsonResult updateMemberRole(String aid, String userId, String role);

    JsonResult quitProject(String aid, String userId);

    JsonResult invitedParticipants(String aid, String email, String password);

    JsonResult applyJoinProject(String aid, EmailEntity emailEntity);

    JsonResult inquiryByConditions(String category, String tag, String keyword, String userId, int page, int size);

//    JsonResult linkActivities(UpdateActivityDTO update, String aid1, String aid2, String pid);
//
//    JsonResult separateActivities(UpdateActivityDTO update, String lastAid, String nextAid);
}

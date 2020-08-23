package cn.edu.njnu.geoproblemsolving.business.activity.service;


import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;

public interface SubprojectService{
    public Object createSubproject(Subproject subproject);

    public Object inquirySubprojects(String projectId);

    public Object findParticipants(String aid);

    public String joinSubproject(String aid, String userId);

    public String updateMemberRole(String aid, String userId, String role);

    public String quitSubproject(String aid, String userId);
}

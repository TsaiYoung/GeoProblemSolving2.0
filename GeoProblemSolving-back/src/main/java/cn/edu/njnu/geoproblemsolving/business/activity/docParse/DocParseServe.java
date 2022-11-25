package cn.edu.njnu.geoproblemsolving.business.activity.docParse;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow.Operation;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/*

 */
@Service
public interface DocParseServe {
    void deleteDoc(String aid);

    void deleteDoc(String aid, String parentId);

    void initActivityDoc(Activity activity);

    Object changeActivityType(String aid, Activity activity);

    void userJoin(String aid, String userId);

    void userOut(String aid, String userId);

    Object userJoin(String aid, HashSet<String> userIds);


    Object uploadResource(String aid, HashMap<String, String> resInfo);

    ArrayList<HashMap<String, String>> uploadResource(String aid, ArrayList<ResourceEntity> resourceEntities);

    ArrayList<HashMap<String, String>> uploadResource(String aid, ArrayList<ResourceEntity> res, HashMap<String, String> meta);

    void shareResource(String fromAid, String toAid, HashMap<String, String> idMap);

    ArrayList<HashMap<String, String>> shareResource(String aid, ArrayList<ResourceEntity> resList);

    void removeResource(String aid, String uid);

    void removeResource(String aid, HashSet<String> uids);

    void resFlow(String fromAid, String endAid, HashSet<String> uids);

    HashMap<String, String> resFlow(String fromAid, String endAid, String uid);

    // void resFlow(String fromAid, String endAid, HashMap<String, String> idMap);

    // HashMap<String, String> resFlow(String fromAid, String endAid, String oldUid, String newUid);



    ArrayList<HashMap<String, String>> getResInfo(String aid, HashSet<String> uids);

    HashMap<String, String> getResInfo(String aid, String uid);

    ArrayList<HashMap<String, String>> getResInfo(String aid);

    void appendChildActivity(String aid, String childId, String name, String creator);

    void initSubActivityDoc(Activity activity);

    Object addTool(String aid, List<Tool> tools);

    void refreshTool(String aid, List<Tool> tools);

    void updateChild(String aid, String childId, String name);

    void updateRoot(String aid, HashMap<String, String> updateInfo);

    Object removeTool(String aid, String tid);

    Object removeTool(String aid, HashSet<String> tids);

    String messageRecord(String tid, MsgRecords msgRecords);

    // zhaoym
    HashSet<Operation> getGeoAnalysisInTool(String aid, String tid);

    //可供外部调用
    String geoAnalysis(String aid, String toolId,
                       HashSet<String> inResId,
                       ArrayList<ResourceEntity> outRes,
                       HashSet<String> participants);

    String geoAnalysis(String aid, String toolId, HashSet<String> inResId, ResourceEntity outRes, HashSet<String> participants);


    String geoAnalysisRLS90(String aid, String toolId,
                            HashSet<String> onlineMembers,
                            String purpose,
                            ResourceEntity input,
                            ResourceEntity output);


    //Mapping
    Object toWorkflowTemplate(String aid);

}

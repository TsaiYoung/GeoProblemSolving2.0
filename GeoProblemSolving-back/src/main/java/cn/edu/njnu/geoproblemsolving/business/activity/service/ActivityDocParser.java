package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface ActivityDocParser {
    //

    /*
        Document level operation
        default: public
     */
    void initActivityDoc(Activity activity);

    void initActivityDoc(String aid, String level);

    void changeActivityType(String aid, Activity activity);


    //=================Generic operation==============================================================
    /*
    Generic operation
    Common item between multi and signal activity:
    Participants
    ResourceCollection
    OperationRecords(Multi: process, activity, communication; Signal: resource, tool, communication, geo-analysis)
     */

    /*
        1. Participants behaviors
     */
    //domain & organization,do not read from the node
    //the more the number of reads and writes, the easier it is to cause out of synchronization.
    Object inActivity(String aid, HashMap<String, String> userInfo);

    //Generate domain and organization form user server.
    Object outActivity(String aid, String userId);

    Object rolePut(String aid, String userId, String role);

    Object userDomainPut(String aid, String userId, HashSet<String> domains);

    Object userOrganizationPut(String aid, String userId, HashSet<String> organizations);

    /*
        2.Resources related operations
     */
    Object uploadResource(String aid, HashMap<String, String> resInfo);

    ArrayList<HashMap<String, String>> uploadResources(String aid, ArrayList<ResourceEntity> uploadList, HashMap<String, String> meta);

    Object resFlow(String aid, HashMap<String, String> resInfo);

    Object uploadResource(String aid, ResourceEntity res);

    Object putRes(String aid, HashMap<String, String> putInfo);

    Object removeRes(String aid, String rid);

    ArrayList<HashMap<String, String>> getResInfo(String aid, HashSet<String> uids);

    HashMap<String, String> getResInfo(String aid, String uid);

    ArrayList<HashMap<String, String>> getAllResInfo(String aid);

    //========================Multi activity operation===================================================
    /*
        1.Operation records
     */



    /*
        2. ChildActivities and activity dependencies
     */

    Object appendChildActivity(String aid, String childId, String name, String creatorId);

    //=======================Signal activity operation===================================================
    /*
        1.Tool related operation, include toolbox
     */
    Object addTools(String aid, List<Tool> tools);

    Object putTools(String aid, List<Tool> tools);
    /*
        2.Task list
     */
    Object bindOperation(String aid, String oid);

    Object unbindOperation(String aid, String oid);

    /*
        3. Task dependency
     */
    Object linkTask(String aid, String fromId, String toId);

    /*
        3.Operation records, remain geo-analysis.
     */
    Object geoAnalysis(String aid, ArrayList<ResourceEntity> inRes, ArrayList<String> inParams, ArrayList<ResourceEntity> outRes);

    Object setGeoAnalysisOutPuts(String aid, String oid, ArrayList<String> output);

    //for RLS90
    String geoAnalysisNoInput(String aid, String toolId,HashSet<String> onlineMemberIds, String purpose, ResourceEntity output);

    String geoAnalysis(String aid, String toolId,
                       HashSet<String> inResId,
                       ArrayList<ResourceEntity> outRes,
                       HashSet<String> participants);

    //for RL90
    //Signal input and output
    String geoAnalysis(String aid, String toolId,
                       HashSet<String> onlineMemberIds, String purpose,
                       ResourceEntity input, ResourceEntity output);

    String storeMessageRecord(String toolId, MsgRecords msgRecords);





        Object setGeoAnalysisOutPut(String aid, String oid, String uid);
    //service
    HashMap<String, String> resFlow(String formId, String endId, String uid);

    void resFlow(String fromId, String endId, HashSet<String> uids);

    Object resFlow(String aid, ArrayList<ResourceEntity> resList);

    Object userJoin(String aid, String userId);

    Object userJoin(String aid, HashSet<String> userIds);

    Object userJoin(String fromId, String endId, HashSet<String> userIds);

    Object extractWorkFlowTemplate(String aid);
}

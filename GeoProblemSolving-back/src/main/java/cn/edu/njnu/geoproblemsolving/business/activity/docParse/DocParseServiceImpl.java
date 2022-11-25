package cn.edu.njnu.geoproblemsolving.business.activity.docParse;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow.Operation;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow.OperationRes;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow.ProjectInfo;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow.WorkflowTemplate;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.DocPerson;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.UserDispatch;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityDocRepository;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 服务层
 *
 * @ClassName DocParseService
 * @Description 服务层与其他部分进行耦合
 * @Author zhngzhng
 * @Date 2021/11/22
 **/
@Service
@Slf4j
public class DocParseServiceImpl implements DocParseServe {
    // private final Logger LOGGER = LoggerFactory.getLogger(DocParseServiceImpl.class);

    @Autowired
    ActivityDocRepository docRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    SubprojectRepository subprojectRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    UserDao userDao;

    @Autowired
    UserDispatch userDispatch;

    //document xml string.
    private ActivityDoc operatingDoc;

    private Document loadXML(String aid) {
        Optional<ActivityDoc> byId = docRepository.findById(aid);
        if (!byId.isPresent()) {
            log.warn("Failed to load document: No such activity document, id ---> " + aid);
            return null;
        }
        operatingDoc = byId.get();
        try {
            return DocumentHelper.parseText(operatingDoc.getDocument());
        } catch (DocumentException e) {
            log.error("Failed to parse document string.");
            e.printStackTrace();
            return null;
        }
    }

    private void saveXml(String docString) {
        if (operatingDoc == null) return;
        operatingDoc.setDocument(docString);
        docRepository.save(operatingDoc);
        operatingDoc = null;
    }

    /*
    member -> participants
    members is no null.
     */
    private HashSet<DocPerson> setDocPerson(JSONArray members) {
        try {
            HashSet<DocPerson> docPersons = new HashSet<>();
            for (Iterator<Object> it = members.iterator(); it.hasNext(); ) {
                DocPerson member = new DocPerson();
                JSONObject memberJson = JSONObject.parseObject(JSONObject.toJSONString(it.next()));
                String userId = memberJson.getString("userId");
                member.setUserId(userId);
                member.setRole(memberJson.getString("role"));
                //Get user information from local database.
                UserEntity user = userDao.findUserByIdOrEmail(userId);
                if (user == null) {
                    log.warn("Failed to covert participant: No such user, id " + userId);
                    return null;
                }
                member.setEmail(user.getEmail());
                member.setName(user.getName());
                ArrayList<String> domain = user.getDomain();
                HashSet<String> domainSet = new HashSet<>();
                ArrayList<String> organizations = user.getOrganizations();
                HashSet<String> orgSet = new HashSet<>();
                if (domain != null) domainSet = (HashSet<String>) domain.stream().collect(Collectors.toSet());
                if (organizations != null)
                    orgSet = (HashSet<String>) organizations.stream().collect(Collectors.toSet());
                member.setDomain(domainSet);
                member.setOrganization(orgSet);
                docPersons.add(member);
            }
            return docPersons;
        } catch (NullPointerException e) {
            log.error("Failed to set document person: members is null.");
            throw e;
        }
    }


    @Override
    public void initActivityDoc(Activity activity) {
        HashSet<DocPerson> docPeoples = setDocPerson(activity.getMembers());
        if (docPeoples == null) return;
        Iterator<DocPerson> it = docPeoples.iterator();
        //仅有一个 Creator
        String xmlStr = DocInterpret.initActivityDoc(activity, it.next());
        ActivityDoc activityDoc = new ActivityDoc();
        activityDoc.setAid(activity.getAid());
        activityDoc.setDocument(xmlStr);


        docRepository.insert(activityDoc);
    }

    /**
     * 删除当前文档及下层文档
     * @param aid
     */
    @Override
    public void deleteDoc(String aid) {
        Document docXml = loadXML(aid);
        dDoc(docXml);
    }

    @Override
    public void deleteDoc(String aid, String parentId) {
        //完成当前文档及下级文档删除
        deleteDoc(aid);
        Document docXml = loadXML(parentId);
        DocInterpret.removeChild(docXml, aid);
        saveXml(docXml.asXML());
    }

    private void dDoc(Document docXml) {
        String type = docXml.getRootElement().attribute("type").getValue();
        String aid = docXml.getRootElement().attribute("id").getValue();
        if (type.equals(ActivityType.Activity_Group.toString())){
            HashSet<HashMap<String, String>> childActivities = DocInterpret.getChildActivities(docXml);
            for (Iterator<HashMap<String, String>> it = childActivities.iterator(); it.hasNext(); ) {
                HashMap<String, String> activityInfo = it.next();
                try {
                    String subId = activityInfo.get("id");
                    Document subDocXml = loadXML(subId);
                    dDoc(subDocXml);
                }catch (Exception e){
                    continue;
                }
            }
        }
        docRepository.deleteById(aid);

    }

    /**
     * 更改活动类型
     *
     * @param aid
     * @param activity
     * @return
     */
    @Override
    public Integer changeActivityType(String aid, Activity activity) {
        Document docXml = loadXML(aid);
        if (docXml == null) return null;
        Element activityEle = docXml.getRootElement();
        activityEle.clearContent();
        String type = activity.getType().toString();
        activityEle.attribute("type").setValue(type);
        HashSet<DocPerson> docPeoples = setDocPerson(activity.getMembers());
        DocInterpret.initActivityDocContent(activityEle, activity, docPeoples);
        saveXml(docXml.asXML());
        return 0;
    }

    @Override
    public void userJoin(String aid, String userId) {
        //Acquire user information from user server.
        JSONObject userTag = userDispatch.getUserTag(userId);
        if (userTag == null) return;
        ArrayList<String> domains = userTag.getObject("domain", ArrayList.class);
        ArrayList<String> organizations = userTag.getObject("organization", ArrayList.class);
        UserEntity user = userDao.findUserByIdOrEmail(userId);
        DocPerson docPerson = new DocPerson();
        docPerson.setRole("ordinary-member");
        docPerson.setName(user.getName());
        docPerson.setEmail(user.getEmail());
        docPerson.setUserId(userId);
        docPerson.setDomain(new HashSet<>(domains));
        docPerson.setOrganization(new HashSet<>(organizations));

        Document activityDocXml = loadXML(aid);
        DocInterpret.appendParticipant(activityDocXml, docPerson);
        saveXml(activityDocXml.asXML());
    }

    @Override
    public void userOut(String aid, String userId) {
        Document docXml = loadXML(aid);
        DocInterpret.removeParticipant(docXml, userId);
        saveXml(docXml.asXML());
    }

    @Override
    public Object userJoin(String aid, HashSet<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return null;
        //增加对临时用户的处理 todo
        JSONObject usersTag = userDispatch.getUsersTag(userIds);
        HashSet<DocPerson> docPeoples = new HashSet<>();
        for (String userId : userIds) {
            UserEntity user = userDao.findUserByIdOrEmail(userId);
            if (user == null) continue;
            DocPerson docPerson = new DocPerson();
            docPerson.setUserId(userId);
            docPerson.setName(user.getName());
            docPerson.setEmail(user.getEmail());
            docPerson.setRole("ordinary-member");

            JSONObject userTag = usersTag.getJSONObject(userId);
            HashSet<String> domains = JSONObject.parseObject(JSONObject.toJSONString(userTag.get("domain")), HashSet.class);
            HashSet<String> organizations = JSONObject.parseObject(JSONObject.toJSONString(userTag.get("domain")), HashSet.class);

            docPerson.setDomain(domains);
            docPerson.setOrganization(organizations);

            docPeoples.add(docPerson);
        }

        Document docXml = loadXML(aid);
        DocInterpret.appendParticipant(docXml, docPeoples);
        saveXml(docXml.asXML());

        return null;
    }

    @Override
    public Object uploadResource(String aid, HashMap<String, String> resInfo) {
        return null;
    }

    @Override
    public ArrayList<HashMap<String, String>> uploadResource(String aid,
                                                             ArrayList<ResourceEntity> resList) {
        Document docXml = loadXML(aid);
        ArrayList<HashMap<String, String>> uploadResInfo = DocInterpret.appendResource(docXml, resList, null, "upload");
        saveXml(docXml.asXML());
        return uploadResInfo;
    }

    @Override
    public ArrayList<HashMap<String, String>> uploadResource(String aid,
                                                             ArrayList<ResourceEntity> res,
                                                             HashMap<String, String> meta) {
        Document docXml = loadXML(aid);
        ArrayList<HashMap<String, String>> uploadResInfo = DocInterpret.appendResource(docXml, res, meta, "upload");
        saveXml(docXml.asXML());
        return uploadResInfo;
    }

    /**
     * 分享资源
     * 从文档中读取被分享资源节点,修改 id后,存入被分享的文档中
     * @param fromAid
     * @param toAid
     * @param idMap 存储资源新旧id的映射关系
     */
    @Override
    public void shareResource(String fromAid, String toAid, HashMap<String, String> idMap) {
        HashSet<String> idSet = (HashSet)idMap.keySet();
        Document docXml = loadXML(fromAid);
        List<Element> resElement = DocInterpret.getResElement(docXml, idSet);
        ArrayList<Element> copyResEle = Lists.newArrayList();
        for (Element item : resElement){
            Element newEle = item.createCopy();
            String id = newEle.attributeValue("id");
            String newUid = idMap.get(id);
            newEle.addAttribute("id", newUid);
            copyResEle.add(newEle);
        }
        Document toDocXml = loadXML(toAid);
        DocInterpret.appendResource(toDocXml, copyResEle);
        saveXml(toDocXml.asXML());
    }

    /**
     * 从个人空间拉取资源到活动
     * 全都是 data 类型
     * @param aid
     * @param resList
     */
    @Override
    public ArrayList<HashMap<String, String>> shareResource(String aid, ArrayList<ResourceEntity> resList) {
        if (resList == null || resList.isEmpty()) return null;
        Document docXml = loadXML(aid);
        ArrayList<HashMap<String, String>> sharedDocResult = DocInterpret.appendResource(docXml, resList, "shared");
        saveXml(docXml.asXML());
        return sharedDocResult;
    }

    @Override
    public void removeResource(String aid, String uid) {
        Document docXml = loadXML(aid);
        DocInterpret.removeResource(docXml, uid);
        saveXml(docXml.asXML());
    }

    @Override
    public void removeResource(String aid, HashSet<String> uids) {
        Document docXml = loadXML(aid);
        DocInterpret.removeResources(docXml, uids);
        saveXml(docXml.asXML());
    }

    @Override
    public void resFlow(String fromAid, String endAid, HashSet<String> uids) {
        Document fromDocXml = loadXML(fromAid);
        if (uids == null || uids.isEmpty()) return;
        Document endDocXml = loadXML(endAid);
        try {
            List<Element> resElementList = DocInterpret.getResElement(fromDocXml, uids);
            if (resElementList == null || resElementList.isEmpty()) return;
            List<Element> copiedResEles = Lists.newArrayList();
            for (Iterator<Element> it = resElementList.iterator(); it.hasNext();){
                Element item = it.next();
                copiedResEles.add(item.createCopy());
            }
            DocInterpret.appendResource(endDocXml, copiedResEles);

            saveXml(endDocXml.asXML());
        }catch (Exception e){
            log.warn("Failed to write document when resource is flowing.");
            return;
        }
    }

    @Override
    public HashMap<String, String> resFlow(String fromAid, String endAid, String uid) {
        Document fromDocXml = loadXML(fromAid);
        Document endDocXml = loadXML(endAid);

        Element resEle = DocInterpret.getResElement(fromDocXml, uid);
        if (resEle == null) return null;
        Element endResEle = resEle.createCopy();
        DocInterpret.appendResource(endDocXml, endResEle);
        saveXml(endDocXml.asXML());
        return DocInterpret.resNode2Map(resEle);
    }

    // @Override
    // public void resFlow(String fromAid, String endAid, HashMap<String, String> idMap) {
    //     try {
    //         Document fromDocXml = loadXML(fromAid);
    //         Document endDocXml = loadXML(endAid);
    //         if (idMap == null || fromDocXml == null || endDocXml == null) return;
    //         Set<String> oldUidSet = idMap.keySet();
    //         if (oldUidSet == null) return;
    //         List<Element> resEleList = DocInterpret.getResElement(fromDocXml, (HashSet<String>) oldUidSet);
    //         List<Element> copiedResEleList = Lists.newArrayList();
    //         for (Element item : resEleList){
    //             Element copiedEle = item.createCopy();
    //             String oldId = item.attributeValue("id");
    //             copiedEle.attribute("id").setValue(idMap.get(oldId));
    //             copiedResEleList.add(copiedEle);
    //         }
    //         DocInterpret.appendResource(endDocXml, copiedResEleList);
    //         saveXml(endDocXml.asXML());
    //     }catch (Exception e){
    //         log.warn("Failed to write document when resource is flowing.");
    //         return;
    //     }
    // }
    //
    // @Override
    // public HashMap<String, String> resFlow(String fromAid, String endAid, String oldUid, String newUid) {
    //     Document fromDocXml = loadXML(fromAid);
    //     Document endDocXml = loadXML(endAid);
    //     Element resEle = DocInterpret.getResElement(fromDocXml, oldUid);
    //     if (resEle == null) return null;
    //     Element endResEle = resEle.createCopy();
    //     endResEle.attribute("id").setValue(newUid);
    //     DocInterpret.appendResource(endDocXml, endResEle);
    //     saveXml(endDocXml.asXML());
    //     return DocInterpret.resNode2Map(resEle);
    // }

    @Override
    public ArrayList<HashMap<String, String>> getResInfo(String aid, HashSet<String> uids) {
        Document docXml = loadXML(aid);
        if (uids == null) return null;
        ArrayList<HashMap<String, String>> resInfos = new ArrayList<>();
        for (String uid : uids) {
            HashMap<String, String> resInfo = DocInterpret.getResInfo(docXml, uid);
            if (resInfo != null) resInfos.add(resInfo);
        }
        return resInfos;
    }

    @Override
    public HashMap<String, String> getResInfo(String aid, String uid) {
        Document docXml = loadXML(aid);
        return DocInterpret.getResInfo(docXml, uid);
    }

    /**
     * 获取该活动文档中的所有 Resource
     * @param aid
     * @return all resource info
     */
    @Override
    public ArrayList<HashMap<String, String>> getResInfo(String aid) {
        Document docXml = loadXML(aid);
        return DocInterpret.getResAllResInfo(docXml);
    }

    @Override
    public void appendChildActivity(String aid, String childId, String name, String creator) {
        Document docXml = loadXML(aid);
        DocInterpret.appendChild(docXml, childId, name, creator);
        saveXml(docXml.asXML());
    }

    @Override
    public void initSubActivityDoc(Activity activity) {
        initActivityDoc(activity);
        Document docXml = loadXML(activity.getParent());
        DocInterpret.appendChild(docXml, activity.getAid(), activity.getName(), activity.getCreator());
        saveXml(docXml.asXML());
    }

    @Override
    public Object addTool(String aid, List<Tool> tools) {
        Document docXml = loadXML(aid);
        DocInterpret.appendTool(docXml, tools);
        saveXml(docXml.asXML());
        return null;
    }

    @Override
    public void refreshTool(String aid, List<Tool> tools) {
        Document docXml = loadXML(aid);
        DocInterpret.emptyTool(docXml);
        DocInterpret.appendTool(docXml, tools);
        saveXml(docXml.asXML());
    }

    @Override
    public void updateChild(String aid, String childId, String name) {
        Document docXml = loadXML(aid);
        DocInterpret.updateChild(docXml, childId, name);
        saveXml(docXml.asXML());
    }

    @Override
    public void updateRoot(String aid, HashMap<String, String> updateInfo) {
        Document docXml = loadXML(aid);
        DocInterpret.updateRoot(docXml, updateInfo);
        saveXml(docXml.asXML());
    }

    @Override
    public Object removeTool(String aid, String tid) {
        Document docXml = loadXML(aid);
        DocInterpret.removeTool(docXml, tid);
        saveXml(docXml.asXML());
        return null;
    }

    @Override
    public Object removeTool(String aid, HashSet<String> tids) {
        return null;
    }

    @Override
    public String messageRecord(String tid, MsgRecords msgRecords) {
        Document docXml = loadXML(msgRecords.getAid());
        Element communicationOperation = DocInterpret.createCommunicationOperation(tid, msgRecords);
        if (communicationOperation == null) return null;
        DocInterpret.appendOperation(docXml, communicationOperation);
        saveXml(docXml.asXML());
        return communicationOperation.attributeValue("id");
    }

    /**
     * geoAnalysis 写文档操作
     * @param aid
     * @param toolId
     * @param inResId
     * @param outRes
     * @param participants
     * @return
     */
    @Override
    public HashSet<Operation> getGeoAnalysisInTool(String aid, String tid){
        // 1.根据aid拿到活动文档
        // 2.根据tid筛选geo—analysis记录
        HashSet<Operation> operations = new HashSet<>();
        Document docXml = loadXML(aid);
        //获取 geoAnalysis 节点
        List<Node> geoAnalysisOperations = DocInterpret.getGeoAnalysisOperation(docXml);
        if (geoAnalysisOperations == null || geoAnalysisOperations.size() == 0) return operations;
        for (Node operationNode : geoAnalysisOperations) {
            Element operationEle = (Element) operationNode;
            String oid = operationEle.attributeValue("id");
            String toolRef = operationEle.attributeValue("toolRef");
            String description = operationEle.attributeValue("purpose");
            String time = operationEle.attributeValue("time");

            if(toolRef.equals(tid)){
                // 判断geo-analysis操作
                // 获取 geoAnalysis 的 input 信息
                List<Node> inputResRefs = operationEle.selectNodes(".//ResRef[@type= 'input']");
                HashSet<OperationRes> inputRes = operationRes2Entity(inputResRefs, docXml, "file");
                List<Node> outputResRefs = operationEle.selectNodes(".//ResRef[@type = 'output']");
                HashSet<OperationRes> outputRes = operationRes2Entity(outputResRefs, docXml, "file");
                // 输入中会有类型为参数的部分
                List<Node> paramRefs = operationEle.selectNodes(".//ResRef[@type = 'param']");
                HashSet<OperationRes> inParam = operationRes2Entity(paramRefs, docXml, "param");
                // throw nullPointException,将参数信息加入输入
                inputRes.addAll(inParam);

                Operation operation = new Operation();
                operation.setInput(inputRes);
                operation.setOutput(outputRes);
                operation.setOid(oid);
                operation.setToolId(toolRef);
                operation.setDescription(description);
                operation.setTime(time);
                operations.add(operation);
            }

        }
        return operations;
    }

    @Override
    public String geoAnalysis(String aid, String toolId,
                              HashSet<String> inResId, ArrayList<ResourceEntity> outRes,
                              HashSet<String> participants) {
        Document docXml = loadXML(aid);
        DocInterpret.appendResource(docXml, outRes, null,"generate");
        HashSet<String> outResId = new HashSet<>();
        for (ResourceEntity item : outRes) {
            outResId.add(item.getUid());
        }
        HashMap<String, String> toolInfo = DocInterpret.getToolInfo(docXml, toolId);
        String purpose = toolInfo.get("function");
        String oid = DocInterpret.appendGeoAnalysisOperation(docXml, toolId, participants, purpose, inResId, outResId);
        saveXml(docXml.asXML());
        return oid;
    }

    @Override
    public String geoAnalysis(String aid, String toolId,
                              HashSet<String> inResId, ResourceEntity outRes,
                              HashSet<String> participants) {
        ArrayList<ResourceEntity> resList = new ArrayList<>();
        resList.add(outRes);
        return geoAnalysis(aid, toolId, inResId, resList, participants);
    }

    @Override
    public String geoAnalysisRLS90(String aid,
                                   String toolId,
                                   HashSet<String> onlineMembers,
                                   String purpose,
                                   ResourceEntity input,
                                   ResourceEntity output) {
        Document docXml = loadXML(aid);
        ArrayList<ResourceEntity> resList = new ArrayList<>();
        resList.add(output);
        DocInterpret.appendResource(docXml, resList, null,"generate");
        HashSet<String> inResId = new HashSet<>();
        HashSet<String> outResId = new HashSet<>();
        if (input != null) inResId.add(input.getUid());
        if (output != null) outResId.add(output.getUid());
        DocInterpret.appendGeoAnalysisOperation(docXml, toolId, onlineMembers, purpose, inResId, outResId);
        saveXml(docXml.asXML());
        return null;
    }

    /**
     * 活动文档生成工作流模板
     * 活动文档 ---> 工作流模板: 参与式平台的实现方式
     * <p>
     * 具体面向的对象应该是参与式地理分析过程
     * <p>
     * 加入部分内容
     *
     * @param aid
     * @return
     */
    @Override
    public Object toWorkflowTemplate(String aid) {
        Document docXml = loadXML(aid);
        Optional<Project> byId = projectRepository.findById(aid);
        if (!byId.isPresent()) {
            log.warn("No such project, activity id is ---> " + aid);
            return null;
        }
        Project project = byId.get();
        //工作流模板中地理问题描述
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setAid(aid);
        projectInfo.setDescription(project.getDescription());
        projectInfo.setTag(project.getTag());
        WorkflowTemplate workflowTemplate = new WorkflowTemplate();
        workflowTemplate.setId(aid);
        workflowTemplate.setProjectInfo(projectInfo);
        workflowTemplate.setResVisible(true);
        workflowTemplate.setName("test");
        int level = 0;
        WorkflowTemplate template = a2WorkflowTemplate(docXml, workflowTemplate, level);

        return template;
        /*
        工具需可见，资源可不可见
        将八个步骤也给放进去
        {
            name: String,
            resVisible: boolean,
            processStep: String,
            //地理问题的简单描述
            projectInfo: {
                aid: string,
                description: string,
                tag: sting
            },
            //Geo-operator
            operations: [
            //输入无对应输入时则增加对应input的输入
                {
                oid: string,
                //模型
                toolId: string,
                description: string,
                //数据
                input: [
                    {
                        uid: string,
                        name: string,
                        description: string
                        type: string[file/param],
                        value: string(address/value)
                    }...
                ],
                //输出结果
                output: [
                    {
                        id: string,
                        name: string,
                        description: string
                        value: String
                    }
                ]
                },
            ],
            resBehavior: [
                {
                    uid: String,
                    name: String,
                    behavior: String,
                    value: String
                }
            ]
         }
         */
    }

    /*
    递归函数
     */
    private WorkflowTemplate a2WorkflowTemplate(Document docXml, WorkflowTemplate workflowTemplate, int level) {
        //传入的是活动文档，所以取到的是 activity
        //通过递归层数判断属于哪个层次 project(0)-subProject(1)-activity(>=2)
        HashMap<String, String> activityInfo = DocInterpret.getRootInfo(docXml);
        String aType = activityInfo.get("type");
        if (aType.equals(ActivityType.Activity_Group.toString())) {
            //group 遍历其中的子活动取出其中的操作
            HashSet<HashMap<String, String>> childActivitiesList = DocInterpret.getChildActivities(docXml);
            if (childActivitiesList == null) return workflowTemplate;
            //递归进入下一层
            for (Iterator<HashMap<String, String>> it = childActivitiesList.iterator(); it.hasNext(); ) {
                HashMap<String, String> aInfo = it.next();
                String aid = aInfo.get("id");
                Document childDocXml = loadXML(aid);
                ++level;
                a2WorkflowTemplate(childDocXml, workflowTemplate, level);
            }
        } else if (aType.equals(ActivityType.Activity_Unit.toString())) {
            //获取 geoAnalysis 节点
            List<Node> geoAnalysisOperations = DocInterpret.getGeoAnalysisOperation(docXml);
            if (geoAnalysisOperations == null || geoAnalysisOperations.size() == 0) return workflowTemplate;
            HashSet<Operation> operations = new HashSet<>();
            List<Node> inputs = Lists.newLinkedList();
            for (Node operationNode : geoAnalysisOperations) {
                Element operationEle = (Element) operationNode;
                String oid = operationEle.attributeValue("id");
                String toolRef = operationEle.attributeValue("toolRef");
                String description = operationEle.attributeValue("purpose");

                //获取 geoAnalysis 的 input 信息
                List<Node> inputResRefs = operationEle.selectNodes(".//ResRef[@type= 'input']");
                HashSet<OperationRes> inputRes = operationRes2Entity(inputResRefs, docXml, "file");
                List<Node> outputResRefs = operationEle.selectNodes(".//ResRef[@type = 'output']");
                HashSet<OperationRes> outputRes = operationRes2Entity(outputResRefs, docXml, "file");
                //输入中会有类型为参数的部分
                List<Node> paramRefs = operationEle.selectNodes(".//ResRef[@type = 'param']");
                HashSet<OperationRes> inParam = operationRes2Entity(paramRefs, docXml, "param");
                //throw nullPointException,将参数信息加入输入
                inputRes.addAll(inParam);

                Operation operation = new Operation();
                operation.setInput(inputRes);
                operation.setOutput(outputRes);
                operation.setOid(oid);
                operation.setToolId(toolRef);
                operation.setDescription(description);
                operations.add(operation);
                //添加purpose 信息
                //活动 id
                String aid = activityInfo.get("id");
                Activity activity;
                if (level == 0) {
                    activity = projectRepository.findById(aid).get();
                } else if (level == 1) {
                    activity = subprojectRepository.findById(aid).get();
                } else {
                    activity = activityRepository.findById(aid).get();
                }
                if (activity != null) operation.setPurpose(activity.getPurpose());

                //提取 upload operation
                inputs.addAll(inputResRefs);
                inputs.addAll(paramRefs);
            }
            //将此子活动的操作添加到整个活动流中去
            HashSet<Operation> previous = workflowTemplate.getOperations();
            if (previous != null) operations.addAll(previous);
            workflowTemplate.setOperations(operations);

            //
            HashSet<HashMap<String, String>> resBehavior = toResBehavior(inputs, docXml);
            HashSet<HashMap<String, String>> previousResBehaviors = workflowTemplate.getResBehavior();
            if (previousResBehaviors != null) resBehavior.addAll(previousResBehaviors);
            workflowTemplate.setResBehavior(resBehavior);
        }
        //当前层次内容处理结束，返回上一层
        --level;
        return workflowTemplate;
    }

    /*
    将 xml analysis 中 in/out 转换为操作资源实体
     */
    private HashSet<OperationRes> operationRes2Entity(List<Node> inRes, Document docXml, String type) {
        if (inRes == null) return null;
        HashSet<OperationRes> operationResList = new HashSet<>();
        for (Node resNode : inRes) {
            Element resEle = (Element) resNode;
            OperationRes operationRes = new OperationRes();
            String uid = resEle.attributeValue("idRef");
            String eventName = resEle.attributeValue("name");
            String eventDescription = resEle.attributeValue("description");
            operationRes.setName(eventName);
            operationRes.setDescription(eventDescription);
            operationRes.setUid(uid);
            operationRes.setType(type);
            Element resElement = DocInterpret.getResElement(docXml, uid);
            if (resElement != null) {
                operationRes.setValue(resElement.attributeValue("href"));
            }
            operationResList.add(operationRes);
        }
        return operationResList;
    }

    /*
    将资源操作转换为所需的格式
     */
    private HashSet<HashMap<String, String>> toResBehavior(List<Node> inRes, Document docXml) {
        if (inRes == null) return null;
        HashSet<HashMap<String, String>> resBehaviors = new HashSet<>();
        for (Node resNode : inRes) {
            Element resOperationEle = (Element) resNode;
            String uid = resOperationEle.attributeValue("idRef");
            //获取资源的上传操作
            Element resUploadEle = DocInterpret.getResourceOperationByResId(docXml, uid);
            if (resUploadEle != null) {
                Element resInfoEle = DocInterpret.getResElement(docXml, uid);
                if (resInfoEle != null) {
                    HashMap<String, String> resBehavior = new HashMap<>();
                    resBehavior.put("uid", resInfoEle.attributeValue("id"));
                    resBehavior.put("name", resInfoEle.attributeValue("name"));
                    resBehavior.put("value", resInfoEle.attributeValue("href"));
                    resBehavior.put("behavior", "upload");
                    resBehaviors.add(resBehavior);
                }
            }
        }
        return resBehaviors;
    }
}

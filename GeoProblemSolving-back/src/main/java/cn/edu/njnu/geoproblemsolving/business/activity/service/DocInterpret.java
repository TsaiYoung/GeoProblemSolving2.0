package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.Dao.Folder.IFolderDao;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.TagUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.UserDispatch;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityDocRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ProjectRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.Impl.ActivityResServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sun.awt.image.GifImageDecoder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName DocInterpret
 * @Description 活动文档解析
 * 整体设计思路应该是外部来调用这里面
 * 而不是来修改内容代码，外部逻辑未知
 * 内部提供丰富的基础接口，服务层来拼装这些接口，来完成业务逻辑
 * 1. 活动文档解析
 * 2. 地理问题求解模板文档
 * 3. 文档间的映射方法
 * <p>
 * 后台解析与前台解析的区别
 * 1. 后台解析肯定是更快
 * 2.
 * <p>
 * 封装的工具类
 * @Author zhngzhng
 * @Date 2021/10/20
 **/
@Service
public class DocInterpret implements ActivityDocParser {
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
    NodeService nodeService;

    @Autowired
    UserDispatch userDispatch;

    @Value("${userServerLocation}")
    String userServer;

    @Autowired
    RestTemplate restTemplate;

    //Used to simplify operations, avoid loading every time.
    private Document activityDocXml;
    private ActivityDoc operatingDoc;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void loadXML(String aid) {
        Optional<ActivityDoc> byId = docRepository.findById(aid);
        if (!byId.isPresent()) {
            return;
        }
        operatingDoc = byId.get();
        try {
            activityDocXml = DocumentHelper.parseText(operatingDoc.getDocument());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void saveXML() {
        if (activityDocXml == null) return;
        String docXMLStr = activityDocXml.asXML();
        operatingDoc.setDocument(docXMLStr);
        docRepository.save(operatingDoc);
    }

    private void syncGlobalVariables(String aid) {
        if (operatingDoc == null || !operatingDoc.getAid().equals(aid)) {
            if (operatingDoc != null) saveXML();
            loadXML(aid);
        }
    }

    //Fast looping which avoids the cost of creating an Iterator object for each loop.
    private void treeWalk(Element element) {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Element) {

            }
        }
    }

    //===================Document level operations===================================================
    /*
    Document level operations
    Initialize document
    Change the document type
     */

    @Override
    public void initActivityDoc(String aid, String level) {

    }

    public void initActivityDoc(Activity activity) {
        Document document = DocumentHelper.createDocument();
        String activityType = activity.getType().toString();

        //add rootElement
        Element activityEle = document.addElement("Activity");
        activityEle.addAttribute("id", activity.getAid());
        activityEle.addAttribute("name", activity.getName());
        activityEle.addAttribute("type", activityType);
        activityEle.addAttribute("description", activity.getDescription());

        initActivityContent(activityEle, activity);

        //文档入库
        String xmlStr = document.asXML();
        ActivityDoc activityDoc = new ActivityDoc();
        activityDoc.setAid(activity.getAid());
        activityDoc.setDocument(xmlStr);
        docRepository.save(activityDoc);
    }

    @Override
    public void changeActivityType(String aid, Activity activity) {
        loadXML(aid);
        if (operatingDoc == null) return;
        Element activityEle = activityDocXml.getRootElement();
        activityEle.clearContent();
        String type = activity.getType().toString();
        activityEle.attribute("type").setValue(type);

        initActivityContent(activityEle, activity);
        saveXML();
    }

    //Change the document type.
    private void activityOperation(Activity activity, String... params) throws DocumentException {
        //param: operationType, String aid, String level
        if (activityDocXml == null) {
            loadXML(activity.getAid());
        }
        String operationType = params[0];
        if (operationType.equals("type")) {
            Element activityEle = activityDocXml.getRootElement();
            String acType = activityEle.valueOf("@type");
            if (acType.equals("Activity_Group")) {
                Element childActivitiesEle = activityEle.element("ChildActivities");
                childActivitiesEle.getParent().remove(childActivitiesEle);
                Element activityDependenciesEle = activityEle.element("ActivityDependencies");
                activityDependenciesEle.getParent().remove(activityDependenciesEle);
            } else if (acType.equals("Activity_Unit")) {
                Element toolBoxEle = activityEle.element("ToolBox");
                toolBoxEle.getParent().remove(toolBoxEle);
                Element taskListEle = activityEle.element("TaskList");
                taskListEle.getParent().remove(taskListEle);
                Element taskDependencyEle = activityEle.element("TaskDependency");
                taskDependencyEle.getParent().remove(taskDependencyEle);
            }
        }
        if (activity == null) {
            String aid = params[1];
            String level = params[2];
        }
    }


    //=================Generic operation==============================================================
    /*
    Generic operation
    Common item between multi and signal activity:
    Participants
    ResourceCollection
    OperationRecords(Multi: process, activity, communication; Signal: resource, tool, communication, geo-analysis)
     */

    /*
        1. Participants related operations
     */
    @Override
    public Object inActivity(String aid, HashMap<String, String> userInfo) {
        loadXML(aid);
        if (activityDocXml == null) return null;
        Element participantsEle = (Element) activityDocXml.selectNodes("/Activity/Participants").get(0);
        Element personEle = participantsEle.addElement("Person");
        personEle.addAttribute("id", userInfo.get("userId"));
        personEle.addAttribute("email", userInfo.get("email"));
        personEle.addAttribute("name", userInfo.get("name"));
        personEle.addAttribute("role", userInfo.get("role"));
        personEle.addAttribute("state", "in");

        //Acquire user's domain and organization
        String userId = userInfo.get("userId");
        String getTagUrl = "http://" + userServer + "/user/tag/" + userId;
        try {
            JSONObject response = restTemplate.getForObject(getTagUrl, JSONObject.class);
            if (response.getInteger("code") != 0) return null;
            JSONObject result = response.getJSONObject("data");
            JSONArray domain = result.getJSONArray("domain");
            if (domain != null) {
                for (Object item : domain) {
                    Element domainEle = personEle.addElement("Domain");
                    domainEle.addAttribute("description", (String) item);
                }
            }
            JSONArray organization = result.getJSONArray("organization");
            if (organization != null) {
                for (Object item : organization) {
                    Element organizationEle = personEle.addElement("Organization");
                    organizationEle.addAttribute("description", (String) item);
                }
            }
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
        saveXML();
        return null;
    }


    @Value("client_id")
    String clientId;
    @Value("client_secret")
    String clientSecret;

    public Object insActivity(String aid, HashSet<String> userIds) throws DocumentException {
        for (String userId : userIds) {
            String reqUrl = "http://" + userServer + "/user/" + userId + "/" + userId + "/" + clientId + "/" + clientSecret;
            JSONObject response = restTemplate.getForObject(reqUrl, JSONObject.class);
            if (response.getInteger("code") == 0) {
                UserEntity user = response.getObject("data", UserEntity.class);
                inActivity(aid, user);
            }
        }
        return null;
    }

    public Object inActivity(String aid, UserEntity user) throws DocumentException {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (activityDocXml == null) return null;
        Element participantsEle = (Element) activityDocXml.selectNodes("/Activity/Participants").get(0);
        Element personEle = participantsEle.addElement("Person");
        personEle.addAttribute("id", user.getUserId());
        personEle.addAttribute("email", user.getEmail());
        personEle.addAttribute("name", user.getName());
        personEle.addAttribute("role", "ordinary-member");
        personEle.addAttribute("state", "in");

        ArrayList<String> domain = user.getDomain();
        if (domain != null) {
            for (String item : domain) {
                Element domainEle = personEle.addElement("Domain");
                domainEle.addAttribute("description", item);
            }
        }
        ArrayList<String> organization = user.getOrganizations();
        if (organization != null) {
            for (String item : organization) {
                Element organizationEle = personEle.addElement("Organization");
                organizationEle.addAttribute("description", (String) item);
            }
        }
        saveXML();
        return null;
    }


    @Override
    public Object outActivity(String aid, String userId) {
        return null;
    }

    @Override
    public Object rolePut(String aid, String userId, String role) {
        return null;
    }

    @Override
    public Object userDomainPut(String aid, String userId, HashSet<String> domains) {
        return null;
    }

    @Override
    public Object userOrganizationPut(String aid, String userId, HashSet<String> organizations) {
        return null;
    }

    /*
        2.Resources related operations
     */

    @Override
    public Object uploadResource(String aid, HashMap<String, String> resInfo) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (activityDocXml == null) return null;
        Element resourceCollectionNode = (Element) activityDocXml.selectNodes("/Activity/ResourceCollection").get(0);
        if (resourceCollectionNode == null) resourceCollectionNode = DocumentHelper.createElement("ResourceCollection");
        Element resourceEle = resourceCollectionNode.addElement("Resource");
        resourceEle.addAttribute("id", resInfo.get("uid"));
        resourceEle.addAttribute("name", resInfo.get("name"));
        resourceCollectionNode.addAttribute("type", resInfo.get("type"));
        resourceCollectionNode.addAttribute("href", resInfo.get("address"));
        resourceCollectionNode.addAttribute("state", "accessible");
        saveXML();
        return null;
    }

    @Override
    public ArrayList<HashMap<String, String>> uploadResources(String aid, ArrayList<ResourceEntity> uploadList, HashMap<String, String> meta) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (activityDocXml == null) return null;
        if (uploadList == null || uploadList.size() == 0) return null;
        ArrayList<HashMap<String, String>> uploadOperationList = new ArrayList<>();
        Element resCollectionEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection");
        Element operationRecordEle = (Element) activityDocXml.selectSingleNode("/Activity/OperationRecords");
        //添加 operation，单活动才需要有操作
        for (ResourceEntity item : uploadList) {
            HashMap<String, String> uploadOperation = new HashMap<>();
            Element resEle = resCollectionEle.addElement("Resource");
            resEle.addAttribute("id", item.getUid());
            resEle.addAttribute("name", item.getName());
            resEle.addAttribute("type", item.getType());
            resEle.addAttribute("provider", item.getUploaderId());
            resEle.addAttribute("href", item.getAddress());
            resEle.addAttribute("state", "accessible");
            if (item.getType().equals("data") && meta != null) {
                for (Map.Entry<String, String> entry : meta.entrySet()) {
                    String metaValue = entry.getValue();
                    if (metaValue != null && !metaValue.equals("")) {
                        Element metaEle = resEle.addElement("Metadata");
                        metaEle.addAttribute("type", entry.getKey());
                        metaEle.addAttribute("description", metaValue);
                    }
                }
            }
            //添加suffix
            if (resEle.selectSingleNode("/Metadata[@type='format']") == null){
                Element formatEle = resEle.addElement("Metadata");
                formatEle.addAttribute("type", "format");
                String suffix = item.getSuffix();
                int index = suffix.indexOf(".");
                formatEle.addAttribute("description", suffix.substring(index+1));
            }
            Element operationEle = operationRecordEle.addElement("Operation");
            String oid = UUID.randomUUID().toString();
            operationEle.addAttribute("id", oid);
            operationEle.addAttribute("type", "resource");
            operationEle.addAttribute("behavior", "upload");
            operationEle.addAttribute("resRef", item.getUid());
            operationEle.addAttribute("operator", item.getUploaderId());
            operationEle.addAttribute("task", "");
            operationEle.addAttribute("time", simpleDateFormat.format(new Date()));

            uploadOperation.put("oid", oid);
            uploadOperation.put("resRef", item.getUid());
            uploadOperationList.add(uploadOperation);
        }
        saveXML();
        return uploadOperationList;
    }

    @Override
    public Object uploadResource(String aid, ResourceEntity res) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (activityDocXml == null) return null;
        Element resourceCollectionNode = (Element) activityDocXml.selectNodes("/Activity/ResourceCollection").get(0);
        if (resourceCollectionNode == null) resourceCollectionNode = DocumentHelper.createElement("ResourceCollection");
        Element resourceEle = resourceCollectionNode.addElement("Resource");
        resourceEle.addAttribute("id", res.getUid());
        resourceEle.addAttribute("name", res.getName());
        resourceCollectionNode.addAttribute("type", res.getType());
        resourceCollectionNode.addAttribute("href", res.getAddress());
        resourceCollectionNode.addAttribute("state", "accessible");
        saveXML();
        return null;
    }

    @Autowired
    ActivityResServiceImpl ripServer;

    //Store the flow resource in the document.
    public Object flowResourceUpload(String fromAid, String endAid, HashSet<String> uids) throws DocumentException {
        if (uids == null || uids.size() == 0) return null;
        // syncGlobalVariables(endAid);
        loadXML(endAid);
        ActivityDoc activityDoc = docRepository.findById(fromAid).get();
        Document fromDocXML = DocumentHelper.parseText(activityDoc.getDocument());
        Element resCollectionEle = (Element) activityDocXml.selectNodes("/Activity/ResourceCollection").get(0);
        for (String uid : uids) {
            Element resEle = (Element) fromDocXML.selectNodes("/Activity/ResourceCollection//Resource[@id = uid]").get(0);
            resCollectionEle.add(resEle);
        }
        return null;
    }

    public Object flowResourceUpload(String fromAid, String endAid, String resId) throws DocumentException {
        HashSet<String> strings = new HashSet<>();
        strings.add(resId);
        flowResourceUpload(fromAid, endAid, strings);
        return null;
    }


    @Override
    public Object putRes(String aid, HashMap<String, String> putInfo) {
        return null;
    }

    @Override
    public Object removeRes(String aid, String rid) {
        return null;
    }

    @Override
    public ArrayList<HashMap<String, String>> getResInfo(String aid, HashSet<String> uids) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        Element activityEle = activityDocXml.getRootElement();
        ArrayList<HashMap<String, String>> resInfoList = new ArrayList<>();
        for (Iterator<String> it = uids.iterator(); it.hasNext(); ) {
            String uid = it.next();
            Element resourceEle = (Element) activityEle.selectSingleNode("//ResourceCollection/Resource[@id = '" + uid + "']");
            if (resourceEle == null) continue;
            String type = resourceEle.attributeValue("type");
            HashMap<String, String> resInfoMap = new HashMap<>();
            resInfoMap.put("uid", uid);
            resInfoMap.put("type", type);
            if (!type.equals("data")) {
                resInfoList.add(resInfoMap);
                continue;
            } else {
                for (Iterator<Element> mIt = resourceEle.elementIterator("Metadata"); mIt.hasNext(); ) {
                    Element metaEle = mIt.next();
                    resInfoMap.put(metaEle.attributeValue("type"), metaEle.attributeValue("description"));
                }
                resInfoList.add(resInfoMap);
            }
        }
        return resInfoList;
    }

    @Override
    public HashMap<String, String> getResInfo(String aid, String uid) {
        if (activityDocXml != null && !operatingDoc.getAid().equals(aid)) saveXML();
        loadXML(aid);
        Element activityEle = activityDocXml.getRootElement();
        Element resourceEle = (Element) activityEle.selectSingleNode("//ResourceCollection/Resource[@id = '" + uid + "']");
        if (resourceEle == null) return null;
        HashMap<String, String> resInfoMap = new HashMap<>();
        String type = resourceEle.attributeValue("type");
        resInfoMap.put("uid", uid);
        resInfoMap.put("type", type);
        if (type.equals("data")) {
            for (Iterator<Element> mIt = resourceEle.elementIterator("Metadata"); mIt.hasNext(); ) {
                Element metaEle = mIt.next();
                resInfoMap.put(metaEle.attributeValue("type"), metaEle.attributeValue("description"));
            }
        }
        return resInfoMap;
    }

    @Override
    public ArrayList<HashMap<String, String>> getAllResInfo(String aid) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        ArrayList<HashMap<String, String>> resInfoList = new ArrayList<>();
        List<Node> resNodes = activityDocXml.selectNodes("/Activity/ResourceCollection/Resource[@state = 'accessible']");
        for (int i = 0; i < resNodes.size(); i++) {
            //虚假的随机访问，实际上每次get，都需要从头往后
            Element resEle = (Element) resNodes.get(i);
            HashMap<String, String> resInfo = new HashMap<>();
            String uid = resEle.attributeValue("id");
            String type = resEle.attributeValue("type");
            resInfo.put("uid", uid);
            resInfo.put("type", type);
            if (type.equals("data")) {
                for (Iterator<Element> mIt = resEle.elementIterator("Metadata"); mIt.hasNext(); ) {
                    Element metaEle = mIt.next();
                    resInfo.put(metaEle.attributeValue("type"), metaEle.attributeValue("description"));
                }
            }
            resInfoList.add(resInfo);
        }
        return resInfoList;
    }

    //========================Multi activity operation===================================================
    /*
        1.Operation records
     */



    /*
        2. ChildActivities and activity dependencies
     */

    @Override
    public Object appendChildActivity(String aid, String childId, String name, String creatorId) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element childActivitiesEle = (Element) activityDocXml.selectSingleNode("/Activity/ChildActivities");
        System.out.println("childActivitiesEle" + childActivitiesEle);
        Element oldChildEle = (Element) childActivitiesEle.selectSingleNode("//Child[@id = '" + childId + "']");
        if (oldChildEle != null) {
            childActivitiesEle.remove(oldChildEle);
        }
        Element activityEle = activityDocXml.getRootElement();
        String activityType = activityEle.attributeValue("type");
        if (childActivitiesEle == null && activityType.equals(ActivityType.Activity_Group)) {
            childActivitiesEle = activityDocXml.getRootElement().addElement("ChildActivities");
        }
        Element childEle = childActivitiesEle.addElement("Child");
        childEle.addAttribute("id", childId);
        childEle.addAttribute("name", name);
        childEle.addAttribute("creator", creatorId);
        childEle.addAttribute("state", "accessible");
        saveXML();
        return null;
    }

    //=======================Signal activity operation===================================================
    /*
        1.Tool related operation, include toolbox
     */

    @Override
    public Object addTools(String aid, List<Tool> tools) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        if (tools == null || tools.isEmpty()) return null;
        Element toolBoxEle = (Element) activityDocXml.selectSingleNode("/Activity/ToolBox");
        appendTools(toolBoxEle, tools);
        saveXML();
        return null;
    }

    @Override
    public Object putTools(String aid, List<Tool> tools) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element toolBoxEle = (Element) activityDocXml.selectSingleNode("/Activity/ToolBox");
        if (toolBoxEle == null) return null;
        toolBoxEle.clearContent();
        // if (oldTools != null && !oldTools.isEmpty()){
        //     for (Node item : oldTools){
        //         Element oldToolEle = (Element) item;
        //         oldToolEle.attribute("state").setValue("removed");
        //     }
        // }
        //将新工具添加到ToolBox 中
        appendTools(toolBoxEle, tools);
        saveXML();
        return null;
    }

    private Element appendTools(Element toolBoxEle, List<Tool> tools) {
        for (Tool item : tools) {
            //判断是否有此工具先
            Element oldToolEle = (Element) toolBoxEle.selectSingleNode("//Tool[@id = '" + item.getTid() + "']");
            if (oldToolEle != null) {
                oldToolEle.getParent().remove(oldToolEle);
            }
            Element toolEle = toolBoxEle.addElement("Tool");
            toolEle.addAttribute("id", item.getTid());
            toolEle.addAttribute("name", item.getToolName());
            toolEle.addAttribute("function", item.getDescription());
            toolEle.addAttribute("toolSet", "false");
            toolEle.addAttribute("provider", item.getProvider());
            toolEle.addAttribute("state", "accessible");

            if (item.getBackendType() != null && item.getBackendType().equals("webTool")) {
                toolEle.addAttribute("href", item.getToolUrl());
            }
        }
        return toolBoxEle;
    }
    /*
        2.Task related operation, include task list and task dependency.
     */

    @Override
    public Object bindOperation(String aid, String oid) {
        return null;
    }

    @Override
    public Object unbindOperation(String aid, String oid) {
        return null;
    }

    @Override
    public Object linkTask(String aid, String fromId, String toId) {
        return null;
    }

    /*
        3.Operation records, remain geo-analysis.
     */


    //Mapping method
    public Object activity2WorkflowTemplate(String rootAid) throws DocumentException {
        // syncGlobalVariables(rootAid);
        loadXML(rootAid);
        if (activityDocXml == null) return "Fail";
        Element activityEle = activityDocXml.getRootElement();
        String type = activityEle.attributeValue("type");

        Document workflowTemplate = DocumentHelper.createDocument();
        Element workflowEle = workflowTemplate.addElement("workflow");
        Element resourceCollectionEle = workflowEle.addElement("ResourceCollection");
        Element participantsEle = workflowEle.addElement("Participants");
        Element toolBoxEle = workflowEle.addElement("ToolBox");

        if (type.equals(ActivityType.Activity_Unit)) {
            List<Node> geoAnalysisNodes = activityEle.selectNodes("/OperationRecords//Operation[@type = geo-analysis]");
            for (int i = 0; i < geoAnalysisNodes.size(); i++) {
                Element gaNode = (Element) geoAnalysisNodes.get(i);
                String toolId = gaNode.attributeValue("toolRef");
                Element toolEle = (Element) activityEle.selectSingleNode("/ToolBox/Tool[@id = toolId]");
                toolBoxEle.add(toolEle);
                List<Node> modelRelationNode = gaNode.selectNodes("/ResRef[@tyep != param]");
                for (int j = 0; j < modelRelationNode.size(); j++) {
                    Element inoutNode = (Element) modelRelationNode.get(i);
                    String resId = inoutNode.attributeValue("idRef");
                    Element resEle = (Element) activityEle.selectSingleNode("/ResourceCollection/Resource[@id = 'resId']");
                    resourceCollectionEle.add(resEle);
                }
                List<Node> personNodes = gaNode.selectNodes("/PersonRef");
                for (int k = 0; k < personNodes.size(); k++) {
                    Element modelPersonEle = (Element) personNodes.get(i);
                    String personId = modelPersonEle.attributeValue("idRef");
                    Element personEle = (Element) activityEle.selectSingleNode("/Participants/Person[@id = 'personId']");
                    participantsEle.add(personEle);
                }
            }
        }
        if (type.equals(ActivityType.Activity_Group)) {
            List<Node> childNodes = activityEle.selectNodes("/ChildActivities/Child");
        }
        return null;
    }

    // private Element a2WorkflowTemplate(List<Node> childActivitiesNode, Document rootActivity) throws DocumentException {
    //     for (int i = 0; i < childActivitiesNode.size(); i++) {
    //         Element childEle = (Element)childActivitiesNode.get(0);
    //         String aid = childEle.attributeValue("id");
    //         Optional<ActivityDoc> byId = docRepository.findById(aid);
    //         if (!byId.isPresent()) continue;
    //         ActivityDoc activityDoc = byId.get();
    //         Document docXML = DocumentHelper.parseText(activityDoc.getDocument());
    //         Element activityEle = docXML.getRootElement();
    //         String type = activityEle.attributeValue("type");
    //
    //
    //         if (type.equals(ActivityType.Activity_Unit)){
    //             List<Node> geoAnalysisNodes = activityEle.selectNodes("/OperationRecords//Operation[@type = 'geo-analysis']");
    //             for (int i = 0; i < geoAnalysisNodes.size(); i++){
    //                 Element gaNode = (Element)geoAnalysisNodes.get(i);
    //                 String toolId = gaNode.attributeValue("toolRef");
    //                 Element toolEle =  (Element)activityEle.selectSingleNode("/ToolBox/Tool[@id = 'toolId']");
    //                 toolBoxEle.add(toolEle);
    //                 List<Node> modelRelationNode = gaNode.selectNodes("/ResRef[@tyep != 'param']");
    //                 for (int j = 0; j < modelRelationNode.size(); j++){
    //                     Element inoutNode =  (Element)modelRelationNode.get(i);
    //                     String resId = inoutNode.attributeValue("idRef");
    //                     Element resEle =  (Element)activityEle.selectSingleNode("/ResourceCollection/Resource[@id = 'resId']");
    //                     resourceCollectionEle.add(resEle);
    //                 }
    //                 List<Node> personNodes = gaNode.selectNodes("/PersonRef");
    //                 for (int k = 0; k < personNodes.size(); k++){
    //                     Element modelPersonEle =  (Element)personNodes.get(i);
    //                     String personId = modelPersonEle.attributeValue("idRef");
    //                     Element personEle =  (Element)activityEle.selectSingleNode("/Participants/Person[@id = 'personId']");
    //                     participantsEle.add(personEle);
    //                 }
    //             }
    //         }
    //         if (type.equals(ActivityType.Activity_Group)){
    //             List<Node> childNodes = activityEle.selectNodes("/ChildActivities/Child");
    //             a2WorkflowTemplate(childNodes, rootActivity);
    //         }
    //     }
    // }

    // @Override
    // public Object geoAnalysis(String aid, String toolId, ArrayList<ResourceEntity> inRes, ArrayList<String> inParams, ArrayList<ResourceEntity> outRes) {
    //     syncGlobalVariables(aid);
    //     if (operatingDoc == null) return null;
    //     Element activityEle = activityDocXml.getRootElement();
    //     Element operationREle = (Element) activityEle.selectSingleNode("/OperationRecords");
    //     operationREle.addElement("Operation");
    //     operationREle.addAttribute("id", UUID.randomUUID().toString());
    //     operationREle.addAttribute("type", "geo-analysis");
    //     operationREle.addAttribute("toolRef", toolId);
    //     operationREle.addAttribute("toolRef", toolId);
    //
    //     return null;
    // }


    @Override
    public Object geoAnalysis(String aid, ArrayList<ResourceEntity> inRes, ArrayList<String> inParams, ArrayList<ResourceEntity> outRes) {
        return null;
    }

    @Override
    public Object setGeoAnalysisOutPuts(String aid, String oid, ArrayList<String> output) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element activityEle = activityDocXml.getRootElement();
        Element operationEle = (Element) activityEle.selectSingleNode("/OperationRecords/Operation[@id = 'oid']");
        for (String item : output) {
            Element resRefEle = operationEle.addElement("ResRef");
            resRefEle.addAttribute("type", "output");
            resRefEle.addAttribute("idRef", item);
        }
        saveXML();
        return null;
    }

    @Override
    public String geoAnalysisNoInput(String aid, String toolId,
                                     HashSet<String> participantsId,
                                     String purpose, ResourceEntity output) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;

        //存入 output
        Element resCollectionEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection");
        Element resourceEle = resCollectionEle.addElement("Resource");
        resourceEle.addAttribute("id", output.getUid());
        resourceEle.addAttribute("name", output.getName());
        resourceEle.addAttribute("type", output.getType());
        resourceEle.addAttribute("provider", output.getUploaderId());
        resourceEle.addAttribute("href", output.getAddress());
        resourceEle.addAttribute("state", "accessible");
        Element formatEle = resourceEle.addElement("Metadata");
        formatEle.addAttribute("type", "format");
        formatEle.addAttribute("description", output.getSuffix().substring(1));


        //添加  operation
        Element operationRecordEle = (Element) activityDocXml.selectSingleNode("/Activity/OperationRecords");
        Element operationEle = operationRecordEle.addElement("Operation");
        String oid = UUID.randomUUID().toString();
        operationEle.addAttribute("id", oid);
        operationEle.addAttribute("type", "geo-analysis");
        operationEle.addAttribute("toolRef", toolId);
        operationEle.addAttribute("purpose", purpose);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", simpleDateFormat.format(new Date()));

        Element outputRefEle = operationEle.addElement("ResRef");
        outputRefEle.addAttribute("type", "output");
        outputRefEle.addAttribute("idRef", output.getUid());

        for (String userId : participantsId) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("type", "participant");
            personRefEle.addAttribute("idRef", userId);
        }
        saveXML();
        return oid;
    }

    @Override
    public String geoAnalysis(String aid, String toolId,
                              HashSet<String> inResId,
                              ArrayList<ResourceEntity> outRes,
                              HashSet<String> participants) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element operationRecordEle = (Element) activityDocXml.selectSingleNode("/Activity/OperationRecords");
        Element resourceCollectionEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection");
        //添加操作
        Element operationEle = operationRecordEle.addElement("Operation");
        String oid = UUID.randomUUID().toString();
        operationEle.addAttribute("id", oid);
        operationEle.addAttribute("type", "geo-analysis");
        operationEle.addAttribute("toolRef", toolId);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", simpleDateFormat.format(new Date()));
        //获取工具 purpose
        String purpose = "";
        try {
            Element toolEle = (Element) activityDocXml.selectSingleNode("/Activity/ToolBox/Tool[@id = '" + toolId + "']");
            purpose = toolEle.attributeValue("function");
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("Read XML: No such tool(" + toolId + ") in the doc.");
        }
        operationEle.addAttribute("purpose", purpose);

        //将输入绑定到operation 上
        if (inResId != null && !inResId.isEmpty()) {
            for (String resId : inResId) {
                Element resRefEle = operationEle.addElement("ResRef");
                resRefEle.addAttribute("type", "input");
                resRefEle.addAttribute("idRef", resId);
            }
        }

        for (String userId : participants) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("type", "participant");
            personRefEle.addAttribute("idRef", userId);
        }

        //不做输入数据存在与否的讨论
        for (ResourceEntity res : outRes) {
            Element resourceEle = resourceCollectionEle.addElement("Resource");
            resourceEle.addAttribute("id", res.getUid());
            resourceEle.addAttribute("name", res.getName());
            resourceEle.addAttribute("type", res.getType());
            resourceEle.addAttribute("provider", "");
            resourceEle.addAttribute("href", res.getAddress());
            resourceEle.addAttribute("state", "accessible");
            Element formatEle = resourceEle.addElement("Metadata");
            formatEle.addAttribute("type", "format");
            formatEle.addAttribute("description", res.getSuffix().substring(1));

            Element outResRefEle = operationEle.addElement("ResRef");
            outResRefEle.addAttribute("type", "output");
            outResRefEle.addAttribute("idRef", res.getUid());
        }
        saveXML();
        return oid;
    }


    @Override
    public String storeMessageRecord(String toolId, MsgRecords msgRecords) {
        // syncGlobalVariables(msgRecords.getAid());
        loadXML(msgRecords.getAid());

        if (operatingDoc == null) return null;
        Element oRecordEle = (Element) activityDocXml.selectSingleNode("/Activity/OperationRecords");
        Element operationEle = oRecordEle.addElement("Operation");
        String oid = UUID.randomUUID().toString();
        operationEle.addAttribute("id", oid);
        operationEle.addAttribute("type", "communication");
        operationEle.addAttribute("toolRef", "");
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("resRef", msgRecords.getRecordId());
        operationEle.addAttribute("time", simpleDateFormat.format(msgRecords.getCreatedTime()));

        //参与者
        ArrayList<String> uids = msgRecords.getParticipants();
        for (String uid : uids) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("idRef", uid);
        }
        saveXML();
        return oid;
    }

    @Override
    public String geoAnalysis(String aid, String toolId,
                              HashSet<String> onlineMemberIds, String purpose,
                              ResourceEntity input, ResourceEntity output) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element operationRecordEle = (Element) activityDocXml.selectSingleNode("/Activity/OperationRecords");

        //存入 output
        Element resCollectionEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection");
        Element outResEle = resCollectionEle.addElement("Resource");
        outResEle.addAttribute("id", output.getUid());
        outResEle.addAttribute("name", output.getName());
        outResEle.addAttribute("type", output.getType());
        outResEle.addAttribute("provider", output.getUploaderId());
        outResEle.addAttribute("href", output.getAddress());
        outResEle.addAttribute("state", "accessible");
        Element formatEle = outResEle.addElement("Metadata");
        formatEle.addAttribute("type", "format");
        formatEle.addAttribute("description", "zip");

        //判断输入是否在文档中, 若无则将其上传
        String inUid = input.getUid();
        Node node = resCollectionEle.selectSingleNode("/Resource[@id = '" + inUid + "']");
        boolean uploadOperationFlag = false;
        if (node == null) {
            uploadOperationFlag = true;
            //将input 存入
            Element inResEle = resCollectionEle.addElement("Resource");
            inResEle.addAttribute("id", input.getUid());
            inResEle.addAttribute("name", input.getName());
            inResEle.addAttribute("type", input.getType());
            inResEle.addAttribute("provider", input.getUploaderId());
            inResEle.addAttribute("href", input.getAddress());
            inResEle.addAttribute("state", "accessible");
            Element inFormatEle = inResEle.addElement("Metadata");
            inFormatEle.addAttribute("type", "format");
            inFormatEle.addAttribute("description", "zip");



            Element operationEle = operationRecordEle.addElement("Operation");
            operationEle.addAttribute("id", UUID.randomUUID().toString());
            operationEle.addAttribute("type", "resource");
            operationEle.addAttribute("behavior", "upload");
            operationEle.addAttribute("resRef", input.getUid());
            operationEle.addAttribute("task", "");
            operationEle.addAttribute("time", simpleDateFormat.format(new Date()));
            saveXML();
        }

        //添加  operation
        Element operationEle = operationRecordEle.addElement("Operation");
        String oid = UUID.randomUUID().toString();
        operationEle.addAttribute("id", oid);
        operationEle.addAttribute("type", "geo-analysis");
        operationEle.addAttribute("toolRef", toolId);
        operationEle.addAttribute("purpose", purpose);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", simpleDateFormat.format(new Date()));

        Element inResRefEle = operationEle.addElement("ResRef");
        inResRefEle.addAttribute("type", "input");
        inResRefEle.addAttribute("idRef", inUid);

        Element outputRefEle = operationEle.addElement("ResRef");
        outputRefEle.addAttribute("type", "output");
        outputRefEle.addAttribute("idRef", output.getUid());

        for (String userId : onlineMemberIds) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("type", "participant");
            personRefEle.addAttribute("idRef", userId);
        }


        saveXML();
        //将新上传资源存入
        if (uploadOperationFlag) nodeService.addResToNode(aid, input.getUid());

        return oid;
    }

    @Override
    public Object setGeoAnalysisOutPut(String aid, String oid, String uid) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element activityEle = activityDocXml.getRootElement();
        Element operationEle = (Element) activityEle.selectSingleNode("/OperationRecords/Operation[@id = 'oid']");
        Element resRefEle = operationEle.addElement("ResRef");
        resRefEle.addAttribute("type", "output");
        resRefEle.addAttribute("idRef", uid);
        saveXML();
        return null;
    }

    //=========================================


    @Override
    public Object resFlow(String aid, HashMap<String, String> resInfo) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        Element activityEle = activityDocXml.getRootElement();
        Element operationRecordEle = (Element) activityEle.selectSingleNode("/OperationRecords");
        Element operationEle = operationRecordEle.addElement("Operation");
        operationEle.addAttribute("id", UUID.randomUUID().toString());
        operationEle.addAttribute("type", "resource");
        operationEle.addAttribute("behavior", "flow");
        operationEle.addAttribute("resRef", resInfo.get("uid"));
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", simpleDateFormat.format(new Date()));

        //更新资源
        Element resCollectionEle = (Element) activityEle.selectSingleNode("/ResourceCollection");
        Element resourceEle = resCollectionEle.addElement("Resource");
        resourceEle.addAttribute("id", resInfo.get("uid"));
        resourceEle.addAttribute("name", resInfo.get("name"));
        resourceEle.addAttribute("href", resInfo.get("address"));
        resourceEle.addAttribute("state", "accessible");
        return null;
    }

    // public Object resFlow(String aid, ArrayList<HashMap<String, String>> resInfoList) {
    //     syncGlobalVariables(aid);
    //     if (operatingDoc == null) return null;
    //     Element activityEle = activityDocXml.getRootElement();
    //     Element operationRecordEle = (Element) activityEle.selectSingleNode("/OperationRecords");
    //     Element resCollectionEle = (Element) activityEle.selectSingleNode("/ResourceCollection");
    //
    //     for (HashMap<String, String> resInfo : resInfoList) {
    //         Element operationEle = operationRecordEle.addElement("Operation");
    //         operationEle.addAttribute("id", UUID.randomUUID().toString());
    //         operationEle.addAttribute("type", "resource");
    //         operationEle.addAttribute("behavior", "flow");
    //         operationEle.addAttribute("resRef", resInfo.get("uid"));
    //         operationEle.addAttribute("time", simpleDateFormat.format(new Date()));
    //
    //         Element resourceEle = resCollectionEle.addElement("Resource");
    //         resourceEle.addAttribute("id", resInfo.get("uid"));
    //         resourceEle.addAttribute("name", resInfo.get("name"));
    //         resourceEle.addAttribute("href", resInfo.get("address"));
    //         resourceEle.addAttribute("state", "accessible");
    //     }
    //     return null;
    // }

    /**
     * 资源流动更新文档，流动不算做操作
     * 记录为临时操作，好像也无相关任务用于
     *
     * @param fromId
     * @param endId
     * @param uid
     * @return
     */
    @Override
    public HashMap<String, String> resFlow(String fromId, String endId, String uid) {
        // syncGlobalVariables(formId);
        loadXML(fromId);
        if (operatingDoc == null) return null;
        Element resourceEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection/Resource[@id = '" + uid + "']");
        if (resourceEle == null) return null;
        HashMap<String, String> resInfo = new HashMap<>();
        resInfo.put("uid", uid);
        resInfo.put("type", resourceEle.attributeValue("type"));
        if (resourceEle.attributeValue("type").equals("data")) {
            for (Iterator<Element> it = resourceEle.elementIterator(); it.hasNext(); ) {
                Element metaEle = it.next();
                resInfo.put(metaEle.attributeValue("type"), metaEle.attributeValue("description"));
            }
        }

        Optional<ActivityDoc> byId = docRepository.findById(endId);
        if (!byId.isPresent()) return null;
        ActivityDoc activityDoc = byId.get();
        try {
            Document endActivityDocXml = DocumentHelper.parseText(activityDoc.getDocument());
            Element endResCollectionEle = (Element) endActivityDocXml.selectSingleNode("/Activity/ResourceCollection");
            Element endRes = resourceEle.createCopy();
            endResCollectionEle.add(endRes);

            //add operation
            // Element operationRecordEle = (Element) endActivityDocXml.selectSingleNode("/Activity/OperationRecords");
            // Element operationEle = operationRecordEle.addElement("Operation");
            // operationEle.addAttribute("id", UUID.randomUUID().toString());
            // operationEle.addAttribute("type", "resource");
            // operationEle.addAttribute("behavior", "flow");
            // operationEle.addAttribute("task", "");
            // operationEle.addAttribute("resRef", uid);
            // operationEle.addAttribute("time", simpleDateFormat.format(new Date()));

            activityDoc.setDocument(endActivityDocXml.asXML());
            docRepository.save(activityDoc);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        saveXML();
        return resInfo;
    }

    @Override
    public Object resFlow(String aid, ArrayList<ResourceEntity> resList) {
        // syncGlobalVariables(aid);
        loadXML(aid);
        if (operatingDoc == null) return null;
        if (resList == null || resList.isEmpty()) return null;
        Element resCollectionEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection");
        Element operationEle = (Element) activityDocXml.selectSingleNode("/Actvitiy/OperationRecords");
        for (ResourceEntity res : resList) {
            Element resEle = resCollectionEle.addElement("Resource");
            resEle.addAttribute("id", res.getUid());
            resEle.addAttribute("name", res.getName());
            resEle.addAttribute("type", res.getType());
            resEle.addAttribute("provider", res.getUploaderId());
            resEle.addAttribute("href", res.getAddress());
            resEle.addAttribute("state", "accessible");
        }
        return null;
    }


    @Override
    public void resFlow(String fromId, String endId, HashSet<String> uids) {
        // syncGlobalVariables(fromId);
        loadXML(fromId);
        if (operatingDoc == null) return;
        if (uids == null || uids.isEmpty()) return;
        Optional<ActivityDoc> byId = docRepository.findById(endId);
        if (!byId.isPresent()) return;
        ActivityDoc activityDoc = byId.get();
        try {
            Document endActivityDocXml = DocumentHelper.parseText(activityDoc.getDocument());
            for (String uid : uids) {
                Element resourceEle = (Element) activityDocXml.selectSingleNode("/Activity/ResourceCollection/Resource[@id = '" + uid + "']");
                if (resourceEle == null) return;
                Node endResEle = endActivityDocXml.selectSingleNode("/Activity/ResourceCollection/Resource[@id = '" + uid + "']");
                if (endResEle != null) continue;
                Element endResCollectionEle = (Element) endActivityDocXml.selectSingleNode("/Activity/ResourceCollection");
                Element endRes = resourceEle.createCopy();
                endResCollectionEle.add(endRes);

                //operation
                // Element operationRecordEle = (Element) endActivityDocXml.selectSingleNode("/Activity/OperationRecords");
                // Element operationEle = operationRecordEle.addElement("Operation");
                // operationEle.addAttribute("id", UUID.randomUUID().toString());
                // operationEle.addAttribute("type", "resource");
                // operationEle.addAttribute("behavior", "flow");
                // operationEle.addAttribute("resRef", uid);
                // operationEle.addAttribute("operator", "");
                // operationEle.addAttribute("task", "");
                // operationEle.addAttribute("time", simpleDateFormat.format(new Date()));
            }
            activityDoc.setDocument(endActivityDocXml.asXML());
            docRepository.save(activityDoc);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public Object userJoin(String aid, String userId) {
        try {
            //有个特殊情况，退了又加
            UserEntity user = userDao.findUserByIdOrEmail(userId);
            ArrayList<String> domains = user.getDomain();
            ArrayList<String> organizations = user.getOrganizations();
            // syncGlobalVariables(aid);
            loadXML(aid);
            if (operatingDoc == null) return null;
            Element participantsEle = (Element) activityDocXml.selectSingleNode("/Activity/Participants");
            Element personEle = participantsEle.addElement("Person");
            personEle.addAttribute("id", userId);
            personEle.addAttribute("email", user.getEmail());
            personEle.addAttribute("name", user.getName());
            personEle.addAttribute("role", "ordinary-member");
            personEle.addAttribute("state", "in");
            if (domains != null && !domains.isEmpty()) {
                for (Object item : domains) {
                    Element domainEle = personEle.addElement("Domain");
                    domainEle.addAttribute("description", (String) item);
                }
            }

            if (organizations != null && !organizations.isEmpty()) {
                for (Object item : organizations) {
                    Element OrganizationEle = personEle.addElement("Organization");
                    OrganizationEle.addAttribute("description", (String) item);
                }
            }
            saveXML();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object userJoin(String aid, HashSet<String> userIds) {
        // syncGlobalVariables(aid);
        loadXML(aid);

        if (operatingDoc == null) return null;
        if (userIds == null || userIds.isEmpty()) return null;
        // JSONObject usersTag = userDispatch.getUsersTag(userIds);
        Element participantsEle = (Element) activityDocXml.selectSingleNode("/Activity/Participants");
        for (String userId : userIds) {
            UserEntity user = userDao.findUserByIdOrEmail(userId);
            Element personEle = participantsEle.addElement("Person");
            personEle.addAttribute("id", userId);
            personEle.addAttribute("email", user.getEmail());
            personEle.addAttribute("name", user.getName());
            personEle.addAttribute("role", "ordinary-member");
            personEle.addAttribute("state", "in");

            ArrayList<String> domains = user.getDomain();
            ArrayList<String> organizations = user.getOrganizations();

            if (domains != null && !domains.isEmpty()) {
                for (Object item : domains) {
                    Element domainEle = personEle.addElement("Domain");
                    domainEle.addAttribute("description", (String) item);
                }
            }

            if (organizations != null && !organizations.isEmpty()) {
                for (Object item : organizations) {
                    Element OrganizationEle = personEle.addElement("Organization");
                    OrganizationEle.addAttribute("description", (String) item);
                }
            }
        }
        saveXML();
        return null;
    }

    @Override
    public Object userJoin(String fromId, String endId, HashSet<String> userIds) {
        return null;
    }

    @Override
    public Object extractWorkFlowTemplate(String aid) {
        syncGlobalVariables(aid);
        if (operatingDoc == null) return null;
        Element activityEle = activityDocXml.getRootElement();
        String activityType = activityEle.attributeValue("type");
        return null;
    }


    private Element initActivityContent(Element activityEle, Activity activity) {
        Element participantsEle = activityEle.addElement("Participants");
        JSONObject memberJson = JSONObject.parseObject(JSONObject.toJSONString(activity.getMembers().get(0)));
        Element personEle = participantsEle.addElement("Person");
        String userId = memberJson.getString("userId");
        personEle.addAttribute("id", userId);
        personEle.addAttribute("role", memberJson.getString("role"));
        personEle.addAttribute("state", "in");
        //增加用户领域等信息
        UserEntity user = userDao.findUserByIdOrEmail(userId);
        if (user != null) {
            personEle.addAttribute("email", user.getEmail());
            personEle.addAttribute("name", user.getName());
            ArrayList<String> domains = user.getDomain();
            if (domains != null && domains.size() != 0) {
                for (String domain : domains) {
                    Element domainEle = personEle.addElement("Domain");
                    domainEle.addAttribute("description", domain);
                }
            }
            ArrayList<String> organizations = user.getOrganizations();
            if (organizations != null && organizations.size() != 0) {
                for (String organization : organizations) {
                    Element domainEle = personEle.addElement("Organization");
                    domainEle.addAttribute("description", organization);
                }
            }
        }

        activityEle.addElement("ResourceCollection");
        activityEle.addElement("OperationRecords");


        String activityType = activity.getType().toString();
        if (activityType.equals("Activity_Unit")) {
            activityEle.addElement("ToolBox");
            activityEle.addElement("TaskList");
            activityEle.addElement("TaskDependency");

        } else if (activityType.equals("Activity_Group")) {
            activityEle.addElement("ChildActivities");
            activityEle.addElement("ActivityDependencies");
        }

        return activityEle;
    }
}

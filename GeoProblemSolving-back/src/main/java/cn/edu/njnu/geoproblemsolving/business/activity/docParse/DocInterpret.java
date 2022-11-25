package cn.edu.njnu.geoproblemsolving.business.activity.docParse;


import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.DocPerson;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import com.google.common.collect.Lists;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工具层
 * 将文档解析的相关内容全部拆分为原子操作
 * 在服务层通过组装这些原子操作来满足业务逻辑需求
 * 以此对外实现独立，只读
 * 这个类包含了操作活动文档的众多方法
 * <p>
 * 基于活动文档开发的工具类，活动文档作为底
 * 如果没有活动文档的底，基于 xml 来开发最后开发出来的东西就应该是 dom4j
 * <p>
 * 如 Collections 工具类，基于 Collection 来进行开发的
 * 尽量做到独立于平台之外，与平台其他内容松耦合
 * <p>
 * 尽量使用原生的内容
 * <p>
 * 基于参与式平台的操作才有此文档，是否脱离了参与式平台该文档就没有作用了
 * 如何能使该此文档的内容能说得脱离参与式平台？
 * 文档只是工具，如果用这个文档?
 * <p>
 * <p>
 * <p>
 * 有点像是为了解决问题而提出问题
 */
public class DocInterpret {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocInterpret.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //采用单例模式
    // private DocInterpret() {}
    // private volatile static DocInterpret docInterpret;
    //
    // public static DocInterpret getInstance(){
    //     if (docInterpret == null){
    //         synchronized (DocInterpret.class){
    //             if (docInterpret == null){
    //                 docInterpret = new DocInterpret();
    //             }
    //         }
    //     }
    //     return docInterpret;
    // }

    /**
     * 初始化活动文档
     *
     * @param activity 项目、子项目、活动对象
     * @return 活动文档的 xml
     */
    public static String initActivityDoc(Activity activity, DocPerson creator) {
        Document document = DocumentHelper.createDocument();
        String activityType = activity.getType().toString();

        //add rootElement
        Element rootEle = document.addElement("Activity");
        rootEle.addAttribute("id", activity.getAid());
        rootEle.addAttribute("name", activity.getName());
        rootEle.addAttribute("type", activityType);
        rootEle.addAttribute("description", activity.getDescription());

        if (creator == null) {
            LOGGER.info("Activity creator is null.");
            return null;
        }
        HashSet<DocPerson> members = new HashSet<>();
        members.add(creator);
        initActivityDocContent(rootEle, activity, members);
        LOGGER.info("Create activity xml document successfully.");
        return document.asXML();
    }

    /**
     * 更改活动类型
     *
     * @param xmlDoc
     * @param activity
     * @return
     */
    public static String changeActivityType(Document xmlDoc, Activity activity, HashSet<DocPerson> members) {
        Element rootEle = xmlDoc.getRootElement();
        rootEle.clearContent();
        rootEle.attribute("type").setValue(activity.getType().toString());

        initActivityDocContent(rootEle, activity, members);

        return rootEle.asXML();
    }

    /**
     * 初始化活动文档内容
     *
     * @param rootEle
     * @param activity
     * @param members
     */
    public static void initActivityDocContent(Element rootEle, Activity activity, HashSet<DocPerson> members) {
        //add general element
        Element participantsEle = rootEle.addElement("Participants");

        try {
            for (DocPerson member : members) {
                Element personEle = createPersonNode(member);
                participantsEle.add(personEle);
            }

            rootEle.addElement("ResourceCollection");
            rootEle.addElement("OperationRecords");

            String activityType = activity.getType().toString();
            if (activityType.equals("Activity_Unit")) {
                rootEle.addElement("ToolBox");
                rootEle.addElement("TaskList");
                rootEle.addElement("TaskDependency");

            } else if (activityType.equals("Activity_Group")) {
                rootEle.addElement("ChildActivities");
                rootEle.addElement("ActivityDependencies");
            }
        } catch (NullPointerException e) {
            LOGGER.error("Failed to init activity document content: Member is null, id " + activity.getAid());
            e.printStackTrace();
        }


    }

    //=================Generic operation==============================================================
    public static HashMap<String, String> getRootInfo(Document docXml) {
        Element rootEle = docXml.getRootElement();
        HashMap<String, String> rootEleInfoMap = new HashMap<>();
        for (Iterator<Attribute> attributeIt = rootEle.attributeIterator(); attributeIt.hasNext(); ) {
            Attribute attribute = attributeIt.next();
            rootEleInfoMap.put(attribute.getName(), attribute.getValue());
        }
        return rootEleInfoMap;
    }

    /*
    Person operation
     */
    public static void appendParticipant(Document docXml, DocPerson member) {
        Element participantEle = (Element) docXml.selectSingleNode("/Activity/Participants");
        if (participantEle == null) {
            participantEle = docXml.getRootElement().addElement("Participants");
        }
        Element oldPersonEle = (Element) participantEle.selectSingleNode("./Person[@id = '" + member.getUserId() + "']");
        if (oldPersonEle != null) participantEle.remove(oldPersonEle);
        Element personEle = createPersonNode(member);
        participantEle.add(personEle);
    }

    public static void appendParticipant(Document docXml, HashSet<DocPerson> members) {
        Element participantEle = (Element) docXml.selectSingleNode("/Activity/Participants");
        if (participantEle == null) {
            participantEle = docXml.getRootElement().addElement("Participants");
        }
        if (members != null && !members.isEmpty()) {
            for (DocPerson item : members) {
                Element oldPersonEle = (Element) participantEle.selectSingleNode("./Person[@id = '" + item.getUserId() + "']");
                if (oldPersonEle != null) oldPersonEle.getParent().remove(oldPersonEle);
                Element personEle = createPersonNode(item);
                participantEle.add(personEle);
            }
        }
    }


    public static Element removeParticipant(Document docXml, String userId) {
        Element participantEle = (Element) docXml.selectSingleNode("/Activity/Participants");
        if (participantEle == null) {
            participantEle = docXml.getRootElement().addElement("Participants");
        }
        Element removePersonEle = (Element) participantEle.selectSingleNode("./Person[@id = '" + userId + "']");
        if (removePersonEle == null) {
            LOGGER.warn("Failed to remove participant: No such participant, id " + userId);
        } else {
            removePersonEle.attribute("state").setValue("out");
        }
        return participantEle;
    }

    public static Element removeParticipants(Document docXml, HashSet<String> userIds) {
        Element participantEle = (Element) docXml.selectSingleNode("/Activity/Participants");
        if (participantEle == null) {
            participantEle = docXml.getRootElement().addElement("Participants");
        }
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                Element removePersonEle = (Element) participantEle.selectSingleNode("//Person[@id = '" + userId + "']");
                if (removePersonEle == null) {
                    LOGGER.warn("Failed to remove participant: No such participant, id " + userId);
                } else {
                    removePersonEle.attribute("state").setValue("out");
                }
            }
        }

        return participantEle;
    }

    //todo
    public static Element putParticipants(Element participantEle, DocPerson docPerson) {
        return participantEle;
    }

    /*
    Resource operation
     */
    public static HashMap<String, String> appendResource(Document docXml,
                                                         ResourceEntity res,
                                                         HashMap<String, String> meta,
                                                         String behaviorType) {
        checkNode(docXml);
        Element resourceCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        Element operationRecordsEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        HashMap<String, String> uploadOperation = resourceBehavior(resourceCollectionEle, operationRecordsEle, meta, res, behaviorType);

        LOGGER.info("Successfully append resource in activity document");
        return uploadOperation;
    }

    public static ArrayList<HashMap<String, String>> appendResource(Document docXml,
                                                                    ArrayList<ResourceEntity> uploadList,
                                                                    HashMap<String, String> meta,
                                                                    String behaviorType) {
        checkNode(docXml);
        Element resourceCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        Element operationRecordsEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        ArrayList<HashMap<String, String>> uploadOperationList = new ArrayList<>();
        if (uploadList != null && !uploadList.isEmpty()) {
            for (ResourceEntity item : uploadList) {
                HashMap<String, String> uploadOperation = resourceBehavior(resourceCollectionEle, operationRecordsEle, meta, item, behaviorType);
                uploadOperationList.add(uploadOperation);
            }
        }
        return uploadOperationList;
    }

    /**
     * 用于支持将分享的资源添加到文档中
     *
     * @param docXml
     * @param resList
     * @param behaviorType
     * @return
     */
    public static ArrayList<HashMap<String, String>> appendResource(Document docXml, ArrayList<ResourceEntity> resList, String behaviorType) {
        checkNode(docXml);
        Element resourceCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        Element operationRecordsEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        ArrayList<HashMap<String, String>> uploadOperationList = new ArrayList<>();
        if (resList != null && !resList.isEmpty()) {
            for (ResourceEntity item : resList) {
                HashMap<String, String> meta = new HashMap<>();
                String type = item.getType();
                HashMap<String, String> uploadOperation = resourceBehavior(resourceCollectionEle, operationRecordsEle, null, item, behaviorType);
                uploadOperationList.add(uploadOperation);
            }
        }

        return uploadOperationList;
    }

    public static void appendResourceNoOperation(Document docXml, ArrayList<ResourceEntity> resList) {
        checkNode(docXml);
        Element resourceCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        if (resList != null && !resList.isEmpty()) {
            for (ResourceEntity item : resList) {
                Element resEle = createResourceNode(item, null);
                resourceCollectionEle.add(resEle);
            }
        }
    }

    public static void appendResource(Document docXml, Element resEle) {
        Element resCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        //去重
        Element oldResEle = (Element) resCollectionEle.selectSingleNode("./Resource[@id = '" + resEle.attributeValue("id") + "']");
        if (oldResEle != null) oldResEle.getParent().remove(oldResEle);
        resCollectionEle.add(resEle);
    }

    public static void appendResource(Document docXml, List<Element> resEleList) {
        if (resEleList == null || resEleList.isEmpty()) return;
        Element resCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        for (int i = 0; i < resEleList.size(); i++) {
            Element resEle = resEleList.get(i);
            Element oldResEle = (Element) resCollectionEle.selectSingleNode("./Resource[@id = '" + resEle.attributeValue("id") + "']");
            if (oldResEle != null) oldResEle.getParent().remove(oldResEle);
            resCollectionEle.add(resEle);
        }
    }


    public static void removeResource(Document docXml, String uid) {
        checkNode(docXml);
        Element resEle = (Element) docXml.selectSingleNode("/Activity//ResourceCollection/Resource[@id = '" + uid + "']");
        if (resEle != null) resEle.attribute("state").setValue("removed");
    }

    public static void removeResources(Document docXml, HashSet<String> uids) {
        checkNode(docXml);
        Element resCollEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        if (uids == null || uids.size() == 0) return;
        for (Iterator<String> it = uids.iterator(); it.hasNext(); ) {
            String uid = it.next();
            Element resEle = (Element) resCollEle.selectSingleNode("./Resource[@id = '" + uid + "']");
            if (resEle != null) resEle.attribute("state").setValue("removed");
        }
    }

    //检查文档中资源及操作的标签是否存在，不存在则添加
    private static void checkNode(Document docXml) {
        Element resourceCollectionEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection");
        if (resourceCollectionEle == null) {
            String aid = docXml.getRootElement().attributeValue("id");
            LOGGER.warn("No resource collection element, activity id " + aid);
            docXml.getRootElement().addElement("ResourceCollection");
        }
        Element operationRecordsEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        if (operationRecordsEle == null) {
            String aid = docXml.getRootElement().attributeValue("id");
            LOGGER.warn("No operation collection element, activity id " + aid);
            docXml.getRootElement().addElement("OperationRecords");
        }
    }

    public static Element getResourceNodeById(Document docXml, String uid) {
        return (Element) docXml.selectSingleNode("/Activity//ResourceCollection/Resource[@id = '" + uid + "']");
    }

    public static HashMap<String, String> getResInfo(Document docXml, String uid) {
        Element resourceEle = (Element) docXml.selectSingleNode("/Activity/ResourceCollection/Resource[@id = '" + uid + "']");
        if (resourceEle == null) {
            LOGGER.warn("Failed to required resource info: No such resource, resource id is " + uid);
            return null;
        }
        return resNode2Map(resourceEle);
    }

    public static Element getResElement(Document docXml, String uid) {
        return (Element) docXml.selectSingleNode("/Activity/ResourceCollection/Resource[@id = '" + uid + "']");
    }

    public static List<Element> getResElement(Document docXml, HashSet<String> uids) {
        if (uids == null || uids.isEmpty()) return null;
        List<Element> resElementList = Lists.newArrayList();
        for (Iterator<String> it = uids.iterator(); it.hasNext(); ) {
            String uid = it.next();
            Element resElement = getResElement(docXml, uid);
            if (resElement != null) resElementList.add(resElement);
        }
        return resElementList;
    }

    public static ArrayList<HashMap<String, String>> getResAllResInfo(Document docXml) {
        ArrayList<HashMap<String, String>> resInfoList = new ArrayList<>();
        List<Node> resNodes = docXml.selectNodes("/Activity/ResourceCollection/Resource[@state = 'accessible']");
        if (resNodes == null || resNodes.isEmpty()) return resInfoList;
        for (Iterator<Node> it = resNodes.iterator(); it.hasNext(); ) {
            Element resEle = (Element) it.next();
            HashMap<String, String> resInfo = resNode2Map(resEle);
            resInfoList.add(resInfo);
        }
        return resInfoList;

    }

    public static HashMap<String, String> resNode2Map(Element resourceEle) {
        HashMap<String, String> resInfoMap = new HashMap<>();
        String uid = resourceEle.attributeValue("id");
        String type = resourceEle.attributeValue("type");
        resInfoMap.put("uid", uid);
        resInfoMap.put("type", type);
        //元数据写入
        if (type.equals("data")) {
            for (Iterator<Element> mIt = resourceEle.elementIterator("Metadata"); mIt.hasNext(); ) {
                Element metaEle = mIt.next();
                resInfoMap.put(metaEle.attributeValue("type"), metaEle.attributeValue("description"));
            }
        }

        return resInfoMap;
    }

    private static HashMap<String, String> resourceBehavior(Element resourceCollectionEle,
                                                            Element operationRecordsEle,
                                                            HashMap<String, String> meta,
                                                            ResourceEntity item, String behaviorType) {
        HashMap<String, String> uploadOperation = new HashMap<>();
        Element resourceEle = createResourceNode(item, meta);
        resourceCollectionEle.add(resourceEle);
        String oid = null;
        if (!behaviorType.equals("flow") && !behaviorType.equals("generate")) {
            //此两种类似无需生成操作
            Element operationEle = createResourceOperation(item.getUid(), item.getUploaderId(), behaviorType);
            operationRecordsEle.add(operationEle);
            oid = operationEle.attributeValue("id");
        }
        uploadOperation.put("oid", oid);
        uploadOperation.put("resRef", item.getUid());
        return uploadOperation;
    }


    /*
    Tool
     */
    public static HashMap<String, String> getToolInfo(Document docXml, String tid) {
        Element element = (Element) docXml.selectSingleNode("/Activity/ToolBox/Tool[@id = '" + tid + "']");
        if (element != null) {
            HashMap<String, String> toolInfo = new HashMap<>();
            for (Iterator<Attribute> it = element.attributeIterator(); it.hasNext(); ) {
                Attribute attribute = it.next();
                toolInfo.put(attribute.getName(), attribute.getStringValue());
            }
            return toolInfo;
        }
        return null;
    }

    public static void appendTool(Document docXml, Tool tool) {
        String aid = docXml.getRootElement().attributeValue("id");
        Element toolBoxEle = (Element) docXml.selectSingleNode("/Activity/ToolBox");
        if (toolBoxEle == null) {
            LOGGER.warn("Failed to append tool to toolbox: Activity document no such toolbox, id " + aid);
            return;
        }
        if (tool == null) return;
        Element oldToolEle = (Element) toolBoxEle.selectSingleNode("//Tool[@id = '" + tool.getTid() + "']");
        if (oldToolEle != null) oldToolEle.getParent().remove(oldToolEle);
        Element toolEle = createToolElement(tool);
        toolBoxEle.add(toolEle);
    }

    public static void appendTool(Document docXml, List<Tool> tools) {
        String aid = docXml.getRootElement().attributeValue("id");
        Element toolBoxEle = (Element) docXml.selectSingleNode("/Activity/ToolBox");
        if (toolBoxEle == null) {
            LOGGER.warn("Failed to append tool to toolbox: Activity document no such toolbox, id " + aid);
            return;
        }
        if (tools == null || tools.isEmpty()) return;
        for (Tool tool : tools) {
            Element oldToolEle = (Element) toolBoxEle.selectSingleNode("//Tool[@id = '" + tool.getTid() + "']");
            if (oldToolEle != null) oldToolEle.getParent().remove(oldToolEle);
            Element toolEle = createToolElement(tool);
            toolBoxEle.add(toolEle);
        }
    }

    public static void removeTool(Document docXml, String tid) {
        Element toolEle = (Element) docXml.selectSingleNode("/Activity/ToolBox/Tool[@id = '" + tid + "']");
        if (toolEle == null) {
            LOGGER.warn("Failed to remove tool: No such tool, id " + tid);
            return;
        }
        toolEle.attribute("state").setValue("removed");
    }

    public static void emptyTool(Document docXml) {
        Element toolboxEle = (Element) docXml.selectSingleNode("/Activity/ToolBox");
        if (toolboxEle == null) {
            LOGGER.warn("Failed to empty toolbox: No such toolbox.");
            return;
        }
        toolboxEle.clearContent();
    }

    private static Element createToolElement(Tool tool) {
        Element toolEle = DocumentHelper.createElement("Tool");
        toolEle.addAttribute("id", tool.getTid());
        toolEle.addAttribute("name", tool.getToolName());
        toolEle.addAttribute("function", tool.getDescription());
        toolEle.addAttribute("provider", tool.getProvider());
        toolEle.addAttribute("state", "accessible");
        return toolEle;
    }


    /*
    May the operation resRef could be expand.
     */
    public static String appendGeoAnalysisOperation(Document docXml,
                                                    String toolId,
                                                    HashSet<String> onlineMembers,
                                                    String purpose,
                                                    HashSet<String> inResId,
                                                    HashSet<String> outResId) {
        Element operationEle = createGeoAnalysisOperation(toolId, purpose, inResId, null, outResId, onlineMembers);
        Element operationRecordEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        operationRecordEle.add(operationEle);
        return operationEle.attributeValue("id");
    }


    /*
    Operation record
     */
    public static void appendOperation(Document docXml, Element element) {
        checkNode(docXml);
        Element operationRecordEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        operationRecordEle.add(element);
    }

    public static Object getAllOperation(Document docXml) {
        Element operationRecordEle = (Element) docXml.selectSingleNode("/Activity/OperationRecords");
        Element rootEle = docXml.getRootElement();
        String aType = rootEle.attributeValue("type");
        String aid = rootEle.attributeValue("id");
        if (operationRecordEle == null && aType.equals(ActivityType.Activity_Unit.toString())) {
            LOGGER.warn("This document lack of operationRecord node, id: " + aid);
        }
        return operationRecordEle;
    }

    /*
    相对来说底层一些
     */
    public static List<Node> getGeoAnalysisOperation(Document docXml) {
        Element rootEle = docXml.getRootElement();
        String aType = rootEle.attributeValue("type");
        if (!aType.equals(ActivityType.Activity_Unit.toString())) {
            LOGGER.warn("This isn't a signal activity.");
            return null;
        }
        return docXml.selectNodes("/Activity/OperationRecords/Operation[@type = 'geo-analysis']");
    }

    public static List<Node> getResourceOperation(Document docXml, String behavior) {
        return docXml.selectNodes("/Activity/OperationRecords/Operation[@type='resource' and @behavior='" + behavior + "']");
    }

    public static Element getResourceOperationByResId(Document docXml, String uid) {
        return (Element) docXml.selectSingleNode("/Activity/OperationRecords/Operation" +
                "[@type='resource' " +
                "and @behavior='upload'" +
                "and @resRef='" + uid + "']");
    }

    public static Element createResourceOperation(String resRef, String operator, String behavior) {
        Element operationEle = DocumentHelper.createElement("Operation");
        operationEle.addAttribute("id", UUID.randomUUID().toString());
        operationEle.addAttribute("type", "resource");
        operationEle.addAttribute("behavior", behavior);
        operationEle.addAttribute("resRef", resRef);
        operationEle.addAttribute("operator", operator);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", DATE_FORMAT.format(new Date()));
        return operationEle;
    }

    public static Element createToolBoxOperation(String toolRef, String operator, String behavior) {
        Element operationEle = DocumentHelper.createElement("Operation");
        operationEle.addAttribute("id", UUID.randomUUID().toString());
        operationEle.addAttribute("type", "tool");
        operationEle.addAttribute("behavior", behavior);
        operationEle.addAttribute("toolRef", toolRef);
        operationEle.addAttribute("operator", operator);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", DATE_FORMAT.format(new Date()));
        return operationEle;
    }

    public static Element createCommunicationOperation(String toolRef, MsgRecords msgRecords) {
        Element operationEle = DocumentHelper.createElement("Operation");
        operationEle.addAttribute("id", UUID.randomUUID().toString());
        operationEle.addAttribute("type", "communication");
        operationEle.addAttribute("toolRef", toolRef);
        operationEle.addAttribute("resRef", msgRecords.getRecordId());
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("time", DATE_FORMAT.format(new Date()));

        ArrayList<String> uids = msgRecords.getParticipants();
        if (uids == null || uids.isEmpty()) return null;
        for (String uid : uids) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("idRef", uid);
        }
        return operationEle;
    }

    public static Element createGeoAnalysisOperation(String toolRef,
                                                     String purpose,
                                                     HashSet<String> inResId,
                                                     HashSet<String> inParam,
                                                     HashSet<String> outResId,
                                                     HashSet<String> participants) {
        Element operationEle = DocumentHelper.createElement("Operation");
        operationEle.addAttribute("id", UUID.randomUUID().toString());
        operationEle.addAttribute("type", "geo-analysis");
        operationEle.addAttribute("toolRef", toolRef);
        operationEle.addAttribute("task", "");
        operationEle.addAttribute("purpose", purpose);
        operationEle.addAttribute("time", DATE_FORMAT.format(new Date()));

        if (inResId != null && !inResId.isEmpty()) {
            for (String resId : inResId) {
                Element resRefEle = operationEle.addElement("ResRef");
                resRefEle.addAttribute("type", "input");
                resRefEle.addAttribute("idRef", resId);
            }
        }

        if (inParam != null && !inParam.isEmpty()) {
            for (String paramId : inParam) {
                Element resRefEle = operationEle.addElement("ResRef");
                resRefEle.addAttribute("type", "param");
                resRefEle.addAttribute("idRef", paramId);
            }
        }


        if (outResId != null && !outResId.isEmpty()) {
            for (String resId : outResId) {
                Element resRefEle = operationEle.addElement("ResRef");
                resRefEle.addAttribute("type", "output");
                resRefEle.addAttribute("idRef", resId);
            }
        }

        for (String userId : participants) {
            Element personRefEle = operationEle.addElement("PersonRef");
            personRefEle.addAttribute("type", "participant");
            personRefEle.addAttribute("idRef", userId);
        }

        return operationEle;
    }

    /*
    Multi activity
     */
    public static HashSet<HashMap<String, String>> getChildActivities(Document docXml) {
        List<Node> childNodes = docXml.selectNodes("/Activity/ChildActivities/Child[@state = 'accessible']");
        if (childNodes == null) {
            LOGGER.info("This isn't ChildActivities");
            return null;
        }
        HashSet<HashMap<String, String>> childActivityList = new HashSet<>();
        for (Iterator<Node> it = childNodes.iterator(); it.hasNext(); ) {
            Element childEle = (Element) it.next();
            HashMap<String, String> aInfo = new HashMap<>();
            for (Iterator<Attribute> aIt = childEle.attributeIterator(); aIt.hasNext(); ) {
                Attribute attribute = aIt.next();
                aInfo.put(attribute.getName(), attribute.getValue());
            }
            childActivityList.add(aInfo);
        }
        return childActivityList;
    }

    /**
     * 新增子活动
     *
     * @param docXml
     * @param childId
     * @param name
     * @param creatorId
     */
    public static void appendChild(Document docXml, String childId, String name, String creatorId) {
        Element childActivitiesEle = (Element) docXml.selectSingleNode("/Activity/ChildActivities");
        if (childActivitiesEle == null) childActivitiesEle = docXml.getRootElement().addElement("ChildActivities");
        Element oldChildNode = (Element) childActivitiesEle.selectSingleNode("./Child[@id='" + childId + "']");
        if (oldChildNode != null) oldChildNode.getParent().remove(oldChildNode);
        Element childNode = createChildNode(childId, name, creatorId);
        childActivitiesEle.add(childNode);
    }

    public static void updateChild(Document docXml, String childId, String name) {
        Element childEle = (Element) docXml.selectSingleNode("/Activity/ChildActivities/Child[@id='" + childId + "']");
        if (childEle != null) childEle.attribute("name").setValue(name);
    }

    public static void removeChild(Document docXml, String childId) {
        Element childEle = (Element) docXml.selectSingleNode("/Activity/ChildActivities/Child[@id='" + childId + "']");
        childEle.getParent().remove(childEle);
    }

    public static void updateRoot(Document docXml, HashMap<String, String> updateInfo) {
        Element rootEle = docXml.getRootElement();
        for (Map.Entry<String, String> item : updateInfo.entrySet()) {
            Attribute attribute = rootEle.attribute(item.getKey());
            if (attribute != null) attribute.setValue(item.getValue());
        }
    }

    /*
    general
     */
    //crate participant node
    private static Element createPersonNode(DocPerson person) {
        Element personEle = DocumentHelper.createElement("Person");
        personEle.addAttribute("id", person.getUserId());
        personEle.addAttribute("email", person.getEmail());
        personEle.addAttribute("name", person.getName());
        personEle.addAttribute("role", person.getRole());
        personEle.addAttribute("state", "in");
        HashSet<String> domains = person.getDomain();
        if (domains != null && !domains.isEmpty()) {
            for (String domain : domains) {
                Element domainEle = personEle.addElement("Domain");
                domainEle.addAttribute("description", domain);
            }
        }
        HashSet<String> organizations = person.getOrganization();
        if (organizations != null && !organizations.isEmpty()) {
            for (String organization : organizations) {
                Element domainEle = personEle.addElement("Organization");
                domainEle.addAttribute("description", organization);
            }
        }
        return personEle;
    }

    private static Element createResourceNode(ResourceEntity res,
                                              HashMap<String, String> meta) {
        Element resEle = DocumentHelper.createElement("Resource");
        resEle.addAttribute("id", res.getUid());
        resEle.addAttribute("name", res.getName());
        resEle.addAttribute("type", res.getType());
        resEle.addAttribute("provider", res.getUploaderId());
        resEle.addAttribute("href", res.getAddress());
        resEle.addAttribute("state", "accessible");
        if (res.getType().equals("data") && meta != null) {
            for (Map.Entry<String, String> entry : meta.entrySet()) {
                String metaValue = entry.getValue();
                if (metaValue != null && !metaValue.equals("")) {
                    Element metaEle = resEle.addElement("Metadata");
                    metaEle.addAttribute("type", entry.getKey());
                    metaEle.addAttribute("description", metaValue);
                }
            }
        }
        //如果元数据中没有format 则用
        if (res.getType().equals("data")){
            if (resEle.selectSingleNode("./Metadata[@type='format']") == null) {
                Element formatEle = resEle.addElement("Metadata");
                formatEle.addAttribute("type", "format");
                String suffix = res.getSuffix();
                int index = suffix.indexOf(".");
                formatEle.addAttribute("description", suffix.substring(index + 1));
            }
        }
        return resEle;
    }

    private static Element createResourceNode(String paramName, String value) {
        Element resEle = DocumentHelper.createElement("Resource");
        resEle.addAttribute("id", UUID.randomUUID().toString());
        resEle.addAttribute("name", paramName);
        resEle.addAttribute("type", value);
        resEle.addAttribute("state", "accessible");
        return resEle;
    }

    private static Element createChildNode(String id, String name, String creatorId) {
        Element childEle = DocumentHelper.createElement("Child");
        childEle.addAttribute("id", id);
        childEle.addAttribute("name", name);
        childEle.addAttribute("creator", creatorId);
        childEle.addAttribute("state", "accessible");
        return childEle;
    }
}

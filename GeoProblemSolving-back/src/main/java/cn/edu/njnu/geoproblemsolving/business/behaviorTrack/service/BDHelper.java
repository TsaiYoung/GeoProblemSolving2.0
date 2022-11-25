package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.service;

import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.InteractiveBehavior;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.Scene;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.SupportElement;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.LinkedMultiValueMap;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 交互行为文档工具类
 * @ClassName BDHelper
 * @Description 工具类
 * @Author zhngzhng
 * @Date 2022/6/29
 **/
@Slf4j
public class BDHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化交互行为文档
     * @param pid 项目id
     * @return 初始化的交互行为文档
     */
    public static Document createBehaviorDoc(String pid) {
        Document document = DocumentHelper.createDocument();
        Element rootEle = document.addElement("BehaviorCollection");
        rootEle.addAttribute("id", pid);
        return document;
    }

    /**
     * 创建交互行为节点
     * @param aid 活动id
     * @param scene 交互行为情景
     * @param ibType 交互行为类别
     * @return
     */
    public static Element createBehaviorEle(String aid, Scene scene, InteractiveBehavior ibType) {
        Element behaviorEle = DocumentHelper.createElement("Behavior");
        behaviorEle.addAttribute("id", UUID.randomUUID().toString());
        behaviorEle.addAttribute("aid", aid);
        behaviorEle.addAttribute("scene", scene.toString());
        behaviorEle.addAttribute("type", ibType.toString());
        behaviorEle.addAttribute("time", DATE_FORMAT.format(new Date()));
        return behaviorEle;
    }
    /**
     * 根据支撑要素类别生成支撑要素节点
     *
     * @param seType 支撑要素类别
     * @param id     支持要素 id
     * @return
     */
    private static Element createSupEleByType(SupportElement seType, String id) {
        Element element;
        if (seType.equals(SupportElement.Activity)) {
            element = DocumentHelper.createElement("Activity");
        } else if (seType.equals(SupportElement.Person)) {
            element = DocumentHelper.createElement("Person");
        } else if (seType.equals(SupportElement.Resource)) {
            element = DocumentHelper.createElement("Resource");
        } else {
            //Tool
            element = DocumentHelper.createElement("Tool");
        }
        element.addAttribute("id", id);
        return element;
    }



    /**
     * 创建不同类型支撑要素
     * @param supType
     * @param id 支撑要素id，其的唯一标识符
     * @return
     */
    private static Element createSupportElement(SupportElement supType, String id) {
        Element supElement;
        if (supType.equals(SupportElement.Person)) {
            supElement = DocumentHelper.createElement("Person");
        } else if (supType.equals(SupportElement.Resource)) {
            supElement = DocumentHelper.createElement("Resource");
        } else if (supType.equals(SupportElement.Activity)) {
            supElement = DocumentHelper.createElement("Activity");
        }else {
            supElement = DocumentHelper.createElement("Tool");
        }
        return supElement;
    }
    /**
     * 添加交互行为主体（n=1）
     * @param behaviorEle 行为
     * @param supType 支撑要素
     * @param id
     * @return
     */
    public static Element appendSubject(Element behaviorEle, SupportElement supType, String id) {
        Element subjectEle = DocumentHelper.createElement("Subject");
        subjectEle.add(createSupEleByType(supType, id));
        //主体节点添加到交互行为节点中
        behaviorEle.add(subjectEle);
        return subjectEle;
    }

    /**
     * 添加交互行为主体(n>1)
     * @param behaviorEle 交互行为节点
     * @param supportElements 多个支撑要素
     *                        key: 支撑要素 id
     *                        value: 支撑要素类型
     * @return
     */
    public static Element appendSubject(Element behaviorEle, LinkedHashMap<String, SupportElement> supportElements) {
        Element subjectEle = DocumentHelper.createElement("Subject");
        for (Iterator<String> it = supportElements.keySet().iterator(); it.hasNext();) {
            String id = it.next();
            SupportElement supType = supportElements.get(id);
            subjectEle.add(createSupEleByType(supType, id));
        }
        behaviorEle.add(subjectEle);
        return subjectEle;
    }


    /**
     * 添加客体节点(n = 1)
     * 简单客体
     * @param behaviorEle
     * @param id
     * @param supType
     * @param attrs 受影响的属性或状态，用于配置客体节点
     * @return
     */
    public static Element appendObjectSimple(Element behaviorEle, String id, SupportElement supType, ArrayList<String> attrs) {
        Element objectEle = DocumentHelper.createElement("Object");
        Element supEle = createSupEleByType(supType, id);
        for (int i = 0; i < attrs.size(); i++) {
            String attr = attrs.get(i);
            supEle.addElement(attr);
        }
        objectEle.add(supEle);
        behaviorEle.add(objectEle);
        return objectEle;

    }

    /**
     * 添加客体节点(n > 1)
     * 简单客体
     * @param behaviorEle 交互行为节点
     * @param ids 客体节点id
     * @param supTypes 支撑要素类型
     * @param attrsList 各支撑要素受影响部分
     * @return
     */
    public static Element appendObjectSimple(Element behaviorEle, ArrayList<String> ids, ArrayList<SupportElement> supTypes, ArrayList<ArrayList<String>> attrsList) {
        Element objectEle = DocumentHelper.createElement("Object");
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            SupportElement supType = supTypes.get(i);
            ArrayList<String> attrs = attrsList.get(i);
            Element supEle = createSupportElement(supType, id);
            for (String attr : attrs) {
                supEle.addElement(attr);
            }
            objectEle.add(supEle);
        }
        behaviorEle.add(objectEle);
        return objectEle;
    }


    /**
     * 添加客体节点（n=1）
     * 复杂客体：受影响部分为对象
     * @param behaviorEle 交互行为节点
     * @param id 支撑要素 id
     * @param supTypes 支撑要素类型
     * @param eleName 客体第三层名称
     * @param eleValue 客体第三层内容
     * @return
     */
    public static Element appendObjectComplex(Element behaviorEle, String id, SupportElement supTypes, ArrayList<String> eleName, ArrayList<String> eleValue) {
        Element objectEle = DocumentHelper.createElement("Object");
        Element supEle = createSupportElement(supTypes, id);
        for (int i = 0; i < eleName.size(); i++) {
            Element element = supEle.addElement(eleName.get(i));
            element.addText(eleValue.get(i));
        }
        objectEle.add(supEle);
        behaviorEle.add(objectEle);
        return objectEle;
    }

    /**
     * 添加操作节点
     * @param behaviorEle 交互行为节点
     * @param operationType 操作类型
     * @param operationContents
     * Map<String, LinkedMultiValueMap<String, String>> 所影响客体id: 类型: 值
     * String: 客体支撑要素 id; String: 客体受影响部分的类型
     * 同类型被归为一类; List<String>: 受影响部分的值
     * eg: id: {
     *     Text: [xxx,xxx,xxx],
     *     Number: [xx,xxx,xxx],
     *     支撑要素: [id1, id2, id3]
     * }
     * 对应的Object
     * 支撑要素: [Name, Type, Description, Scale]
     * @return 操作节点
     *
     */
    public static Element appendOperation(Element behaviorEle, String operationType, LinkedHashMap<String, LinkedMultiValueMap<String, String>> operationContents) {
        Element operationEle = DocumentHelper.createElement("Operation");
        operationEle.addAttribute("Type", operationType);
        //获取客体对象id
        Set<String> idSet = operationContents.keySet();
        for (Iterator<String> it = idSet.iterator(); it.hasNext();) {
            String id = it.next();
            Element contentEle = operationEle.addElement("Content");
            contentEle.addAttribute("id", id);
            //获取当前客体对象所有受影响的属性或状态的值类型
            //排序：Text(具体值), Number(具体值), 支撑要素(id)
            LinkedMultiValueMap<String, String> info = operationContents.get(id);
            Set<String> contentTypes = info.keySet();
            for (String contentName : contentTypes) {
                //遍历同一类型，所有值将其写入content下
                List<String> contentValueList = info.get(contentName);
                for (String contentValue : contentValueList){
                    contentEle.addElement(contentName).addText(contentValue);
                }
            }
        }
        behaviorEle.add(operationEle);
        return operationEle;
    }

    public static Element appendOperation(Element behaviorEle, String operationType, String contentId, LinkedMultiValueMap<String, String> operationContent) {
        LinkedHashMap<String, LinkedMultiValueMap<String, String>> contentInfo = new LinkedHashMap<>();
        contentInfo.put(contentId, operationContent);
        return appendOperation(behaviorEle, operationType, contentInfo);
    }

}

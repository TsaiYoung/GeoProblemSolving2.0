package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.service;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity.BehaviorDoc;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.InteractiveBehavior;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.Scene;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums.SupportElement;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.repository.BehaviorDocRepository;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @ClassName BehaviorTrackServiceImpl
 * @Description 交互行为追溯的实现
 * 交互行为：主体的
 * @Author zhngzhng
 * @Date 2022/7/13
 **/
@Service
@Slf4j
public class BehaviorTrackServiceImpl implements BehaviorTrackService{
    private final BDLockService bdLockService;

    private final BehaviorDocRepository docRepository;

    private final long LOCK_EXPIRATION = 2000;

    public BehaviorTrackServiceImpl(BDLockService bdLockService, BehaviorDocRepository docRepository) {
        this.bdLockService = bdLockService;
        this.docRepository = docRepository;
    }

    /**
     * 尝试获取锁
     * @param bid
     * @return
     */
    @Override
    public BehaviorDoc tryAcquireLock(String bid) {
        while (true) {
            BehaviorDoc behaviorDoc = bdLockService.acquire(bid, LOCK_EXPIRATION);
            //拿到锁，可对文档进行操作
            if (behaviorDoc != null) return behaviorDoc;
            try {
                //等待固定时间间隔后再次尝试获取
                Thread.sleep(LOCK_EXPIRATION);
            } catch (InterruptedException e) {
                log.error("Acquire lock failed: {}", e.toString());
            }
        }
    }

    /**
     * 释放锁的同时将更新好的文档存入
     * @param bid
     * @param doc
     */
    @Override
    public void tryReleaseLock(String bid, String doc) {
        bdLockService.release(bid, doc);
    }

    @Override
    public void tryRefreshLock(String bid, long expirationTime) {
        bdLockService.refresh(bid, expirationTime);
    }


    /**
     * str ---> xml
     * @param xmlStr
     * @return
     */
    private Document parseText(String xmlStr) {
        try {
            return DocumentHelper.parseText(xmlStr);
        }catch (DocumentException e) {
            log.warn("The document could not be parsed.");
            return null;
        }
    }


    /**
     * 初始化交互行为文档
     * 在活动创建时初始化
     * 包括一个活动创建行为
     * 协作式而不是协同，包含小范围的协同
     * @param pid 项目id
     * @param userId 创建者 id
     * @param project
     */
    @Override
    public void initBehaviorDoc(String pid, String userId, Project project) {
        //初始化文档
        // Document behaviorDocXml = BDHelper.createBehaviorDoc(pid);
        // //生成创建活动的交互行为
        // //1. 创建交互行为节点、主体节点、客体节点、操作节点
        // //2. 将主体、客体、操作放入交互节点，并将其放入
        // Element behaviorEle = BDHelper.createBehaviorEle(pid, Scene.activityStructure, InteractiveBehavior.createActivity);
        // BDHelper.appendSubject(behaviorEle, SupportElement.person, userId);
        // ArrayList<String> attrs = new ArrayList<>(Arrays.asList("Name","Category", "Description", "Privacy"));
        // ArrayList<String> attrsValue = new ArrayList<>(Arrays.asList(project.getName(),project.getCategory().toString(), project.getDescription(), project.getPrivacy().toString()));
        // String tag = project.getTag();
        // if (tag != null) {
        //     attrs.add("Tag");
        //     attrsValue.add(tag);
        // }
        // if (project.getPicture() != null) {
        //     attrs.add("Picture");
        //     attrsValue.add(project.getPicture());
        // }
        // BDHelper.appendObjectSimple(behaviorEle, project.getAid(), SupportElement.activity, attrs);
        // //操作节点
        // LinkedMultiValueMap<String, String> info = new LinkedMultiValueMap<>();
        // info.put("Text", attrsValue);
        // LinkedHashMap<String, LinkedMultiValueMap<String, String>> objectIdAndAttrs = new LinkedHashMap<>();
        // objectIdAndAttrs.put(project.getAid(), info);
        // //将信息添加到交互行为文档中
        // BDHelper.appendOperation(behaviorEle, "input", objectIdAndAttrs);
        // behaviorDocXml.getRootElement().add(behaviorEle);
        // BehaviorDoc behaviorDocEntity = new BehaviorDoc();
        // behaviorDocEntity.setBid(project.getAid());
        // behaviorDocEntity.setDocument(behaviorDocXml.asXML());
        // docRepository.save(behaviorDocEntity);
    }

    @Override
    public void delActivity(String pid, String userId, String aid) {
        // BehaviorDoc behaviorDoc = tryAcquireLock(pid);
        // Document docXml = parseText(behaviorDoc.getDocument());
        // Element behaviorEle = BDHelper.createBehaviorEle(aid, Scene.activityStructure, InteractiveBehavior.delActivity);
        // BDHelper.appendSubject(behaviorEle, SupportElement.person, userId);
        // BDHelper.appendObjectSimple(behaviorEle, aid, SupportElement.activity, new ArrayList<>(Arrays.asList("State")));
        // LinkedMultiValueMap<String, String> attrs = new LinkedMultiValueMap<>();
        // attrs.add("Text", "Off");
        // BDHelper.appendOperation(behaviorEle, "delete", aid, attrs);
        // docXml.getRootElement().add(behaviorEle);
        // tryReleaseLock(pid, docXml.asXML());
    }

    @Override
    public void editActivity(String pid, String userId, HashMap<String, String> attrs) {

    }

    @Override
    public void editActivity(String pid, String userId, String attrKey, String attrValue) {

    }

    @Override
    public void userJoin(String pid, String userId, String aid) {

    }

    @Override
    public void userExit(String pid, String userId, String aid) {

    }

    @Override
    public void userRoleChange(String pid, String userId, String subjectId, String role) {

    }

    @Override
    public void toolAdd(String pid, String userId, String tid, String aid) {

    }

    @Override
    public void toolDel(String pid, String userId, String tid, String aid) {

    }

    @Override
    public void toolEdit(String pid, String userId, HashMap<String, String> attrs) {

    }
}

package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.service;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity.BehaviorDoc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ClassName BehaviorTrackService
 * @Description 提供交互行为组织与追溯接口
 * @Author zhngzhng
 * @Date 2022/6/29
 **/
public interface BehaviorTrackService {
    BehaviorDoc tryAcquireLock(String bid);

    void tryReleaseLock(String bid, String doc);

    void tryRefreshLock(String bid, long expirationTime);

    //===========活动构建=====================
    void initBehaviorDoc(String pid, String userId, Project project);

    void delActivity(String pid, String userId, String aid);

    void editActivity(String pid, String userId, HashMap<String, String> attrs);

    void editActivity(String pid, String userId, String attrKey, String attrValue);

    void userJoin(String pid, String userId, String aid);

    void userExit(String pid, String userId, String aid);

    void userRoleChange(String pid, String userId, String subjectId, String role);

    void toolAdd(String pid, String userId, String tid, String aid);

    void toolDel(String pid, String userId, String tid, String aid);

    void toolEdit(String pid, String userId, HashMap<String, String> attrs);




    /*
    过程组织
     */

    /*
    任务分配
     */

    /*
    问题定义
     */

    /*
    研究区认知
     */

    /*
    模型认知
     */

    /*
    资源共享
     */

    /*
    数据自动处理
     */

    /*
    数据半自动处理
     */

    /*
    数据手动处理
     */

    /*
    概念建模
     */

    /*
    逻辑建模
     */

    /*
    计算建模
     */

    /*
    模型配置
     */

    /*
    模型调用
     */

    /*
    结果比较
     */

    /*
    结果评估
     */

    /*
    决策支持
     */
}

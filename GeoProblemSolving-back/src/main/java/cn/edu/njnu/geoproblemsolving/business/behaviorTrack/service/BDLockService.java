package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.service;

import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity.BehaviorDoc;

/*
* @Author zhngzhng
* @Description 用于操作交互行为document级的锁
* @Param
* @Date 2022/7/4
*/
public interface BDLockService {
    /*
    查看该文档是否为可写状态
     */
    BehaviorDoc acquire(String pid, long expiration);

    /*
    写完成后释放锁，过期时间修改为当前
     */
    boolean release(String bid, String doc);

    /*
    延长锁的时间
     */
    boolean refresh(String pid, long expiration);
}

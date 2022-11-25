package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.service;

import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity.BehaviorDoc;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * @ClassName BDLockServiceImpl
 * @Description 交互行为文档锁的操作
 * @Author zhngzhng
 * @Date 2022/7/6
 **/
@Slf4j
@Service
public class BDLockServiceImpl implements BDLockService {
    private final MongoTemplate mongoTemplate;

    public BDLockServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 获取锁、判断是否能操作该文档，如下两种情况可以获取到锁
     * 1. False 直接获取到锁
     * 2. True time < currentTime, 可获取到锁
     * upsert无此文档是创建
     *
     * @param bid        交互行为文档的 id
     * @param expiration 锁的过期时间
     * @return 是否能操作该文档
     */
    @Override
    public BehaviorDoc acquire(String bid, long expiration) {
        Query query = Query.query(Criteria.where("bid").is(bid));
        long releaseTime = System.currentTimeMillis() + expiration;
        Update update = new Update();
        update.set("lockFlag", true);
        update.set("expireAt", releaseTime);
        // 将改文档lockFlag修改为true
        // 过期时间设置为指定时间间隔后
        // 并返回修改前文档
        BehaviorDoc behaviorDoc = mongoTemplate.findAndModify(query, update, BehaviorDoc.class);
        //无此文档,几乎是不可能的发生的
        if (behaviorDoc == null) return null;
        // 不可获取锁: true & valid（会给之前持有锁的客户端续时间，正常结束后无法标记为false
        // 有一种情况会死锁: A客户端持有锁但卡死，在过期前不断有其他客户端尝试获取锁，A客户端的持有时间被无线延长
        // 该种情况下，各客户端配合超时机制可解决）
        // 可获取锁: false、true & expire
        if (!behaviorDoc.isLockFlag() || behaviorDoc.getExpireAt() <= System.currentTimeMillis()) {
            behaviorDoc.setExpireAt(releaseTime);
            return behaviorDoc;
        }
        return null;
    }

    /**
     * acquire 方法确保仅有一个client在对文档进行操作
     * 释放锁
     * @param bid 活动文档的 id 也是项目 id
     * @param doc 交互行为文档xml 字符
     * @return 是否成功释放锁
     */
    @Override
    public boolean release(String bid, String doc) {
        Query query = Query.query(Criteria.where("bid").is(bid));
        Update update = new Update()
                .addToSet("lockFlag", false)
                .addToSet("expireAt", System.currentTimeMillis());
        if (doc != null) update.addToSet("document", doc);
        //将锁标记设置为false，并将过期时间设置为现在
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        BehaviorDoc behaviorDoc = mongoTemplate.findAndModify(query, update, options, BehaviorDoc.class);
        if (!behaviorDoc.isLockFlag()) return true;
        log.error("Unexpected result from release for behavior document lock {}", bid);
        return false;
    }

    /**
     * 延长锁的时间
     * @param bid
     * @param expiration
     * @return
     */
    @Override
    public boolean refresh(String bid, long expiration) {
        Query query = Query.query(Criteria.where("bid").is(bid));
        Update update = new Update()
                .addToSet("expireAt", System.currentTimeMillis() + expiration);
        UpdateResult updated = mongoTemplate.updateFirst(query, update, BehaviorDoc.class);
        final boolean refreshed = updated.getModifiedCount() == 1;
        if (refreshed) log.debug("Refresh lock successfully affected 1 record for id {}", bid);
        else log.warn("Refresh didn't affect any records for id {}", bid);
        return refreshed;
    }
}

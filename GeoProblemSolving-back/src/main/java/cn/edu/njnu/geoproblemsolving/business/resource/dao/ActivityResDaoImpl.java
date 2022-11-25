package cn.edu.njnu.geoproblemsolving.business.resource.dao;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @ClassName ResourceDaoImpl
 * @Description 项目中资源相关相关操作
 * @Author zhngzhng
 * @Date 2021/4/20
 **/
@Repository
public class ActivityResDaoImpl implements ActivityResDao {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ResourceEntity addResource(ResourceEntity res) {
        return mongoTemplate.save(res);
    }

    @Override
    public Collection<ResourceEntity> addResource(List<ResourceEntity> resList) {
         return mongoTemplate.insertAll(resList);
    }

    @Override
    public Long delResource(String uid) {
        Query query = new Query(Criteria.where("uid").is(uid));
        long deleteResult = mongoTemplate.remove(query, ResourceEntity.class).getDeletedCount();
        return deleteResult;
    }

    @Override
    public Long delResource(String uid, String aid) {
        Query query = new Query();
        Criteria uidCriteria = new Criteria("uid").is(uid);
        Criteria criteria = new Criteria("activityId").is(aid);
        query.addCriteria(uidCriteria);
        query.addCriteria(criteria);
        return mongoTemplate.remove(query, ResourceEntity.class).getDeletedCount();
    }

    @Override
    public List<ResourceEntity> queryByAid(String aid) {
        try {
            Query query = new Query(Criteria.where("activityId").is(aid));
            List<ResourceEntity> resourceEntityList = mongoTemplate.find(query, ResourceEntity.class);
            return resourceEntityList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String updateRes(Query query, Update update) {
        UpdateResult upsert = mongoTemplate.upsert(query, update, ResourceEntity.class);
        if (upsert.getModifiedCount() == 1){
            return "suc";
        }else {
            return "fail";
        }
    }

    @Override
    public ResourceEntity queryByUid(String uid) {
        Query query = new Query(Criteria.where("uid").is(uid));
        return mongoTemplate.findOne(query, ResourceEntity.class);
    }

    @Override
    public ResourceEntity queryByAidAndName(String aid, String folderName) {
        Query query = new Query();
        Criteria criteria = new Criteria("activityId").is(aid);
        Criteria criteria1 = new Criteria("name").is(folderName);
        query.addCriteria(criteria);
        query.addCriteria(criteria1);
        return mongoTemplate.findOne(query, ResourceEntity.class);
    }

    @Override
    public List<ResourceEntity> findAll() {
        return mongoTemplate.findAll(ResourceEntity.class);
    }
}

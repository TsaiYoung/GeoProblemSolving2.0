package cn.edu.njnu.geoproblemsolving.business.resource.dao;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;

public interface ActivityResDao {
    ResourceEntity addResource(ResourceEntity res);

    Collection<ResourceEntity> addResource(List<ResourceEntity> resList);

    /*
    根目录下资源直接删除document
    非根目录下内容实际就是更新字段，更新根目录下资源的字段
     */
    Long delResource(String uid);

    Long delResource(String uid, String aid);

    List<ResourceEntity> queryByAid(String aid);

    String updateRes(Query query, Update update);

    ResourceEntity queryByUid(String uid);

    ResourceEntity queryByAidAndName(String aid, String folderName);

    List<ResourceEntity> findAll();
}

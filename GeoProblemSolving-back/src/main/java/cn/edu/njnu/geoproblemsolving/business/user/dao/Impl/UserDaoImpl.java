package cn.edu.njnu.geoproblemsolving.business.user.dao.Impl;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.Project;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.dto.InquiryUserDto;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserVo;
import cn.edu.njnu.geoproblemsolving.business.user.util.ICommonUtil;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Repository
public class UserDaoImpl implements UserDao {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public UserEntity findUserById(String userId) {
        //错误处理
        try {
            Query query = new Query(Criteria.where("userId").is(userId));
            UserEntity user = mongoTemplate.findOne(query, UserEntity.class);
            return user;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Object updateUserInfo(HttpServletRequest req) {
        try {
            String userId = req.getParameter("userId");
            Query query = new Query(Criteria.where("userId").is(userId));
            ICommonUtil commonUtil = new ICommonUtil();
            Update update = commonUtil.setUpdate(req);
            mongoTemplate.updateFirst(query, update, UserEntity.class);
            UserEntity newUser = mongoTemplate.findOne(query, UserEntity.class);
            return newUser;
        } catch (Exception e) {
            return "Fail";
        }
    }

    @Override
    public Object updateUserInfo(UserEntity user) {

        return null;
    }

    @Override
    public Object saveUser(JSONObject remoteUser) {
        //JSONObject to Local UserEntity
        //只会把相同字段内容写入，多余字段不会写入
        String userName = (String) remoteUser.get("name");
        remoteUser.remove("name");
        remoteUser.put("userName", userName);
        String organizations = remoteUser.get("organizations").toString();
        remoteUser.remove("organizations");
        remoteUser.put("organization", organizations);
        UserVo localUser = JSONObject.toJavaObject(remoteUser, UserVo.class);
        try {
            mongoTemplate.save(localUser);
        } catch (Exception e) {
            return e;
        }
        return localUser;
    }

    @Override
    public Object saveLocalUser(UserEntity user) {
        try {
            UserEntity localUser = mongoTemplate.save(user);
            return localUser;
        } catch (Exception e) {
            return e;
        }
    }

    @Override
    public JsonResult getJoinedProjectList(String[] projectIdList) {
        ArrayList<Project> projects = new ArrayList<>();
        for (int i = 0; i < projectIdList.length; i++) {
            try {
                Query query = new Query(Criteria.where("_id").is(projectIdList[i]));
                Project project = mongoTemplate.findOne(query, Project.class);
                projects.add(project);
            } catch (Exception e) {
                return ResultUtils.error(-1, "Fail");
            }
        }
        return ResultUtils.success(projects);
    }

    @Override
    public JsonResult getUserInfo(String key, String value) {
        try {
            Query query = new Query(Criteria.where(key).is(value));
            InquiryUserDto user = mongoTemplate.findOne(query, InquiryUserDto.class);
            if (user == null) {
                return ResultUtils.error(-1, "Fail: user does not exist.");
            } else {
                return ResultUtils.success(user);
            }
        } catch (Exception ex) {
            return ResultUtils.error(-2, "Fail: Exception");
        }
    }

    @Override
    public UserEntity findByUserEmail(String email) {
        return mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), UserEntity.class);
    }

    @Override
    public JsonResult uploadUserRes(String uploaderId, ResourceEntity res) {
        Query query = new Query(Criteria.where("userId").is(uploaderId));
        Update update = new Update();
        try {
            UserEntity user = mongoTemplate.findOne(query, UserEntity.class);
            ArrayList<ResourceEntity> resources = user.getResource();
            if (resources == null) {
                resources = new ArrayList<ResourceEntity>();
            }
            resources.add(res);
            update.set("resources", resources);
            mongoTemplate.updateFirst(query, update, UserEntity.class);
            return ResultUtils.success(mongoTemplate.findOne(query, UserEntity.class));
        } catch (Exception e) {
            return ResultUtils.error(-2, "Failed to update user resource filed.");
        }
    }

    @Override
    public JsonResult delUserRes(String userId, String[] rids) {
        Query query = new Query(Criteria.where("userId").is(userId));
        UserEntity user = mongoTemplate.findOne(query, UserEntity.class);
        try {
            ArrayList<ResourceEntity> resources = user.getResource();
            //ArrayList 的动态删除
            ArrayList<Integer> indexArray = new ArrayList<>();
            for (int i = 0; i < rids.length; i++) {
                for (int index = 0; index < resources.size(); index++) {
                    String address = resources.get(index).getAddress();
                    String rid = address.split("//data//")[1];
                    if (rid.equals(rids[i])) {
                        indexArray.add(index);
                        break;
                    }
                }
            }
            indexArray.sort(Comparator.naturalOrder());
            for (int j = 0; j < indexArray.size(); j++) {
                resources.remove(indexArray.get(j) - j);
            }
            Update update = new Update();
            update.set("resources", resources);
            mongoTemplate.updateFirst(query, update, UserEntity.class);
            return ResultUtils.success();

        } catch (Exception e) {
            return ResultUtils.error(-2, "Delete local user field failed.");
        }
    }


    @Override
    public JsonResult deleteUserProject(HttpServletRequest req) {
        String userId = req.getParameter("userId");
        String projectId = req.getParameter("projectId");
        Update update = new Update();
        Query query = new Query(Criteria.where("userId").is(userId));
        Query projectQuery = new Query(Criteria.where("aid").is(projectId));
        try {
            UserEntity user = mongoTemplate.findOne(query, UserEntity.class);
            ArrayList<String> joinedProjects = user.getJoinedProjects();
            for (int i = 0; i < joinedProjects.size(); i++) {
                if (joinedProjects.get(i).equals(projectId)) {
                    joinedProjects.remove(i);
                }
            }
            update.set("joinedProjects", joinedProjects);
            mongoTemplate.updateFirst(query, update, UserEntity.class);

            //更新项目成员信息
            Project updateProject = mongoTemplate.findOne(projectQuery, Project.class);
            JSONArray members = updateProject.getMembers();
            for (int i=0; i<members.size(); i++){
                String quitUserId = members.getJSONObject(i).getString("userId");
                if (quitUserId.equals(userId)){
                    members.remove(i);
                }
            }
            Update projectUpdate = new Update();
            projectUpdate.set("members", members);
            mongoTemplate.updateFirst(projectQuery, projectUpdate, Project.class);

            return ResultUtils.success();
        }catch (Exception e){
            return ResultUtils.error(-2, "Fail");
        }

    }



    //============================================================
    //
    // @Override
    // public JsonResult saveUser(UserEntity user) {
    //     try {
    //         user.setPassword("");
    //         return ResultUtils.success(mongoTemplate.save(user));
    //     }catch (MongoException e){
    //         return ResultUtils.error(-2, e.toString());
    //     }
    // }
    //
    // @Override
    // public JsonResult updateInfo(String id, Update update) {
    //     return null;
    // }
    //
    // @Override
    // public JsonResult findUserByIdOrEmail(String filed) {
    //     try {
    //         Criteria criteria = new Criteria().orOperator(Criteria.where("userId").is(filed),
    //                 Criteria.where("email").is(filed));
    //         Query query = new Query(criteria);
    //         UserEntity user = mongoTemplate.findOne(query, UserEntity.class);
    //         if (user == null){
    //             return ResultUtils.error(-3, "No Object");
    //         }
    //         return ResultUtils.success(user);
    //     }catch (MongoException e){
    //         return ResultUtils.error(-2, e.toString());
    //     }
    // }


    @Override
    public JsonResult saveUser(UserEntity user) {
        return ResultUtils.success(mongoTemplate.save(user));
    }

    /*
    id 为 userId
     */
    @Override
    public UserEntity updateInfo(String userId, Update update) {
        Query query = new Query(Criteria.where("userId").is(userId));
        UpdateResult updateResult = mongoTemplate.upsert(query, update, UserEntity.class);
        if (updateResult.getMatchedCount() != 1){
            return null;
        }
        return findUserByIdOrEmail(userId);
    }

    @Override
    public UserEntity findUserByIdOrEmail(String filed) {
        Criteria criteria = new Criteria().orOperator(Criteria.where("email").is(filed),
                Criteria.where("userId").is(filed));
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, UserEntity.class);
    }

    @Override
    public List<UserEntity> findUsers(Set<String> userIds) {
        Query query = new Query();
        if (userIds == null || userIds.isEmpty()) return null;
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();
        for (String item : userIds){
            criteriaList.add(Criteria.where("userId").is(item));
        }
        criteria.orOperator(criteriaList.stream().toArray(Criteria[]::new));
        query.addCriteria(criteria);
        return mongoTemplate.find(query, UserEntity.class);
    }
}

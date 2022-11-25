package cn.edu.njnu.geoproblemsolving.business.user.dao;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.mongodb.core.query.Update;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface UserDao {
    Object findUserById(String userId);

    Object updateUserInfo(HttpServletRequest req);

    //前端传输用户信息
    Object updateUserInfo(UserEntity user);

    Object saveUser(JSONObject user);

    Object saveLocalUser(UserEntity user);

    JsonResult getJoinedProjectList(String[] projectIdList);

    JsonResult getUserInfo(String key, String value);

    UserEntity findByUserEmail(String email);


    // 用户资源字段 curd 处理
    JsonResult uploadUserRes(String uploaderId, ResourceEntity res);

    JsonResult delUserRes(String userId, String[] rids);



    //用户项目字段相关
    JsonResult deleteUserProject(HttpServletRequest req);

    //
    // //修改版本内容3.31
    JsonResult saveUser(UserEntity user);
    // //此处id可以为 userId 也可以为 email
    // JsonResult updateInfo(String id, Update update);
    //
    // JsonResult findUserByIdOrEmail(String filed);


    UserEntity updateInfo(String id, Update update);

    UserEntity findUserByIdOrEmail(String filed);

    List<UserEntity> findUsers(Set<String> userIds);

}

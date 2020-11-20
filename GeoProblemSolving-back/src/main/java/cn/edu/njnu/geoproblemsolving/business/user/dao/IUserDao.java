package cn.edu.njnu.geoproblemsolving.business.user.dao;

import cn.edu.njnu.geoproblemsolving.business.user.entity.User;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserDto;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;

public interface IUserDao {
    Object findUserById(String userId);
    Object updateUserInfo(HttpServletRequest req);
    //前端传输用户信息
    Object updateUserInfo(User user);
    Object saveUser(JSONObject user);
    Object saveLocalUser(User user);
}
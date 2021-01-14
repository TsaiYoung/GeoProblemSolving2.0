package cn.edu.njnu.geoproblemsolving.business.user.service.Impl;

import cn.edu.njnu.geoproblemsolving.Entity.Resources.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourcePojo;
import cn.edu.njnu.geoproblemsolving.business.resource.util.RestTemplateUtil;
import cn.edu.njnu.geoproblemsolving.business.user.dao.IUserImpl;
import cn.edu.njnu.geoproblemsolving.business.user.entity.User;
import cn.edu.njnu.geoproblemsolving.business.user.repository.UserRepository;
import cn.edu.njnu.geoproblemsolving.business.user.service.UserService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    IUserImpl userDao;

    @Value("${authServerIp}")
    String authServerIp;

    public UserServiceImpl(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User findUser(String userId){
        try {
            Optional user =  userRepository.findById(userId);
            if (user.isPresent())
                return (User) user.get();
            else
                return null;
        }
        catch (Exception ex){
            return null;
        }
    }

    @Override
    public Object updataUserInfo(User user){
        try {
            String userId = user.getUserId();
            if(userRepository.findById(userId).isPresent()) return "The user does not exist.";

            String name = user.getName();
            if(name != null) {
                Query queryResource = Query.query(Criteria.where("uploaderId").is(userId));
                List<ResourceEntity> resourceEntities = mongoTemplate.find(queryResource, ResourceEntity.class);
                for (ResourceEntity resourceEntity : resourceEntities) {
                    String resourceId = resourceEntity.getResourceId();
                    Query query = Query.query(Criteria.where("resourceId").is(resourceId));
                    Update update = new Update();
                    update.set("uploaderName", name);
                    mongoTemplate.updateFirst(query, update, ResourceEntity.class);
                }
            }
            userRepository.save(user);

            return user;
        }
        catch (Exception ex){
            return "Fail: Exception";
        }
    }

    @Override
    public Object register(User user){
        try {
            String email = user.getEmail();
            if(userRepository.findByEmail(email) != null) return "The email address has been registered.";

            String userId = UUID.randomUUID().toString();
            user.setUserId(userId);

            user.setJoinedProjects(new ArrayList<>());
            user.setCreatedProjects(new ArrayList<>());

            userRepository.save(user);
            return user;
        }
        catch (Exception ex){
            return "Fail: Exception";
        }
    }

    @Override
    public Object login(String email, String password){
        try {
            User user = userRepository.findByEmail(email);
            if(user == null) return "The email address has not been registered.";

            if(user.getPassword().equals(password)){
                return user;
            }
            else {
                return "Fail: Worrg password";
            }
        }
        catch (Exception ex){
            return "Fail: Exception";
        }
    }

    @Override
    public JsonResult uploadResourceField(String email, ArrayList<ResourcePojo> res) {
        JsonResult updateUserResFieldResult = userDao.sharedUserRes(email, res);
        if (updateUserResFieldResult.getCode() == 0){
            //更新成功，然后发送邮件
            //增加用户通知
            return ResultUtils.success();
        }else {
            return updateUserResFieldResult;
        }

    }

    @Override
    public JsonResult sendResetPwdEmail(String email) {
        RestTemplateUtil restTemplateUtil = new RestTemplateUtil();
        String sendResetPwdEmailUrl = "http://" + authServerIp + "/AuthServer/user/resetPassword?email=" + email;
        Object jsonObject = restTemplateUtil.sendReqToAuthServer(sendResetPwdEmailUrl, HttpMethod.POST);
        if (jsonObject.toString().equals("Fail")){
            return ResultUtils.error(-2, "Fail");
        }
        JSONObject sendEmailResult = JSONObject.parseObject(jsonObject.toString());
        Integer resultCode = sendEmailResult.getInteger("code");
        if (resultCode == 0){
            return ResultUtils.success();
        }else{
            return ResultUtils.error(-1, "No such user");
        }
    }

    @Override
    public Object resetPwd(String email, String oldPwd, String newPwd) {
        RestTemplateUtil restTemplateUtil = new RestTemplateUtil();
        String paramUrl = "email=" + email + "&oldPwd=" + oldPwd + "&newPwd=" + newPwd;
        String resetPwdUrl = "http://" + authServerIp + "/AuthServer/user/newPassword?" + paramUrl;
        Object jsonObject = restTemplateUtil.sendReqToAuthServer(resetPwdUrl, HttpMethod.POST);
        return jsonObject;
    }


    @Override
    public String changePassword(String email, String password){
        try {
            User user = userRepository.findByEmail(email);
            if(user == null) return "The email address has not been registered.";

            user.setPassword(password);
            userRepository.save(user);

            return "Success";
        }
        catch (Exception ex){
            return "Fail: Exception";
        }
    }
}

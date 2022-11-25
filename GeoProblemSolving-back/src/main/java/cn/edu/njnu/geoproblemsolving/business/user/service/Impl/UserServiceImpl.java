package cn.edu.njnu.geoproblemsolving.business.user.service.Impl;

import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.resource.util.RestTemplateUtil;
import cn.edu.njnu.geoproblemsolving.business.user.dao.Impl.UserDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.user.entity.TokenInfo;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserVo;
import cn.edu.njnu.geoproblemsolving.business.user.repository.UserRepository;
import cn.edu.njnu.geoproblemsolving.business.user.service.TokenTask;
import cn.edu.njnu.geoproblemsolving.business.user.service.UserService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    UserDaoImpl userDao;

    public UserServiceImpl(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }



    //===3.31修改版本======================================================================

    @Value("${userServerLocation}")
    String userServerIpAndPort;

    @Value("${client_id}")
    String client_id;
    @Value("${client_secret}")
    String client_secret;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TokenTask tokenTask;

    @Autowired
    NodeService nodeService;

    /**
     * 用户注册服务
     * 1. 用户服务器注册
     * 2. 然后平台存储
     *
     * @param register
     * @return
     */
    @Override
    public JsonResult registerService(UserEntity register) {
        //构造 http 发送给用户服务器
        String url = "http://" + userServerIpAndPort + "/user";
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json;charset=UTF-8");
        httpHeaders.setContentType(mediaType);
        String password = register.getPassword();
        register.setPassword(DigestUtils.sha256Hex(password.getBytes()));
        HttpEntity<Object> httpEntity = new HttpEntity<>(register, httpHeaders);
        ResponseEntity<JSONObject> registerRes = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
        JsonResult result = reqStatus(registerRes);
        int code = (int) registerRes.getBody().get("code");
        //注册成功在本平台进行注册
        if (code == 0) {
            UserEntity user = JSON.parseObject(JSON.toJSONString(result.getData()), UserEntity.class);
            user.setTokenInfo(null);
            return userDao.saveUser(user);
        } else {
            return result;
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param email
     * @return
     */
    @Override
    public JsonResult sendResetPwdEmail(String email) {
        String url = "http://" + userServerIpAndPort + "/user/resetPwd/" + email;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        return reqStatus(responseEntity);
    }

    @Override
    public JsonResult resetPwdByCode(String email, String code, String newPwd) {
        String encodePwd = DigestUtils.sha256Hex(newPwd.getBytes());
        String url = "http://" + userServerIpAndPort + "/user/resetPwd/" + email + "/" + code + "/" + encodePwd;
        ResponseEntity<JSONObject> responseEntity = restTemplate.getForEntity(url, JSONObject.class);
        return reqStatus(responseEntity);
    }

    @Override
    public JsonResult resetPwdByOldPwd(String email, String oldPwd, String newPwd) {
        oldPwd = DigestUtils.sha256Hex(oldPwd.getBytes());
        newPwd = DigestUtils.sha256Hex(newPwd.getBytes());
        UserEntity user = userDao.findUserByIdOrEmail(email);
        TokenInfo tokenInfo = user.getTokenInfo();
        String access_token = tokenInfo.getAccess_token();
        HttpHeaders headers = new HttpHeaders();
        //从用户表中读取access_token内容
        headers.add("Authorization", "Bearer " + access_token);
        //HttpEntity(MultiValueMap<String, String> headers)
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        String resetPwdUri = "http://" + userServerIpAndPort + "/auth/newPwd/" + oldPwd + "/" + newPwd;
        ResponseEntity<JSONObject> resetResult = restTemplate.exchange(resetPwdUri, HttpMethod.GET, httpEntity, JSONObject.class);
        return JSON.parseObject(JSON.toJSONString(resetResult.getBody()), JsonResult.class);
    }

    /**
     * 授权并获取用户信息
     *
     * @param email
     * @param password
     * @return
     */
    @Override
    public JsonResult loginAndAcquireInfo(String email, String password, String ipAddress) {
        // password 模式获取token
        // UserEntity user = userDao.findByUserEmail(email);
        LinkedMultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        String encodePassword = DigestUtils.sha256Hex(password.getBytes());
        paramMap.add("client_id", client_id);
        paramMap.add("client_secret", client_secret);
        paramMap.add("username", email);
        paramMap.add("password", encodePassword);
        paramMap.add("scope", "all");
        paramMap.add("grant_type", "password");
        String authUri = "http://" + userServerIpAndPort + "/oauth/token";
        try {
            JSONObject tokenJson = restTemplate.postForObject(authUri, paramMap, JSONObject.class);
            // 获取 token 失败
            if (tokenJson == null) {
                return ResultUtils.error(-2, "Fail to access token");
            }

            // 将获取到的 token 相关内容给存到用户数据库中
            //MongoDB 的存储机制中, _id 相同的话使用 save 方法会更新字段
            String access_token = (String) tokenJson.get("access_token");
            String refresh_token = (String) tokenJson.get("refresh_token");
            Date invalidTime = JSON.parseObject(JSON.toJSONString(tokenJson.get("invalidTime")), Date.class);
            int expires = (int) tokenJson.get("expires_in");
            TokenInfo userToken = new TokenInfo(access_token, refresh_token, expires, invalidTime);

            // 使用access_token 获取用户信息
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + access_token);
            //httpEntity = httpHeader + httpBody,当然也可以只有其中一部分
            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
            String getInfoUri = "http://" + userServerIpAndPort + "/auth/login/" + ipAddress;
            //Url, RequestType, RequestContent, ResponseDataType
            //是否能用 UserEntity 接收返回结果？
            ResponseEntity<JSONObject> userJson = restTemplate.exchange(getInfoUri, HttpMethod.GET, httpEntity, JSONObject.class);
            //还是改用 JsonResult 来接，然后再使用 code 进行判断
            if (userJson.getBody().get("data") == null) {
                return ResultUtils.error(-2, "Failed to get user information.");
            }
            //转换成为了 localUser
            UserEntity loginUser = JSON.parseObject(JSON.toJSONString(userJson.getBody().get("data")), UserEntity.class);
            //获取对象空字段字段名
            String[] nullPropertyNames = getNullPropertyNames(loginUser);
            //本地的用户
            UserEntity localUser = userDao.findUserByIdOrEmail(email);
            /*
            如果本地无此用户，则直接将userServer 返回用户存入
            如果有此用户，则将获取到的字段信息复制到本地用户中
             */
            if (localUser == null) {
                localUser = new UserEntity();
            }
            localUser.setTokenInfo(userToken);
            BeanUtils.copyProperties(loginUser, localUser, nullPropertyNames);
            userDao.saveUser(localUser);

            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(localUser, userVo);
            return ResultUtils.success(userVo);
        } catch (Exception e) {
            return ResultUtils.error(-2, "Email or Password was not found.");
        }
    }

    //获取空字段名
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object propertyValue = src.getPropertyValue(pd.getName());
            if (propertyValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    //获取请求 ip 地址
    public String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        return ipAddress;
    }

    /**
     * 用于处理用户服务器返回内容
     *
     * @param responseEntity
     * @return
     */
    public JsonResult reqStatus(ResponseEntity<JSONObject> responseEntity) {
        int code = (int) responseEntity.getBody().get("code");
        String msg = (String) responseEntity.getBody().get("msg");
        Object data = responseEntity.getBody().get("data");
        switch (code) {
            case 0:
                return ResultUtils.success(data);
            case -1:
                return ResultUtils.error(-1, msg);
            case -2:
                return ResultUtils.error(-2, msg);
            case -3:
                return ResultUtils.error(-3, msg);
            default:
                return ResultUtils.error(-2, "Fail");
        }
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     * @return
     */
    @Override
    public JsonResult updateUserInfo(JSONObject userInfo) {
        String userId = userInfo.getString("userId");
        //取出 token
        UserEntity localUser = userDao.findUserByIdOrEmail(userId);
        //不存在用户不存在情况，除非故意捣乱
        if (localUser == null) {
            return ResultUtils.error(-2, "The user does not exist.");
        }
        String access_token = localUser.getTokenInfo().getAccess_token();

        // 更新用户服务器中的用户字段
        String updateUrl = "http://" + userServerIpAndPort + "/auth/update";
        RestTemplateUtil httpUtil = new RestTemplateUtil();

        try {
            JSONObject updateResultJson = httpUtil.postRequestToServer(updateUrl, access_token, userInfo).getBody();
            if (updateResultJson.getInteger("code") != 0) {
                return ResultUtils.error(-2, "update fail");
            }
            //用户服务器更新成功，更新本地用户
            UserEntity serverUser = updateResultJson.getObject("data", UserEntity.class);
            String[] nullPropertyNames = getNullPropertyNames(serverUser);

            //update node
            ArrayList<String> localOrg = JSONObject.parseObject(JSONObject.toJSONString(localUser.getOrganizations()), ArrayList.class);
            ArrayList<String> localDomain = JSONObject.parseObject(JSONObject.toJSONString(localUser.getDomain()), ArrayList.class);

            BeanUtils.copyProperties(serverUser, localUser, nullPropertyNames);
            userDao.saveUser(localUser);


            //update node
            ArrayList<String> serverOrg = serverUser.getOrganizations();
            ArrayList<String> serverDomain = serverUser.getDomain();

            nodeService.putUserInfoToNode(userId, localOrg, serverOrg, localDomain, serverDomain);

            return ResultUtils.success(serverUser.getAvatar());
        } catch (Exception e) {
            return ResultUtils.error(-2, "Failed to update user information.");
        }
    }

    @Override
    public JsonResult uploadAvatar(HttpServletRequest req) throws IOException, ServletException {
        String email = (String) req.getSession().getAttribute("email");
        UserEntity localUser = userDao.findUserByIdOrEmail(email);
        String access_token = localUser.getTokenInfo().getAccess_token();
        //判断 request content-type类型
        if (!ServletFileUpload.isMultipartContent(req)) {
            System.out.println("File is not multimedia.");
            return ResultUtils.error(-2, "File is not multimedia.");
        }
        //form-data:  picture: file
        Part avatarPic = req.getPart("picture");
        InputStream is = avatarPic.getInputStream();
        byte[] imageByte = new byte[(int) avatarPic.getSize()];
        // read(byte b[], int off, int len)
        is.read(imageByte, 0, imageByte.length);
        is.close();
        // 图片base64
        // String encodeToString(byte[] src)
        String baseStr = Base64.getEncoder().encodeToString(imageByte);

        String updateUrl = "http://" + userServerIpAndPort + "/auth/update";
        RestTemplateUtil httpUtil = new RestTemplateUtil();
        JSONObject payloadJson = new JSONObject();
        payloadJson.put("avatar", baseStr);
        try {
            JSONObject updateResultJson = httpUtil.postRequestToServer(updateUrl, access_token, payloadJson).getBody();
            if (updateResultJson.getInteger("code") != 0) {
                return ResultUtils.error(-2, updateResultJson.getString("data"));
            }
            UserEntity serverUser = updateResultJson.getObject("data", UserEntity.class);
            String avatar = serverUser.getAvatar();
            return ResultUtils.success(avatar);
        } catch (Exception e) {
            return ResultUtils.error(-2, e.toString());
        }
    }

    @Override
    public void logout(String userId) {
        UserEntity logoutUser = userDao.findUserByIdOrEmail(userId);
        logoutUser.setTokenInfo(null);
        userDao.saveUser(logoutUser);
    }

    @Override
    public UserVo getUserVo(String userId) {
        UserEntity localUser = userDao.findUserByIdOrEmail(userId);
        if (localUser == null) {
            return null;
        }
        UserVo userVo = JSONObject.parseObject(JSONObject.toJSONString(localUser), UserVo.class);
        return userVo;
    }

    @Override
    public JsonResult deleteUserProjectService(HttpServletRequest req) {
        return userDao.deleteUserProject(req);
    }

    @Override
    public Integer validEmailRegistered(String email) {
        String url = "http://" + userServerIpAndPort + "/user/registered/" + email;
        JSONObject validResponse = restTemplate.getForObject(url, JSONObject.class);
        Integer code = validResponse.getInteger("code");
        if (code == -1){
            //未注册
            return 0;
        }
        //已经被注册
        return 1;
    }
}

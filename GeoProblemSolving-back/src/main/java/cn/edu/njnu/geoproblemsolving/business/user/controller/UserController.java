package cn.edu.njnu.geoproblemsolving.business.user.controller;

import cn.edu.njnu.geoproblemsolving.business.user.entity.TodoEntity;
import cn.edu.njnu.geoproblemsolving.business.user.service.Impl.TodoServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.user.dao.Impl.UserDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserVo;
import cn.edu.njnu.geoproblemsolving.business.user.service.Impl.UserServiceImpl;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    UserDaoImpl userDao;

    @Autowired
    UserServiceImpl userService;


    @RequestMapping(method = RequestMethod.PUT, value = "/quitProject")
    public JsonResult updateUserProject(HttpServletRequest req) {
        return userService.deleteUserProjectService(req);
    }



    /**
     * @Author mzy
     * @Date 2020.12.18
     */

    /**
     * Inquiry information of one user
     *
     * @param key：userId, email
     * @param value
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public JsonResult getUserInfo(@RequestParam String key, @RequestParam String value) {
        return userDao.getUserInfo(key, value);
    }




    /**
     * @Author zhngzhng
     * @Date 2021/4/4
     * @Description
     * 我想的话，common Response 仅在 controller 层使用即可
     * 我这里用得似乎并不规范,但也不想改了😄
     * 不得不说，common Response当返回值就像Object一样简单
     */

    /**
     * 用户注册
     * 两个步骤
     * 1.用户服务器进行注册
     * 2.注册成功则在参与式本地进行保存，失败则返回对应信息回前端
     *
     * @param user
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public JsonResult register(@RequestBody UserEntity user) {
        return userService.registerService(user);
    }

    @RequestMapping(value = "/sendEmail/{email}", method = RequestMethod.GET)
    public JsonResult sendCode(@PathVariable String email) {
        return userService.sendResetPwdEmail(email);
    }

    @RequestMapping(value = "/changePwd/{email}/{code}/{newPwd}", method = RequestMethod.GET)
    public JsonResult changePassword(@PathVariable("email") String email,
                                     @PathVariable("code") String code,
                                     @PathVariable("newPwd") String newPwd) {
        return userService.resetPwdByCode(email, code, newPwd);
    }

    @RequestMapping(value = "/resetPwd/{email}/{oldPwd}/{newPwd}", method = RequestMethod.GET)
    public JsonResult resetPassword(@PathVariable("email") String email,
                                    @PathVariable("oldPwd") String oldPwd,
                                    @PathVariable("newPwd") String newPwd) {
        return userService.resetPwdByOldPwd(email, oldPwd, newPwd);
    }

    /**
     * 需要修改，增加 httpRequest 获取用户登录ip
     * @param email
     * @param password
     * @param request
     * @return
     */
    @RequestMapping(value = "/login/{email}/{password}", method = RequestMethod.GET)
    public JsonResult login(@PathVariable("email") String email,
                            @PathVariable("password") String password,
                            HttpServletRequest request) {
        String ipAddr = userService.getIpAddr(request);
        JsonResult loginResult = userService.loginAndAcquireInfo(email, password, ipAddr);
        if (loginResult.getCode() != 0) {
            return loginResult;
        }
        UserVo localUser = (UserVo)loginResult.getData();
        HttpSession session = request.getSession();
        session.setAttribute("userId", localUser.getUserId());
        session.setAttribute("name", localUser.getName());
        session.setAttribute("avatar", localUser.getAvatar());
        session.setAttribute("email", localUser.getEmail());
        session.setMaxInactiveInterval(60 * 60 * 2);
        System.out.println("User login. User name: " + localUser.getName());
        return loginResult;
    }

    /**
     * 用户登录状态判定
     * 通过session判断
     * 这个比较关键（判断用户登录状态）
     * 若有的话则返回指定字段到前端即可
     * 前端用于获取用户信息
     * @param req
     * @return
     */
    @RequestMapping(value = "/state", method = RequestMethod.POST)
    public Object userState(HttpServletRequest req) {
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("userId");
        if (userId == null) { return false; }
        return userService.getUserVo(userId);

    }

    /**
     * 涉及到到 resource 的需要将所有内容搞定
     * @param user
     * @return
     */
    @RequestMapping(produces = "application/json;charset=UTF-8", method = RequestMethod.PUT)
    public JsonResult updateUserInfo(@RequestBody JSONObject user){
        //此处 user 必须将其的userId 带过来
        return userService.updateUserInfo(user);
    }

    /**
     * 登出，将用户tokenInfo字段置空
     * @param request
     * @throws IOException
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        System.out.println("User logout. UserName: " + session.getAttribute("name"));
        String userId = (String)session.getAttribute("userId");
        //清除 Token
        userService.logout(userId);
        session.invalidate();
    }

    /**
     * 上传用户头像
     * picture: file
     * @param req
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public JsonResult sendAvatar(HttpServletRequest req) throws IOException, ServletException {
        return userService.uploadAvatar(req);
    }

    @RequestMapping(value = "/registered/{email}", method = RequestMethod.GET)
    public JsonResult validEmailIsRegistered(@PathVariable String email){
        Integer code = userService.validEmailRegistered(email);
        if (code == 0){
            return ResultUtils.error(-1, "Email isn't be registered.");
        }
        return ResultUtils.success();
    }


    /*
    --------------------------------------------------------------------------------------------------------------------
    TodoList 相关接口全部迁移过来
    考虑一下是否需要todo改为user的字段
    如果这样的话，又全是遍历、遍历、遍历
     */
    @Autowired
    TodoServiceImpl todoService;

    /**
     * 创建 personal Task
     * @param personalTask
     * @return
     */
    @RequestMapping(value = "/todo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public JsonResult createTodoTask(@RequestBody TodoEntity personalTask){
        return todoService.createPersonalTask(personalTask);
    }

    /**
     * 删除
     * @param ptId
     * @return
     */
    @RequestMapping(value = "/todo/{ptId}", method = RequestMethod.DELETE)
    public JsonResult delTodoTask(@PathVariable String ptId){
        return todoService.delPTask(ptId);
    }

    /**
     *修改
     * @param jsonTask
     * @return
     */
    @RequestMapping(value = "/todo", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public JsonResult updateTask(@RequestBody JSONObject jsonTask){
        return todoService.updatePTask(jsonTask);
    }

    /**
     * 使用userId 查询
     * @param userId
     * @return
     */
    @RequestMapping(value = "/todo/{userId}", method = RequestMethod.GET)
    public JsonResult getUserTask(@PathVariable String userId){
        return todoService.findPersonalTask(userId);
    }



}

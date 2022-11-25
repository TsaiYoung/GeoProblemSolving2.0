package cn.edu.njnu.geoproblemsolving.business.activity.controller;

import cn.edu.njnu.geoproblemsolving.business.CommonUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.ShareToken;
import cn.edu.njnu.geoproblemsolving.business.activity.service.ShareTokenService;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ClassName ShareTokenController
 * @Description 临时用户
 * @Author zhngzhng
 * @Date 2021/12/13
 **/
@RestController
@RequestMapping(value = "/token")
public class ShareTokenController {
    @Autowired
    ShareTokenService tokenService;


    /**
     * 获取现在所有的token
     * @param aid
     * @param inviterId
     * @return
     */
    @RequestMapping(value = "/all/{aid}/{inviterId}", method = RequestMethod.GET)
    public JsonResult getTokens(@PathVariable String aid, @PathVariable String inviterId){
        ArrayList<HashMap<String, String>> allToken = tokenService.getAllToken(aid, inviterId);
        return ResultUtils.success(allToken);
    }

    /**
     * 新增 token
     * @param inviterId
     * @param aid
     * @return
     */
    @RequestMapping(value = "/{aid}/{inviterId}", method = RequestMethod.GET)
    public JsonResult inviteTemp(@PathVariable String inviterId, @PathVariable String aid){
        HashMap<String, String> tokenInfo = tokenService.setToken(inviterId, aid);
        if (tokenInfo == null ) return ResultUtils.error(-2, "Fail");
        return ResultUtils.success(tokenInfo);
    }

    /**
     * 使用token登录
     * @param tid
     * @param httpReq
     * @return
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public JsonResult getTempUserInfo(@PathVariable(value = "token") String tid, HttpServletRequest httpReq){
        ShareToken shareToken = tokenService.tokenIsPresent(tid);
        if (shareToken == null) return ResultUtils.error(-2, "No such token:  " + tid);
        UserEntity tempUserInfo = tokenService.getTempUserInfo(shareToken);
        if (tempUserInfo == null) return ResultUtils.error(-2, "No such temp user, id: " +  shareToken.getTempUserId());
        HttpSession session = httpReq.getSession();
        session.setAttribute("userId", tempUserInfo.getUserId());
        session.setAttribute("name", tempUserInfo.getName());
        session.setMaxInactiveInterval(60 * 60 * 2);
        System.out.println("User login. User name: " + tempUserInfo.getName());
        return ResultUtils.success(tempUserInfo);
    }


    @RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
    public JsonResult delToken(@PathVariable(value = "token") String tid){
        boolean result = tokenService.delToken(tid);
        if (result){
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "Fail");
    }

    @Autowired
    DocParseServiceImpl docParseService;
    @RequestMapping(value = "/ssss", method = RequestMethod.GET)
    public Object de(){
        return docParseService.toWorkflowTemplate("7cfdacd1-599e-4313-badd-714be99bbbb7");
    }
}

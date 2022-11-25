package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.business.CommonUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.ShareToken;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ShareTokenRepository;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName ShareTokenService
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/12/13
 **/
@Service
public class ShareTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(ShareTokenService.class);

    @Autowired
    ProjectService projectService;
    @Autowired
    UserDao userDao;
    @Autowired
    ShareTokenRepository shareTokenRepository;

    public ArrayList<HashMap<String, String>> getAllToken(String aid, String inviterId){
        ArrayList<HashMap<String, String>> tokenInfos = new ArrayList<>();

        List<ShareToken> shareTokens = shareTokenRepository.findAllByAid(aid);
        if (shareTokens == null || shareTokens.size() == 0){
            //该活动无token，创建一个
            ShareToken shareToken = new ShareToken();
            shareToken.setTid(UUID.randomUUID().toString());
            shareToken.setAid(aid);
            shareToken.setTempUserName("TempOperator1");
            shareToken.setCreateTime(new Date());
            shareToken.setInviterId(inviterId);
            shareTokenRepository.save(shareToken);

            HashMap<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("Token", shareToken.getTid());
            tokenInfo.put("TempUserName", shareToken.getTempUserName());
            tokenInfos.add(tokenInfo);
            return tokenInfos;
        }
        //有token,则将内容返回
        for (ShareToken token : shareTokens){
            HashMap<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("Token", token.getTid());
            tokenInfo.put("TempUserName", token.getTempUserName());
            tokenInfos.add(tokenInfo);
        }
        return tokenInfos;
    }

    public HashMap<String, String> setToken(String inviterId, String aid){
        ShareToken shareToken = new ShareToken();
        shareToken.setTid(UUID.randomUUID().toString());
        shareToken.setAid(aid);
        shareToken.setCreateTime(new Date());
        shareToken.setInviterId(inviterId);
        try {
            Long tempUserNum = shareTokenRepository.countByAid(aid) + 1;
            shareToken.setTempUserName("TempOperator" + tempUserNum);
            shareTokenRepository.save(shareToken);

            HashMap<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("Token", shareToken.getTid());
            tokenInfo.put("TempUserName", shareToken.getTempUserName());
            return tokenInfo;
        }catch (MongoException e){
            LOGGER.warn("Failed to create token, mongo throw a exception: " + e);
            return null;
        }
    }

    public ShareToken tokenIsPresent(String tid){
        Optional<ShareToken> byId = shareTokenRepository.findById(tid);
        return byId.get();
    }

    public UserEntity getTempUserInfo(ShareToken shareToken){
        //有此 token，取出用户，返回给前端
        String tempUserId = shareToken.getTempUserId();
        //说明无相关联的用户，说明是第一次使用这token
        if (tempUserId == null){
            UserEntity tempUser = new UserEntity();
            tempUserId = shareToken.getTid() + UUID.randomUUID().toString();
            String tempUserName = shareToken.getTempUserName();
            tempUser.setUserId(tempUserId);
            tempUser.setName(tempUserName);
            userDao.saveLocalUser(tempUser);
            //加入项目
            String aid = shareToken.getAid();
            projectService.joinProject(aid, tempUserId);

            //更新token
            shareToken.setTempUserId(tempUserId);
            shareTokenRepository.save(shareToken);

            return tempUser;
        }
        //有此用户将其取出
        return userDao.findUserByIdOrEmail(tempUserId);
    }


    public boolean delToken(String tid){
        Optional<ShareToken> byId = shareTokenRepository.findById(tid);
        if (!byId.isPresent()) {
            LOGGER.info("No such token, default delete successfully.");
            return true;
        }
        //有此 token
        ShareToken shareToken = byId.get();
        //查看有无绑定的用户，有则退出项目并删除临时用户
        String tempUserId = shareToken.getTempUserId();
        try {
            if (tempUserId != null){
                //退出项目，并删除用户
                projectService.quitProject(shareToken.getAid(), tempUserId);
            }
            //删除token
            shareTokenRepository.delete(shareToken);
            return true;
        }catch (Exception e){
            LOGGER.warn("Failed to delete token: " + e);
            return false;
        }
    }

}

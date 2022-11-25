package cn.edu.njnu.geoproblemsolving.business;

import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.mongodb.core.query.Update;

import javax.servlet.http.Part;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @ClassName CommonUtil
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/9/13
 **/
public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);
    //存储上传的文件
    public static JsonResult fileStore(Part part, String filePath, String folderPath) {
        try {
            String fileName = filePath.substring(0, filePath.lastIndexOf("."));
            String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
            String regexp = "[^A-Za-z_0-9\\u4E00-\\u9FA5]";
            String saveName = fileName.replaceAll(regexp, "");

            File temp = new File(folderPath);
            if (!temp.exists()) {
                temp.mkdirs();
            }
            int randomNum = (int) (Math.random() * 10 + 1);
            for (int i = 0; i < 5; i++) {
                randomNum = randomNum * 10 + (int) (Math.random() * 10 + 1);
            }
            String newFileTitle = saveName + "_" + randomNum + "." + suffix;
            String localPath = temp + "/" + newFileTitle;

            File file = new File(localPath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            InputStream inputStream = part.getInputStream();
            byte[] buffer = new byte[1024 * 1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.close();
            inputStream.close();

            return ResultUtils.success(newFileTitle);
        } catch (Exception ex) {
            return ResultUtils.error(-2, ex.toString());
        }
    }

    //获取对象空字段名
    public static String[] getNullPropertyNames(Object source) {
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

    /**
     * Map ---> update
     * @param putInfo
     * @return
     */
    public static Update setUpdate(Map<String, Object> putInfo){
        Update update = new Update();
        for (Map.Entry<String, Object> item : putInfo.entrySet()){
            String key = item.getKey();
            Object value = item.getValue();
            update.set(key, value);
        }
        return update;
    }



    //============== jwtToken =====================================

    public static String createToken(HashMap<String, String> userInfo){
        String userId = userInfo.get("userId");
        String aid = userInfo.get("aid");
        String inviterId = userInfo.get("inviterId");
        return JWT.create().withAudience(userId, aid, inviterId).sign(Algorithm.HMAC256("geoProblemSolving"));
    }

    public static HashMap<String, String> getTempUserInfo(String jwtToken){
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("geoProblemSolving")).build();
            jwtVerifier.verify(jwtToken);
            try {
                DecodedJWT decodedJWT = JWT.decode(jwtToken);
                List<String> audience = decodedJWT.getAudience();
                String userId = audience.get(0);
                String aid = audience.get(1);
                HashMap<String, String> tempUserInfo = new HashMap<>();
                tempUserInfo.put("userId", userId);
                tempUserInfo.put("aid", aid);
                return tempUserInfo;
            }catch (JWTDecodeException e){
                LOGGER.warn("JWT token decode failed: " + e);
                return null;
            }

        }catch (JWTVerificationException e){
            LOGGER.info("Wrong JWT token sign.");
            return null;
        }
    }

    public static String getTokenUserId(String jwtToken){
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("geoProblemSolving")).build();
            jwtVerifier.verify(jwtToken);
            try {
                DecodedJWT decodedJWT = JWT.decode(jwtToken);
                List<String> audience = decodedJWT.getAudience();
                return audience.get(0);
            }catch (JWTDecodeException e){
                LOGGER.warn("JWT token decode failed: " + e.toString());
                return null;
            }

        }catch (JWTVerificationException e){
            LOGGER.info("Wrong JWT token sign.");
            return null;
        }
    }
}

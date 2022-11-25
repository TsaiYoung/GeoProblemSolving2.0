package cn.edu.njnu.geoproblemsolving.business.tool.markDown;

import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.resource.dao.ActivityResDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.Impl.ActivityResServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.util.RestTemplateUtil;
import cn.edu.njnu.geoproblemsolving.business.user.dao.Impl.UserDaoImpl;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import javax.net.ssl.*;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


@Service
@Slf4j
public class mdService {

    @Autowired
    UserDaoImpl userDao;

    @Autowired
    ActivityResDaoImpl activityResDao;

    @Autowired
    NodeService nodeService;

    @Value("${dataContainer}")
    String dataContainerIp;


    public JsonResult newMdFileUpload(JSONObject req){
        // 获取文件属性及内容
        String name = req.getString("name");
        String privacy = req.getString("privacy");
        String suffix = req.getString("suffix");
        String type = req.getString("type");
        String description = req.getString("description");
        String value = req.getString("value");
        String aid = req.getString("aid");
        String userId = req.getString("userId");
        String[] pathsArray = req.getString("paths").split(",");
        String graphId = req.getString("graphId");
        Long fileSize;
        // 新建临时文件
        File tempFile = null;
        FileOutputStream f1 = null;
        try {
            tempFile = File.createTempFile("new_md", suffix);
            // 读文件流
            f1 = new FileOutputStream(tempFile);
            byte[] buff = value.getBytes();//将字符串转换为字节数组
            f1.write(buff);//把字节数组的内容写进去文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 调用ActivityResController中的upload方法 上传文件
        fileSize = tempFile.length();
        String uploadName = userDao.findUserByIdOrEmail(userId).getName();
        List<ResourceEntity> resourceList = activityResDao.queryByAid(aid);
        ArrayList<String> paths = new ArrayList<>();
        for (String path : pathsArray) {
            paths.add(path);
        }
        RestTemplateUtil httpUtil = new RestTemplateUtil();
        HashSet<String> uploadUids = new HashSet<>();
        ResourceEntity res = new ResourceEntity();
        try {
            //将数据上传到数据容器
            LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
            FileSystemResource multipartFile = new FileSystemResource(tempFile);      //临时文件

//            String file = part.getSubmittedFileName();
//            String temp[] = file.split("\\.");
//            String fileName = temp[0];
//            String suffix = "." + temp[1];
//
//            File tempFile = File.createTempFile(UUID.randomUUID().toString(), suffix);//创建临时文件
//            FileUtils.copyInputStreamToFile(part.getInputStream(), tempFile);
//            // MultipartFile multipartFile = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), part.getInputStream());
//            FileSystemResource multipartFile = new FileSystemResource(tempFile);      //临时文件
//
            valueMap.add("datafile", multipartFile);
            valueMap.add("name", name);
            String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
            //向dataContainer传输数据
            JSONObject uploadRemoteResult = httpUtil.postRequestMap(uploadRemoteUrl, valueMap).getBody();
            Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
            String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
//
            if (!uploadResultInfo.equals(1)) {
//                uploadInfos.failed.add(fileName);
                valueMap.clear();
                tempFile.delete();
                return ResultUtils.error(-2,"上传数据容器失败");
            }
            tempFile.delete();
            //成功将资源上传到数据容器中
            // 接下来将资源存入GSM数据库中
            // String fileSize;
            // DecimalFormat df = new DecimalFormat("##0.00");
            // if (part.getSize() > 1024 * 1024) {
            //     fileSize = df.format((float) part.getSize() / (float) (1024 * 1024)) + "MB";
            // } else {
            //     fileSize = df.format((float) part.getSize() / (float) (1024)) + "KB";
            // }
            String uid = UUID.randomUUID().toString();
            res.setUid(uid);
            res.setName(name);
            res.setSuffix(suffix);
            res.setUploadTime(new Date());
            res.setFileSize(fileSize);
            res.setPrivacy(privacy);
            res.setType(type);
            res.setDescription(description);
            res.setFolder(false);
            res.setUserUpload(true);
            String address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
            res.setAddress(address);
            res.setUploaderId(userId);
            res.setUploaderName(uploadName);
            res.setActivityId(aid);
//            try {
//                String thumbnail = req.getParameter("thumbnail");
//                String editToolInfo = req.getParameter("editToolInfo");
//                res.setThumbnail(thumbnail);
//                res.setEditToolInfo(editToolInfo);
//            } catch (Exception ex) {
//
//            }
//
//            uploadInfos.uploaded.add(res);
            //======活动链接相关操作======================================
            //资源自动更新内容, public can auto update
            if (nodeService.nodeIsPresent(aid) != null) {
                uploadUids.add(uid);
            }
            //===================================================
            //批量上传至于最后一个资源，开始将资源存入字段中
            String rootResUid = paths.get(paths.size() - 1);
            //如果为根目录直接存入表中
            if (rootResUid.equals("0") && paths.size() == 1) {
                activityResDao.addResource(res);
            }
            //将上传的资源存入文件夹中
            List<ResourceEntity> putResList = aRes(resourceList, paths, res , "0");
            int index = 0;
            for (int i = 0; i < putResList.size(); i++) {
                ResourceEntity item = putResList.get(i);
                if (item.getUid().equals(rootResUid)) {
                    index = i;
                    break;
                }
            }

            Query query = new Query(Criteria.where("uid").is(rootResUid));
            Update update = new Update();
            update.set("children", putResList.get(index).getChildren());
            //将所上传的资源存入当前活动中
            activityResDao.updateRes(query, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultUtils.success(res);
    }

    public JsonResult updateMdFileUpload(JSONObject req){
        // 获取文件属性及内容
        String uid = req.getString("uid");
        String[] pathsArray = req.getString("paths").split(",");
        String name = req.getString("name");
        String suffix = req.getString("suffix");
        String privacy = req.getString("privacy");
        String description = req.getString("description");
        String value = req.getString("value");
        String aid = req.getString("aid");
        Long fileSize;
        // 新建临时文件
        File tempFile = null;
        FileOutputStream f1 = null;
        try {
            tempFile = File.createTempFile("new_md", suffix);
            // 读文件流
            f1 = new FileOutputStream(tempFile);
            byte[] buff = value.getBytes();//将字符串转换为字节数组
            f1.write(buff);//把字节数组的内容写进去文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileSize = tempFile.length();
        // 调用ActivityResController中的upload方法 上传文件
        ArrayList<String> paths = new ArrayList<>();
        for (String path : pathsArray) {
            paths.add(path);
        }
        RestTemplateUtil httpUtil = new RestTemplateUtil();
        String updateRes = "";
        String address = "";
        try {
            //将数据上传到数据容器
            LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
            FileSystemResource multipartFile = new FileSystemResource(tempFile);      //临时文件
            valueMap.add("datafile", multipartFile);
            valueMap.add("name", name);
            String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
            //向dataContainer传输数据
            JSONObject uploadRemoteResult = httpUtil.postRequestMap(uploadRemoteUrl, valueMap).getBody();
            Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
            String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
            address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
//
            if (!uploadResultInfo.equals(1)) {
//                uploadInfos.failed.add(fileName);
                valueMap.clear();
                tempFile.delete();
                return ResultUtils.error(-2,"上传数据容器失败");
            }
            tempFile.delete();
            // 替换数据库中的address
            ArrayList<ResourceEntity> resourceList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
            ResourceEntity putRes = new ResourceEntity();
            putRes.setUid(uid);
            putRes.setName(name);
            putRes.setDescription(description);
            putRes.setPrivacy(privacy);
            putRes.setSuffix(suffix);
            putRes.setFileSize(fileSize);
            putRes.setAddress(address);
            ArrayList<ResourceEntity> putResList = pResourceByPath(resourceList, putRes, paths);
            updateRes = updateResourceInDB(uid, putResList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (updateRes.equals("suc")) {
            return ResultUtils.success(address);
        }
        return ResultUtils.error(-2, "fail");
    }

    public JsonResult loadMarkDownFile(JSONObject md){
        // 后台解析md文件 并以txt的格式返回
        // 读取文件
        String path = md.getString("address");
        String suffix = md.getString("suffix");
        File file = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            file = File.createTempFile("net_url", suffix);

            SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
            sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
            URL url = new URL(path);
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslsession) {
//                    logger.warn("WARNING: Hostname is not matched for cert.");
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            HttpsURLConnection urlCon = (HttpsURLConnection) url.openConnection();
//            User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36
            urlCon.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
            urlCon.setConnectTimeout(6000);
            urlCon.setReadTimeout(6000);
            int code = urlCon.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new Exception("文件读取失败");
            }
            // 读文件流
            in = new DataInputStream(urlCon.getInputStream());
            out = new DataOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[2048];

            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
        } catch (Exception e) {
//            log.error("远程图片获取错误："+fileUrl);
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        // 解析markDown
        String txt = new String();
        String html = new String();
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            char[] cbconf = new char[1024];
            int count = 0;
            int readNum = 0;
            while((count = fr.read(cbconf)) > 0){
                txt = txt + new String(cbconf);
                readNum = count;
            }
            txt = TruncateTailString(txt, 1024 - readNum + 1);
            PegDownProcessor pdp = new PegDownProcessor(Integer.MAX_VALUE);
            html = pdp.markdownToHtml(txt);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //立即删除文件
            file.delete();
        }
        mdEntity mdEntity = new mdEntity();
        mdEntity.setTxt(txt);
        mdEntity.setHtml(html);
        return ResultUtils.success(mdEntity);
    }

    //从尾部开始删除字符的方法
    public static String TruncateTailString(String origin, int count) {
        if (origin == null || origin.length() < count) {
            return null;
        }
        char[] arr = origin.toCharArray();
        char[] ret = new char[arr.length - count+1];
        for (int i = 0; i < ret.length; i ++) {
            ret[i] = arr[i];
        }

        return String.copyValueOf(ret);
    }

    //将更新后资源更新到数据库中
    private String updateResourceInDB(String uid, ArrayList<ResourceEntity> putResList) {
        //putResList 含该activity 下所有资源
        Query query = new Query(Criteria.where("uid").is(uid));
        Update update = new Update();
        //遍历找到修改资源的根资源，然后进行更新
        for (int i = 0; i < putResList.size(); i++) {
            ResourceEntity item = putResList.get(i);
            if (item.getUid().equals(uid)) {
                update.set("name", item.getName());
                update.set("description", item.getDescription());
                update.set("privacy", item.getPrivacy());
                update.set("fileSize", item.getFileSize());
                update.set("address", item.getAddress());
                break;
            }
        }
        return activityResDao.updateRes(query, update);
    }

    private List<ResourceEntity> aRes(List<ResourceEntity> resList, ArrayList<String> paths, List<ResourceEntity> resources, String parent) {
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            for (ResourceEntity resource : resources) {
                resource.setParent(parent);
                resList.add(resource);
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < resList.size(); i++) {
                ResourceEntity res = resList.get(i);
                if (res.getFolder()) {
                    if (res.getUid().equals(path)) {
                        aRes(res.getChildren(), paths, resources, path);
                        resList.set(i, res);
                        break;
                    }
                } else {
                    //不是文件夹，无 children 一说，直接跳过
                    continue;
                }
            }
        }
        return resList;
    }

    //将单个资源存入
    private List<ResourceEntity> aRes(List<ResourceEntity> resList, ArrayList<String> paths, ResourceEntity res, String parent) {
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            res.setParent(parent);
            resList.add(res);
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < resList.size(); i++) {
                if (res.getFolder()) {
                    if (res.getUid().equals(path)) {
                        aRes(res.getChildren(), paths, res, path);
                        resList.set(i, res);
                        break;
                    }
                } else {
                    //不是文件夹，无 children 一说，直接跳过
                    continue;
                }
            }
        }
        return resList;
    }

    private ArrayList<ResourceEntity> pResourceByPath(ArrayList<ResourceEntity> rootResList, ResourceEntity putRes, ArrayList<String> paths) {
        //此判断条件确定如果不是根目录的话，前面不能加0
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            for (int i = 0; i < rootResList.size(); i++) {
                ResourceEntity res = rootResList.get(i);
                if (res.getUid().equals(putRes.getUid())) {
                    String[] nullPropertyNames = getNullPropertyNames(putRes);
                    BeanUtils.copyProperties(putRes, res, nullPropertyNames);
                    rootResList.remove(i);
                    rootResList.add(i, res);
                    break;
                }
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int j = 0; j < rootResList.size(); j++) {
                ResourceEntity resItem = rootResList.get(j);
                if (resItem.getUid().equals(path)) {
                    pResourceByPath(resItem.getChildren(), putRes, paths);
                    rootResList.set(j, resItem);
                }
            }
        }
        return rootResList;
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

}

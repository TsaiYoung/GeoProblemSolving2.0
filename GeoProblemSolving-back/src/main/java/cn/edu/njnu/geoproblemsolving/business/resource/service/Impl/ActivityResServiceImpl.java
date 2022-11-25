package cn.edu.njnu.geoproblemsolving.business.resource.service.Impl;

import cn.edu.njnu.geoproblemsolving.business.CommonUtil;
import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServiceImpl;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Activity;
import cn.edu.njnu.geoproblemsolving.business.activity.entity.Subproject;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.ActivityRepository;
import cn.edu.njnu.geoproblemsolving.business.activity.repository.SubprojectRepository;
import cn.edu.njnu.geoproblemsolving.business.resource.dao.ActivityResDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.IUploadResult;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.ActivityResService;
import cn.edu.njnu.geoproblemsolving.business.resource.util.RestTemplateUtil;
import cn.edu.njnu.geoproblemsolving.business.user.dao.Impl.UserDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @ClassName ResInProjectServiceImpl
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/4/20
 **/
@Service
@Slf4j
public class ActivityResServiceImpl implements ActivityResService {
    @Autowired
    UserDaoImpl userDao;

    @Autowired
    ActivityResDaoImpl activityResDao;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    SubprojectRepository subprojectRepository;

    @Autowired
    GeoAnalysisProcess geoAnalysisProcess;

    @Autowired
    NodeService nodeService;

    @Autowired
    DocParseServiceImpl docParseService;


    @Value("${dataContainer}")
    String dataContainerIp;

    @Override
    public JSONObject createFolder(String folderName, String aid, ArrayList<String> paths, String userId) {
        UserEntity creator = userDao.findUserByIdOrEmail(userId);
        ResourceEntity folder = new ResourceEntity();
        folder.setUid(UUID.randomUUID().toString());
        folder.setName(folderName);
        folder.setFolder(true);
        folder.setUploadTime(new Date());
        folder.setChildren(new ArrayList<>());
        folder.setUploaderId(userId);
        folder.setUploaderName(creator.getName());
        folder.setActivityId(aid);

        JSONObject folderJson = new JSONObject();
        folderJson.put("uid", folder.getUid());
        folderJson.put("uploadTime", folder.getUploadTime());
        folderJson.put("name", folder.getName());
        folderJson.put("folder", true);
        //若为根目录则直接存入即可
        if (paths.get(0).equals("0") && paths.size() == 1) {
            activityResDao.addResource(folder);
            return folderJson;
        }
        //不是根目录则需要通过 aid 查询 resource 然后按路径进行递归存入，然后采用更新进行更新
        List<ResourceEntity> resourceList = activityResDao.queryByAid(aid);
        ArrayList<ResourceEntity> folders = new ArrayList<>();
        folders.add(folder);
        String rootResUid = paths.get(paths.size() - 1);
        //返回的是
        List<ResourceEntity> putResList = aRes(resourceList, paths, folders, "0");
        Update update = new Update();
        Query query = new Query(Criteria.where("uid").is(rootResUid));
        for (int i = 0; i < putResList.size(); i++) {
            ResourceEntity res = putResList.get(i);
            if (res.getUid().equals(rootResUid)) {
                update.set("children", res.getChildren());
                break;
            }
        }
        String crateResult = activityResDao.updateRes(query, update);
        if (crateResult.equals("suc")) {
            return folderJson;
        }
        return null;
    }


    @Override
    public Object uploadFileInProject(HttpServletRequest req) {
        IUploadResult uploadInfos = new IUploadResult();
        //记录上传状态
        uploadInfos.failed = new ArrayList<>();
        uploadInfos.sizeOver = new ArrayList<>();
        uploadInfos.uploaded = new ArrayList<>();
        try {
            if (!ServletFileUpload.isMultipartContent(req)) {
                System.out.println("File is not multimedia.");
                return "Fail";
            }
            HttpSession session = req.getSession();
            String userId = (String) session.getAttribute("userId");
            String uploadName = userDao.findUserByIdOrEmail(userId).getName();
            String aid = req.getParameter("aid");
            String type = req.getParameter("type");
            //添加数据元数据相关操作
            HashMap<String, String> meta = null;
            if (type.equals("data")) {
                meta = new HashMap<>();
                meta.put("format", req.getParameter("format"));
                meta.put("scale", req.getParameter("scale"));
                meta.put("reference", req.getParameter("reference"));
                meta.put("unit", req.getParameter("unit"));
                meta.put("concept", req.getParameter("concept"));
            }
            List<ResourceEntity> resourceList = activityResDao.queryByAid(aid);
            String[] pathsArray = req.getParameter("paths").split(",");
            ArrayList<String> paths = new ArrayList<>();
            for (String path : pathsArray) {
                paths.add(path);
            }

            Collection<Part> parts = req.getParts();
            //多个肯定是在同一个文件夹的可以直接存入
            //确定request 中的文件数量
            int fileNum = 0;
            for (Part part : parts) {
                if (part.getName().equals("file")) {
                    fileNum++;
                }
            }

            //post payLoad存储，使用LinkedMultiValueMap<String, Object>key/value形式进行存储

            //restTemplate工具类
            RestTemplateUtil httpUtil = new RestTemplateUtil();
            HashSet<String> uploadUids = new HashSet<>();
            for (Part part : parts) {
                try {
                    if (part.getName().equals("file")) {
                        LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
                        //用于标记文件上传进度
                        fileNum--;
                        if (part.getSize() < 1024 * 1024 * 1024) {
                            //将数据上传到数据容器
                            String file = part.getSubmittedFileName();
                            String temp[] = file.split("\\.");
                            String fileName = temp[0];
                            String suffix = "." + temp[1];

                            File tempFile = File.createTempFile(UUID.randomUUID().toString(), suffix);//创建临时文件
                            FileUtils.copyInputStreamToFile(part.getInputStream(), tempFile);
                            // MultipartFile multipartFile = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), part.getInputStream());
                            FileSystemResource multipartFile = new FileSystemResource(tempFile);      //临时文件

                            valueMap.add("datafile", multipartFile);
                            valueMap.add("name", fileName);
                            String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
                            //向dataContainer传输数据
                            JSONObject uploadRemoteResult = httpUtil.postRequestMap(uploadRemoteUrl, valueMap).getBody();
                            Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
                            String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");

                            if (!uploadResultInfo.equals(1)) {
                                uploadInfos.failed.add(fileName);
                                valueMap.clear();
                                continue;
                            }
                            //成功将资源上传到数据容器中
                            // 接下来将资源存入GSM数据库中
                            // String fileSize;
                            // DecimalFormat df = new DecimalFormat("##0.00");
                            // if (part.getSize() > 1024 * 1024) {
                            //     fileSize = df.format((float) part.getSize() / (float) (1024 * 1024)) + "MB";
                            // } else {
                            //     fileSize = df.format((float) part.getSize() / (float) (1024)) + "KB";
                            // }
                            ResourceEntity res = new ResourceEntity();
                            String uid = UUID.randomUUID().toString();
                            res.setUid(uid);
                            res.setName(fileName);
                            res.setSuffix(suffix);
                            res.setUploadTime(new Date());
                            res.setFileSize(part.getSize());
                            res.setPrivacy(req.getParameter("privacy"));
                            res.setType(req.getParameter("type"));
                            res.setDescription(req.getParameter("description"));
                            res.setFolder(false);
                            res.setUserUpload(true);
                            String address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
                            res.setAddress(address);
                            res.setUploaderId(userId);
                            res.setUploaderName(uploadName);
                            res.setActivityId(aid);
                            try {
                                String thumbnail = req.getParameter("thumbnail");
                                String editToolInfo = req.getParameter("editToolInfo");
                                res.setThumbnail(thumbnail);
                                res.setEditToolInfo(editToolInfo);
                            } catch (Exception ex) {

                            }

                            uploadInfos.uploaded.add(res);
                            //======活动链接相关操作======================================
                            //资源自动更新内容, public can auto update
                            if (nodeService.nodeIsPresent(aid) != null) {
                                uploadUids.add(uid);
                            }
                            //===================================================
                            //如果不是最后一个，则进入下一次循环
                            if (fileNum != 0) {
                                continue;
                            }
                            //批量上传至于最后一个资源，开始将资源存入字段中
                            String rootResUid = paths.get(paths.size() - 1);
                            //如果为根目录直接存入表中
                            if (rootResUid.equals("0") && paths.size() == 1) {
                                for (ResourceEntity resItem : uploadInfos.uploaded) {
                                    activityResDao.addResource(resItem);
                                }
                                break;
                            }
                            //将上传的资源存入文件夹中
                            List<ResourceEntity> putResList = aRes(resourceList, paths, uploadInfos.uploaded, "0");
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

                        } else {
                            uploadInfos.sizeOver.add(part.getSubmittedFileName());
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                    uploadInfos.failed.add(part.getSubmittedFileName());
                }
            }
            //更新文档
            uploadInfos.uploadedOperation = docParseService.uploadResource(aid, uploadInfos.uploaded, meta);
            //更新当前节点,有节点的时候添加,无节点的时候不管。节点在有链接的时候创建,没有连接的时候无节点
            nodeService.addResToNodeBatch(aid, uploadUids);
            /*
            资源流动
             */
            String graphId = req.getParameter("graphId");
            geoAnalysisProcess.batchResFlowAutoUpdate(graphId, aid, uploadUids);
            return uploadInfos;

        } catch (Exception e) {
            System.out.println(e.toString());
            return "Fail";
        }
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

    @Deprecated
    @Override
    public String delResource(String aid, ArrayList<String> uids, ArrayList<String> paths) {
        //批量删除也只能删除一个文件夹内容的
        if (paths.get(0).equals("0") && paths.size() == 1) {
            for (String uid : uids) {
                activityResDao.delResource(uid);
            }
            //update document
            docParseService.removeResource(aid, new HashSet<>(uids));
            //update node
            nodeService.delResInNodeBatch(aid, new HashSet<>(uids));
            return "suc";
        }
        //不是根目录情况则需要遍历删除
        ArrayList<ResourceEntity> resInProjectList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
        String rootResUid = paths.get(paths.size() - 1);
        ArrayList<ResourceEntity> putResList = dResource(resInProjectList, uids, paths);
        Query query = new Query(Criteria.where("uid").is(rootResUid));
        Update update = new Update();

        for (int i = 0; i < putResList.size(); i++) {
            ResourceEntity item = putResList.get(i);
            if (item.getUid().equals(rootResUid)) {
                update.set("children", putResList.get(i).getChildren());
                break;
            }
        }

        //update node
        nodeService.delResInNodeBatch(aid, new HashSet<>(uids));

        //增加同步节点
        return activityResDao.updateRes(query, update);
    }

    /**
     * 删除属于活动的资源
     * 由于资源分享的存在（可让资源在各活动之间移动,由于需要判断资源是否重复,所以并未改变资源的 Id,实际上本来就是相同资源）
     * 当资源在根目录时候,资源实体直接是一个 document,若是直接以 uid 删除的话, 会把所有活动中的该资源都删除了
     * 资源存于文件夹中时,因为文件夹是属于单个活动的,所以里面的资源也是和活动绑定了
     * 支持批量删除(同一个文件夹), 仅支持同一个文件夹
     * @param aid
     * @param uids
     * @param paths
     * @return
     */
    @Override
    public String delActivityRes(String aid, ArrayList<String> uids, ArrayList<String> paths) {
        if (uids == null || uids.isEmpty()) return null;
        try {
            //处理根目录资源
            if (paths.get(0).equals("0") && paths.size() == 1){
                for(Iterator<String> it = uids.iterator(); it.hasNext();){
                    String uid = it.next();
                    activityResDao.delResource(uid, aid);
                }
            }else {
                //非根目录情况
                ArrayList<ResourceEntity> resInProjectList =  (ArrayList<ResourceEntity>)activityResDao.queryByAid(aid);
                String rootFolderUid = paths.get(paths.size() - 1);
                ArrayList<ResourceEntity> putResList = dResource(resInProjectList, uids, paths);
                Query query = new Query(Criteria.where("uid").is(rootFolderUid));
                Update update = new Update();
                for (int i = 0; i < putResList.size(); i++) {
                    ResourceEntity item = putResList.get(i);
                    if (item.getUid().equals(rootFolderUid)){
                        update.set("children", item.getChildren());
                        break;
                    }
                }
                activityResDao.updateRes(query, update);
            }
            //处理文档及节点
            docParseService.removeResource(aid, new HashSet<>(uids));
            nodeService.delResInNodeBatch(aid, new HashSet<>(uids));
            return "suc";
        }catch (Exception e){
            log.error("Failed to remove resource in activity ---> ", e.toString());
            return "fail";
        }
    }

    //提供批量删除功能
    private ArrayList<ResourceEntity> dResource(ArrayList<ResourceEntity> resInProjectList, ArrayList<String> uids, ArrayList<String> paths) {
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            ArrayList<Integer> delIndex = new ArrayList<>();
            for (int j = 0; j < resInProjectList.size(); j++) {
                ResourceEntity res = resInProjectList.get(j);
                for (int k = 0; k < uids.size(); k++) {
                    String uid = uids.get(k);
                    if (res.getUid().equals(uid)) {
                        delIndex.add(j);
                    }
                }
            }
            Collections.sort(delIndex);
            int num = 0;
            for (Integer index : delIndex) {
                resInProjectList.remove(index - num);
                num++;
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < resInProjectList.size(); i++) {
                ResourceEntity resource = resInProjectList.get(i);
                if (resource.getFolder()) {
                    if (resource.getUid().equals(path)) {
                        dResource(resource.getChildren(), uids, paths);
                    }
                } else {
                    continue;
                }
            }
        }
        return resInProjectList;

    }

    private ArrayList<ResourceEntity> dRes(ArrayList<ResourceEntity> rootRes, String uid, ArrayList<String> paths) {
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            for (int i = 0; i < rootRes.size(); i++) {
                ResourceEntity resource = rootRes.get(i);
                if (resource.getUid().equals(uid)) {
                    rootRes.remove(i);
                }
            }
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int j = 0; j < rootRes.size(); j++) {
                ResourceEntity res = rootRes.get(j);
                if (res.getFolder()) {
                    if (res.getUid().equals(path)) {
                        dRes(res.getChildren(), uid, paths);
                        rootRes.set(j, res);
                    }
                } else {
                    continue;
                }

            }
        }
        return rootRes;
    }

    //修改文件地址
    @Override
    public JsonResult changeResEntity(String aid, String uid, ArrayList<String> paths, HttpServletRequest req) throws IOException, ServletException {

        //1.根据 aid find 该项目所有资源
        //2.根据路径及 uid 查该resource
        //3.将数据上传到然后替换地址
        //4.put

        //为了增加效率，减少递归，默认肯定由此资源，不用查出来比对
        //1. 上传资源获取地址
        Part file = req.getPart("file");
        JSONObject uploadRemoteResult = uploadFileToDataContainer(file);
        Integer uploadResultCode = uploadRemoteResult.getInteger("code");
        if (uploadResultCode != 1) {
            return ResultUtils.error(-2, "Failed uploading file");
        }
        String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
        String address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
        //2. put
        ArrayList<ResourceEntity> resourceList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
        ResourceEntity putRes = new ResourceEntity();
        putRes.setUid(uid);
        putRes.setAddress(address);
        String rootResUid = paths.get(paths.size() - 1);
        ArrayList<ResourceEntity> putResList = pResourceByPath(resourceList, putRes, paths);
        String updateRes = updateResourceInDB(rootResUid, putResList);
        if (updateRes.equals("suc")) {
            return ResultUtils.success(address);
        }
        return ResultUtils.error(-2, "fail");
    }

    //将更新后资源更新到数据库中
    private String updateResourceInDB(String rootResUid, ArrayList<ResourceEntity> putResList) {
        //putResList 含该activity 下所有资源
        Query query = new Query(Criteria.where("uid").is(rootResUid));
        Update update = new Update();
        //遍历找到修改资源的根资源，然后进行更新
        for (int i = 0; i < putResList.size(); i++) {
            ResourceEntity item = putResList.get(i);
            if (item.getUid().equals(rootResUid)) {
                update.set("children", item.getChildren());
                break;
            }
        }
        return activityResDao.updateRes(query, update);
    }


    //单文件上传到数据容器
    private JSONObject uploadFileToDataContainer(Part part) throws IOException {
        String fileSubmittedFileName = part.getSubmittedFileName();
        String temp[] = fileSubmittedFileName.split("\\.");
        String fileName = temp[0];
        String suffix = "." + temp[1];

        File fileTemp = File.createTempFile(UUID.randomUUID().toString(), suffix);//创建临时文件
        FileUtils.copyInputStreamToFile(part.getInputStream(), fileTemp);
        FileSystemResource multipartFile = new FileSystemResource(fileTemp);      //临时文件

        LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("datafile", multipartFile);
        valueMap.add("name", fileName);
        String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
        //向dataContainer传输数据
        RestTemplateUtil httpUtil = new RestTemplateUtil();
        JSONObject uploadRemoteResult = httpUtil.postRequestMap(uploadRemoteUrl, valueMap).getBody();
        return uploadRemoteResult;
    }

    @Override
    public String putResourceByPath(String aid, ResourceEntity putRes, ArrayList<String> paths) {
        if (paths.size() == 1 && paths.get(0).equals("0")) {
            String delResUid = putRes.getUid();
            ResourceEntity res = activityResDao.queryByUid(delResUid);
            String[] nullPropertyNames = getNullPropertyNames(putRes);
            BeanUtils.copyProperties(putRes, res, nullPropertyNames);
            activityResDao.addResource(res);
            return "suc";
        }
        ArrayList<ResourceEntity> rootResList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
        String rootResUid = paths.get(paths.size() - 1);
        ArrayList<ResourceEntity> putResList = pResourceByPath(rootResList, putRes, paths);
        Query query = new Query(Criteria.where("uid").is(rootResUid));
        Update update = new Update();
        for (int i = 0; i < putResList.size(); i++) {
            ResourceEntity item = putResList.get(i);
            if (item.getUid().equals(rootResUid)) {
                update.set("children", item.getChildren());
                break;
            }
        }
        return activityResDao.updateRes(query, update);
    }

    /**
     * 新版资源更新
     * 支持修改资源描述对应的资源实体(address)
     *
     * @param aid
     * @param paths
     * @param putResInfo
     * @return
     */
    @Override
    public JsonResult putResourceByPath(String aid, ArrayList<String> paths, String putResInfo, HttpServletRequest request) throws IOException, ServletException {
        Part file = request.getPart("file");
        ResourceEntity putRes = JSONObject.parseObject(putResInfo, ResourceEntity.class);
        if (file != null) {
            //上传文件
            JSONObject uploadResult = uploadFileToDataContainer(file);
            Integer code = uploadResult.getInteger("code");
            if (code != 1) {
                return ResultUtils.error(-2, "Failed uploading file.");
            }
            uploadResult.getString("id");
            String fileId = uploadResult.getJSONObject("data").getString("id");
            String address = "http://" + dataContainerIp + ":8082" + "/data/" + fileId;
            // String fileSize;
            // DecimalFormat df = new DecimalFormat("##0.00");
            // long size = file.getSize();
            // if ( size> 1024 * 1024) {
            //     fileSize = df.format((float) size / (float) (1024 * 1024)) + "MB";
            // } else {
            //     fileSize = df.format((float) size / (float) (1024)) + "KB";
            // }
            putRes.setAddress(address);
            putRes.setFileSize(file.getSize());
        }
        ArrayList<ResourceEntity> rootResList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
        if (paths.size() == 1 && paths.get(0).equals("0")) {
            String delResUid = putRes.getUid();
            ResourceEntity res = activityResDao.queryByUid(delResUid);
            //获取对象的属性为空的字段
            String[] nullPropertyNames = getNullPropertyNames(putRes);
            BeanUtils.copyProperties(putRes, res, nullPropertyNames);
            activityResDao.addResource(res);
            return ResultUtils.success();
        }
        String rootResUid = paths.get(paths.size() - 1);
        ArrayList<ResourceEntity> putResList = pResourceByPath(rootResList, putRes, paths);
        //更新数据库
        String updateResult = updateResourceInDB(rootResUid, putResList);
        if (updateResult.equals("suc")) {
            return ResultUtils.success(putRes);
        }
        return ResultUtils.error(-2, "Failed to update MongoDB.");
    }

    private ArrayList<ResourceEntity> pResourceByUid(ArrayList<ResourceEntity> rootResList, ResourceEntity putRes) {
        if (rootResList.size() != 0 || rootResList != null) {
            for (int i = 0; i < rootResList.size(); i++) {
                ResourceEntity res = rootResList.get(i);
                if (res.getUid().equals(putRes.getUid())) {
                    String[] nullPropertyNames = getNullPropertyNames(putRes);
                    BeanUtils.copyProperties(putRes, res, nullPropertyNames);
                    rootResList.remove(i);
                    rootResList.add(i, res);
                    break;
                } else {
                    if (res.getFolder()) {
                        pResourceByUid(res.getChildren(), putRes);
                    } else {
                        continue;
                    }
                }
            }
        }
        return rootResList;
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

    @Override
    public ArrayList<ResourceEntity> getAllRes(String aid, ArrayList<String> paths) {
        ArrayList<ResourceEntity> resList = (ArrayList<ResourceEntity>) activityResDao.queryByAid(aid);
        return gAllRes(resList, paths);
    }

    private ArrayList<ResourceEntity> gAllRes(ArrayList<ResourceEntity> resInProjectList, ArrayList<String> paths) {
        //退出递归条件：paths 为空， 或者paths长度为1且值为“0”此情况表示为根目录
        ArrayList<ResourceEntity> resList = new ArrayList<ResourceEntity>();
        if (paths.size() == 0 || paths.get(0).equals("0")) {
            return resInProjectList;
        } else {
            String path = paths.remove(paths.size() - 1);
            for (int i = 0; i < resInProjectList.size(); i++) {
                ResourceEntity resource = resInProjectList.get(i);
                if (resource.getUid().equals(path)) {
                    resList = gAllRes(resource.getChildren(), paths);
                }
            }
        }
        return resList;
    }


    @Value("${userServerLocation}")
    String userServerLocation;

    @Override
    public JSONObject userResourceToProject(String userId, String aid, String uids, ArrayList<String> paths) {
        UserEntity sharer = userDao.findUserByIdOrEmail(userId);
        String access_token = sharer.getTokenInfo().getAccess_token();
        String sharerName = sharer.getName();
        String url = "http://" + userServerLocation + "/auth/res/s/" + uids;
        RestTemplateUtil httpUtil = new RestTemplateUtil();
        Object[] pathArray = paths.toArray();
        try {
            //在用户服务器根据 uid 查询资源
            JSONObject getResult = httpUtil.getRequestToServer(url, access_token).getBody();
            assert getResult != null;

            JSONArray fileList = getResult.getJSONArray("data");
            List<ResourceEntity> rootResList = activityResDao.queryByAid(aid);
            //用于保存传过来的资源
            ArrayList<ResourceEntity> resList = new ArrayList<>();
            HashSet<String> resIdSet = new HashSet<>();

            ArrayList<String> path = new ArrayList<>();
            for (Object item : pathArray) {
                path.add(item.toString());
            }
            //将获取到的资源转换为 gsm 的资源，并将其存入对应的项目中
            for (Object item : fileList) {
                ResourceEntity resourceEntity = JSONObject.parseObject(JSONObject.toJSONString(item), ResourceEntity.class);
                resourceEntity.setActivityId(aid);
                resourceEntity.setUploaderId(userId);
                resourceEntity.setUploaderName(sharerName);
                if (path.size() == 1 && path.get(0).equals("0")) {
                    activityResDao.addResource(resourceEntity);
                }
                resIdSet.add(resourceEntity.getUid());
                resList.add(resourceEntity);
            }
            JSONObject sharedResultJson = new JSONObject();
            sharedResultJson.put("sharedFile", resList);

            Activity activity = null;
            Optional<Activity> activityOptional = activityRepository.findById(aid);
            if (!activityOptional.isPresent()) {
                Optional<Subproject> subprojectOptional = subprojectRepository.findById(aid);
                if (subprojectOptional.isPresent()){
                    //分享到活动为subProject 可能有连接
                    activity = subprojectOptional.get();
                }
            }else {
                activity = activityOptional.get();
            }
            String graphId = null;
            if (activity != null) graphId = activity.getParent();
            if (path.size() == 1 && path.get(0).equals("0")) {
                ArrayList<HashMap<String, String>> sharedResult = docParseService.shareResource(aid, resList);
                nodeService.addResToNodeBatch(aid, resIdSet);
                sharedResultJson.put("sharedResult", sharedResult);
                geoAnalysisProcess.batchResFlowAutoUpdate(graphId, aid, resIdSet);
                return sharedResultJson;
            }
            String rootResUid = path.get(path.size() - 1);
            //如果不是根目录则存入对应的文件夹下
            List<ResourceEntity> putResList = aRes(rootResList, path, resList, "0");
            Query query = new Query(Criteria.where("uid").is(rootResUid));
            Update update = new Update();
            for (ResourceEntity item : putResList) {
                if (item.getUid().equals(rootResUid)) {
                    update.set("children", item.getChildren());
                    break;
                }
            }
            activityResDao.updateRes(query, update);

            //添加到文档 ---> 添加到节点
            ArrayList<HashMap<String, String>> sharedResult = docParseService.shareResource(aid, resList);

            nodeService.addResToNodeBatch(aid, resIdSet);
            sharedResultJson.put("sharedResult", sharedResult);
            return sharedResultJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<ResourceEntity> searchRes(String aid, String key, String value) {
        List<ResourceEntity> projectRes = activityResDao.queryByAid(aid);
        ArrayList<ResourceEntity> returnRes = Lists.newArrayList();
        if (key.equals("type")) {
            //通过 type 进行查询
            return sResByType(value, projectRes, returnRes);
        } else if (key.equals("privacy")) {
            //通过 privacy 进行查询
            return sResByPrivacy(value, projectRes, returnRes);
        } else if (key.equals("uid")) {
            //通过 privacy 进行查询
            return sResByUid(value, projectRes, returnRes);
        }
        return null;
    }

    private ArrayList<ResourceEntity> sResByType(String type, List<ResourceEntity> projectRes, ArrayList<ResourceEntity> choseRes) {
        for (int i = 0; i < projectRes.size(); i++) {
            ResourceEntity res = projectRes.get(i);
            if (res.getFolder()) {
                sResByType(type, res.getChildren(), choseRes);
            } else {
                if (res.getType().equals(type)) {
                    choseRes.add(res);
                }
            }
        }
        return choseRes;
    }

    private ArrayList<ResourceEntity> sResByPrivacy(String privacy, List<ResourceEntity> projectRes, ArrayList<ResourceEntity> choseRes) {
        for (int i = 0; i < projectRes.size(); i++) {
            ResourceEntity res = projectRes.get(i);
            if (res.getFolder()) {
                sResByType(privacy, res.getChildren(), choseRes);
            } else {
                if (res.getPrivacy().equals(privacy)) {
                    choseRes.add(res);
                }
            }
        }
        return choseRes;
    }

    private ArrayList<ResourceEntity> sResByUid(String uid, List<ResourceEntity> projectRes, ArrayList<ResourceEntity> choseRes) {
        projectRes.stream().forEach(res -> {
            if (res.getFolder()) {
                sResByType(uid, res.getChildren(), choseRes);
            } else {
                if (res.getUid().equals(uid)) {
                    choseRes.add(res);
                }
            }
        });
        return choseRes;
    }

    @Override
    public String bindResToProject(ResourceEntity res, String aid) {
        res.setActivityId(aid);
        if (res.getUid() == null || res.getUid().equals("")) {
            res.setUid(UUID.randomUUID().toString());
            res.setUploadTime(new Date());
            String fullName = res.getName();
            String name = fullName.split("\\.")[0];
            String suffix = fullName.split("\\.")[1];
            res.setSuffix("." + suffix);
            res.setUserUpload(false);
            res.setName(name);
        }
        ResourceEntity addResource = activityResDao.addResource(res);
        if (addResource == null) {
            return "fail";
        }
        //存储成功 ---> 修改文档 ---> 修改节点
        return "suc";
    }


    /**
     * 当无路径的时候使用
     * @param fromAid
     * @param toAid
     * @param resList
     * @return
     */
    @Override
    public String selectedResFlow(String fromAid, String toAid, List<ResourceEntity> resList) {
        if (resList == null ||  resList.isEmpty()) return null;
        HashSet<String> resIdSet = new HashSet<>();
        resList.forEach(item->{
            resIdSet.add(item.getUid());
        });
        if (activityResDao.addResource(resList) != null){
            docParseService.resFlow(fromAid, toAid, resIdSet);
            nodeService.addResToNodeBatch(toAid, resIdSet);
            return "suc";
        }
        return null;
    }

    @Override
    public String selectedResFlow(String fromAid, String toAid, List<ResourceEntity> resList, ArrayList<String> paths) {
        try {
            if (resList == null ||  resList.isEmpty()) return null;
            HashSet<String> resIdSet = new HashSet<>();
            resList.forEach(item->{
                resIdSet.add(item.getUid());
            });
            Activity activity = null;
            Optional<Activity> activityOptional = activityRepository.findById(toAid);
            if (!activityOptional.isPresent()) {
                Optional<Subproject> subprojectOptional = subprojectRepository.findById(toAid);
                if (subprojectOptional.isPresent()){
                    //分享到活动为subProject 可能有连接
                    activity = subprojectOptional.get();
                }
            }else {
                activity = activityOptional.get();
            }
            String graphId = null;
            if (activity != null) graphId = activity.getParent();
            if (paths.size() == 1 && paths.get(0).equals("0")) {
                for (ResourceEntity item: resList){
                    item.setActivityId(toAid);
                    activityResDao.addResource(item);
                }

                docParseService.resFlow(fromAid, toAid, resIdSet);
                nodeService.addResToNodeBatch(toAid, resIdSet);
                geoAnalysisProcess.batchResFlowAutoUpdate(graphId, toAid, resIdSet);
                return "suc";
            }
            String rootResUid = paths.get(paths.size() - 1);
            List<ResourceEntity> rootResList = activityResDao.queryByAid(fromAid);
            //如果不是根目录则存入对应的文件夹下
            List<ResourceEntity> putResList = aRes(rootResList, paths, resList, "0");
            Query query = new Query(Criteria.where("uid").is(rootResUid));
            Update update = new Update();
            for (ResourceEntity item : putResList) {
                if (item.getUid().equals(rootResUid)) {
                    update.set("children", item.getChildren());
                    break;
                }
            }
            activityResDao.updateRes(query, update);
            docParseService.resFlow(fromAid, toAid, resIdSet);
            nodeService.addResToNodeBatch(toAid, resIdSet);
            geoAnalysisProcess.batchResFlowAutoUpdate(graphId, toAid, resIdSet);
            return "suc";
        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }

    @Override
    public ArrayList<ResourceEntity> getAllFileInProject(String aid) {
        List<ResourceEntity> resList = activityResDao.queryByAid(aid);
        ArrayList<ResourceEntity> fileList = Lists.newArrayList();
        ArrayList<ResourceEntity> fileInProjectList = gallFileInProject(resList, fileList);
        return fileInProjectList;
    }

    private ArrayList<ResourceEntity> gallFileInProject(List<ResourceEntity> resInProject, ArrayList<ResourceEntity> fileList) {
        for (int i = 0; i < resInProject.size(); i++) {
            ResourceEntity res = resInProject.get(i);
            if (res.getFolder()) {
                gallFileInProject(res.getChildren(), fileList);
            } else {
                fileList.add(res);
            }
        }
        return fileList;
    }

    /**
     * 地理过程驱动
     *
     * @param aid
     * @param uid
     * @return
     */
    @Override
    public ResourceEntity getFileById(String aid, String uid) {
        List<ResourceEntity> resList = activityResDao.queryByAid(aid);
        return gFileById(resList, uid);
    }

    private ResourceEntity gFileById(List<ResourceEntity> resList, String uid) {
        for (ResourceEntity res : resList) {
            if (!res.getFolder()) {
                if (res.getUid().equals(uid)) {
                    return res;
                }
            } else {
                ArrayList<ResourceEntity> resChildren = res.getChildren();
                ResourceEntity resourceEntity = gFileById(resChildren, uid);
                if (resourceEntity != null) {
                    return resourceEntity;
                }
            }
        }
        return null;
    }


    @Override
    public ArrayList<ResourceEntity> getFilesByIds(String aid, HashSet<String> uids) {
        List<ResourceEntity> resList = activityResDao.queryByAid(aid);
        return gFilesByIds(resList, uids, new ArrayList<>());
    }

    private ArrayList<ResourceEntity> gFilesByIds(List<ResourceEntity> resList, HashSet<String> uids, ArrayList<ResourceEntity> queriedRes) {
        for (ResourceEntity res : resList) {
            if (!res.getFolder()) {
                if (uids.contains(res.getUid())) {
                    queriedRes.add(res);
                }
            } else {
                gFilesByIds(res.getChildren(), uids, queriedRes);
            }
        }
        return queriedRes;
    }


    @Override
    public ResourceEntity getFlowFolder(String aid, String folderName) {
        return activityResDao.queryByAidAndName(aid, folderName);
    }

    @Override
    public List<ResourceEntity> getAllPublicService() {
        ArrayList<ResourceEntity> allResource = (ArrayList<ResourceEntity>) activityResDao.findAll();
        List<ResourceEntity> publicResource = new ArrayList<>();
        gAllPublicResource(allResource, publicResource);
        return publicResource;
    }

    private List<ResourceEntity> gAllPublicResource(ArrayList<ResourceEntity> resource, List<ResourceEntity> publicResource) {
        for (ResourceEntity item : resource) {
            //try-catch item.getFolder() == null
            if (item.getFolder() != null && item.getFolder()) {
                if (item.getChildren() != null) {
                    gAllPublicResource(item.getChildren(), publicResource);
                }
            } else {
                if (item.getPrivacy() != null && item.getPrivacy().equals("public")) {
                    publicResource.add(item);
                }
            }
        }
        return publicResource;
    }


    @Override
    public Map<String, ArrayList<ResourceEntity>> getAllFileInProjects(HashSet<String> aids) {
        HashMap<String, ArrayList<ResourceEntity>> projectFile = new HashMap<>();
        try {
            Iterator<String> iterator = aids.iterator();
            while (iterator.hasNext()) {
                String aid = iterator.next();
                List<ResourceEntity> resList = activityResDao.queryByAid(aid);
                ArrayList<ResourceEntity> fileList = Lists.newArrayList();
                if (resList != null) gallFileInProject(resList, fileList);
                projectFile.put(aid, fileList);
            }
            return projectFile;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

    }


//    ===============================更新一些内容(重新组织一下代码) 使得资源模块更加独立,模块化一些, 仅处理与资源有关的内容=============================
//
//     @Override
//     public ResourceEntity uploadRes(Part filePart, String userId, String aid, String path) throws IOException {
//         if (filePart == null) return null;
//         InputStreamResource isResource = new InputStreamResource(filePart.getInputStream()) {
//             @Override
//             public long contentLength() {
//                 return filePart.getSize();
//             }
//
//             @Override
//             public String getFilename() {
//                 return filePart.getSubmittedFileName();
//             }
//         };
//         LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
//         String fileFullName = filePart.getSubmittedFileName();
//         String fileName;
//         try {
//             fileName = fileFullName.split("\\.")[0];
//         } catch (IndexOutOfBoundsException e) {
//             fileName = fileFullName;
//         }
//         multiValueMap.add("datafile", isResource);
//         multiValueMap.add("name", fileName);
//         RestTemplateUtil restTemplateUtil = new RestTemplateUtil();
//         String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
//         //向dataContainer传输数据
//         JSONObject uploadRemoteResult = restTemplateUtil.postRequestMap(uploadRemoteUrl, multiValueMap).getBody();
//         Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
//         String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
//
//         //上传失败
//         if (!uploadResultInfo.equals(1)) return null;
//
//         //成功将资源上传到数据容器中,将资源存入数据库
//         ResourceEntity res = new ResourceEntity();
//         String uid = UUID.randomUUID().toString();
//         res.setUid(uid);
//         res.setName(fileName);
//         res.setSuffix(suffix);
//         res.setUploadTime(new Date());
//         res.setFileSize(part.getSize());
//         res.setPrivacy(req.getParameter("privacy"));
//         res.setType(req.getParameter("type"));
//         res.setDescription(req.getParameter("description"));
//         res.setFolder(false);
//         res.setUserUpload(true);
//         String address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
//         res.setAddress(address);
//         res.setUploaderId(userId);
//         res.setUploaderName(uploadName);
//         res.setActivityId(aid);
//         try {
//             String thumbnail = req.getParameter("thumbnail");
//             String editToolInfo = req.getParameter("editToolInfo");
//             res.setThumbnail(thumbnail);
//             res.setEditToolInfo(editToolInfo);
//         } catch (Exception ex) {
//
//         }
//
//         uploadInfos.uploaded.add(res);
//         //======活动链接相关操作======================================
//         //资源自动更新内容, public can auto update
//         if (nodeService.nodeIsPresent(aid) != null) {
//             uploadUids.add(uid);
//         }
//         //===================================================
//         //如果不是最后一个，则进入下一次循环
//         if (fileNum != 0) {
//             continue;
//         }
//         //批量上传至于最后一个资源，开始将资源存入字段中
//         String rootResUid = paths.get(paths.size() - 1);
//         //如果为根目录直接存入表中
//         if (rootResUid.equals("0") && paths.size() == 1) {
//             for (ResourceEntity resItem : uploadInfos.uploaded) {
//                 activityResDao.addResource(resItem);
//             }
//             break;
//         }
//         //将上传的资源存入文件夹中
//         List<ResourceEntity> putResList = aRes(resourceList, paths, uploadInfos.uploaded, "0");
//         int index = 0;
//         for (int i = 0; i < putResList.size(); i++) {
//             ResourceEntity item = putResList.get(i);
//             if (item.getUid().equals(rootResUid)) {
//                 index = i;
//                 break;
//             }
//         }
//
//         Query query = new Query(Criteria.where("uid").is(rootResUid));
//         Update update = new Update();
//         update.set("children", putResList.get(index).getChildren());
//         //将所上传的资源存入当前活动中
//         activityResDao.updateRes(query, update);
//
//         return null;
//     }
//
//     @Override
//     public ResourceEntity uploadRes(MultipartFile filePart) throws IOException {
//         if (filePart == null) return null;
//         ByteArrayResource byteArrayResource = new ByteArrayResource(filePart.getBytes()) {
//             @Override
//             public long contentLength() {
//                 return filePart.getSize();
//             }
//
//             @Override
//             public String getFilename() {
//                 return filePart.getOriginalFilename();
//             }
//         };
//         return null;
//     }
//
//     //上传本地资源
//     @Override
//     public ResourceEntity uploadRes(String path) {
//         FileSystemResource fileSystemResource = new FileSystemResource(path);
//
//         return null;
//     }
//
//
//     private boolean setResourceToDB(HashSet<ResourceEntity> resList, ArrayList<String> paths) {
//         //批量上传至于最后一个资源，开始将资源存入字段中
//         String rootResUid = paths.get(paths.size() - 1);
//         //如果为根目录直接存入表中
//         if (rootResUid.equals("0") && paths.size() == 1) {
//             for (ResourceEntity resItem : uploadInfos.uploaded) {
//                 activityResDao.addResource(resItem);
//             }
//             break;
//         }
//         //将上传的资源存入文件夹中
//         List<ResourceEntity> putResList = aRes(resourceList, paths, uploadInfos.uploaded, "0");
//         int index = 0;
//         for (int i = 0; i < putResList.size(); i++) {
//             ResourceEntity item = putResList.get(i);
//             if (item.getUid().equals(rootResUid)) {
//                 index = i;
//                 break;
//             }
//         }
//
//         Query query = new Query(Criteria.where("uid").is(rootResUid));
//         Update update = new Update();
//         update.set("children", putResList.get(index).getChildren());
//         //将所上传的资源存入当前活动中
//         activityResDao.updateRes(query, update);
//     }

    @Override
    public ResourceEntity uploadRes(File file, Object res, String aid, ArrayList<String> paths) {
        FileSystemResource fileSystemResource = new FileSystemResource(file);
        String fileFullName = file.getName();
        String fileName;
        try {
            fileName = fileFullName.split("\\.")[0];
        } catch (IndexOutOfBoundsException e) {
            fileName = fileFullName;
        }
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("datafile", fileSystemResource);
        multiValueMap.add("name", fileName);
        RestTemplateUtil restTemplateUtil = new RestTemplateUtil();
        String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
        //向dataContainer传输数据
        JSONObject uploadRemoteResult = restTemplateUtil.postRequestMap(uploadRemoteUrl, multiValueMap).getBody();
        Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
        String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
        String address = "/data/" + dataIdInContainer;
        //上传失败
        if (!uploadResultInfo.equals(1)) return null;
        String[] nullPropertyNames = CommonUtil.getNullPropertyNames(res);
        ResourceEntity localRes = new ResourceEntity();
        BeanUtils.copyProperties(res, localRes, nullPropertyNames);
        localRes.setAddress(address);

        //存入数据库
        String rootResUid = paths.get(paths.size() - 1);
        //如果为根目录直接存入表中
        if (rootResUid.equals("0") && paths.size() == 1) {
            activityResDao.addResource(localRes);
        }else {
            List<ResourceEntity> resourceList = activityResDao.queryByAid(aid);
            //将上传的资源存入文件夹中
            List<ResourceEntity> putResList = aRes(resourceList, paths, localRes, "0");
            //获取需要更新的资源
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
        }
        return localRes;
    }
}


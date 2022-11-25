package cn.edu.njnu.geoproblemsolving.business.resource.service;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface ActivityResService {
    Object createFolder(String folderName, String aid, ArrayList<String> paths, String userId);

    Object uploadFileInProject(HttpServletRequest req);

    Object delResource(String aid, ArrayList<String> uids, ArrayList<String> paths);

    String delActivityRes(String aid, ArrayList<String> uids, ArrayList<String> paths);

    Object putResourceByPath(String aid, ResourceEntity putRes, ArrayList<String> paths);

    JsonResult putResourceByPath(String aid, ArrayList<String> paths, String putResInfo, HttpServletRequest request) throws IOException, ServletException;

    ArrayList<ResourceEntity> getAllRes(String aid, ArrayList<String> paths);

    ResourceEntity getFileById(String aid, String uid);

    JSONObject userResourceToProject(String userId, String aid, String uids, ArrayList<String> paths);

    //按条件查询项目资源，在此方法进行分流
    ArrayList<ResourceEntity> searchRes(String aid, String key, String value);

    // ArrayList<ResourceEntity> searchResByType(String aid, String type);

    Object bindResToProject(ResourceEntity res, String aid);

    String selectedResFlow(String fromAid, String toAid, List<ResourceEntity> resList);

    String selectedResFlow(String fromAid, String toAid, List<ResourceEntity> resList, ArrayList<String> paths);

    //资源权限控制
    ArrayList<ResourceEntity> getAllFileInProject(String aid);

    //获取节点的所有文件
    Map<String, ArrayList<ResourceEntity>> getAllFileInProjects(HashSet<String> aids);

    JsonResult changeResEntity(String aid, String uid, ArrayList<String> paths, HttpServletRequest req) throws IOException, ServletException;

    ResourceEntity getFlowFolder(String aid, String folderName);

    ArrayList<ResourceEntity> getFilesByIds(String aid, HashSet<String> uids);

    //获取项目项目资源资源中的所有 public
    List<ResourceEntity> getAllPublicService();

    /*
    资源模块,相关接口
     */
    /*
    上传资源,作为使用者可能是不知道这些字段的
    在上传资源的时候,只提供 address 及 uuid
    1.
     */
    // ResourceEntity uploadRes(Part filePart, String userId, String aid, String path) throws IOException;
    //
    // ResourceEntity uploadRes(MultipartFile filePart, String userId, String aid, String path);

    //本地文件上传
    ResourceEntity uploadRes(File file, Object res, String aid, ArrayList<String> paths);


}

package cn.edu.njnu.geoproblemsolving.business.resource.controller;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.Impl.ActivityResServiceImpl;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * 项目中资源，这一块属于是参与式平台自己的部分
 * @ClassName ResourceInProjectController
 * @Description 项目中资源的相关操作
 * 项目中资源与项目绑定，需要与用户解耦
 * 参数说明
 * aid: 活动 id
 * uid: 资源 id
 * paths: 文件路径 "0"代表根目录
 * Resource: 资源，包含 folder与 file
 * folder
 * file
 * <p>
 * rip = resource in project
 * @Author zhngzhng
 * @Date 2021/4/20
 **/
@RestController
@RequestMapping(value = "/rip")
public class ActivityResController {

    @Autowired
    ActivityResServiceImpl resService;

    /**
     * 获取路径下内容
     *
     * @param paths "0" 代表根目录
     * @param aid   活动id
     * @param req
     * @return
     */
    @RequestMapping(value = "/{aid}/{paths}", method = RequestMethod.GET)
    public JsonResult getAllResource(@PathVariable String aid, @PathVariable ArrayList<String> paths, HttpServletRequest req) {
        ArrayList<ResourceEntity> allRes = resService.getAllRes(aid, paths);
        return ResultUtils.success(allRes);
    }

    /**
     * 新建资源文件夹在项目中
     *
     * @param req
     * @param folderName
     * @param paths
     * @param aid
     * @return
     */
    @RequestMapping(value = "/folder", method = RequestMethod.POST)
    public JsonResult createFolder(HttpServletRequest req,
                                   @RequestParam String folderName,
                                   @RequestParam ArrayList<String> paths,
                                   @RequestParam String aid) {
        String userId = (String) req.getSession().getAttribute("userId");
        JSONObject createResult = resService.createFolder(folderName, aid, paths, userId);
        if (createResult != null) {
            return ResultUtils.success(createResult);
        } else {
            return ResultUtils.error(-2, "fail");
        }
    }


    @RequestMapping(value = "/folder", method = RequestMethod.PUT)
    public JsonResult putFolder(@RequestParam ArrayList<String> paths,
                                @RequestParam String folderId,
                                @RequestParam String aid,
                                @RequestParam String newFolderName
    ) {
        ResourceEntity res = new ResourceEntity();
        res.setName(newFolderName);
        res.setUid(folderId);
        String putResult = resService.putResourceByPath(aid, res, paths);
        if (putResult.equals("suc")) {
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }

    /**
     * 上传资源到项目中
     * 直接返回三类内容
     * uploaded， sizeOvered, failed
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    public Object uploadFile(HttpServletRequest req) {
        return resService.uploadFileInProject(req);
    }


    // @RequestMapping(value = "/file/{aid}/{paths}", method = RequestMethod.PUT)
    // public JsonResult putFile(@RequestBody ResourceEntity putRes,
    //                           @PathVariable String aid,
    //                           @PathVariable ArrayList<String> paths) {
    //     String putResult = resService.putResourceByPath(aid, putRes, paths);
    //     if (putResult.equals("suc")) {
    //         return ResultUtils.success();
    //     }
    //     return ResultUtils.error(-2, "fail");
    // }

    /**
     * 更新资源字段
     * 包含替换所对应的资源实体
     * @param aid activity id
     * @param paths
     * @param resInfo 资源需要修改的字段，必须包含uid
     * @param req
     * req.file != null,有文件上传
     * req.resInfo JSONObject 资源的字段
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "/file/{aid}/{paths}", method = RequestMethod.PUT)
    public JsonResult putFile(@PathVariable String aid,
                              @PathVariable ArrayList<String> paths,
                              @RequestParam String resInfo,
                              HttpServletRequest req) throws IOException, ServletException {
        return resService.putResourceByPath(aid, paths, resInfo, req);
    }


    /**
     * 删除资源，支持批量删除
     *
     * @param uids
     * @param aid   用于属于本项目的资源查找出来
     * @param paths
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public JsonResult delResource(@RequestParam ArrayList<String> uids,
                                  @RequestParam String aid,
                                  @RequestParam ArrayList<String> paths) {
        String delResult = resService.delActivityRes(aid, uids, paths);
        if (delResult.equals("suc")) {
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }


    /**
     * 将个人空间资源复制到活动中
     * @param req
     * @param aid 项目id
     * @param uids 资源id
     * @param paths 路径
     * @return
     */
    @RequestMapping(value = "/shareToProject/{aid}/{uids}/{paths}", method = RequestMethod.GET)
    public JsonResult resToProject(HttpServletRequest req,
                                   @PathVariable String aid,
                                   @PathVariable String uids,
                                   @PathVariable ArrayList<String> paths) {
        String userId = (String) req.getSession().getAttribute("userId");
        JSONObject shareResult = resService.userResourceToProject(userId, aid, uids, paths);
        if (shareResult != null) {
            return ResultUtils.success(shareResult);
        }
        return ResultUtils.error(-2, "fail");
    }

    @RequestMapping(value = "/file/{aid}/{key}/{value}", method = RequestMethod.GET)
    public JsonResult searchResource(@PathVariable String aid, @PathVariable String key, @PathVariable String value) {
        ArrayList<ResourceEntity> res = resService.searchRes(aid, key, value);
        return ResultUtils.success(res);
    }

    /**
     * 将资源从子活动复制到指定活动
     * @param modelOutput
     * @param aid
     * @return
     */
    @RequestMapping(value = "/file/bind/{aid}", method = RequestMethod.POST)
    public JsonResult bindProject(@RequestBody ResourceEntity modelOutput, @PathVariable String aid) {
        String bindResult = resService.bindResToProject(modelOutput, aid);
        if (bindResult.equals("suc")) {
            return ResultUtils.success();
        }
        return ResultUtils.error(-2, "fail");
    }

    /**
     * 将资源从 A 活动复制到 B 活动
     * @param resList
     * @param fromAid
     * @param toAid
     * @return
     */
    @RequestMapping(value = "/file/bind/{fromAid}/{toAid}/{paths}", method = RequestMethod.POST)
    public JsonResult shareToProject(@RequestBody List<ResourceEntity> resList,
                                     @PathVariable String fromAid,
                                     @PathVariable String toAid,
                                     @PathVariable ArrayList<String> paths){
        String result = resService.selectedResFlow(fromAid, toAid, resList, paths);
        if (result.equals("suc")) return ResultUtils.success();
        else return ResultUtils.error(-2, "fail");
    }

    /**
     * 返回 activity 中所有文件
     * 包括 privacy 及 public 内容
     *
     * @param aid
     * @return
     */
    @RequestMapping(value = "/file/all/{aid}", method = RequestMethod.GET)
    public JsonResult getAllFileInProject(@PathVariable String aid) {
        ArrayList<ResourceEntity> allFileInProject = resService.getAllFileInProject(aid);
        return ResultUtils.success(allFileInProject);
    }

    @RequestMapping(value = "/file/{aids}", method = RequestMethod.GET)
    public JsonResult getAllFileInProject(@PathVariable HashSet<String> aids){
        Map<String, ArrayList<ResourceEntity>> allFileInProjects = resService.getAllFileInProjects(aids);
        if (allFileInProjects == null) return ResultUtils.error(-2, "Fail");
        return ResultUtils.success(allFileInProjects);
    }

    /**
     * 更改资源所对应的资源实体
     * @param aid 活动id
     * @param uid 资源id
     * @param paths  文件夹路径
     * @param req parameter： "file": File
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "/file/{aid}/{uid}/{paths}", method = RequestMethod.POST)
    public JsonResult changeResEntity(@PathVariable String aid,
                                      @PathVariable String uid,
                                      @PathVariable ArrayList<String> paths,
                                      HttpServletRequest req
    ) throws IOException, ServletException {
        return resService.changeResEntity(aid, uid, paths, req);
    }

    @RequestMapping(value = "/file/allPublic", method = RequestMethod.GET)
    public JsonResult getAllPublicRes(){
        return ResultUtils.success(resService.getAllPublicService());
    }


}

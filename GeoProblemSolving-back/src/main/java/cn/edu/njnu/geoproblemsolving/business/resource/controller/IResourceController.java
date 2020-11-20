package cn.edu.njnu.geoproblemsolving.business.resource.controller;

import cn.edu.njnu.geoproblemsolving.business.resource.service.IResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping(value = "/resource")
public class IResourceController {
    @Autowired
    IResourceServiceImpl IResourceService;

    /**
     * 单文件上传
     * POST, form-data
     * @param req
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Object uploadResource(HttpServletRequest req){
        Object uploadResult = IResourceService.uploadRemote(req);
        return uploadResult;
    }

    /**
     * 单文件下载
     * GET，sourceStoreId,String
     * /resource/downloadRemote?uid=0b86cb92-4380-4075-b6bb-a9a3ac94ad07
     * @param sourceStoreId
     * @return
     */
    @RequestMapping(value = "/downloadRemote", method = RequestMethod.GET)
    public Object downloadResource(@RequestParam("sourceStoreId") String sourceStoreId){
        ResponseEntity<byte[]> responseEntity = IResourceService.downloadRemote(sourceStoreId);
        return responseEntity;
    }

    /**
     * 文件批量下载
     * GET，sourceStoreId，ArrayList<String>
     * /resource/downSomeRemote?sourceStoreIds=0b86cb92-4380-4075-b6bb-a9a3ac94ad07,52f77d35-9420-4f0f-9ad5-a678d3c58f4a
     * @param sourceStoreIds
     * @return
     */
    @RequestMapping(value = "/downSomeRemote", method = RequestMethod.GET)
    public Object downSomeResource(@RequestParam("sourceStoreIds")ArrayList<String> sourceStoreIds){
        ResponseEntity<byte[]> responseEntity = IResourceService.downSomeRemote(sourceStoreIds);
        return responseEntity;
    }

    /**
     * 删除：单个文件
     * GET，uid, String
     * /resource/deleteRemote?uid=0b86cb92-4380-4075-b6bb-a9a3ac94ad07
     * @param sourceStoreId
     * @return
     */
    @RequestMapping(value = "/deleteRemote", method = RequestMethod.GET)
    public Object deleteRemote(@RequestParam("uid") String sourceStoreId){
        String responseBody = (String)IResourceService.deleteRemote(sourceStoreId);
        return responseBody;
    }

    /**
     * 删除：多文件
     * @param sourceStoreIds
     * @return
     */
    @RequestMapping(value = "/delSomeRemote", method = RequestMethod.GET)
    public Object delSomeRemote(@RequestParam("oids") ArrayList<String> sourceStoreIds){
        Object delResult = IResourceService.delSomeRemote(sourceStoreIds);
        return delResult;
    }

}
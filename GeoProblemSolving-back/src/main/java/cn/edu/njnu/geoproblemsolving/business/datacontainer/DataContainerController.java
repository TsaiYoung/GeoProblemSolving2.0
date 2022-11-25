package cn.edu.njnu.geoproblemsolving.business.datacontainer;


import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

/**
 * @ClassName DataContainerController
 * @Description
 * @Author Zhiyi
 * @Date 2019/12/16  21:58
 * @Version 1.0.0
 */
@RestController
@RequestMapping(value = "/dataContainer")
public class DataContainerController {
    @Autowired
    DataIContainerService dataIContainerService;

    @RequestMapping(value = "/addDataItem", method = RequestMethod.POST)
    public Object addDataItem(HttpServletRequest request) throws IOException, ServletException {
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Part part = multiRequest.getPart("file");
        String header = part.getHeader("Content-Disposition");
        String filename2 = header.substring(header.indexOf("filename=\"") + 10, header.lastIndexOf("\""));//filename=" (整个字符串长度为10，所以要加10)
        // 获取文件名
        String fileName = part.getName();
        File file = File.createTempFile(part.getName(), "." + FilenameUtils.getExtension(filename2));//创建临时文件
        FileUtils.copyInputStreamToFile(multiRequest.getPart("file").getInputStream(), file);
        return dataIContainerService.addDataItem(file);
    }

    @RequestMapping(value = "/download/{uid}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@PathVariable("uid") String uid) {
        return dataIContainerService.download(uid);
    }

    //    无需配置文件的上传接口
    @RequestMapping(value = "/uploadSingle", method = RequestMethod.POST)
    public JsonResult uploadData(HttpServletRequest request) throws IOException, ServletException {
        String userId = (String) request.getSession().getAttribute("userId");
        String userName = (String) request.getSession().getAttribute("name");
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Part part = multiRequest.getPart("file");
        String header = part.getHeader("Content-Disposition");
        String filename2 = header.substring(header.indexOf("filename=\"") + 10, header.lastIndexOf("\""));//filename=" (整个字符串长度为10，所以要加10)
        //  获取文件后缀名
        String suffix = "." + FilenameUtils.getExtension(filename2);
        File file = File.createTempFile(part.getName(), suffix);//创建临时文件
        FileUtils.copyInputStreamToFile(part.getInputStream(), file);
        return ResultUtils.success(dataIContainerService.upload(file,userId,userName));
    }

    @RequestMapping(value = "/dataService/getData", method = RequestMethod.POST)
    public JsonResult getDataService(@RequestBody JSONObject jsonObject) {
        String id = jsonObject.getString("serverId");
        String token = jsonObject.getString("token");
        String type = jsonObject.getString("type");
        return ResultUtils.success( dataIContainerService.getDataService(id,token,type));
    }

    @RequestMapping(value = "/remote", method = RequestMethod.POST)
    public JsonResult getRemoteData(@RequestBody JSONObject jsonObject) {
        String id = jsonObject.getString("serverId");
        String token = jsonObject.getString("token");
        return ResultUtils.success( dataIContainerService.getRemoteData(id,token));
    }



    @RequestMapping(value = "/invoke", method = RequestMethod.POST)
    public JsonResult invoke(@RequestBody JSONObject jsonObject) {
        return ResultUtils.success( dataIContainerService.invoke(jsonObject));
    }



    /*
    数据方法初始化接口
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public JsonResult initDataService(@PathVariable String token){
        Integer statusCode = dataIContainerService.initDataService(token);
        if (statusCode != 0){
            return ResultUtils.error(-2, "node offline");
        }
        return ResultUtils.success();
    }

}

package cn.edu.njnu.geoproblemsolving.business.tool.markDown;

import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


@CrossOrigin(origins = "*",allowCredentials = "true")
@RestController
@RequestMapping("/md")
public class mdController {

    @Value("${dataContainer}")
    String dataContainerIp;

    @Autowired
    mdService mdService;

    @ApiOperation(value = "新建md文件")
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public Object uploadFile(@RequestBody JSONObject req) {
        return mdService.newMdFileUpload(req);
    }

    @ApiOperation(value = "更新md文件")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Object updateFile(@RequestBody JSONObject req) {
        return mdService.updateMdFileUpload(req);
    }

    @ApiOperation(value = "读取md文件")
    @RequestMapping(value = "/loadMarkDown", method = RequestMethod.POST)
    public JsonResult loadMarkDown(@RequestBody JSONObject md) {
        return mdService.loadMarkDownFile(md);
    }
}

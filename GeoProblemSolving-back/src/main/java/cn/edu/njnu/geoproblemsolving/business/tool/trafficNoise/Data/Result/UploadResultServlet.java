package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Result;

//import cn.edu.njnu.trafficNoiseCaculating.UserData.Domain.EDataType;

import cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao.fileUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao.prepareData.*;


@RestController
@RequestMapping(value = "/uploadResultServlet")
public class UploadResultServlet extends HttpServlet {

    final static String RESULT_FILE_NAME = "NoiseResult";

    @RequestMapping(method = RequestMethod.POST)
    protected String doPost(@RequestBody String data, HttpServletRequest req) throws ServletException, IOException {

        JSONObject respJson = new JSONObject();

        respJson.put("respCode", 0);

        // 解析shapefile压缩包
        try {
            JSONObject map = JSONObject.parseObject(data);
            String address = map.getString("address");
            String name = map.getString("name");
            String suffix = map.getString("suffix");
            // 获取文件
            String id = map.getString("uid");
            String zipUrl = "data" + File.separator + "TrafficNoise" + File.separator + id + File.separator;
            String localDir = req.getServletContext().getRealPath("./") + zipUrl;
            File file = new File(localDir);
            if (!file.exists()) {
                file.mkdirs();
            } else {
                fileUtils.delAllFiles(localDir);
            }

            fileUtils.downloadFileFromURL(address, localDir, name + suffix);
            fileUtils.upZipFile(localDir + name + suffix, localDir);

            respJson.put("respCode", 1);
            respJson.put("url", "\\GeoProblemSolving\\" + zipUrl + RESULT_FILE_NAME + ".tif");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return respJson.toString();
    }

    @RequestMapping(method = RequestMethod.GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

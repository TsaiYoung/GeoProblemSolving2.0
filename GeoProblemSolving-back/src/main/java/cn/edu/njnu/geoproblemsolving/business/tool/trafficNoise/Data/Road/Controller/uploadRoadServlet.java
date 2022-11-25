package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Road.Controller;

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
import java.io.*;

import static cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao.prepareData.*;


@RestController
@RequestMapping(value = "/uploadRoadServlet")
public class uploadRoadServlet extends HttpServlet {

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

            JSONObject dataInfo = prepareRoadData(localDir + "RoadCenterLine");
            copyDbfFile(localDir + "RoadCenterLine");
            if (dataInfo.size() > 0) {
                respJson.put("respCode", 1);
                double centerLong = dataInfo.getDouble("Lng");
                double centerLat = dataInfo.getDouble("Lat");
                double roadMaxID = dataInfo.getInteger("maxID");
                respJson.put("roadMaxID", roadMaxID);
                respJson.put("centerLong", centerLong);
                respJson.put("centerLat", centerLat);
                respJson.put("url", "\\GeoProblemSolving\\" + zipUrl + "RoadCenterLine");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return respJson.toString();
    }

    @RequestMapping(method = RequestMethod.GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

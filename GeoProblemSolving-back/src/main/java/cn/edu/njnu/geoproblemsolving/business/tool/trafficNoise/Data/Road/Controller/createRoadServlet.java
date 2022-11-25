package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Road.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.gdal.ogr.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import static cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao.prepareData.copyDbfFile;
import static cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao.shapefileUtil.getDataSource;


@RestController
@RequestMapping(value = "/createRoadServlet")
public class createRoadServlet extends HttpServlet {
    final static String ROAD_FILE_NAME = "RoadCenterLine";

    @RequestMapping(method = RequestMethod.POST)
    protected String doPost(HttpServletRequest req) throws ServletException, IOException {

        JSONObject respJson = new JSONObject();
        respJson.put("respCode", 0);
        respJson.put("msg", "failed.");

        String userId = req.getParameter("userId");
        String roadDataId = req.getParameter("roadDataId");

        String prop = req.getParameter("prop");
        JSONObject roadProp = JSONObject.parseObject(prop);
//        获取点集的JSON数组
        JSONArray pointsArr = JSONArray.parseArray(req.getParameter("prjPoint"));

        String dataDir = req.getServletContext().getRealPath("./") + "data" + File.separator + "userData" + File.separator + userId + File.separator + "InputData" + File.separator + ROAD_FILE_NAME + File.separator + roadDataId + File.separator;
        String roadFile = dataDir + ROAD_FILE_NAME + ".shp";

        DataSource dSource = getDataSource(roadFile);
        if (dSource == null) {
            return respJson.toString();
        }
        Layer layer = dSource.GetLayerByIndex(0);
        if (layer == null) {
            return respJson.toString();
        }

        FeatureDefn oDefn = layer.GetLayerDefn();
        Feature tempFeature = new Feature(oDefn);

        Geometry polyline = new Geometry(ogr.wkbLineString);
        for(int i=0,l=pointsArr.size();i<l;i++){
            JSONArray point = JSONArray.parseArray(pointsArr.getJSONObject(i).toJSONString());
            polyline.AddPoint_2D(Double.parseDouble(point.get(0).toString()), Double.parseDouble(point.get(1).toString()));
        }
        tempFeature.SetGeometry(polyline);
        layer.CreateFeature(tempFeature);

//        长度保留3位小数防止写入属性表超长
        DecimalFormat df = new DecimalFormat("#.000");
        double length = Double.parseDouble(df.format(polyline.Length()));

        tempFeature.SetField("OBJECTID", roadProp.getInteger("OBJECTID"));
        tempFeature.SetField("Width", roadProp.getDouble("Width"));
        tempFeature.SetField("M", roadProp.getDouble("M"));
        tempFeature.SetField("P", roadProp.getDouble("P"));
        tempFeature.SetField("CarSpeed", roadProp.getDouble("CarSpeed"));
        tempFeature.SetField("TruckSpeed", roadProp.getDouble("TruckSpeed"));
        tempFeature.SetField("HasReflect", roadProp.getInteger("HasReflect"));
        tempFeature.SetField("RefHeight", roadProp.getDouble("RefHeight"));
        tempFeature.SetField("RefWidth", roadProp.getDouble("RefWidth"));
        tempFeature.SetField("RefAbsorb", roadProp.getInteger("RefAbsorb"));
        tempFeature.SetField("Slope", roadProp.getDouble("Slope"));
        tempFeature.SetField("Surface", roadProp.getInteger("Surface"));
        tempFeature.SetField("Length", length);

        layer.SyncToDisk();
        layer.delete();
        dSource.SyncToDisk();
        dSource.delete();

//              因为玄武盾的原因，需要拷贝一份mdbf文件
        copyDbfFile(dataDir + ROAD_FILE_NAME);

        respJson.put("respCode", 1);
        respJson.put("msg", "success.");
        return respJson.toString();
    }

    @RequestMapping(method = RequestMethod.GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Controller;

//import cn.edu.njnu.trafficNoiseCaculating.UserData.Domain.EDataType;

import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServe;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.resource.dao.ActivityResDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.util.RestTemplateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
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
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Model.Service.runModelService.genBoundBoxUdxData;


@RestController
@RequestMapping(value = "/exportRegionServlet")
public class exportRegionServlet extends HttpServlet {

    @Autowired
    ActivityResDaoImpl ripDao;

    @Autowired
    GeoAnalysisProcess geoAnalysisProcess;

    @Value("${dataContainer}")
    String dataContainerIp;

    @Autowired
    DocParseServe docParseServe;

    @Autowired
    NodeService nodeService;

    @RequestMapping(method = RequestMethod.POST)
    protected String doPost(@RequestBody String data, HttpServletRequest req) throws ServletException, IOException {

        JSONObject respJson = new JSONObject();
        respJson.put("respCode", 0);

        // 解析shapefile压缩包
        try {
            JSONObject map = JSONObject.parseObject(data);
            String aid = map.getString("aid");
            String graphId = map.getString("graphId");
            String top = map.getString("top");
            String bottom = map.getString("bottom");
            String right = map.getString("right");
            String left = map.getString("left");

            String toolId = map.getString("toolId");
            JSONArray participant = map.getJSONArray("participant");


            String resultId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            String zipUrl = "data" + File.separator + "TrafficNoise" + File.separator + resultId + File.separator;
            String resultDir = req.getServletContext().getRealPath("./") + zipUrl;
            new File(resultDir).mkdirs();

            String boundBoxValue = left + ", " + bottom + ", " + right + ", " + top;
            genBoundBoxUdxData(resultDir, boundBoxValue);

            File regionBBoxUdxFile = new File(resultDir + "udx_xml_RegionBBox.xml");

            RestTemplateUtil httpUtil = new RestTemplateUtil();
            if (regionBBoxUdxFile.exists()) {
                LinkedMultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
                String fileTemp = resultDir + "udx_xml_RegionBBox.xml";
                FileSystemResource multipartFile = new FileSystemResource(fileTemp);      //临时文件
                valueMap.add("datafile", multipartFile);
                valueMap.add("name", "udx_xml_RegionBBox");
                String uploadRemoteUrl = "http://" + dataContainerIp + ":8082/data";
                //向dataContainer传输数据
                JSONObject uploadRemoteResult = httpUtil.postRequestMap(uploadRemoteUrl, valueMap).getBody();

                Integer uploadResultInfo = uploadRemoteResult.getInteger("code");
                String dataIdInContainer = uploadRemoteResult.getJSONObject("data").getString("id");
                if (!uploadResultInfo.equals(1)) {
                    respJson.put("respCode", 0);
                    respJson.put("msg", "failed.");
                    return respJson.toJSONString();
                }
                String address = "http://" + dataContainerIp + ":8082" + "/data/" + dataIdInContainer;
                // 将资源保存至活动中
                ResourceEntity outputEntity = new ResourceEntity();
                String uid = UUID.randomUUID().toString();
                outputEntity.setUserUpload(false);
                outputEntity.setUid(uid);
                outputEntity.setName("udx_xml_RegionBBox");
                outputEntity.setSuffix(".xml");
                outputEntity.setType("data");
                outputEntity.setUploadTime(new Date());
                outputEntity.setPrivacy("private");
                outputEntity.setAddress(address);
                outputEntity.setActivityId(aid);
                outputEntity.setDescription("Traffic noise simulation area.");
                outputEntity.setFolder(false);
                ripDao.addResource(outputEntity);


                //文档相关操作
                HashSet<String> userIds = new HashSet<>();
                for (Object item : participant){
                    JSONObject userInfo = JSONObject.parseObject(JSONObject.toJSONString(item));
                    userIds.add(userInfo.getString("userId"));
                }
                // String oid = docParser.geoAnalysisNoInput(aid, toolId, userIds, "Data processing", outputEntity);
                String oid = docParseServe.geoAnalysisRLS90(aid, toolId, userIds, "Data processing", null, outputEntity);
                //保存到节点中
                nodeService.addResToNode(aid, uid);

                //资源自动更新
                if(graphId != null && graphId != ""){
                    geoAnalysisProcess.batchResFlowAutoUpdate(graphId, aid, uid);
                }
                respJson.put("respCode", 1);
                respJson.put("path", address);
                respJson.put("operationId", oid);
            } else {
                respJson.put("respCode", 0);
                respJson.put("msg", "failed.");
                return respJson.toJSONString();
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

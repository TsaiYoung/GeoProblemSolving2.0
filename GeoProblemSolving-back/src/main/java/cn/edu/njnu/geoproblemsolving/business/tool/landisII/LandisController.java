package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServe;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.resource.service.ActivityResService;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @ClassName LandisController
 * @Description Landis 工具
 * @Author zhngzhng
 * @Date 2022/3/2
 **/
@RestController
@RequestMapping(value = "/landis")
public class LandisController {
    @Autowired
    LandisIIService landisServer;
    @Autowired
    ActivityResService activityResService;

    @Autowired
    DocParseServe docParseServe;

    @Autowired
    NodeService nodeService;

    @Autowired
    GeoAnalysisProcess geoAnalysisProcess;

    @PostMapping(value = "/biomass")
    public JsonResult generateBiomassSuccession(@RequestBody JSONObject map, HttpServletRequest req) {
        JSONArray participant = map.getJSONArray("participant");
        LandisBiomassSuccession biomassSuccession = map.getObject("biomassSuccession", LandisBiomassSuccession.class);
        String userId = (String)req.getSession().getAttribute("userId");
        if (userId == null) return ResultUtils.error(-2, "Offline");
        String aid = map.getString("aid");
        String toolId = map.getString("toolId");
        String graphId = map.getString("graphId");
        File file = landisServer.writeBiomassSuccession(biomassSuccession);
        if (file == null) return ResultUtils.error(-2, "Failed to write file.");
        ResourceEntity outputEntity = new ResourceEntity();
        String[] fileName = file.getName().split("\\.");
        outputEntity.setName(fileName[0]);
        outputEntity.setSuffix('.' + fileName[1]);
        outputEntity.setActivityId(aid);
        outputEntity.setFolder(false);
        outputEntity.setFileSize(file.length());
        outputEntity.setActivityId(aid);
        outputEntity.setUserUpload(false);
        outputEntity.setUploaderId(userId);
        outputEntity.setDescription("Species for Landis II model.");
        ArrayList<String> paths = new ArrayList<>();
        paths.add("0");
        ResourceEntity localRes = activityResService.uploadRes(file, outputEntity, aid, paths);
        if (localRes == null) return ResultUtils.error(-2, "Failed to save in db.");

        JSONObject operationResult = landisServer.docNodeAndFlow(aid, toolId, graphId, localRes, participant);
        if (operationResult == null) return ResultUtils.error(-2, "Failed to operation file");
        return ResultUtils.success(operationResult);
    }

    @PostMapping(value = "/species")
    public JsonResult generateSpecies(@RequestBody JSONObject map, HttpServletRequest req) {
        JSONArray participant = map.getJSONArray("participant");
        ArrayList species = map.getObject("species", ArrayList.class);
        ArrayList<LandisSpecies> landisSpecies = new ArrayList<>();
        for (Object item : species) {
            landisSpecies.add(JSONObject.parseObject(JSONObject.toJSONString(item), LandisSpecies.class));
        }
        String userId = (String)req.getSession().getAttribute("userId");
        if (userId == null) return ResultUtils.error(-2, "Offline");
        String aid = map.getString("aid");
        String toolId = map.getString("toolId");
        String graphId = map.getString("graphId");
        File file = landisServer.writeSpeciesFile(landisSpecies);
        if (file == null) return ResultUtils.error(-2, "Failed to write file.");
        ResourceEntity outputEntity = new ResourceEntity();
        String[] fileName = file.getName().split("\\.");
        outputEntity.setName(fileName[0]);
        outputEntity.setSuffix('.' + fileName[1]);
        outputEntity.setActivityId(aid);
        outputEntity.setFolder(false);
        outputEntity.setFileSize(file.length());
        outputEntity.setActivityId(aid);
        outputEntity.setUserUpload(false);
        outputEntity.setUploaderId(userId);
        outputEntity.setDescription("Species for Landis II model.");
        ArrayList<String> paths = new ArrayList<>();
        paths.add("0");
        ResourceEntity localRes = activityResService.uploadRes(file, outputEntity, aid, paths);
        if (localRes == null) return ResultUtils.error(-2, "Failed to save in db.");
        JSONObject operationResult = landisServer.docNodeAndFlow(aid, toolId, graphId, localRes, participant);
        if (operationResult == null) return ResultUtils.error(-2, "Failed to operation file");
        return ResultUtils.success(operationResult);
    }

    @PostMapping(value = "/climateConfig")
    public JsonResult generateClimateConfig(@RequestBody JSONObject map, HttpServletRequest req) {
        JSONArray participant = map.getJSONArray("participant");
        LandisClimateConfig climateConfig = map.getObject("climateConfig", LandisClimateConfig.class);
        String userId = (String)req.getSession().getAttribute("userId");
        if (userId == null) return ResultUtils.error(-2, "Offline");
        String aid = map.getString("aid");
        String toolId = map.getString("toolId");
        String graphId = map.getString("graphId");
        File file = landisServer.writeClimateConfig(climateConfig);
        if (file == null) return ResultUtils.error(-2, "Failed to write file.");
        ResourceEntity outputEntity = new ResourceEntity();
        String[] fileName = file.getName().split("\\.");
        outputEntity.setName(fileName[0]);
        outputEntity.setSuffix('.' + fileName[1]);
        outputEntity.setActivityId(aid);
        outputEntity.setFolder(false);
        outputEntity.setFileSize(file.length());
        outputEntity.setActivityId(aid);
        outputEntity.setUserUpload(false);
        outputEntity.setUploaderId(userId);
        outputEntity.setDescription("Climate config for Landis II model.");
        ArrayList<String> paths = new ArrayList<>();
        paths.add("0");
        ResourceEntity localRes = activityResService.uploadRes(file, outputEntity, aid, paths);
        if (localRes == null) return ResultUtils.error(-2, "Failed to save in db.");
        JSONObject operationResult = landisServer.docNodeAndFlow(aid, toolId, graphId, localRes, participant);
        if (operationResult == null) return ResultUtils.error(-2, "Failed to operation file");
        return ResultUtils.success(operationResult);
    }

    @PostMapping(value = "/dynamicInput")
    public JsonResult generateDynamicInput(@RequestBody JSONObject map, HttpServletRequest req) {
        JSONArray participant = map.getJSONArray("participant");
        ArrayList dynamicInput = map.getObject("dynamicInput", ArrayList.class);
        ArrayList<LandisDynamicInput> dynamicInputs = new ArrayList<>();
        for (Object item : dynamicInput) {
            dynamicInputs.add(JSONObject.parseObject(JSONObject.toJSONString(item), LandisDynamicInput.class));
        }
        String userId = (String)req.getSession().getAttribute("userId");
        if (userId == null) return ResultUtils.error(-2, "Offline");
        String aid = map.getString("aid");
        String toolId = map.getString("toolId");
        String graphId = map.getString("graphId");
        File file = landisServer.writeDynamicData(dynamicInputs);
        if (file == null) return ResultUtils.error(-2, "Failed to write file.");
        ResourceEntity outputEntity = new ResourceEntity();
        String[] fileName = file.getName().split("\\.");
        outputEntity.setName(fileName[0]);
        outputEntity.setSuffix('.' + fileName[1]);
        outputEntity.setActivityId(aid);
        outputEntity.setFolder(false);
        outputEntity.setFileSize(file.length());
        outputEntity.setActivityId(aid);
        outputEntity.setUserUpload(false);
        outputEntity.setUploaderId(userId);
        outputEntity.setDescription("Biomass succession dynamic inputs for Landis II model.");
        ArrayList<String> paths = new ArrayList<>();
        paths.add("0");
        ResourceEntity localRes = activityResService.uploadRes(file, outputEntity, aid, paths);
        if (localRes == null) return ResultUtils.error(-2, "Failed to save in db.");
        JSONObject operationResult = landisServer.docNodeAndFlow(aid, toolId, graphId, localRes, participant);
        if (operationResult == null) return ResultUtils.error(-2, "Failed to operation file");
        return ResultUtils.success(operationResult);
    }
}

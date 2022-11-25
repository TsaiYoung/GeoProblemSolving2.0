package cn.edu.njnu.geoproblemsolving.business.tool.generalTool.service;

import cn.edu.njnu.geoproblemsolving.business.CommonUtil;
import cn.edu.njnu.geoproblemsolving.business.collaboration.compute.entity.ToolTestData;
import cn.edu.njnu.geoproblemsolving.business.modeltask.Utils;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.dao.impl.ToolDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.Tool;
import cn.edu.njnu.geoproblemsolving.business.tool.generalTool.entity.ToolSetVo;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @ClassName ToolServiceImpl
 * @Description
 * @Author zhngzhng
 * @Date 2021/5/13
 **/
@Service
public class ToolServiceImpl implements ToolService {
    @Autowired
    ToolDaoImpl toolDao;

    @Autowired
    RestTemplate restTemplate;

    @Value("${portalLocation}")
    String portalIpAndPort;

    @Value("${dataMethodProxyServerLocation}")
    String proxyServerLocation;

    /**
     * 通过userId 在门户获取计算模型
     *
     * @param page
     * @return
     */
    @Override
    public List<JSONObject> getComputeModelByUserEmail(String email, int page, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("user-agent", "geoProblemSolving");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        String url = "http://" + portalIpAndPort + "/computableModel/searchDeployedModelByUser?page=" + page
                + "&size=" + size + "&asc=-1&searchText=&userName=" + email;
        ResponseEntity<JSONObject> getResult = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        JSONObject resultData = getResult.getBody().getJSONObject("data");
        int computableModelNum = resultData.getInteger("total");
        JSONArray computableArrays = resultData.getJSONArray("content");
        List<JSONObject> computableModelList = computableArrays.toJavaList(JSONObject.class);
        //只需要返回部分特定内容即可
        return computableModelList;
    }

    @Override
    public List<JSONObject> getPublicComputeModel(int page, int size) {
        String url = "http://" + portalIpAndPort + "/computableModel/searchDeployedModel?page=" + page + "&size=" + size + "&asc=-1&searchText=";
        HttpHeaders headers = new HttpHeaders();
        headers.set("user-agent", "geoProblemSolving");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        JSONObject getResult = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        // JSONObject getResult = restTemplate.getForEntity(url, JSONObject.class).getBody();
        JSONArray jsonArray = getResult.getJSONObject("data").getJSONArray("content");
        if (jsonArray == null) {
            return null;
        }
        return jsonArray.toJavaList(JSONObject.class);
    }


    @Override
    public JSONObject queryPublicComputeModelByName(String searchText, int page) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("user-agent", "geoProblemSolving");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        String url = "http://" + portalIpAndPort + "/computableModel/searchDeployedModel?page=" + page
                + "&size=20&asc=-1&searchText=" + searchText;
        //默认 pageSize = 20
        JSONObject getResult = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        // JSONObject getResult = restTemplate.getForEntity(url, JSONObject.class).getBody();
        return getResult.getJSONObject("data");
    }

    @Override
    public List<JSONObject> getDataMethodByUserEmail(String email, int page, int size) {
        //第一步通过通过email获取用户oid
        HttpHeaders headers = new HttpHeaders();
        headers.set("user-agent", "geoProblemSolving");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        String email2OidUrl = "http://" + portalIpAndPort + "/user/getOidByEmail?email=" + email;
        Integer oid = restTemplate.exchange(email2OidUrl, HttpMethod.GET, httpEntity, JSONObject.class).getBody().getInteger("data");
        // Integer oid = restTemplate.getForEntity(email2OidUrl, JSONObject.class).getBody().getInteger("data");
        //此用户从未在门户登录过，故门户无此用户
        if (oid == null) {
            return null;
        }
        String userDataMethodUrl = "http://" + portalIpAndPort + "/dataApplication/getApplication?userOid=" + oid +
                "&page=" + page + "&pagesize=" + size + "&asc=-1&type=";
        JSONObject getResult = restTemplate.exchange(userDataMethodUrl, HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        // JSONObject getResult = restTemplate.getForEntity(userDataMethodUrl, JSONObject.class).getBody();
        JSONObject publicModelJson = getResult.getJSONObject("data");
        if (publicModelJson.getBooleanValue("empty")) {
            return null;
        }
        List<JSONObject> userDataMethodList = publicModelJson.getJSONArray("content").toJavaList(JSONObject.class);
        return userDataMethodList;
    }

    @Override
    public List<JSONObject> getPublicDataMethod(int page, int size) {
        JSONObject jsonObject = queryDataMethodReq("", "", page, size);
        JSONArray dataMethodArray = jsonObject.getJSONArray("list");
        if (dataMethodArray == null) {
            return null;
        }
        return dataMethodArray.toJavaList(JSONObject.class);
    }


    @Override
    public JSONObject queryPublicDataMethodByName(String searchText, int page) {
        //查询，返回
        return queryDataMethodReq(searchText, "", page, 5);
    }

    private JSONObject queryDataMethodReq(String searchText, String methodType, int page, int size) {
        String url = "http://" + portalIpAndPort + "/dataApplication/methods/getApplicationInvokable";
        JSONObject searchJson = new JSONObject();
        searchJson.put("asc", false);
        searchJson.put("curQueryField", "name");
        searchJson.put("searchText", searchText);
        searchJson.put("method", methodType);
        searchJson.put("page", page);
        searchJson.put("pageSize", size);
        HttpHeaders headers = new HttpHeaders();
        headers.set("user-agent", "geoProblemSolving");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(searchJson, headers);
        try {
            ResponseEntity<JSONObject> searchRes = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
            //集合类型相同地址
            ArrayList dataMethodArray = searchRes.getBody().getJSONObject("data").getObject("list", ArrayList.class);
            if (dataMethodArray != null && dataMethodArray.size() != 0){
                for (int i = dataMethodArray.size() - 1; i >= 0; i--){
                    JSONObject dataMethodJson = JSONObject.parseObject(JSONObject.toJSONString(dataMethodArray.get(i)));
                    if (dataMethodJson.get("invokeServices") == null){
                        dataMethodArray.remove(i);
                    }
                }
            }
            return searchRes.getBody().getJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public JSONObject getComputableModelList(String email, int page, int size) {
        List<JSONObject> userModelList = getComputeModelByUserEmail(email, page, size);
        List<JSONObject> publicModelList = getPublicComputeModel(page, size);
        JSONObject modelJson = new JSONObject();
        modelJson.put("userModel", userModelList);
        modelJson.put("publicModel", publicModelList);
        return modelJson;
    }

    @Override
    public JSONObject getDataMethodList(String email, int page, int size) {
        List<JSONObject> userDataMethod = getDataMethodByUserEmail(email, page, size);
        List<JSONObject> publicDataMethod = getPublicDataMethod(page, size);
        JSONObject modelJson = new JSONObject();
        modelJson.put("userMethod", userDataMethod);
        modelJson.put("publicMethod", publicDataMethod);
        return modelJson;
    }


    @Override
    public Tool createTool(JSONObject toolJson) throws UnsupportedEncodingException {
        Tool tool = new Tool();
        String tid = UUID.randomUUID().toString();
        String toolName = toolJson.getString("toolName");
        String description = toolJson.getString("description");
        String provider = toolJson.getString("provider");
        String privacy = toolJson.getString("privacy");
        String scope = toolJson.getString("scope");
        JSONArray recomSteps = toolJson.getJSONArray("recomStep");
        String toolImg = toolJson.getString("toolImg");
        ArrayList<String> recommendation = Lists.newArrayList();
        if (recomSteps != null) {
            for (int i = 0; i < recomSteps.size(); i++) {
                String recomStep = (String) recomSteps.get(i);
                recommendation.add(recomStep);
            }
        }
        JSONArray categoryTags = toolJson.getJSONArray("categoryTag");
        ArrayList<String> tags = Lists.newArrayList();
        if (categoryTags != null) {
            for (int j = 0; j < categoryTags.size(); j++) {
                String tag = (String) categoryTags.get(j);
                tags.add(tag);
            }
        }
        Date createDate = new Date();
        tool.setTid(tid);
        tool.setToolName(toolName);
        tool.setDescription(description);
        tool.setProvider(provider);
        tool.setPrivacy(privacy);
        tool.setScope(scope);
        tool.setCreatedTime(createDate);
        tool.setRecommendation(recommendation);
        tool.setTags(tags);
        tool.setToolImg(toolImg);

        //toolImage暂时先不放

        //不同类型工具特有内容
        String backendType = toolJson.getString("backendType");
        tool.setBackendType(backendType);
        JSONObject backendService = toolJson.getJSONObject("backendService");
        if (backendType.equals("webTool")) {
            String toolUrl = toolJson.getString("toolUrl");
            tool.setToolUrl(toolUrl);
        } else {
            if (backendType.equals("modelItem")) {
                String modelId = backendService.getString("oid");
                String modelMd5 = backendService.getString("md5");
                String author = backendService.getString("author");
                JSONObject modelMdlJson = backendService.getJSONObject("mdlJson");
                JSONObject stateAndDataSetJson = Utils.convertMdl(modelMd5, modelId, modelMdlJson);

                String backendServiceName = backendService.getString("name");
                //load test data 咋做的？


                tool.setBackendName(backendServiceName);
                tool.setComputableModelId(modelId);
                tool.setComputableModelMd5(modelMd5);
                tool.setBackendProvider(author);
                tool.setMdlJson(stateAndDataSetJson);
            } else if (backendType.equals("dataMethod")) {
                String backendProvider = backendService.getString("authorName");
                JSONArray invokeServices = backendService.getJSONArray("invokeServices");
                //
                if (invokeServices == null) {
                    return null;
                }
                String backendServiceName = backendService.getString("name");
                JSONObject service = JSONObject.parseObject(JSONObject.toJSONString(invokeServices.get(0)));
                String dataMethodType = service.getString("method");
                String dataMethodId = service.getString("serviceId");
                String serviceToken = service.getString("token");
                tool.setBackendName(backendServiceName);
                tool.setBackendProvider(backendProvider);
                tool.setDataMethodId(dataMethodId);
                tool.setDataMethodType(dataMethodType);
                tool.setToken(serviceToken);
                String encodeToken = URLEncoder.encode(serviceToken, "UTF-8");

                //三种服务类型：Processing, Visualization, Data
                String tempType = "Processing";
                if (dataMethodType.equals("Visualization")) {
                    tempType = "Visualization";
                }
                //数据处理方法元数据
                try {
                    String metaUrl = "http://" + proxyServerLocation + "/capability?id=" + dataMethodId + "&type=" + tempType + "&token=" + encodeToken;
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", "application/json");
                    HttpEntity httpEntity = new HttpEntity<>(headers);
                    ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.exchange(metaUrl, HttpMethod.GET, httpEntity, JSONObject.class);
                    if (jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
                        JSONObject dataMethodMeta = jsonObjectResponseEntity.getBody();
                        Integer code = dataMethodMeta.getInteger("code");
                        if (code == 0) {
                            tool.setDataMethodMeta(dataMethodMeta.getJSONObject("capability"));
                        } else {
                            return null;
                        }
                    }
                    // tool.setDataMethodMeta(dataMethodMeta);

                    //从数据容器读取测试数据直接存入工具库中
                    // ArrayList<ToolTestData> toolTestDatas = new ArrayList<>();
                    // String url = "http://" + proxyServerLocation + "/files?id=" + dataMethodId + "&token=" + encodeToken;
                    // JSONObject testDataJson = restTemplate.getForEntity(url, JSONObject.class).getBody();
                    // JSONArray testIds = testDataJson.getJSONArray("id");
                    // for (int i = 0; i < testIds.size(); i++) {
                    //     ToolTestData toolTestData = new ToolTestData();
                    //     toolTestData.setTestDataId(UUID.randomUUID().toString());
                    //     JSONObject testDataId = JSONObject.parseObject(JSONObject.toJSONString(testIds.get(i)));
                    //     String fileName = testDataId.getString("file_name");
                    //     String[] fileNameSplit = fileName.split("\\.");
                    //     String dataUrl = testDataId.getString("url");
                    //     toolTestData.setFileName(fileNameSplit[0]);
                    //     toolTestData.setSuffix(fileNameSplit[1]);
                    //     toolTestData.setUrl(dataUrl);
                    //     toolTestDatas.add(toolTestData);
                    // }
                    // tool.setToolTestData(toolTestDatas);
                }catch (Exception e){
                    System.out.println("Data proxy server error: " + e.toString());
                    return null;
                }

            }
        }
        return toolDao.createTool(tool);
    }

    @Override
    public ToolSetVo createToolSet(Tool toolSet) {
        toolSet.setTid(UUID.randomUUID().toString());
        toolSet.setToolSet(true);
        toolSet.setCreatedTime(new Date());
        Tool tool = toolDao.saveTool(toolSet);
        if (tool == null) return null;
        ToolSetVo toolSetVo = new ToolSetVo();
        BeanUtils.copyProperties(tool, toolSetVo);
        return toolSetVo;
    }

    @Override
    public List<Tool> queryTool(ArrayList<String> keys, ArrayList<String> values) {
        if (keys.size() != values.size()) return null;
        Query query = new Query();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = values.get(i);
            Criteria criteria = new Criteria(key).is(value);
            query.addCriteria(criteria);
        }
        Criteria criteria = new Criteria("present").is(true);
        query.addCriteria(criteria);
        return toolDao.findByFields(query);
    }

    @Override
    public List<Tool> getToolByProviderService(String provider) {
        return toolDao.findAllByToolCreator(provider);
    }

    @Override
    public Tool updateToolService(Tool putTool) {
        String[] nullPropertyNames = CommonUtil.getNullPropertyNames(putTool);
        return toolDao.updateTool(putTool, nullPropertyNames);
    }

    @Override
    public void delToolService(String tid) {
        Tool tool = toolDao.findToolById(tid);
        if (tool != null) {
            //将工具标记为删除
            tool.setPresent(false);
            toolDao.saveTool(tool);
        }
    }

    @Override
    public String emptyProviderService(String tid) {
        return toolDao.emptyProvider(tid);
    }

    @Override
    public List<Tool> getToolByIds(HashSet<String> ids) {
        return toolDao.findToolByIds(ids);
    }

    @Override
    public Tool getToolByTid(String tid) {
        return toolDao.findToolById(tid);
    }

    @Override
    public ToolSetVo addToolInToolSet(String tid, ArrayList<String> tids) {
        Tool toolSet = toolDao.findToolById(tid);
        if (toolSet == null) return null;
        ArrayList<String> toolList = toolSet.getToolList();
        toolList.removeAll(tids);
        toolList.addAll(tids);
        Tool tool = toolDao.saveTool(toolSet);
        ToolSetVo toolSetVo = new ToolSetVo();
        BeanUtils.copyProperties(tool, toolSetVo);
        return toolSetVo;
    }

    @Override
    public ToolSetVo delToolInToolSet(String tid, ArrayList<String> tids) {
        Tool toolSet = toolDao.findToolById(tid);
        if (toolSet == null) return null;
        toolSet.getToolList().removeAll(tids);
        Tool tool = toolDao.saveTool(toolSet);
        ToolSetVo toolSetVo = new ToolSetVo();
        BeanUtils.copyProperties(tool, toolSetVo);
        return toolSetVo;
    }

    @Override
    public JsonResult uploadToolImg(HttpServletRequest req) throws IOException, ServletException {
        //确认是否有文件
        if (!ServletFileUpload.isMultipartContent(req)) {
            System.out.println("File is not multimedia.");
            return ResultUtils.error(-2, "File is not multimedia.");
        }

        //新建存储文件夹
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if (!path.exists()) {
            path = new File("");
        }
        File toolImgLocation = new File(path.getAbsolutePath(), "static/images");
        if (!toolImgLocation.exists()) {
            toolImgLocation.mkdirs();
        }
        try {
            Collection<Part> parts = req.getParts();
            String pathURL = "Fail";
            String newFileName = "";
            for (Part part : parts) {
                if (part.getName().equals("img")) {
                    String filePath = part.getSubmittedFileName();
                    String folderPath = toolImgLocation.getPath();

                    JsonResult storeResult = CommonUtil.fileStore(part, filePath, folderPath);
                    if (storeResult.getCode() == 0) newFileName = storeResult.getData().toString();
                    else return storeResult;
                    pathURL = "/GeoProblemSolving/images/" + newFileName;
                    break;
                }
            }
            return ResultUtils.success(pathURL);
        } catch (Exception e) {
            return ResultUtils.error(-2, e.toString());
        }
    }


    @Override
    public List<Tool> getRelevantPurposeTool(String purpose) {
        if (purpose.equals("Other purpose")) purpose = "All";
        Query query = new Query();
        Criteria criteria = new Criteria("privacy").is("Public");
        Criteria inCriteria = new Criteria("recommendation").in("All", purpose);
        Criteria presentCriteria = new Criteria("present").is(true);
        query.addCriteria(presentCriteria);
        query.addCriteria(criteria);
        query.addCriteria(inCriteria);
        return toolDao.findByFields(query);
    }
}

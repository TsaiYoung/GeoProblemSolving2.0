package cn.edu.njnu.geoproblemsolving.business.modeltask;

//import cn.edu.njnu.geoproblemsolving.Dao.CModel.ModelItemDao;


import cn.edu.njnu.geoproblemsolving.common.enums.ResultEnum;
import cn.edu.njnu.geoproblemsolving.common.exception.MyException;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static cn.edu.njnu.geoproblemsolving.business.modeltask.Utils.convertMdl;


@Service
public class ModelTaskService {

//    @Autowired
//    ModelItemDao modelItemDao;

    @Value("${managerServerIpAndPort}")
    private String managerServerIpAndPort;

    public JSONObject getComputeModel(String doi) {//根据pid.md5获得
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.getForEntity(urlStr, JSONObject.class);//虚拟http请求
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        return result;
    }

    public JsonResult getConvertComputeModel(String doi) {//根据pid.md5获得
        RestTemplate restTemplate = new RestTemplate();

        //我是Chrome
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.exchange(urlStr, HttpMethod.GET, httpEntity, JSONObject.class);

//        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.getForEntity(urlStr, JSONObject.class);//虚拟http请求
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        return ResultUtils.success(convertMdl(result));
    }

    public JsonResult createTask(String pid, String userId) {
        // 获得任务服务器
        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://" + managerServerIpAndPort + "/GeoModeling/taskNode/getServiceTask/" + pid; ////Step0:根据MD5获取可用的任务服务器

        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.getForEntity(urlStr, JSONObject.class);//虚拟http请求
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        if (jsonObjectResponseEntity.getBody().getInteger("code") != 1){
            return ResultUtils.error(-1, jsonObjectResponseEntity.getBody().getString("msg"));
        }
        JSONObject result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        String ip = result.getString("host");
        String port = result.getString("port");

        JSONObject initTaskJson = new JSONObject();
        initTaskJson.put("ip", ip);
        initTaskJson.put("port", port);
        initTaskJson.put("pid", pid);
        initTaskJson.put("username", userId);

        urlStr = "http://" + managerServerIpAndPort + "/GeoModeling/computableModel/createTask";//Step1:createTask
        jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, initTaskJson, JSONObject.class);
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        //保存url到相应的event和value
        return ResultUtils.success(result);
    }

    public String invoke(JSONObject obj) {
        String urlStr = "http://" + managerServerIpAndPort + "/GeoModeling/computableModel/invoke";//Step3:配置完数据数据，开始调用模型
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, obj, JSONObject.class);
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        String taskTid = jsonObjectResponseEntity.getBody().getJSONObject("data").getString("tid");
        return taskTid;
    }

    public JSONObject getRecord(JSONObject data) {
        String urlStr = "http://" + managerServerIpAndPort + "/GeoModeling/computableModel/refreshTaskRecord"; //Step4:根据task id去查询模型运行记录
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, data, JSONObject.class);
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody();
        return result;
    }

    public Object getAllService(String asc, String page) {
        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://223.2.41.253:8080/GeoModeling/loadDeployedModel?asc=" + asc + "&page=" + page + "&size=10"; ////Step0:根据MD5获取可用的任务服务器

        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.getForEntity(urlStr, JSONObject.class);//虚拟http请求
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        return ResultUtils.success(result);
    }

//    public String upload(File file,String userId) {
//        FileSystemResource resource = new FileSystemResource(file);      //临时文件
//        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
//        form.add("serverNode", "china");
//        form.add("userId", 2);
//        form.add("ogmsdata", resource);
//        form.add("name", userId);
//        form.add("origination", "GeoProblemSolving_3r");
//
//        String urlStr = "http://221.226.60.2:8082/data";
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, form, JSONObject.class);
//        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new MyException(ResultEnum.ERROR);
//        }
//        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
//        String urlResult = result.getJSONObject("data").getString("url");
//        return urlResult;
//    }
//
//    public String uploadFileForm(Collection<Part> parts , String userId) throws IOException {
//        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
//        for (Part part : parts) {
//            String header = part.getHeader("Content-Disposition");
//            String filename2 = header.substring(header.indexOf("filename=\"") + 10, header.lastIndexOf("\""));//filename=" (整个字符串长度为10，所以要加10)
//            // 获取文件名
////            String fileName = part.getName();
//            //  获取文件后缀名
//            String suffix = "." + FilenameUtils.getExtension(filename2);
//            File file = File.createTempFile(part.getName(), suffix);//创建临时文件
//            FileUtils.copyInputStreamToFile(part.getInputStream(), file);
//            FileSystemResource fileSystemResource = new FileSystemResource(file);
//            form.add("ogmsdata", fileSystemResource);
//        }
//
//        form.add("serverNode", "china");
//        form.add("userId", "2");
//        form.add("name", userId);
//        form.add("origination", "GeoProblemSolving");
//
//        String urlStr = "http://111.229.14.128:8899/data";
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, form, JSONObject.class);
//        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new MyException(ResultEnum.ERROR);
//        }
//        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
//        String resultId = result.getJSONObject("data").getString("source_store_id");
//        return resultId;
//    }
//
//
//
//    public ArrayList<Object> getInvokeItem(JSONObject obj) {
//        ArrayList<Object> inputList = new ArrayList<>();
//        JSONArray stateList = obj.getJSONArray("stateList");
//        for (int i = 0; i < stateList.size(); i++) {
//            JSONObject state = stateList.getJSONObject(i);
//
//            JSONArray eventList = state.getJSONArray("eventList");
//            for (int j = 0; j < eventList.size(); j++) {
//                if (eventList.getJSONObject(j).getString("url") != "" && eventList.getJSONObject(j).getString("url") != null) {
//                    //筛选出有url的event
//                    JSONObject input = new JSONObject();
//                    input.put("statename", state.getString("name"));//获得statename字段
//                    input.put("event", eventList.getJSONObject(j).getString("name"));//获得event字段
//                    input.put("url", eventList.getJSONObject(j).getString("url"));//获得url
//                    input.put("tag", eventList.getJSONObject(j).getString("name"));//tag
//                    inputList.add(input);
//                } else {
//                    continue;
//                }
//            }
//        }
//        return inputList;
//    }

//    public Object add() {
//        ComputableModelDaoImpl modelItemDao = new ComputableModelDaoImpl(mongoTemplate);
//        return modelItemDao.addDataTemplate();
//    }

//    public Object download(String uid) {
//        String urlStr = "http://111.229.14.128:8899/data";
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, form, JSONObject.class);
//        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new MyException(ResultEnum.ERROR);
//        }
//        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
//        String resultId = result.getJSONObject("data").getString("source_store_id");
//        return resultId;
//    }
}


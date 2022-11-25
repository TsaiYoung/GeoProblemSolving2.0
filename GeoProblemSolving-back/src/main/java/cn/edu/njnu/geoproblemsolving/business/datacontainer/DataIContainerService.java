package cn.edu.njnu.geoproblemsolving.business.datacontainer;


import cn.edu.njnu.geoproblemsolving.common.enums.ResultEnum;
import cn.edu.njnu.geoproblemsolving.common.exception.MyException;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import cn.hutool.json.JSONException;
import cn.hutool.json.XML;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName dataItemService
 * @Description
 * @Author Zhiyi
 * @Date 2019/12/16  22:06
 * @Version 1.0.0
 */
@Service
public class DataIContainerService {
    @Value("${dataContainer}")
    private String dataContainer;

    public JSONObject addDataItem(File file) {
        FileSystemResource resource = new FileSystemResource(file);      //临时文件
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", resource);
        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://" + dataContainer + ":8081" + "/dataitem/addByStorage"; ////Step0:根据MD5获取可用的任务服务器

        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, form, JSONObject.class);//虚拟http请求
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody().getJSONObject("data");
        return result;
    }

    public ResponseEntity<byte[]> download(String id) {
        RestTemplate restTemplate = new RestTemplate();
//        String urlStr = "http://" + dataContainer + ":8082/data?uid=" + id;
        String urlStr = "http://" + dataContainer + ":8082/data/" + id;
        ResponseEntity<byte[]> response = restTemplate.exchange(urlStr, HttpMethod.GET,
                null, byte[].class);
        return response;
    }

    public String upload(File file, String userId, String userName) {
        FileSystemResource resource = new FileSystemResource(file);      //临时文件
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("serverNode", "china");
        form.add("userId", userId);
        form.add("datafile", resource);
        form.add("name", userName);
        form.add("origination", "GeoProblemSolving_3r");

        String urlStr = "http://" + dataContainer + ":8082/data";
//        String urlStr = "http://" + dataContainer + ":8082/dataNoneConfig";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(urlStr, form, JSONObject.class);
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }
        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
//        String urlResult = result.getJSONObject("data").getString("source_store_id");
        String urlResult = result.getJSONObject("data").getString("id");
        return urlResult;
    }

    public Object getDataService(String id, String token, String type) {

        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://111.229.14.128:8898/capability?id=" + id + "&type=" + type + "&token=" + URLEncoder.encode(token);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(null, headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.exchange(urlStr, HttpMethod.GET, requestEntity, JSONObject.class);

//        ResponseEntity<String> jsonObjectResponseEntity = restTemplate.getForEntity(urlStr, String.class);
        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }

        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
//        cn.hutool.json.JSONObject object = XML.toJSONObject(result);
        //xml根节点
//        String jsonData = object.get("returnsms").toString();
        //转换成json对象
//        JSONObject jsonObject = JSON.parseObject(jsonData);
//        String urlResult = result.getJSONObject("data").getString("source_store_id");
//        JSONObject data = result.getJSONObject("data");
        return ResultUtils.success(result);
    }


    @SneakyThrows
    public String invoke(JSONObject jsonObject) {

        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://111.229.14.128:8898/invokeUrlsDataPcs";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("token", URLEncoder.encode(jsonObject.getString("token"), "UTF-8"));
        requestBody.add("pcsId", jsonObject.getString("pcsId"));
        requestBody.add("urls", jsonObject.getString("urls"));
        requestBody.add("params", jsonObject.getString("params"));


        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(requestBody, headers);

        JSONObject result = restTemplate.postForObject(urlStr, requestEntity, JSONObject.class);


        String url = result.getString("url");

        return url;
    }

    public Object change(Object object) {
        JSONObject json = (JSONObject) JSON.toJSON(object);
        JSONObject method = (json.getJSONObject("Capability")).getJSONObject("metaDetail").getJSONObject("Method");
        Object inputJson = method.getJSONObject("Input").get("Item");
        Object outputJson = method.getJSONObject("Output").get("Item");
        Object parameterJson = method.getJSONObject("Parameter").get("Item");
        JSONObject newJson = new JSONObject();
        if (!(inputJson instanceof JSONArray)) {
            List list = new ArrayList<>();
            list.add(inputJson);
            newJson.put("inputs", list);
        } else {
            newJson.put("inputs", inputJson);
        }
        if (!(outputJson instanceof JSONArray)) {
            List list = new ArrayList<>();
            list.add(outputJson);
            newJson.put("outputs", list);
        } else {
            newJson.put("inputs", outputJson);
        }
        if (!(parameterJson instanceof JSONArray)) {
            List list = new ArrayList<>();
            list.add(parameterJson);
            newJson.put("parameters", list);
        } else {
            newJson.put("inputs", parameterJson);
        }
        newJson.put("date", json.getJSONObject("Capability").getString("date"));
        newJson.put("name", json.getJSONObject("Capability").getString("name"));
        newJson.put("description", method.getString("Description"));

        return newJson;

    }

    public JSONObject getRemoteData(String id, String token) {
        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://111.229.14.128:8898/files?id=" + id + "&token=" + URLEncoder.encode(token);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(null, headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.exchange(urlStr, HttpMethod.GET, requestEntity, JSONObject.class);

        if (!jsonObjectResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new MyException(ResultEnum.ERROR);
        }

        JSONObject result = jsonObjectResponseEntity.getBody();//获得上传数据的URL
        return result;

    }

    public Integer initDataService(String token) {
        //查看节点是否在线
        JSONObject onlineStatus = null;
        RestTemplate restTemplate = new RestTemplate();
        String urlStr = "http://111.229.14.128:8898/state?token=" + URLEncoder.encode(token);
        try {
            onlineStatus  = restTemplate.getForObject(urlStr, JSONObject.class);
        }catch (HttpClientErrorException e){
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<JSONObject> httpEntity = new HttpEntity<>(headers);
            onlineStatus = restTemplate.exchange(urlStr, HttpMethod.GET, httpEntity, JSONObject.class).getBody();
        }
        return onlineStatus.getInteger("code");
    }
}

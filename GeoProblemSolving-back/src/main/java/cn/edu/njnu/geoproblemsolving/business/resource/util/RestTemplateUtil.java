package cn.edu.njnu.geoproblemsolving.business.resource.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class RestTemplateUtil {
    // @Autowired
    // RestTemplate restTemplate;
    RestTemplate restTemplate = new RestTemplate();

    /**
     * 文件下载请求
     * @param downRemoteUrl
     * @return
     */
    public ResponseEntity<byte[]> getDownRemoteResult(String downRemoteUrl) {
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(downRemoteUrl, HttpMethod.GET, entity, byte[].class);
        return response;
    }

    public ResponseEntity getDelRemoteResult(String delRemoteUrl) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(delRemoteUrl, HttpMethod.DELETE, httpEntity, String.class);
        return response;
    }

    /**
     * 将数据上传到数据容器
     * @param uploadRemoteUrl
     * @param paramMap
     * @return
     */
    public JSONObject uploadRemote(String uploadRemoteUrl, LinkedMultiValueMap<String, Object> paramMap) {
        JSONObject uploadResult = restTemplate.postForObject(uploadRemoteUrl, paramMap, JSONObject.class);
        return uploadResult;
    }

    //需要测试
    // public Object setUserBase(String userServerUrl,LinkedMultiValueMap<String, Object> paramMap){
    //     ResponseEntity response =  restTemplate.postForObject(userServerUrl, paramMap, ResponseEntity.class);
    //     return response;
    // }
    public Object setUserBase(String userServerUrl,JSONObject userBaseJson){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(userBaseJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(userServerUrl, HttpMethod.POST, httpEntity, String.class);
        return response;
    }
}
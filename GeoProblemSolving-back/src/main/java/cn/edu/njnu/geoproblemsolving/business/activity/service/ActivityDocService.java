package cn.edu.njnu.geoproblemsolving.business.activity.service;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.ActivityDoc;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;

public interface ActivityDocService {

    JsonResult saveDocument(ActivityDoc activityDoc);

    JsonResult findDocument(String aid);

    JsonResult findGeoAnalysisOperation(String aid, String tid);

    JsonResult deleteDocument(String aid);

    JsonResult updateDocument(String aid, String document);

    JsonResult findDocuments(HashSet<String> aids);
}

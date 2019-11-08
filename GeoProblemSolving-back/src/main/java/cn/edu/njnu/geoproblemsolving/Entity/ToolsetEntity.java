package cn.edu.njnu.geoproblemsolving.Entity;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;

@Document(collection = "Toolset")
@Data
public class ToolsetEntity {
    private String tsId;
    private String toolsetName;
    private JSONArray toolList; // 创建的时候不添加toolList，在toolCollection页面添加，或者创建tool 选择 toolset时，自动添加
    private ToolsetEntity subToolsets;
    private ArrayList<String> categoryTag;
    private String recomStep; // step类型 or general
    private String provider;
    private String privacy;
    private String toolsetImg;
    private String createTime;

    public String getTsId() {
        return tsId;
    }

    public String getToolsetName() {
        return toolsetName;
    }

    public JSONArray getToolList() {
        return toolList;
    }

    public ToolsetEntity getSubToolsets() {
        return subToolsets;
    }

    public ArrayList<String> getCategoryTag() {
        return categoryTag;
    }

    public String getRecomStep() {
        return recomStep;
    }

    public String getProvider() {
        return provider;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getToolsetImg() {
        return toolsetImg;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setTsId(String tsId) {
        this.tsId = tsId;
    }

    public void setToolsetName(String toolsetName) {
        this.toolsetName = toolsetName;
    }

    public void setToolList(JSONArray toolList) {
        this.toolList = toolList;
    }

    public void setSubToolsets(ToolsetEntity subToolsets) {
        this.subToolsets = subToolsets;
    }

    public void setCategoryTag(ArrayList<String> categoryTag) {
        this.categoryTag = categoryTag;
    }

    public void setRecomStep(String recomStep) {
        this.recomStep = recomStep;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public void setToolsetImg(String toolsetImg) {
        this.toolsetImg = toolsetImg;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
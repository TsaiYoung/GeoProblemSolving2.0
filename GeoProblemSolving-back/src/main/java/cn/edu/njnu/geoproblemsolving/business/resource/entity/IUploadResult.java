package cn.edu.njnu.geoproblemsolving.business.resource.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class IUploadResult {
    public ArrayList<ResourceEntity> uploaded;
    public ArrayList<String> failed;
    public ArrayList<String> sizeOver;
    public ArrayList<HashMap<String, String>> uploadedOperation;
}

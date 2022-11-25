package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import com.alibaba.fastjson.JSONArray;

import java.util.*;

/**
 * @ClassName TagUtil
 * @Description 标签工具类：处理用于与资源标签
 * @Author zhngzhng
 * @Date 2021/8/19
 **/
public class TagUtil {

    public static HashMap<String, String> recoveryResTag(String tagStr){
        HashMap<String, String> resInfo = new HashMap<>();
        String[] tagArray = tagStr.split("O517");
        String type = tagArray[0];
        resInfo.put("type", type);
        if (type.equals("data")) {
            try {
                String format = tagArray[1];
                if (!format.equals("")) resInfo.put("format", format);
                String scale = tagArray[2];
                if (!scale.equals("")) resInfo.put("scale", scale);
                String reference = tagArray[3];
                if (!reference.equals("")) resInfo.put("reference", reference);
                String unit = tagArray[4];
                if (!unit.equals("")) resInfo.put("unit", unit);
                String concept = tagArray[5];
                // 不报错就肯定是有值
                resInfo.put("concept", concept);
            }catch (ArrayIndexOutOfBoundsException e){
                // 不用做处理，超出范围则说明为空，不用添加
            }


        }
        return resInfo;
    }

    public static HashMap<String, HashSet<String>> recoveryUserTag(String tagStr){
        HashMap<String, HashSet<String>> userTagMap = new HashMap<>();
        String[] tagArray = tagStr.split("O517");
        //三类判断条件
        String roles =  tagArray[0];
        HashSet<String> roleSet = new HashSet<>(Arrays.asList(roles.split("#")));
        try {
            String domains = tagArray[1];
            HashSet<String> domainsSet = new HashSet<>(Arrays.asList(domains.split("#")));
            userTagMap.put("domain", domainsSet);
        }catch (ArrayIndexOutOfBoundsException e){
            userTagMap.put("domain", new HashSet<>());
        }
        try {
            String organizations = tagArray[2];
            HashSet<String> organizationsSet = new HashSet<>(Arrays.asList(organizations.split("#")));
            userTagMap.put("organization", organizationsSet);
        }catch (ArrayIndexOutOfBoundsException e){
            userTagMap.put("organization", new HashSet<>());
        }
        userTagMap.put("role", roleSet);
        return userTagMap;
    }

    public static String setResourceTag(ResourceEntity res){
        //需要从活动文档中进行读取
        String type = res.getType();
        String suffix = res.getSuffix();
        return addFlagInTags(type, suffix);
    }

    public static String setResourceTag(HashMap<String,String> res){
        String type = res.get("type");
        return addFlagInTags(type);
    }

    public static String setResTag(HashMap<String, String> resInfo){
        String type = resInfo.get("type");
        if (!type.equals("data")){
            return addFlagInTags(type);
        }else {
            String format = "";
            String scale = "";
            String reference = "";
            String unit = "";
            String concept = "";
            for (Map.Entry<String, String> item : resInfo.entrySet()){
                String key = item.getKey();
                String value = item.getValue();
                switch (key){
                    case "format":
                        format = value;
                        break;
                    case "scale":
                        scale = value;
                        break;
                    case "reference":
                        reference = value;
                        break;
                    case "unit":
                        unit = value;
                        break;
                    case "concept":
                        concept = value;
                        break;
                }
            }
            return addFlagInTags(type, format, scale, reference, unit, concept);
        }
    }


    public static String setUserTag(String role, JSONArray domainArray, JSONArray orgArray){
        String domain = array2String(domainArray);
        String organization = array2String(orgArray);
        return addFlagInTags(role, domain, organization);
    }
    /**
     * 为不同类型字符串添加分隔符
     * @param separator
     * @param tags
     * @return
     */
    public static String addFlag(String separator, String... tags){
        String tagStr = "";
        for (int i = 0; i < tags.length; i++) {
            tagStr += (i == tags.length - 1) ? tags[i] : tags[i] + separator;
        }
        return tagStr;
    }

    public static String addFlagInTags(String... tags){
        return addFlag("O517", tags);
    }

    /**
     * 将 jsonArray 转为带分隔符的字符串
     * @param separator
     * @param array
     * @return
     */
    public static String jsonArrayToString(String separator, JSONArray array){
        String arrayStr = "";
        if (array != null && array.size() != 0){
            for (int i = 0; i < array.size(); i++) {
                String item = (String)array.get(i);
                arrayStr += (i == array.size() - 1) ? item : item +separator;
            }
        }
        return arrayStr;
    }

    public static String array2String(JSONArray array){
        return jsonArrayToString("#", array);
    }

    public static String changeSeparator(String str){
        String[] split = str.split(",");
        if (split.length == 1){
            return str;
        }
        JSONArray jsonArray = new JSONArray();
        for (String item : split){
            jsonArray.add(item);
        }
        return array2String(jsonArray);
    }

    /**
     * 返回两个 Set 是否有相同的元素
     * @param source
     * @param target
     * @return
     */
    public static boolean checkTagContain(HashSet source, HashSet target){
        if (source == null) return false;
        int oriSize = source.size();
        //若 source 中包含 target 中内容会被移除
        source.removeAll(target);
        return oriSize != source.size();
    }






}

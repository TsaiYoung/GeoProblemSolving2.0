package cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @ClassName ActivityNode
 * @Description 节点对象
 * 这个只需要存在于对应的 document 中即可
 * 简化版 activity，强化了用户与资源的存储性能
 * 可能会涉及到的变化
 * 1.节点有成员发生变化（这个相对好处理，只需要修改图中的内容）
 * 2.
 * @Author zhngzhng
 * @Date 2021/7/23
 **/
@Data
@Document(collection = "activityNode")
public class ActivityNode {
    /**
     * id： activityId
     * key: userId
     * value:
     *  key:
     *  name
     *  org
     *  domain
     * <p>
     * 不同类型限制条件使用 固定标记(分号不太合适，用 O517 来分割，是 O 不是零)   来进行分割
     * 同类多条使用 “#” 进行分割
     * 这样处理getTag，更加方便可以直接从 Node 中进行提取，通过NodeIdList查找Node,然后处理字符串即可
     */
    String id;
    /*
    key: userId
    value: 用户role, domain, org 字段构成的字符串
     */
    HashMap<String, String> members;
    //反向思维，由条件组合来反向获得资源id;  还是有资源 id 再来获取资源
    /*
    key: 资源 id
    value: 由各限制条件构成的条件字符串
    value的组成形式
    type != data ? type : type/format/scale/reference/unit/concept
     */
    HashMap<String, String> resources;

    Date createDate;
}

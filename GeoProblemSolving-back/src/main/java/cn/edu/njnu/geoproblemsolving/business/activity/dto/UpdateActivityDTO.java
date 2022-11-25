package cn.edu.njnu.geoproblemsolving.business.activity.dto;

import cn.edu.njnu.geoproblemsolving.common.utils.ToDomainConverter;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ActivityType;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.HashSet;
import java.util.List;


@Data
public class UpdateActivityDTO implements ToDomainConverter {
    /**
     * Common activity
     */
    private String aid;
    private String name;
    private String description;
    private String purpose;
    private ActivityType type;
    private JSONArray pathway;
    private String permission;
    private HashSet<String> toolList;

}

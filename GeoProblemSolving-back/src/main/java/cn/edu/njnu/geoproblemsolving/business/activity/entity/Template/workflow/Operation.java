package cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow;

import lombok.Data;

import java.util.HashSet;

/**
 * @ClassName Operation
 * @Description 操作实体，用于描述操作的基本信息
 * @Author zhngzhng
 * @Date 2021/12/9
 **/
@Data
public class Operation {
    String oid;
    String toolId;
    String description;
    String time;
    //与活动文档中的步骤对应起来
    String purpose;
    HashSet<OperationRes> input;
    HashSet<OperationRes> output;
}

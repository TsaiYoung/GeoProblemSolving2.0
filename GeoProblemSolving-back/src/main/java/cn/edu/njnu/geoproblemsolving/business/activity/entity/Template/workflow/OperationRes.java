package cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow;

import lombok.Data;

/**
 * @ClassName OperationRes
 * @Description 操作中资源的信息
 * @Author zhngzhng
 * @Date 2021/12/9
 **/
@Data
public class OperationRes {
    String uid;
    String name;
    String description;
    String type;
    String value;
}

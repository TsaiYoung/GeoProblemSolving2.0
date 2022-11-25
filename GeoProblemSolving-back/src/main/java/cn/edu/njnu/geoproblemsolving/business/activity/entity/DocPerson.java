package cn.edu.njnu.geoproblemsolving.business.activity.entity;

import lombok.Data;

import java.util.HashSet;

/**
 * @ClassName DocPerson
 * @Description 活动文档中的成员
 * @Author zhngzhng
 * @Date 2021/11/26
 **/

@Data
public class DocPerson {
    private String userId;
    //在活动中的角色
    private String role;
    private String email;
    private String name;
    private HashSet<String> domain;
    private HashSet<String> organization;
}

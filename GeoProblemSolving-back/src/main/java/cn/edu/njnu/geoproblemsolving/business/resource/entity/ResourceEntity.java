package cn.edu.njnu.geoproblemsolving.business.resource.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * @ClassName ResourceEntity
 * @Description 资源实体，不用存储
 * 只存储一些项目中的资源
 * @Id注解，会把此实体类认定为表进行存储
 * @Author zhngzhng
 * @Date 2021/3/31
 **/
@Data
@Document(collection = "Resource")
public class ResourceEntity {
    @Id
    private String uid = UUID.randomUUID().toString();
    private String name;
    //数据在数据容器中的链接
    private String address;
    private Boolean folder;
    private String type = "data";
    private Boolean userUpload = false;
    //个人空间内的文件夹不存在 privacy
    private String privacy = "private";
    //缩略图路径
    private String thumbnail;
    private String editToolInfo;
    private long fileSize;
    //存储父资源的 uuid
    private String parent;
    private String md5;
    private String suffix;
    private String description;
    private String template;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date uploadTime = new Date();
    private ArrayList<ResourceEntity> children;

    //GSM 部分字段
    String uploaderId;
    String uploaderName;
    /*
    所绑定的项目id
    在当前资源设计中
    实际上只是在根目录的时候起作用
     */
    String activityId;
}

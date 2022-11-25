package cn.edu.njnu.geoproblemsolving.business.user.entity;

import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.user.enums.UserTitle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;

@Data
@Document(collection = "User")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Necessary
     */
    @Id
    private String userId;  //UUID
    private String name;
    //平台上用户无需有password
    private String password;    //MD5
    private String email;   //used for registration

    /**
     * UserEntity server
     */
    private String avatar;
    private String phone;
    private UserTitle title;
    private String country;
    private String State;    // County / State / Province
    private String city;
    private String homepage;
    private String introduction;
    //resource space capacity
    private long capacity;
    private long usedCapacity;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdTime;
    private ArrayList<String> organizations;
    private ArrayList<String> domain;
    private ArrayList<String> loginIp;
    private ArrayList<ResourceEntity> resource;

    /**
     * Unique
     * adi
     */
    private ArrayList<String> joinedProjects;
    private ArrayList<String> createdProjects;

    //存储令牌相关内容
    TokenInfo tokenInfo;
}

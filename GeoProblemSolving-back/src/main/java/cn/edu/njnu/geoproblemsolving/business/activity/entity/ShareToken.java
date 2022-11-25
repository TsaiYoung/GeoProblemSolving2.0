package cn.edu.njnu.geoproblemsolving.business.activity.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName ShareToken
 * @Description 活动共享的对象
 * @Author zhngzhng
 * @Date 2021/12/14
 **/
@Document(collection = "shareToken")
@Data
public class ShareToken {
    @Id
    String tid;
    String aid;
    String token;
    String tempUserName;
    String tempUserId;
    String inviterId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Date createTime;
    long validTime;
}

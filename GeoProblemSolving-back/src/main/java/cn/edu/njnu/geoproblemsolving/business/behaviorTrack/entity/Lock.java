package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @ClassName Lock
 * @Description 锁存储对象
 * @Author zhngzhng
 * @Date 2022/6/30
 **/
@Data
@Document(collection = "distributedLock")
public class Lock {
    @Id
    private String id;
    private long expireAt;
    private String token;
}

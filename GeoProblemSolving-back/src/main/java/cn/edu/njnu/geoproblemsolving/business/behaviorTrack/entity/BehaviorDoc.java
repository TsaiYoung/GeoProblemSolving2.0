package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @ClassName BehaviorDoc
 * @Description 行为文档入库
 * 在读写时加入乐观锁来实现并发写
 * @Author zhngzhng
 * @Date 2022/6/29
 **/
@Data
@Document(collection = "BehaviorDoc")
public class BehaviorDoc {
    //bid = project's id
    @Id
    private String bid;
    private String document;
    /*
    lockFlag: 锁标记
    expireAt: 锁的过期时间
    通过操作这俩字段实现document级的锁
     */
    private boolean lockFlag = false;
    //悲观锁过期时间
    private long expireAt = System.currentTimeMillis();
}

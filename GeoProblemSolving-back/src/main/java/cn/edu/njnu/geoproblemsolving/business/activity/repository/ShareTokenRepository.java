package cn.edu.njnu.geoproblemsolving.business.activity.repository;

import cn.edu.njnu.geoproblemsolving.business.activity.entity.ShareToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName ShareTokenRepository
 * @Description
 * @Author zhngzhng
 * @Date 2021/12/14
 **/
@Repository("shareToken")
public interface ShareTokenRepository extends MongoRepository<ShareToken, String> {
    List<ShareToken> findAllByAid(String aid);

    Long countByAid(String aid);

}

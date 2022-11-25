package cn.edu.njnu.geoproblemsolving.business.activity.repository;

import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.entity.ActivityNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "activityNode")
public interface ActivityNodeRepository extends MongoRepository<ActivityNode, String> {

}

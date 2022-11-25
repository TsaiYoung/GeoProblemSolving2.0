package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.repository;

import cn.edu.njnu.geoproblemsolving.business.behaviorTrack.entity.BehaviorDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "BehaviorDoc")
public interface BehaviorDocRepository extends MongoRepository<BehaviorDoc, String> {
}

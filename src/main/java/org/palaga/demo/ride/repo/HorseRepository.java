package org.palaga.demo.ride.repo;

import org.palaga.demo.ride.model.Horse;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RepositoryRestResource(collectionResourceRel = "horse", path = "horse")
public interface HorseRepository extends PagingAndSortingRepository<Horse, String> {

}

package org.palaga.demo.ride.repo;

import org.palaga.demo.ride.model.Stable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RepositoryRestResource(collectionResourceRel = "stable", path = "stable")
public interface StableRepository extends PagingAndSortingRepository<Stable, String> {

}

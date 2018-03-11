package org.palaga.demo.ride.repo;

import org.palaga.demo.ride.model.Ride;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RepositoryRestResource(collectionResourceRel = "ride", path = "ride")
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {

}

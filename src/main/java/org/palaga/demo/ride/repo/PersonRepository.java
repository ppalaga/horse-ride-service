package org.palaga.demo.ride.repo;

import org.palaga.demo.ride.model.Person;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RepositoryRestResource(collectionResourceRel = "person", path = "person")
public interface PersonRepository extends PagingAndSortingRepository<Person, String> {

}

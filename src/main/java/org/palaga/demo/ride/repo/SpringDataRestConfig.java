package org.palaga.demo.ride.repo;

import org.palaga.demo.ride.model.Horse;
import org.palaga.demo.ride.model.Person;
import org.palaga.demo.ride.model.Ride;
import org.palaga.demo.ride.model.Stable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Configuration
public class SpringDataRestConfig {

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {

        return new RepositoryRestConfigurerAdapter() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                config.exposeIdsFor(Horse.class, Person.class, Ride.class, Stable.class);
            }
        };

    }

}

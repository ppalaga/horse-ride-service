package org.palaga.demo.ride.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(UnhealthEndpoint.class);
    }
}

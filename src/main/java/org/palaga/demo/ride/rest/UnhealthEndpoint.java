package org.palaga.demo.ride.rest;

import java.io.Serializable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RestController
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
@Path("/unhealth")
public class UnhealthEndpoint implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(UnhealthEndpoint.class);

    @Value("${demo.git.revision}")
    private String revision;

    private Status status = Status.SERVICE_UNAVAILABLE;

    /**
     *
     */
    private static final long serialVersionUID = -7227732980791688773L;

    @GET
    @Path("/info")
    public Response info() {
        final String msg = "" + status + " " + revision;
        return Response.status(Status.OK).entity(msg).build();
    }

    @GET
    @Path("/")
    public Response status() {
        final String msg = "" + status + " " + revision;
        log.info("/unhealth: "+ msg);
        return Response.status(status).build();
    }
}

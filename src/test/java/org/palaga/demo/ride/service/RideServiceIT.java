package org.palaga.demo.ride.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.Assert;
import org.junit.Test;
import org.palaga.demo.ride.JacksonConfig;
import org.palaga.demo.ride.model.Horse;
import org.palaga.demo.ride.model.Horse.Sex;
import org.palaga.demo.ride.model.Person;
import org.palaga.demo.ride.model.Person.Gender;
import org.palaga.demo.ride.model.Ride;
import org.palaga.demo.ride.model.Stable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class RideServiceIT {

    private static final String baseUri;
    private static final String serviceHost;
    private static final int servicePort;

    static {
        serviceHost = Optional.ofNullable(System.getenv("HORSE_RIDE_SERVICE_HOST")).orElse("localhost");
        servicePort = Integer.parseInt(Optional.ofNullable(System.getenv("HORSE_RIDE_SERVICE_PORT")).orElse("8080"));
        baseUri = String.format("http://%s:%d", serviceHost, servicePort);
    }

    final static Logger log = LoggerFactory.getLogger(RideServiceIT.class);

    private static ObjectMapper configureMapper() {
        return new JacksonConfig().objectMapper(new Jackson2ObjectMapperBuilder());
    }

    private Client client = ClientBuilder.newClient(new ClientConfig(
            new JacksonJaxbJsonProvider(configureMapper(), JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS)));

    @Test
    public void crud() {
        for (int i = 0; i < 20; i++) {
            Socket s = null;
            try {
                log.info("trying to connect = " + baseUri);
                s = new Socket();
                s.connect(new InetSocketAddress(serviceHost, servicePort), 500);
                break;
            } catch (IOException e) {
                log.info("catched" + e);
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                    }
                }
            }
            try {
                log.info("sleeping");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("baseUri = " + baseUri);

        final String stableId = UUID.randomUUID().toString();
        final Stable stable = new Stable(stableId, "Herrengasse", 48.208954, 16.365986);
        {
            final Response r = request("/stable").post(Entity.entity(stable, MediaType.APPLICATION_JSON));
            Assert.assertEquals(201, r.getStatus());
        }
        {
            final Response r = request("/stable/" + stable.getStableId()).get();
            Assert.assertEquals(200, r.getStatus());
            Assert.assertEquals(stable, r.readEntity(Stable.class));
        }

        final String horseId = UUID.randomUUID().toString();
        final Horse horse = new Horse(horseId, "Bucephalus", Sex.stallion, LocalDate.of(340, 1, 1), "Thessalian",
                "back", stable);
        {
            final Response r = request("/horse").post(Entity.entity(horse, MediaType.APPLICATION_JSON));
            Assert.assertEquals(201, r.getStatus());
        }

        final String personId = UUID.randomUUID().toString();
        final Person joe = new Person(personId, "Joe", "Doe", Gender.male, "+123456789", "joe@doe.me");
        {
            final Response r = request("/person").post(Entity.entity(joe, MediaType.APPLICATION_JSON));
            Assert.assertEquals(201, r.getStatus());
        }

        final String rideId = UUID.randomUUID().toString();
        final Ride ride = new Ride(rideId, joe, horse, ZonedDateTime.now(), Duration.ofHours(2), 48.208954, 16.365986,
                48.208954, 16.365986);
        {
            final Response r = request("/ride").post(Entity.entity(ride, MediaType.APPLICATION_JSON));
            Assert.assertEquals(201, r.getStatus());
        }

        {
            Assert.assertEquals(204, request("/ride/" + rideId).delete().getStatus());
            final Response r = request("/ride/" + rideId).get();
            Assert.assertEquals(404, r.getStatus());
        }
        {
            Assert.assertEquals(204, request("/person/" + personId).delete().getStatus());
            final Response r = request("/person/" + personId).get();
            Assert.assertEquals(404, r.getStatus());
        }
        {
            Assert.assertEquals(204, request("/horse/" + horseId).delete().getStatus());
            final Response r = request("/horse/" + horseId).get();
            Assert.assertEquals(404, r.getStatus());
        }
        {
            Assert.assertEquals(204, request("/stable/" + stableId).delete().getStatus());
            final Response r = request("/stable/" + stableId).get();
            Assert.assertEquals("got response " + r, 404, r.getStatus());
        }
    }

    private Builder request(String path) {
        return client.target(baseUri).path(path).request(MediaType.APPLICATION_JSON);
    }

}

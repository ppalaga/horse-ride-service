package org.palaga.demo.ride.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.palaga.demo.ride.model.Horse;
import org.palaga.demo.ride.model.Horse.Sex;
import org.palaga.demo.ride.model.Person;
import org.palaga.demo.ride.model.Person.Gender;
import org.palaga.demo.ride.model.Ride;
import org.palaga.demo.ride.model.Stable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RideServiceSpringBootTest {

    final static Logger log = LoggerFactory.getLogger(RideServiceSpringBootTest.class);

    static class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {


        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            log.info("< " + request.getMethod().name() + " " + request.getURI());
            log.info("< " + request.getHeaders());
            log.info("< " + new String(body, StandardCharsets.UTF_8));
            ClientHttpResponse response = execution.execute(request, body);
//            log.info("> " + response.getHeaders());
//            log.info("> " + StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
            return response;
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void crud() {

        this.restTemplate.getRestTemplate().setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.restTemplate.getRestTemplate().setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));

        for (HttpMessageConverter<?> c : restTemplate.getRestTemplate().getMessageConverters()) {
            log.info(" converter = "+ c);
        }

        final String stableId = UUID.randomUUID().toString();
        final Stable stable = new Stable(stableId, "Herrengasse", 48.208954, 16.365986);
        {
            final ResponseEntity<Stable> r = this.restTemplate.postForEntity("/stable", stable, Stable.class);
            Assert.assertEquals(201, r.getStatusCodeValue());
        }
        {
            final ResponseEntity<Stable> r = this.restTemplate.getForEntity("/stable/" + stable.getStableId(), Stable.class);
            Assert.assertEquals(200, r.getStatusCodeValue());
            Assert.assertEquals(stable, r.getBody());
        }
        {
            ResponseEntity<PagedResources<Stable>> r = this.restTemplate.exchange("/stable",
                    HttpMethod.GET, null, new ParameterizedTypeReference<PagedResources<Stable>>() {});
            Assert.assertEquals("got response " + r, 200, r.getStatusCodeValue());
            Assert.assertEquals(new ArrayList<>(Arrays.asList(stable)), new ArrayList<>(r.getBody().getContent()));
        }

        final String horseId = UUID.randomUUID().toString();
        final Horse horse = new Horse(horseId, "Bucephalus", Sex.stallion, LocalDate.of(340, 1, 1), "Thessalian", "back", stable);
        {
            final ResponseEntity<Horse> r = this.restTemplate.postForEntity("/horse", horse, Horse.class);
            Assert.assertEquals(201, r.getStatusCodeValue());
        }

        final String personId = UUID.randomUUID().toString();
        final Person joe = new Person(personId, "Joe", "Doe", Gender.male, "+123456789", "joe@doe.me");
        {
            final ResponseEntity<Person> r = this.restTemplate.postForEntity("/person", joe, Person.class);
            Assert.assertEquals(201, r.getStatusCodeValue());
        }

        final String rideId = UUID.randomUUID().toString();
        final Ride ride = new Ride(rideId, joe, horse, ZonedDateTime.now(), Duration.ofHours(2), 48.208954, 16.365986, 48.208954, 16.365986);
        {
            final ResponseEntity<Ride> r = this.restTemplate.postForEntity("/ride", ride, Ride.class);
            Assert.assertEquals(201, r.getStatusCodeValue());
        }

        {
            this.restTemplate.delete("/ride/" + rideId);
            final ResponseEntity<Ride> r = this.restTemplate.getForEntity("/ride/" + rideId, Ride.class);
            Assert.assertEquals(404, r.getStatusCodeValue());
        }
        {
            this.restTemplate.delete("/person/" + personId);
            final ResponseEntity<Person> r = this.restTemplate.getForEntity("/person/" + personId, Person.class);
            Assert.assertEquals(404, r.getStatusCodeValue());
        }
        {
            this.restTemplate.delete("/horse/" + horseId);
            final ResponseEntity<Horse> r = this.restTemplate.getForEntity("/horse/" + horseId, Horse.class);
            Assert.assertEquals(404, r.getStatusCodeValue());
        }
        {
            this.restTemplate.delete("/stable/" + stableId);
            final ResponseEntity<Stable> r = this.restTemplate.getForEntity("/stable/" + stableId, Stable.class);
            Assert.assertEquals("got response " + r, 404, r.getStatusCodeValue());
        }
    }

}

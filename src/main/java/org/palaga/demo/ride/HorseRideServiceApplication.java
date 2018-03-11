package org.palaga.demo.ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@SpringBootApplication
@EnableFeignClients
public class HorseRideServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HorseRideServiceApplication.class, args);
    }
}

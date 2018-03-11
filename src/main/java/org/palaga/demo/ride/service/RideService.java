package org.palaga.demo.ride.service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.palaga.demo.ride.model.Person;
import org.palaga.demo.ride.model.Ride;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface RideService {

    List<Ride> getCurrentRides();

    Ride scheduleRide(ZonedDateTime start, Duration duration, Person rider, double geoLatitudeStart,
            double geoLongitudeStart, double geoLatitudeEnd, double geoLongitudeEnd) throws SchedulingException;

    List<Ride> getRides(ZonedDateTime start, Duration duration);
}

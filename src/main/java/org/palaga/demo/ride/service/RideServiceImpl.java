package org.palaga.demo.ride.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.palaga.demo.ride.model.Horse;
import org.palaga.demo.ride.model.Person;
import org.palaga.demo.ride.model.Ride;
import org.palaga.demo.ride.repo.HorseRepository;
import org.palaga.demo.ride.repo.RideRepository;
import org.palaga.demo.ride.repo.StableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.threeten.extra.Interval;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Service
public class RideServiceImpl implements RideService {

    Clock clock = Clock.systemDefaultZone();

    @Autowired
    StableRepository stableRepository;
    @Autowired
    HorseRepository horseRepository;
    @Autowired
    RideRepository rideRepository;

    @Override
    public List<Ride> getCurrentRides() {
        Instant now = clock.instant();
        return StreamSupport.stream(rideRepository.findAll().spliterator(), false)
                .filter(r -> Interval.of(r.getStart().toInstant(), r.getDuration()).contains(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ride> getRides(ZonedDateTime start, Duration duration) {
        final Interval interval = Interval.of(start.toInstant(), duration);
        return StreamSupport.stream(rideRepository.findAll().spliterator(), false)
                .filter(r -> interval.overlaps(Interval.of(r.getStart().toInstant(), r.getDuration())))
                .collect(Collectors.toList());
    }

    @Override
    public Ride scheduleRide(ZonedDateTime start, Duration duration, Person rider, double geoLatitudeStart,
            double geoLongitudeStart, double geoLatitudeEnd, double geoLongitudeEnd) throws SchedulingException {
        Stream<Horse> stream = StreamSupport.stream(horseRepository.findAll().spliterator(), false);
        Set<String> availableHorseIds = stream.map(h -> h.getHorseId()).collect(Collectors.toSet());

        final Interval interval = Interval.of(start.toInstant(), duration);
        StreamSupport.stream(rideRepository.findAll().spliterator(), false)
                .filter(r -> interval.overlaps(Interval.of(r.getStart().toInstant(), r.getDuration())))
                .forEach(r -> availableHorseIds.remove(r.getHorse().getHorseId()));

        if (availableHorseIds.isEmpty()) {
            throw new SchedulingException(String.format("No horse available for ride starting ar %s for %s", start, duration));
        }
        final Horse horse = horseRepository.findOne(availableHorseIds.iterator().next());
        Ride result = new Ride(UUID.randomUUID().toString(), rider, horse, start, duration, geoLatitudeStart, geoLongitudeStart, geoLatitudeEnd, geoLongitudeEnd);
        rideRepository.save(result);
        return result;
    }


}

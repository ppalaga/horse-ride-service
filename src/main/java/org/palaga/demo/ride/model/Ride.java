package org.palaga.demo.ride.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.rest.core.annotation.RestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Entity
public class Ride implements Serializable {

    /**  */
    private static final long serialVersionUID = -2760825879810595391L;

    private Duration duration;

    private double geoLatitudeEnd;

    private double geoLatitudeStart;
    private double geoLongitudeEnd;
    private double geoLongitudeStart;

    @ManyToOne(optional = false)
    @RestResource(exported = false)
    private Horse horse;
    @Id
    @Column(length = 40, updatable = false, nullable = false)
    private String rideId;
    @ManyToOne(optional = false)
    @RestResource(exported = false)
    private Person rider;
    private ZonedDateTime start;

    public Ride() {
    }

    public Ride(String rideId, Person rider, Horse horse, ZonedDateTime start, Duration duration,
            double geoLatitudeStart, double geoLongitudeStart, double geoLatitudeEnd, double geoLongitudeEnd) {
        super();
        this.rideId = rideId;
        this.rider = rider;
        this.horse = horse;
        this.start = start;
        this.duration = duration;
        this.geoLatitudeStart = geoLatitudeStart;
        this.geoLongitudeStart = geoLongitudeStart;
        this.geoLatitudeEnd = geoLatitudeEnd;
        this.geoLongitudeEnd = geoLongitudeEnd;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ride other = (Ride) obj;
        if (duration == null) {
            if (other.duration != null)
                return false;
        } else if (!duration.equals(other.duration))
            return false;
        if (Double.doubleToLongBits(geoLatitudeEnd) != Double.doubleToLongBits(other.geoLatitudeEnd))
            return false;
        if (Double.doubleToLongBits(geoLatitudeStart) != Double.doubleToLongBits(other.geoLatitudeStart))
            return false;
        if (Double.doubleToLongBits(geoLongitudeEnd) != Double.doubleToLongBits(other.geoLongitudeEnd))
            return false;
        if (Double.doubleToLongBits(geoLongitudeStart) != Double.doubleToLongBits(other.geoLongitudeStart))
            return false;
        if (horse == null) {
            if (other.horse != null)
                return false;
        } else if (!horse.equals(other.horse))
            return false;
        if (rideId == null) {
            if (other.rideId != null)
                return false;
        } else if (!rideId.equals(other.rideId))
            return false;
        if (rider == null) {
            if (other.rider != null)
                return false;
        } else if (!rider.equals(other.rider))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        return true;
    }

    public Duration getDuration() {
        return duration;
    }

    public double getGeoLatitudeEnd() {
        return geoLatitudeEnd;
    }

    public double getGeoLatitudeStart() {
        return geoLatitudeStart;
    }

    public double getGeoLongitudeEnd() {
        return geoLongitudeEnd;
    }

    public double getGeoLongitudeStart() {
        return geoLongitudeStart;
    }

    public Horse getHorse() {
        return horse;
    }

    public String getRideId() {
        return rideId;
    }

    public Person getRider() {
        return rider;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        long temp;
        temp = Double.doubleToLongBits(geoLatitudeEnd);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(geoLatitudeStart);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(geoLongitudeEnd);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(geoLongitudeStart);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((horse == null) ? 0 : horse.hashCode());
        result = prime * result + ((rideId == null) ? 0 : rideId.hashCode());
        result = prime * result + ((rider == null) ? 0 : rider.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setGeoLatitudeEnd(double geoLatitudeEnd) {
        this.geoLatitudeEnd = geoLatitudeEnd;
    }

    public void setGeoLatitudeStart(double geoLatitudeStart) {
        this.geoLatitudeStart = geoLatitudeStart;
    }

    public void setGeoLongitudeEnd(double geoLongitudeEnd) {
        this.geoLongitudeEnd = geoLongitudeEnd;
    }

    public void setGeoLongitudeStart(double geoLongitudeStart) {
        this.geoLongitudeStart = geoLongitudeStart;
    }

    public void setHorse(Horse horse) {
        this.horse = horse;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public void setRider(Person rider) {
        this.rider = rider;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return "Ride [rideId=" + rideId + ", rider=" + rider + ", horse=" + horse + ", start=" + start + ", duration="
                + duration + ", geoLatitudeStart=" + geoLatitudeStart + ", geoLongitudeStart=" + geoLongitudeStart
                + ", geoLatitudeEnd=" + geoLatitudeEnd + ", geoLongitudeEnd=" + geoLongitudeEnd + "]";
    }
}

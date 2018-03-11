package org.palaga.demo.ride.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Entity
public class Stable implements Serializable {
    private static final long serialVersionUID = 5429363222735167394L;

    private double geoLatitude;

    private double geoLongitude;
    private String name;
    @Id
    @Column(length = 40, updatable = false, nullable = false)
    private String stableId;

    public Stable() {
    }

    public Stable(String stableId, String name, double geoLatitude, double geoLongitude) {
        super();
        this.stableId = stableId;
        this.name = name;
        this.geoLatitude = geoLatitude;
        this.geoLongitude = geoLongitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stable other = (Stable) obj;
        if (Double.doubleToLongBits(geoLatitude) != Double.doubleToLongBits(other.geoLatitude))
            return false;
        if (Double.doubleToLongBits(geoLongitude) != Double.doubleToLongBits(other.geoLongitude))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (stableId == null) {
            if (other.stableId != null)
                return false;
        } else if (!stableId.equals(other.stableId))
            return false;
        return true;
    }

    public double getGeoLatitude() {
        return geoLatitude;
    }

    public double getGeoLongitude() {
        return geoLongitude;
    }

    public String getName() {
        return name;
    }

    public String getStableId() {
        return stableId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(geoLatitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(geoLongitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((stableId == null) ? 0 : stableId.hashCode());
        return result;
    }

    public void setGeoLatitude(double geoLatitude) {
        this.geoLatitude = geoLatitude;
    }

    public void setGeoLongitude(double geoLongitude) {
        this.geoLongitude = geoLongitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    @Override
    public String toString() {
        return "Stable [stableId=" + stableId + ", name=" + name + ", geoLatitude=" + geoLatitude + ", geoLongitude="
                + geoLongitude + "]";
    }

}

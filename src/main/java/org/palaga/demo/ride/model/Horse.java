package org.palaga.demo.ride.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.rest.core.annotation.RestResource;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Entity
public class Horse implements Serializable {
    public enum Sex {
        /** castrated male */
        gelding,
        /** Female */
        mare,
        /** non-castrated male */
        stallion
    }

    private static final long serialVersionUID = 5429363222735167394L;

    private LocalDate birthDate;

    private String breed;

    private String color;

    @Id
    @Column(length = 40, updatable = false, nullable = false)
    private String horseId;;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Sex sex;

    @ManyToOne(optional = false)
    @RestResource(exported = false)
    private Stable stable;

    public Horse() {
    }

    public Horse(String horseId, String horseName, Sex sex, LocalDate birthDate, String breed, String color,
            Stable stable) {
        super();
        this.horseId = horseId;
        this.birthDate = birthDate;
        this.breed = breed;
        this.color = color;
        this.name = horseName;
        this.sex = sex;
        this.stable = stable;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Horse other = (Horse) obj;
        if (birthDate == null) {
            if (other.birthDate != null)
                return false;
        } else if (!birthDate.equals(other.birthDate))
            return false;
        if (breed == null) {
            if (other.breed != null)
                return false;
        } else if (!breed.equals(other.breed))
            return false;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (horseId == null) {
            if (other.horseId != null)
                return false;
        } else if (!horseId.equals(other.horseId))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (sex != other.sex)
            return false;
        if (stable == null) {
            if (other.stable != null)
                return false;
        } else if (!stable.equals(other.stable))
            return false;
        return true;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBreed() {
        return breed;
    }

    public String getColor() {
        return color;
    }

    public String getHorseId() {
        return horseId;
    }

    public String getName() {
        return name;
    }

    public Sex getSex() {
        return sex;
    }

    public Stable getStable() {
        return stable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((birthDate == null) ? 0 : birthDate.hashCode());
        result = prime * result + ((breed == null) ? 0 : breed.hashCode());
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((horseId == null) ? 0 : horseId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sex == null) ? 0 : sex.hashCode());
        result = prime * result + ((stable == null) ? 0 : stable.hashCode());
        return result;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setHorseId(String horseId) {
        this.horseId = horseId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public void setStable(Stable stable) {
        this.stable = stable;
    }

    @Override
    public String toString() {
        return "Horse [birthDate=" + birthDate + ", breed=" + breed + ", color=" + color + ", horseId=" + horseId
                + ", name=" + name + ", sex=" + sex + ", stable=" + stable + "]";
    }

}

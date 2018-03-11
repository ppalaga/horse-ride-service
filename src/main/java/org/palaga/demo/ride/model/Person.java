package org.palaga.demo.ride.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Entity
public class Person implements Serializable {
    public enum Gender {
        female, male, other
    }

    /**  */
    private static final long serialVersionUID = -4491289891380048504L;

    public static long getSerialversionuid() {
        return serialVersionUID;
    };

    private String email;

    private String firstName;

    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private Gender gender;

    @Id
    @Column(length = 40, updatable = false, nullable = false)
    private String personId;

    private String phone;

    private String surname;

    public Person() {
    }

    public Person(String personId, String firstName, String surname, Gender gender, String phone, String email) {
        super();
        this.personId = personId;
        this.email = email;
        this.firstName = firstName;
        this.gender = gender;
        this.phone = phone;
        this.surname = surname;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (gender != other.gender)
            return false;
        if (personId == null) {
            if (other.personId != null)
                return false;
        } else if (!personId.equals(other.personId))
            return false;
        if (phone == null) {
            if (other.phone != null)
                return false;
        } else if (!phone.equals(other.phone))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPersonId() {
        return personId;
    }

    public String getPhone() {
        return phone;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((gender == null) ? 0 : gender.hashCode());
        result = prime * result + ((personId == null) ? 0 : personId.hashCode());
        result = prime * result + ((phone == null) ? 0 : phone.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        return result;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Person [personId=" + personId + ", firstName=" + firstName + ", surname=" + surname + ", gender="
                + gender + ", email=" + email + ", phone=" + phone + "]";
    }
}

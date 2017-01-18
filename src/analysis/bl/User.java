/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.bl;

import java.util.Objects;

/**
 *
 * @author rubinhus
 */
public class User {

    private final String email;
    private String phoneNumber;
    private String sessionid;
    private String name;

    public User(String name, String email, String phoneNumber, String sessionid) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.sessionid = sessionid;
        this.name = name;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.email);
        hash = 71 * hash + Objects.hashCode(this.sessionid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        return Objects.equals(this.sessionid, other.sessionid);
    }

    @Override
    public String toString() {
        return "User:" + this.hashCode() + ":{" + "email=" + email + ", phoneNumber=" + phoneNumber + ", sessionid=" + sessionid + ", name=" + name + '}';
    }

}

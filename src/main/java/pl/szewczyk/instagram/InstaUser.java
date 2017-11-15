package pl.szewczyk.instagram;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by przem on 16.09.2017.
 */
@Entity
@Table(name = "instauser", schema = "instabot")
public class InstaUser implements Serializable {

    @Id
    @SequenceGenerator(name="user_seq", sequenceName="user_seq", allocationSize=1, schema = "instabot")
    private Long id;

    private String instaUserName;

    @Column(name = "passwd")
    private byte[] password;

    private String accessToken;

    private String profilePictureURL;

    private String fullName;

    private String bio;

    private String website;

    private boolean business;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstaUserName() {
        return instaUserName;
    }

    public void setInstaUserName(String instaUserName) {
        this.instaUserName = instaUserName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isBusiness() {
        return business;
    }

    public void setBusiness(boolean business) {
        this.business = business;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstaUser instaUser = (InstaUser) o;

        if (!id.equals(instaUser.id)) return false;
        return instaUserName.equals(instaUser.instaUserName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + instaUserName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstaUser{" +
                "id=" + id +
                ", instaUserName='" + instaUserName + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", profilePictureURL='" + profilePictureURL + '\'' +
                ", fullName='" + fullName + '\'' +
                ", bio='" + bio + '\'' +
                ", website='" + website + '\'' +
                ", business=" + business +
                '}';
    }
}

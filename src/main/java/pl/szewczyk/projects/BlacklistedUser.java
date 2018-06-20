package pl.szewczyk.projects;

import javax.persistence.Embeddable;

@Embeddable
public class BlacklistedUser {
    private String username;
    private String profilePicUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    @Override
    public String toString() {
        return "BlacklistedUser{" +
                "username='" + username + '\'' +
                '}';
    }
}

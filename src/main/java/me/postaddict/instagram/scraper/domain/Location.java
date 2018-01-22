package me.postaddict.instagram.scraper.domain;

import java.util.Map;

/**
 * Created by przem on 19.12.2017.
 */
public class Location {
    private String pk;
    private String name;
    private String address;//	"Zamek Wawel 9"
    private String city;
    private String shortName;//	"Kraków Zamek Wawel"
    private Double lng;//	19.934503576673
    private Double lat;//	50.053305963108
    private String externalSource;    //"facebook_places"
    private Double facebookPlacesId;    //187558774767720
    private String title;//	"Kraków Zamek Wawel"
    private String subtitle;//	"Zamek Wawel 9"
    private String slug;//	"krakow-zamek-wawel"

    public static Location fromApi(Map map) {
        Location location = new Location();
        location.setPk((String) map.get("pk"));
        location.setName((String) map.get("name"));
        location.setAddress((String) map.get("address"));
        location.setCity((String) map.get("city"));
        location.setShortName((String) map.get("short_name"));
        location.setLng((Double) map.get("lng"));
        location.setLat((Double) map.get("lat"));
        location.setExternalSource((String) map.get("external_source"));
        location.setFacebookPlacesId((Double) map.get("facebook_places_id"));
        location.setTitle((String) map.get("title"));
        location.setSubtitle((String) map.get("subtitle"));
        location.setSlug((String) map.get("slug"));

        return location;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }

    public Double getFacebookPlacesId() {
        return facebookPlacesId;
    }

    public void setFacebookPlacesId(Double facebookPlacesId) {
        this.facebookPlacesId = facebookPlacesId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}

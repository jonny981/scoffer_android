package com.scoff.scoffer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class OfferItem implements Serializable {

    private String offerStartDate, offerExpiryDate, furtherInfo, companyName, description, type,
            emailAddress, offerTitle, phoneNum, imageURL, title, url, openingTime, closingTime;
    private double latitude, longitude, distance;
    private boolean favourite;

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    private int companyID;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOfferStartDate() {
        return offerStartDate;
    }

    public void setOfferStartDate(String offerStartDate) {
        this.offerStartDate = offerStartDate;
    }

    public String getOfferExpiryDate() {
        return offerExpiryDate;
    }

    public void setOfferExpiryDate(String offerExpiryDate) {
        this.offerExpiryDate = offerExpiryDate;
    }

    public String getFurtherInfo() {
        return furtherInfo;
    }

    public void setFurtherInfo(String furtherInfo) {
        this.furtherInfo = furtherInfo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getOfferTitle() {
        return offerTitle;
    }

    public void setOfferTitle(String offerTitle) {
        this.offerTitle = offerTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public OfferItem buildFromJSON(JSONObject offer, int index) {
        try {
            this.setCompanyName(offer.getString("CompanyName"));
            this.setCompanyID(offer.getInt("CompanyID"));
            this.setTitle(offer.getString("OfferTitle"));
            this.setDescription(offer.getString("Description"));
            this.setFurtherInfo(offer.getString("FurtherInfo"));
            this.setOfferExpiryDate(offer.getString("OfferExpiryDate"));
            this.setType(offer.getString("Type"));
            this.setLatitude(offer.getDouble("Latitude"));
            this.setLongitude(offer.getDouble("Longitude"));
            this.setEmailAddress(offer.getString("Email"));
            this.setPhoneNum(offer.getString("PhoneNum"));
            this.setUrl(offer.getString("WebsiteURL"));
            this.setDistance(offer.getDouble("distance"));
            this.setOpeningTime(offer.getString("OpeningTime"));
            this.setClosingTime(offer.getString("ClosingTime"));
            this.setImageURL(offer.getString("ImageURL"));
            this.setFavourite(false);
//            this.setImageURL("http://scoffer.ddns.net/images/" + offer.getString("OfferID") + ".jpg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return "OfferItem{" +
                "offerStartDate='" + offerStartDate + '\'' +
                ",\n offerExpiryDate='" + offerExpiryDate + '\'' +
                ",\n furtherInfo='" + furtherInfo + '\'' +
                ",\n companyName='" + companyName + '\'' +
                ",\n description='" + description + '\'' +
                ",\n type='" + type + '\'' +
                ",\n emailAddress='" + emailAddress + '\'' +
                ",\n offerTitle='" + offerTitle + '\'' +
                ",\n phoneNum='" + phoneNum + '\'' +
                ",\n imageURL='" + imageURL + '\'' +
                ",\n title='" + title + '\'' +
                ",\n url='" + url + '\'' +
                ",\n openingTime='" + openingTime + '\'' +
                ",\n closingTime='" + closingTime + '\'' +
                ",\n latitude=" + latitude +
                ",\n longitude=" + longitude +
                ",\n distance=" + distance +
                ",\n favourite=" + favourite +
                ",\n companyID=" + companyID +
                '}';
    }
}


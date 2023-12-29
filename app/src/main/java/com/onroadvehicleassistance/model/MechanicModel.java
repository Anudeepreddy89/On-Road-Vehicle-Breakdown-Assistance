package com.onroadvehicleassistance.model;

import java.io.Serializable;

public class MechanicModel implements Serializable {
    String mechanicName;
    String mechanicImage;
    String mechanicDescription;
    String mechanicAddress;
    String mechanicFindingLocation;
    String mechanicPhoneString;
    String mechanicCity;

    public MechanicModel(String mechanicName, String mechanicImage, String mechanicDescription, String mechanicAddress, String mechanicFindingLocation, String mechanicPhoneString, String mechanicCity) {
        this.mechanicName = mechanicName;
        this.mechanicImage = mechanicImage;
        this.mechanicDescription = mechanicDescription;
        this.mechanicAddress = mechanicAddress;
        this.mechanicFindingLocation = mechanicFindingLocation;
        this.mechanicPhoneString = mechanicPhoneString;
        this.mechanicCity = mechanicCity;
    }

    public MechanicModel() {
    }

    public String getMechanicCity() {
        return mechanicCity;
    }

    public void setMechanicCity(String mechanicCity) {
        this.mechanicCity = mechanicCity;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName) {
        this.mechanicName = mechanicName;
    }

    public String getMechanicImage() {
        return mechanicImage;
    }

    public void setMechanicImage(String mechanicImage) {
        this.mechanicImage = mechanicImage;
    }

    public String getMechanicDescription() {
        return mechanicDescription;
    }

    public void setMechanicDescription(String mechanicDescription) {
        this.mechanicDescription = mechanicDescription;
    }

    public String getMechanicAddress() {
        return mechanicAddress;
    }

    public void setMechanicAddress(String mechanicAddress) {
        this.mechanicAddress = mechanicAddress;
    }

    public String getMechanicFindingLocation() {
        return mechanicFindingLocation;
    }

    public void setMechanicFindingLocation(String mechanicFindingLocation) {
        this.mechanicFindingLocation = mechanicFindingLocation;
    }

    public String getMechanicPhoneString() {
        return mechanicPhoneString;
    }

    public void setMechanicPhoneString(String mechanicPhoneString) {
        this.mechanicPhoneString = mechanicPhoneString;
    }
}

package com.sanny_tech.carapp.taxi_utils;

public class TaxiPrice {
    private Double Economy;
    private Double Classic;

    private Double Xl;

    private Double BodaBoda;

    public TaxiPrice() {
    }

    public Double getEconomy() {
        return Economy;
    }

    public void setEconomy(Double economy) {
        Economy = economy;
    }

    public Double getClassic() {
        return Classic;
    }

    public void setClassic(Double classic) {
        Classic = classic;
    }

    public Double getXl() {
        return Xl;
    }

    public void setXl(Double xl) {
        Xl = xl;
    }

    public Double getBodaBoda() {
        return BodaBoda;
    }

    public void setBodaBoda(Double bodaBoda) {
        BodaBoda = bodaBoda;
    }
}

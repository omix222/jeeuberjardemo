package com.example.jeedemo;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * JSF backing bean for mileage ↔ kilometer conversion.
 */
@Named
@RequestScoped
public class MileageBean {

    private Double kilometers;
    private Double miles;
    private String resultMessage;

    public void kmToMiles() {
        if (kilometers != null && kilometers >= 0) {
            miles = kilometers * 0.621371;
            resultMessage = String.format("%.3f km  =  %.4f マイル", kilometers, miles);
        } else {
            resultMessage = "キロメートルを入力してください。";
        }
    }

    public void milesToKm() {
        if (miles != null && miles >= 0) {
            kilometers = miles * 1.60934;
            resultMessage = String.format("%.4f マイル  =  %.3f km", miles, kilometers);
        } else {
            resultMessage = "マイルを入力してください。";
        }
    }

    public Double getKilometers() { return kilometers; }
    public void setKilometers(Double kilometers) { this.kilometers = kilometers; }

    public Double getMiles() { return miles; }
    public void setMiles(Double miles) { this.miles = miles; }

    public String getResultMessage() { return resultMessage; }
}

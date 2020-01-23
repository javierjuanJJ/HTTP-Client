package dam.androidjavierjuanuceda.u5t9httpclient;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class GeonamesPlace {
    private String description;
    private double latitute;
    private double longitude;

    public GeonamesPlace(String description, double latitute, double longitude) {
        this.description = description;
        this.latitute = latitute;
        this.longitude = longitude;
    }

    public GeonamesPlace() {
        this.description = "No information found";
        this.latitute = 0.0;
        this.longitude = 0.0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String toString() {
        return this.getDescription() + System.lineSeparator() + "LAT " + this.getLatitute() + ", LON " + this.getLongitude();
    }
}

package dam.androidjavierjuanuceda.u5t9httpclient;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class GeonamesPlace {
    private String description;
    private double latitute;
    private double longitude;
    private static final double COORDENATES_NOT_FOUND = 0.0;

    public GeonamesPlace(String description, double latitute, double longitude) {
        this.description = description;
        this.latitute = latitute;
        this.longitude = longitude;
    }

    public GeonamesPlace() {
        this.description = MainActivity.getMainActivity().getString(R.string.No_information_found);
        this.latitute = COORDENATES_NOT_FOUND;
        this.longitude = COORDENATES_NOT_FOUND;
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
        return (this.latitute == COORDENATES_NOT_FOUND && this.longitude == COORDENATES_NOT_FOUND) ? this.getDescription() : this.getDescription() + System.lineSeparator() + "LAT " + this.getLatitute() + ", LON " + this.getLongitude();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        GeonamesPlace geonamesPlace = (GeonamesPlace) obj;
        return (this.latitute == geonamesPlace.latitute && this.longitude == geonamesPlace.longitude);
    }
}

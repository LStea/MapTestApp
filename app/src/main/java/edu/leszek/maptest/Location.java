package edu.leszek.maptest;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

import java.util.prefs.Preferences;

/**
 * Created by Michal on 01.02.2018.
 */

public final class Location {
    private final LatLng mPosition;
    private final float mRadius;
    private final String mName;
    private final String mDescription;

    public static final String PLACES_LAT_KEY = "places_lat";
    public static final String PLACES_LONG_KEY = "places_long";
    public static final String PLACES_NAME_KEY = "places_name";
    public static final String PLACES_DESCRIPTION_KEY = "places_description";
    public static final String PLACES_RADIUS_KEY = "places_radius";

    public Location(LatLng mPosition, float mRadius, String mName, String mDescription) {
        this.mPosition = mPosition;
        this.mRadius = mRadius;
        this.mName = mName;
        this.mDescription = mDescription;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public void saveToSharedPreferences(int id, SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(PLACES_NAME_KEY + id, mName);
        editor.putString(PLACES_DESCRIPTION_KEY + id, mDescription);
        editor.putInt(PLACES_LAT_KEY + id, (int)Math.round(mPosition.latitude * 1000000.0));
        editor.putInt(PLACES_LONG_KEY + id, (int)Math.round(mPosition.longitude * 1000000.0));
        editor.putFloat(PLACES_RADIUS_KEY + id, mRadius);

        editor.commit();
    }

    public static Location readFromSharedPreferences(int id, SharedPreferences preferences) {
        return new Location(
                new LatLng(preferences.getInt(PLACES_LAT_KEY + id, 0) / 1000000.0, preferences.getInt(PLACES_LONG_KEY + id, 0) / 1000000.0),
                preferences.getFloat(PLACES_RADIUS_KEY + id, 5f),
                preferences.getString(PLACES_NAME_KEY + id, "Nieznane miejsce " + id),
                preferences.getString(PLACES_DESCRIPTION_KEY+ id, "Brak opisu")
        );
    }

    public float getRadius() {
        return mRadius;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (Float.compare(location.mRadius, mRadius) != 0) return false;
        if (!mPosition.equals(location.mPosition)) return false;
        if (!mName.equals(location.mName)) return false;
        return mDescription.equals(location.mDescription);
    }

    @Override
    public int hashCode() {
        int result = mPosition.hashCode();
        result = 31 * result + (mRadius != +0.0f ? Float.floatToIntBits(mRadius) : 0);
        result = 31 * result + mName.hashCode();
        result = 31 * result + mDescription.hashCode();
        return result;
    }
}

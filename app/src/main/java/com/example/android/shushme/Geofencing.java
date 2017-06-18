package com.example.android.shushme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author meugen
 */

public class Geofencing {

    private static final long GEOFENCE_EXPIRATION_DURATION
            = TimeUnit.HOURS.toMillis(24);
    private static final float GEOFENCE_RADIUS = 50;

    private final Context context;
    private final GoogleApiClient client;
    private List<Geofence> geofenceList;

    public Geofencing(final Context context, final GoogleApiClient client) {
        this.context = context;
        this.client = client;
        geofenceList = new ArrayList<>();
    }

    public void updateGeofencesList(final PlaceBuffer places) {
        geofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) {
            return;
        }
        for (Place place : places) {
            final LatLng latLng = place.getLatLng();

            final Geofence geofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setExpirationDuration(GEOFENCE_EXPIRATION_DURATION)
                    .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            geofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .addGeofences(geofenceList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        final Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void registerAllGeofences() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(client,
                    getGeofencingRequest(), getGeofencePendingIntent());
        }
    }

    public void unRegisterAllGeofences() {
        LocationServices.GeofencingApi.removeGeofences(client,
                getGeofencePendingIntent());
    }
}

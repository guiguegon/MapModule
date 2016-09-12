package es.guiguegon.mapmodule.helpers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;

/**
 * Created by guiguegon on 23/10/2015.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MAX_RESULTS = 5;
    private static final long TWO_MINUTES = 2 * 60 * 1000;

    private static LocationHelper mInstance;
    private WeakReference<AppCompatActivity> activityWeak;

    // Google Api vars
    private GoogleApiClient mGoogleApiClient;

    private LocationHelper() {
    }

    public static LocationHelper getInstance() {
        if (mInstance == null) {
            mInstance = new LocationHelper();
        }
        return mInstance;
    }

    public static boolean isLocationValid(Location location) {
        long timeDelta = new Date().getTime() - location.getTime();
        return timeDelta <= TWO_MINUTES;
    }

    public void onStart(AppCompatActivity activity) {
        activityWeak = new WeakReference<>(activity);
        initGoogleApiClient();
        connect();
    }

    public void onStop() {
        activityWeak.clear();
        activityWeak = null;
        disconnect();
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activityWeak.get()).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public boolean isConnected() {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
        } else {
            return false;
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void getLocationFromAddress(final String address) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(activityWeak.get(), Locale.getDefault());
                    geocoder.getFromLocationName(address, MAX_RESULTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getLocationFromLatLng(final LatLng latLng) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(activityWeak.get(), Locale.getDefault());
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, MAX_RESULTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void requestLocationUpdate(ViewGroup root, LocationRequest locationRequest,
            final LocationListener locationListener) {
        try {
            if (mGoogleApiClient.isConnected() && activityWeak != null && activityWeak.get() != null) {
                if (ActivityCompat.checkSelfPermission(activityWeak.get(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(activityWeak.get(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission(root, locationRequest, locationListener);
                }
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null && isLocationValid(location)) {
                    locationListener.onLocationChanged(location);
                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest,
                            locationListener);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermission(ViewGroup root, LocationRequest locationRequest,
            final LocationListener locationListener) {
        try {
            PermissionsManager.requestMultiplePermissions(root,
                    () -> requestLocationUpdate(root, locationRequest, locationListener),
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        } catch (Exception e) {
            //empty
        }
    }

    // Google Api callbacks
    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}

package es.guiguegon.mapmodule.helpers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by guiguegon on 23/10/2015.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MAX_RESULTS = 5;
    private static final long TWO_MINUTES = 2 * 60 * 1000;

    private static LocationHelper sInstance;
    private AppCompatActivity activity;

    // Google Api vars
    private GoogleApiClient mGoogleApiClient;
    private LocationHelperListener locationHelperListener;

    private LocationHelper() {
    }

    public static LocationHelper getInstance() {
        if (sInstance == null) {
            sInstance = new LocationHelper();
        }
        return sInstance;
    }

    public static boolean isLocationValid(Location location) {
        long timeDelta = new Date().getTime() - location.getTime();
        return timeDelta <= TWO_MINUTES;
    }

    public void setLocationHelperListener(LocationHelperListener locationHelperListener) {
        this.locationHelperListener = locationHelperListener;
    }

    public void onStart(AppCompatActivity activity) {
        this.activity = activity;
        initGoogleApiClient();
        connect();
    }

    public void onStop() {
        activity = null;
        locationHelperListener = null;
        disconnect();
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity).addConnectionCallbacks(this)
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

    //<editor-fold desc="get location from address">
    public void getLocationFromAddressAsync(final String address) {
        final Observable<List<Address>> observable =
                Observable.create((Observable.OnSubscribe<List<Address>>) subscriber -> {
                    subscriber.onNext(getLocationFromAddress(address));
                    subscriber.onCompleted();
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(this::onGotAddress, this::onError);
    }

    private List<Address> getLocationFromAddress(final String address) {
        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            return geocoder.getFromLocationName(address, MAX_RESULTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getLocationFromLatLngAsync(final LatLng latLng) {
        final Observable<List<Address>> observable =
                Observable.create((Observable.OnSubscribe<List<Address>>) subscriber -> {
                    subscriber.onNext(getLocationFromLatLng(latLng));
                    subscriber.onCompleted();
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(this::onGotAddress, this::onError);
    }

    private List<Address> getLocationFromLatLng(final LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            return geocoder.getFromLocation(latLng.latitude, latLng.longitude, MAX_RESULTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onError(Throwable throwable) {
        if (locationHelperListener != null) {
            locationHelperListener.onError();
        }
    }

    private void onGotAddress(List<Address> addresses) {
        if (locationHelperListener != null) {
            locationHelperListener.onGotAddress(addresses);
        }
    }
    //</editor-fold>

    public void requestLocationUpdate(ViewGroup root, LocationRequest locationRequest,
            final LocationListener locationListener) {
        try {
            if (mGoogleApiClient.isConnected() && activity != null) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
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

    public interface LocationHelperListener {
        void onGotAddress(List<Address> addresses);

        void onError();
    }
}

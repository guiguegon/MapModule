package es.guiguegon.mapmodule.helpers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import es.guiguegon.mapmodule.R;
import es.guiguegon.mapmodule.model.Place;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by guiguegon on 23/10/2015.
 */
public class GoogleMapHelper
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapLongClickListener {

    public final static int SMALL_ZOOM = 7;
    public final static int NORMAL_ZOOM = 15;

    private static GoogleMapHelper mInstance;
    private SupportMapFragment supportMapFragment;

    private GoogleMap mMap;
    private WeakReference<AppCompatActivity> activityWeak;
    private WeakReference<GoogleMap.OnMapLongClickListener> onMapLongClickListenerWeak;
    private WeakReference<GoogleMap.OnMapLoadedCallback> onMapLoadedListenerWeak;

    private ArrayList<Place> places = new ArrayList<>();
    private boolean isLoaded;

    public static GoogleMapHelper getInstance() {
        if (mInstance == null) {
            mInstance = new GoogleMapHelper();
        }
        return mInstance;
    }

    private GoogleMapHelper() {
    }

    public void onStart(AppCompatActivity activity) {
        activityWeak = new WeakReference<>(activity);
    }

    public void onStop() {
        activityWeak.clear();
        activityWeak = null;
    }

    public void setOnMapLoadedListener(GoogleMap.OnMapLoadedCallback onMapLoadedListener) {
        this.onMapLoadedListenerWeak = new WeakReference<>(onMapLoadedListener);
    }

    public void setOnMapLongClickListener(GoogleMap.OnMapLongClickListener onMapLongClickListenerWeak) {
        this.onMapLongClickListenerWeak = new WeakReference<>(onMapLongClickListenerWeak);
    }

    public boolean isMapLoaded() {
        return mMap != null && isLoaded;
    }

    private void initMapFragment() {
        supportMapFragment = (SupportMapFragment) activityWeak.get()
                .getSupportFragmentManager()
                .findFragmentByTag(SupportMapFragment.class.getSimpleName());
        if (supportMapFragment == null) {
            supportMapFragment = new SupportMapFragment();
        }
    }

    public void loadMap(Place place, int mapResId) {
        loadMap(mapResId);
        addPlace(place);
    }

    public void loadMap(int mapResId) {
        initMapFragment();
        activityWeak.get()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(mapResId, supportMapFragment, SupportMapFragment.class.getSimpleName())
                .commit();
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
    }

    @Override
    public void onMapLoaded() {
        isLoaded = true;
        if (onMapLoadedListenerWeak != null && onMapLoadedListenerWeak.get() != null) {
            onMapLoadedListenerWeak.get().onMapLoaded();
        }
    }

    private void setupMap() {
        try {
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnMapLoadedCallback(this);
            mMap.setOnMapLongClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPlace(Place place) {
        places.add(place);
    }

    public void addPlace(ArrayList<Place> places) {
        this.places.addAll(places);
    }

    public void clearMarkers() {
        places.clear();
    }

    public void paintMarker(Place place) {
        addPlace(place);
        animateCamera(place.getPosition());
    }

    public void paintMarker(ArrayList<Place> places) {
        addPlace(places);
        animateCamera(generateLatLngBoundsFromMarkers());
    }

    public LatLngBounds generateLatLngBoundsFromMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Place place : places) {
            builder.include(place.getPosition());
        }
        return builder.build();
    }

    public void moveCamera(LatLng latLng) {
        moveCamera(latLng, NORMAL_ZOOM);
    }

    public void moveCamera(LatLng latLng, int zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void moveCamera(LatLngBounds latLngBounds) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,
                (int) activityWeak.get().getResources().getDimension(R.dimen.map_padding)));
    }

    public void animateCamera(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        animateCamera(latLng, NORMAL_ZOOM);
    }

    public void animateCamera(LatLng latLng) {
        animateCamera(latLng, NORMAL_ZOOM);
    }

    public void animateCamera(LatLng latLng, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void animateCamera(LatLngBounds latLngBounds) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,
                (int) activityWeak.get().getResources().getDimension(R.dimen.map_padding)));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (onMapLongClickListenerWeak != null
                && onMapLongClickListenerWeak.get() != null
                && onMapLongClickListenerWeak != null
                && onMapLongClickListenerWeak.get() != null) {
            onMapLongClickListenerWeak.get().onMapLongClick(latLng);
        }
    }

    public void initMyPosition() {
        checkPermission();
    }

    private void checkPermission() {
        try {
            PermissionsManager.requestMultiplePermissions((ViewGroup) supportMapFragment.getView(),
                    this::setMyPositionEnabled, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        } catch (Exception e) {
            //empty
        }
    }

    private void setMyPositionEnabled() {
        if (ActivityCompat.checkSelfPermission(supportMapFragment.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(supportMapFragment.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}


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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import es.guiguegon.mapmodule.R;
import es.guiguegon.mapmodule.model.Place;
import java.util.ArrayList;

/**
 * Created by guiguegon on 23/10/2015.
 */
public class GoogleMapHelper
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, ClusterManager.OnClusterItemClickListener<Place> {

    public final static int NORMAL_ZOOM = 14;

    private static GoogleMapHelper sInstance;
    private SupportMapFragment supportMapFragment;
    private AppCompatActivity activity;

    private GoogleMap mMap;
    private ClusterManager<Place> mClusterManager;

    // listeners
    private GoogleMap.OnMapLongClickListener onMapLongClickListener;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnMapLoadedCallback onMapLoadedListener;
    private ClusterManager.OnClusterItemClickListener<Place> onClusterItemClickListener;

    private ArrayList<Place> places = new ArrayList<>();

    private GoogleMapHelper() {
    }

    public static GoogleMapHelper getInstance() {
        if (sInstance == null) {
            sInstance = new GoogleMapHelper();
        }
        return sInstance;
    }

    public void onStart(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void onStop() {
        activity = null;
        onMapClickListener = null;
        onMapLoadedListener = null;
        onMapClickListener = null;
        onClusterItemClickListener = null;
    }

    public void setOnMapLoadedListener(GoogleMap.OnMapLoadedCallback onMapLoadedListener) {
        this.onMapLoadedListener = onMapLoadedListener;
    }

    public void setOnMapLongClickListener(GoogleMap.OnMapLongClickListener onMapLongClickListener) {
        this.onMapLongClickListener = onMapLongClickListener;
    }

    public void setOnMapClickListener(GoogleMap.OnMapClickListener onMapClickListener) {
        this.onMapClickListener = onMapClickListener;
    }

    public void setOnClusterItemClickListener(
            ClusterManager.OnClusterItemClickListener<Place> onClusterItemClickListener) {
        this.onClusterItemClickListener = onClusterItemClickListener;
    }

    private void initMapFragment() {
        supportMapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(SupportMapFragment.class.getSimpleName());
        if (supportMapFragment == null) {
            supportMapFragment = new SupportMapFragment();
        }
    }

    public void loadMap(int mapResId) {
        initMapFragment();
        activity.getSupportFragmentManager()
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
        if (onMapLoadedListener != null) {
            onMapLoadedListener.onMapLoaded();
        }
    }

    private void setupMap() {
        try {
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnMapLoadedCallback(this);
            mMap.setOnMapLongClickListener(this);
            mMap.setOnMapClickListener(this);
            setupClusterManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClusterManager() {
        mClusterManager = new ClusterManager<>(activity, mMap);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnCameraIdleListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setRenderer(new DefaultClusterRenderer<Place>(activity, mMap, mClusterManager) {
            @Override
            protected boolean shouldRenderAsCluster(Cluster cluster) {
                return false;
            }
        });
    }

    private void refreshMap() {
        mClusterManager.clearItems();
        mClusterManager.cluster();
        mClusterManager.addItems(places);
        mClusterManager.cluster();
    }

    private void addPlace(Place place) {
        places.add(place);
    }

    private void addPlace(ArrayList<Place> places) {
        this.places.addAll(places);
    }

    public void clearMarkers() {
        places.clear();
    }

    public void paintMarker(Place place) {
        addPlace(place);
        refreshMap();
        animateCamera(place.getPosition());
    }

    public void paintMarker(ArrayList<Place> places) {
        addPlace(places);
        refreshMap();
        animateCamera(generateLatLngBoundsFromMarkers());
    }

    private LatLngBounds generateLatLngBoundsFromMarkers() {
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
                (int) activity.getResources().getDimension(R.dimen.map_padding)));
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
                (int) activity.getResources().getDimension(R.dimen.map_padding)));
    }

    // My position
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

    // Click listeners
    @Override
    public void onMapLongClick(LatLng latLng) {
        if (onMapLongClickListener != null) {
            onMapLongClickListener.onMapLongClick(latLng);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (onMapClickListener != null) {
            onMapClickListener.onMapClick(latLng);
        }
    }

    @Override
    public boolean onClusterItemClick(Place place) {
        if (onClusterItemClickListener != null) {
            onClusterItemClickListener.onClusterItemClick(place);
        }
        return false;
    }
}


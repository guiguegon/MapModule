package es.guiguegon.mapmodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.karumi.dexter.Dexter;
import es.guiguegon.mapmodule.adapters.AddressAdapter;
import es.guiguegon.mapmodule.helpers.GoogleMapHelper;
import es.guiguegon.mapmodule.helpers.LocationHelper;
import es.guiguegon.mapmodule.model.Place;
import es.guiguegon.mapmodule.utils.LocationUtils;
import es.guiguegon.mapmodule.utils.Utils;
import java.util.List;

/**
 * Created by guiguegon on 09/09/16.
 */
public class MapActivity extends AppCompatActivity
        implements GoogleMap.OnMapLoadedCallback, LocationListener, LocationHelper.LocationHelperListener,
        AddressAdapter.AddressAdapterListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        ClusterManager.OnClusterItemClickListener<Place> {

    public static final String RESULT_PLACE = "result_place";

    // Dependencies
    LocationHelper locationHelper;
    GoogleMapHelper googleMapHelper;

    // Views
    ImageView myLocationBtn;
    ProgressBar loadingBar;
    ImageView submitBtn;
    EditText submitEditText;
    CardView resultsLayout;
    RecyclerView resultsRecycler;
    CardView locationSelectedLayout;
    TextView locationSelectedRawAddress;
    FloatingActionButton locationSelectedFAB;

    // Adapters
    AddressAdapter addressAdapter;

    // Vars
    Place selectedPlace;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MapActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dexter.initialize(this);
        googleMapHelper = GoogleMapHelper.getInstance();
        locationHelper = LocationHelper.getInstance();
        if (savedInstanceState == null) {
        }
        setContentView(R.layout.activity_map); //something wrong
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
        }
        bindViews();
        loadMap();
        setClickListeners();
        locationResultPanel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleMapHelper.onStart(this);
        googleMapHelper.setOnMapClickListener(this);
        googleMapHelper.setOnMapLongClickListener(this);
        googleMapHelper.setOnClusterItemClickListener(this);
        locationHelper.onStart(this);
        locationHelper.setLocationHelperListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleMapHelper.onStop();
        locationHelper.onStop();
    }

    private void locationResultPanel() {
        addressAdapter = new AddressAdapter();
        addressAdapter.setAddressAdapterListener(this);
        resultsRecycler.setAdapter(addressAdapter);
        resultsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resultsRecycler.setItemAnimator(new DefaultItemAnimator());
    }

    public void fillSelectedResult(Address address) {
        fillSelectedResult(new Place(address));
    }

    public void fillSelectedResult(Place place) {
        this.selectedPlace = place;
        locationSelectedRawAddress.setText(LocationUtils.getFullAddress(selectedPlace));
    }

    private void setClickListeners() {
        myLocationBtn.setOnClickListener(this::onMyLocationBtn);
        submitBtn.setOnClickListener(this::onSubmitBtn);
        locationSelectedFAB.setOnClickListener(this::onLocationSelected);
    }

    private void loadMap() {
        googleMapHelper.loadMap(R.id.map_view);
        googleMapHelper.setOnMapLoadedListener(this);
    }

    private void bindViews() {
        myLocationBtn = (ImageView) findViewById(R.id.my_location_btn);
        submitBtn = (ImageView) findViewById(R.id.toolbar_search_submit);
        loadingBar = (ProgressBar) findViewById(R.id.toolbar_progress);
        submitEditText = (EditText) findViewById(R.id.toolbar_edittext);
        resultsLayout = (CardView) findViewById(R.id.results_layout);
        resultsRecycler = (RecyclerView) findViewById(R.id.results_recycler);
        locationSelectedLayout = (CardView) findViewById(R.id.location_selected_layout);
        locationSelectedRawAddress = (TextView) findViewById(R.id.item_address_raw_address);
        locationSelectedFAB = (FloatingActionButton) findViewById(R.id.location_selected_fab);
    }

    // Panels
    private void queryResultsPanel(boolean queryResultsPanel) {
        //this.queryResultsPanel = queryResultsPanel;
        Animation animation;
        if (queryResultsPanel) {
            animation = AnimationUtils.loadAnimation(this, R.anim.ytranslate_from_100_to_0_in_400);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    resultsLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            resultsLayout.startAnimation(animation);
        } else {
            animation = AnimationUtils.loadAnimation(this, R.anim.ytranslate_from_0_to_100_in_400);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    resultsLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            resultsLayout.startAnimation(animation);
        }
    }

    private void selectedLocationPanel(boolean selectedLocationPanel) {
        //this.selectedLocationPanel = selectedLocationPanel;
        Animation animation;
        if (selectedLocationPanel) {
            animation = AnimationUtils.loadAnimation(this, R.anim.ytranslate_from_100_to_0_in_400);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    locationSelectedLayout.setVisibility(View.VISIBLE);
                    locationSelectedFAB.animate().alpha(1f).setDuration(400).start();
                    locationSelectedFAB.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            locationSelectedLayout.startAnimation(animation);
        } else {
            animation = AnimationUtils.loadAnimation(this, R.anim.ytranslate_from_0_to_100_in_400);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    locationSelectedFAB.animate().alpha(0f).setDuration(400).start();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    locationSelectedLayout.setVisibility(View.GONE);
                    locationSelectedFAB.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        locationSelectedLayout.startAnimation(animation);
    }

    // Loading
    private void setLoadingAddress() {
        submitBtn.setVisibility(View.GONE);
        loadingBar.setVisibility(View.VISIBLE);
    }

    private void setLoadingComplete() {
        submitBtn.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.GONE);
    }

    // OnClickListeners
    private void onSubmitBtn(View view) {
        if (!TextUtils.isEmpty(submitEditText.getText())) {
            locationHelper.getLocationFromAddressAsync(submitEditText.getText().toString());
            setLoadingAddress();
            Utils.hideKeyboardFrom(this, getCurrentFocus());
        }
    }

    private void onMyLocationBtn(View view) {
        locationHelper.requestLocationUpdate((ViewGroup) view.getRootView(),
                LocationUtils.createRequestLocationUpdateOnce(), this);
    }

    private void onLocationSelected(View view) {
        if (selectedPlace != null) {
            Intent intent = new Intent();
            intent.putExtra(MapActivity.RESULT_PLACE, selectedPlace);
            setResult(Activity.RESULT_OK, intent);
            supportFinishAfterTransition();
        }
    }

    // GoogleMap.OnMapLoadedCallback interface
    @Override
    public void onMapLoaded() {
        googleMapHelper.initMyPosition();
    }

    // GoogleMap.OnMapClickListener interface
    @Override
    public void onMapClick(LatLng latLng) {
        Log.i("[MapActivity]", "onMapClick " + latLng.toString());
        googleMapHelper.clearMarkers();
        googleMapHelper.paintMarker(new Place(latLng));
    }

    // ClusterManager.OnClusterItemClickListener<Place> interface
    @Override
    public boolean onClusterItemClick(Place place) {
        Log.i("[MapActivity]", "onMapClick " + place.getPosition().toString());
        selectedPlace = place;
        fillSelectedResult(place);
        selectedLocationPanel(true);
        return true;
    }

    // GoogleMap.OnMapLongClickListener interface
    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.i("[MapActivity]", "onMapLongClick " + latLng.toString());
        locationHelper.getLocationFromLatLngAsync(latLng);
        setLoadingAddress();
        Utils.hideKeyboardFrom(this, getCurrentFocus());
    }

    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        googleMapHelper.initMyPosition();
        googleMapHelper.animateCamera(location);
    }

    // LocationHelper.LocationHelperListener interface
    @Override
    public void onGotAddress(List<Address> addresses) {
        setLoadingComplete();
        addressAdapter.setAddresses(addresses);
        queryResultsPanel(true);
        selectedLocationPanel(false);
    }

    @Override
    public void onError() {
        setLoadingComplete();
    }

    // AddressAdapterListener interface
    @Override
    public void onAddressClick(Address address) {
        fillSelectedResult(address);
        googleMapHelper.clearMarkers();
        googleMapHelper.paintMarker(new Place(address));
        queryResultsPanel(false);
        selectedLocationPanel(true);
    }
}

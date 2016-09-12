package es.guiguegon.mapmodule;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.karumi.dexter.Dexter;
import es.guiguegon.mapmodule.helpers.GoogleMapHelper;
import es.guiguegon.mapmodule.helpers.LocationHelper;
import es.guiguegon.mapmodule.utils.LocationUtils;
import java.util.List;

/**
 * Created by guiguegon on 09/09/16.
 */
public class MapActivity extends AppCompatActivity implements GoogleMap.OnMapLoadedCallback, LocationListener {

    public static final String RESULT_PLACE_LIST = "result_place_list";

    LocationHelper locationHelper;
    GoogleMapHelper googleMapHelper;
    ImageView myLocationBtn;

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
    protected void onStart() {
        super.onStart();
        googleMapHelper.onStart(this);
        locationHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleMapHelper.onStop();
        locationHelper.onStop();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
        }
        myLocationBtn = (ImageView) findViewById(R.id.my_location_btn);
        googleMapHelper.loadMap(R.id.map_view);
        googleMapHelper.setOnMapLoadedListener(this);
        myLocationBtn.setOnClickListener(this::onMyLocationBtn);
    }

    @Override
    public void onMapLoaded() {
        googleMapHelper.initMyPosition();
    }

    private void onMyLocationBtn(View view) {
        locationHelper.requestLocationUpdate((ViewGroup) view.getRootView(),
                LocationUtils.createRequestLocationUpdateOnce(), this);
    }

    @Override
    public void onLocationChanged(Location location) {
        googleMapHelper.initMyPosition();
        googleMapHelper.animateCamera(location);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    protected void replaceFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }
}

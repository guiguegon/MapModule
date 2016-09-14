package es.guiguegon.mapmodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import es.guiguegon.mapmodule.model.Place;

public class LocationUtils {

    public final static int REQUEST_CODE_GPS = 101001;

    public static double USER_LOCATION_RADIUS = 600 * 1000;
    public static int LOCATION_TIMEOUT = 60 * 1000;

    public static boolean isGPSLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        if (lm.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            try {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gpsEnabled;
    }

    public static LocationRequest createRequestLocationUpdateOnce() {
        return new LocationRequest().setNumUpdates(1).setExpirationDuration(LOCATION_TIMEOUT);
    }

    public static void launchGPSIntent(Activity activity) {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(locationIntent, REQUEST_CODE_GPS);
    }

    public static LatLngBounds getUserLatLngBounds(Location userLocation) {
        LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(SphericalUtil.computeOffset(latLng, USER_LOCATION_RADIUS, 0))
                .include(SphericalUtil.computeOffset(latLng, USER_LOCATION_RADIUS, 90))
                .include(SphericalUtil.computeOffset(latLng, USER_LOCATION_RADIUS, 180))
                .include(SphericalUtil.computeOffset(latLng, USER_LOCATION_RADIUS, 270))
                .build();
        return latLngBounds;
    }

    public static String getFullAddress(Place place) {
        if (place.hasAddress()) {
            return getFullAddress(place.getAddress());
        } else {
            return place.getPosition().toString();
        }
    }

    public static String getFullAddress(Address address) {
        StringBuilder fullAddressBuilder = new StringBuilder();
        int addressLines = address.getMaxAddressLineIndex();
        for (int i = 0; i <= addressLines; i++) {
            fullAddressBuilder.append(address.getAddressLine(i));
            fullAddressBuilder.append(", ");
        }

        return fullAddressBuilder.substring(0, fullAddressBuilder.length() - 2);
    }
}

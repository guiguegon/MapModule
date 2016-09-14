package es.guiguegon.mapmodule.model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by guillermoguerrero on 15/1/16.
 */
public class Place implements ClusterItem, Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    private Address address;
    private LatLng latLng;

    public Place(Address address) {
        this.address = address;
    }

    public Place(LatLng latLng) {
        this.latLng = latLng;
    }

    protected Place(Parcel in) {
        address = (Address) in.readValue(Address.class.getClassLoader());
        latLng = (LatLng) in.readValue(LatLng.class.getClassLoader());
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean hasAddress() {
        return address != null;
    }

    @Override
    public LatLng getPosition() {
        if (hasAddress() && latLng == null) {
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
        }
        return latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(address);
        dest.writeValue(latLng);
    }

    @Override
    public String toString() {
        return "Place{" +
                "address=" + address +
                ", latLng=" + latLng +
                '}';
    }
}
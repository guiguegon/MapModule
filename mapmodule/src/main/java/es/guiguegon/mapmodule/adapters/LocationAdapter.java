package es.guiguegon.mapmodule.adapters;

import android.app.Activity;
import android.location.Address;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import es.guiguegon.mapmodule.R;
import es.guiguegon.mapmodule.utils.LocationUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guiguegon on 16/12/15.
 */
public class LocationAdapter extends RecyclerView.Adapter {

    List<Address> locations;
    LocationAdapterListener locationAdapterListener;
    WeakReference<Activity> weakReference;

    public LocationAdapter(Activity activity, LocationAdapterListener locationAdapterListener) {
        weakReference = new WeakReference<>(activity);
        locations = new ArrayList<>();
        this.locationAdapterListener = locationAdapterListener;
    }

    public void addLocations(List<Address> locations) {
        this.locations.addAll(locations);
        notifyDataSetChanged();
    }

    public void setLocations(List<Address> locations) {
        clear();
        this.locations.addAll(locations);
        notifyDataSetChanged();
    }

    public void clear() {
        locations.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_location, viewGroup, false);
        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        try {
            ((LocationViewHolder) viewHolder).fill(locations.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView locationRawAddress;
        Address address;

        public LocationViewHolder(View v) {
            super(v);
            locationRawAddress = (TextView) v.findViewById(R.id.item_location_raw_address);
        }

        public void fill(Address address) {
            this.address = address;
            if (address != null) {
                locationRawAddress.setText(LocationUtils.getFullAddress(address));
            }
        }

        public Address getAddress() {
            return address;
        }
    }

    public interface LocationAdapterListener {

    }
}

package es.guiguegon.mapmodule.adapters;

import android.location.Address;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import es.guiguegon.mapmodule.R;
import es.guiguegon.mapmodule.utils.LocationUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guiguegon on 16/12/15.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    List<Address> addresses;
    AddressAdapterListener addressAdapterListener;

    public AddressAdapter() {
        addresses = new ArrayList<>();
    }

    public void setAddressAdapterListener(AddressAdapterListener addressAdapterListener) {
        this.addressAdapterListener = addressAdapterListener;
    }

    public void addAddresses(List<Address> addresses) {
        this.addresses.addAll(addresses);
        notifyDataSetChanged();
    }

    public void setAddresses(List<Address> addresses) {
        clear();
        this.addresses.addAll(addresses);
        notifyDataSetChanged();
    }

    public void clear() {
        addresses.clear();
        notifyDataSetChanged();
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new AddressViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_address, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(AddressViewHolder viewHolder, int position) {
        try {
            fillAddressViewHoler(viewHolder, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillAddressViewHoler(AddressViewHolder viewHolder, int position) {
        Address address = getAddress(position);
        viewHolder.addressRawAddress.setText(LocationUtils.getFullAddress(address));
        viewHolder.addressRawAddress.setOnClickListener((view) -> {
            if (addressAdapterListener != null) {
                addressAdapterListener.onAddressClick(address);
            }
        });
    }

    private Address getAddress(int position) {
        return addresses.get(position);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public interface AddressAdapterListener {
        void onAddressClick(Address address);
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {

        TextView addressRawAddress;

        public AddressViewHolder(View v) {
            super(v);
            addressRawAddress = (TextView) v.findViewById(R.id.item_address_raw_address);
        }
    }
}

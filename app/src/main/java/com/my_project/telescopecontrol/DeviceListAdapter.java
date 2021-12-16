package com.my_project.telescopecontrol;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.my_project.telescopecontrol.MainActivity.handler;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.myViewHolder> {

    private Context context;
    private List<DeviceInfoModel> deviceList;
    private OnDeviceListener myOnDeviceListener;


    public DeviceListAdapter(List<DeviceInfoModel> deviceList, OnDeviceListener OnDevicelistener) {
        this.deviceList = deviceList;
        this.myOnDeviceListener = OnDevicelistener;

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.device_info_layout, parent, false);

        myViewHolder vh = new myViewHolder(v, myOnDeviceListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        holder.textName.setText(deviceInfoModel.getDeviceName());
        holder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());


    }

    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textName, textAddress;
        LinearLayout linearLayout;
        OnDeviceListener onDeviceListener;

        public myViewHolder(@NonNull View v, OnDeviceListener onDeviceListener) {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo);
            this.onDeviceListener = onDeviceListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            onDeviceListener.onDeviceClick(getAdapterPosition());
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);


        }

    }

    public interface OnDeviceListener {
        void onDeviceClick(int position);
    }


    @Override
    public int getItemCount() {
        int dataCount = deviceList.size();
        return dataCount;
    }
}

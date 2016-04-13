package com.example.nilif.bleinteraction;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by nilif on 2016/4/13.
 */
public class ScanDeviceActivity extends ListActivity{

    private LeDeviceListAdapter mLeDeciveListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler handler;


    private class LeDeviceListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}

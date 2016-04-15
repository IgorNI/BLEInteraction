package com.example.nilif.bleinteraction;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nilif on 2016/4/13.
 */
public class ScanDeviceActivity extends ListActivity{

    private static final long SCAN_PERIOD = 1000;
    private LeDeviceListAdapter mLeDeciveListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.scan_device_layout);
        getActionBar().setTitle("扫描设备");
        mHandler = new Handler();

        //如果设备不支持BLE，则显示不支持BLE的信息
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,R.string.ble_not_supported,Toast.LENGTH_SHORT).show();
            finish();
        }

        //如果支持BLE，则再判断是否支持蓝牙
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null) {
            Toast.makeText(this,R.string.bluetooth_not_support,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        if(!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    //对选项栏的操作
    public boolean onOptionsItemSelected (MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_scan :
                mLeDeciveListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop :
                scanLeDevice(false);
                break;
        }
        return true;
    }

    protected void onResume(){
        super.onResume();

        // 保证蓝牙设备始终保持着连接，若没有连接，则尝试重新连接
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
            }
        }

        mLeDeciveListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeciveListAdapter);
        scanLeDevice(true);
    }

    protected void onActivityResult (int requsetCode , int resultCode, Intent data) {

        if (requsetCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requsetCode,resultCode,data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeciveListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeciveListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this,DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    //扫描设备
    private void scanLeDevice(final boolean b) {
        // 如果扫描设备
        if(b){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            },SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            // 如果不允许扫描，则。
        }else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // 扫描时保持设备的容器
    private class LeDeviceListAdapter extends BaseAdapter{

        private ArrayList<BluetoothDevice> mLeDevice;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevice = new ArrayList<BluetoothDevice>();
            mInflater = ScanDeviceActivity.this.getLayoutInflater();
        }

        // 获取设备
        public BluetoothDevice getDevice (int position) {
            return mLeDevice.get(position);
        }


        public void clear() {
            mLeDevice.clear();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevice.contains(device)) {
                mLeDevice.add(device);
            }


        }

        @Override
        public int getCount() {
            return mLeDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return mLeDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.scan_device_layout,null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.Device_Adress);
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.Device_Name);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = mLeDevice.get(position);
            final String deviceName = device.getName();
            final String deviceAddress = device.getAddress();
            if(deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName .setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(deviceAddress);

            return convertView;
        }





    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback (){

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeciveListAdapter.addDevice(device);
                            mLeDeciveListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}

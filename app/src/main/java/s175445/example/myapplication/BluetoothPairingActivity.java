package s175445.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class BluetoothPairingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private ListView lvNewDevices;
    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private Button toggle;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action == null)
                return;

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                final String tag = "Bluetooth on/off change";

                if(state == BluetoothAdapter.STATE_TURNING_ON)
                    Log.i(tag, "turning on");
                if(state == BluetoothAdapter.STATE_ON)
                    Log.i(tag, "turned on");
                if(state == BluetoothAdapter.STATE_TURNING_OFF)
                    Log.i(tag, "turning off");
                if(state == BluetoothAdapter.STATE_OFF)
                    Log.i(tag, "turned off");
            }

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device == null)
                    return;

                //Consider checking if device name is missing - we might not want those
                mBTDevices.add(device);
                Log.i("New device found", device.getName() + " " + device.getAddress());

                DeviceListAdapter mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_pairing);

        toggle = findViewById(R.id.bluetoothToggle);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(this);


        toggle.setOnClickListener(this);

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

    }

    @Override
    public void onClick(View v) {
        if(v == toggle){
            toggleBluetooth();
        }
    }

    private void toggleBluetooth() {
        if(mBluetoothAdapter == null){
            System.out.println("Bluetooth not supported");
            return;
        }

        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        } else {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        System.out.println("Looking for bluetooth devices");

        if(mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        int permission = PackageManager.PERMISSION_GRANTED;
        permission += checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        permission += checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permission != PackageManager.PERMISSION_GRANTED){
            String[] permissions = new String[2];
            permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
            requestPermissions(permissions, 0);
        }

        mBluetoothAdapter.startDiscovery();


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO: Create progress dialogue and properly wait
        mBTDevices.get(position).createBond();

        //Dummy wait
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Stackoverflow code snippet
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.bluetooth.BluetoothSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
    }

    public void playSound(View view) {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.test);
        mp.start();
    }

}

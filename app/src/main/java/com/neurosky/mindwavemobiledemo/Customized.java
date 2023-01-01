package com.neurosky.mindwavemobiledemo;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.BodyDataType;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.lang.reflect.Method;
import java.util.Set;

public class Customized extends AppCompatActivity {

    private TgStreamReader tgStreamReader;

    // TODO connection sdk
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_customized);

        initView();

        try {
            // TODO
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                finish();
//				return;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }
    private TextView tv_ps = null;
    private TextView tv_attention = null;
    private TextView tv_meditation = null;
    private TextView  tv_hr = null;

    private Button btn_start = null;
    private Button btn_stop = null;
    private Button btn_selectdevice = null;
    private int badPacketCount = 0;

    private ProgressBar medi_prog = null;
    private ProgressBar atten_prog = null;


    private void initView() {
       /* tv_ps = (TextView) findViewById(R.id.tv_ps);
        tv_attention = (TextView) findViewById(R.id.tv_attention);
        tv_meditation = (TextView) findViewById(R.id.tv_meditation);
        tv_delta = (TextView) findViewById(R.id.tv_delta);
        tv_theta = (TextView) findViewById(R.id.tv_theta);
        tv_lowalpha = (TextView) findViewById(R.id.tv_lowalpha);

        tv_highalpha = (TextView) findViewById(R.id.tv_highalpha);
        tv_lowbeta= (TextView) findViewById(R.id.tv_lowbeta);
        tv_highbeta= (TextView) findViewById(R.id.tv_highbeta);

        tv_lowgamma = (TextView) findViewById(R.id.tv_lowgamma);
        tv_middlegamma= (TextView) findViewById(R.id.tv_middlegamma);
        tv_badpacket = (TextView) findViewById(R.id.tv_badpacket);

        tv_hr = findViewById(R.id.heart_rate);


        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        wave_layout = (LinearLayout) findViewById(R.id.wave_layout);*/

        tv_ps = (TextView) findViewById(R.id.signal_txt);
        tv_attention = (TextView) findViewById(R.id.atten);
        tv_meditation = (TextView) findViewById(R.id.medit);
        tv_hr = findViewById(R.id.heart_rate_txt);
        btn_start = (Button) findViewById(R.id.start_bt);
        btn_stop = (Button) findViewById(R.id.stop_bt);
        medi_prog = findViewById(R.id.progressBar_medit);
        atten_prog = findViewById(R.id.progressBar_atten);


        btn_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                badPacketCount = 0;
                showToast("connecting ...",Toast.LENGTH_SHORT);
                start();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(tgStreamReader != null){
                    tgStreamReader.stop();
                }
            }

        });

        btn_selectdevice =  (Button) findViewById(R.id.device_bt);

        btn_selectdevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                scanDevice();
            }

        });
    }


    private void start(){
        if(address != null){
            BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(address);
            createStreamReader(bd);

            tgStreamReader.connectAndStart();
        }else{
            showToast("Please select device first!", Toast.LENGTH_SHORT);
        }
    }

    public void stop() {
        if(tgStreamReader != null){
            tgStreamReader.stop();
            tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate
            tgStreamReader = null;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(tgStreamReader != null){
            tgStreamReader.close();
            tgStreamReader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        stop();
    }

    // TODO view

    // (2) demo of drawing ECG, set up of view

    // (2) demo of drawing ECG, update view

    private int currentState = 0;
    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            currentState  = connectionStates;
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                    //sensor.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    //byte[] cmd = new byte[1];
                    //cmd[0] = 's';
                    //tgStreamReader.sendCommandtoDevice(cmd);
                    LinkDetectedHandler.sendEmptyMessageDelayed(1234, 5000);
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    //get data time out
                    break;
                case ConnectionStates.STATE_COMPLETE:
                    //read file complete
                    break;
                case ConnectionStates.STATE_STOPPED:
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    break;
                case ConnectionStates.STATE_ERROR:
                    break;
                case ConnectionStates.STATE_FAILED:
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);


        }

        @Override
        public void onRecordFail(int a) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // TODO Auto-generated method stub

            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // TODO Auto-generated method stub
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
            //Log.i(TAG,"onDataReceived");
        }

    };

    private boolean isPressing = false;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    private static final int MSG_CONNECT = 1003;
    private boolean isReadFilter = false;

    int raw;
    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1234:
                    tgStreamReader.MWM15_getFilterType();
                    isReadFilter = true;

                    break;
                case 1235:
                    tgStreamReader.MWM15_setFilterType(MindDataType.FilterType.FILTER_60HZ);
                    LinkDetectedHandler.sendEmptyMessageDelayed(1237, 1000);
                    break;
                case 1236:
                    tgStreamReader.MWM15_setFilterType(MindDataType.FilterType.FILTER_50HZ);
                    LinkDetectedHandler.sendEmptyMessageDelayed(1237, 1000);
                    break;

                case 1237:
                    tgStreamReader.MWM15_getFilterType();

                    break;

                case MindDataType.CODE_FILTER_TYPE:
                    if(isReadFilter){
                        isReadFilter = false;
                        if(msg.arg1 == MindDataType.FilterType.FILTER_50HZ.getValue()){
                            LinkDetectedHandler.sendEmptyMessageDelayed(1235, 1000);
                        }else if(msg.arg1 == MindDataType.FilterType.FILTER_60HZ.getValue()){
                            LinkDetectedHandler.sendEmptyMessageDelayed(1236, 1000);
                        }else{
                        }
                    }

                    break;



                /*case MindDataType.CODE_RAW:
                    updateWaveView(msg.arg1);
                    break;*/
                case MindDataType.CODE_MEDITATION:
                    tv_meditation.setText("meditation = " +msg.arg1 );
                    medi_prog.setProgress(msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    tv_attention.setText("attention = " +msg.arg1 );
                    atten_prog.setProgress(msg.arg1);
                    break;
                /*case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower)msg.obj;
                    if(power.isValidate()){
                        tv_delta.setText("" +power.delta);
                        tv_theta.setText("" +power.theta);
                        tv_lowalpha.setText("" +power.lowAlpha);
                        tv_highalpha.setText("" +power.highAlpha);
                        tv_lowbeta.setText("" +power.lowBeta);
                        tv_highbeta.setText("" +power.highBeta);
                        tv_lowgamma.setText("" +power.lowGamma);
                        tv_middlegamma.setText("" +power.middleGamma);
                    }
                    break;*/
                case MindDataType.CODE_POOR_SIGNAL://
                    int poorSignal = msg.arg1;

                    poorSignal = poorSignal/2;
                    poorSignal = poorSignal*(-1);
                    tv_ps.setText("signal = "+poorSignal);

                    break;
                case BodyDataType.CODE_HEATRATE:
                    tv_hr.setText("heart rate = " +msg.arg1 );
                    break;
                /*case MSG_UPDATE_BAD_PACKET:
                    tv_badpacket.setText("" + msg.arg1);

                    break;*/
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public void showToast(final String msg,final int timeStyle){
        Customized.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    //show device list while scanning
    private ListView list_select;
    private BTDeviceListAdapter deviceListApapter = null;
    private Dialog selectDialog;

    // (3) Demo of getting Bluetooth device dynamically
    public void scanDevice(){

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        setUpDeviceListView();
        //register the receiver for scanning
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    private void setUpDeviceListView(){

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_select_device, null);
        list_select = (ListView) view.findViewById(R.id.list_select);
        selectDialog = new Dialog(this, R.style.dialog1);
        selectDialog.setContentView(view);
        //List device dialog

        deviceListApapter = new BTDeviceListAdapter(this);
        list_select.setAdapter(deviceListApapter);
        list_select.setOnItemClickListener(selectDeviceItemClickListener);

        selectDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
                Customized.this.unregisterReceiver(mReceiver);
            }

        });

        selectDialog.show();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            deviceListApapter.addDevice(device);
        }
        deviceListApapter.notifyDataSetChanged();
    }

    //Select device operation
    private AdapterView.OnItemClickListener selectDeviceItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
            // TODO Auto-generated method stub
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            //unregister receiver
            Customized.this.unregisterReceiver(mReceiver);

            mBluetoothDevice =deviceListApapter.getDevice(arg2);
            selectDialog.dismiss();
            selectDialog = null;

            address = mBluetoothDevice.getAddress().toString();

            //ger remote device
            BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress().toString());

            //bind and connect
            //bindToDevice(remoteDevice); // create bond works unstable on Samsung S5
            //showToast("pairing ...",Toast.LENGTH_SHORT);

            tgStreamReader = createStreamReader(remoteDevice);
            tgStreamReader.connectAndStart();

        }

    };

    /**
     * If the TgStreamReader is created, just change the bluetooth
     * else create TgStreamReader, set data receiver, TgStreamHandler and parser
     * @param bd
     * @return TgStreamReader
     */
    public TgStreamReader createStreamReader(BluetoothDevice bd){

        if(tgStreamReader == null){
            // Example of constructor public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
            tgStreamReader = new TgStreamReader(bd,callback);
            tgStreamReader.startLog();
        }else{
            // (1) Demo of changeBluetoothDevice
            tgStreamReader.changeBluetoothDevice(bd);

            // (4) Demo of setTgStreamHandler, you can change the data handler by this function
            tgStreamReader.setTgStreamHandler(callback);
        }
        return tgStreamReader;
    }

    /**
     * Check whether the given device is bonded, if not, bond it
     * @param bd
     */
    public void bindToDevice(BluetoothDevice bd){
        int ispaired = 0;
        if(bd.getBondState() != BluetoothDevice.BOND_BONDED){
            //ispaired = remoteDevice.createBond();
            try {
                //Set pin
                if(Utils.autoBond(bd.getClass(), bd, "0000")){
                    ispaired += 1;
                }
                //bind to device
                if(Utils.createBond(bd.getClass(), bd)){
                    ispaired += 2;
                }
                Method createCancelMethod=BluetoothDevice.class.getMethod("cancelBondProcess");
                boolean bool=(Boolean)createCancelMethod.invoke(bd);

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        }

    }

    //The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // update to UI
                deviceListApapter.addDevice(device);
                deviceListApapter.notifyDataSetChanged();

            }
        }
    };


}

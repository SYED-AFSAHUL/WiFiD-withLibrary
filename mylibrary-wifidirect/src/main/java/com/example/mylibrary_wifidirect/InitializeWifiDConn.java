package com.example.mylibrary_wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by afsahulsyed on 27/5/17.
 */

public class InitializeWifiDConn extends AppCompatActivity{

    /**
     * class members
     */
    private boolean mConnSuccess = false;
    private final String TAG = "sMess";

    private Context mContext;
    private WifiP2pInfo info;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private List peers = new ArrayList();;
    private WifiP2pDevice mDevice;
    private ServerSide mServerSide;
    //private List<HashMap<String, String>> peersshow = new ArrayList();
    private boolean WiFiDirectIsEnable = false;

    public InitializeWifiDConn(Context context){
        mContext = context;
    }


    /**
     *
     * ------------------PUBLIC METHODS-----------------
     *
     *
     */

    /** 1
     * Initializes the device/app WiFi Direct Connection to be established
     *
     * @return boolean returns false if error occurred during initializing
     */
    public boolean initConn(){
        try {
            initIntentFilter();
            initReceiver();

            RegisterReceiver();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** 2
     * Check if WiFi direct is enabled on the device
     *
     * @return true if enabled , false if not enabled
     */
    public boolean CheckWiFiDState(){
        if(WiFiDirectIsEnable){
            Log.d(TAG,"WiFiDirectEnabled--true");
            return true;
        }else{
            Log.d(TAG,"WiFiDirectEnabled--false");
            return false;
        }
    }

    /** 3
     * notify wifi direct status
     *
     *access modifier - default
     * @param WiFiDState- status
     */
    void SetWiFiDEnable(Boolean WiFiDState){
        this.WiFiDirectIsEnable = WiFiDState;
    }

    /**
     *
     *
     */
    public void DiscoverPeer(){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"peer Discover Successful");
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG,"peer Discover Successful");
            }
        });
    }

    public boolean SelectDevice(){
        mDevice = (WifiP2pDevice) peers.get(0);
        ConnectToDevice();
        return mConnSuccess;
    }

    /**
     * creates connection with device passed in parameter
     */
    public void ConnectToDevice(){

        final WifiP2pConfig config = new WifiP2pConfig();
        //mDevice = (WifiP2pDevice) peers.get(0);
        Log.d(TAG,"inside connect/n connecting to "+ mDevice.deviceName);
        config.deviceAddress = mDevice.deviceAddress;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"Success");
                ConnectSuccess(true);
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG,"Failure" + reason);
                ConnectSuccess(false);
            }
        });//connect method ends

        Log.d(TAG,"exiting CreateConnection");
    }


    public void SendData(String data){
        Log.d(TAG,"sendData");

        Intent serviceIntent = new Intent(mContext, ClientSide.class);
        serviceIntent.setAction(ClientSide.ACTION_SEND_FILE);
        serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());

        Log.d(TAG, "ownership is " + info.groupOwnerAddress.getHostAddress());

        serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_PORT, 8888);
        serviceIntent.putExtra(ClientSide.EXTRAS_DATA, data);
        serviceIntent.putExtra(ClientSide.EXTRAS_DATA_TYPE,"data");
        mContext.startService(serviceIntent);
    }

    public void SendPicture(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("images/*");
        startActivityForResult(intent, 20);
    }

    public void SendAudio(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "onActivityResult");
        Intent serviceIntent = new Intent(mContext, ClientSide.class);

        Uri uri = data.getData();
        Log.d(TAG,"uri - "+ uri);
        serviceIntent.putExtra(ClientSide.EXTRAS_FILE_PATH, uri.toString());

        serviceIntent.setAction(ClientSide.ACTION_SEND_FILE);
        serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());


        serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_PORT, 8988);
        serviceIntent.putExtra(ClientSide.EXTRAS_DATA_TYPE,"audio");

        mContext.startService(serviceIntent);
    }

    public void ReceiveData(){
        if(info.groupFormed && info.isGroupOwner) {

            mServerSide = new ServerSide(mContext);
            mServerSide.SetDetails("audio",".mp3");
            mServerSide.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void UnRegisterReceiver(){
        try {
            mContext.unregisterReceiver(mReceiver);
            Log.d(TAG, "unregisterReceiver(mReceiver)");
        }catch (Exception e){e.printStackTrace();}
    }

    public void RegisterReceiver(){
        mContext.registerReceiver(mReceiver, mIntentFilter);
        Log.d(TAG,"RegisterReceiver");
    }
    /**
     *
     * -------------------PRIVATE METHODS---------------------
     *
     *
     */

    /**
     * Initializes intent filter
     */
    private void initIntentFilter() {
        Log.d(TAG,"inside initIntentFilter");
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    private void initReceiver() {
        Log.d(TAG,"initReceiver");
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        Log.d(TAG,"mManager - " + mManager);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                try {
                    Log.d(TAG, "OnPeersAvailable");
                    if (!peers.isEmpty())
                        peers.clear();
                    //peersshow.clear();
                    Collection<WifiP2pDevice> aList = peersList.getDeviceList();
                    peers.addAll(aList);

                    Log.d(TAG, "Device List :- ");
                    for (int i = 0; i < aList.size(); i++) {
                        WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    /*HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);
                    peersshow.add(map);*/
                        Log.d(TAG, "device name -; " + a.deviceName + "  address -: " + a.deviceAddress);
                    }
                    Log.d(TAG,"exiting onPeerListener");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo mInfo) {

                Log.d(TAG, "InfoAvailable is on");

                info = mInfo;
                //TextView view = (TextView) findViewById(R.id.textView);
                if (info.groupFormed && info.isGroupOwner) {
                    Log.d(TAG, "Group formed and you are the owner");

                   // mServerSide = new ServerSide(InitializeWifiDConn.this);
                    //mServerSide.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    // mDataTask = new ServerService(getApplicationContext(), view);
                    // mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (info.groupFormed) {
                    Log.d(TAG,"group Formed & you are not the owner");
                }
            }
        };

        mReceiver = new WiFiDirectBroadCastReceiver(mManager, mChannel,mPeerListListener,
                                                            mInfoListener,this,mContext);
    }

    private void ConnectSuccess(boolean state){
        mConnSuccess = state;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*Log.d(TAG, "onActivityResult");
        Intent serviceIntent = new Intent(mContext, ClientSide.class);

        Uri uri = data.getData();
        Log.d(TAG,"uri - "+ uri);
        serviceIntent.putExtra(ClientSide.EXTRAS_FILE_PATH, uri.toString());

        serviceIntent.setAction(ClientSide.ACTION_SEND_FILE);
        serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());

        if (requestCode == 20) {
            serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_PORT, 8888);
            serviceIntent.putExtra(ClientSide.EXTRAS_DATA_TYPE,"image");
        }else if(requestCode == 30){
            serviceIntent.putExtra(ClientSide.EXTRAS_GROUP_OWNER_PORT, 8988);
            serviceIntent.putExtra(ClientSide.EXTRAS_DATA_TYPE,"audio");
        }
        mContext.startService(serviceIntent);*/
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        Log.d(TAG,"onResume");
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        Log.d(TAG,"onPause");
    }
}

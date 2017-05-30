package com.example.mylibrary_wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by afsahulsyed on 27/5/17.
 */

public class WiFiDirectBroadCastReceiver extends BroadcastReceiver {

    private static final String TAG = "sMess";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mInfoListener;
    private InitializeWifiDConn mActivity;
    private Context mContext;

    public WiFiDirectBroadCastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       WifiP2pManager.PeerListListener peerListListener,
                                       WifiP2pManager.ConnectionInfoListener infoListener,
                                        InitializeWifiDConn activity,
                                        Context context) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mPeerListListener = peerListListener;
        this.mInfoListener = infoListener;
        this.mActivity = activity;
        mContext = context;
        Log.d(TAG, "WiFiDirectBroadCastReceiver");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"onReceive");
        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                Log.d(TAG,"WIFI_P2P_STATE_CHANGED_ACTION");

                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {   // Wifi P2P is enabled
                    mActivity.SetWiFiDEnable(true);
                    Log.d(TAG,"Wifi P2P is enabled");
                } else {                                                // Wi-Fi P2P is not enabled
                    mActivity.SetWiFiDEnable(false);
                    Log.d(TAG,"Wifi P2P is not enabled");
                }

                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                Log.d(TAG,"WIFI_P2P_PEERS_CHANGED_ACTION");
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                mManager.requestPeers(mChannel, mPeerListListener);
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                Log.d(TAG,"WIFI_P2P_CONNECTION_CHANGED_ACTION");
                // Respond to new connection or disconnections
                if (mManager == null) {
                    Log.d(TAG,"mManager = null");
                    return;
                }

                NetworkInfo networkInfo = intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    Log.d(TAG, "Connected");
                    Toast.makeText(mContext,"Connected",Toast.LENGTH_LONG).show();
                    mManager.requestConnectionInfo(mChannel, mInfoListener);
                } else {
                    Log.d(TAG, "DisConnected");
                    return;
                }
                break;
            case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION:
                Log.d(TAG,"WIFI_P2P_DISCOVERY_CHANGED_ACTION");
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Log.d(TAG,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                // Respond to this device's wifi state changing
                break;
            default :
                Log.d(TAG,"in default in switch, action -: "+ action);
        }
    }
}

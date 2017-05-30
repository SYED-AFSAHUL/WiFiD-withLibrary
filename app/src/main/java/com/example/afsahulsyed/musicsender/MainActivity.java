package com.example.afsahulsyed.musicsender;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mylibrary_wifidirect.*;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "sMess";
    InitializeWifiDConn mInitializeWifiDConn;
    Button discoverBT;
    Button connectBT;
    Button sendDataBT;
    Button sendMusBT;
    Button receiveAudBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
        mInitializeWifiDConn = new InitializeWifiDConn(this.getApplicationContext());
        InitButtons();

    }

    private void InitView(){
        Log.d(TAG,"inside initView");
        discoverBT = (Button) findViewById(R.id.buttonD);
        connectBT = (Button)findViewById(R.id.buttonC);
        sendDataBT = (Button)findViewById(R.id.buttonData);
        sendMusBT = (Button)findViewById(R.id.buttonM);
        receiveAudBT = (Button)findViewById(R.id.buttonServer);
    }

    private void InitButtons(){
        discoverBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(mInitializeWifiDConn.initConn()){
                    if(mInitializeWifiDConn.CheckWiFiDState()){
                        Log.d(TAG,"WiFi Direct enabled");
                        mInitializeWifiDConn.DiscoverPeer();
                    }else {
                        Log.d(TAG,"Please switch on your WiFi and try again");
                        Toast.makeText(getApplicationContext(),
                                "Please switch on your WiFi and try again", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(mInitializeWifiDConn,"Some error occurred. Try again",
                            Toast.LENGTH_LONG).show();
                }
            }//OnClick ends
        });

        connectBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mInitializeWifiDConn.SelectDevice();
            }
        });

        sendDataBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mInitializeWifiDConn.SendData("asdfgh");
            }
        });

        sendMusBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Intent mIntentMus = new Intent(Intent.ACTION_GET_CONTENT);
                    mIntentMus.setType("audio/*");
                    startActivityForResult(
                            mIntentMus, 30);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG,e.getMessage());
                }
              //  mInitializeWifiDConn.SendAudio();
            }
        });

        receiveAudBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mInitializeWifiDConn.ReceiveData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mInitializeWifiDConn.SendAudio(requestCode, resultCode, data);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        mInitializeWifiDConn.UnRegisterReceiver();
        Log.d(TAG,"onPause");
    }

}

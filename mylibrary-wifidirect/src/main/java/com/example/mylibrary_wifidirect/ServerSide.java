package com.example.mylibrary_wifidirect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by afsahulsyed on 29/5/17.
 */

public class ServerSide extends AsyncTask<Void, Void, String> {

    private static final String TAG = "sMess";
    private Context mContext;
    private String mDataType = "string";
    private String mFileExtension = ".mp3";

    public ServerSide(){
        Log.d(TAG,"ServerSide()");
    }

    public ServerSide(Context context) {
        Log.d(TAG,"ServerSide(Context context)");
        this.mContext = context;
    }

    public void SetDetails(String DataType, String ext){
        mDataType = DataType;
        mFileExtension = ext;
    }
    @Override
    protected String doInBackground(Void... params) {
        if (mDataType.equals("string")) {
            try {
                Log.d(TAG, "doInBackground");
                Log.d(TAG, "Waiting for client......");
                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();
                Log.d(TAG, "connection accepted");

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                InputStream inputstream = client.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Log.d(TAG, "baos -: " + baos);
                int i;
                while ((i = inputstream.read()) != -1) {
                    baos.write(i);
                }

                String str = baos.toString();
                serverSocket.close();
                Log.d(TAG, "result string " + str);
                return str;
            } catch (IOException e) {
                // Log.e(WiFiDirectActivity.TAG, e.getMessage());
                e.printStackTrace();
                return null;
            }
        }else if(mDataType.equals("audio")){
            Log.d(TAG,"doInBackground- serverfiles ---audio");
            try {

                Log.d(TAG,"waiting to accept connection.....");
                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8988);
                Socket client = serverSocket.accept();
                Log.d(TAG,"Connection Accepted");
                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File f = new File(path,"wifip2pshared-"+System.currentTimeMillis()+ mFileExtension);

                File dirs = new File(f.getParent());
                try {
                    if (!dirs.exists()) {
                        dirs.mkdirs();
                        Log.d(TAG, "!dirs.exists()");
                    }
                    f.createNewFile();
                }catch (Exception e){
                    Log.d(TAG,e.getMessage());
                }
                if (f.exists()) {
                    Log.d(TAG, "dir exist");
                }else{
                    Log.d(TAG, "dir still doesn't exist");
                }
                InputStream inputstream = client.getInputStream();
                if(copyFile(inputstream, new FileOutputStream(f))){
                    Log.d(TAG,"successfully copied");
                } else {
                    Log.d(TAG,"error in coping");
                }
                serverSocket.close();
                Log.d(TAG,"exiting doInBackGround");
                return f.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG,"onPostExecute");
        try {
            if (result != null) {
               // statusText.setText("File copied - " + result);
                Log.d(TAG,"location --- "+ result);
                if(!mDataType.equals("string")) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + result), "audio/*");
                    mContext.startActivity(intent);
                }
            }
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        Log.d(TAG,"copyFile");
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,e.getMessage());
            return false;
        }
        return true;
    }
}


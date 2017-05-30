package com.example.mylibrary_wifidirect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by afsahulsyed on 27/5/17.
 */

public class ClientSide extends IntentService {

    private static final String TAG = "sMess";
    private static final int SOCKET_TIMEOUT = 500000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "sf_file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "sf_go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "sf_go_port";
    public static final String EXTRAS_DATA = "afsa_data";
    public static final String EXTRAS_DATA_TYPE = "afsa_data_type";
    byte buf[]  = new byte[1024];
    int len;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ClientSide(String name) {
        super(name);
    }

    public ClientSide() {
        super("ClientSide");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
        int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

        Socket socket = new Socket();

        if (intent.getExtras().getString(EXTRAS_DATA_TYPE).equals("data")) {
            Log.d(TAG,"DATA_TYPE)==data");

            try {
                Log.d(TAG,"waiting for connection for port - " + port);
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(TAG,"connection accepted");
                /**
                 * Create a byte stream from a JPEG file and pipe it to the output stream
                 * of the socket. This data will be retrieved by the server device.
                 */
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(intent.getExtras().getString(EXTRAS_DATA).getBytes());

                outputStream.close();
                Log.d(TAG, "Transmission on client side is done");
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             * */
            finally{
                Log.d(TAG, "in finally clientService");
                if (socket.isConnected()) {
                    try {
                        socket.close();
                        Log.d(TAG, "Socket closed");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (intent.getExtras().getString(EXTRAS_DATA_TYPE).equals("audio")){
            Log.d(TAG,"EXTRAS_DATA_TYPE)== audio");
            Context context = getApplicationContext();

            if (intent.getAction().equals(ACTION_SEND_FILE)) {
                Log.d(TAG, "ACTION_SEND_FILE");

                String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);

                try {
                    if (socket.isConnected()) {
                        socket.close();
                        Log.d(TAG, "Socket was connected, now closed");
                    }
                    /**
                     * Create a client socket with the host,
                     * port, and timeout information.
                     */
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    Log.d(TAG, "Sending pic started.....");
                    /**
                     * Create a byte stream from a JPEG file and pipe it to the output stream
                     * of the socket. This data will be retrieved by the server device.
                     */
                    OutputStream outputStream = socket.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    InputStream inputStream = null;
                    inputStream = cr.openInputStream(Uri.parse(fileUri));//("path/to/picture.jpg"));
                    while ((len = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                    Log.d(TAG, "Sending pic Finished.");
                    outputStream.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(TAG, (" FileNotFoundException - " + e.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "IOException " + e.getMessage());
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG,e.getMessage());
                }

                /**
                 * Clean up any open sockets when done
                 * transferring or if an exception occurred.
                 */ finally {
                    Log.d(TAG, "in finally");
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG,"exiting onHandle");
    }
}

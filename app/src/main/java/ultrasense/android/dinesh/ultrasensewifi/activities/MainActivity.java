package ultrasense.android.dinesh.ultrasensewifi.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import ultrasense.android.dinesh.ultrasensewifi.R;
import ultrasense.android.dinesh.ultrasensewifi.api.Manager;
import ultrasense.android.dinesh.ultrasensewifi.api.StopWatch;
import ultrasense.android.dinesh.ultrasensewifi.configuration.Config;
import ultrasense.android.dinesh.ultrasensewifi.configuration.Constants;
import ultrasense.android.dinesh.ultrasensewifi.wifidirect.WiFiDirectBroadcastReceiver;

public class MainActivity extends AppCompatActivity /*implements WifiP2pManager.PeerListListener*/ {

    private static String TAG = MainActivity.class.getSimpleName();
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private UltraSenseModule ultraSenseModule;
    StopWatch timer = new StopWatch();
    private WiFiDirectBroadcastReceiver receiver;
    private Button discover;
    private Button connect;
    private Button serverConnect;
    private Button serverDisconnect;
    private Button clientStart;
    private Button startMarker;
    private Button endMarker;
    private Button clientEnd;
    private TextView discoveryStatus;
    private TextView peerDeviceName;
    private TextView permissionLayout;
    private boolean discovered = false;
    File file;
    BufferedWriter bw;
    String waveFilename = "";
    String textFilename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setIntentFilters();
        this.ultraSenseModule = new UltraSenseModule(MainActivity.this);
        init();
        initListeners();
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

    }

    private void init() {
        discover = (Button) findViewById(R.id.wifi_discover);
        connect = (Button) findViewById(R.id.wifi_connect);
        discoveryStatus = (TextView) findViewById(R.id.discovery_status);
        peerDeviceName = (TextView) findViewById(R.id.device_address);
        clientStart = (Button) findViewById(R.id.client_start);
        startMarker = (Button) findViewById(R.id.start_marker);
        endMarker = (Button) findViewById(R.id.end_marker);
        clientEnd = (Button) findViewById(R.id.client_end);
        serverConnect = (Button) findViewById(R.id.server);
        serverDisconnect = (Button) findViewById(R.id.server_end);
        permissionLayout = (TextView) findViewById(R.id.permission_layout);
    }

    private void initListeners() {
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover();
                getPermissionForMicrophone();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
                getPermissionForFile();
            }
        });
        serverConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover(); connect();
                receiver.receiveData();
            }
        });
        serverDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover(); connect();
                receiver.sendData(Constants.STOP);
            }
        });
        clientStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover(); connect();
                receiver.sendData(Constants.START);
            }
        });
        startMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                discover(); connect();
                receiver.sendData(Constants.START_MARKER);
            }
        });
        endMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    //BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    bw.write("Markers i");
                    bw.write("\t");
                    //bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                discover(); connect();
                receiver.sendData(Constants.END_MARKER);
            }
        });
        clientEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover(); connect();
                receiver.sendData(Constants.END);
            }
        });
    }

    private void connect() {
        if(!receiver.getPeerDeviceName().equals("")) {
            receiver.connect();
            if(receiver.isPeerConnect()) {
                peerDeviceName.setText("Peer device "+receiver.getPeer().deviceName);
            } else {
                receiver.connect();
                Log.d(TAG,"Trying to connect... ");
            }
        } else {
            peerDeviceName.setText("Peer device is not fetchable");
            permissionLayout.setText("Check wifi direct settings on your phone");
            Log.d(TAG,"peer device not not fetchable");
        }
    }

    private void discover() {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                setDiscovered(true);
                discoveryStatus.setText("Discovery status : True");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG,"onFailure");
                setDiscovered(false);
                discoveryStatus.setText("Discovery status : False");
            }
        });
    }

    private void setIntentFilters() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Indicates the messages obtained by the device using Wifi direct
        intentFilter.addAction(Constants.BROADCAST_RECEIVE_PROGRESS);

        //Indicates the countdown done before the recording is started
        intentFilter.addAction(Constants.BROADCAST_COUNTDOWN_TIMER);

        //Indicates the error messages for the user to see
        intentFilter.addAction(Constants.BROADCAST_RECEIVE_MESSAGES);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, getApplicationContext(), channel, this);
        registerReceiver(receiver, intentFilter);
        registerReceiver(threadInfoReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(threadInfoReceiver);
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    private final BroadcastReceiver threadInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constants.BROADCAST_RECEIVE_PROGRESS)) {
                Bundle bundle = intent.getExtras();
                int controlMsg = bundle.getInt(Constants.WIFI_DIRECT_MSG);
                /*FileWriter writer = null;
                try { writer = new FileWriter(file); } catch (IOException e) { e.printStackTrace(); }*/
                Log.d(TAG, "control msg is "+controlMsg);
                if(controlMsg==Constants.START) {
                    Log.d(TAG,"start recording "+controlMsg);
                    ultraSenseModule.createCustomScenario();
                    ultraSenseModule.startRecord();
                } else if (controlMsg==Constants.END) {
                    ultraSenseModule.stopRecord();
                    try {
                        Log.d(TAG,"waveFilename "+waveFilename);
                        ultraSenseModule.saveRecordedFiles(waveFilename);
                        timer.stop();
                        permissionLayout.setText("Recording stopped - Stop exercise");
                        if(bw!=null)
                            bw.close();
                        String writeText = waveFilename +" audio file & "+  textFilename +" text file saved successfully";
                        permissionLayout.setText(writeText);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (controlMsg==Constants.START_MARKER) {
                    if(file.exists()) {
                        try {
                            //FileInputStream fileStream = new FileInputStream(file);
                            //writer = new FileWriter(file);
                            permissionLayout.setText("Start marker :"+String.valueOf((float)timer.getElapsedTime()/1000));
                            bw.write(String.valueOf((float)timer.getElapsedTime()/1000));
                            bw.write("\t"); bw.write("\t"); bw.write("\t");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG,"file doesn't exist");
                    }
                } else if (controlMsg==Constants.END_MARKER) {
                    if(file.exists()) {
                        try {
                            //FileInputStream fileStream = new FileInputStream(file);
                            //writer = new FileWriter(file);
                            permissionLayout.setText("End marker :"+String.valueOf((float)timer.getElapsedTime()/1000));
                            bw.write(String.valueOf((float)timer.getElapsedTime()/1000));
                            bw.newLine();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG,"file doesn't exist");
                    }
                }
            } else if (intent.getAction().equals(Constants.BROADCAST_COUNTDOWN_TIMER)) {
                Bundle bundle = intent.getExtras();
                String remainingTime = bundle.getString(Constants.TIMER);
                if (!remainingTime.trim().equals("1")) {
                    permissionLayout.setText("Remaining time to start : "+ remainingTime);
                }  else if (remainingTime.trim().equals("1")) {
                    waveFilename = Manager.getWaveFileName();
                    timer.start();
                    permissionLayout.setText("Recording started - Begin exercise");
                }
            }  else if (intent.getAction().equals(Constants.BROADCAST_RECEIVE_MESSAGES)) {
                Bundle bundle = intent.getExtras();
                String msg = bundle.getString(Constants.ERROR_MSG);
                if(!msg.equals("")) {
                    permissionLayout.setText("MESSAGE: " +msg);
                }
            }

        }
    };

    private void getPermissionForMicrophone() {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    Config.PERMISSIONS_AUDIO,Config.REQUEST_AUDIO_RECORDING);
        } else if (permission == PackageManager.PERMISSION_GRANTED) {
            permissionLayout.setText("microphone permission granted");
            Log.d(TAG,"microphone permission granted");
        }
    }

    private void getPermissionForFile() {

        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d(TAG,"permission "+permission);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    Config.PERMISSIONS_STORAGE,Config.REQUEST_EXTERNAL_STORAGE);
        } else if (permission == PackageManager.PERMISSION_GRANTED) {
            permissionLayout.setText("file writing permission granted");
            Log.d(TAG,"file writing permission granted");
            initFile();
        }
    }

    private void initFile() {
        try {
            //make directory for file.
            new File(Config.fileDir).mkdirs();
            textFilename = Manager.getTextFileName();
            String path = Config.fileDir + getString(R.string.app_name) +"_"+textFilename+ ".txt";
            file = new File(path);
            bw = new BufferedWriter(new FileWriter(file));
            bw.write("Markers: "+textFilename);
            bw.newLine();
            permissionLayout.setText("file created for markers");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

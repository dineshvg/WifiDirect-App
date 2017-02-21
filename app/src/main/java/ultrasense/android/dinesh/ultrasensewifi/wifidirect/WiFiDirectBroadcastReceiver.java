package ultrasense.android.dinesh.ultrasensewifi.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ultrasense.android.dinesh.ultrasensewifi.activities.MainActivity;
import ultrasense.android.dinesh.ultrasensewifi.api.StopWatch;
import ultrasense.android.dinesh.ultrasensewifi.configuration.Constants;

/**
 * Created by dinesh on 18.02.17.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = WiFiDirectBroadcastReceiver.class.getSimpleName();
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    WifiP2pConfig config = new WifiP2pConfig();
    MainActivity activity;
    private String peerDeviceName = "";
    private boolean peerConnect = false;
    private WifiP2pDevice peer;
    private Context context;
    private WifiP2pInfo info;
    private String hostAddress;
    private StopWatch.ClientAsyncTask clientAsyncTask;
    private StopWatch.ServerAsyncTask serverAsyncTask;

    public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, Context context,
                                       WifiP2pManager.Channel channel, MainActivity mainActivity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel= channel;
        this.activity = mainActivity;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"onReceive  in receiver class "+  action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //TODO tell activity that wifip2p is enabled
                //Log.d(TAG,"Wifi p2p is enabled");

            } else {
                //TODO tell activity that wifip2p is disabled
                //Log.d(TAG,"Wifi p2p is disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(wifiP2pManager!=null) {
                Log.d(TAG,"Peers have been changed");
                wifiP2pManager.requestPeers(channel,peerListListener);
                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //Log.d(TAG,"P2p connection has been changed");

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            if(wifiP2pManager!=null) {
                Log.d(TAG,"P2p configuration has been changed");
                wifiP2pManager.requestPeers(channel,peerListListener);
                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);
            }
        }

    }

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            info = wifiP2pInfo;
            if(info.isGroupOwner) {
                Log.d(TAG,"owner");
                Intent intent = new Intent(Constants.BROADCAST_RECEIVE_MESSAGES);
                intent.putExtra(Constants.ERROR_MSG, "you are group owner, u start server");
                context.sendBroadcast(intent);
            } else {
                Log.d(TAG,"not owner");
                Intent intent = new Intent(Constants.BROADCAST_RECEIVE_MESSAGES);
                intent.putExtra(Constants.ERROR_MSG, "you are group member, u be client");
                context.sendBroadcast(intent);
            }
            if(info!=null) {
                if(info.groupOwnerAddress!=null) {
                    if(info.groupOwnerAddress.getHostAddress()!=null) {
                        setHostAddress(info.groupOwnerAddress.getHostAddress());
                        clientAsyncTask = new StopWatch.ClientAsyncTask(getHostAddress(), 8988, context);
                        serverAsyncTask = new StopWatch.ServerAsyncTask(context);
                    }
                }
            }
        }
    };

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            //Log.d(TAG,"peers requested...");
            for (int i=0; i<peers.size(); i++) {
                WifiP2pDevice peer = peers.get(i);
                setPeer(peer);
                //connect();
                Log.d(TAG,"Peer name : "+ peer.deviceName);
                setPeerDeviceName(peer.deviceName);
            }
        }
    };

    public void connect() {
        config.deviceAddress = getPeer().deviceAddress;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Log.d(TAG,"On connect success");
                setPeerConnect(true);
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG,"On connect failure");
                setPeerConnect(false);
            }
        });
    }

    public void sendData(int sentVal) {
        if(isPeerConnect()) {
            if(clientAsyncTask!=null || clientAsyncTask.isCancelled()) {
                if(!clientAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    try {
                        clientAsyncTask.execute(sentVal);
                    } catch (IllegalStateException e) {
                        clientAsyncTask.cancel(true);
                        //clientAsyncTask.execute("");
                    }
                } else
                    Log.d(TAG,"Async task still running");
            } else
                Log.d(TAG, "Client is null");
        }
    }

    /*public void sendData() {
        if(isPeerConnect()) {
            if(clientAsyncTask!=null || clientAsyncTask.isCancelled()) {
                if(!clientAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    try {
                        clientAsyncTask.writeData();
                    } catch (IllegalStateException e) {
                        clientAsyncTask.cancel(true);
                        //clientAsyncTask.writeData();
                    }
                }
                else
                    Log.d(TAG,"Async task still running");
            } else
                Log.d(TAG,"Client is null");
        }
    }*/

    public void receiveData() {
        if(isPeerConnect()){
            serverAsyncTask.execute("");
        }
    }

    public void closeServerSocket() {
        serverAsyncTask.closeSocket();
    }

    public String getPeerDeviceName() {
        return peerDeviceName;
    }

    public void setPeerDeviceName(String peerDeviceName) {
        //if(isPeerConnect())
        this.peerDeviceName = peerDeviceName;
    }

    public boolean isPeerConnect() {
        return peerConnect;
    }

    public void setPeerConnect(boolean peerConnect) {
        this.peerConnect = peerConnect;
    }

    public WifiP2pDevice getPeer() {
        return peer;
    }

    public void setPeer(WifiP2pDevice peer) {
        this.peer = peer;
    }

    public String getHostAddress() {
        Log.d(TAG,"Host address is :"+hostAddress);
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        Log.d(TAG, hostAddress);
        this.hostAddress = hostAddress;
    }

    public WifiP2pInfo getInfo() {
        return info;
    }

    public void setInfo(WifiP2pInfo info) {
        this.info = info;
    }
}

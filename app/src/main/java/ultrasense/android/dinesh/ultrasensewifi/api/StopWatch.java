package ultrasense.android.dinesh.ultrasensewifi.api;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ultrasense.android.dinesh.ultrasensewifi.configuration.Constants;

/**
 * Created by dinesh on 15.11.16.
 */

/*
 *  Copyright 2006 Corey Goldberg (cgoldberg _at_ gmail.com)
 *
 *  This file is part of NetPlot.
 *
 *  NetPlot is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NetPlot is distributed in the hope that it will be useful,
 *  but without any warranty; without even the implied warranty of
 *  merchantability or fitness for a particular purpose.  See the
 *  GNU General Public License for more details.
 */


public class StopWatch {

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;


    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }


    // elaspsed time in milliseconds
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return stopTime - startTime;
    }

    /**
     * Created by dinesh on 19.02.17.
     */

    public static class ClientAsyncTask extends AsyncTask {

        private String TAG = ClientAsyncTask.class.getSimpleName();
        private int CLIENT_TIME_OUT = 10000;
        String host;
        int port;
        Socket socket = new Socket();
        Context context;

        public ClientAsyncTask(String host, int port, Context context) {
            this.host = host;
            this.port = port;
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Object doInBackground(Object[] objects) {
            /**
             * Create a socket and wait for connections. This
             * call blocks until a connection is accepted
             *
             */
            try {
                if(socket!=null) {
                    socket.close();
                    socket = new Socket();
                }
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), CLIENT_TIME_OUT);
                socket.setKeepAlive(true);
                Log.d(TAG, "socket connected");
                if(objects.length>0) {
                    int val = (int) objects[0];
                    Log.d(TAG,"obtained value is "+val);
                    write2OutputStream(socket,val);
                }
            } catch (SocketException s) {
                Log.e(TAG,"socket exception!");
                Intent intent = new Intent(Constants.BROADCAST_RECEIVE_MESSAGES);
                intent.putExtra(Constants.ERROR_MSG, "socket exception");
                context.sendBroadcast(intent);
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket!=null) {
                    if(socket.isConnected()) {
                        try {
                            Log.d(TAG,"Finally close socket");
                            socket.close();
                        } catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                }
            }

            /**
             * If this code is reached, a server has connected and transferred data
             * Save the input stream from the server
             */

            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void write2OutputStream(Socket socket, int value) {
            try {
                if(socket!=null || socket.isConnected()) {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(value);
                    Log.d(TAG,"Output written "+ value);
                    outputStream.close();
                    socket.close();
                }
            } catch (SocketException s) {
                Log.e(TAG,"socket exception app needs restart!");
                Intent intent = new Intent(Constants.BROADCAST_RECEIVE_MESSAGES);
                intent.putExtra(Constants.ERROR_MSG, "socket exception app needs restart");
                context.sendBroadcast(intent);
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket!=null) {
                    if(socket.isConnected()) {
                        try {
                            Log.d(TAG,"Finally: "+ socket.getSoTimeout());
                            socket.close();
                        } catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    /**
     * Created by dinesh on 19.02.17.
     */

    public static class ServerAsyncTask extends AsyncTask {

        private String TAG = ServerAsyncTask.class.getSimpleName();
        private Context context;
        private boolean LOOPER_FOR_SERVER = true;
        ServerSocket serverSocket;

        public ServerAsyncTask(Context context) {
            this.context = context;
        }


        @Override
        protected Object doInBackground(Object[] objects) {

            int readVal = -1;
            serverSocket = null;
            try {
                serverSocket = new ServerSocket(8988);
                Socket client;
                InputStream inputstream;
                Log.d(TAG,"Socket accepted");
                while(getLooperForServer()) {
                    client = serverSocket.accept();
                    inputstream = client.getInputStream();
                    readVal = inputstream.read();
                    if(readVal!=-1) {
                        Log.d(TAG,"input stream read");
                        if(readVal == Constants.START) {
                            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
                            intent.putExtra(Constants.WIFI_DIRECT_MSG, Constants.START);
                            context.sendBroadcast(intent);
                            Log.d(TAG,"Start recording");
                            //Log.d(TAG, "Looper value "+getLooperForServer());
                        } else if(readVal == Constants.START_MARKER) {
                            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
                            intent.putExtra(Constants.WIFI_DIRECT_MSG, Constants.START_MARKER);
                            context.sendBroadcast(intent);
                            Log.d(TAG,"Update timestamp");
                            //Log.d(TAG, "Looper value "+getLooperForServer());
                        } else if(readVal == Constants.END_MARKER) {
                            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
                            intent.putExtra(Constants.WIFI_DIRECT_MSG, Constants.END_MARKER);
                            context.sendBroadcast(intent);
                            Log.d(TAG,"Update timestamp");
                            //Log.d(TAG, "Looper value "+getLooperForServer());
                        } else if(readVal == Constants.END) {
                            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
                            intent.putExtra(Constants.WIFI_DIRECT_MSG, Constants.END);
                            context.sendBroadcast(intent);
                            Log.d(TAG,"End recording");
                            //Log.d(TAG, "Looper value "+getLooperForServer());
                        } else if (readVal == Constants.STOP) {
                            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
                            intent.putExtra(Constants.WIFI_DIRECT_MSG, Constants.STOP);
                            context.sendBroadcast(intent);
                            Log.d(TAG,"Stop server");
                            break;
                        }
                    }
                }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                try {
                    //serverSocket.close();
                    Log.d(TAG,"Server socket is closed: "+serverSocket.isClosed());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return readVal;
        }

        public void closeSocket() {
            setLooperForServer(false);
            if(serverSocket!=null) {
                try {
                    serverSocket.close();
                    Log.d(TAG,"closing socket");
                } catch (SocketException se) {
                  se.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    serverSocket = null;
                }
            }
        }

        public boolean getLooperForServer() {
            return LOOPER_FOR_SERVER;
        }

        public void setLooperForServer(boolean value) {
            this.LOOPER_FOR_SERVER = value;
        }
    }
}

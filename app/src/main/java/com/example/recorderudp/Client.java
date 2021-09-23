package com.example.recorderudp;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MicrophoneInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;


class Client extends Thread {
    public static final String TAG = "Client";
    public Handler mHandler;
    public Handler mainHandler;
    AudioRecord recorder;
    DatagramSocket clientSocket;
    static final int bytes_in_frames = 2304;
    byte[] audioData = new byte[bytes_in_frames]; //udp packet frames
    Boolean isRecording =false ;
    Visualizer v;
    private final Object lock = new Object();
    Runnable recordRun = () -> { //Runable responsible for sending mic data with UDP on recordRunT
        while(true) {
            if (isRecording) {
                //long start2 = System.currentTimeMillis();
                int res = recorder.read(audioData, 0, bytes_in_frames, AudioRecord.READ_BLOCKING);

                //e_send_to_ui(" Number red or error: " + res);
                DatagramPacket dp = new DatagramPacket(audioData, bytes_in_frames);
                //long end2 = System.currentTimeMillis();
                //e_send_to_ui("Elapsed Time in milli seconds: "+ (end2-start2));
                try {
                    clientSocket.send(dp);
                } catch (IOException e) {
                    logExpection(e);
                }
            }else{
                try {
                    synchronized (lock){
                        /// send silence for 16 times
                        Arrays.fill(audioData , (byte)(0));
                        for(int i =0 ; i<16 ; i++){
                            DatagramPacket dp = new DatagramPacket(audioData, bytes_in_frames);
                            try {
                                clientSocket.send(dp);
                            } catch (IOException e) {
                                logExpection(e);
                            }
                        }
                        e_send_to_ui(" Waiting for recorder to send..");
                        lock.wait();
                    }

                } catch (InterruptedException e) {
                    logExpection(e);
                }
            }
        }
    };
    InetAddress ip;
    int port = 6666;
    int minBuffSize = 16000;

    //////////////////
    /// INITIALIZE ///
    /////////////////
    Client(Handler mainHandler){
        this.mainHandler = mainHandler;

    }

    public void run() { // Thread of Client responsible for handling ui send requests Thread 2
        try {
            clientSocket = new DatagramSocket();
            ip = InetAddress.getByName("192.168.0.12");
            clientSocket.connect(ip,port);
            //Build Audio Recorder
            minBuffSize =  AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
            Log.i(TAG, (Thread.currentThread().getName() + ": minbufsize: "+minBuffSize));
            recorder = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(44100)
                            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                            .build())
                    .setBufferSizeInBytes(2*minBuffSize)
                    //.setBufferSizeInBytes(14112)
                    .build();

            int recordingState  =recorder.getRecordingState();
            List<MicrophoneInfo> list_mic_info  =recorder.getActiveMicrophones();
            e_send_to_ui("Min buff Size : "+minBuffSize);
           // e_send_to_ui(" Number of mic list : " + list_mic_info.size());
            e_send_to_ui(" recorder.getBufferSizeInFrames() " + recorder.getBufferSizeInFrames());
            e_send_to_ui(" recorder.getRecordingState(): " + recordingState);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.i(TAG, (Thread.currentThread().getName() + ": Expection catched: "+e.getMessage()));
            e_send_to_ui((Thread.currentThread().getName() + ": Expection catched: " + e.getMessage()));
        } catch (UnknownHostException e) {
            logExpection(e);
        } catch (IOException e) {
            logExpection(e);
        }
        /// Other Stuff
        Thread recordRunT = new Thread(recordRun);
        recordRunT.start();

        v = new Visualizer(mainHandler , audioData);
        v.start();

        Looper.prepare();

        mHandler = new Handler(Looper.myLooper()) { //create handler
            public void handleMessage(Message msg) {
                if(msg.what == MainActivity.OUTGOING_MSG){
                    //send it to server
                    Log.i(TAG, (Thread.currentThread().getName() + ": ButtonPressed: "+msg.obj));
                    if(isRecording){
                        isRecording=false;
                        recorder.stop();
                        v.pause();
                    }else{
                        isRecording=true;
                        e_send_to_ui("Sending");
                        synchronized (lock){
                            lock.notify();
                        }
                        v.begin();
                        synchronized (v.mlock){
                            v.mlock.notify();
                        }
                        recorder.startRecording();
                    }


                    /*
                    try {

                        String str = ((String)(msg.obj) + '\n') ;
                        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, port);
                        clientSocket.send(dp);

                    } catch (IOException|NullPointerException e) {
                        e.printStackTrace();
                        Log.i(TAG, (Thread.currentThread().getName() + ": Expection catched: "+e.getMessage()));
                        e_send_to_ui((Thread.currentThread().getName() + ": Expection catched: " + e.getMessage()));
                    }*/
                }else if(msg.what == MainActivity.COMMAND){
                    ((Runnable)msg.obj).run();
                }
            }
        };
        Message msg_h = mainHandler.obtainMessage();
        msg_h.what = MainActivity.CLIENT_HANDLER;
        msg_h.obj = mHandler;
        mainHandler.sendMessage(msg_h); //connect handler to ui
        Log.i(TAG, (Thread.currentThread().getName() + ": Sent handler ref to main looping client handler now "));
        e_send_to_ui((Thread.currentThread().getName() + ": Sent handler ref to main looping client handler now " ));
        Looper.loop(); //Everything done Msg handler is looping , can communicate with UI
        e_send_to_ui((Thread.currentThread().getName() + ": End of main looper: " ));
    }
    public void e_send_to_ui(String m){
        Message msg = mainHandler.obtainMessage();
        msg.what = MainActivity.INCOMING_MSG;
        msg.obj = Thread.currentThread().getName() + "|| " +m;
        mainHandler.sendMessage(msg);
    }

    public void logExpection(Exception e){

        Log.i(TAG, (Thread.currentThread().getName() + ": Expection catched: "+e.getMessage()));
        e_send_to_ui((Thread.currentThread().getName() + ": Expection catched: " + e.getMessage()));
    }
    /*public void restartListener(){
        clientListenerT = new Thread(listenerRunnable);
        clientListenerT.start();
    }*/
}

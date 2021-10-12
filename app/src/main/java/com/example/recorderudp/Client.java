package com.example.recorderudp;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;


class Client extends Thread {
    public static final String TAG = "Client";
    public Handler mHandler; //This threads handler
    public Handler mainHandler; //UI thread handler
    AudioRecord recorder; // Recorder to read digital signals from
    DatagramSocket clientSocket; // UDP socket
    static final int bytes_in_frames = 2304; //number of bytes per udp packet (currently random can be changed)
    byte[] audioData = new byte[bytes_in_frames]; //udp packets that contain raw pcm audio data
    Boolean isRecording =false ;
    Visualizer v; //UI object that shows microphone amplitude
    private final Object lock = new Object();
    Runnable recordRun = () -> { //Runnable responsible for sending live microphone data with UDP on recordRunT thread
        while(true) {
            if (isRecording) {
                //while recording keep reading microphone sensor and send UDP packets to server
                recorder.read(audioData, 0, bytes_in_frames, AudioRecord.READ_BLOCKING);
                DatagramPacket dp = new DatagramPacket(audioData, bytes_in_frames);
                try {
                    clientSocket.send(dp);
                } catch (IOException e) {
                    logExpection(e);
                }
            }else{
                //if not recording send digital silence (all zeros)
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
                        //block recordRunT thread until its woken up again to record
                        e_send_to_ui("Stopped Waiting for recorder..");
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

    public void run() { // run method of Client thread ,  responsible for coordinating with UI
        try {
            clientSocket = new DatagramSocket();
            ip = InetAddress.getByName("192.168.0.12");
            clientSocket.connect(ip,port);
            //Build Audio Recorder with sample rate of 44100hz  , 2 channel(STEREO) , 2byte(16bit) amplitude resolution per sample
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
                    .build();

            
        } catch (SocketException e) {
            e.printStackTrace();
            logExpection(e);
        } catch (UnknownHostException e) {
            logExpection(e);
        }

        Thread recordRunT = new Thread(recordRun);
        recordRunT.start();/// Start thread for recording and streaming

        v = new Visualizer(mainHandler , audioData);
        v.start(); /// Start UI Visualizer that shows microphone signal amplitude

        Looper.prepare();
        // Prepare to loop this thread
        mHandler = new Handler(Looper.myLooper()) { //create handler
            public void handleMessage(Message msg) {//parse incoming messages from UI thread while looping
                if(msg.what == MainActivity.OUTGOING_MSG){//if UI Record button is pressed

                    Log.i(TAG, (Thread.currentThread().getName() + ": Record button Pressed: "+msg.obj));
                    if(isRecording){ //if state was recording stop recording
                        isRecording=false;
                        recorder.stop();
                        v.pause();
                    }else{//if state was not recording start recording
                        isRecording=true;
                        e_send_to_ui("Recording and sending UDP Packets... ");
                        synchronized (lock){
                            lock.notify();
                        }
                        v.begin();
                        synchronized (v.mlock){
                            v.mlock.notify();
                        }
                        recorder.startRecording();
                    }

                }else if(msg.what == MainActivity.COMMAND){ // run the Runnable from UI thread
                    ((Runnable)msg.obj).run();
                }
            }
        };
        Message msg_h = mainHandler.obtainMessage();
        msg_h.what = MainActivity.CLIENT_HANDLER;
        msg_h.obj = mHandler;
        mainHandler.sendMessage(msg_h); //Send handler reference  to UI thread
        Log.i(TAG, (Thread.currentThread().getName() + ": Sent handler ref to main ,looping client handler now "));

        Looper.loop(); //Looping the Client Thread
        e_send_to_ui((Thread.currentThread().getName() + ": End of main looper: " ));
    }
    public void e_send_to_ui(String m){ //Function that prints strings to UI
        Message msg = mainHandler.obtainMessage();
        msg.what = MainActivity.INCOMING_MSG;
        msg.obj = Thread.currentThread().getName() + "|| " +m;
        mainHandler.sendMessage(msg);
    }

    public void logExpection(Exception e){
        Log.i(TAG, (Thread.currentThread().getName() + ": Expection catched: "+e.getMessage()));
        e_send_to_ui((Thread.currentThread().getName() + ": Expection catched: " + e.getMessage()));
    }

}

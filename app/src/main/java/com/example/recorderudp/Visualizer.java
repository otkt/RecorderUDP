package com.example.recorderudp;

import android.os.Handler;
import android.os.Message;

public class Visualizer extends  Thread{
    byte[] inputArray;
    final String refString = "I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I";
    String outputString = "I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I ";
    boolean mrun = false ;
    Handler mainHandler  ;
    Object mlock = new Object();

    Visualizer(Handler mainHandler , byte[] inputArray ){
        /*
        Visualizer objects that is responsible for representing microphone sensor amplitude
         */
        this.inputArray  = inputArray ;
        this.mainHandler = mainHandler ;
    }
    public void run(){//Runnable for Visualizer thread

        while(true){
            if(mrun) {//if recording draw microphone amplitude representation every 50ms
                process();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{//if not recording block the thread until recording
                to_ui("Not running. on wait()...");
                synchronized (mlock){
                    try {
                        mlock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }

    public void begin(){
        mrun = true ;
    }

    public void pause(){
        mrun = false ;
    }

    private void process() {
        /*
            Read some pcm data every 50 ms get the max amplitude
         */
        int max = 0;
        int cur = 0;
        for (int i = 1 ;  i < Client.bytes_in_frames-8 ; i = i+4){
            int inp = Math.abs(inputArray[i]);
            float res = ((float)(inp)) /128 ;
            cur = ((int)(res*100));
            if(cur > max){
                max = cur;
            }

        }
        // Represent max amplitude as string for drawing
        outputString = refString.substring(0,max);
        to_ui(outputString);

    }
    public void to_ui(String m){//Draw amplitude to UI Visualizer Box
        Message msg = mainHandler.obtainMessage();
        msg.what = MainActivity.VIS;
        msg.obj = m;
        mainHandler.sendMessage(msg);
    }


}

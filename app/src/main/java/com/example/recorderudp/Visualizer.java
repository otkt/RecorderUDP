package com.example.recorderudp;

import android.os.Handler;
import android.os.Message;

public class Visualizer extends  Thread{
    byte[] inputArray;
    final String refString = "I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I";
    String outputString = "I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I I ";
    int outRepresentation;

    boolean mrun = false ;
    Handler mainHandler  ;
    Object mlock = new Object();
    boolean mgoingup = true ;
    Visualizer(Handler mainHandler , byte[] inputArray ){
        this.inputArray  = inputArray ;
        this.mainHandler = mainHandler ;
    }
    public void run(){

        while(true){
            if(mrun) {
                process();
                //to_ui(outputString);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
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
    ///Other Methods
    public void begin(){
        mrun = true ;
    }

    public void pause(){
        mrun = false ;
    }
    private void zigzag(){
        if(outputString.length() > 79 && mgoingup){
            mgoingup = false;
        }else if(outputString.length() < 5 && !mgoingup){
            mgoingup = true;
        }

        if(mgoingup){
            outputString  = outputString + " I";
        }else{
            outputString = outputString.substring(0 , outputString.length()-2);
        }
    }
    private void process() {
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
        //int inp0 = Byte.toUnsignedInt(inputArray[0]);
        //inp0 = (inp0 > 127) ?  ~inp0 : inp0 ;
        //to_ui(Math.abs(inputArray[1]) + " || "+Math.abs(inputArray[5]) + " || "+Math.abs(inputArray[9])
                //+" || "+Math.abs(inputArray[13]) );
        outputString = refString.substring(0,max);
        to_ui(outputString);

    }
    public void to_ui(String m){
        Message msg = mainHandler.obtainMessage();
        msg.what = MainActivity.VIS;
        msg.obj = m;
        mainHandler.sendMessage(msg);
    }


}

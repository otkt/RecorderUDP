package com.example.recorderudp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    TextView textView_activity_state;
    TextView VisBox;
    TextView info_activity_state_tw;
    View.OnClickListener clickListener;
    Button buttunX;
    Handler mainHandler;
    Handler clientHandler;
    public static final int  INCOMING_MSG =  1 ;
    public static final int  CLIENT_HANDLER =  3 ;
    public static final int  OUTGOING_MSG =  2 ;
    public static final int  COMMAND =  4 ;
    //public static final int  E_MSG=  5 ;
    public static final int VIS=6;
    //public static final int RESTART_LISTEN=7;
    Client client;
    Runnable close_socket = () ->{
        client.clientSocket.close();
    };
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Set activity content from layout
        clickListener = (View v) -> {
            if(v.getId()==R.id.buttonX) {
                Message msg_h = clientHandler.obtainMessage();
                msg_h.what = MainActivity.OUTGOING_MSG;
                String fileName = getExternalCacheDir().getAbsolutePath();
                fileName += "/audiorecordtest.ts";
                msg_h.obj = fileName;
                clientHandler.sendMessage(msg_h);
            }
        }; //Create anonymous inner class that implements OnClickListener interface
        textView_activity_state = findViewById(R.id.activity_state);//reference from layout
        VisBox = findViewById(R.id.audiogram);
        textView_activity_state.setMovementMethod(new ScrollingMovementMethod());
        buttunX = findViewById(R.id.buttonX);
        buttunX.setOnClickListener(clickListener);
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                //Log.i(TAG, (Thread.currentThread().getName() +" :Received a msg with msg.what id:" + msg.what));
                super.handleMessage(msg);
                if (msg.what == MainActivity.INCOMING_MSG ){
                    append_to_textView((String)msg.obj);
                }else if(msg.what == MainActivity.CLIENT_HANDLER){
                    clientHandler= (Handler) msg.obj;
                }else if(msg.what == MainActivity.VIS){
                    setVis((String)msg.obj);
                }
            }
        };
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        client = new Client(mainHandler);
        client.start();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        append_to_textView("onCreate() executed");
    }
    protected void onStart() {
        super.onStart();

        append_to_textView("onStart() executed");
    }
    protected void onResume() {
        super.onResume();
        append_to_textView("onResume() executed");
    }
    protected void onPause() {
        super.onPause();
        append_to_textView("onPause() executed");
    }
    protected void onStop() {
        super.onStop();
        append_to_textView("onStop() executed");
    }
    protected void onRestart() {
        super.onRestart();
        append_to_textView("onRestart() executed");
    }
    protected void onDestroy() {
        super.onDestroy();
        send_command_client(close_socket);
        append_to_textView("onDestroy() executed");
    }
    protected void append_to_textView(String s){
        if (textView_activity_state !=null && clickListener != null){
            textView_activity_state.append(System.getProperty("line.separator"));
            textView_activity_state.append(s);
        }
    }
    protected void send_command_client(Runnable r){
        Message msg_h = clientHandler.obtainMessage();
        msg_h.what = MainActivity.COMMAND;
        msg_h.obj = r;
        clientHandler.sendMessage(msg_h);
    }
    protected void setVis(String s){
        if (VisBox !=null && clickListener != null){
            VisBox.setText(s);
        }
    }

}

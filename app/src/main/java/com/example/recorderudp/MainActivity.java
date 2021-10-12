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
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    TextView textView_activity_state; //Info box
    TextView VisBox;//Visualizer to show microphone amplitude
    View.OnClickListener clickListener;
    Button recordButton;
    Handler mainHandler; // handler for UI(MainActivity)
    Handler clientHandler;//handler for Client thread
    EditText editTextIP;
    EditText editTextPort;
    public static final int  INCOMING_MSG =  1 ;
    public static final int  CLIENT_HANDLER =  3 ;
    public static final int  OUTGOING_MSG =  2 ;
    public static final int  COMMAND =  4 ;
    public static final int VIS=6;
    Client client; // client thread that is responsible for recording and sending UDP packets
    Runnable close_socket = () ->{
        client.clientSocket.close();
    };

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Set activity content from layout
        clickListener = (View v) -> {
            if(v.getId()==R.id.recorderButton) {//Record button is pressed
                Message msg_h = clientHandler.obtainMessage();
                msg_h.what = MainActivity.OUTGOING_MSG;
                String[] arr = {editTextIP.getText().toString(),editTextPort.getText().toString()};
                msg_h.obj = arr ;
                clientHandler.sendMessage(msg_h);
            }
        }; //Create anonymous inner class that implements OnClickListener interface

        //references from layout
        editTextIP = findViewById(R.id.numberIP);
        editTextIP.setText("192.168.0.12");
        editTextPort = findViewById(R.id.numberPort);
        editTextPort.setText("6666");
        textView_activity_state = findViewById(R.id.activity_state);
        VisBox = findViewById(R.id.audiogram);
        textView_activity_state.setMovementMethod(new ScrollingMovementMethod());
        recordButton = findViewById(R.id.recorderButton);
        recordButton.setOnClickListener(clickListener);
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
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
        //Request Permission for Recording Audio
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        client = new Client(mainHandler);
        client.start();//Start the client thread
        //Keep the screen ON while recording
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        append_to_textView("onCreate() executed");
    }

    protected void onDestroy() {
        super.onDestroy();
        send_command_client(close_socket);
        append_to_textView("onDestroy() executed");
    }
    protected void append_to_textView(String s){ // Append next line to info box
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

}

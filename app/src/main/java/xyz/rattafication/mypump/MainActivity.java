package xyz.rattafication.mypump;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public final String DEVICE_ADDRESS = "00:19:07:00:5F:03"; //MAC Address of Bluetooth Module
    public final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public BluetoothDevice device;
    public BluetoothSocket socket;
    public BluetoothAdapter mBluetoothAdapter;
    final String TAG = "MainActivityTag";
    boolean c_connected = false;
    boolean start_working = false;
    boolean disconnected = true;
    public OutputStream outputStream;
    public InputStream inputStream;
    public int command;
    public Button btn_connect;
    public Button btn_disconnect;
    public Button btn_send;
    public Button btn_stop;
    public Button btn_start;
    public Button btn_mreset;
    public TextView tv2;
    public TextView tv_raw;
    public TextView tv3;
    public TextView tv_test;
    public TextView tv4;
    public TextView tv_seek;
    public TextView tv1;
    public TextView tv_amount;
    public TextView tv_upcount;
    public TextView tv_cent;
    public SeekBar seekBar;
    public ImageView iv_bt;
    public ImageView iv_run;
    public ProgressBar progressBar;
    public ProgressBar progress_Bar;
    public boolean running_input;
    public boolean auto;
    public boolean under;
    public boolean found=false;
    public double collector;
    boolean silence=true;


    private  long START_TIME_IN_MILLIS;
    private  long START_TIME_IN_MILLIS1;
    private  long START_TIME_IN_MILLIS2;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;
    private long mTimerightInMillis;
    private long mTimerightInMillis1;
    private boolean over_run_timer;
    private MediaPlayer mediaPlayer;


    private DecimalFormat df;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_view);
        btn_connect=findViewById(R.id.btn_c);
        iv_bt=findViewById(R.id.iv_btindicator);
        iv_run=findViewById(R.id.iv_running);

        btn_disconnect=findViewById(R.id.btn_d);
        btn_send=findViewById(R.id.btn_send);
        btn_start=findViewById(R.id.btn_start);
        btn_stop=findViewById(R.id.btn_stop);
        btn_mreset=findViewById(R.id.btn_reset);
        tv2=findViewById(R.id.tv2);
        tv3=findViewById(R.id.tv3);
        tv4=findViewById(R.id.tv4);
        tv_seek=findViewById(R.id.tv_seek);
        tv1=findViewById(R.id.tv1);
        tv_cent=findViewById(R.id.tv_cent);
        tv_upcount=findViewById(R.id.tv_upcount);
        tv_amount=findViewById(R.id.tv_amount);
        tv_raw=findViewById(R.id.tv_raw);
        tv_test=findViewById(R.id.tv_test);
        seekBar=findViewById(R.id.seekBar2);
        progressBar=findViewById(R.id.progressBar);
        progress_Bar=findViewById(R.id.progress_bar);
        running_input=true;
        auto=false;
        BTinit();

        df = new DecimalFormat("#.##");

        ///////////////////////////////////////////

        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        over_run_timer=false;
        mTimerRunning=false;
        under=false;



        /////////////////////////////////////////////////////////////////////////////////////////
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int fixProgress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressBar.setProgress(progress);
                fixProgress=progress/2;
                tv_seek.setText(String.valueOf(fixProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tv_seek.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_seek.setVisibility(View.INVISIBLE);
                tv_test.setText(String.valueOf(fixProgress)+" min");
               command=fixProgress;
               auto=true;
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTinit()){
                    BTconnect();
                }
                else{
                    BTinit();
                }
            }
        });

        btn_mreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c_connected){

                    try {
                        outputStream.write(225);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!silence){
                    silence=true;
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (start_working) {
                    try {
                        outputStream.write(250);
                        start_working=false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    iv_run.setVisibility(View.INVISIBLE);
                    progress_Bar.setProgress(0);
                    mTextViewCountDown.setText("STOP");

                }


            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!start_working) {
                    try {
                        outputStream.write(200);
                        start_working=true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    iv_run.setVisibility(View.VISIBLE);
                    iv_run.setImageResource(R.mipmap.indicator);
                    progress_Bar.setProgress(10000);
                    mTextViewCountDown.setText("WORK");
                }
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c_connected) {
                    BTdisconnect();
                }

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c_connected) {
                    try {

                        outputStream.write(command);
                        tv2.setText(String.valueOf(command) + " min");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(silence){
                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.block);
                        mediaPlayer.start();
                        silence=false;
                    }

                    if ((command > 0) && (command < 200)) {
                        //tv_amount.setText("went for reading");
                        read_data();
                    } else {
                        //tv_amount.setText("no read");
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"press connect",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

///////////////////////////////////////////blurtooth connection starts///////////////////////////////////////////////////////////////////////////////////////

    public boolean BTinit() {
        boolean found = false;

         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }


        //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

        //check for paired devices
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Make sure to pair device on first run", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }


    public boolean BTconnect() {

        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();
            c_connected = true;
            Toast.makeText(getApplicationContext(),"Pump Module connected",Toast.LENGTH_SHORT).show();
            iv_bt.setImageResource(R.mipmap.connected_v1);
        } catch (IOException e) {
            e.printStackTrace();
            c_connected = false;
        }

        if (c_connected) {
            try {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream(); //gets the output stream of the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return c_connected;
    }

    public void BTdisconnect(){
        if(c_connected) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),"Pump Module Disconnected",Toast.LENGTH_SHORT).show();
            disconnected=true;
            iv_bt.setImageResource(R.mipmap.nosignal_v1);
        }
    }

    ///////////////////////////////Bluetooth connection ends/////////////////////////////////////////////////////////////////////////////////////////////

    public void read_data(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        int byteCount = 0;
        try {
            byteCount = inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tv3.setText(String.valueOf(byteCount));
        if(byteCount <=26)
        {
            byte[] rawBytes = new byte[byteCount];
            try {
                inputStream.read(rawBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //display.setText(String.valueOf(rawBytes));
            String volt_read = null;
            try {
                volt_read = new String(rawBytes,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            hitTimer();

            tv4.setText(volt_read);

        }
    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(over_run_timer){
                    if(under){
                        mTimeLeftInMillis = millisUntilFinished;
                        mTimerightInMillis=START_TIME_IN_MILLIS2-mTimeLeftInMillis;
                        //tv_amount.setText(String.valueOf(mTimerightInMillis));
                        collector = ((double)mTimeLeftInMillis/START_TIME_IN_MILLIS2)*100;
                        cent_show();
                        upcounter();
                        updateCountDownText();

                    }
                    mTimeLeftInMillis = millisUntilFinished;
                    mTimerightInMillis=START_TIME_IN_MILLIS1-mTimeLeftInMillis;
                   // tv_amount.setText(String.valueOf(mTimerightInMillis));
                    collector = ((double)mTimeLeftInMillis/START_TIME_IN_MILLIS2)*100;
                    upcounter();
                    cent_show();
                    updateCountDownText();

                }
                else{
                    mTimeLeftInMillis = millisUntilFinished;
                    mTimerightInMillis=START_TIME_IN_MILLIS1-mTimeLeftInMillis;
                    collector = ((double)mTimeLeftInMillis/START_TIME_IN_MILLIS1)*100;
                    cent_show();
                    upcounter();
                    updateCountDownText();

                }

            }
            @Override
            public void onFinish() {
                silence=true;
                mediaPlayer.stop();
                mediaPlayer.release();
                mTimerRunning = false;
                over_run_timer=false;
                under=false;
                progress_Bar.setProgress(0);
                tv_cent.setText("00.00 %");

            }
        }.start();
        mTimerRunning = true;

    }

    private void resetTimer() {
        mCountDownTimer.cancel();
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        //updateCountDownText();
        mTimerRunning = false;
        under=false;
        //silence=true;
        //mediaPlayer.stop();
        //mediaPlayer.release();
        hitTimer();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void hitTimer(){
        if (mTimerRunning) {
            over_run_timer=true;
            resetTimer();
            found=true;

        } else {
            if(over_run_timer) {
                START_TIME_IN_MILLIS2 = 60000 * (long) command;
                if(START_TIME_IN_MILLIS>=START_TIME_IN_MILLIS2){
                    under=true;
                    mTimeLeftInMillis = START_TIME_IN_MILLIS2;
                    startTimer();
                    updateCountDownText();
                    Toast.makeText(getApplicationContext(),"Thanks for saving water",Toast.LENGTH_SHORT).show();
                }
                else{
                    START_TIME_IN_MILLIS=START_TIME_IN_MILLIS2-mTimerightInMillis;
                    //mTimerightInMillis1=mTimerightInMillis;
                    mTimeLeftInMillis = START_TIME_IN_MILLIS;
                    updateCountDownText();
                    startTimer();
                }

            }
            else {
                START_TIME_IN_MILLIS = 60000 * (long) command;
                START_TIME_IN_MILLIS1=START_TIME_IN_MILLIS;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                //tv_raw.setText("first run");
                updateCountDownText();
                startTimer();
            }
        }
    }



    private void updateCountupText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void upcounter(){
        if(over_run_timer){
            long total_right_millies=mTimerightInMillis1+mTimerightInMillis;
            tv_upcount.setText(String.valueOf(total_right_millies));
        }
        else{
            tv_upcount.setText(String.valueOf(mTimerightInMillis));
        }

    }



    private void cent_show(){
        String n=(df.format(collector));
        float l= Float.parseFloat(n)*100;
        int p= Math.round(l);
        float f= (float) p/100;
        float a= 100-f;
        String y=(df.format(a));
        tv_cent.setText(String.valueOf(y)+"%");
        tv1.setText(String.valueOf(y)+"%");
        progress_Bar.setProgress(p);
    }




}





package com.led_on_off.led;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;


public class dinoControl extends ActionBarActivity {

    // Button btnOn, btnOff, btnDis;
    ImageButton Discnt, Abt;
    Button Up, Up2, Down, Down2, Left, Right, Mouth, Blink;
    SeekBar Eyes, Jaw;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // A list of commands that can be sent to the Arduino. At present, we can assign up to 256 distinct commands
    private final byte UP = 0;
    private final byte VERTSTOP = 1;
    private final byte DOWN = 2;
    // Additional Up-Down movement
    private final byte UP2 = 3;
    private final byte VERTSTOP2 = 4;
    private final byte DOWN2 = 5;
    // Side to side movement
    private final byte LEFT = 6;
    private final byte HORIZSTOP = 7;
    private final byte RIGHT = 8;
    // Special servo actions
    private final byte MOUTH = 9;
    private final byte EYES = 10;
    // Start listening for an int
    private final byte MOUTHstart = 100;
    private final byte EYEstart = 102;

    private static byte toByte(Enum<?> e) {
        return e.name().getBytes()[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        address = getIntent().getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the dinoControl
        setContentView(R.layout.activity_dino_control);

        //call the widgets
        Discnt = (ImageButton) findViewById(R.id.discnt);
        Abt = (ImageButton) findViewById(R.id.abt);

        // Call custom widgets
        Up = (Button) findViewById(R.id.up);
        Up2 = (Button) findViewById(R.id.up2);
        Down = (Button) findViewById(R.id.down);
        Down2 = (Button) findViewById(R.id.down2);
        Left = (Button) findViewById(R.id.left);
        Right = (Button) findViewById(R.id.right);

        Eyes = (SeekBar) findViewById(R.id.eyes);
        Jaw = (SeekBar) findViewById(R.id.jaw);

        Blink = (Button) findViewById(R.id.blink);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        Discnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });

        Blink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendByte(EYES);
            }
        });

        Eyes.setMax(90);
        Eyes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sendByte(EYEstart);
                sendByte(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Jaw.setMax(85);
        Jaw.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sendByte(MOUTHstart);
                sendByte(i + 5);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        makeListener(Up, UP, VERTSTOP);
        makeListener(Down, DOWN, VERTSTOP);
        makeListener(Left, LEFT, HORIZSTOP);
        makeListener(Right, RIGHT, HORIZSTOP);
        makeListener(Up2, UP2, VERTSTOP2);
        makeListener(Down2, DOWN2, VERTSTOP2);

    }

    private void makeListener(Button button, final byte pressed, final byte released) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        sendByte(pressed);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // RELEASED
                        sendByte(released);
                        return true;
                }
                return false;
            }
        });
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            }
        }
        finish(); //return to the first layout

    }

    private void sendByte(byte b) {
        sendByte((int) b);
    }

    private void sendByte(int b) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(b);
            } catch (IOException e) {
                msg("Error");
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void about(View v) {
        if (v.getId() == R.id.abt) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(dinoControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}

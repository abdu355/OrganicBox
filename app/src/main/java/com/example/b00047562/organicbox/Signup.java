package com.example.b00047562.organicbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;

public class Signup extends AppCompatActivity implements SensorEventListener {


    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected EditText dobEditText;
    protected Button signUpButton;
    private static final int SHAKE_THRESHOLD = 1700;
    private SensorManager MySensorManager;
    private Sensor MyAclmeter;
    private float ax, ay, az, lastx, lasty, lastz;
    private long lastUpdate;
    private ArrayList<AccelData> sensorData;
    boolean dialogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        usernameEditText = (EditText)findViewById(R.id.et_name);
        passwordEditText = (EditText)findViewById(R.id.et_pass);
        emailEditText = (EditText)findViewById(R.id.et_email);
        signUpButton = (Button)findViewById(R.id.btn_sign);
        dobEditText = (EditText)findViewById(R.id.et_dob);
        dialogShown=false;

        //Acc
        MySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (MySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            MyAclmeter = MySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            MySensorManager.registerListener(this, MyAclmeter, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d("Accelerometer not found", "Accelerometer not found");
        }
        //Acc

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Signup.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    //setProgressBarIndeterminateVisibility(true);

                    ParseUser newUser = new ParseUser();//create new user data
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    newUser.put("DOB", dobEditText.getText().toString());

                    //newUser.pinInBackground();
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            //setProgressBarIndeterminateVisibility(false);

                            if (e == null) {
                                // Success!
                                Intent intent = new Intent(Signup.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                try {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Signup.this);
                                    builder.setMessage(e.getMessage())
                                            .setTitle(R.string.signup_error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // outputUpdater.post(outputUpdaterTask);
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    //Get accelerometer values
                    ax = event.values[0];
                    ay = event.values[1];
                    az = event.values[2];
                    float speed = Math.abs(ax + ay + az - lastx - lasty - lastz) / diffTime * 10000;

                    //record accelerometer values and store in arraylist of data

                    //AccelData data = new AccelData(curTime, ax, ay, az);
                    //sensorData.add(data);


                    if (speed > SHAKE_THRESHOLD) {
                        //Log.d("sensor", "shake detected w/ speed: " + speed);
                        //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                        if(dialogShown)
                        {
                            //do nothing
                        }
                        else
                        {
                            dialogShown = true;
                            new AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Clear Fields")
                                    .setMessage("Clear All Fields?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //clear fields
                                            usernameEditText.setText("Name");
                                            emailEditText.setText("Email");
                                            passwordEditText.setText("Password");
                                            dobEditText.setText("Date of Birth");
                                            dialogShown = false;
                                        }

                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogShown = false;
                                        }
                                    })
                                    .show();
                        }

                    }

                    lastx = ax;
                    lasty = ay;
                    lastz = az;
                }
            }
        } catch (Exception e) {
           Log.d("Acc",e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

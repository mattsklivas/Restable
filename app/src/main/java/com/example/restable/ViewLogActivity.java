package com.example.restable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class ViewLogActivity extends AppCompatActivity {

    private static final String TAG = "LogsActivity";

    private LineChart humiditychart;
    private LineChart tempchart;
    private LineChart soundchart;
    private LineChart motionchart;

    private String humidity = "Humidity";
    private String temperature = "Temperature";
    private String sound = "Sound";
    private String motion = "Motion";
    private DatabaseReference databaseReference;
    private SleepData sleepData;

    private ArrayList<Float> humidityData;
    private ArrayList<Float> tempData;
    private ArrayList<Float> soundData;
    private ArrayList<Float> motionData;

    private LocalDateTime stopTime;
    private LocalDateTime startTime;

    protected TextView start_Time;
    protected TextView stop_Time;
    protected TextView average_Temp;
    protected TextView average_Humid;
    protected TextView time_Slept;

    private Duration duration;

    //protected ConstraintLayout rootLayout;
    //protected AnimationDrawable animDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log);
        Log.d(TAG, "onCreate called");

        // Add animated background gradient
        //rootLayout = (ConstraintLayout) findViewById(R.id.view_log_layout);
        //animDrawable = (AnimationDrawable) rootLayout.getBackground();
        //animDrawable.setEnterFadeDuration(10);
        //animDrawable.setExitFadeDuration(5000);
        //animDrawable.start();

        start_Time = findViewById(R.id.start_time_log);
        stop_Time = findViewById(R.id.stop_time_log);
        average_Temp = findViewById(R.id.average_temp_log);
        average_Humid = findViewById(R.id.average_humidity_log);
        time_Slept = findViewById(R.id.time_slept_log);

        sleepData = (SleepData) getIntent().getSerializableExtra("sleepData");
        assert sleepData != null;
        humidityData = sleepData.getHumidityData();
        tempData = sleepData.getTempData();
        soundData = sleepData.getSoundData();
        motionData = sleepData.getMotionData();
        startTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(sleepData.getStartTime()), ZoneId.systemDefault());
        stopTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(sleepData.getEndTime()), ZoneId.systemDefault());

        databaseReference = FirebaseDatabase.getInstance().getReference("Sessions");

        System.out.println("Dummy data if user hasn't connected to the hardware:");
        System.out.println("tempData:" + tempData);
        System.out.println("humidityData:" + humidityData);
        System.out.println("soundData:" + soundData);
        System.out.println("motionData:" + motionData);

        humiditychart = findViewById(R.id.line_chart_humidity_log);
        humiditychart.setDragEnabled(true);
        humiditychart.setScaleEnabled(true);
        humiditychart.getDescription().setEnabled(false);
        humiditychart.getXAxis().setDrawGridLines(false);
        humiditychart.getAxisRight().setEnabled(false);
        humiditychart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        tempchart = findViewById(R.id.line_chart_temp_log);
        tempchart.setDragEnabled(true);
        tempchart.setScaleEnabled(true);
        tempchart.getDescription().setEnabled(false);
        tempchart.getXAxis().setDrawGridLines(false);
        tempchart.getAxisRight().setEnabled(false);
        tempchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        soundchart = findViewById(R.id.line_chart_sound_log);
        soundchart.setDragEnabled(true);
        soundchart.setScaleEnabled(true);
        soundchart.getDescription().setEnabled(false);
        soundchart.getXAxis().setDrawGridLines(false);
        soundchart.getAxisRight().setEnabled(false);
        soundchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        motionchart = findViewById(R.id.line_chart_motion_log);
        motionchart.setDragEnabled(true);
        motionchart.setScaleEnabled(true);
        motionchart.getDescription().setEnabled(false);
        motionchart.getXAxis().setDrawGridLines(false);
        motionchart.getAxisRight().setEnabled(false);
        motionchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        setData(tempData,tempchart,temperature);
        setData(humidityData,humiditychart,humidity);
        setData(soundData,soundchart,sound);
        setData(motionData,motionchart,motion);

        duration = Duration.between(startTime, stopTime);

        start_Time.setText(String.format("Start Time %s", startTime.format(DateTimeFormatter.ofPattern("h:mm a"))));
        stop_Time.setText(String.format("Stop Time %s", stopTime.format(DateTimeFormatter.ofPattern("h:mm a"))));
        average_Temp.setText(String.format("Average Temperature (°C): %s", calculateAverage(tempData)));
        average_Humid.setText(String.format("Average Humidity (RH %%): %s", calculateAverage(humidityData)));
        time_Slept.setText(String.format(Locale.getDefault(), "Time Slept: %d Hours %d Minutes", duration.toHours(), duration.toMinutes()));
    }

    protected void setData(ArrayList<Float> data, LineChart chart, String name ){
        ArrayList<Entry> yValues = new ArrayList<>();
        for (int x = 0; x < data.size(); x++)
        {
            yValues.add(new Entry(x, data.get(x)));
        }

        LineDataSet set = new LineDataSet(yValues, name + " Data Set");
        set.setDrawValues(false);

        set.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        LineData linedata = new LineData(dataSets);

        chart.setData(linedata);
    }

    private String calculateAverage(ArrayList <Float> marks) {
        Float sum = (float) 0;
        if(!marks.isEmpty()) {
            float average;
            for (Float mark : marks) {
                sum += mark;
            }
            average = sum / marks.size();
            return String.format(Locale.getDefault(), "%.2f", average);
        }
        return "0";
    }
}
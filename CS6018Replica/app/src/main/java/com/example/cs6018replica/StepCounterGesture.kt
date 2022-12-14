package com.example.cs6018replica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepCounterGesture : AppCompatActivity(), GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener, SensorEventListener {
    private lateinit var stepCounter : TextView
    var sensorManager: SensorManager? = null
    var totalSteps = 0
    var stepsOn = false
    private lateinit var stepsDb : StepsDatabase
    private lateinit var stepsValue : TextView

    private lateinit var mDetector: GestureDetectorCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        if (stepsSensor == null) {
//            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
//        } else {
//            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
//        }

        setContentView(R.layout.activity_step_gesture)

        mDetector = GestureDetectorCompat(this, this)
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("Debug: ", "Permission for activity not granted")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQUEST_CODE

                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
        Log.d("Debug: ", "onSingleTapConfirmed: event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d("Debug: ", "onDoubleTap: $event")
        if(stepsOn){
            stepsOn = false
            super.onPause()
            sensorManager?.unregisterListener(this)
        }
        super.onResume()
        stepsOn = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }




        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
        Log.d("Debug: ", "onDoubleTapEvent: event")
        return true
    }


    fun onBackButton(view : View) {
        val intent = Intent(this, ProfileHomePage::class.java ). apply{

        }
        startActivity(intent)

    }

    override fun onDown(p0: MotionEvent): Boolean {
        Log.d("Debug: ", "onSingleTapConfirmed: event")
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        Log.d("Debug: ", "onSingleTapConfirmed: event")

    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        Log.d("Debug: ", "onSingleTapConfirmed: event")
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.d("Debug: ", "onSingleTapConfirmed: event")
        initializeDb()
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        Log.d("Debug: ", "onSingleTapConfirmed: event")

    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.d("Debug: ", "onSingleTapConfirmed: event")
        if (stepsOn){
            totalSteps++
        }
        return true
    }

    fun initializeDb(){
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date());
        val steps = StepCounter(0, currentDate)
        stepsDb = StepsDatabase.getDatabase(this)

        GlobalScope.launch(Dispatchers.IO){
            stepsDb.StepCounterDAO().insert(steps)
        }
        showYesterday()

    }

    fun showYesterday(){
        val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        var yesterday = dateFormat.format(cal)
        stepsValue = findViewById(R.id.yesterdaySteps)

        try {
            val stepString = stepsDb.StepCounterDAO().getYesterdaySteps(yesterday).toString()

            stepsValue.text = stepString
        }
        catch (error: NullPointerException){
            Log.e("error", "stepString == null")


        }
    }




    //functions needed to use Sensor functions

    override fun onResume() {
        super.onResume()
        stepsOn = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        stepsOn = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (stepsOn) {
            stepCounter = findViewById(R.id.stepCounterGesture)
            stepCounter.text = "" + event.values[0]
        }
    }

}



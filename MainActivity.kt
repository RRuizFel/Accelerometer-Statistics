package com.example.accelerometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData: MutableList<FloatArray> = mutableListOf()

    private lateinit var textViewX: TextView
    private lateinit var textViewY: TextView
    private lateinit var textViewZ: TextView
    private lateinit var textViewSpeed: TextView
    private lateinit var textViewSpeedMph: TextView
    private lateinit var textViewAverage: TextView
    private lateinit var textViewMPHAverage: TextView
    private lateinit var textViewMaximum: TextView
    private lateinit var textViewMPHMaximum: TextView
    private lateinit var textViewMinimum: TextView
    private lateinit var textViewMPHMinimum: TextView
    private lateinit var textViewVariance: TextView
    private lateinit var textViewMPHVariance: TextView
    private lateinit var textViewStandardDeviation: TextView
    private lateinit var textViewMPHStandardDeviation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        textViewX = findViewById(R.id.textViewX)
        textViewY = findViewById(R.id.textViewY)
        textViewZ = findViewById(R.id.textViewZ)
        textViewSpeed = findViewById(R.id.textViewSpeed)
        textViewSpeedMph = findViewById(R.id.textViewSpeed)
        textViewAverage = findViewById(R.id.textViewAverage)
        textViewMPHAverage = findViewById(R.id.textViewMPHAverage)
        textViewMaximum = findViewById(R.id.textViewMaximum)
        textViewMPHMaximum = findViewById(R.id.textViewMPHMaximum)
        textViewMinimum = findViewById(R.id.textViewMinimum)
        textViewMPHMinimum = findViewById(R.id.textViewMPHMinimum)
        textViewVariance = findViewById(R.id.textViewVariance)
        textViewMPHVariance = findViewById(R.id.textViewMPHVariance)
        textViewStandardDeviation = findViewById(R.id.textViewStandardDeviation)
        textViewMPHStandardDeviation = findViewById(R.id.textViewMPHStandardDeviation)

        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BODY_SENSORS),
                        1
                    )
                } else {
                    sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            } else {
                sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
        findViewById<Button>(R.id.startButton).setOnClickListener {
            startSensor()
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopSensor()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } else {
                Toast.makeText(this, "Permission denied. App cannot access the accelerometer.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val xAxisValue = event.values[0]
            val yAxisValue = event.values[1]
            val zAxisValue = event.values[2]

            val speedMph = calculateSpeed(xAxisValue, yAxisValue, zAxisValue)

            textViewX.text = "X-axis: $xAxisValue"
            textViewY.text = "Y-axis: $yAxisValue"
            textViewZ.text = "Z-axis: $zAxisValue"
            textViewSpeed.text = "Speed: ${String.format("%.2f", speedMph)} units"
            textViewSpeedMph.text = "Speed (mph): ${String.format("%.2f", speedMph * 2.23694f)} mph"

            val values = floatArrayOf(xAxisValue, yAxisValue, zAxisValue)
            sensorData.add(values)
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    private fun calculateSpeed(x: Float, y: Float, z: Float): Float {
        val deltaSpeed = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
        return deltaSpeed
    }
    private fun calculateAverage(): FloatArray {
        val size = sensorData.size
        if (size == 0) return FloatArray(3) { 0f }

        val sums = FloatArray(3)
        sensorData.forEach { values ->
            sums[0] += values[0]
            sums[1] += values[1]
            sums[2] += values[2]
        }
        return FloatArray(3) { index -> sums[index] / size }
    }
    private fun calculateMaximum(): FloatArray {
        if (sensorData.isEmpty()) return FloatArray(3) { 0f }
        val maxValues = FloatArray(3) { Float.MIN_VALUE }
        sensorData.forEach { values ->
            maxValues[0] = maxOf(maxValues[0], values[0])
            maxValues[1] = maxOf(maxValues[1], values[1])
            maxValues[2] = maxOf(maxValues[2], values[2])
        }
        return maxValues
    }
    private fun calculateMinimum(): FloatArray {
        if (sensorData.isEmpty()) return FloatArray(3) { 0f }
        val minValues = FloatArray(3) { Float.MAX_VALUE }
        sensorData.forEach { values ->
            minValues[0] = minOf(minValues[0], values[0])
            minValues[1] = minOf(minValues[1], values[1])
            minValues[2] = minOf(minValues[2], values[2])
        }
        return minValues
    }

    private fun calculateVariance(): FloatArray {
        val size = sensorData.size
        if (size == 0) return FloatArray(3) { 0f }

        val averages = calculateAverage()
        val sumSquares = FloatArray(3)
        sensorData.forEach { values ->
            sumSquares[0] += (values[0] - averages[0]).pow(2)
            sumSquares[1] += (values[1] - averages[1]).pow(2)
            sumSquares[2] += (values[2] - averages[2]).pow(2)
        }
        return FloatArray(3) { index -> sumSquares[index] / size }
    }
    private fun calculateStandardDeviation(): FloatArray {
        val variances = calculateVariance()
        return FloatArray(3) { index -> sqrt(variances[index]) }
    }
    private fun performDataAnalysis() {
        val average = calculateAverage().map { String.format("%.2f", it) }
        val maximum = calculateMaximum().map { String.format("%.2f", it) }
        val minimum = calculateMinimum().map { String.format("%.2f", it) }
        val variance = calculateVariance().map { String.format("%.2f", it) }
        val standardDeviation = calculateStandardDeviation().map { String.format("%.2f", it) }

        findViewById<TextView>(R.id.textViewAverage).text = "Average: ${average.joinToString(", ")}"
        findViewById<TextView>(R.id.textViewMaximum).text = "Maximum: ${maximum.joinToString(", ")}"
        findViewById<TextView>(R.id.textViewMinimum).text = "Minimum: ${minimum.joinToString(", ")}"
        findViewById<TextView>(R.id.textViewVariance).text = "Variance: ${variance.joinToString(", ")}"
        findViewById<TextView>(R.id.textViewStandardDeviation).text = "Standard Deviation: ${standardDeviation.joinToString(", ")}"

        val speedMphList = sensorData.map { calculateSpeed(it[0], it[1], it[2]) * 2.23694f }
        val mphAverage = speedMphList.average()
        val mphMax = speedMphList.maxOrNull() ?: 0f
        val mphMin = speedMphList.minOrNull() ?: 0f
        val mphVariance = speedMphList.map { (it - mphAverage).pow(2) }.average()
        val mphStandardDeviation = sqrt(mphVariance)

        findViewById<TextView>(R.id.textViewMPHAverage).text = "MPH Average: ${String.format("%.2f", mphAverage)} MPH"
        findViewById<TextView>(R.id.textViewMPHMaximum).text = "MPH Maximum: ${String.format("%.2f", mphMax)} MPH"
        findViewById<TextView>(R.id.textViewMPHMinimum).text = "MPH Minimum: ${String.format("%.2f", mphMin)} MPH "
        findViewById<TextView>(R.id.textViewMPHVariance).text = "MPH Variance: ${String.format("%.2f", mphVariance)}"
        findViewById<TextView>(R.id.textViewMPHStandardDeviation).text = "MPH Standard Deviation: ${String.format("%.2f", mphStandardDeviation)}"
    }
    private fun startSensor() {
        sensorData.clear()
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        Toast.makeText(this, "Sensor started", Toast.LENGTH_SHORT).show()
    }
    private fun stopSensor() {
        sensorManager.unregisterListener(this)
        Toast.makeText(this, "Sensor stopped", Toast.LENGTH_SHORT).show()
        performDataAnalysis()
    }
}
package com.example.controllertest

import io.github.controlwear.virtual.joystick.android.JoystickView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import android.Manifest
import android.view.View
import android.widget.EditText
import java.net.InetSocketAddress


private const val INTERNET_PERMISSION_CODE = 1001
const val TIMEOUT_MS = 500

fun createJsonPacket(speed: Int, angle: Int, status: String = "on"): String {
    val jsonData = JSONObject()
    jsonData.put("status", status)
    jsonData.put("angle", angle)
    jsonData.put("speed", speed)
    return jsonData.toString()
}

fun sendPacket(ipAddress: String, serverPort: Int, speed: Int, angle: Int, status: String = "on") {
    Thread {
        val jsonPacket = createJsonPacket(speed, angle, status)

        val socket = Socket()

        println("Trying to send packet: $jsonPacket")
        try {
            val socketAddress = InetSocketAddress(ipAddress, serverPort)
            socket.connect(socketAddress, TIMEOUT_MS)

            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            writer.write(jsonPacket)
            writer.newLine()
            writer.flush()

            println("Sent successfully: $jsonPacket")
        } catch (e: Exception) {
            println("Error during connect or transmission: ${e.message}")
        } finally {
            socket.close()
        }
    }.start()
}


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnOn = findViewById<Button>(R.id.btnOn)
        val btnOff = findViewById<Button>(R.id.btnOff)

        val ipAddressView = findViewById<EditText>(R.id.textIP)
        val serverPortView = findViewById<EditText>(R.id.textPort)

        var ipAddress = ipAddressView.text.toString()
        var serverPort = serverPortView.text.toString().toInt()

        // check for permissions based on version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
                INTERNET_PERMISSION_CODE
            )
        }
        else
        {
            Toast.makeText(
                this@MainActivity,
                "Version don´t need special permission",
                Toast.LENGTH_LONG
            ).show()
        }


        btnOn.setOnClickListener{
            ipAddress = ipAddressView.text.toString()
            serverPort = serverPortView.text.toString().toInt()
            Toast.makeText(
                this@MainActivity,
                "Turning On",
                Toast.LENGTH_LONG
            ).show()
            sendPacket(ipAddress, serverPort, 0, 0, "on")
        }
        btnOff.setOnClickListener{
            ipAddress = ipAddressView.text.toString()
            serverPort = serverPortView.text.toString().toInt()
            Toast.makeText(
                this@MainActivity,
                "Turning Off",
                Toast.LENGTH_LONG
            ).show()
            sendPacket(ipAddress, serverPort, 0, 0, "off")
        }

        // snippet copied from https://github.com/controlwear/virtual-joystick-android#gist
        val joystick = findViewById<View>(R.id.joystickView) as JoystickView
        joystick.setOnMoveListener { angle, strength ->
            // steeringAngle: 0 at vertical, -ve at left, +ve at right
            // strength: [0, 100]
            val steeringAngle = 90 - angle
            // send packet
            sendPacket(ipAddress, serverPort, strength, steeringAngle, "on")
        }

    }
}
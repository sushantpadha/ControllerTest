package com.example.controllertest

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
import android.widget.EditText
import java.net.InetSocketAddress

private val INTERNET_PERMISSION_CODE = 1001
public val TIMEOUT_MS = 500

val serverPort = 4321 // Server Port

fun createJsonPacket(of_off : String ): String {
    val jsonData = JSONObject()
    jsonData.put("status", "ok")
    jsonData.put("gpio", "$of_off")
    return jsonData.toString()
}

fun send_package(ip_address :String, on_off : String) {
    Thread {
        val jsonPacket = createJsonPacket(on_off)

        val socket = Socket()

        try {
            val socketAddress = InetSocketAddress(ip_address, serverPort)
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
        val btn_on = findViewById<Button>(R.id.btnOn)
        val btn_off = findViewById<Button>(R.id.btnOff)
        val input_field = findViewById<EditText>(R.id.textIP)
        var ip_address=""

        ip_address = input_field.text.toString()

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


        btn_on.setOnClickListener{
            ip_address = input_field.text.toString()
            Toast.makeText(
                this@MainActivity,
                "Turning On",
                Toast.LENGTH_LONG
            ).show()
            send_package("$ip_address","on")
        }
        btn_off.setOnClickListener{
            ip_address = input_field.text.toString()
            Toast.makeText(
                this@MainActivity,
                "Turning Off",
                Toast.LENGTH_LONG
            ).show()
            send_package("$ip_address","off")
        }


    }
}
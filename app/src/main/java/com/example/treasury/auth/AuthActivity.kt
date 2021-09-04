package com.example.treasury.auth

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.treasury.R
import java.math.BigInteger
import java.security.MessageDigest

class AuthActivity : AppCompatActivity() {

    private val passwordProcessor = PasswordProcessor(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (!passwordProcessor.havePassword()){
            setPasswordDialog()
        }

        findViewById<Button>(R.id.verify_button)
            .setOnClickListener {
                val input = findViewById<EditText>(R.id.password_edit).text.toString()
                val success = passwordProcessor.verifyPassword(input)
                if (success){
                    finish()
                }else{
                    Toast.makeText(this, "密碼錯誤", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun setPasswordDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editText = EditText(this) //final一個editText
        builder.setView(editText)
        builder.setTitle("設定密碼")
        builder.setPositiveButton("確定") { _, _ ->
            val title = editText.text.toString()
            if (title.replace("\\s+".toRegex(), "") != "") {
                passwordProcessor.setPassword(title)
            }else{
                Toast.makeText(this, "密碼不能為空", Toast.LENGTH_LONG).show()
                setPasswordDialog()
            }
        }
        builder.create().show()
    }
}
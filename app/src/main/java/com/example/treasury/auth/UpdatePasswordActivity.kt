package com.example.treasury.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.treasury.R

class UpdatePasswordActivity : AppCompatActivity() {

    private val passwordProcessor = PasswordProcessor(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        findViewById<Button>(R.id.verify_button)
            .setOnClickListener {
                val originalPassword = findViewById<EditText>(R.id.original_password_edit).text.toString()
                val success = passwordProcessor.verifyPassword(originalPassword)
                if (success){
                    val newPassword = findViewById<EditText>(R.id.new_password_edit).text.toString()
                    passwordProcessor.setPassword(newPassword)
                    finish()
                }else{
                    Toast.makeText(this, "密碼錯誤", Toast.LENGTH_LONG).show()
                }
            }
    }
}
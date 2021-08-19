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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val user = getSharedPreferences("user", MODE_PRIVATE)
        var password = user.getString("password", null)
        if (password == null){
            setPasswordDialog()
        }

        findViewById<Button>(R.id.verify_button)
            .setOnClickListener {
                val input = findViewById<EditText>(R.id.password_edit).text.toString()
                val success = verifyPassword(input)
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
                setPassword(title)
            }else{
                Toast.makeText(this, "密碼不能為空", Toast.LENGTH_LONG).show()
                setPasswordDialog()
            }
        }
        builder.create().show()
    }
    private fun sha256(input: String): String{
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        digest.update(input.toByteArray())
        return java.lang.String.format("%064x", BigInteger(1, digest.digest()))
    }
    private fun setPassword(password: String){
        val hash = sha256(password)
        val user = getSharedPreferences("user", MODE_PRIVATE)
        user.edit().putString("password", hash).apply()
    }
    private fun verifyPassword(password: String): Boolean{
        val hash = sha256(password)
        val user = getSharedPreferences("user", MODE_PRIVATE)
        val currentHash = user.getString("password", null)
        println(hash)
        println(currentHash)
        return hash == currentHash
    }
}
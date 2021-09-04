package com.example.treasury.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger
import java.security.MessageDigest

class PasswordProcessor(base: Context) : ContextWrapper(base){
    private fun sha256(input: String): String{
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        digest.update(input.toByteArray())
        return java.lang.String.format("%064x", BigInteger(1, digest.digest()))
    }
    fun verifyPassword(password: String): Boolean{
        val hash = sha256(password)
        val user = getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val currentHash = user.getString("password", null)
        return hash == currentHash
    }
    fun setPassword(password: String){
        val hash = sha256(password)
        val user = getSharedPreferences("user", MODE_PRIVATE)
        user.edit().putString("password", hash).apply()
    }
    fun havePassword(): Boolean{
        val user = getSharedPreferences("user", MODE_PRIVATE)
        val password = user.getString("password", null)
        return password != null
    }
}
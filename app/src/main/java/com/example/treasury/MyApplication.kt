package com.example.treasury

import android.app.Application
import com.example.treasury.form.FormDatabase
import com.example.treasury.form.FormRepository
import java.util.*
import kotlin.properties.Delegates

class MyApplication : Application() {
    private val formDao by lazy { FormDatabase.getInstance(this).getDao() }
    val formRepository by lazy { FormRepository(formDao) }

    override fun onCreate() {
        super.onCreate()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        current = year * 12 + month
    }

    companion object{
        const val start = 1951*12
        const val end = 2100*12
        var current by Delegates.notNull<Int>()
    }
}
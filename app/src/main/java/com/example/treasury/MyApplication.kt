package com.example.treasury

import android.app.Application
import com.example.treasury.date.DateDatabase
import com.example.treasury.date.DateRepository
import com.example.treasury.form.FormDatabase
import com.example.treasury.form.FormRepository
import java.util.*
import kotlin.properties.Delegates

class MyApplication : Application() {
    private val formDao by lazy { FormDatabase.getInstance(this).getDao() }
    private val dateDao by lazy { DateDatabase.getInstance(this).getDao() }
    val formRepository by lazy { FormRepository(formDao) }
    val dateRepository by lazy { DateRepository(dateDao) }

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
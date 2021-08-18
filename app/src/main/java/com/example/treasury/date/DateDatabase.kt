package com.example.treasury.date

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Date::class], version = 1, exportSchema = false)
abstract class DateDatabase : RoomDatabase(){
    abstract fun getDao(): DateDao

    companion object{
        private var instance: DateDatabase? = null
        private const val databaseName = "Treasury.db"

        fun getInstance(context: Context): DateDatabase {
            instance ?: synchronized(DateDatabase::class){
                instance = Room.databaseBuilder(context, DateDatabase::class.java, databaseName)
                    .addMigrations(migration_3_1)
                    .build()

            }
            return instance!!
        }
        val migration_3_1 = object : Migration(3, 1){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tbl_date RENAME TO tbl_date_old")
                database.execSQL("CREATE TABLE tbl_date (" +
                        "year_month INTEGER PRIMARY KEY NOT NULL," +
                        "year TEXT NOT NULL," +
                        "month TEXT NOT NULL," +
                        "day TEXT NOT NULL)")
                database.execSQL("INSERT INTO tbl_date SELECT * FROM tbl_date_old")
                database.execSQL("DROP TABLE tbl_date_old")
            }
        }
    }
}
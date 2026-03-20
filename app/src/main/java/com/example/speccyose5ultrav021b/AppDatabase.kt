package com.example.speccyose5ultrav021b

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Game::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speccy_os_database"
                )
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Mejor rendimiento en concurrencia
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Optimización de SQLite para evitar congelaciones
                        db.execSQL("PRAGMA synchronous = NORMAL")
                        db.execSQL("PRAGMA temp_store = MEMORY")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

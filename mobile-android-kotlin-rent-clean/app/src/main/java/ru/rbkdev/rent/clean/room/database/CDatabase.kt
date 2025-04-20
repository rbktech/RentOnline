package ru.rbkdev.rent.clean.room.database

import ru.rbkdev.rent.clean.room.database.keys.CKeysDao
import ru.rbkdev.rent.clean.room.database.keys.CKeysTable

import android.content.Context

import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CKeysTable::class],
    version = 1,
    exportSchema = false
)

/***/
abstract class CDatabase : RoomDatabase() {

    /***/
    abstract fun mKeysDao(): CKeysDao

    companion object {

        @Volatile
        private var mInstance: CDatabase? = null

        /** Singleton */
        fun getInstance(context: Context): CDatabase {

            /** "synchronized" - Only one thread can access instance */
            return mInstance ?: synchronized(this) {

                val instance = Room
                    .databaseBuilder(context.applicationContext, CDatabase::class.java, DATA_BASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()

                mInstance = instance

                instance
            }
        }
    }
}
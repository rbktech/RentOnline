package ru.rbkdev.rent.room.database.keys

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import ru.rbkdev.rent.room.database.*
import ru.rbkdev.rent.ui.house.entry.CStatus

import java.io.Serializable

/***/
@Entity(tableName = DATA_BASE_TABLE)
data class CKeysTable(
    /***/
    @PrimaryKey(autoGenerate = true) var idDatabase: Long = 0
) : Serializable {

    /***/
    @ColumnInfo(name = COLUMN_STATUS)
    var status: CStatus = CStatus.NULL

    /***/
    @ColumnInfo(name = COLUMN_ID_HOUSE)
    var idHouse: String = ""

    /***/
    @ColumnInfo(name = COLUMN_ADDRESS_HOUSE)
    var addressHouse: String = ""

    /***/
    @ColumnInfo(name = COLUMN_ADDRESS_LOCK)
    var addressLock: String = ""

    /***/
    @ColumnInfo(name = COLUMN_COUNTER)
    var counter: Int = 0

    /***/
    @ColumnInfo(name = COLUMN_ADDRESS_BRIDGE)
    var addressBridge: String = ""

    /***/
    @ColumnInfo(name = COLUMN_TIME_BEGIN)
    var timeBegin: String = ""

    /***/
    @ColumnInfo(name = COLUMN_TIME_END)
    var timeEnd: String = ""

    /***/
    @ColumnInfo(name = COLUMN_CODE_PASSWORD)
    var codePassword: String = ""

    /***/
    @ColumnInfo(name = COLUMN_LATITUDE)
    var latitude: Double = 0.0

    /***/
    @ColumnInfo(name = COLUMN_LONGITUDE)
    var longitude: Double = 0.0
}
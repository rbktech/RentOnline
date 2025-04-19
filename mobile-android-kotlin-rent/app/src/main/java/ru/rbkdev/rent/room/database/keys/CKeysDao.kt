package ru.rbkdev.rent.room.database.keys

import androidx.room.*
import androidx.lifecycle.LiveData

/***/
@Dao
interface CKeysDao {

    /***/
    @Query("SELECT * FROM keys_table ORDER BY address_house DESC")
    fun getAlphabetizedBarcodes(): LiveData<List<CKeysTable>>

    /***/
    @Query("SELECT * FROM keys_table WHERE idDatabase = :value")
    fun get(value: Long): CKeysTable?

    /***/
    @Query("SELECT * FROM keys_table WHERE address_house LIKE :value")
    fun find(value: String): CKeysTable?

    /***/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(value: CKeysTable)

    /***/
    @Update
    fun update(value: CKeysTable)

    /***/
    @Delete
    fun delete(value: CKeysTable)

    /***/
    @Query("DELETE FROM keys_table")
    fun clear()

    /***/
    @Query("DELETE FROM keys_table WHERE address_house LIKE :value")
    fun deleteList(value: String)
}
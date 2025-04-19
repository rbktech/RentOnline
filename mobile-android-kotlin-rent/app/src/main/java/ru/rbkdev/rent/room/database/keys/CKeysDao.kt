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
    // @Query("SELECT * FROM keys_table WHERE idDatabase = :value")
    // suspend fun get(value: Long): CKeysTable?

    /***/
    // @Query("SELECT * FROM keys_table WHERE address_house LIKE :value")
    // suspend fun find(value: String): CKeysTable?

    /***/
    // @Insert(onConflict = OnConflictStrategy.IGNORE)
    // suspend fun insert(value: CKeysTable)

    /***/
    // @Update
    // suspend fun update(value: CKeysTable)

    /***/
    // @Delete
    // suspend fun delete(value: CKeysTable)

    /***/
    // @Query("DELETE FROM keys_table")
    // suspend fun clear()

    /***/
    // @Query("DELETE FROM keys_table WHERE address_house LIKE :value")
    // suspend fun deleteList(value: String)
}
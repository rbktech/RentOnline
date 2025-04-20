package ru.rbkdev.rent.clean.room

import android.app.Application
import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.AndroidViewModel
import ru.rbkdev.rent.clean.room.database.CDatabase
import ru.rbkdev.rent.clean.room.database.keys.CKeysDao
import ru.rbkdev.rent.clean.room.database.keys.CKeysTable

import kotlinx.coroutines.runBlocking

abstract class CViewModelBase(application: Application) : AndroidViewModel(application) {

    /**
     * Get database
     */
    private val mDatabase: CDatabase = CDatabase.getInstance(application)

    /** Type */
    // -------------------------------------------------------------------

    /**
     * Get dao type of database
     */
    protected val mKeysDao: CKeysDao = mDatabase.mKeysDao()

    /*fun getTypeLabel(id: Long): String = runBlocking {

        val data: CKeysDao? = mTypeDao.get(id)
        if (data != null)
            return@runBlocking data.type

        ""
    }*/

    /*fun getListType(): LiveData<List<CKeysDao>> {
        return mTypeDao.getTypeList()
    }*/

    /*fun resetListType(data: TListData) {
        viewModelScope.launch {

            mTypeDao.clear()
            for ((k, v) in data.list)
                mTypeDao.insert(CTypeTable(k, v))
        }
    }*/

    /** Location */
    // -------------------------------------------------------------------

    /**
     * Get dao location of database
     */
    /*private val mLocationDao: CLocationDao = mDatabase.mLocationDao()

    fun getListLocation(): LiveData<List<CLocationTable>> {
        return mLocationDao.getLocation()
    }

    fun getLocationLabel(id: Long): String = runBlocking {

        val data = mLocationDao.get(id)
        if (data != null)
            return@runBlocking data.location

        ""
    }

    fun resetListLocation(data: TListData) {
        viewModelScope.launch {

            mLocationDao.clear()
            for ((k, v) in data.list)
                mLocationDao.insert(CLocationTable(k, v))
        }
    }*/

    /** Barcode */
    // -------------------------------------------------------------------

    /**
     * Get dao barcode of database
     */
    //protected val mBarcodeDao: CBarcodeDao = mDatabase.mBarcodeDao()

    /** Abstract */
    // -------------------------------------------------------------------

    /*abstract fun find(context: Context, barcode: TBarcodeData)

    abstract fun insert(context: Context, barcode: TBarcodeData)

    abstract fun update(context: Context, barcode: TBarcodeData)

    abstract fun delete(context: Context, barcode: TBarcodeData)

    abstract fun clear()*/
}

/*open class CDatabaseViewModelItem(application: Application) : CViewModelBase(application) {

    override fun find(context: Context, barcode: TBarcodeData) = runBlocking {

        barcode.setCodeAndMessage(context, R.string.ui_error)

        val data = mBarcodeDao.find(barcode.data.barcode)
        if (data != null) {

            barcode.localId = data.id
            barcode.place = data.place

            barcode.setCharacteristic(data)

            barcode.setCodeAndMessage(context, R.string.ui_barcode_find)
        } else
            barcode.setCodeAndMessage(context, R.string.ui_barcode_find_not)
    }

    override fun update(context: Context, barcode: TBarcodeData) = runBlocking {

        barcode.setCodeAndMessage(context, R.string.ui_error)

        val data = CBarcodeTable(barcode.localId)
        data.place = barcode.place

        data.barcode = barcode.data.barcode
        data.name = barcode.data.name
        data.desc = barcode.data.desc
        data.type = barcode.data.type
        data.location = barcode.data.location

        mBarcodeDao.update(data)

        barcode.setCodeAndMessage(context, R.string.ui_barcode_update)
    }

    override fun clear() {
        viewModelScope.launch {
            mBarcodeDao.clear()
        }
    }
}*/

open class CDatabaseViewModel(application: Application) : CViewModelBase(application) {

    /**
     * Init barcode list
     */
    private val mKeysList: LiveData<List<CKeysTable>> = mKeysDao.getAlphabetizedBarcodes()

    fun getListKeys(): LiveData<List<CKeysTable>> {
        return mKeysList
    }

    fun insert(context: Context, item: CKeysTable) = runBlocking {

        //barcode.setCodeAndMessage(context, R.string.ui_error)

        /*val data = CBarcodeTable(0)

        data.barcode = barcode.data.barcode
        data.type = barcode.data.type
        data.location = barcode.data.location
        data.name = barcode.data.name
        data.desc = barcode.data.desc

        if (barcode.place.isEmpty())
            data.place = context.getString(R.string.local)
        else
            data.place = barcode.place*/

        mKeysDao.insert(item)

        //barcode.setCodeAndMessage(context, R.string.ui_barcode_insert)
    }

    fun delete(context: Context, item: CKeysTable) = runBlocking {

        //barcode.setCodeAndMessage(context, R.string.ui_error)

        mKeysDao.delete(item)

        // barcode.setCodeAndMessage(context, R.string.ui_barcode_delete)
        // barcode.setCode(R.string.ui_success)
    }


    fun update(context: Context, item: CKeysTable) = runBlocking {

        //barcode.setCodeAndMessage(context, R.string.ui_error)

        mKeysDao.update(item)

        // barcode.setCodeAndMessage(context, R.string.ui_barcode_delete)
        // barcode.setCode(R.string.ui_success)
    }

    /*fun deleteList(barcode: String) {
        viewModelScope.launch {
            mBarcodeDao.deleteList(barcode)
        }
    }

    override fun clear() {
        viewModelScope.launch {
            mBarcodeDao.clear()
        }
    }

    fun get(id: Long): String = runBlocking {

        val barcodeTable = mBarcodeDao.get(id)
        if (barcodeTable != null)
            return@runBlocking barcodeTable.barcode

        ""
    }*/
}
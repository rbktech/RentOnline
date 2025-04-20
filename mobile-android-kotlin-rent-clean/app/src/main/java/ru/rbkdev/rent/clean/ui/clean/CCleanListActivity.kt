package ru.rbkdev.rent.clean.ui.clean

import ru.rbkdev.rent.clean.*
import ru.rbkdev.rent.clean.CSettings
import ru.rbkdev.rent.clean.room.CDatabaseViewModel
import ru.rbkdev.rent.clean.room.database.keys.CKeysTable

import android.os.Bundle
import android.widget.Toast
import android.widget.ListView
import android.widget.ArrayAdapter

import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import java.util.*
import java.text.SimpleDateFormat

import kotlin.collections.ArrayList

/***/
class CCleanListActivity : AppCompatActivity() {

    private var mHouse: CKeysTable? = null

    private var mListCodePassword = mutableListOf<CDataItem>()

    private var mArrayCleanList = ArrayList<String>()

    /** BACKEND */
    // -------------------------------------------------------------------

    private var mViewModelDatabase: CDatabaseViewModel? = null

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clean_list_activity)

        mHouse = intent.getSerializableExtra(resources.getString(R.string.intent_house)) as CKeysTable?

        val swipeRefreshCleanHousesList = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshCleanHousesList)
        val lvCleanList = findViewById<ListView>(R.id.lvCleanList)

        /** Settings view module database */
        mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        /** Back button  */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mArrayCleanList)

        lvCleanList.adapter = arrayAdapter

        // lvCleanList.setOnItemClickListener { adapterView, view, i, l ->
        lvCleanList.setOnItemClickListener { _, _, i, _ ->

            if (mListCodePassword.isNotEmpty())
                setCleanHouse(mListCodePassword[i])
        }

        swipeRefreshCleanHousesList.setOnRefreshListener {
            swipeRefreshCleanHousesList.isRefreshing = false
            getCleanList(arrayAdapter)
        }

        getCleanList(arrayAdapter)
    }

    /** Back button */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun getCleanList(adapter: ArrayAdapter<String>) {

        mArrayCleanList.clear()
        mListCodePassword.clear()

        mHouse?.let { house ->

            val dataExchange = CDataListExchange()
            dataExchange.setCommand("get_clean_data")
            dataExchange.setIdUser(CSettings.getInstance().getUserId())
            dataExchange.setIdHouse(house.idHouse)

            CClient.startExchange(baseContext, dataExchange)
            if (dataExchange.getCode() == R.string.code_success) {

                dataExchange.getDataList().forEach {

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.dataBegin * 1000
                    val date = SimpleDateFormat("d MMM yyyy - HH:mm", Locale.getDefault()).format(calendar.time)

                    mArrayCleanList.add(date)
                    mListCodePassword.add(it)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun setCleanHouse(dataItem: CDataItem) {

        mHouse?.let { house ->

            val dataExchange = CDataExchange()
            dataExchange.setCommand("set_cleaning")
            dataExchange.setIdUser(CSettings.getInstance().getUserId())
            dataExchange.setIdHouse(house.idHouse)
            dataExchange.setCodePassword(dataItem.codePassword)

            CClient.startExchange(baseContext, dataExchange)
            if (dataExchange.getCode() == R.string.code_success) {

                house.timeBegin = dataItem.dataBegin.toString()
                house.timeEnd = dataItem.dataEnd.toString()
                house.codePassword = dataItem.codePassword

                mViewModelDatabase?.insert(baseContext, house)

                finish()
            }

            Toast.makeText(applicationContext, resources.getString(dataExchange.getCode()), Toast.LENGTH_SHORT).show()
        }
    }
}
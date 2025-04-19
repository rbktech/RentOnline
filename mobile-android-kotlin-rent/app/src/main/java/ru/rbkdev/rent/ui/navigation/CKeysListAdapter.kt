package ru.rbkdev.rent.ui.navigation

import ru.rbkdev.rent.R
import ru.rbkdev.rent.CClient
import ru.rbkdev.rent.CDataExchange
import ru.rbkdev.rent.CSettings
import ru.rbkdev.rent.room.CDatabaseViewModel
import ru.rbkdev.rent.room.database.keys.CKeysTable
import ru.rbkdev.rent.ui.house.control.CHouseCtrlActivity
import ru.rbkdev.rent.ui.house.entry.CHouseEntryActivity
import ru.rbkdev.rent.ui.house.entry.CStatus

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.content.Intent
import android.widget.Toast

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import java.util.*
import java.text.SimpleDateFormat

/***/
class CBarcodeListAdapter(viewModelDatabase: CDatabaseViewModel?) :
    ListAdapter<CKeysTable, CBarcodeListAdapter.CBarcodeListViewHolder>(WORDS_COMPARATOR) {

    private val mViewModelDatabase = viewModelDatabase

    companion object {

        private val WORDS_COMPARATOR = object : DiffUtil.ItemCallback<CKeysTable>() {

            override fun areItemsTheSame(oldItem: CKeysTable, newItem: CKeysTable): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: CKeysTable, newItem: CKeysTable): Boolean {
                return oldItem == newItem
            }
        }
    }

    /***/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CBarcodeListViewHolder {

        LayoutInflater.from(parent.context).inflate(R.layout.keys_list_item, parent, false).let {
            return CBarcodeListViewHolder(it, this)
        }
    }

    /***/
    override fun onBindViewHolder(holder: CBarcodeListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /***/
    inner class CBarcodeListViewHolder(itemView: View, private val adapter: CBarcodeListAdapter) :
        RecyclerView.ViewHolder(itemView) {

        private val mContext = itemView.context
        private val txtKeysListItem: TextView = itemView.findViewById(R.id.txtKeysListItem)
        private val txtHousesListTimeBegin: TextView = itemView.findViewById(R.id.txtHousesListTimeBegin)
        private val txtHousesListTimeEnd: TextView = itemView.findViewById(R.id.txtHousesListTimeEnd)
        private val btnHousesListCancelBooking: TextView = itemView.findViewById(R.id.btnHousesListCancelBooking)

        init {
            itemView.setOnClickListener { clickItem() }
            btnHousesListCancelBooking.setOnClickListener { cancel() }
        }

        /***/
        fun bind(item: CKeysTable) {
            val formatter = SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())

            txtKeysListItem.text = item.addressHouse
            txtHousesListTimeBegin.text = formatter.format(Date(item.timeBegin.toLong() * 1000L))
            txtHousesListTimeEnd.text = formatter.format(Date(item.timeEnd.toLong() * 1000L))
        }

        private fun clickItem() {

            val intent: Intent
            val house = adapter.currentList[absoluteAdapterPosition]

            intent = if (house.status == CStatus.REGISTRATION)
                Intent(mContext, CHouseCtrlActivity::class.java)
            else
                Intent(mContext, CHouseEntryActivity::class.java)

            intent.putExtra(mContext.getString(R.string.intent_house), house)

            mContext.startActivity(intent)
        }

        private fun cancel() {

            if (adapter.itemCount != 0) {
                val house = adapter.currentList[absoluteAdapterPosition]

                val dataExchange = CDataExchange()
                dataExchange.setCommand("cancel_booking")
                dataExchange.setIdHouse(house.idHouse)
                dataExchange.setCodePassword(house.codePassword)
                dataExchange.setIdUser(CSettings.getInstance().getUserId())

                CClient.startExchange(mContext, dataExchange)
                if (dataExchange.getCode() == R.string.code_success)
                    mViewModelDatabase?.delete(mContext, house)

                Toast.makeText(mContext, mContext.getString(dataExchange.getCode()), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package ru.rbkdev.rent.ui.tools

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.content.Context
import android.graphics.Color

/***/
class CSpinnerAdapter(context: Context) :
    ArrayAdapter<Pair<Long, String>>(context, android.R.layout.simple_spinner_item) {

    /***/
    private var mHideStart = -1L
    private var mHideEnd = 90000L

    private var mListEnable = mutableListOf<Long>()

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        add(Pair(0L, "00:00"))
        add(Pair(3600L, "01:00"))
        add(Pair(7200L, "02:00"))
        add(Pair(10800L, "03:00"))
        add(Pair(14400L, "04:00"))
        add(Pair(18000L, "05:00"))
        add(Pair(21600L, "06:00"))
        add(Pair(25200L, "07:00"))
        add(Pair(28800L, "08:00"))
        add(Pair(32400L, "09:00"))
        add(Pair(36000L, "10:00"))
        add(Pair(39600L, "11:00"))
        add(Pair(43200L, "12:00"))
        add(Pair(46800L, "13:00"))
        add(Pair(50400L, "14:00"))
        add(Pair(54000L, "15:00"))
        add(Pair(57600L, "16:00"))
        add(Pair(61200L, "17:00"))
        add(Pair(64800L, "18:00"))
        add(Pair(68400L, "19:00"))
        add(Pair(72000L, "20:00"))
        add(Pair(75600L, "21:00"))
        add(Pair(79200L, "22:00"))
        add(Pair(82800L, "23:00"))
    }

    /***/
    fun setListEnable(listEnable: MutableList<Long>) {

        mListEnable = listEnable
    }

    /***/
    fun setHideStart(hideStart: Long) {
        mHideStart = hideStart
    }

    /***/
    fun setHideEnd(hideEnd: Long) {
        mHideEnd = hideEnd
    }

    /***/
    fun clearHideValue() {
        mHideStart = -1L
        mHideEnd = 90000L
    }

    /***/
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.setTextColor(Color.BLACK)

        val item = getItem(position)
        if (item != null)
            textView.text = item.second

        return view
    }

    /***/
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        val item = getItem(position)
        if (item != null) {
            textView.text = item.second

            try {
                mListEnable.forEach {
                    if (it == item.first)
                        throw Exception()
                }

                textView.setBackgroundColor(Color.WHITE)

                if ((item.first > mHideStart) && (item.first < mHideEnd))
                    textView.setTextColor(Color.BLACK)
                else
                    textView.setTextColor(Color.GRAY)

            } catch (exc: Exception) {

                textView.setTextColor(Color.GRAY)
                textView.setBackgroundColor(Color.YELLOW)
            }
        }

        return view
    }

    /***/
    override fun isEnabled(position: Int): Boolean {

        val item = getItem(position)
        if (item != null) {

            mListEnable.forEach {
                if (it == item.first)
                    return false
            }

            return (item.first > mHideStart) && (item.first < mHideEnd)
        }
        return true
    }
}

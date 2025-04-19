package ru.rbkdev.rent.ui.calendar

import ru.rbkdev.rent.*
import ru.rbkdev.rent.CSettings
import ru.rbkdev.rent.ui.tools.CSpinnerAdapter
import ru.rbkdev.rent.room.CDatabaseViewModel
import ru.rbkdev.rent.room.database.keys.CKeysTable

import android.widget.*
import android.os.Bundle
import android.view.View
import android.app.Activity
import android.content.Intent
import android.graphics.Color

import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import java.time.*
import java.util.*
import java.sql.Time
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.utils.yearMonth

import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod

/***/
fun daysOfWeekFromLocale(): Array<DayOfWeek> {

    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()

    if (firstDayOfWeek != DayOfWeek.MONDAY) {

        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }

    return daysOfWeek
}

/***/
class CCalendarActivity : AppCompatActivity() {

    private val mToday = LocalDate.now()
    private var mStartDate: LocalDate? = null
    private var mEndDate: LocalDate? = null

    private var mTxtCalendarSelected: TextView? = null
    private var mSpCalendarHourBegin: Spinner? = null
    private var mSpCalendarHourEnd: Spinner? = null

    private lateinit var mListBookingData: MutableList<Pair<LocalDate, MutableList<Long>>>

    /** BACKEND */
    // -------------------------------------------------------------------

    private var mViewModelDatabase: CDatabaseViewModel? = null

    private lateinit var mResultLauncher: ActivityResultLauncher<Intent>

    /***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)

        val btnCalendarApply = findViewById<Button>(R.id.btnCalendarApply)
        val calendarView: com.kizitonwose.calendarview.CalendarView = findViewById(R.id.calendarView)
        mTxtCalendarSelected = findViewById(R.id.txtCalendarSelected)
        mSpCalendarHourBegin = findViewById(R.id.spCalendarHourBegin)
        mSpCalendarHourEnd = findViewById(R.id.spCalendarHourEnd)

        /** Settings view module database */
        mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        val house = intent.getSerializableExtra(resources.getString(R.string.intent_house)) as CKeysTable

        mSpCalendarHourBegin?.adapter = CSpinnerAdapter(baseContext)
        mSpCalendarHourEnd?.adapter = CSpinnerAdapter(baseContext)
        mSpCalendarHourEnd?.let { it.setSelection(it.count - 1) }

        mListBookingData = mutableListOf()

        mSpCalendarHourBegin?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                getData()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getData()
            }
        }

        mSpCalendarHourEnd?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                getData()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getData()
            }
        }

        val dataList = CDataListExchange()
        dataList.setIdHouse(house.idHouse)
        dataList.setIdUser(CSettings.getInstance().getAddressUser(baseContext))
        dataList.setCommand("get_user_date")
        CClient.startExchange(baseContext, dataList)
        if (dataList.getCode() == R.string.code_success) {

            dataList.getDataList().forEach {

                for (i in it.dataBegin until it.dataEnd step 3600) {

                    val cal = Calendar.getInstance()
                    cal.time = Time(i * 1000)
                    val hours = (cal.get(Calendar.HOUR_OF_DAY) * 60 * 60).toLong()

                    val day = Instant.ofEpochMilli(i * 1000).atZone(ZoneId.systemDefault()).toLocalDate()

                    try {

                        mListBookingData.forEach { (first, second) ->
                            if (first == day) {
                                second.add(hours)
                                throw Exception()
                            }
                        }

                        mListBookingData.add(Pair(day, mutableListOf()))
                        mListBookingData[mListBookingData.size - 1].second.add(hours)

                    } catch (exc: Exception) {
                    }
                }
            }

        } else {
            Toast.makeText(applicationContext, resources.getString(R.string.not_connect), Toast.LENGTH_SHORT).show()
            finish()
        }

        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            when (result.resultCode) {

                Activity.RESULT_OK -> {

                    val token: String
                    result.data?.let { intent ->
                        token = Checkout.createTokenizationResult(intent).paymentToken
                    }

                    intent.getStringExtra(getString(R.string.intent_pay_result))?.let { date ->

                        val dataSetBooking = CDataExchange()
                        dataSetBooking.setIdHouse(house.idHouse)
                        dataSetBooking.setIdUser(CSettings.getInstance().getAddressUser(baseContext))
                        dataSetBooking.setCommand("set_booking")
                        dataSetBooking.setMessage(date)
                        CClient.startExchange(baseContext, dataSetBooking)
                        if (dataSetBooking.getCode() == R.string.code_success) {

                            house.codePassword = dataSetBooking.getCodePassword()
                            val listTime = date.split(":")
                            house.timeBegin = listTime[0]
                            house.timeEnd = listTime[1]

                            mViewModelDatabase?.insert(baseContext, house)

                            finish()
                        }

                        Toast.makeText(applicationContext, resources.getString(dataSetBooking.getCode()), Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }

        btnCalendarApply.setOnClickListener {

            val resultData = getData()
            if (resultData.isNotEmpty()) {

                /** Payments */

                val listTime = resultData.split(":")
                val hour = (listTime[1].toLong() - listTime[0].toLong()) / 3600
                val price: Double = 150.0 * hour
                val title: String = house.addressHouse
                val subtitle = "Количесто часов: $hour"

                val paymentMethodTypes: HashSet<PaymentMethodType> = HashSet<PaymentMethodType>()
                paymentMethodTypes.add(PaymentMethodType.BANK_CARD)
                paymentMethodTypes.add(PaymentMethodType.SBERBANK)
                paymentMethodTypes.add(PaymentMethodType.GOOGLE_PAY)

                val paymentParameters = PaymentParameters(
                    Amount(BigDecimal(price), Currency.getInstance("RUB")),
                    title,
                    subtitle,
                    "getString(R.string.ya_token)",
                    "getString(R.string.ya_id)",
                    SavePaymentMethod.OFF,
                    paymentMethodTypes
                )

                intent.putExtra(getString(R.string.intent_pay_result), resultData)

                val intent = Checkout.createTokenizeIntent(baseContext, paymentParameters)
                mResultLauncher.launch(intent)
            }
        }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay

            val textView = view.findViewById<TextView>(R.id.txtCalendarDay)
            val roundBgView = view.findViewById<View>(R.id.viewCalendarDayRound)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == mToday || day.date.isAfter(mToday))) {
                        val date = day.date
                        if (mStartDate != null) {
                            if (date < mStartDate || mEndDate != null) {
                                mStartDate = date
                                mEndDate = null
                            } else if (date != mStartDate) {
                                mEndDate = date
                            }
                        } else {
                            mStartDate = date
                        }
                        calendarView.notifyCalendarChanged()
                        getData()
                    }
                }
            }
        }

        calendarView.dayBinder =
            object : DayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.day = day
                    val textView = container.textView
                    val roundBgView = container.roundBgView

                    textView.text = null
                    textView.background = null
                    roundBgView.visibility = View.INVISIBLE

                    val startDate = mStartDate
                    val endDate = mEndDate

                    when (day.owner) {
                        DayOwner.THIS_MONTH -> {
                            textView.text = day.day.toString()
                            if (day.date.isBefore(mToday)) {
                                textView.setTextColor(Color.parseColor("#F0F0F0")) // Gray
                            } else {

                                var bookingToday = false

                                mListBookingData.forEach {
                                    if (it.first == day.date) {

                                        textView.setTextColor(Color.parseColor("#FFFFFF"))

                                        roundBgView.visibility = View.VISIBLE
                                        roundBgView.setBackgroundResource(R.drawable.selected_booking_circle)
                                    }

                                    if (it.first == mToday)
                                        bookingToday = true
                                }

                                when {

                                    startDate == day.date && endDate == null -> {

                                        textView.setTextColor(Color.parseColor("#FFFFFF"))

                                        roundBgView.visibility = View.VISIBLE
                                        roundBgView.setBackgroundResource(R.drawable.single_selected_bg)
                                    }
                                    day.date == startDate -> {
                                        textView.setTextColor(Color.parseColor("#FFFFFF"))
                                        textView.setBackgroundResource(R.drawable.continuous_selected_bg_start)
                                    }
                                    startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                                        textView.setTextColor(Color.parseColor("#FFFFFF"))
                                        textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                                    }
                                    day.date == endDate -> {
                                        textView.setTextColor(Color.parseColor("#FFFFFF"))
                                        textView.setBackgroundResource(R.drawable.continuous_selected_bg_end)
                                    }
                                    day.date == mToday -> {
                                        if (!bookingToday) {
                                            textView.setTextColor(Color.parseColor("#000000")) // Black

                                            roundBgView.visibility = View.VISIBLE
                                            roundBgView.setBackgroundResource(R.drawable.today_bg)
                                        } else {

                                            textView.setTextColor(Color.parseColor("#FFFFFF"))

                                            roundBgView.visibility = View.VISIBLE
                                            roundBgView.setBackgroundResource(R.drawable.today_bg_booking)
                                        }
                                    }
                                    else -> textView.setTextColor(Color.parseColor("#000000")) // Black
                                }
                            }
                        }

                        DayOwner.PREVIOUS_MONTH ->
                            if (startDate != null && endDate != null && isInDateBetween(day.date, startDate, endDate)) {
                                textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                            }
                        DayOwner.NEXT_MONTH ->
                            if (startDate != null && endDate != null && isOutDateBetween(day.date, startDate, endDate)) {
                                textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                            }
                    }
                }
            }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.txtCalendarHeader)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {

            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                val arrayMonth = arrayOf(
                    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
                )

                val monthTitle: String = arrayMonth[month.month - 1] + " ${month.year}"
                container.textView.text = monthTitle
            }
        }
    }

    private fun isInDateBetween(inDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (inDate.yearMonth == startDate.yearMonth) return true
        val firstDateInThisMonth = inDate.plusMonths(1).yearMonth.atDay(1)
        return firstDateInThisMonth >= startDate && firstDateInThisMonth <= endDate && startDate != firstDateInThisMonth
    }

    private fun isOutDateBetween(outDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.minusMonths(1).yearMonth.atEndOfMonth()
        return lastDateInThisMonth >= startDate && lastDateInThisMonth <= endDate && endDate != lastDateInThisMonth
    }

    private fun getData(): String {

        var lStartDate: Long
        var lEndDate: Long

        val formatter = DateTimeFormatter.ofPattern("d.MM.yyyy")
        val zoneId: ZoneId = ZoneId.systemDefault()

        val startDate = mStartDate
        val endDate = mEndDate

        var hourBegin = (mSpCalendarHourBegin?.selectedItem as Pair<*, *>)
        var hourEnd = (mSpCalendarHourEnd?.selectedItem as Pair<*, *>)

        (mSpCalendarHourBegin?.adapter as CSpinnerAdapter).setListEnable(mutableListOf())
        (mSpCalendarHourEnd?.adapter as CSpinnerAdapter).setListEnable(mutableListOf())

        mListBookingData.forEach {

            if (it.first == startDate)
                (mSpCalendarHourBegin?.adapter as CSpinnerAdapter).setListEnable(it.second)

            if (endDate != null && it.first == endDate)
                (mSpCalendarHourEnd?.adapter as CSpinnerAdapter).setListEnable(it.second)

            if (endDate == null && it.first == startDate)
                (mSpCalendarHourEnd?.adapter as CSpinnerAdapter).setListEnable(it.second)
        }

        if (startDate != null && endDate != null) {

            lStartDate = startDate.atStartOfDay(zoneId).toEpochSecond()
            lEndDate = endDate.atStartOfDay(zoneId).toEpochSecond()

            (mSpCalendarHourBegin?.adapter as CSpinnerAdapter).clearHideValue()
            (mSpCalendarHourEnd?.adapter as CSpinnerAdapter).clearHideValue()

            val text = "${formatter.format(startDate)} ${hourBegin.second} - ${formatter.format(endDate)} ${hourEnd.second}"
            mTxtCalendarSelected?.text = text
        } else if (startDate != null && endDate == null) {

            lStartDate = startDate.atStartOfDay(zoneId).toEpochSecond()
            lEndDate = lStartDate

            (mSpCalendarHourBegin?.adapter as CSpinnerAdapter).setHideEnd(hourEnd.first as Long)
            (mSpCalendarHourEnd?.adapter as CSpinnerAdapter).setHideStart(hourBegin.first as Long)

            mSpCalendarHourBegin?.let { spBegin ->
                mSpCalendarHourEnd?.let { spEnd ->

                    val posEnd = spEnd.selectedItemPosition
                    val posBegin = spBegin.selectedItemPosition

                    if (posBegin == spBegin.adapter.count - 1) {
                        mSpCalendarHourBegin?.setSelection(posBegin - 1)
                        hourBegin = (mSpCalendarHourBegin?.selectedItem as Pair<*, *>)
                    } else if (posEnd <= posBegin) {
                        mSpCalendarHourEnd?.setSelection(posBegin + 1)
                        hourEnd = (mSpCalendarHourEnd?.selectedItem as Pair<*, *>)
                    }
                }
            }

            val text = "${formatter.format(startDate)} ${hourBegin.second} - ${formatter.format(startDate)} ${hourEnd.second}"
            mTxtCalendarSelected?.text = text
        } else
            return ""

        lStartDate += hourBegin.first as Long
        lEndDate += hourEnd.first as Long

        return "${lStartDate}:${lEndDate}"
    }
}
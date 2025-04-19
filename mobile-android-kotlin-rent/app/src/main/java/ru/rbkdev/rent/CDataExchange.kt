package ru.rbkdev.rent

import ru.rbkdev.rent.room.database.keys.CKeysTable

import androidx.lifecycle.MutableLiveData

/***/
open class CDataExchange {

    private var mCode: Int = R.string.code_error
    private var mAddress: String = ""
    private var mIdHouse: String = ""
    private var mIdUser: String = ""
    private var mMessage: String = ""
    private var mStatus: String = ""
    private var mCommand: String = ""
    private var mCodePassword: String = ""

    /***/
    fun getCode(): Int {
        return mCode
    }

    /***/
    fun setCode(code: Int) {
        mCode = code
    }

    /***/
    fun getAddress(): String {
        return mAddress
    }

    /***/
    fun setAddress(address: String) {
        mAddress = address
    }

    /***/
    fun setIdHouse(id: String) {
        mIdHouse = id
    }

    /***/
    fun getIdHouse(): String = mIdHouse

    /***/
    fun setIdUser(id: String) {
        mIdUser = id
    }

    /***/
    fun getIdUser(): String = mIdUser

    /***/
    fun getMessage(): String {
        return mMessage
    }

    /***/
    fun setMessage(message: String) {
        mMessage = message
    }

    /***/
    fun getCommand(): String {
        return mCommand
    }

    /***/
    fun setCommand(command: String) {
        mCommand = command
    }

    /***/
    fun getCodePassword(): String {
        return mCodePassword
    }

    /***/
    fun setCodePassword(codePassword: String) {
        mCodePassword = codePassword
    }

    /***/
    fun setStatus(status: String) {
        mStatus = status
    }

    /***/
    fun getStatus(): String {
        return mStatus
    }

    /***/
    open fun getList(): MutableList<CKeysTable>? {
        return null
    }

    /***/
    open fun getDataList(): MutableList<CDataItem>? {
        return null
    }
}

/***/
class CListExchange : CDataExchange() {

    private val mListLiveData = MutableLiveData<MutableList<CKeysTable>>()

    init {
        mListLiveData.value = mutableListOf()
    }

    /***/
    fun getLiveData(): MutableLiveData<MutableList<CKeysTable>> {
        return mListLiveData
    }

    /***/
    override fun getList(): MutableList<CKeysTable>? {
        return mListLiveData.value
    }
}

/***/
data class CDataItem(
    /***/
    var dataBegin: Long = 0,

    /***/
    var dataEnd: Long = 0
)

/***/
class CDataListExchange : CDataExchange() {

    private val mList = mutableListOf<CDataItem>()

    /***/
    override fun getDataList(): MutableList<CDataItem> {
        return mList
    }
}
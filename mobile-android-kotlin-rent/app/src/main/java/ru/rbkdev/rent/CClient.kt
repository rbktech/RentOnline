package ru.rbkdev.rent

import ru.rbkdev.rent.room.database.keys.CKeysTable

import android.os.Looper
import android.os.Handler
import android.widget.Toast
import android.util.JsonToken
import android.util.JsonReader
import android.util.JsonWriter
import android.content.Context

import java.io.*
import java.net.Socket
import java.net.InetSocketAddress

/***/
object CClient {

    /***/
    fun startExchange(context: Context, data: CDataExchange) {
        Thread { dataExchange(context, data) }.also { it.start(); it.join() }
    }

    /***/
    private fun dataExchange(context: Context, dataExchange: CDataExchange) {

        val socket = Socket()
        val ip = CSettings.getInstance().getIp(context)
        val port = CSettings.getInstance().getPortInt(context)
        val timeout = CSettings.getInstance().getTimeout(context)

        try {
            socket.soTimeout = timeout
            socket.connect(InetSocketAddress(ip, port), timeout)

            val message = collectMessage(context, dataExchange)
            val size = message.size()

            val stream = socket.getInputStream()
            val dos = DataOutputStream(socket.getOutputStream())
            dos.writeInt(size)
            dos.write(message.toByteArray(), 0, size)
            dos.flush()

            val data = ByteArray(DEFAULT_BUFFER_SIZE)
            val count = stream.read(data)
            if (count != -1)
                parseMessage(context, data, dataExchange)

        } catch (e: IOException) {
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }, 0)
        } finally {
            socket.close()
        }
    }

    /***/
    fun collectMessage(context: Context, data: CDataExchange): ByteArrayOutputStream {

        val outStream = ByteArrayOutputStream()
        val jsonWriter = JsonWriter(OutputStreamWriter(outStream, "UTF-8"))

        jsonWriter.beginArray()

        jsonWriter.beginObject()

        if (data.getCommand().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_command)).value(data.getCommand())

        if (data.getStatus().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_status)).value(data.getStatus())

        if (data.getAddress().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_address_house)).value(data.getAddress())

        if (data.getCodePassword().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_code_password)).value(data.getCodePassword())

        if (data.getIdUser().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_id_user)).value(data.getIdUser())

        if (data.getIdHouse().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_id_house)).value(data.getIdHouse())

        if (data.getMessage().isNotEmpty())
            jsonWriter.name(context.getString(R.string.exchange_message)).value(data.getMessage())

        jsonWriter.endObject()

        jsonWriter.endArray()

        jsonWriter.close()

        return outStream
    }

    private fun parseMessage(context: Context, array: ByteArray, dataExchange: CDataExchange) {

        val jsonReader = JsonReader(InputStreamReader(ByteArrayInputStream(array)))

        jsonReader.beginArray()
        jsonReader.beginObject()

        while (jsonReader.hasNext()) {

            when (jsonReader.nextName()) {

                context.getString(R.string.exchange_code_password) -> {
                    dataExchange.setCodePassword(jsonReader.nextString())
                }

                context.getString(R.string.exchange_message) -> {

                    val type = jsonReader.peek()
                    if (type != JsonToken.NULL) {
                        when (type) {

                            JsonToken.BEGIN_ARRAY -> {

                                if (dataExchange.getCommand() == "get_lock_list") {

                                    dataExchange.getList()?.clear()

                                    jsonReader.beginArray()

                                    while (jsonReader.hasNext()) {

                                        val table = CKeysTable()

                                        jsonReader.beginObject()

                                        while (jsonReader.hasNext()) {

                                            when (jsonReader.nextName()) {

                                                "id_house" -> table.idHouse = jsonReader.nextString()
                                                "address_house" -> table.addressHouse = jsonReader.nextString()
                                                else -> jsonReader.skipValue()
                                            }
                                        }

                                        jsonReader.endObject()

                                        dataExchange.getList()?.add(table)
                                    }

                                    jsonReader.endArray()
                                }

                                // --------------

                                if (dataExchange.getCommand() == "get_user_date") {

                                    dataExchange.getDataList()?.clear()

                                    jsonReader.beginArray()

                                    while (jsonReader.hasNext()) {

                                        val item = CDataItem()

                                        jsonReader.beginObject()

                                        while (jsonReader.hasNext()) {

                                            when (jsonReader.nextName()) {

                                                "data_begin" -> item.dataBegin = jsonReader.nextLong()
                                                "data_end" -> item.dataEnd = jsonReader.nextLong()
                                                else -> jsonReader.skipValue()
                                            }
                                        }

                                        jsonReader.endObject()

                                        dataExchange.getDataList()?.add(item)
                                    }

                                    jsonReader.endArray()
                                }
                            }

                            else -> jsonReader.skipValue()
                        }
                    } else
                        jsonReader.skipValue()
                }

                context.getString(R.string.exchange_status) -> {
                    val result = jsonReader.nextString()
                    if (result.equals("success"))
                        dataExchange.setCode(R.string.code_success)
                    else
                        dataExchange.setCode(R.string.code_error)
                }

                context.getString(R.string.exchange_command) -> {
                    if (dataExchange.getCommand() != jsonReader.nextString())
                        dataExchange.setCode(R.string.code_error)
                }

                else -> jsonReader.skipValue()

            }
        }

        jsonReader.endObject()
        jsonReader.endArray()
        jsonReader.close()
    }
}

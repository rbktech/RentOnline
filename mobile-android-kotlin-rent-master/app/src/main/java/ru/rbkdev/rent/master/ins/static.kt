package ru.rbkdev.rent.master.ins

import android.os.Looper
import android.os.Handler

/***/
fun sendToOriginalThread(func: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        func()
    }, 0)
}
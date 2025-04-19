package ru.rbkdev.rent.tools

import android.os.Looper
import android.os.Handler

/***/
fun sendToOriginalThread(func: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        func()
    }, 0)
}
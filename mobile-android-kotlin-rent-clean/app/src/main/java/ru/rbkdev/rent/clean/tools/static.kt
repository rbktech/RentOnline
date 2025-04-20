package ru.rbkdev.rent.clean.tools

import android.os.Looper
import android.os.Handler

/***/
fun sendToOriginalThread(func: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        func()
    }, 0)
}
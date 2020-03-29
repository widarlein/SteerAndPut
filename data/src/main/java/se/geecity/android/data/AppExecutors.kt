package se.geecity.android.data

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(val worker: Executor,
                   val mainThread: Executor) {
    constructor() : this(Executors.newFixedThreadPool(3),
            MainThreadExecutor())

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
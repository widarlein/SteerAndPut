package se.geecity.android.data

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(val worker: Executor) {
    constructor() : this(Executors.newFixedThreadPool(3))
}
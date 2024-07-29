package com.nht.nhtcamera.utils

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulerConfigurationImpl : SchedulerConfiguration {
    override fun ioScheduler(): Scheduler {
        return Schedulers.io()
    }

    override fun mainScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }
}
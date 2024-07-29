package com.nht.nhtcamera.utils

import io.reactivex.Scheduler

interface SchedulerConfiguration {
    fun ioScheduler(): Scheduler
    fun mainScheduler(): Scheduler
}
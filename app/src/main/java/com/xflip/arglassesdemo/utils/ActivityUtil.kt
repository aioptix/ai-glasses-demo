package com.xflip.arglassesdemo.utils

import android.app.Activity

object ActivityUtil {

    private val activities: MutableList<Activity> = ArrayList()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun getActivities(): List<Activity> {
        return activities
    }

    fun finishAll(vararg exceptClassName: String?) {
        val names = mutableListOf(*exceptClassName)
        for (activity in activities) {
            if (!activity.isFinishing && !names.contains(activity.javaClass.name)) {
                activity.finish()
            }
        }
    }

    fun finish(vararg exceptClassName: String?) {
        val names = mutableListOf(*exceptClassName)
        for (activity in activities) {
            if (!activity.isFinishing && names.contains(activity.javaClass.name)) {
                activity.finish()
            }
        }
    }

}
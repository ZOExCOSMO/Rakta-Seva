package com.raktaseva.connect.utils

import kotlin.math.*

object LocationUtils {
    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun estimatedEtaMinutes(distKm: Double, avgSpeedKmh: Double = 30.0): Int {
        return ((distKm / avgSpeedKmh) * 60).toInt().coerceAtLeast(1)
    }
}
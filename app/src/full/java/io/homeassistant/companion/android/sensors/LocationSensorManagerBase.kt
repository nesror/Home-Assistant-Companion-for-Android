package io.homeassistant.companion.android.sensors

import android.content.BroadcastReceiver
import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.sensors.SensorManager
import javax.inject.Inject

abstract class LocationSensorManagerBase : BroadcastReceiver(), SensorManager {
    @Inject
    lateinit var serverManager: ServerManager
}
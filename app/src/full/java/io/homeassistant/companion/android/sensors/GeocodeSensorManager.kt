package io.homeassistant.companion.android.sensors

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.database.AppDatabase
import io.homeassistant.companion.android.database.sensor.Setting
import io.homeassistant.companion.android.location.HighAccuracyLocationService
import java.lang.Exception

class GeocodeSensorManager : SensorManager {

    companion object {
        const val SETTING_ACCURACY = "Minimum Accuracy"
        const val DEFAULT_MINIMUM_ACCURACY = 200
        private const val TAG = "GeocodeSM"
        val geocodedLocation = SensorManager.BasicSensor(
            "geocoded_location",
            "sensor",
            R.string.basic_sensor_name_geolocation,
            R.string.sensor_description_geocoded_location
        )
    }

    override val enabledByDefault: Boolean
        get() = false
    override val name: Int
        get() = R.string.sensor_name_geolocation
    override val availableSensors: List<SensorManager.BasicSensor>
        get() = listOf(geocodedLocation)

    override fun requiredPermissions(sensorId: String): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun requestSensorUpdate(
        context: Context
    ) {
        updateGeocodedLocation(context)
    }

    private fun updateGeocodedLocation(context: Context) {
        if (!isEnabled(context, geocodedLocation.id) || !checkPermission(
                context,
                geocodedLocation.id
            )
        )
            return
//        val locApi = LocationServices.getFusedLocationProviderClient(context)
//        locApi.lastLocation.addOnSuccessListener { location ->
//            addressUpdata(context)
//        }
    }

    private fun addressUpdata(context: Context, location: Location) {
        var address: Address? = null
        try {
            if (location == null) {
                Log.e(TAG, "Somehow location is null even though it was successful")
                return
            }

            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val sensorSettings = sensorDao.getSettings(geocodedLocation.id)
            val minAccuracy = sensorSettings
                .firstOrNull { it.name == SETTING_ACCURACY }?.value?.toIntOrNull()
                ?: DEFAULT_MINIMUM_ACCURACY
            sensorDao.add(
                Setting(
                    geocodedLocation.id,
                    SETTING_ACCURACY,
                    minAccuracy.toString(),
                    "number"
                )
            )

            if (location.accuracy <= minAccuracy)
                address = Geocoder(context)
                    .getFromLocation(location.latitude, location.longitude, 1)
                    .firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get geocoded location", e)
        }
        val attributes = address?.let {
            mapOf(
                "Administrative Area" to it.adminArea,
                "Country" to it.countryName,
                "ISO Country Code" to it.countryCode,
                "Locality" to it.locality,
                "Latitude" to it.latitude,
                "Longitude" to it.longitude,
                "Postal Code" to it.postalCode,
                "Sub Administrative Area" to it.subAdminArea,
                "Sub Locality" to it.subLocality,
                "Sub Thoroughfare" to it.subThoroughfare,
                "Thoroughfare" to it.thoroughfare
            )
        }.orEmpty()

        val prettyAddress = address?.getAddressLine(0) ?: "Unknown"

        HighAccuracyLocationService.updateNotificationAddress(context, location, prettyAddress)

        onSensorUpdated(
            context,
            geocodedLocation,
            prettyAddress,
            "mdi:map",
            attributes
        )
    }
}

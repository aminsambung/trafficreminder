package com.example.trafficreminder.data.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>?,
    @SerializedName("status") val status: String?
)

data class Route(
    @SerializedName("legs") val legs: List<Leg>?,
    @SerializedName("summary") val summary: String?
)

data class Leg(
    @SerializedName("duration") val duration: DurationInfo?,
    @SerializedName("duration_in_traffic") val durationInTraffic: DurationInfo?,
    @SerializedName("distance") val distance: DistanceInfo?,
    @SerializedName("start_address") val startAddress: String?,
    @SerializedName("end_address") val endAddress: String?
)

data class DurationInfo(
    @SerializedName("value") val value: Int,
    @SerializedName("text") val text: String
)

data class DistanceInfo(
    @SerializedName("value") val value: Int,
    @SerializedName("text") val text: String
)

package com.outdu.camconnect.ui.models

import com.google.android.gms.maps.GoogleMap

enum class MapType(val googleMapType: Int) {
    NORMAL(GoogleMap.MAP_TYPE_NORMAL),
    SATELLITE(GoogleMap.MAP_TYPE_SATELLITE),
    TERRAIN(GoogleMap.MAP_TYPE_TERRAIN)
}
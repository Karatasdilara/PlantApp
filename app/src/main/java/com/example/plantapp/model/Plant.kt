package com.example.plantapp.model

import android.os.Parcelable

data class Plant(
    var plantName: String = "",
    var imageUrl: String = "",
    var documentId: String = "",
    var selectedDays: List<String> = emptyList() //günlerin listesi

)

package com.example.parkingapp.DataModels
//Data class  for carpark details
data class CarParks(
    var carparkID: String = "",
    var carparkName: String = "",
    var standardBays: Int? = null,  // Now nullable
    var disabledBays: Int? = null,
    var carparkAddress: String = "",
    var carparkPrices: HashMap<String, String> = hashMapOf(),
    var entranceImageUrl: String = "",
    var entranceDescription: String = "",
    var restrictions: String = "",
    var carparkOpeningTimes: HashMap<String, String> = hashMapOf(),
    var reviews: Map<String, Review> = mapOf(),
    var paymentMethods: String = ""
)





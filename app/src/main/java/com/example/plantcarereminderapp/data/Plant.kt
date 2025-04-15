data class Plant(
    val name: String,
    val wateringFrequency: Int,
    val daysUntilWatering: Int,
    val fertilizingFrequency: Int?,
    val daysUntilFertilizing: Int?,
    val repottingFrequency: Int?,
    val daysUntilRepotting: Int?,
    val notificationsEnabled: Boolean
)

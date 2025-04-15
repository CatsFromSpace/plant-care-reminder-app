package com.example.plantcarereminderapp

import Plant
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plantcarereminderapp.ui.theme.PlantCareReminderAppTheme
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantCareReminderAppTheme {
                HomeScreen()
            }
        }
    }
}

// Main screen composable that displays the list of plants and handles adding new plants
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var plantToEdit by remember { mutableStateOf<Plant?>(null) }
    var plants by remember { mutableStateOf(listOf<Plant>()) }

    // Sort plants by days until next watering
    val sortedPlants = remember(plants) {
        plants.sortedBy { it.daysUntilWatering }
    }

    // Function to update an existing plant
    fun updatePlant(oldPlant: Plant, newPlant: Plant) {
        plants = plants.map { if (it == oldPlant) newPlant else it }
    }

    // Function to update a plant's notification settings
    fun updatePlantNotifications(plant: Plant, enabled: Boolean) {
        plants = plants.map { 
            if (it == plant) it.copy(notificationsEnabled = enabled) else it 
        }
    }

    // Function to reset care countdown for a plant
    fun resetCareCycle(plant: Plant, careType: String) {
        plants = plants.map { 
            if (it == plant) {
                when (careType) {
                    "water" -> it.copy(daysUntilWatering = it.wateringFrequency)
                    "fertilize" -> it.copy(daysUntilFertilizing = it.fertilizingFrequency)
                    "repot" -> it.copy(daysUntilRepotting = it.repottingFrequency)
                    else -> it
                }
            } else it 
        }
    }

    Scaffold(
        // Top app bar with the app title
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Plant Care App",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // Floating action button for adding new plants
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPlantDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a plant")
            }
        }
    ) { padding ->
        // Main content showing the list of plants
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedPlants) { plant ->
                    PlantCard(
                        plant = plant,
                        onNotificationToggled = { enabled ->
                            updatePlantNotifications(plant, enabled)
                        },
                        onEditClicked = {
                            plantToEdit = plant
                        },
                        onCareActionCompleted = { careType ->
                            resetCareCycle(plant, careType)
                        }
                    )
                }
            }
        }

        // Show add/edit plant dialog
        if (showAddPlantDialog || plantToEdit != null) {
            AddEditPlantDialog(
                plantToEdit = plantToEdit,
                onDismiss = { 
                    showAddPlantDialog = false
                    plantToEdit = null
                },
                onPlantSaved = { plant ->
                    if (plantToEdit != null) {
                        updatePlant(plantToEdit!!, plant)
                    } else {
                        plants = plants + plant
                    }
                    showAddPlantDialog = false
                    plantToEdit = null
                }
            )
        }
    }
}

// Card composable for displaying individual plant information
@Composable
fun PlantCard(
    plant: Plant,
    onNotificationToggled: (Boolean) -> Unit,
    onEditClicked: () -> Unit,
    onCareActionCompleted: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onEditClicked) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit plant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Watering info with reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Watering frequency: Every ${plant.wateringFrequency} days")
                    Text(
                        text = when (plant.daysUntilWatering) {
                            0 -> "Water today!"
                            1 -> "Water tomorrow"
                            else -> "Water in ${plant.daysUntilWatering} days"
                        },
                        color = if (plant.daysUntilWatering <= 1) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                if (plant.daysUntilWatering == 0) {
                    TextButton(onClick = { onCareActionCompleted("water") }) {
                        Text("Done")
                    }
                }
            }

            // Fertilizing info with reset button (if scheduled)
            plant.fertilizingFrequency?.let { frequency ->
                plant.daysUntilFertilizing?.let { daysUntil ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Fertilizing frequency: Every $frequency days")
                            Text(
                                text = when (daysUntil) {
                                    0 -> "Fertilize today!"
                                    1 -> "Fertilize tomorrow"
                                    else -> "Fertilize in $daysUntil days"
                                },
                                color = if (daysUntil <= 1) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (daysUntil == 0) {
                            TextButton(onClick = { onCareActionCompleted("fertilize") }) {
                                Text("Done")
                            }
                        }
                    }
                }
            }

            // Repotting info with reset button (if scheduled)
            plant.repottingFrequency?.let { frequency ->
                plant.daysUntilRepotting?.let { daysUntil ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Repotting frequency: Every $frequency days")
                            Text(
                                text = when (daysUntil) {
                                    0 -> "Repot today!"
                                    1 -> "Repot tomorrow"
                                    else -> "Repot in $daysUntil days"
                                },
                                color = if (daysUntil <= 1) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (daysUntil == 0) {
                            TextButton(onClick = { onCareActionCompleted("repot") }) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
            
            // Notifications toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications")
                Switch(
                    checked = plant.notificationsEnabled,
                    onCheckedChange = onNotificationToggled
                )
            }
        }
    }
}

// Dialog for adding/editing plants
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantDialog(
    plantToEdit: Plant? = null,
    onDismiss: () -> Unit,
    onPlantSaved: (Plant) -> Unit
) {
    var plantName by remember { mutableStateOf(plantToEdit?.name ?: "") }
    var wateringFrequency by remember { mutableStateOf(plantToEdit?.wateringFrequency?.toString() ?: "") }
    var fertilizingFrequency by remember { mutableStateOf(plantToEdit?.fertilizingFrequency?.toString() ?: "") }
    var repottingFrequency by remember { mutableStateOf(plantToEdit?.repottingFrequency?.toString() ?: "") }
    var notificationsEnabled by remember { mutableStateOf(plantToEdit?.notificationsEnabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (plantToEdit == null) "Add a Plant" else "Edit Plant") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = plantName,
                    onValueChange = { plantName = it },
                    label = { Text("Plant Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                TextField(
                    value = wateringFrequency,
                    onValueChange = { wateringFrequency = it.filter { char -> char.isDigit() } },
                    label = { Text("Watering Frequency (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                TextField(
                    value = fertilizingFrequency,
                    onValueChange = { fertilizingFrequency = it.filter { char -> char.isDigit() } },
                    label = { Text("Fertilizing Frequency (days, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                TextField(
                    value = repottingFrequency,
                    onValueChange = { repottingFrequency = it.filter { char -> char.isDigit() } },
                    label = { Text("Repotting Frequency (days, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (plantName.isNotBlank() && wateringFrequency.isNotBlank()) {
                        onPlantSaved(
                            Plant(
                                name = plantName,
                                wateringFrequency = wateringFrequency.toInt(),
                                daysUntilWatering = wateringFrequency.toInt(),
                                fertilizingFrequency = fertilizingFrequency.takeIf { it.isNotBlank() }?.toInt(),
                                daysUntilFertilizing = fertilizingFrequency.takeIf { it.isNotBlank() }?.toInt(),
                                repottingFrequency = repottingFrequency.takeIf { it.isNotBlank() }?.toInt(),
                                daysUntilRepotting = repottingFrequency.takeIf { it.isNotBlank() }?.toInt(),
                                notificationsEnabled = notificationsEnabled
                            )
                        )
                    }
                }
            ) {
                Text(if (plantToEdit == null) "Add Plant" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
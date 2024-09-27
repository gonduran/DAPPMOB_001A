package com.example.alarmavisual.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LocationScreen(navController: NavHostController) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Buscando ubicación...") }
    var latLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val mapView = rememberMapViewWithLifecycle()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermissionGranted = remember { mutableStateOf(false) }

    // Obtener usuario autenticado
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: ""

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                locationPermissionGranted.value = true
                updateLocation(fusedLocationClient, context, userId, onLocationReceived = { loc, text ->
                    latLng = loc
                    locationText = text
                })
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted.value = true
            updateLocation(fusedLocationClient, context, userId, onLocationReceived = { loc, text ->
                latLng = loc
                locationText = text
            })
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Mostrar el mapa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
        ) {
            if (locationPermissionGranted.value) {
                AndroidView({ mapView }) { mapView ->
                    mapView.getMapAsync { googleMap ->
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Tu ubicación"))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Mostrar la ubicación en texto
        Text(
            text = locationText,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Agregar botones para "Actualizar ubicación" y "Volver"
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                // Acción para volver a buscar la ubicación
                if (locationPermissionGranted.value) {
                    updateLocation(fusedLocationClient, context, userId, onLocationReceived = { loc, text ->
                        latLng = loc
                        locationText = text
                    })
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Actualizar ubicación")
            }

            // Botón para volver
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Volver")
            }
        }
    }
}

// Función para actualizar la ubicación y guardarla en Firebase
@SuppressLint("MissingPermission")
fun updateLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: Context,
    userId: String,
    onLocationReceived: (LatLng, String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            val locationText = getAddressFromLatLng(context, location.latitude, location.longitude)

            // Guardar en Firebase
            saveLocationToFirestore(userId, latLng, locationText)

            onLocationReceived(latLng, locationText)
        } else {
            Toast.makeText(context, "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show()
        }
    }
}

// Función para guardar la ubicación en Firestore
fun saveLocationToFirestore(userId: String, latLng: LatLng, locationText: String) {
    val firestore = FirebaseFirestore.getInstance()
    val locationData = hashMapOf(
        "latitude" to latLng.latitude,
        "longitude" to latLng.longitude,
        "address" to locationText
    )

    firestore.collection("users").document(userId).collection("locations")
        .add(locationData)
        .addOnSuccessListener {
            println("Ubicación guardada con éxito.")
        }
        .addOnFailureListener { e ->
            println("Error al guardar la ubicación: ${e.message}")
        }
}

// Función para obtener la dirección desde las coordenadas
fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
    return if (!addresses.isNullOrEmpty()) {
        val address = addresses[0]
        "${address.getAddressLine(0)}, ${address.locality}"
    } else {
        "Dirección no disponible"
    }
}

// Recordar MapView y manejar ciclo de vida
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver {
    return remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    // Aquí simulamos una ubicación estática para mostrar en el Preview
    val fakeLatLng = LatLng(-34.0, 151.0)  // Coordenadas simuladas
    val fakeLocationText = "Simulación: Sydney, Australia"

    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF77A8AF)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Mostrar el mapa (simulación en Preview)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
        ) {
            Text(
                text = "Mapa no disponible en el Preview",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Mostrar la ubicación simulada en texto
        Text(
            text = fakeLocationText,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Agregar botones para "Actualizar ubicación" y "Volver"
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* Acción simulada de actualizar */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Actualizar ubicación")
            }

            // Botón para cancelar
            Button(
                onClick = { /* Acción simulada de volver */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Volver")
            }
        }
    }
}
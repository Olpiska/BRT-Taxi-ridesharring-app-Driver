package com.example.brtaxidriver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.brtaxidriver.data.model.RideResponse
import com.example.brtaxidriver.ui.LoginStep
import com.example.brtaxidriver.ui.MainViewModel
import com.example.brtaxidriver.ui.theme.BRTaxiDriverTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BRTaxiDriverTheme {
                val loginStep by viewModel.loginStep.collectAsState()
                val nationality by viewModel.nationality.collectAsState()
                var currentScreen by remember { mutableStateOf("main") }

                if (loginStep == LoginStep.LOGGED_IN) {
                    if (currentScreen == "account") {
                        AccountScreen(viewModel) { currentScreen = "main" }
                    } else {
                        DriverAppScreen(viewModel) { currentScreen = "account" }
                    }
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        when (loginStep) {
                            LoginStep.PHONE -> PhoneLoginScreen(viewModel)
                            LoginStep.OTP -> OtpVerificationScreen(viewModel)
                            LoginStep.REGISTER -> RegisterScreen(viewModel)
                            LoginStep.DOCUMENTS -> DocumentsScreen(viewModel, nationality)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val stats by viewModel.accountStats.collectAsState()
    val history by viewModel.rideHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Performance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Trips", fontSize = 12.sp, color = Color.Gray)
                            Text("${stats?.total_trips ?: 0}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column {
                            Text("Total Distance", fontSize = 12.sp, color = Color.Gray)
                            Text(String.format(Locale.US, "%.1f km", stats?.total_distance ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Earnings", fontSize = 12.sp, color = Color.Gray)
                    Text(String.format(Locale.US, "%.2f BRToken", stats?.total_earnings ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4CAF50))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Ride History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (history.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No ride history found.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history) { ride ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Trip #${ride.id}", fontWeight = FontWeight.Bold)
                                    Text(ride.createdAt, fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("+${ride.fare} BRToken", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneLoginScreen(viewModel: MainViewModel) {
    var phone by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("BR Taxi Driver", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.setPhoneNumber(phone); viewModel.sendOtp() }, modifier = Modifier.fillMaxWidth()) { Text("Send Code") }
    }
}

@Composable
fun OtpVerificationScreen(viewModel: MainViewModel) {
    var otp by remember { mutableStateOf("") }
    val phone by viewModel.phoneNumber.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Verification", style = MaterialTheme.typography.headlineLarge)
        Text("Enter the code sent to $phone")
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = otp, onValueChange = { otp = it }, label = { Text("SMS Code") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.verifyOtp(otp) }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
    }
}

@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val genderOptions = listOf("Male", "Female", "Other")
    var selectedGender by remember { mutableStateOf(genderOptions[0]) }
    val nationalities = listOf("Poland", "Turkey", "Ukraine", "Other")
    var selectedNationality by remember { mutableStateOf(nationalities[0]) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nationality:", modifier = Modifier.align(Alignment.Start))
        nationalities.forEach { text ->
            Row(Modifier.fillMaxWidth().selectable(selected = (text == selectedNationality), onClick = { selectedNationality = text }, role = Role.RadioButton).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (text == selectedNationality), onClick = null)
                Text(text = text, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Gender:", modifier = Modifier.align(Alignment.Start))
        genderOptions.forEach { text ->
            Row(Modifier.fillMaxWidth().selectable(selected = (text == selectedGender), onClick = { selectedGender = text }, role = Role.RadioButton).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (text == selectedGender), onClick = null)
                Text(text = text, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.registerDriver(firstName, lastName, selectedGender, selectedNationality) }, modifier = Modifier.fillMaxWidth()) { Text("Continue to Documents") }
    }
}

@Composable
fun DocumentsScreen(viewModel: MainViewModel, nationality: String) {
    var hasId by remember { mutableStateOf(false) }
    var hasMed by remember { mutableStateOf(false) }
    var hasPsy by remember { mutableStateOf(false) }
    var hasCrimPl by remember { mutableStateOf(false) }
    var hasCrimOt by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Text("Upload Documents", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        DocumentItem("ID/Passport/Residence Permit", hasId) { hasId = it }
        DocumentItem("Medical Check", hasMed) { hasMed = it }
        DocumentItem("Psychotechnical", hasPsy) { hasPsy = it }
        DocumentItem("Criminal Record (Poland)", hasCrimPl) { hasCrimPl = it }
        if (nationality != "Poland") {
            DocumentItem("Criminal Record ($nationality)", hasCrimOt) { hasCrimOt = it }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.uploadDocuments(hasId, hasMed, hasPsy, hasCrimPl, hasCrimOt) },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasId && hasMed && hasPsy && hasCrimPl && (nationality == "Poland" || hasCrimOt)
        ) { Text("Finish & Go to App") }
    }
}

@Composable
fun DocumentItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun DriverAppScreen(viewModel: MainViewModel, onOpenAccount: () -> Unit) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()
    val availableRides by viewModel.availableRides.collectAsState()
    val currentRide by viewModel.currentRide.collectAsState()
    
    val earnings by viewModel.earnings.collectAsState()
    val score by viewModel.driverScore.collectAsState()
    val rate by viewModel.acceptanceRate.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> hasLocationPermission = isGranted }

    LaunchedEffect(Unit) { if (!hasLocationPermission) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(isOnline, hasLocationPermission) {
        if (isOnline && hasLocationPermission) {
            while (true) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let { viewModel.updateLocation(it.latitude, it.longitude) }
                    }
                } catch (e: SecurityException) { e.printStackTrace() }
                kotlinx.coroutines.delay(10000)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val wroclaw = LatLng(51.1079, 17.0385)
        val cameraState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(wroclaw, 12f) }

        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraState, properties = MapProperties(isMyLocationEnabled = hasLocationPermission))

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)) {
            FloatingActionButton(onClick = onOpenAccount, containerColor = Color.White, contentColor = Color.Black, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Account")
            }
        }

        if (isOnline) {
            Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { viewModel.toggleOnline() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp).width(160.dp)
                ) {
                    Text("GO OFFLINE", fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(16.dp)) {
            if (!isOnline) {
                Button(
                    onClick = { viewModel.toggleOnline() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("GO ONLINE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("Earnings", String.format(Locale.US, "%.2f BRToken", earnings))
                StatBox("Score", String.format(Locale.US, "%.1f", score), isScore = true)
                StatBox("Acceptance", "$rate%")
            }

            if (isOnline && currentRide == null) {
                Spacer(modifier = Modifier.height(16.dp))
                AvailableRidesList(rides = availableRides, onAccept = { viewModel.acceptRide(it) })
            }
        }

        currentRide?.let { ride ->
            ActiveRideInfo(ride = ride, onUpdateStatus = { viewModel.updateRideStatus(it) }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}

@Composable
fun StatBox(label: String, value: String, isScore: Boolean = false) {
    Card(modifier = Modifier.width(100.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (isScore) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun AvailableRidesList(rides: List<RideResponse>, onAccept: (RideResponse) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
        Text("Requests Near You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (rides.isEmpty()) {
            Text("Searching for rides...", fontSize = 12.sp, color = Color.Gray)
        } else {
            LazyColumn {
                items(rides) { ride ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Passenger #${ride.userId}", fontSize = 14.sp)
                            Text("Fare: ${ride.fare} BRToken", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Button(onClick = { onAccept(ride) }, shape = RoundedCornerShape(8.dp)) { Text("Accept") }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveRideInfo(ride: RideResponse, onUpdateStatus: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Trip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(String.format(Locale.US, "Pickup: %.4f, %.4f", ride.pickupLat, ride.pickupLng))
            Text(String.format(Locale.US, "Destination: %.4f, %.4f", ride.dropoffLat, ride.dropoffLng))
            Text("Total: ${ride.fare} BRToken", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (ride.status == "accepted") {
                Text("Verification", fontWeight = FontWeight.Bold)
                Text("Ask for the last 4 digits of the passenger's phone:", fontSize = 12.sp)
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 4) otpInput = it },
                    label = { Text("Last 4 digits") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Button(
                    onClick = {
                        val lastFour = ride.passengerPhone?.takeLast(4) ?: ""
                        if (otpInput == lastFour) {
                            onUpdateStatus("started")
                        } else {
                            Toast.makeText(context, "Invalid Verification Code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Verify & Start Trip")
                }
            } else if (ride.status == "started") {
                Button(
                    onClick = { onUpdateStatus("completed") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Trip")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onUpdateStatus("cancelled") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Trip")
            }
        }
    }
}

package com.example.cuentas_clarasapp.screens.expense

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.cuentas_clarasapp.screens.home.HomeUiState
import com.example.cuentas_clarasapp.screens.home.HomeViewModel
import kotlinx.coroutines.launch
import java.io.File

private val Purple    = Color(0xFF985EFF)
private val BgDark    = Color(0xFF111013)
private val BgCard    = Color(0xFF1A1820)
private val TextMuted = Color(0x59FFFFFF)
private val TextDim   = Color(0x33FFFFFF)

data class CategoriaItem(
    val id: String,
    val nombre: String,
    val icono: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    homeViewModel: HomeViewModel, //  Monitorea el saldo del Home
    viewModel: AddExpenseViewModel //  Gestiona la inserción limpia en Room
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    // Contenedor para mostrar mensajes flotantes (Snackbars)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Leemos el balance real desde el HomeViewModel de manera reactiva
    val balanceDisponibleHome = when (homeUiState) {
        is HomeUiState.Success -> (homeUiState as HomeUiState.Success).data.saldoDisponible
        else -> 0.0
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            photoUri = pendingCameraUri
            viewModel.onFotoUriChange(pendingCameraUri.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = crearUriTemporalParaFoto(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    fun onCameraClick() {
        val permisoOtorgado = context.checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (permisoOtorgado) {
            val uri = crearUriTemporalParaFoto(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val categorias = remember {
        listOf(
            CategoriaItem("alimentacion", "Alimentación", Icons.Default.Restaurant),
            CategoriaItem("transporte",   "Transporte",   Icons.Default.DirectionsCar),
            CategoriaItem("ocio",         "Ocio",         Icons.Default.SportsEsports),
            CategoriaItem("compras",      "Compras",      Icons.Default.ShoppingBag),
            CategoriaItem("educacion",    "Educación",    Icons.Default.School),
            CategoriaItem("otros",        "Otros",        Icons.Default.MoreHoriz)
        )
    }

    // --- Validaciones de Negocio ---
    val montoIngresado = uiState.monto.toDoubleOrNull() ?: 0.0
    val tieneMontoValido = uiState.monto.isNotEmpty() && montoIngresado > 0.0
    val tieneCategoria = uiState.categoriaId != null
    val tieneSaldoSuficiente = montoIngresado <= balanceDisponibleHome

    // El botón se activa únicamente si cumple con los requisitos mínimos de guardado
    val botonHabilitado = tieneMontoValido && tieneCategoria && tieneSaldoSuficiente && !uiState.isSaving

    Scaffold(
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo gasto", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 22.dp)) {
                Button(
                    onClick = {
                        // Verificación de alertas prioritarias antes de procesar
                        if (!tieneMontoValido) {
                            scope.launch { snackbarHostState.showSnackbar("Debes insertar una cantidad válida antes de guardar.") }
                        } else if (!tieneCategoria) {
                            scope.launch { snackbarHostState.showSnackbar("Debes seleccionar una categoría antes de guardar el gasto.") }
                        } else if (!tieneSaldoSuficiente) {
                            scope.launch { snackbarHostState.showSnackbar("No tienes saldo suficiente en tu balance disponible para este gasto.") }
                        } else {
                            viewModel.guardarGasto(onSuccess = {
                                homeViewModel.cargarDatosFinancieros()
                                navController.popBackStack()
                            })
                        }
                    },
                    enabled = botonHabilitado,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple,
                        disabledContainerColor = BgCard,
                        contentColor = Color.White,
                        disabledContentColor = TextDim
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar gasto", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // --- Fila de foto del recibo ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .clickable { onCameraClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Purple.copy(alpha = 0.10f))
                        .border(1.5.dp, Purple.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Foto del recibo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Purple, modifier = Modifier.size(24.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (photoUri != null) "Foto agregada" else "Agregar foto del recibo",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (photoUri != null) "Toca para cambiarla" else "Opcional",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
            }

            // --- Monto ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("¿Cuánto gastaste?", color = TextMuted, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("$", color = Color.White.copy(alpha = 0.40f), fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))

                    androidx.compose.foundation.text.BasicTextField(
                        value = uiState.monto,
                        onValueChange = { nuevo ->
                            if (nuevo.isEmpty() || nuevo.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                viewModel.onMontoChange(nuevo)
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Purple),
                        modifier = Modifier.widthIn(min = 120.dp, max = 280.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.Center) {
                                if (uiState.monto.isEmpty()) {
                                    Text("0.00", color = Color.White.copy(alpha = 0.25f), fontSize = 52.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                                inner()
                            }
                        }
                    )
                }
                Box(modifier = Modifier.width(160.dp).height(2.dp).background(Purple, RoundedCornerShape(2.dp)))
            }

            // --- Categorías ---
            Column {
                Text(
                    "CATEGORÍA",
                    color = TextMuted,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val filas = categorias.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filas.forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            fila.forEach { cat ->
                                val seleccionado = uiState.categoriaId == cat.id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (seleccionado) Purple.copy(alpha = 0.12f) else BgCard)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (seleccionado) Purple else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.onCategoriaSelected(cat.id) }
                                        .padding(vertical = 14.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (seleccionado) Purple.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = cat.icono,
                                            contentDescription = cat.nombre,
                                            tint = if (seleccionado) Purple else Color.White.copy(alpha = 0.55f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = cat.nombre,
                                        color = if (seleccionado) Purple else Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.sp,
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Nota opcional ---
            Column {
                Text(
                    "NOTA (OPCIONAL)",
                    color = TextMuted,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = uiState.nota,
                    onValueChange = { viewModel.onNotaChange(it) },
                    placeholder = { Text("Ej. Almuerzo con amigos", color = Color.White.copy(alpha = 0.20f), fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                        focusedBorderColor = Purple.copy(alpha = 0.60f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        cursorColor = Purple
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            containerColor = BgCard,
            title = { Text("Permiso de cámara necesario", color = Color.White, fontSize = 16.sp) },
            text = {
                Text(
                    "Para adjuntar la foto de tu recibo necesitamos acceso a la cámara. Podés activarlo desde los ajustes de la app.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Entendido", color = Purple)
                }
            }
        )
    }
}

private fun crearUriTemporalParaFoto(context: android.content.Context): Uri {
    val archivo = File.createTempFile(
        "gasto_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}
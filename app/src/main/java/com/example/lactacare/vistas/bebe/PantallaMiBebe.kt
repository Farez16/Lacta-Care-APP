package com.example.lactacare.vistas.bebe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lactacare.datos.MockBebeRepository
import com.example.lactacare.dominio.model.Bebe
import com.example.lactacare.vistas.theme.MomPrimary
import com.example.lactacare.vistas.theme.MomAccent
import com.example.lactacare.vistas.theme.TextoOscuroClean

// --- COLORES SUAVES (Estilo Maternal/Clean) ---
val SoftPinkBg = Color(0xFFFFF5F7) // Fondo general
val BoyBlue = Color(0xFF60A5FA)    // Azul suave niño
val GirlPink = Color(0xFFF472B6)   // Rosa suave niña
val BoyBg = Color(0xFFEFF6FF)
val GirlBg = Color(0xFFFDF2F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMiBebe(
    onVolver: () -> Unit,
    // --- AGREGAMOS ESTO: El evento para ir a crear bebé ---
    onNavAnadirBebe: () -> Unit,

    // Inyección de Dependencias
    viewModel: BebeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BebeViewModel(bebeRepository = MockBebeRepository()) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SoftPinkBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis Bebés",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextoOscuroClean
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = TextoOscuroClean)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SoftPinkBg.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            // --- AQUÍ USAMOS TU CÓDIGO ---
            FloatingActionButton(
                onClick = { onNavAnadirBebe() },
                containerColor = MomPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "Añadir")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // --- MANEJO DE ESTADOS ---
            when (val estado = uiState) {
                is BebeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MomPrimary)
                    }
                }
                is BebeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${estado.mensaje}", color = Color.Red)
                    }
                }
                is BebeUiState.Success -> {
                    if (estado.bebes.isEmpty()) {
                        EmptyStateBebes()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(estado.bebes) { bebe ->
                                CardBebe(bebe)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardBebe(bebe: Bebe) {
    val isGirl = bebe.genero.equals("Femenino", ignoreCase = true)

    // Configuración visual dinámica según género
    val themeColor = if (isGirl) GirlPink else BoyBlue
    val themeBg = if (isGirl) GirlBg else BoyBg
    val avatarPlaceholder = if (isGirl)
        "https://cdn-icons-png.flaticon.com/512/4086/4086577.png"
    else
        "https://cdn-icons-png.flaticon.com/512/4086/4086632.png"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(themeBg)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(avatarPlaceholder),
                    contentDescription = "Foto Bebé",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = bebe.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextoOscuroClean
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Cake, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(bebe.fechaNacimiento, fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = themeBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = bebe.genero,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateBebes() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MomAccent.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ChildCare,
                null,
                tint = MomPrimary,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "¡Aún no tienes bebés registrados!",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextoOscuroClean
        )
        Text(
            "Agrega a tu pequeño para llevar\nel control de su lactancia.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
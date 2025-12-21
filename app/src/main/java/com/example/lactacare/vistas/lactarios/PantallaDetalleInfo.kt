package com.example.lactacare.vistas.lactarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.vistas.auth.FondoBlancoClean
import com.example.lactacare.vistas.auth.TextoOscuroClean
import com.example.lactacare.vistas.theme.MomPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleInfo(titulo: String, onVolver: () -> Unit) {
    Scaffold(
        containerColor = FondoBlancoClean,
        topBar = {
            TopAppBar(
                title = { Text("Información", fontWeight = FontWeight.Bold, color = TextoOscuroClean) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextoOscuroClean)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(titulo, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MomPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Aquí iría el contenido detallado sobre $titulo. \n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                fontSize = 16.sp,
                color = TextoOscuroClean,
                lineHeight = 24.sp
            )
        }
    }
}
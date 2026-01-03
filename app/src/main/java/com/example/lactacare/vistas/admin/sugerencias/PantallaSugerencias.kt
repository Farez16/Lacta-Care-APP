package com.example.lactacare.vistas.admin.sugerencias

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lactacare.vistas.theme.AdminPrimary

@Composable
fun PantallaSugerencias() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Feedback, null, tint = AdminPrimary, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Buzón de Sugerencias", color = AdminPrimary)
            Text("Próximamente disponible", color = Color.Gray)
        }
    }
}

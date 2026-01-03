package com.example.lactacare.vistas.admin.lactarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.lactacare.datos.dto.SalaLactanciaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGuardarLactario(
    lactarioEditar: SalaLactanciaDto? = null,
    onDismiss: () -> Unit,
    onGuardar: (SalaLactanciaDto) -> Unit
) {
    var nombre by remember { mutableStateOf(lactarioEditar?.nombre ?: "") }
    var direccion by remember { mutableStateOf(lactarioEditar?.direccion ?: "") }
    var telefono by remember { mutableStateOf(lactarioEditar?.telefono ?: "") }
    var correo by remember { mutableStateOf(lactarioEditar?.correo ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (lactarioEditar == null) "Nuevo Lactario" else "Editar Lactario",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Centro Médico") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val nuevoLactario = SalaLactanciaDto(
                                id = lactarioEditar?.id ?: 0,
                                nombre = nombre,
                                direccion = direccion,
                                telefono = telefono,
                                correo = correo
                            )
                            onGuardar(nuevoLactario)
                        },
                        enabled = nombre.isNotBlank() && direccion.isNotBlank()
                    ) {
                        Text(if (lactarioEditar == null) "Guardar" else "Actualizar")
                    }
                }
            }
        }
    }
}

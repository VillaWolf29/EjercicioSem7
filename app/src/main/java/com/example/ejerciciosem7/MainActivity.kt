package com.example.ejerciciosem7

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ejerciciosem7.ui.theme.EjercicioSem7Theme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Modelo de datos con dos atributos: nombre y precio
data class Item(
    val id: String = "",
    val nombre: String = "",
    val precio: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EjercicioSem7Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrudFirebaseScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CrudFirebaseScreen(modifier: Modifier = Modifier) {
    val db = Firebase.firestore
    val context = LocalContext.current
    
    // Estados para los campos de entrada
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    
    // Lista para mostrar los datos
    val itemsList = remember { mutableStateListOf<Item>() }

    // Función para LISTAR (en tiempo real)
    LaunchedEffect(Unit) {
        db.collection("productos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            
            itemsList.clear()
            snapshot?.documents?.forEach { doc ->
                val item = Item(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    precio = doc.getString("precio") ?: ""
                )
                itemsList.add(item)
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("CRUD Firebase (Agregar y Listar)", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Formulario para AGREGAR
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombre.isNotBlank() && precio.isNotBlank()) {
                    val nuevoItem = hashMapOf(
                        "nombre" to nombre,
                        "precio" to precio
                    )
                    db.collection("productos").add(nuevoItem)
                        .addOnSuccessListener {
                            nombre = ""
                            precio = ""
                            Toast.makeText(context, "Agregado con éxito", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Por favor completa ambos campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Lista de Productos:", style = MaterialTheme.typography.titleLarge)
        
        // LazyColumn para LISTAR los elementos
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(itemsList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Producto: ${item.nombre}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Precio: S/ ${item.precio}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

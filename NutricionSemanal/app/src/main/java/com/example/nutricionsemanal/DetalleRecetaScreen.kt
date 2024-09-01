package com.example.nutricionsemanal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.nutricionsemanal.receta.RecetaRepository

@Composable
fun DetalleRecetaScreen(navController: NavHostController, recetaIndex: Int, recetaRepository: RecetaRepository) {
    val receta = recetaRepository.getRecetaByIndex(recetaIndex)
    val context = LocalContext.current

    // Define tus colores de degradado
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFCDDC39)
    )

    receta?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .background(Brush.verticalGradient(gradientColors)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de la receta
            Image(
                painter = painterResource(id = R.drawable.comida_saludable_sf),
                contentDescription = receta.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nombre de la receta
            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Breve descripcion de la receta
            Text(
                text = receta.breveDescripcion,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Detalle de la receta
            Text(
                text = receta.detalle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para volver a la lista de recetas
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Volver")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleRecetaPreview() {
    val navController = rememberNavController()
    val recetaRepository = RecetaRepository()
    MaterialTheme {
        DetalleRecetaScreen(navController = navController, 0, recetaRepository = recetaRepository)
    }
}
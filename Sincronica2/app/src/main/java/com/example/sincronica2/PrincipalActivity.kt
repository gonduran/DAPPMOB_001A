package com.example.sincronica2

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recetas = arrayOf(
            arrayOf("Ensalada César", "Alto en proteínas, bajo en carbohidratos"),
            arrayOf("Pasta al Pesto", "Fuente de carbohidratos y grasas saludables"),
            arrayOf("Pollo a la plancha", "Bajo en calorías y alto en proteínas"),
            arrayOf("Sopa de verduras", "Rico en fibra y vitaminas"),
            arrayOf("Batido de frutas", "Fuente de antioxidantes y energía rápida")
        )

        val textView: TextView = findViewById(R.id.text_view_list)

        recetas.joinToString()
        val concatenaRecetas = recetas.joinToString( "\n\n" ) { receta ->
            "${receta[0]}\n${receta[1]}"
        }

        textView.text = concatenaRecetas
    }
}
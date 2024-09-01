package com.example.nutricionsemanal.receta

class RecetaRepository {
    private val recetas = listOf(
        Receta(
            "Ensalada César",
            "Fresca ensalada con pollo y aderezo César.",
            "Ingredientes: Lechuga romana, pollo, crutones, queso parmesano y aderezo César..."
        ),
        Receta(
            "Sopa de Tomate",
            "Sopa cremosa de tomate con albahaca.",
            "Ingredientes: Tomates, albahaca fresca, cebolla, ajo, caldo de verduras..."
        ),
        Receta(
            "Pasta al Pesto",
            "Pasta con salsa de pesto casera.",
            "Ingredientes: Albahaca fresca, ajo, queso parmesano, piñones, aceite de oliva..."
        ),
        Receta(
            "Pollo a la Plancha",
            "Pollo marinado con hierbas a la plancha.",
            "Ingredientes: Pechugas de pollo, hierbas aromáticas, ajo, limón y aceite de oliva..."
        ),
        Receta(
            "Arroz Frito con Verduras",
            "Arroz salteado con verduras frescas y salsa de soya.",
            "Ingredientes: Arroz cocido, zanahoria, guisantes, maíz, salsa de soya..."
        ),
        Receta(
            "Tacos de Pescado",
            "Tacos rellenos de pescado frito con salsa de yogur.",
            "Ingredientes: Filetes de pescado, tortillas de maíz, col rallada, salsa de yogur..."
        ),
        Receta(
            "Batido de Frutas",
            "Batido refrescante con una mezcla de frutas tropicales.",
            "Ingredientes: Mango, piña, leche de coco, hielo, miel..."
        )
    )

    fun getAllRecetas(): List<Receta> = recetas

    fun getRecetaByIndex(index: Int): Receta? {
        return recetas.getOrNull(index)
    }
}
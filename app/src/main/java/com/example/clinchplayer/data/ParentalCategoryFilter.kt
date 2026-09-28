package com.example.clinchplayer.data

/**
 * Devuelve solamente las categorías visibles para el perfil activo.
 *
 * Una categoría se oculta únicamente cuando:
 * 1. El control parental está activado para el perfil activo.
 * 2. La categoría fue marcada como protegida en Settings.
 */
fun <T> SettingsManager.filterVisibleCategories(
    contentType: String,
    categories: List<T>,
    categoryId: (T) -> String
): List<T> {
    return categories.filterNot { item ->
        isCategoryHidden(
            contentType = contentType,
            categoryId = categoryId(item)
        )
    }
}

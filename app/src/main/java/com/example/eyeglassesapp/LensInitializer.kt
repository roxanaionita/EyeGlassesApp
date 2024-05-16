package com.example.eyeglassesapp

import com.example.eyeglassesapp.dao.LensDao
import com.example.eyeglassesapp.entities.LensEntity

class LensInitializer(private val lensDao: LensDao) {
    suspend fun initializeLens(){
        val types = LensUtils.types
        val materials = LensUtils.materials
        val filters = LensUtils.filters

        val combinations = generateLensCombinations(types, materials, filters)

        // Insert toate combinațiile de lentile în database
        combinations.forEach { lensEntity ->
            lensDao.insertLens(lensEntity)
        }
    }
}
private fun generateLensCombinations(types: List<String>, materials: List<String>, filters: List<Boolean>): List<LensEntity> {
    val combinations = mutableListOf<LensEntity>()

    for (type in types) {
        for (material in materials) {
            for (pcFilter in filters) {
                for (uvFilter in filters) {
                    val price = calculateLensPrice(type, material, pcFilter, uvFilter)
                    val lensEntity = LensEntity(
                        type = type,
                        material = material,
                        pcFilter = pcFilter,
                        uvFilter = uvFilter,
                        price = price
                    )
                    combinations.add(lensEntity)
                }
            }
        }
    }

    return combinations
}
private fun calculateLensPrice(type: String, material: String, pcFilter: Boolean, uvFilter: Boolean): Double {
    // Definește prețurile de bază pentru fiecare tip de lentilă
    val basePriceMap = mapOf(
        "Oftalmic" to 50.0,
        "Polarized" to 80.0,
        "Antireflex" to 100.0
    )

    // Obține prețul de bază în funcție de tipul lentilei
    val basePrice = basePriceMap[type] ?: 50.0 // Dacă tipul nu este găsit, se folosește prețul de bază implicit

    // Ajustează prețul în funcție de material
    val materialPriceAdjustment = when (material) {
        "Plastic" -> 20.0
        "Glass" -> 40.0
        else -> 0.0
    }

    // Ajustează prețul în funcție de filtrele selectate
    var totalPrice = basePrice + materialPriceAdjustment
    if (pcFilter) {
        totalPrice += 20.0 // Prețul pentru filtrul PC
    }
    if (uvFilter) {
        totalPrice += 10.0 // Prețul pentru filtrul UV
    }

    return totalPrice
}

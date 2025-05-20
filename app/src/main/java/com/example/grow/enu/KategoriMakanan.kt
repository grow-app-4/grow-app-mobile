package com.example.grow.enu

enum class KategoriMakanan(val label: String) {
    MAKANAN_POKOK("Makanan Pokok"),
    PROTEIN_HEWANI("Protein Hewani"),
    PROTEIN_NABATI("Protein Nabati"),
    SAYUR("Sayur-sayuran"),
    BUAH("Buah-buahan");

    companion object {
        val ordered = values().toList()
    }
}
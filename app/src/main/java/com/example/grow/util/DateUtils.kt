package com.example.grow.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun formatTanggalToIndo(tanggal: String?): String {
    if (tanggal.isNullOrBlank()) return "-"

    // Cek apakah sudah dalam format Indonesia (misalnya: "14 Juni 2024")
    val indoRegex = Regex("""\d{1,2} [A-Za-z]+ \d{4}""")
    if (indoRegex.matches(tanggal)) {
        return tanggal // Sudah dalam format Indonesia, langsung return
    }

    // Daftar kemungkinan format input
    val possibleFormats = listOf(
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "yyyy/MM/dd",
        "dd/MM/yyyy"
    )

    val localeID = Locale("id", "ID")
    val outputFormat = SimpleDateFormat("dd MMMM yyyy", localeID)

    for (inputFormatStr in possibleFormats) {
        try {
            val inputFormat = SimpleDateFormat(inputFormatStr, Locale.getDefault())
            inputFormat.isLenient = false
            val date = inputFormat.parse(tanggal)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (_: ParseException) {
            // lanjut ke format berikutnya
        }
    }

    return "-" // fallback kalau semua format gagal
}


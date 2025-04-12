package com.example.grow.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.example.grow.R
import com.github.mikephil.charting.utils.MPPointF

class CustomMarkerView(context: Context) : MarkerView(context, R.layout.marker_view_layout) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    init {
        tvContent.setBackgroundColor(Color.parseColor("#0D47A1")) // Biru gelap
        tvContent.setTextColor(Color.WHITE)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val usia = e.x.toInt()
            val nilai = e.y
            tvContent.text = "Usia: $usia bln\nNilai: %.2f".format(nilai)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // Letakkan marker sedikit di atas titik
        return MPPointF(-(width / 2f), -height.toFloat())
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        super.draw(canvas, posX, posY)
    }
}

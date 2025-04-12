package com.example.grow.ui.screen

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.grow.data.AnakEntity
import com.example.grow.viewmodel.GrafikViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import android.view.LayoutInflater
import com.example.grow.R

@Composable
fun GrafikPertumbuhanScreen(
    anak: AnakEntity,
    idJenis: Int,
    viewModel: GrafikViewModel = hiltViewModel()
) {
    val grafikAnak by viewModel.grafikAnak.collectAsState()
    val grafikWHO by viewModel.grafikWHO.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(anak.idAnak, idJenis) {
        viewModel.loadGrafik(anak, idJenis)
    }

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
                )
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                setBackgroundColor(Color.WHITE)
                setExtraOffsets(10f, 10f, 10f, 10f)
                legend.isEnabled = false
                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = Color.DKGRAY
                    textSize = 12f
                    valueFormatter = UsiaValueFormatter()
                }

                axisLeft.apply {
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.DKGRAY
                    textSize = 12f
                    axisMinimum = when (idJenis) {
                        1 -> 30f
                        2 -> 0f
                        3 -> 20f
                        else -> 0f
                    }
                    axisMaximum = when (idJenis) {
                        1 -> 130f
                        2 -> 40f
                        3 -> 50f
                        else -> 100f
                    }
                }

                marker = CustomMarkerView(context)
            }
        },
        update = { chart ->
            val lineDataSets = mutableListOf<ILineDataSet>()

            val entriesAnak = grafikAnak.map { (x, y) -> Entry(x.toFloat(), y) }
            val dataAnak = LineDataSet(entriesAnak, "Data Anak").apply {
                color = Color.parseColor("#2196F3")
                setCircleColor(Color.parseColor("#1976D2"))
                lineWidth = 2.5f
                circleRadius = 5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            lineDataSets.add(dataAnak)

            val warnaZ = mapOf(
                -3f to Color.RED,
                -2f to Color.parseColor("#FFA500"),
                0f to Color.BLUE,
                2f to Color.parseColor("#FFA500"),
                3f to Color.RED
            )

            val sortedZ = listOf(-3f, -2f, 0f, 2f, 3f)
            val datasetsWHO = sortedZ.mapNotNull { z ->
                grafikWHO[z]?.map { (x, y) -> Entry(x.toFloat(), y) }?.let { entries ->
                    val dataSet = LineDataSet(entries, "z=$z").apply {
                        color = warnaZ[z] ?: Color.GRAY
                        setDrawCircles(false)
                        lineWidth = 2f
                        setDrawValues(false)
                        mode = LineDataSet.Mode.LINEAR
                        if (z == 0f) enableDashedLine(10f, 5f, 0f)
                    }
                    dataSet
                }
            }
            lineDataSets.addAll(datasetsWHO)
            chart.data = LineData(lineDataSets)
            chart.setVisibleXRangeMaximum(4f)
            chart.moveViewToX(0f)
            chart.invalidate()
        }
    )
}

class UsiaValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
    override fun getFormattedValue(value: Float): String = "${value.toInt()} bln"
}

class CustomMarkerView(context: android.content.Context) : MarkerView(context, R.layout.marker_view_layout) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    init {
        tvContent.setBackgroundColor(Color.parseColor("#0D47A1"))
        tvContent.setTextColor(Color.WHITE)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val usia = "Usia: ${e.x.toInt()} bln"
            val nilai = "Nilai: ${e.y}"
            tvContent.text = "$usia\n$nilai"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-width / 2).toFloat(), -height.toFloat())
    }
}

@Preview
@Composable
fun PreviewGrafik() {
    GrafikPertumbuhanScreen(
        anak = AnakEntity(
            idUser = 1,
            idAnak = 1,
            namaAnak = "Budi",
            tanggalLahir = "2020-01-01",
            jenisKelamin = "L"
        ),
        idJenis = 1
    )
}

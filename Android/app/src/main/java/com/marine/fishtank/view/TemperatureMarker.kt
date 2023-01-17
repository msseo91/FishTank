package com.marine.fishtank.view

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.marine.fishtank.R
import com.marine.fishtank.model.Temperature
import java.text.SimpleDateFormat

class TemperatureMarker(
    context: Context
): MarkerView(context, R.layout.custom_marker_view) {

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val textView = findViewById<TextView>(R.id.tvContent)
        textView.text = "${e?.y.toString()} °C"
        if(e?.data is Temperature) {
            val temperature = e.data as Temperature
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm").format(temperature.time)
            textView.append("\n${time}")
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(0f, -(height + 20).toFloat())
    }
}
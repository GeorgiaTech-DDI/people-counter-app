package com.example.people_counter_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counter_ui()
    }

    private fun counter_ui() {
        val bar_chart = findViewById<BarChart>(R.id.barChart)
        val curr_num_view = findViewById<TextView>(R.id.people_num)


        val x_name = listOf("Wood Room", "Metal Room", "The Hub", "Montgomery")
        val x_axis_name = bar_chart.xAxis
        x_axis_name.position = XAxis.XAxisPosition.BOTTOM
        x_axis_name.textSize = 20f
        x_axis_name.setLabelCount(x_name.size)
        x_axis_name.valueFormatter = IndexAxisValueFormatter(x_name)

        val entry = listOf(
            BarEntry(0f, 15f),
            BarEntry(1f, 7f),
            BarEntry(2f, 12f),
            BarEntry(3f, 10f)
        )

        val graph_set = BarDataSet(entry, "Current Number of People")
        val barData = BarData(graph_set)
        bar_chart.data = barData

        val x_axis = bar_chart.xAxis
        x_axis.textSize = 10f
        x_axis.position = XAxis.XAxisPosition.BOTTOM
        x_axis.setDrawAxisLine(true)
        x_axis.setDrawGridLines(false)

        val y_axis = bar_chart.axisLeft
        y_axis.textSize = 10f
        y_axis.setDrawAxisLine(true)
        y_axis.setDrawZeroLine(true)
        y_axis.setDrawGridLines(false)

        val not_use_y_axis = bar_chart.axisRight
        not_use_y_axis.isEnabled = false

        bar_chart.description.isEnabled = false

//        val y_sum = (entry.sumByDouble { it.y.toDouble() }).toInt()

        bar_chart.description.isEnabled = false

//        val maxNum_place =

        val y_max = bar_chart.yMax
        var max_x = 0
        for (i in 0 until barData.dataSetCount) {
            val dataSet = barData.getDataSetByIndex(i) as BarDataSet
            val entry_i = dataSet.getEntryForIndex(i)
            if (entry_i.y == y_max) {
                max_x = i
            }
        }

        curr_num_view.text = "${x_name[max_x]}: ${y_max.toInt()}"



        // barChart.invalidate()
    }

}
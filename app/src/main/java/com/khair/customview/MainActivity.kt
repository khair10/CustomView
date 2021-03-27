package com.khair.customview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChartView: PieChartView = findViewById<PieChartView>(R.id.pcv_info).apply {
            title = "BestTitle"
            pieBorderSize = 2f
            pieBorderColor = Color.BLUE
            isDrawValues = true
            isOrdered = false
            valuesTextColor = Color.WHITE
            valuesTextSize = 10f
            valuePairs = ArrayList<Int>().apply {
                add(5)
                add(2)
                add(10)
                add(3)
            }
        }
    }
}

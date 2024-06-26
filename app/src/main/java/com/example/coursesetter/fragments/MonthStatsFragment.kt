package com.example.coursesetter.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.coursesetter.MainActivity
import com.example.coursesetter.R
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var floatArrayMonth = FloatArray(31)
private lateinit var userID: String
var highestRunMonth = 0
var monthHeader : String = ""
/**
 * A simple [Fragment] subclass.
 * Use the [MonthStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthStatsFragment : Fragment() {
    // TODO: Rename and change types of parameters


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_month_stats, container, false)
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        MonthRunDists()

        return view


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val database = Firebase.database

        val composeView = view.findViewById<ComposeView>(R.id.lineChartComposeViewMonth)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                    //.background(MaterialTheme.colorScheme.tertiary)

                )
                {
                    Text(
                        text = monthHeader,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    LineChartScreenMonth() //When this is called it displays the graph with the data from the array.
                }

                // In Compose world

            }


        }


    }
    fun MonthHeaderFill(date1 : String, date2 : String)
    {
        monthHeader = "$date1 - $date2"
    }
    @Composable
    fun LineChartScreenMonth() {
        MonthRunDists()
        val steps = 6
        val pointsData = mutableListOf(
            Point(0f, floatArrayMonth[0])
        )
        for(i in 0.. (floatArrayMonth.size - 1))
        {
            pointsData.add(Point(i.toFloat(), floatArrayMonth[i]))
        }


        //Log.e("BUG", "highest run $highestRunMonth")
        val xAxisData = AxisData.Builder()
            .backgroundColor(Color.Transparent)
            .steps(pointsData.size - 1)
            .labelData { i ->
                if(i % 5 == 0){i.toString()}
                else{" "}
            }
            .labelAndAxisLinePadding(15.dp)
            .axisLineColor(MaterialTheme.colorScheme.tertiary)
            .axisLabelColor(MaterialTheme.colorScheme.tertiary)
            .axisStepSize(10.5.dp)
            .build()
        val yAxisData = AxisData.Builder()
            .steps(highestRunMonth)
            .backgroundColor(Color.Transparent)
            .labelAndAxisLinePadding(25.dp)
            .labelData { i ->
                val yScale = highestRunMonth / 1
                (i).toString()
            }
            .axisLineColor(MaterialTheme.colorScheme.tertiary)
            .axisLabelColor(MaterialTheme.colorScheme.tertiary)
            .build()
        val lineChartData = LineChartData(
            linePlotData = LinePlotData(
                lines = listOf(
                    Line(
                        dataPoints = pointsData,
                        LineStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            lineType = LineType.SmoothCurve(isDotted = false)
                        ),
                        IntersectionPoint(
                            color = MaterialTheme.colorScheme.tertiary,
                            alpha = 0.0f
                        ),
                        SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                        ShadowUnderLine(
                            alpha = 0.5f,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.inversePrimary,
                                    Color.Transparent
                                )
                            )
                        ),
                        SelectionHighlightPopUp()
                    )
                ),
            ),
            backgroundColor = MaterialTheme.colorScheme.surface,
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant, enableVerticalLines = false),
            isZoomAllowed = false,
            //paddingTop = 30.dp,
            // bottomPadding = 20.dp,
            //paddingRight = 10.dp,
            //containerPaddingEnd = 15.dp,


        )
        LineChart(
            modifier = Modifier
                //.fillMaxWidth()
                .fillMaxWidth()
                .height(350.dp),
            lineChartData = lineChartData
        )

    }
    //This function runs through the distances and dates array to find the values in the past month
    fun MonthRunDists() {

        val DBRunDistances = (activity as MainActivity).DBRunDistances
        val DBRunDates = (activity as MainActivity).DBRunDates
        val totalRuns = DBRunDistances.size
        val date: LocalDate = LocalDate.now()
        var dbDate: LocalDate = date
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        var distRan = 0
        var oldDays = date.minusDays(30)

        MonthHeaderFill(oldDays.format(formatter), date.format(formatter))
        //Log.e("MonthStats","30 Days Ago: $oldDays")

        for(i in 0..(totalRuns -1)){
            dbDate = DBRunDates[i]
            //Log.e("MonthStats", "Date is $dbDate")
            var daysDifference = ChronoUnit.DAYS.between(oldDays, dbDate)
            if(daysDifference >= 0) {
                //Log.e("MonthStats", "$dbDate is $daysDifference days from $oldDays")
                distRan = DBRunDistances[i].toInt()
                floatArrayMonth[daysDifference.toInt()] = distRan.toFloat()
                if(distRan > highestRunMonth){
                    highestRunMonth = distRan
                    //Log.e("MonthStats", "$daysDifference new record $highestRunMonth")
                }
            }
        }
    }
}


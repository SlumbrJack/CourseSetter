package com.example.coursesetter.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.fragment.app.Fragment

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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var userID: String
var distArray = intArrayOf(6,6,6,6,6,6,6)
var floatArray = floatArrayOf(0f,0f,0f,0f,0f,0f,0f)
var highestRunWeek = 0
var weekHeader : String = ""

class WeekStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userID = FirebaseAuth.getInstance().currentUser!!.uid
        WeekRunDists() //This function fills an array with data for the graph

        val view = inflater.inflate(R.layout.fragment_week_stats, container, false)

        //Log.e("RAAAAA","${(activity as MainActivity).RunDatabaseQuery()}")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var testString = ""
        for(i in (activity as MainActivity).DBRunDistances){
            testString += " $i"
        }

        val composeView = view.findViewById<ComposeView>(R.id.lineChartComposeView)
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
                        text = weekHeader,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    //Log.e("BUG", "CHART FILLED")
                    LineChartScreen() //When this is called it displays the graph with the data from the array.
                }

                // In Compose world

            }


        }


    }
    //This Function makes a line
    @Composable
    fun LineChartScreen() {
        WeekRunDists()
        val dayOfWeekArray = arrayOf("M", "T", "W", "Th", "F", "S", "Su")
        val steps = 6

        val pointsData = mutableListOf(
            Point(0f, floatArray[0]),
            Point(1f,  floatArray[1]),
            Point(2f, floatArray[2]),
            Point(3f,  floatArray[3]),
            Point(4f,  floatArray[4]),
            Point(5f,  floatArray[5]),
            Point(6f,  floatArray[6])
        )

        //Log.e("BUG", "highest run $highestRunWeek")
        val xAxisData = AxisData.Builder()
            .backgroundColor(Color.Transparent)
            .steps(pointsData.size - 1)
            .labelData { i -> dayOfWeekArray[i] }
            .labelAndAxisLinePadding(15.dp)
            .axisLineColor(MaterialTheme.colorScheme.tertiary)
            .axisLabelColor(MaterialTheme.colorScheme.tertiary)
            .axisStepSize(50.dp)
            .build()
        val yAxisData = AxisData.Builder()
            .steps(highestRunWeek)
            .backgroundColor(Color.Transparent)
            .labelAndAxisLinePadding(25.dp)
            .labelData { i ->
                val yScale = highestRunWeek / 1
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
            paddingTop = 30.dp,
            bottomPadding = 20.dp,
            paddingRight = 10.dp,
            containerPaddingEnd = 15.dp,

            )
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            lineChartData = lineChartData
        )

    }
    //This function fills a string with the range of dates for the graph
    fun WeekHeaderFill(date1 : String, date2 : String)
    {
        weekHeader = "$date1 - $date2"
    }
    //This function runs through the distances array from main and finds the dates in the past week
    fun WeekRunDists() {
        var DBRunDistances = (activity as MainActivity).DBRunDistances
        var DBRunDates = (activity as MainActivity).DBRunDates
        var totalRuns = DBRunDistances.size

        var runNum = 1

        var keepChecking = true

        val date: LocalDate = LocalDate.now()
        var dbDate: LocalDate = date
        //most recent monday
        val firstDay: LocalDate =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        var distRan = 0
        WeekHeaderFill(firstDay.format(formatter), firstDay.plusDays(6).format(formatter))
        //NEW STUFF
        for(i in 0..(totalRuns - 1)){
            dbDate = DBRunDates[i]

            //Log.e("Week", " current $dbDate first $firstDay")
            if(dbDate.isAfter(firstDay) or dbDate.isEqual(firstDay)){
                var dayOfWeekValue = dbDate.dayOfWeek.value
                floatArray[dayOfWeekValue - 1] = DBRunDistances[i]
                distRan = DBRunDistances[i].toInt()
                if(distRan > highestRunWeek){
                    highestRunWeek = distRan
                    //Log.e("Week", "Day: $dayOfWeekValue Highest: $highestRunWeek")
                }
            }
        }
    }
}



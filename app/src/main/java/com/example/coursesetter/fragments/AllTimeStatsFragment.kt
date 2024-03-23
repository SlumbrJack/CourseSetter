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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var floatListAll = mutableListOf<Float>()
private lateinit var userID: String
var highestRun = 0
var allTimeHeader : String = ""
/**
 * A simple [Fragment] subclass.
 * Use the [AllTimeStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllTimeStatsFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        AllTimeRunDists()
        return inflater.inflate(R.layout.fragment_all_time_stats, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val database = Firebase.database

        val composeView = view.findViewById<ComposeView>(R.id.lineChartComposeViewAll)
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
                        text = allTimeHeader,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                    )
                    LineChartScreenAll() //When this is called it displays the graph with the data from the array.
                }

                // In Compose world

            }


        }


    }
    @Composable
    fun LineChartScreenAll() {
        AllTimeRunDists()
        val steps = 6

        val pointsData = mutableListOf(
            Point(0f, 0f)
        )
        for(i in 0.. (floatListAll.size - 1))
        {
            pointsData.add(Point(i.toFloat(), floatListAll[i]))
        }


        Log.e("AllTime", "highest run $highestRun")
        val xAxisData = AxisData.Builder()
            .backgroundColor(Color.Transparent)
            .steps(pointsData.size - 1)
            //.labelData { i -> i.toString() }
            .labelAndAxisLinePadding(15.dp)
            .axisLineColor(MaterialTheme.colorScheme.tertiary)
            .axisLabelColor(MaterialTheme.colorScheme.tertiary)
            .axisStepSize((375/pointsData.size).dp)
            .build()
        val yAxisData = AxisData.Builder()
            .steps(highestRun)
            .backgroundColor(Color.Transparent)
            .labelAndAxisLinePadding(25.dp)
            .labelData { i ->
                val yScale = highestRun / 1
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
            isZoomAllowed = false


        )
        LineChart(
            modifier = Modifier
                //.fillMaxWidth()
                .fillMaxWidth()
                .height(350.dp),
            lineChartData = lineChartData
        )

    }
    fun AllTimeHeaderFill(date1 : String, date2 : String)
    {
        allTimeHeader = "$date1 - $date2"
    }
    fun AllTimeRunDists() {
        floatListAll.clear()
        var DBRunDistances = (activity as MainActivity).DBRunDistances
        var DBRunDates = (activity as MainActivity).DBRunDates
        var totalRuns = DBRunDistances.size
        val date: LocalDate = LocalDate.now()
        var dbDate: LocalDate = date

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        var distRan = 0f

        var oldestDate = DBRunDates[0]
        AllTimeHeaderFill(oldestDate.format(formatter), date.format(formatter))
        var noDateFound = false
        var increment = 1
        for(i in 1..(totalRuns -1)){
            noDateFound = false
            dbDate = DBRunDates[i]
            if(distRan > highestRun){
                //Log.e("AllTime", "Highest Updated to: $distRan")
                highestRun = distRan.toInt()
            }
            //Log.e("DEBUG- Alltime", "Loop: $i, DBDate: $dbDate, DateFound: $noDateFound")
            if(oldestDate.plusDays(1).isEqual(dbDate))
            {
                distRan = DBRunDistances[i]
                floatListAll.add(distRan)

                increment++

            }
            else{

                while(!noDateFound){

                    if(oldestDate.plusDays(increment.toLong()).isEqual(dbDate))
                    {
                        noDateFound = true
                        //Log.e("DEBUG- Alltime- True Checked", "${oldestDate.plusDays(increment.toLong()).isEqual(dbDate)}")
                        distRan = DBRunDistances[i]
                        floatListAll.add(distRan)

                        //Log.e("Alltime", "Date added after $increment extra runs: $dbDate")
                        // Log.e("DEBUG- Alltime- Added Full", "Loop: $i, Increment: $increment, DBDate: $dbDate, Checking Date: ${oldestDate.plusDays(increment.toLong())}")
                        increment = 1
                        oldestDate = dbDate

                    }
                    else{
                        floatListAll.add(0f)
                        increment++
                        //Log.e("Alltime", "Added empty: ${oldestDate.plusDays(increment.toLong())}")
                        //Log.e("DEBUG- Alltime- Add Empty", "Loop: $i, Increment: $increment, DBDate: $dbDate, Checking Date: ${oldestDate.plusDays(increment.toLong())}")
                    }
                }
            }

        }
    }
}
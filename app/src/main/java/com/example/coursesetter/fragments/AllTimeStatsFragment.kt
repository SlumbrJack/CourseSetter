package com.example.coursesetter.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.FloatList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
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
import com.example.coursesetter.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var floatListAll = mutableListOf<Float>()
private lateinit var userID: String
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                )
                {

                    Log.e("BUG", "CHART FILLED")
                    LineChartScreenMonth() //When this is called it displays the graph with the data from the array.
                }

                // In Compose world

            }


        }


    }
    @Composable
    fun LineChartScreenMonth() {
        val steps = 6

        val pointsData = mutableListOf(
            Point(0f, 0f)
        )
        for(i in 0.. (floatListAll.size - 1))
        {
            pointsData.add(Point(i.toFloat(), floatListAll[i]))
        }


        Log.e("BUG", "highest run $highestRun")
        val xAxisData = AxisData.Builder()
            .backgroundColor(Color.Transparent)
            .steps(pointsData.size - 1)
            .labelData { i -> i.toString() }
            .labelAndAxisLinePadding(15.dp)
            //.startDrawPadding(10.dp) Fixes cutoff but messes up points
            //.startPadding(10.dp)
            .axisLineColor(MaterialTheme.colorScheme.tertiary)
            .axisLabelColor(MaterialTheme.colorScheme.tertiary)
            .axisStepSize(50.dp)
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
                            color = MaterialTheme.colorScheme.tertiary
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
            gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
            isZoomAllowed = false,
            paddingTop = 30.dp,
            bottomPadding = 20.dp,
            paddingRight = 10.dp,
            containerPaddingEnd = 15.dp,


            )
        LineChart(
            modifier = Modifier
                //.fillMaxWidth()
                .width(500.dp)
                .height(350.dp),
            lineChartData = lineChartData
        )

    }

    fun AllTimeRunDists() {

        val date: LocalDate = LocalDate.now()
        var dbDate: LocalDate = date

        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        var distRan = 0f
        var totalRuns = 0
        var oldestDate = date
        var noDateFound = false


        Firebase.database.getReference("Users Runs").child("Users").child(userID)
            .child("Total Runs").get().addOnSuccessListener {

                Log.e("AllStats", "total runs is ${it.value}")
                totalRuns = it.value.toString().toInt()


            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }.addOnCompleteListener {

                Firebase.database.getReference("Users Runs").child("Users").child(userID)
                    .child("1").child("Date").get().addOnSuccessListener {

                        dbDate = LocalDate.parse(it.value.toString(), formatter)
                        oldestDate = dbDate

                        Firebase.database.getReference("Users Runs").child("Users")
                                .child(userID).child("1").child("Distance").get().addOnSuccessListener {
                                    distRan = it.value.toString().toFloat()
                                    floatListAll.add(distRan)
                                    Log.e("Alltime", "First Date: $dbDate")

                                }.addOnCompleteListener {
                                var increment = 1
                                for (i in 2..totalRuns) {
                                noDateFound = false
                                    Firebase.database.getReference("Users Runs").child("Users").child(userID)
                                        .child("$i").child("Date").get().addOnSuccessListener {
                                            dbDate = LocalDate.parse(it.value.toString(), formatter)
                                            noDateFound = false
                                            Log.e("DEBUG- Alltime", "Loop: $i, DBDate: $dbDate, DateFound: $noDateFound")



                                                if(oldestDate.plusDays(1).isEqual(dbDate))
                                                {
                                                    Firebase.database.getReference("Users Runs").child("Users").child(userID)
                                                        .child("$i").child("Distance").get().addOnSuccessListener {
                                                            distRan = it.value.toString().toFloat()
                                                            floatListAll.add(distRan)
                                                            increment++
                                                        }
                                                }
                                                else{

                                                    while(!noDateFound){

                                                        if(oldestDate.plusDays(increment.toLong()).isEqual(dbDate))
                                                        {
                                                            noDateFound = true
                                                            Log.e("DEBUG- Alltime- True Checked", "${oldestDate.plusDays(increment.toLong()).isEqual(dbDate)}")
                                                            Firebase.database.getReference("Users Runs").child("Users").child(userID)
                                                                .child("$i").child("Distance").get().addOnSuccessListener {
                                                                    distRan = it.value.toString().toFloat()

                                                                }.addOnCompleteListener {
                                                                    floatListAll.add(distRan)

                                                                    Log.e("Alltime", "Date added after $increment extra runs: $dbDate")
                                                                    Log.e("DEBUG- Alltime- Added Full", "Loop: $i, Increment: $increment, DBDate: $dbDate, Checking Date: ${oldestDate.plusDays(increment.toLong())}")
                                                                    increment++
                                                                }
                                                        }
                                                        else{
                                                            floatListAll.add(0f)
                                                            increment++
                                                            Log.e("Alltime", "Added empty: ${oldestDate.plusDays(increment.toLong())}")
                                                            Log.e("DEBUG- Alltime- Add Empty", "Loop: $i, Increment: $increment, DBDate: $dbDate, Checking Date: ${oldestDate.plusDays(increment.toLong())}")
                                                        }
                                                    }

                                                }


                                        }.addOnFailureListener {Log.e("firebase", "Error getting data", it)}
                                }
                            }

                    }





                /*
                /// ORIGINAL
                        Firebase.database.getReference("Users Runs").child("Users").child(userID)
                            .child("Total Runs").get().addOnSuccessListener {

                                Log.e("AllStats", "total runs is ${it.value}")
                                totalRuns = it.value.toString().toInt()
                                for (i in 1..totalRuns) {

                                    Firebase.database.getReference("Users Runs").child("Users").child(userID)
                                        .child("$i").child("Date").get().addOnSuccessListener {

                                            dbDate = LocalDate.parse(it.value.toString(), formatter)
                                            if(i == 1)
                                            {
                                                oldestDate = dbDate
                                                ChronoUnit.DAYS.between(oldestDate, date)
                                            }
                                            ChronoUnit.DAYS.between(oldestDate, date)
                                            //Log.e("date", "Date is $dbDate")
                                            var daysDifference = ChronoUnit.DAYS.between(oldDays, dbDate)
                                            if(daysDifference >= 0)
                                            {
                                                Log.e("MonthStats", "$dbDate is $daysDifference days from $oldDays")
                                                Firebase.database.getReference("Users Runs").child("Users").child(userID)
                                                    .child("$i").child("Distance").get().addOnSuccessListener {

                                                        distRan = it.value.toString().toInt()
                                                        Log.e("dist", "dist is $distRan")
                                                        floatListAll[daysDifference.toInt()] = distRan.toFloat()

                                                        if(distRan > highestRun){
                                                            highestRun = distRan
                                                            Log.e("MonthStats", "$daysDifference new record $highestRun")
                                                        }
                                                    }.addOnFailureListener {
                                                        Log.e("firebase", "Error getting data", it)
                                                    }
                                            }


                                        }.addOnFailureListener {
                                            Log.e("firebase", "Error getting data", it)
                                        }
                                }
                            }.addOnFailureListener {
                                Log.e("firebase", "Error getting data", it)
                         */   }
    }
}
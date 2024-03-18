package com.example.coursesetter.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.coursesetter.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private lateinit var userID: String
var distArray = intArrayOf(6,6,6,6,6,6,6)
var floatArray = floatArrayOf(6f,6f,6f,6f,6f,6f,6f)
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        WeekRunDists()
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val composeView = view.findViewById<ComposeView>(R.id.lineChartComposeView)
        composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                )
                {
                    LineChartScreen()
                }

                // In Compose world

            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
@Composable
fun LineChartScreen()
{


    val dayOfWeekArray = arrayOf("M", "T", "W", "Th", "F", "S", "Su")
    val steps = 6
    val pointsData = listOf(
        Point(0f,  1f + floatArray[0]),
        Point(1f, 1f + floatArray[1]),
        Point(2f, 1f + floatArray[2]),
        Point(3f, 1f + floatArray[3]),
        Point(4f, 1f + floatArray[4]),
        Point(5f, 1f + floatArray[5]),
        Point(6f, 2f + floatArray[6])
    )
    val xAxisData = AxisData.Builder()
        .backgroundColor(Color.Transparent)
        .steps(pointsData.size - 1)
        .labelData{i -> dayOfWeekArray[i] }
        .labelAndAxisLinePadding(15.dp)
        //.startDrawPadding(10.dp) Fixes cutoff but messes up points
        //.startPadding(10.dp)
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .axisStepSize(50.dp)
        .build()
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(25.dp)
        .labelData { i ->
            val yScale = 90 / steps
            (i * yScale).toString()
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
            .fillMaxWidth()
            .height(350.dp),
        lineChartData = lineChartData
    )
}
fun WeekRunDists() {
    var runNum = 1

    var keepChecking = true
    // if(dbDate.time.compareTo(firstDayOfWeek))
    //{

    // }
    // NEW
    val date: LocalDate = LocalDate.now()
    var dbDate : LocalDate = date
    //most recent monday
    val firstDay: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    var distRan = 0
    var totalRuns = 0
    Firebase.database.getReference("Users Runs").child("Users").child(userID).child("Total Runs").get().addOnSuccessListener {
        Log.e("dist", "total runs is ${it.value}")
        totalRuns = it.value.toString().toInt()
        for(i in 1..totalRuns){
            Firebase.database.getReference("Users Runs").child("Users").child(userID).child("$i").child("Date").get().addOnSuccessListener {
                dbDate = LocalDate.parse(it.value.toString(), formatter)
                //Log.e("date", "Date is $dbDate")
                if(dbDate.isAfter(firstDay)){
                    //Get distance ran and day of week
                    var dayOfWeekValue = date.dayOfWeek.value

                    Firebase.database.getReference("Users Runs").child("Users").child(userID).child("$i").child("Distance").get().addOnSuccessListener {
                        distRan = it.value.toString().toInt()
                        Log.e("dist", "dist is $distRan")
                        floatArray[dayOfWeekValue - 1] += distRan.toFloat()
                        runNum++
                    }.addOnFailureListener{
                        Log.e("firebase", "Error getting data", it)
                    }
                }
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
                keepChecking = false
            }
        }
    }.addOnFailureListener{
        Log.e("firebase", "Error getting data", it)
    }


    Log.e("firebase", "Runs ${floatArray[6]}")


}
package com.example.coursesetter.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.coursesetter.MainActivity
import com.example.coursesetter.R
import com.example.coursesetter.databinding.FragmentHomeBinding

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random


private lateinit var userID: String

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    val date: LocalDate = LocalDate.now()

    val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")

    var totalRuns: Int = 4
    // This property is only valid between onCreateView and
    // onDestroyView.
    //Database Code vvv

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = Firebase.database
        val generateDataBtn = view.findViewById<Button>(R.id.GenerateSampleButton)

        //run data code.


        var totalRunsAny: Any?


        //Listener for the "total runs" var in DB
        Firebase.database.getReference("Users Runs").child("Users").child(userID)
            .child("Total Runs").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    totalRunsAny = dataSnapshot.value
                    if (totalRunsAny != null) {
                        totalRuns = totalRunsAny.toString().toInt()
                        Log.d(TAG, "Value is: $totalRunsAny")
                    } else {
                        Firebase.database.getReference("Users Runs").child("Users")
                            .child(userID)
                            .child("Total Runs").setValue(0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })

        //on button press, generate fake run data for a new run for the signed in user
        generateDataBtn.setOnClickListener {

            if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                val dbLocation = Firebase.database.getReference("Users Runs").child("Users").child(userID)
                var foundBool : Boolean = false
                dbLocation.child("Total Runs").get()
                    .addOnSuccessListener {//Gets the total # of runs in DB
                        totalRuns = it.value.toString().toInt()
                        FoundMatch()

                    }.addOnFailureListener {
                        Log.e("firebase", "Error getting data", it)
                    }
            }

        }
    }

    fun FoundMatch(){
        var dbDate: LocalDate
        val dbLocation = Firebase.database.getReference("Users Runs").child("Users").child(userID)
        var foundMatch = false
        var DBRunDistances = (activity as MainActivity).DBRunDistances
        var DBRunDates = (activity as MainActivity).DBRunDates
        var totalRuns = DBRunDistances.size
        for (i in 0..totalRuns - 1) {
            dbDate = DBRunDates[i]
            if (dbDate.isEqual(date)){
                Log.e("Home", "$dbDate = $date, ${DBRunDistances[i]}")
                foundMatch = true
                DBRunDistances[i] = DBRunDistances[i] + Random.nextInt(1,5)
                dbLocation.child("${i+1}").child("Distance").setValue(DBRunDistances[i])
            }
            if(i == totalRuns-1)
            {
                if(!foundMatch){
                    totalRuns++

                    dbLocation.child("Total Runs").setValue(totalRuns)
                    val userRunLocation = dbLocation.child("$totalRuns")

                    userRunLocation.child("Distance").setValue("3")
                    userRunLocation.child("Steps").setValue("3000")
                    userRunLocation.child("Calories Burned").setValue("200")
                    userRunLocation.child("Time").setValue("20:10")
                    Log.e("Home: Error", "Adding date here FALSE")

                    //new date stuff
                    val formatted: String =
                        date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                    userRunLocation.child("Date").setValue(formatted)
                    userRunLocation.child("Day").setValue(date.dayOfWeek)

                    //Add locally
                    (activity as MainActivity).DBRunDistances.add(3f)
                    (activity as MainActivity).DBRunDates.add(date)
                }
            }


            /*
            dbLocation.child("$i").child("Date").get()
                .addOnSuccessListener { //Gets the date of every run

                    dbDate = LocalDate.parse(it.value.toString(), formatter)
                    if (dbDate.isEqual(date)) //if the date is the same as today,
                    {
                        foundMatch = true
                        Log.e("ERROR", "DATE SAME $foundMatch")
                        //Get current distance from db
                        dbLocation.child("$i").child("Distance").get()
                            .addOnSuccessListener {
                            //add distance to currdistance
                            //and new total to db
                            dbLocation.child("$i").child("Distance").setValue(
                                "${
                                    it.value.toString().toInt() + Random.nextInt(
                                        1,
                                        5
                                    )
                                }"
                            )

                        }.addOnFailureListener {
                            Log.e("firebase", "Error getting data", it)
                        }
                    }
                }.addOnFailureListener {
                    Log.e("firebase", "Error getting data", it)
                }.addOnCompleteListener {
                    if(i == totalRuns)
                    {
                        if(!foundMatch){
                            totalRuns++
                            dbLocation.child("Total Runs").setValue(totalRuns)
                            val userRunLocation = dbLocation.child("$totalRuns")

                            userRunLocation.child("Distance").setValue("3")
                            userRunLocation.child("Steps").setValue("3000")
                            userRunLocation.child("Calories Burned").setValue("200")
                            userRunLocation.child("Time").setValue("20:10")
                            Log.e("ERROR", "Adding date here FALSE")
                            //new date stuff

                            val formatted: String =
                                date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                            userRunLocation.child("Date").setValue(formatted)
                            userRunLocation.child("Day").setValue(date.dayOfWeek)
                        }
                    }

                }

*/
        }

        if (totalRuns == 0) {
            totalRuns++
            dbLocation.child("Total Runs").setValue(totalRuns)
            val userRunLocation = dbLocation.child("$totalRuns")

            userRunLocation.child("Distance").setValue("3")
            userRunLocation.child("Steps").setValue("3000")
            userRunLocation.child("Calories Burned").setValue("200")
            userRunLocation.child("Time").setValue("20:10")

            //new date stuff

            val formatted: String =
                date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
            userRunLocation.child("Date").setValue(formatted)
            userRunLocation.child("Day").setValue(date.dayOfWeek)

            (activity as MainActivity).DBRunDistances.add(3f)
            (activity as MainActivity).DBRunDates.add(date)
            Log.e("ERROR", "Adding DATE HERE")
        }
        Log.e("ERROR", "VALUE $foundMatch")


    }

}

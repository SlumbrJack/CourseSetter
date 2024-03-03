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
import com.example.coursesetter.R
import com.example.coursesetter.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

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
        var totalRuns : Int = 4
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        var totalRunsAny : Any?

        //Listener for the "total runs" var in DB
        Firebase.database.getReference("Users Runs").child("Users").child(userID).child("Total Runs").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                totalRunsAny = dataSnapshot.value
                if(totalRunsAny != null)
                {
                    totalRuns = totalRunsAny.toString().toInt()
                    Log.d(TAG, "Value is: $totalRunsAny")
                }
                else
                {
                    Firebase.database.getReference("Users Runs").child("Users").child(userID).child("Total Runs").setValue(0)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        //on button press, generate fake run data for a new run for the signed in user
        generateDataBtn.setOnClickListener {
            if(FirebaseAuth.getInstance().currentUser?.uid != null)
            {
                //this code adds demo data to the data base for the signed in user
                totalRuns++
                Firebase.database.getReference("Users Runs").child("Users").child(userID).child("Total Runs").setValue(totalRuns)
                val userRunLocation = Firebase.database.getReference("Users Runs").child("Users").child(userID).child("$totalRuns")

                userRunLocation.child("Distance").setValue("3")
                userRunLocation.child("Steps").setValue("3000")
                userRunLocation.child("Calories Burned").setValue("200")
                userRunLocation.child("Time").setValue("20:10")

                //date stuff
                val c: Calendar = Calendar.getInstance()
                var d: Date = c.time
                val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                //d = Date("09-Mar-2024")
                //c.time = d

                val formattedDate: String = df.format(d)
                userRunLocation.child("Date").setValue(formattedDate)

                val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
                userRunLocation.child("Day").setValue(dayOfWeek)
            }
        }
    }


}
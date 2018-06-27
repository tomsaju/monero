package com.monero.addActivities


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.monero.R


/**
 * A simple [Fragment] subclass.
 */
public class AddActivityFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       var view = inflater!!.inflate(R.layout.new_activity_fragment, container, false);



        return view;
    }

}// Required empty public constructor

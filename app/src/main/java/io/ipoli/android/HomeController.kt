package io.ipoli.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.bluelinelabs.conductor.Controller

class HomeController : Controller() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_home, container, false)
//        (view.findViewById(R.id.tv_title) as TextView).text = "Hello World"
        return view
    }

}
package com.example.profesores.Fragments.profesores

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.profesores.Fragments.FragmentProfesorCurso
import com.example.profesores.R
import com.example.profesores.activities.ActivityMain
import com.example.profesores.adapters.AdapterProfesor
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser

class FragmentProfesores : Fragment(), ProfesoresContract.View, AdapterProfesor.OnItemClickListener, AdapterProfesor.makeFavListener {
    private lateinit var adapter: AdapterProfesor
    private val currentUser = ParseUser.getCurrentUser()
    private lateinit var searchView: EditText //para filters
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profesores, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.pr_rv)
        val query = ParseQuery<ParseObject>("Profesores")

        //Para filterd:
        searchView =  view.findViewById(R.id.pr_searchEdit)
        query.include("cursos")
        query.findInBackground { list, e ->
            if (e == null) {
                adapter = AdapterProfesor(list)
                adapter.setListener(this)
                adapter.setFavListener(this)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this.context)
            } else
                error { "Error $e" }  // Log.e using anko
        }

        return view
    }

    //Para filters
    @Override
    override fun onResume() {
        super.onResume()
        searchView.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.v("Filter", "Perform filter ${p0.toString()}")
                adapter.filter.filter(p0)
            }

        })
    }
    //

    override fun onItemClick(position: Int) {
        //(activity as ActivityMain).openProfesorCurso()
        val fragment = FragmentProfesorCurso()
        val args = Bundle()
        args.putString("profesorDeCurso", adapter.names[position].objectId)
        (activity as ActivityMain).openFragment(fragment, args)
    }

    override fun favItemClick(position: Int) {

        val userProfes = currentUser.getRelation<ParseObject>("profesoresFav")

        userProfes.query.whereEqualTo("name", adapter.names[position]["name"]).getFirstInBackground {
            _, e->
            if(e == null){
                    adapter.names[position].saveInBackground {
                        userProfes.remove(adapter.names[position])
                    }
                }
            else {
                adapter.names[position].saveInBackground {
                    userProfes.add(adapter.names[position])
                }
            }
        }

        currentUser.saveInBackground()
        adapter.names[position].saveInBackground()
        adapter.notifyDataSetChanged()

    }
}
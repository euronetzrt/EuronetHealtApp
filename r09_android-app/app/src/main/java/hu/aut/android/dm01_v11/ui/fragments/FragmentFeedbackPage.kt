package hu.aut.android.dm01_v11.ui.fragments

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.aut.android.dm01_v11.R
import kotlinx.android.synthetic.main.fragment_feedback_page.*

class FragmentFeedbackPage : Fragment() {

    companion object{
        val TAG = "FEEDBACK"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_page,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendFeedback.setOnClickListener {
            fragmentManager!!.popBackStack()
        }
    }
}
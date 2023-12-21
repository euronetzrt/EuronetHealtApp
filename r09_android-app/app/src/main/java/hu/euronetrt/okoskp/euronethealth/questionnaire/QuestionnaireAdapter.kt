package hu.euronetrt.okoskp.euronethealth.questionnaire

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import kotlinx.android.synthetic.main.questionnaire_row.view.*
import java.text.SimpleDateFormat


class QuestionnaireAdapter(private var mContext: Context, private var items: MutableList<QuestionnaireDataClass>) : RecyclerView.Adapter<QuestionnaireAdapter.ViewHolder>() {

    companion object {
        private val TAG = "QuestionnaireAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vH = LayoutInflater.from(parent.context).inflate(R.layout.questionnaire_row, parent, false)
        return ViewHolder(vH)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "size : ${items.size}")
        return items.size
    }

    fun removeAll (){
        items.clear()
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("adapterecske", "position : $position")
        holder.name.text = items[position].questionnairegetObject.name
        holder.description.text = items[position].questionnairegetObject.description
        holder.questionnaireResultListFillableElement = items[position].questionnaireResultListFillableElement
        holder.questionnairegetObject = items[position].questionnairegetObject

        if (items[position].questionnaireResultListFillableElement.scheduledUntil != null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val long = items[position].questionnaireResultListFillableElement.scheduledUntil?.date
            val formatted = formatter.format(long)
            holder.time.text = "Term: $formatted"
        } else {
            holder.time.text = "Term: Undefined"
        }

        if (holder.questionnairegetObject.template.required) {
            holder.icon.visibility = View.VISIBLE
        } else {
            holder.icon.visibility = View.GONE
        }
        holder.bind(mContext, items[position].questionnaireResultListFillableElement, items[position].questionnairegetObject)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.id_que_name
        var description: TextView = itemView.id_que_Description
        lateinit var questionnaireResultListFillableElement: QuestionnaireResultListFillable
        lateinit var questionnairegetObject: QuestionnaireGetModel
        var time: TextView = itemView.id_time
        var icon: ImageView = itemView.id_imageViewRequireIcon
        fun bind(context: Context, questionnaireResultListFillableElement: QuestionnaireResultListFillable, questionnairegetObject: QuestionnaireGetModel) {
            itemView.setOnClickListener {
                (context as DeviceMainActivity).getQuestionnaireReferenc().questionnaireInterface(questionnaireResultListFillableElement, questionnairegetObject)
            }
        }
    }
}
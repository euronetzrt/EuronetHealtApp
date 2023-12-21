package hu.aut.android.dm01_v11.ui.fragments.questionnaire

import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.warkiz.tickseekbar.TextPosition
import com.warkiz.tickseekbar.TickMarkType
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.MySeekBar.MyOnSeekChangeListener
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.MySeekBar.MySeekParams
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.MySeekBar.MyTickSeekBar
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultForServer
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.TemplateSectionModel
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import kotlinx.android.synthetic.main.questions_fragment.*

@Suppress("UNCHECKED_CAST")
class Questionsfragement(var sectionsFinished: MutableMap<Int, OwnModel?>,
                         var questionnaireResultListFillableElement: QuestionnaireResultListFillable,
                         var questionnairegetObject: QuestionnaireGetModel,
                         var actualSectionBlock: TemplateSectionModel,
                         var sectionFragmentContext : SectionFragment) : Fragment() {

    companion object {
        private val TAG = "Questionsfragement"
        private val DATALOADTAG = "DATALOADTAG"
        private val DIVIDER_MARGIN = 8
        private val DEFAULT_MARGIN = 8
        private val CHECKBOX_MARGIN = 8
        private val QUESTION_TOP_BOTTOM_MARGIN = 8
    }

    var startDate = System.currentTimeMillis()
    private var saveDone: Boolean? = null
    private var firstLikertAnswer = true
    private var newMultipleChoice = true
    /*kérdés típusa és választ tartalmazó view id-ja*/
    private var typeAndAnswerViewIDPairs = mutableListOf<Pair<Int, Int>>()
    /*A kérdés sorszáma és a hozzá tartozó typeAndAnswerViewIDPairs*/
    private var answerListHelper = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    /*első paraméter a view Id-ja, a második eleme V: valós érték amit használni kell*/
    private var answerLIKERTHelper = mutableMapOf<Int, Int>()

    private lateinit var meteor: EuronetMeteorSingleton

    private lateinit var questionnaireResultForServer: QuestionnaireResultForServer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.questions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id_questionsFragment.setOnClickListener {
            val imm: InputMethodManager = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        val layout = id_alFragmentLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)

        var questionCounterForId = 1
        var dividerCounterForId = 100
        var questionTypeCounterForId = 200
        var singleChooseTypeRadioButtonCounterForId = 300
        var singleChooseTypeImageViewCounterForId = 400
        var multipleChoiceTypeCounterForId = 500
        var multipleChoiceTypeImageViewCounterForId = 600
        var likertTypeCounterForId = 700
        var likertTypeImageViewCounterForId = 800
        var likertITEMTypeCounterForId = 900

        actualSectionBlock.questions.forEach { actBlockQuestionItem ->

            var uploadValue: Any? = null

            questionnaireResultListFillableElement.sections.forEach { qResultGetSectionItem ->
                Log.d(DATALOADTAG, "Szekció: ${qResultGetSectionItem.order}")
                if (actualSectionBlock.order == qResultGetSectionItem.order) {
                    /**Találtunk ehhez a szekvcióhoz mentett szekciót*/
                    if (qResultGetSectionItem.answers.size > 0) {
                        /**van mentett válasz*/
                        qResultGetSectionItem.answers.forEach {
                            if (actBlockQuestionItem.order == it.questionOrder) {
                                uploadValue = it.value
                            }
                        }
                    }
                }
            }

            val dividerView = View(view.context)
            dividerView.id = dividerCounterForId

            constraintSet.constrainHeight(dividerView.id, 1)
            constraintSet.constrainWidth(dividerView.id, ConstraintSet.MATCH_CONSTRAINT)
            //dividerView.setBackgroundColor(Color.RED)

            constraintSet.connect(dividerView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            constraintSet.connect(dividerView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

            val questionTextView = TextView(view.context)
            questionTextView.text = actBlockQuestionItem.text
            questionTextView.id = questionCounterForId
            questionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())

            constraintSet.constrainHeight(questionTextView.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(questionTextView.id, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.connect(questionTextView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, DEFAULT_MARGIN)
            if (questionCounterForId == 1) {
                /*Lista elején vagyunk */
                //  Log.d(TAG, "Kérdés id: $questionCounterForId  parnthez tesszük ")
                constraintSet.connect(questionTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, QUESTION_TOP_BOTTOM_MARGIN)
            } else {
                //Log.d(TAG, "Kérdés id: $questionCounterForId  dividerhez tesszük last divider id:${dividerView.id - 1} aktuális uj divider lesz : $dividerCounterForId ")
                constraintSet.connect(questionTextView.id, ConstraintSet.TOP, dividerView.id - 1, ConstraintSet.BOTTOM, QUESTION_TOP_BOTTOM_MARGIN)
            }

            layout.addView(questionTextView)
            /**
             * Type  0 : Normal
             *       1 : Likert*/
            if (actualSectionBlock.type == 0) {

                when (actBlockQuestionItem.type) {
                    QuestionnaireQuestionType.Text.type -> {
                        if (actBlockQuestionItem.answerMultiline) {

                            val questionMultiLineEditText = EditText(view.context)
                            questionMultiLineEditText.id = questionTypeCounterForId
                            if (uploadValue != null) {
                                questionMultiLineEditText.setText(uploadValue.toString())
                            }
                            questionMultiLineEditText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            questionMultiLineEditText.setSingleLine(false)
                            questionMultiLineEditText.isVerticalScrollBarEnabled = true
                            questionMultiLineEditText.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
                            questionMultiLineEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())

                            constraintSet.constrainHeight(questionMultiLineEditText.id, ConstraintSet.WRAP_CONTENT)
                            constraintSet.constrainWidth(questionMultiLineEditText.id, ConstraintSet.MATCH_CONSTRAINT)
                            constraintSet.connect(questionMultiLineEditText.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
                            constraintSet.connect(questionMultiLineEditText.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
                            constraintSet.connect(questionMultiLineEditText.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM)

                            constraintSet.connect(dividerView.id, ConstraintSet.TOP, questionMultiLineEditText.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)

                            layout.addView(questionMultiLineEditText)
                            typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.Text.type, questionMultiLineEditText.id))

                        } else {
                            val questionEditText = EditText(view.context)
                            questionEditText.hint = "Answer"
                            //disable enter code here
                            questionEditText.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_ENTER }
                            questionEditText.filters = arrayOf(LengthFilter(actBlockQuestionItem.answerMaxLength))
                            questionEditText.id = questionTypeCounterForId
                            questionEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
                            questionEditText.text
                            if (uploadValue != null) {
                                questionEditText.setText(uploadValue.toString())
                            }

                            constraintSet.constrainHeight(questionEditText.id, ConstraintSet.WRAP_CONTENT)
                            constraintSet.constrainWidth(questionEditText.id, ConstraintSet.MATCH_CONSTRAINT)
                            constraintSet.connect(questionEditText.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
                            constraintSet.connect(questionEditText.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
                            constraintSet.connect(questionEditText.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM)

                            constraintSet.connect(dividerView.id, ConstraintSet.TOP, questionEditText.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)

                            layout.addView(questionEditText)

                            typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.Text.type, questionEditText.id))
                        }
                        layout.addView(dividerView)
                    }
                    QuestionnaireQuestionType.SingleChoice.type -> {
                        // radio button
                        val radioGroup = RadioGroup(view.context)
                        radioGroup.id = questionTypeCounterForId
                        radioGroup.orientation = LinearLayout.VERTICAL
                        constraintSet.constrainHeight(radioGroup.id, ConstraintSet.WRAP_CONTENT)
                        constraintSet.constrainWidth(radioGroup.id, ConstraintSet.MATCH_CONSTRAINT)
                        constraintSet.connect(radioGroup.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
                        constraintSet.connect(radioGroup.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)

                        actBlockQuestionItem.answers.forEach {
                            if (it != null) {
                               // val imageView = ImageView(view.context)
                              //  imageView.id = singleChooseTypeImageViewCounterForId
                                // imageView.scaleType = ImageView.ScaleType.FIT_XY
                                //  imageView.adjustViewBounds = true

                                //if (it.image != null || it.image != "") {
                                  //  val imageString = it.image!!.replace("data:image/png;base64,", "")
                                 //   val base64 = Base64.decode(imageString, Base64.DEFAULT)
                                  ///  val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
                                   // imageView.setImageBitmap(bitmap)
                               // }

                                val radioButton = RadioButton(view.context)
                                radioButton.id = singleChooseTypeRadioButtonCounterForId
                                radioButton.text = it.text

                                if (uploadValue != null) {
                                    var doubleVal = uploadValue as Double
                                    if (doubleVal.toInt() == it.order) {
                                        radioButton.isChecked = true
                                    }
                                }

                                radioGroup.addView(radioButton)
                                //radioGroup.addView(imageView)

                                singleChooseTypeRadioButtonCounterForId++
                                singleChooseTypeImageViewCounterForId++
                            }
                        }

                        layout.addView(radioGroup)
                        constraintSet.connect(dividerView.id, ConstraintSet.TOP, radioGroup.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        layout.addView(dividerView)

                        typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.SingleChoice.type, radioGroup.id))
                    }
                    QuestionnaireQuestionType.MultipleChoice.type -> {

                        var checkBox: CheckBox? = null

                        actBlockQuestionItem.answers.forEach {
                            if (it != null) {
                                checkBox = CheckBox(view.context)
                               /* val imageView = ImageView(view.context)
                                imageView.id = multipleChoiceTypeImageViewCounterForId
                                imageView.maxWidth = 50
                                imageView.maxHeight = 50
                                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                                imageView.adjustViewBounds = true
                                constraintSet.constrainHeight(imageView.id, 50)
                                constraintSet.constrainWidth(imageView.id, 50)

                                if (it.image != null || it.image != "") {

                                    val imageString = it.image!!.replace("data:image/png;base64,", "")
                                    val base64 = Base64.decode(imageString, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
                                    imageView.setImageBitmap(bitmap)
                                }
*/

                 /*               constraintSet.connect(imageView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, DEFAULT_MARGIN)
                                layout.addView(imageView)
*/
                                checkBox!!.id = multipleChoiceTypeCounterForId

                                if (newMultipleChoice) {
                                    newMultipleChoice = false
                                    //első elemet a kérdés tetejéhez rendezzük
                                    constraintSet.connect(checkBox!!.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DEFAULT_MARGIN)
                                } else {
                                    constraintSet.connect(checkBox!!.id, ConstraintSet.TOP, checkBox!!.id - 1, ConstraintSet.BOTTOM, DEFAULT_MARGIN)
                                }
                                constraintSet.constrainHeight(checkBox!!.id, ConstraintSet.WRAP_CONTENT)
                                constraintSet.constrainWidth(checkBox!!.id, ConstraintSet.WRAP_CONTENT)
                                checkBox!!.text = it.text

                                val value = uploadValue
                                if (value != null) {
                                    val arrayDouble = value as ArrayList<Double>
                                    val intArrayValue = arrayDouble.map { it.toInt() }
                                    ArrayList<Int>(intArrayValue).forEach { valueItem ->
                                        if (valueItem == it.order) {
                                            checkBox!!.isChecked = true
                                        }
                                    }
                                }

                                constraintSet.connect(checkBox!!.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, CHECKBOX_MARGIN)
                              //  constraintSet.connect(checkBox!!.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM)

                                multipleChoiceTypeCounterForId++
                                multipleChoiceTypeImageViewCounterForId++

                                layout.addView(checkBox)

                                typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.MultipleChoice.type, checkBox!!.id))
                            }
                        }

                        newMultipleChoice = true
                        if (checkBox == null) {
                            // Log.d(TAG, "checkbox biz null")
                            constraintSet.connect(dividerView.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        } else {
                            //  Log.d(TAG, "checkbox biz nem null")
                            constraintSet.connect(dividerView.id, ConstraintSet.TOP, checkBox!!.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        }
                        layout.addView(dividerView)
                    }
                    QuestionnaireQuestionType.LikertScale.type -> {

                        val likertFirstAnswer = TextView(view.context)
                        likertFirstAnswer.text = actBlockQuestionItem.answers[0]!!.text
                        likertFirstAnswer.id = likertTypeCounterForId
                        likertFirstAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.toFloat())
                        constraintSet.constrainHeight(likertFirstAnswer.id, ConstraintSet.WRAP_CONTENT)
                        constraintSet.constrainWidth(likertFirstAnswer.id, ConstraintSet.WRAP_CONTENT)
                        likertFirstAnswer.setTextColor(Color.GRAY)

                        constraintSet.connect(likertFirstAnswer.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        constraintSet.connect(likertFirstAnswer.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 40)

                        layout.addView(likertFirstAnswer)

                        likertTypeCounterForId++

                        val firstImageView = ImageView(view.context)
                        firstImageView.id = likertTypeImageViewCounterForId
                        firstImageView.maxWidth = 50
                        firstImageView.maxHeight = 50
                        firstImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        firstImageView.adjustViewBounds = true
                        constraintSet.constrainHeight(firstImageView.id, 50)
                        constraintSet.constrainWidth(firstImageView.id, 50)

                        var imageString: String
                        if (actBlockQuestionItem.answers[0]?.image != null || actBlockQuestionItem.answers[0]?.image != "") {
                            imageString = actBlockQuestionItem.answers[0]!!.image!!.replace("data:image/png;base64,", "")
                            val base64 = Base64.decode(imageString, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
                            firstImageView.setImageBitmap(bitmap)
                        }

                        constraintSet.connect(firstImageView.id, ConstraintSet.TOP, likertFirstAnswer.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        constraintSet.connect(firstImageView.id, ConstraintSet.LEFT, likertFirstAnswer.id, ConstraintSet.LEFT, DIVIDER_MARGIN)
                        constraintSet.connect(firstImageView.id, ConstraintSet.RIGHT, likertFirstAnswer.id, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                        layout.addView(firstImageView)
                        likertTypeImageViewCounterForId++

                        val likertLastAnswer = TextView(view.context)
                        likertLastAnswer.text = if (actBlockQuestionItem.answers[actBlockQuestionItem.answers.lastIndex]!!.text.isNullOrEmpty()) "--" else actBlockQuestionItem.answers[actBlockQuestionItem.answers.lastIndex]!!.text
                        likertLastAnswer.id = likertTypeCounterForId
                        likertLastAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.toFloat())
                        likertLastAnswer.setTextColor(Color.GRAY)
                        constraintSet.constrainHeight(likertLastAnswer.id, ConstraintSet.WRAP_CONTENT)
                        constraintSet.constrainWidth(likertLastAnswer.id, ConstraintSet.WRAP_CONTENT)

                        constraintSet.connect(likertLastAnswer.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        constraintSet.connect(likertLastAnswer.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                        layout.addView(likertLastAnswer)
                        likertTypeCounterForId++

                        val lastImageView = ImageView(view.context)
                        lastImageView.id = likertTypeImageViewCounterForId
                        lastImageView.maxWidth = 50
                        lastImageView.maxHeight = 50
                        lastImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        lastImageView.adjustViewBounds = true
                        constraintSet.constrainHeight(lastImageView.id, 50)
                        constraintSet.constrainWidth(lastImageView.id, 50)


                        if (actBlockQuestionItem.answers[0]?.image != null || actBlockQuestionItem.answers[0]?.image != "") {
                            imageString = actBlockQuestionItem.answers[actBlockQuestionItem.answers.lastIndex]!!.image!!.replace("data:image/png;base64,", "")
                            val base64 = Base64.decode(imageString, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
                            lastImageView.setImageBitmap(bitmap)
                        }

                        constraintSet.connect(lastImageView.id, ConstraintSet.TOP, likertLastAnswer.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        constraintSet.connect(lastImageView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                        layout.addView(lastImageView)
                        likertTypeImageViewCounterForId++

                        val answers = Array<String>(actBlockQuestionItem.answers.size + 1) { "" }
                        answers[0] = 0.toString()
                        var j = 1
                        for (element in actBlockQuestionItem.answers) {
                            answers[j] = element!!.order.toString()
                            j++
                        }

                        val seekBar = MyTickSeekBar
                                .with(view.context)
                                .tickMarksSize(20)
                                .thumbSize(16)
                                .progressValueFloat(true)
                                .tickCount(actBlockQuestionItem.answers.size + 1)
                                .max(actBlockQuestionItem.answers.size.toFloat())
                                .tickTextsColor(Color.BLACK)
                                .tickMarksColor(Color.BLACK)
                                .thumbColor(if (actualSectionBlock.required) Color.RED else ContextCompat.getColor(view.context, R.color.colorPrimary))
                                .tickTextsArray(answers)
                                .thumbAutoAdjust(true)
                                .trackProgressColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                                .showTickMarksType(TickMarkType.DIVIDER)
                                .showTickTextsPosition(TextPosition.BELOW)
                                .build()

                        seekBar.id = likertTypeCounterForId

                        if (uploadValue != null) {
                            var doubleValue = uploadValue as Double
                            var intValue = doubleValue.toInt()
                            seekBar.setProgress(answers.indexOf(intValue.toString()).toFloat())

                            if (!answerLIKERTHelper.contains(seekBar.id)) {
                                /*beletesszük a listába*/
                                answerLIKERTHelper.put(seekBar.id,doubleValue.toInt())
                            } else {
                                /*ha bent van csak módosítjuk az értékét*/
                                answerLIKERTHelper[seekBar.id] = doubleValue.toInt()
                            }
                        }

                        constraintSet.constrainHeight(seekBar.id, ConstraintSet.WRAP_CONTENT)
                        constraintSet.constrainWidth(seekBar.id, ConstraintSet.MATCH_CONSTRAINT)

                        constraintSet.connect(seekBar.id, ConstraintSet.TOP, firstImageView.id, ConstraintSet.BOTTOM, 20)
                        constraintSet.connect(seekBar.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 40)
                        constraintSet.connect(seekBar.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 30)


                        seekBar.setOnSeekChangeListener(object : MyOnSeekChangeListener {
                            override fun onSeeking(seekParams: MySeekParams) {
                                Log.d(TAG, "progress Value : ${seekParams.progress}")
                                if (actualSectionBlock.required) {
                                    if (seekParams.progress == 0) {
                                        seekBar.thumbColor((Color.RED))
                                    } else {
                                        seekBar.thumbColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                                    }
                                }

                                if (!answerLIKERTHelper.contains(seekBar.id)) {
                                    /*beletesszük a listába*/
                                    answerLIKERTHelper.put(seekBar.id, seekParams.tickText.toInt())
                                } else {
                                    /*ha bent van csak módosítjuk az értékét*/
                                    answerLIKERTHelper[seekBar.id] = seekParams.tickText.toInt()
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: MyTickSeekBar) {}
                            override fun onStopTrackingTouch(seekBar: MyTickSeekBar) {}
                        })

                        layout.addView(seekBar)

                        likertTypeCounterForId++
                        constraintSet.connect(dividerView.id, ConstraintSet.TOP, seekBar.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                        layout.addView(dividerView)

                        typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.LikertScale.type, seekBar.id))
                    }
                }
            } else {
                if (actBlockQuestionItem.type == QuestionnaireQuestionType.LikertItem.type) {

                    val likertFirstAnswer = TextView(view.context)
                    likertFirstAnswer.text = actualSectionBlock.likertAnswers[questionCounterForId - 1]!!.text
                    likertFirstAnswer.id = likertITEMTypeCounterForId
                    likertFirstAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.toFloat())
                    constraintSet.constrainHeight(likertFirstAnswer.id, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainWidth(likertFirstAnswer.id, ConstraintSet.WRAP_CONTENT)
                    likertFirstAnswer.setTextColor(Color.GRAY)

                    constraintSet.connect(likertFirstAnswer.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                    constraintSet.connect(likertFirstAnswer.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, DIVIDER_MARGIN)

                    layout.addView(likertFirstAnswer)

                    likertITEMTypeCounterForId++

                    val firstImageView = ImageView(view.context)
                    firstImageView.id = likertTypeImageViewCounterForId
                    firstImageView.maxWidth = 50
                    firstImageView.maxHeight = 50
                    firstImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    firstImageView.adjustViewBounds = true
                    constraintSet.constrainHeight(firstImageView.id, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainWidth(firstImageView.id, ConstraintSet.WRAP_CONTENT)

                    constraintSet.connect(firstImageView.id, ConstraintSet.TOP, likertFirstAnswer.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                    constraintSet.connect(firstImageView.id, ConstraintSet.RIGHT, likertFirstAnswer.id, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                    layout.addView(firstImageView)
                    likertTypeImageViewCounterForId++

                    val likertLastAnswer = TextView(view.context)
                    likertLastAnswer.text = if (actualSectionBlock.likertAnswers[actualSectionBlock.likertAnswers.lastIndex]!!.text.isNullOrEmpty()) "--" else actualSectionBlock.likertAnswers[actualSectionBlock.likertAnswers.lastIndex]!!.text
                    likertLastAnswer.id = likertITEMTypeCounterForId
                    likertLastAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.toFloat())
                    likertLastAnswer.setTextColor(Color.GRAY)
                    constraintSet.constrainHeight(likertLastAnswer.id, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainWidth(likertLastAnswer.id, ConstraintSet.WRAP_CONTENT)

                    constraintSet.connect(likertLastAnswer.id, ConstraintSet.TOP, questionTextView.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                    constraintSet.connect(likertLastAnswer.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                    layout.addView(likertLastAnswer)
                    likertITEMTypeCounterForId++

                    val lastImageView = ImageView(view.context)
                    lastImageView.id = likertTypeImageViewCounterForId
                    lastImageView.maxWidth = 20
                    lastImageView.maxHeight = 20
                    lastImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    lastImageView.adjustViewBounds = true
                    constraintSet.constrainHeight(lastImageView.id, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainWidth(lastImageView.id, ConstraintSet.WRAP_CONTENT)

                    constraintSet.connect(lastImageView.id, ConstraintSet.TOP, likertLastAnswer.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                    constraintSet.connect(lastImageView.id, ConstraintSet.LEFT, likertLastAnswer.id, ConstraintSet.LEFT, DIVIDER_MARGIN)
                    constraintSet.connect(lastImageView.id, ConstraintSet.RIGHT, likertLastAnswer.id, ConstraintSet.RIGHT, DIVIDER_MARGIN)
                    layout.addView(lastImageView)
                    likertTypeImageViewCounterForId++

                    val answers = Array(actualSectionBlock.likertAnswers.size + 1) { "" }
                    answers[0] = 0.toString()
                    var j = 1
                    for (element in actualSectionBlock.likertAnswers) {
                        answers[j] = element!!.value.toString()
                        j++
                    }

                    val seekBarLikertItem = MyTickSeekBar
                            .with(view.context)
                            .progressValueFloat(true)
                            .tickMarksSize(20)
                            .thumbColor(if (actualSectionBlock.required) Color.RED else ContextCompat.getColor(view.context, R.color.colorPrimary))
                            .tickCount(actualSectionBlock.likertAnswers.size + 1)
                            .max(actualSectionBlock.likertAnswers.size.toFloat())
                            .tickTextsColor(Color.BLACK)
                            .tickMarksColor(Color.BLACK)
                            .thumbAutoAdjust(true)
                            .tickTextsArray(answers)
                            .thumbSize(16)
                            .trackProgressColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                            .showTickMarksType(TickMarkType.DIVIDER)
                            .showTickTextsPosition(TextPosition.BELOW)
                            .build()

                    seekBarLikertItem.id = likertITEMTypeCounterForId

                    if (uploadValue != null) {
                        var doubleValue = uploadValue as Double
                        var intValue = doubleValue.toInt()
                        seekBarLikertItem.setProgress(answers.indexOf(intValue.toString()).toFloat())
                        if (!answerLIKERTHelper.contains(seekBarLikertItem.id)) {
                            /*beletesszük a listába*/
                            answerLIKERTHelper.put(seekBarLikertItem.id,doubleValue.toInt())
                        } else {
                            /*ha bent van csak módosítjuk az értékét*/
                            answerLIKERTHelper[seekBarLikertItem.id] = doubleValue.toInt()
                        }

                    }

                    constraintSet.constrainHeight(seekBarLikertItem.id, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainWidth(seekBarLikertItem.id, ConstraintSet.MATCH_CONSTRAINT)

                    constraintSet.connect(seekBarLikertItem.id, ConstraintSet.TOP, firstImageView.id, ConstraintSet.BOTTOM, 20)
                    constraintSet.connect(seekBarLikertItem.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 40)
                    constraintSet.connect(seekBarLikertItem.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 30)


                    seekBarLikertItem.setOnSeekChangeListener(object : MyOnSeekChangeListener {
                        override fun onSeeking(seekParams: MySeekParams) {

                            if (actualSectionBlock.required) {
                                if (seekParams.progress == 0) {
                                    seekBarLikertItem.thumbColor((Color.RED))
                                } else {
                                    seekBarLikertItem.thumbColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                                }
                            }

                            if (!answerLIKERTHelper.contains(seekBarLikertItem.id)) {
                                /*beletesszük a listába*/
                                answerLIKERTHelper.put(seekBarLikertItem.id, seekParams.tickText.toInt())
                            } else {
                                /*ha bent van csak módosítjuk az értékét*/
                                answerLIKERTHelper[seekBarLikertItem.id] = seekParams.tickText.toInt()
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: MyTickSeekBar) {}
                        override fun onStopTrackingTouch(seekBar: MyTickSeekBar) {}
                    })

                    layout.addView(seekBarLikertItem)

                    likertITEMTypeCounterForId++
                    constraintSet.connect(dividerView.id, ConstraintSet.TOP, seekBarLikertItem.id, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
                    layout.addView(dividerView)

                   typeAndAnswerViewIDPairs.add(Pair(QuestionnaireQuestionType.LikertItem.type, seekBarLikertItem.id))
                }
            }

            if (questionCounterForId == actualSectionBlock.questions.size) {
                constraintSet.connect(dividerView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, DIVIDER_MARGIN)
            }

            val copyTypeAndAnswerViewIDPairs = mutableListOf<Pair<Int, Int>>()
            copyTypeAndAnswerViewIDPairs.addAll(typeAndAnswerViewIDPairs)

            //  Log.e(TAG,"válasz typeAndAnswerViewIDPairs: $typeAndAnswerViewIDPairs")
            answerListHelper.put(questionCounterForId, copyTypeAndAnswerViewIDPairs)

            typeAndAnswerViewIDPairs.clear()

            questionCounterForId++
            dividerCounterForId++
            questionTypeCounterForId++
        }

        constraintSet.applyTo(layout)
    }

    override fun onStart() {
        if (actualSectionBlock.required) {
            answerListHelper.forEach {

                var value: Int

                /*az it értéke a mutable list melynek az első eleme az adott kérdéstípusa */
                when (it.value[0].first) {
                    QuestionnaireQuestionType.LikertScale.type -> {
                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        val viewS = view!!.findViewById<MyTickSeekBar>(it.value[0].second)
                        value = viewS.progress
                        Log.e(TAG, "value LikertScale:  $value ${viewS.id}")
                        if (value == 0) {
                            viewS.thumbColor((Color.RED))
                        } else {
                            viewS.thumbColor(ContextCompat.getColor(view!!.context, R.color.colorPrimary))
                        }
                    }
                    QuestionnaireQuestionType.LikertItem.type -> {
                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        val viewS = view!!.findViewById<MyTickSeekBar>(it.value[0].second)
                        value = viewS.progress
                        Log.e(TAG, "value LikertItem: $value ${viewS.id}")
                        if (value == 0) {
                            viewS.thumbColor((Color.RED))
                        } else {
                            viewS.thumbColor(ContextCompat.getColor(view!!.context, R.color.colorPrimary))
                        }
                    }
                }
            }
        }
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

    }

  /*  @Synchronized
    private fun getFreshModel() {
        var serverConnTimeout = true
        val gson = Gson()
        val params = arrayOf("")
        params[0] = questionnaireResultListFillableElement.id

        meteor.call("questionnaire-result.get", params, object : ResultListener {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "questionnaire-result.get  ${result}")
                val REVIEW_TYPE2 = object : TypeToken<QuestionnaireResultListFillable>() {}.type

                questionnaireResultListFillableElement = gson.fromJson(result, REVIEW_TYPE2)
                //  val questionnaireObejtTesztMiatt2 = gson.toJson(questionnaireResultListFillableElement, REVIEW_TYPE2)
                //  Log.d(TAG, "questionnaireObejtTesztMiatt - getFreshModel  ${questionnaireObejtTesztMiatt2} \n\n\n")

                update()
                serverConnTimeout = false
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "-->error $error ")
                Log.d(TAG, "-->error $reason ")
                Log.d(TAG, "-->error $details ")
                Log.d(TAG, "-->error BAJ VAN EZ NEM FORDULHAT ELŐ ILYENKORRA MÁR KELL LEGYEN EGY CREATE!!! ")
            }
        })

        Handler().postDelayed({
            if(serverConnTimeout){
                Snackbar.make(view!!, "Server is not available!", Snackbar.LENGTH_LONG).show()
            }
        },2500)
    }

    @Synchronized
    private fun update() {
        var sectionArray = ArrayList<QuestionnaireResultSection>()

        questionnaireResultListFillableElement.sections.forEach { qResultGetSectionItem ->
            /**A szekció order sorszáma nem egyenlő a szekciós tömb indexével ezért kell korrigálni 1-el */
            Log.d(DATALOADTAG, "Szekció: ${qResultGetSectionItem.order}")

            val uploadAnswers = ArrayList<QuestionnaireResultAnswer>()
/**Betöltjük ami eddig a modelben van*/
            questionnairegetObject.template.sections.forEach {
                if (it.order == qResultGetSectionItem.order) {
                    it.questions.forEach { qGetTempSecQuestionItem ->
                        Log.d(DATALOADTAG, "kérdés sorszáma: ${qGetTempSecQuestionItem.order} típusa")
                        when (qGetTempSecQuestionItem.type) {
                            QuestionnaireQuestionType.Text.type -> {
                                Log.d(DATALOADTAG, "Text value: ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value} ")
                                /**a kérdés sorszámával megegyező válasz indexe kell*/
                                val answer = QuestionnaireResultAnswerStringValue(
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value.toString()
                                )
                                uploadAnswers.add(answer)
                            }
                            QuestionnaireQuestionType.SingleChoice.type -> {
                                Log.d(DATALOADTAG, "SingleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                /**a kérdés sorszámával megegyező válasz indexe kell*/
                                val answer = QuestionnaireResultAnswerIntValue(
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                        (value as Double).toInt()
                                )
                                uploadAnswers.add(answer)
                            }
                            QuestionnaireQuestionType.MultipleChoice.type -> {
                                Log.d(DATALOADTAG, "MultipleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")

                                val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                val arrayDouble = value as ArrayList<Double>

                                val intArrayValue = arrayDouble.map { it.toInt() }

                                /**a kérdés sorszámával megegyező válasz indexe kell*/
                                val answer = QuestionnaireResultAnswerArrayValue(
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                        ArrayList<Int>(intArrayValue)
                                )
                                uploadAnswers.add(answer)
                            }
                            QuestionnaireQuestionType.LikertScale.type -> {
                                Log.d(DATALOADTAG, "LikertScale ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                /**a kérdés sorszámával megegyező válasz indexe kell*/
                                val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                val answer = QuestionnaireResultAnswerIntValue(
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                        (value as Double).toInt()
                                )
                                uploadAnswers.add(answer)
                            }
                            QuestionnaireQuestionType.LikertItem.type -> {
                                Log.d(DATALOADTAG, "LikertItem ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                /**a kérdés sorszámával megegyező válasz indexe kell*/
                                val answer = QuestionnaireResultAnswerIntValue(
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                        qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                        (value as Double).toInt()
                                )
                                uploadAnswers.add(answer)
                            }
                        }
                    }
                }
            }
//*modell visszatöltés eddig*//


            val section = QuestionnaireResultSection(
                    qResultGetSectionItem.order,
                    qResultGetSectionItem.type,
                    uploadAnswers,
                    qResultGetSectionItem.startedAt?.date,
                    qResultGetSectionItem.finishedAt?.date
            )

            sectionArray.add(section)
        }
        /**
         * A kérdőív kitöltésének megszakításának és folytatásának időpontjai.
         *
         * Alapeset: Egy felfüggesztés-, majd egy folytatás-jelzés érkezik.
         *           Ekkor a felfüggesztés jelre létrejön egy új elem a tömbben
         *           kitöltve a 'suspendedAt' értékét, majd a folytatás jelzésre
         *           az utolsó tömb-elem 'resumedAt' értéke kerül kitöltésre.
         * Két felfüggesztés egymás után:
         *           !!! Kivétel - látható legyen, hogy már fel van függesztve
         * Két folytatás esemény jön egymás után:
         *           !!! Kivétel - látható legyen, hogy már újra van indítva
         */
        val suspendArray = ArrayList<QuestionnaireResultSuspend>()
        val subSus = QuestionnaireResultSuspend(null, null)
        suspendArray.add(subSus)

        val answers = ArrayList<QuestionnaireResultAnswer>()

        answerListHelper.forEach { answerListHelperItem ->

            var value: Any? = null
            val qNumber = answerListHelperItem.key
            var valueType: Int = -1

          //  val qTextView = view!!.findViewById<TextView>(qNumber)

            /*az it értéke a mutable list melynek az első eleme az adott kérdéstípusa */
            when (answerListHelperItem.value[0].first) {
                QuestionnaireQuestionType.Text.type -> {

                    Log.e(TAG, "Adatellenőrzések: Text")
                    /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                    val viewT = view!!.findViewById<EditText>(answerListHelperItem.value[0].second)
                    value = viewT.text
                    valueType = AnswerType.STRING.type
                    Log.e(TAG, "Adatellenőrzések: Text value :$value")

                   /* if (actualSectionBlock.required && (value.isNullOrEmpty() || value == "" || value == " " || value == "  " || value == "  ")) {
                        Snackbar.make(qTextView, "All fields are required!", Snackbar.LENGTH_LONG).show()
                        return
                    }*/
                }
                QuestionnaireQuestionType.SingleChoice.type -> {
                    Log.e(TAG, "Adatellenőrzések: SingleChoice radiogroup :${answerListHelperItem.value[0].second}")

                    /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                    val viewRadio = view!!.findViewById<RadioGroup>(answerListHelperItem.value[0].second)

                    var i = 1
                    viewRadio.forEach {
                        try {
                            val radiobutton = view!!.findViewById<RadioButton>(it.id)
                            if (radiobutton != null) {
                                if (radiobutton.isChecked) {
                                    value = i
                                    valueType = AnswerType.INT.type
                                    //  return /**Egyetlen elem választható szóval nem kell tovább nézni*/
                                }
                                i++
                            } else {
                                value = 0 //nem választott semmit
                                valueType = AnswerType.INT.type
                                i++
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Semmi gond csak egy imageview a radioGroupban , nem érdekes.")
                        }
                    }

               /*     if (actualSectionBlock.required && (value == null || value == 0)) {
                        Snackbar.make(qTextView, "All fields are required! $qNumber. question", Snackbar.LENGTH_LONG).show()
                        return
                    }*/
                }
                QuestionnaireQuestionType.MultipleChoice.type -> {
                    Log.e(TAG, "Adatellenőrzések: MultipleChoice")
                    val multiValue = ArrayList<Int>()

                    for (i in 0 until answerListHelperItem.value.size) {
                        /*csak a secoundal dolgozunk az a view id!*/
                        val viewCh = view!!.findViewById<CheckBox>(answerListHelperItem.value[i].second)
                        if (viewCh.isChecked) {
                            multiValue.add(actualSectionBlock.questions[qNumber - 1].answers[i]!!.order)
                        }
                    }
                    value = multiValue
                    valueType = AnswerType.ARRAY.type

                  /*  if (actualSectionBlock.required && (value == null || multiValue.size == 0)) {
                        Snackbar.make(qTextView, "All fields are required!", Snackbar.LENGTH_LONG).show()
                        return
                    }*/
                }
                QuestionnaireQuestionType.LikertScale.type -> {
                    Log.e(TAG, "Adatellenőrzések: LikertScale")
                    /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                    value = if (answerLIKERTHelper[answerListHelperItem.value[0].second] == null) 0 else answerLIKERTHelper[answerListHelperItem.value[0].second]
                    Log.e(TAG, "value: LikertScale savere :$value")

                    valueType = AnswerType.INT.type

                  /*  if (actualSectionBlock.required && (value == null || value == 0)) {
                        Snackbar.make(qTextView, "All fields are required!", Snackbar.LENGTH_LONG).show()
                        return
                    }*/
                }
                QuestionnaireQuestionType.LikertItem.type -> {
                    Log.e(TAG, "Adatellenőrzések: LikertItem")
                    /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                    value = if (answerLIKERTHelper[answerListHelperItem.value[0].second] == null) 0 else answerLIKERTHelper[answerListHelperItem.value[0].second]
                    Log.e(TAG, "value: LikertItem savere :$value")
                    valueType = AnswerType.INT.type

                   /* if (actualSectionBlock.required && (value == null || value == 0)) {
                        Log.e(TAG, "value: LikertItem savere :$value")
                        Snackbar.make(qTextView, "All fields are required!", Snackbar.LENGTH_LONG).show()
                        return
                    }*/
                }
            }

            if (value != null) {

                when (valueType) {
                    AnswerType.INT.type -> {
                        val qRAnswer = QuestionnaireResultAnswerIntValue(
                                /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                questionnaireResultListFillableElement.startedAt?.date ?: startDate,
                                System.currentTimeMillis(),
                                value!! as Int
                        )
                        answers.add(qRAnswer)
                    }
                    AnswerType.STRING.type -> {
                        val qRAnswer = QuestionnaireResultAnswerStringValue(
                                /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                questionnaireResultListFillableElement.startedAt?.date ?: startDate,
                                System.currentTimeMillis(),// QuestionnaireResultAnswerFinishedAt(null),
                                value!!.toString()
                        )
                        answers.add(qRAnswer)
                    }
                    AnswerType.ARRAY.type -> {
                        val qRAnswer = QuestionnaireResultAnswerArrayValue(
                                /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                questionnaireResultListFillableElement.startedAt?.date ?: startDate,
                                System.currentTimeMillis(),// QuestionnaireResultAnswerFinishedAt(null),
                                value!! as ArrayList<Int>
                        )
                        answers.add(qRAnswer)
                    }
                }
            }
        }

        if (answers.size != 0) {
            val qRSection = QuestionnaireResultSection(
                    actualSectionBlock.order,
                    actualSectionBlock.type,
                    answers,
                    questionnaireResultListFillableElement.startedAt?.date ?: startDate,
                    System.currentTimeMillis()//QuestionnaireResultAnswerFinishedAt(null)
            )

            var mustAdd = true

            for (i in 0 until sectionArray.size) {
                if (sectionArray[i].order == actualSectionBlock.order) {
                    sectionArray[i] = qRSection
                    mustAdd = false
                }
            }

            if (mustAdd) {
                sectionArray.add(qRSection)
            }

            val userId = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_USERID, null)

            if (!userId.isNullOrEmpty()) {
                questionnaireResultListFillableElement.userId = userId

                questionnaireResultForServer = QuestionnaireResultForServer(
                        questionnaireResultListFillableElement.seq,
                        questionnaireResultListFillableElement.creator,
                        questionnaireResultListFillableElement.createdAt.date,
                        questionnaireResultListFillableElement.modifier,
                        questionnaireResultListFillableElement.modifiedAt?.date,
                        questionnaireResultListFillableElement.active,
                        questionnaireResultListFillableElement.id,
                        questionnaireResultListFillableElement.questionnaireId,
                        questionnaireResultListFillableElement.userId,
                        questionnaireResultListFillableElement.scheduleOrder,
                        questionnaireResultListFillableElement.scheduledAt?.date,//questionnaireResultListFillableElement.scheduledAt!!.date,
                        questionnaireResultListFillableElement.scheduledUntil?.date,
                        questionnaireResultListFillableElement.state,
                        questionnaireResultListFillableElement.startedAt!!.date,
                        questionnaireResultListFillableElement.finishedAt?.date,
                        sectionArray,
                        suspendArray
                )

                if (actualSectionBlock.order == questionnaireResultListFillableElement.sections[questionnaireResultListFillableElement.sections.lastIndex].order) {
                    /*elméletileg ez az utolsó section a kérdőíven*/
                    finishbutton.visibility = View.VISIBLE
                }

                //val deCodeId = ObjectId(questionareObject._id!!._idObj_idField.binary.toByteArray())

                val params = Array<Any>(3) {}
                params[0] = questionnaireResultListFillableElement.id
                params[1] = questionnaireResultListFillableElement.seq
                params[2] = questionnaireResultForServer

                Log.d(TAG, "questionnaire-result.update params:  ${params[0]}  ${params[1]}  ${params[2]} ")

                val gson = Gson()
                val REVIEW_TYPE = object : TypeToken<QuestionnaireResultForServer>() {}.type
                val questionnaireUPDATE = gson.toJson(questionnaireResultForServer, REVIEW_TYPE)
                Log.d(TAG, "questionnaireUPDATE  ${questionnaireUPDATE} ")

                meteor.call("questionnaire-result.update", params, object : ResultListener {
                    override fun onSuccess(result: String?) {

                        Snackbar.make(view!!, "Saved!", Snackbar.LENGTH_LONG).show()
                        //   Log.d(TAG, " questionnaire-result.update -->result ${if(result.isNullOrEmpty()) "null vagy üres a result" else result} ")
                        val REVIEW_TYPE_UPDATE = object : TypeToken<QuestionnaireResultUpdateModel>() {}.type
                        val gson = Gson()
                        val updateResult: QuestionnaireResultUpdateModel = gson.fromJson(result, REVIEW_TYPE_UPDATE)

                        questionnaireResultForServer.seq = updateResult.seq
                        questionnaireResultForServer.creator = updateResult.creator
                        questionnaireResultForServer.created_at = updateResult.created_at
                        questionnaireResultForServer.modifier = updateResult.modifier
                        questionnaireResultForServer.modified_at = updateResult.modified_at
                        questionnaireResultForServer.active = updateResult.active
                        // questionnaireResultForServer.id = updateResult.data.id?  update/data -ban nem jön
                        questionnaireResultForServer.questionnaireId = updateResult.questionnaireId
                        questionnaireResultForServer.userId = updateResult.userId
                        questionnaireResultForServer.scheduleOrder = updateResult.scheduleOrder
                        questionnaireResultForServer.scheduledAt = updateResult.scheduledAt?.date
                        questionnaireResultForServer.scheduledUntil = updateResult.scheduledUntil?.date
                        questionnaireResultForServer.state = updateResult.state
                        questionnaireResultForServer.startedAt = updateResult.startedAt?.date
                        questionnaireResultForServer.finishedAt = updateResult.finishedAt?.date
                        //nem kell bántani ugyan az amit beküldtünk questionnaireResultForServer.sections = updateResult.data.sections
                        questionnaireResultForServer.suspends = updateResult.suspends

                        /**Mire ide elérünk minden szükséges adatnak kell legyen értéke. Ezért megjelöljük hogy ez a szekció készen van */
                        // akár hányszor nyomható  save mert felülírja a friss adatokkal.
                     //   sectionsFinished[actualSectionBlock.order] = OwnModel(true, questionnaireResultForServer)
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAG, "questionnaire-result.update -->error $error ")
                        Log.d(TAG, "questionnaire-result.update -->error $reason ")
                        Log.d(TAG, "questionnaire-result.update -->error $details ")
                    }
                })
            } else {
                Log.e(TAG, "USERID is null !!!!!!!!!!!!!!! $userId ")
                Toast.makeText(view!!.context, "User information is empty!", Toast.LENGTH_LONG).show()
            }
        }
    }*/

   /* fun updateSection() {
        meteor = if (EuronetMeteorSingleton.hasInstance()) {
            EuronetMeteorSingleton.getInstance()
        } else {
            EuronetMeteorSingleton.createInstance(view!!.context, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
        }

        if (meteor.isConnected) {
            getFreshModel()
            Log.e(TAG, " updateSection")
        }
    }*/

    fun getAnswerHelper() : MutableMap<Int, MutableList<Pair<Int, Int>>>{
        return answerListHelper
    }

    fun getAnswerLIKERTHelper() : MutableMap<Int, Int>{
        return answerLIKERTHelper
    }

 /*   fun getModel(): QuestionnaireResultForServer {
        return questionnaireResultForServer
    }*/
}
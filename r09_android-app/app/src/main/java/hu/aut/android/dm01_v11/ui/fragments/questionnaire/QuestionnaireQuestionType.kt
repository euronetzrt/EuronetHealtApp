package hu.aut.android.dm01_v11.ui.fragments.questionnaire

enum class QuestionnaireQuestionType (val type: Int) {
    Text (0),
    SingleChoice (1),
    MultipleChoice(2),
    LikertScale(3),
    LikertItem(4)
}
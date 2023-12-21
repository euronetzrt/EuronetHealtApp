package hu.euronetrt.okoskp.euronethealth.questionnaire

enum class QuestionnaireType(val type: Int) {
    QUESTIONNAIRE_RESULT_LISTFILLABLE (111),
    QUESTIONNAIRE_GET (222),
    QUESTIONNAIRE_TEMPLATE_GET (333)
}
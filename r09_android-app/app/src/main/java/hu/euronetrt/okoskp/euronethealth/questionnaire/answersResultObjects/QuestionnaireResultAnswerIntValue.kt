package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

class QuestionnaireResultAnswerIntValue (
        questionOrder: Int,
        startedAt: Long?, //QuestionnaireResultAnswerStartedAt?,
        finishedAt : Long?, //QuestionnaireResultAnswerFinishedAt?,
        var value : Int
) : QuestionnaireResultAnswer(questionOrder,
        startedAt,
        finishedAt
)
package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

class QuestionnaireResultAnswerArrayValue(
        questionOrder: Int,
        startedAt: Long?,//QuestionnaireResultAnswerStartedAt?,
        finishedAt : Long?,//QuestionnaireResultAnswerFinishedAt?,
        var value : ArrayList<Int>
) : QuestionnaireResultAnswer(questionOrder,
        startedAt,
        finishedAt
        )
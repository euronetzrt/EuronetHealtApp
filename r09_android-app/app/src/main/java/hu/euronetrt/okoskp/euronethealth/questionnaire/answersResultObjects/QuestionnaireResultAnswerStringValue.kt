package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

class QuestionnaireResultAnswerStringValue (
        questionOrder: Int,
        startedAt: Long?, //QuestionnaireResultAnswerStartedAt?,
        finishedAt :Long?,// QuestionnaireResultAnswerFinishedAt?,
        var value : String
) : QuestionnaireResultAnswer(questionOrder,
        startedAt,
        finishedAt
)
package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

abstract class QuestionnaireResultAnswer {
    /**
     * Egy kérdésre adott válasz sorszáma.
     * - kötelező
     * - típusa integer
     * - meg kell, hogy egyezzen a kapcsolódo kéréds sorszámával (order)!
     */
     var questionOrder: Int

    /**
     * A kérdőív-válasz kitöltés kezdetének időpontja.
     * - 'Finished' státusz esetén kötelező
     */
    var startedAt: Long?//QuestionnaireResultAnswerStartedAt?
    /**
     * A kérdőív-válasz kitöltés befejezésének időpontja.
     * - 'Finished' státusz esetén kötelező
     * - nem lehet kisebb, mint a startedAt
     */
    var finishedAt: Long?//QuestionnaireResultAnswerFinishedAt?

    constructor(questionOrder: Int,
                startedAt: Long?,//QuestionnaireResultAnswerStartedAt?,
                finishedAt: Long?){//QuestionnaireResultAnswerFinishedAt?){
        this.questionOrder = questionOrder
        this.startedAt = startedAt
        this.finishedAt = finishedAt
    }
}
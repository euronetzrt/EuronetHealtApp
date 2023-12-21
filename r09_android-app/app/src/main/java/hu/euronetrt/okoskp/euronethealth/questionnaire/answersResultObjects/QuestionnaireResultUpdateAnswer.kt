package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

class QuestionnaireResultUpdateAnswer(
        /**
         * Egy kérdésre adott válasz sorszáma.
         * - kötelező
         * - típusa integer
         * - meg kell, hogy egyezzen a kapcsolódo kéréds sorszámával (order)!
         */
        var questionOrder: Int,
        /**
         * A kérdőív-válasz kitöltés kezdetének időpontja.
         * - 'Finished' státusz esetén kötelező
         */
        var startedAt: QuestionnaireResultAnswerStartedAt?,
        /**
         * A kérdőív-válasz kitöltés befejezésének időpontja.
         * - 'Finished' státusz esetén kötelező
         * - nem lehet kisebb, mint a startedAt
         */
        var finishedAt: QuestionnaireResultAnswerFinishedAt?,

        var value: Any?)
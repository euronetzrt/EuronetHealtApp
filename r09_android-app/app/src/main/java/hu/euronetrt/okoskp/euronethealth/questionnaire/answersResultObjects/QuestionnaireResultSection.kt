package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

class QuestionnaireResultSection(
        /**
         * A kitöltött kérdőív-szekció sorszáma. Meg kell, hogy egyezzen a sablon
         * megfelelő szekciójának sorszámával (order).
         * - kötelező
         * - típusa integer
         * - 0-nál nagyobb
         * - kérdőív-kitöltésenként egyedinek kell lennie
         * - a sablonban kell ilyen order értékű szekciónak lennie
         */
        var order: Int,

        /**
         * - kötelező (lehet 0)
         * - típusa integer
         * - a kapcsolódó kérdőív típusával meg kell egyezzen
         */
        var type: Int,

        /**
         * A szekcióban lévő kérdésekre adott válaszok.
         * - kötelező
         * - 'Finished' státusz esetén nem lehet üres
         * - 'Finished' státusz esetén elemszámának meg kell egyezzen a kapcsolódó
         *   sablon-szekcióban lévő kérdések számával
         */
        var answers: ArrayList<QuestionnaireResultAnswer>,

        /**
         * A kérdőív-szakasz kitöltés kezdetének időpontja.
         * - 'Finished' státusz esetén kötelező
         */
        var startedAt: Long?,//QuestionnaireResultAnswerStartedAt?,
        /**
         * A kérdőív-szakasz kitöltés befejezésének időpontja.
         * - 'Finished' státusz esetén kötelező
         * - nem lehet kisebb, mint a startedAt
         */
        var finishedAt:Long?// QuestionnaireResultAnswerFinishedAt?
)
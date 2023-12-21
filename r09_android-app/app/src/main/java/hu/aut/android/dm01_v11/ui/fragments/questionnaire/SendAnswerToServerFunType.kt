package hu.aut.android.dm01_v11.ui.fragments.questionnaire

 enum class SendAnswerToServerFunType(val type : Int){
     /** A kérdőív-kitöltéshez szükséges adastruktúra elkészült. */
     CREATED (0),
     /** A kliens jelezte, hogy a kitöltés lezárult. */
     UPDATE (1)
 }
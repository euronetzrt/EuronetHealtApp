package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class SchedulesModel(
        @SerializedName("order")
        var order: Int,

        @SerializedName("year")
        var year: String,

        @SerializedName("month")
        var month: String,

        @SerializedName("day")
        var day: String,

        @SerializedName("dayOfWeek")
        var dayOfWeek: String,

        @SerializedName("hour")
        var hour: String,

        @SerializedName("minute")
        var minute: String,

        @SerializedName("requiredAlways")
        var requiredAlways: Boolean,

        @SerializedName("activeTime")
        var activeTime: Int
)
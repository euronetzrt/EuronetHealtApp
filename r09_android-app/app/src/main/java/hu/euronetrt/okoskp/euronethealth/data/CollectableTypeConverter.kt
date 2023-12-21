package hu.euronetrt.okoskp.euronethealth.data

import androidx.room.TypeConverter

class CollectableTypeConverter {
        @TypeConverter
        fun getCollectableType(type : Int) : CollectableType{
            return CollectableType.values().associateBy(CollectableType::type).getValue(type)
        }
        @TypeConverter
        fun getCollectableTypeInt(type : CollectableType) : Int {
            return type.type
        }
}
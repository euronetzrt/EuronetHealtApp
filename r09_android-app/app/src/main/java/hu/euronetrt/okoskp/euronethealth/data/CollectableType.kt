package hu.euronetrt.okoskp.euronethealth.data

/**
 * Collectable Type
 *
 * @property type
 */
enum class CollectableType (val type: Int) {
    IBI (2),
    PPG (3),
    IMU(4),
    STEP(5),
    OTHER(7),
    HEARTRATE(9999)
}

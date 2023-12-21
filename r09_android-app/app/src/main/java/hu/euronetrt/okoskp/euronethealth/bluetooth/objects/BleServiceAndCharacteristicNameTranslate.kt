package hu.euronetrt.okoskp.euronethealth.bluetooth.objects

import android.util.Log
import java.util.*

object BleServiceAndCharacteristicNameTranslate {

    /*
     * Szerviz és karakterisztika listák létrehozása
     */
    private var servicesListNameTranslate = mutableMapOf<UUID, String>()
    private var characteristicsListNameTranslate = mutableMapOf<UUID, String>()

    /*
     * 16 bites UUID szervizekből létrehozunk 128 bites UUID -ket mivel a dokumentáció és
     * a használt UUID nem egységes!
     */

    private var BATTERY_SERVICE_UUID: UUID = convertFromInteger(0x180F)
    private var GENERIC_ATTRIBUTE_SERVICE_UUID: UUID = convertFromInteger(0x1801)
    private var GENERIC_ACCESS_SERVICE_UUID: UUID = convertFromInteger(0x1800)
    private var ALERT_NOTIFICATION_SERVICE_UUID: UUID = convertFromInteger(0x1811)
    private var BLOOD_PRESSURE_SERVICE_UUID: UUID = convertFromInteger(0x1810)
    private var CURRENT_TIME_SERVICE_UUID: UUID = convertFromInteger(0x1805)
    private var DEVICE_INFORMATION_SERVICE_UUID: UUID = convertFromInteger(0x180A)
    private var FITNESS_MACHINE_SERVICE_UUID: UUID = convertFromInteger(0x1826)
    private var HEALTH_THERMOMETER_SERVICE_UUID: UUID = convertFromInteger(0x1809)
    private var HEART_RATE_SERVICE_UUID: UUID = convertFromInteger(0x180D)
    private var HUMAN_INTERFACE_DEVICE_SERVICE_UUID: UUID = convertFromInteger(0x1812)
    private var PULSE_OXIMETER_SERVICE_UUID: UUID = convertFromInteger(0x1822)
    private var BODY_COMPOSITION_SERVICE_UUID: UUID = convertFromInteger(0x181B)
    private var SECURE_DFU_SERVICE_UUID: UUID = convertFromInteger(0xFE59)
    private var NORDIC_UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")

    /*
     * Szerviz nevek létrehozása
     */
    private val BATTERY_SERVICE: String = "Battery Service"
    private val GENERIC_ATTRIBUTE_SERVICE: String = "Generic Attribute Service"
    private val GENERIC_ACCESS_SERVICE: String = "Generic Access Service"
    private val ALERT_NOTIFICATION: String = "Alert Notification Service"
    private val BLOOD_PRESSURE: String = "Blood Pressure Service"
    private val CURRENT_TIME_SERVICE: String = "Current Time Service"
    private val DEVICE_INFORMATION: String = "Device Information Service"
    private val FITNESS_MACHINE: String = "Fitness Machine Service"
    private val HEALTH_THERMOMETER: String = "Health Thermometer Service"
    private val HEART_RATE: String = "Heart Rate Service"
    private val HUMAN_INTERFACE_DEVICE: String = "Human Interface Device Service"
    private val PULSE_OXIMETER_SERVICE: String = "Pulse Oximeter Service"
    private val BODY_COMPOSITION: String = "Body Composition Service"
    private var SECURE_DFU_SERVICE: String = "Secure DFU Service"
    private var NORDIC_UART_SERVICE: String = "Nordic UART Service"

    /*
     * 16 bites UUID karakterisztikákból létrehozunk 128 bites UUID -ket mivel a dokumentáció és
     * a használt UUID nem egységes!
     */
    private var BATTERY_LEVEL_CHAR_UUID: UUID = convertFromInteger(0x2A19)
    private var SERVICE_CHANGED_CHAR_UUID: UUID = convertFromInteger(0x2A05)
    private var DEVICE_NAME_CHAR_UUID: UUID = convertFromInteger(0x2A00)
    private var PERIPHERAL_PRIVACY_FLAG_CHAR_UUID: UUID = convertFromInteger(0x2A02)
    private var RECONNECTION_ADDRESS_CHAR_UUID: UUID = convertFromInteger(0x2A03)
    private var PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_CHAR_UUID: UUID = convertFromInteger(0x2A04)
    private var SUPPORTED_NEW_ALERT_CATEGORY_CHAR_UUID: UUID = convertFromInteger(0x2A47)
    private var NEW_ALERT_CHAR_UUID: UUID = convertFromInteger(0x2A46)
    private var SUPPORTED_UNREAD_ALERT_CATEGORY_CHAR_UUID: UUID = convertFromInteger(0x2A48)
    private var UNREAD_ALERT_STATUS_CHAR_UUID: UUID = convertFromInteger(0x2A45)
    private var ALERT_NOTIFICATION_CONTROL_POINT_CHAR_UUID: UUID = convertFromInteger(0x2A44)
    private var BLOOD_PRESSURE_MEASUREMENT_CHAR_UUID: UUID = convertFromInteger(0x2A35)
    private var INTERMEDIATE_CUFF_PRESSURE_CHAR_UUID: UUID = convertFromInteger(0x2A36)
    private var BLOOD_PRESSURE_FEATURE_CHAR_UUID: UUID = convertFromInteger(0x2A49)
    private var CURRENT_TIME_CHAR_UUID: UUID = convertFromInteger(0x2A2B)
    private var LOCAL_TIME_INFORMATION_CHAR_UUID: UUID = convertFromInteger(0x2A0F)
    private var REFERENCE_TIME_INFORMATION_CHAR_UUID: UUID = convertFromInteger(0x2A14)
    private var MANUFACTURER_NAME_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A29)
    private var MODEL_NUMBER_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A24)
    private var SERIAL_NUMBER_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A25)
    private var HARDWARE_REVISION_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A27)
    private var FIRMWARE_REVISION_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A26)
    private var SOFTWARE_REVISION_STRING_CHAR_UUID: UUID = convertFromInteger(0x2A28)
    private var SYSTEM_ID_CHAR_UUID: UUID = convertFromInteger(0x2A23)
    private var IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST_CHAR_UUID: UUID = convertFromInteger(0x2A2A)
    private var PNP_ID_CHAR_UUID: UUID = convertFromInteger(0x2A50)
    private var FITNESS_MACHINE_FEATURE_CHAR_UUID: UUID = convertFromInteger(0x2ACC)
    private var TREADMILL_DATA_CHAR_UUID: UUID = convertFromInteger(0x2ACD)
    private var CLIENT_CHARACTERISTIC_CONFIGURATION_CHAR_UUID: UUID = convertFromInteger(0x2902)
    private var CROSS_TRAINER_DATA_CHAR_UUID: UUID = convertFromInteger(0x2ACE)
    private var STEP_CLIMBER_DATA_CHAR_UUID: UUID = convertFromInteger(0x2ACF)
    private var STAIR_CLIMBER_DATA_CHAR_UUID: UUID = convertFromInteger(0x2AD0)
    private var ROWER_DATA_CHAR_UUID: UUID = convertFromInteger(0x2AD1)
    private var INDOOR_BIKE_DATA_CHAR_UUID: UUID = convertFromInteger(0x2AD2)
    private var TRAINING_STATUS_CHAR_UUID: UUID = convertFromInteger(0x2AD3)
    private var SUPPORTED_SPEED_RANGE_CHAR_UUID: UUID = convertFromInteger(0x2AD4)
    private var SUPPORTED_INCLINATION_RANGE_CHAR_UUID: UUID = convertFromInteger(0x2AD5)
    private var SUPPORTED_RESISTANCE_LEVE_RANGE_CHAR_UUID: UUID = convertFromInteger(0x2AD6)
    private var SUPPORTED_POWER_RANGE_CHAR_UUID: UUID = convertFromInteger(0x2AD8)
    private var SUPPORTED_HEART_RATE_RANGE_CHAR_UUID: UUID = convertFromInteger(0x2AD7)
    private var FITNESS_MACHINE_CONTROL_POINT_CHAR_UUID: UUID = convertFromInteger(0x2AD9)
    private var FITNESS_MACHINE_STATUS_CHAR_UUID: UUID = convertFromInteger(0x2ADA)
    private var TEMPERATURE_MEASUREMENT_CHAR_UUID: UUID = convertFromInteger(0x2A1C)
    private var TEMPERATURE_TYPE_CHAR_UUID: UUID = convertFromInteger(0x2A1D)
    private var INTERMEDIATE_TEMPERATURE_CHAR_UUID: UUID = convertFromInteger(0x2A1E)
    private var MEASUREMENT_INTERVAL_CHAR_UUID: UUID = convertFromInteger(0x2A21)
    private var HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = convertFromInteger(0x2A37)
    private var BODY_SENSOR_LOCATION_CHAR_UUID: UUID = convertFromInteger(0x2A38)
    private var HEART_RATE_CONTROL_POINT_CHAR_UUID: UUID = convertFromInteger(0x2A39)
    private var PROTOCOL_MODE_CHAR_UUID: UUID = convertFromInteger(0x2A4E)
    private var REPORT_CHAR_UUID: UUID = convertFromInteger(0x2A4D)
    private var REPORT_MAP_CHAR_UUID: UUID = convertFromInteger(0x2A4B)
    private var BOOT_KEYBOARD_INPUT_REPORT_CHAR_UUID: UUID = convertFromInteger(0x2A22)
    private var BOOT_KEYBOARD_OUTPUT_REPORT_CHAR_UUID: UUID = convertFromInteger(0x2A32)
    private var BOOT_MOUSE_INPUT_REPORT_CHAR_UUID: UUID = convertFromInteger(0x2A33)
    private var HID_INFORMATION_CHAR_UUID: UUID = convertFromInteger(0x2A4A)
    private var HID_CONTROL_POINT_CHAR_UUID: UUID = convertFromInteger(0x2A4C)
    private var PLX_SPOT_CHECK_MEASUREMENT_CHAR_UUID: UUID = convertFromInteger(0x2A5E)
    private var PLX_CONTINUOUS_MEASUREMENT_CHARACTERISTIC_CHAR_UUID: UUID = convertFromInteger(0x2A5F)
    private var PLX_FEATURES_CHAR_UUID: UUID = convertFromInteger(0x2A60)
    private var RECORD_ACCESS_CONTROL_POINT_CHAR_UUID: UUID = convertFromInteger(0x2A52)
    private var BODY_COMPOSITION_FEATURE_CHAR_UUID: UUID = convertFromInteger(0x2A9B)
    private var BODY_COMPOSITION_MEASUREMENT_CHAR_UUID: UUID = convertFromInteger(0x2A9C)

    /*
     * Nem szükséges konvertálni megtalálhtó a megfelelő UUID
     */
    private var RX_CHAR_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    private var TX_CHAR_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    /*
     * Karakterisztikák létrehozása
     */
    private val BATTERY_LEVEL: String = "Battery level"
    private val SERVICE_CHANGED: String = "Service Changed"
    private val DEVICE_NAME: String = "Device Name"
    private val PERIPHERAL_PRIVACY_FLAG: String = "Peripheral Privacy Flag"
    private val RECONNECTION_ADDRESS: String = "Reconnection Address"
    private val PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS: String = "Peripheral Preferred Connection Parameters"
    private val SUPPORTED_NEW_ALERT_CATEGORY: String = "Supported New Alert Category"
    private val NEW_ALERT: String = "New Alert"
    private val SUPPORTED_UNREAD_ALERT_CATEGORY: String = "Supported Unread Alert Category"
    private val UNREAD_ALERT_STATUS: String = "Unread Alert Status"
    private val ALERT_NOTIFICATION_CONTROL_POINT: String = "Alert Notification Control Point"
    private val BLOOD_PRESSURE_MEASUREMENT: String = "Blood Pressure Measurement"
    private val INTERMEDIATE_CUFF_PRESSURE: String = "Intermediate Cuff Pressure"
    private val BLOOD_PRESSURE_FEATURE: String = "Blood Pressure Feature"
    private val CURRENT_TIME: String = "Current Time"
    private val LOCAL_TIME_INFORMATION: String = "Local Time Information"
    private val REFERENCE_TIME_INFORMATION: String = "Reference Time Information"
    private val MANUFACTURER_NAME_STRING: String = "Manufacturer Name String"
    private val MODEL_NUMBER_STRING: String = "Model Number String"
    private val SERIAL_NUMBER_STRING: String = "Serial Number String"
    private val HARDWARE_REVISION_STRING: String = "Hardware Revision String"
    private val FIRMWARE_REVISION_STRING: String = "Firmware Revision String"
    private val SOFTWARE_REVISION_STRING: String = "Software Revision String"
    private val SYSTEM_ID: String = "System ID"
    private val IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST: String = "IEEE 11073-20601 Regulatory Certification Data List"
    private val PNP_ID: String = "PnP ID"
    private val FITNESS_MACHINE_FEATURE: String = "Fitness Machine Feature"
    private val TREADMILL_DATA: String = "Treadmill Data"
    private val CLIENT_CHARACTERISTIC_CONFIGURATION: String = "Client Characteristic Configuration"
    private val CROSS_TRAINER_DATA: String = "Cross Trainer Data"
    private val STEP_CLIMBER_DATA: String = "Step Climber Data"
    private val STAIR_CLIMBER_DATA: String = "Stair Climber Data"
    private val ROWER_DATA: String = "Rower Data"
    private val INDOOR_BIKE_DATA: String = "Indoor Bike Data"
    private val TRAINING_STATUS: String = "Training Status"
    private val SUPPORTED_SPEED_RANGE: String = "Supported Speed Range"
    private val SUPPORTED_INCLINATION_RANGE: String = "Supported Inclination Range"
    private val SUPPORTED_RESISTANCE_LEVE_RANGE: String = "Supported Resistance Level Range"
    private val SUPPORTED_POWER_RANGE: String = "Supported Power Range"
    private val SUPPORTED_HEART_RATE_RANGE: String = "Supported Heart Rate Range"
    private val FITNESS_MACHINE_CONTROL_POINT: String = "Fitness Machine Control Point"
    private val FITNESS_MACHINE_STATUS: String = "Fitness Machine Status"
    private val TEMPERATURE_MEASUREMENT: String = "Temperature Measurement"
    private val TEMPERATURE_TYPE: String = "Temperature Type"
    private val INTERMEDIATE_TEMPERATURE: String = "Intermediate Temperature"
    private val MEASUREMENT_INTERVAL: String = "Measurement Interval"
    private val HEART_RATE_MEASUREMENT: String = "Heart Rate Measurement"
    private val BODY_SENSOR_LOCATION: String = "Body Sensor Location"
    private val HEART_RATE_CONTROL_POINT: String = "Heart Rate Control Point"
    private val PROTOCOL_MODE: String = "Protocol Mode"
    private val REPORT: String = "Report"
    private val REPORT_MAP: String = "Report Map"
    private val BOOT_KEYBOARD_INPUT_REPORT: String = "Boot Keyboard Input Report"
    private val BOOT_KEYBOARD_OUTPUT_REPORT: String = "Boot Keyboard Output Report"
    private val BOOT_MOUSE_INPUT_REPORT: String = "Boot Mouse Input Report"
    private val HID_INFORMATION: String = "HID Information"
    private val HID_CONTROL_POINT: String = "HID Control Point"
    private val PLX_SPOT_CHECK_MEASUREMENT: String = "PLX Spot-check Measurement"
    private val PLX_CONTINUOUS_MEASUREMENT_CHARACTERISTIC: String = "PLX Continuous Measurement Characteristic"
    private val PLX_FEATURES: String = "PLX Features"
    private val RECORD_ACCESS_CONTROL_POINT: String = "Record Access Control Point"
    private val BODY_COMPOSITION_FEATURE: String = "Body Composition Feature"
    private val BODY_COMPOSITION_MEASUREMENT: String = "Body Composition Measurement"
    private var RX: String = "RX Characteristic"
    private var TX: String = "TX Characteristic"

    init {
        /*
         * Szerviz hozzáadás a listához!
         */
        servicesListNameTranslate[BATTERY_SERVICE_UUID] = BATTERY_SERVICE
        servicesListNameTranslate[GENERIC_ATTRIBUTE_SERVICE_UUID] = GENERIC_ATTRIBUTE_SERVICE
        servicesListNameTranslate[GENERIC_ACCESS_SERVICE_UUID] = GENERIC_ACCESS_SERVICE
        servicesListNameTranslate[ALERT_NOTIFICATION_SERVICE_UUID] = ALERT_NOTIFICATION
        servicesListNameTranslate[BLOOD_PRESSURE_SERVICE_UUID] = BLOOD_PRESSURE
        servicesListNameTranslate[CURRENT_TIME_SERVICE_UUID] = CURRENT_TIME_SERVICE
        servicesListNameTranslate[DEVICE_INFORMATION_SERVICE_UUID] = DEVICE_INFORMATION
        servicesListNameTranslate[FITNESS_MACHINE_SERVICE_UUID] = FITNESS_MACHINE
        servicesListNameTranslate[HEALTH_THERMOMETER_SERVICE_UUID] = HEALTH_THERMOMETER
        servicesListNameTranslate[HEART_RATE_SERVICE_UUID] = HEART_RATE
        servicesListNameTranslate[HUMAN_INTERFACE_DEVICE_SERVICE_UUID] = HUMAN_INTERFACE_DEVICE
        servicesListNameTranslate[PULSE_OXIMETER_SERVICE_UUID] = PULSE_OXIMETER_SERVICE
        servicesListNameTranslate[BODY_COMPOSITION_SERVICE_UUID] = BODY_COMPOSITION
        servicesListNameTranslate[SECURE_DFU_SERVICE_UUID] = SECURE_DFU_SERVICE
        servicesListNameTranslate[NORDIC_UART_SERVICE_UUID] = NORDIC_UART_SERVICE

        /*
         * Karakterisztikák hozzáadása a listához!
         */
        characteristicsListNameTranslate[BATTERY_LEVEL_CHAR_UUID] = BATTERY_LEVEL
        characteristicsListNameTranslate[SERVICE_CHANGED_CHAR_UUID] = SERVICE_CHANGED
        characteristicsListNameTranslate[DEVICE_NAME_CHAR_UUID] = DEVICE_NAME
        characteristicsListNameTranslate[PERIPHERAL_PRIVACY_FLAG_CHAR_UUID] = PERIPHERAL_PRIVACY_FLAG
        characteristicsListNameTranslate[RECONNECTION_ADDRESS_CHAR_UUID] = RECONNECTION_ADDRESS
        characteristicsListNameTranslate[PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_CHAR_UUID] = PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS
        characteristicsListNameTranslate[SUPPORTED_NEW_ALERT_CATEGORY_CHAR_UUID] = SUPPORTED_NEW_ALERT_CATEGORY
        characteristicsListNameTranslate[NEW_ALERT_CHAR_UUID] = NEW_ALERT
        characteristicsListNameTranslate[SUPPORTED_UNREAD_ALERT_CATEGORY_CHAR_UUID] = SUPPORTED_UNREAD_ALERT_CATEGORY
        characteristicsListNameTranslate[UNREAD_ALERT_STATUS_CHAR_UUID] = UNREAD_ALERT_STATUS
        characteristicsListNameTranslate[ALERT_NOTIFICATION_CONTROL_POINT_CHAR_UUID] = ALERT_NOTIFICATION_CONTROL_POINT
        characteristicsListNameTranslate[BLOOD_PRESSURE_MEASUREMENT_CHAR_UUID] = BLOOD_PRESSURE_MEASUREMENT
        characteristicsListNameTranslate[INTERMEDIATE_CUFF_PRESSURE_CHAR_UUID] = INTERMEDIATE_CUFF_PRESSURE
        characteristicsListNameTranslate[BLOOD_PRESSURE_FEATURE_CHAR_UUID] = BLOOD_PRESSURE_FEATURE
        characteristicsListNameTranslate[CURRENT_TIME_CHAR_UUID] = CURRENT_TIME
        characteristicsListNameTranslate[LOCAL_TIME_INFORMATION_CHAR_UUID] = LOCAL_TIME_INFORMATION
        characteristicsListNameTranslate[REFERENCE_TIME_INFORMATION_CHAR_UUID] = REFERENCE_TIME_INFORMATION
        characteristicsListNameTranslate[MANUFACTURER_NAME_STRING_CHAR_UUID] = MANUFACTURER_NAME_STRING
        characteristicsListNameTranslate[MODEL_NUMBER_STRING_CHAR_UUID] = MODEL_NUMBER_STRING
        characteristicsListNameTranslate[SERIAL_NUMBER_STRING_CHAR_UUID] = SERIAL_NUMBER_STRING
        characteristicsListNameTranslate[HARDWARE_REVISION_STRING_CHAR_UUID] = HARDWARE_REVISION_STRING
        characteristicsListNameTranslate[FIRMWARE_REVISION_STRING_CHAR_UUID] = FIRMWARE_REVISION_STRING
        characteristicsListNameTranslate[SOFTWARE_REVISION_STRING_CHAR_UUID] = SOFTWARE_REVISION_STRING
        characteristicsListNameTranslate[SYSTEM_ID_CHAR_UUID] = SYSTEM_ID
        characteristicsListNameTranslate[IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST_CHAR_UUID] = IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST
        characteristicsListNameTranslate[PNP_ID_CHAR_UUID] = PNP_ID
        characteristicsListNameTranslate[FITNESS_MACHINE_FEATURE_CHAR_UUID] = FITNESS_MACHINE_FEATURE
        characteristicsListNameTranslate[TREADMILL_DATA_CHAR_UUID] = TREADMILL_DATA
        characteristicsListNameTranslate[CLIENT_CHARACTERISTIC_CONFIGURATION_CHAR_UUID] = CLIENT_CHARACTERISTIC_CONFIGURATION
        characteristicsListNameTranslate[CROSS_TRAINER_DATA_CHAR_UUID] = CROSS_TRAINER_DATA
        characteristicsListNameTranslate[STEP_CLIMBER_DATA_CHAR_UUID] = STEP_CLIMBER_DATA
        characteristicsListNameTranslate[STAIR_CLIMBER_DATA_CHAR_UUID] = STAIR_CLIMBER_DATA
        characteristicsListNameTranslate[ROWER_DATA_CHAR_UUID] = ROWER_DATA
        characteristicsListNameTranslate[INDOOR_BIKE_DATA_CHAR_UUID] = INDOOR_BIKE_DATA
        characteristicsListNameTranslate[TRAINING_STATUS_CHAR_UUID] = TRAINING_STATUS
        characteristicsListNameTranslate[SUPPORTED_SPEED_RANGE_CHAR_UUID] = SUPPORTED_SPEED_RANGE
        characteristicsListNameTranslate[SUPPORTED_INCLINATION_RANGE_CHAR_UUID] = SUPPORTED_INCLINATION_RANGE
        characteristicsListNameTranslate[SUPPORTED_RESISTANCE_LEVE_RANGE_CHAR_UUID] = SUPPORTED_RESISTANCE_LEVE_RANGE
        characteristicsListNameTranslate[SUPPORTED_POWER_RANGE_CHAR_UUID] = SUPPORTED_POWER_RANGE
        characteristicsListNameTranslate[SUPPORTED_HEART_RATE_RANGE_CHAR_UUID] = SUPPORTED_HEART_RATE_RANGE
        characteristicsListNameTranslate[FITNESS_MACHINE_CONTROL_POINT_CHAR_UUID] = FITNESS_MACHINE_CONTROL_POINT
        characteristicsListNameTranslate[FITNESS_MACHINE_STATUS_CHAR_UUID] = FITNESS_MACHINE_STATUS
        characteristicsListNameTranslate[TEMPERATURE_MEASUREMENT_CHAR_UUID] = TEMPERATURE_MEASUREMENT
        characteristicsListNameTranslate[TEMPERATURE_TYPE_CHAR_UUID] = TEMPERATURE_TYPE
        characteristicsListNameTranslate[INTERMEDIATE_TEMPERATURE_CHAR_UUID] = INTERMEDIATE_TEMPERATURE
        characteristicsListNameTranslate[MEASUREMENT_INTERVAL_CHAR_UUID] = MEASUREMENT_INTERVAL
        characteristicsListNameTranslate[HEART_RATE_MEASUREMENT_CHAR_UUID] = HEART_RATE_MEASUREMENT
        characteristicsListNameTranslate[BODY_SENSOR_LOCATION_CHAR_UUID] = BODY_SENSOR_LOCATION
        characteristicsListNameTranslate[HEART_RATE_CONTROL_POINT_CHAR_UUID] = HEART_RATE_CONTROL_POINT
        characteristicsListNameTranslate[PROTOCOL_MODE_CHAR_UUID] = PROTOCOL_MODE
        characteristicsListNameTranslate[REPORT_CHAR_UUID] = REPORT
        characteristicsListNameTranslate[REPORT_MAP_CHAR_UUID] = REPORT_MAP
        characteristicsListNameTranslate[BOOT_KEYBOARD_INPUT_REPORT_CHAR_UUID] = BOOT_KEYBOARD_INPUT_REPORT
        characteristicsListNameTranslate[BOOT_KEYBOARD_OUTPUT_REPORT_CHAR_UUID] = BOOT_KEYBOARD_OUTPUT_REPORT
        characteristicsListNameTranslate[BOOT_MOUSE_INPUT_REPORT_CHAR_UUID] = BOOT_MOUSE_INPUT_REPORT
        characteristicsListNameTranslate[HID_INFORMATION_CHAR_UUID] = HID_INFORMATION
        characteristicsListNameTranslate[HID_CONTROL_POINT_CHAR_UUID] = HID_CONTROL_POINT
        characteristicsListNameTranslate[PLX_SPOT_CHECK_MEASUREMENT_CHAR_UUID] = PLX_SPOT_CHECK_MEASUREMENT
        characteristicsListNameTranslate[PLX_CONTINUOUS_MEASUREMENT_CHARACTERISTIC_CHAR_UUID] = PLX_CONTINUOUS_MEASUREMENT_CHARACTERISTIC
        characteristicsListNameTranslate[PLX_FEATURES_CHAR_UUID] = PLX_FEATURES
        characteristicsListNameTranslate[RECORD_ACCESS_CONTROL_POINT_CHAR_UUID] = RECORD_ACCESS_CONTROL_POINT
        characteristicsListNameTranslate[BODY_COMPOSITION_FEATURE_CHAR_UUID] = BODY_COMPOSITION_FEATURE
        characteristicsListNameTranslate[BODY_COMPOSITION_MEASUREMENT_CHAR_UUID] = BODY_COMPOSITION_MEASUREMENT
        characteristicsListNameTranslate[RX_CHAR_UUID] = RX
        characteristicsListNameTranslate[TX_CHAR_UUID] = TX
    }

    fun getServiceName(uuid: UUID): String {
        var responseName = "Unknown service name  \nService uuid : $uuid"

        for (item in servicesListNameTranslate) {
            if (item.key.compareTo(uuid) == 0) {
                responseName = item.value + "  \nService uuid : $uuid"
            }
        }
        return responseName
    }

    fun getCharacteristicName(uuid: UUID): String {
        var responseName = "Unknown characteristic name  \nCharacteristic uuid : $uuid"

        for (item in characteristicsListNameTranslate) {
            if (item.key.compareTo(uuid) == 0) {
                responseName = item.value + "  \nCharacteristic uuid : $uuid"
            }
        }
        return responseName
    }

    fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        val resposeUUID = UUID(MSB or (value shl 32), LSB)
        Log.d("convertFromInteger", "$resposeUUID")
        return resposeUUID
    }
}
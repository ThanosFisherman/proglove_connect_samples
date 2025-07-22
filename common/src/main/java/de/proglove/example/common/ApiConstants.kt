package de.proglove.example.common

/**
 * These are the constants used by the intent API.
 */
object ApiConstants {
    const val ACTION_DISCONNECT_INTENT = "com.proglove.api.DISCONNECT"
    const val ACTION_GET_STATE_INTENT = "com.proglove.api.GET_SCANNER_STATE"
    const val ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE"
    const val ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE"
    const val ACTION_BARCODE_INTENT_IVANTI = "com.wavelink.intent.action.BARCODE"
    const val ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT = "com.proglove.api.PLAY_FEEDBACK"
    const val ACTION_SCANNER_CONFIG = "com.proglove.api.CONFIG"
    const val ACTION_SCANNER_SET_CONFIG = "com.proglove.api.SET_CONFIG"
    const val ACTION_BARCODE_VIA_START_ACTIVITY_INTENT = "com.proglove.api.BARCODE_START_ACTIVITY"
    const val ACTION_CHANGE_CONFIG_PROFILE = "com.proglove.api.CHANGE_CONFIG_PROFILE"
    const val ACTION_BLOCK_TRIGGER = "com.proglove.api.BLOCK_TRIGGER"
    const val ACTION_UNBLOCK_TRIGGER = "com.proglove.api.UNBLOCK_TRIGGER"
    const val ACTION_TRIGGER_UNBLOCKED_INTENT = "com.proglove.api.TRIGGER_UNBLOCKED"
    const val ACTION_GET_CONFIG_PROFILES = "com.proglove.api.GET_CONFIG_PROFILES"
    const val ACTION_CONFIG_PROFILES = "com.proglove.api.CONFIG_PROFILES"
    const val ACTION_SCANNER_CONFIG_CHANGE = "com.proglove.api.SCANNER_CONFIG_CHANGE"

    const val ACTION_DISCONNECT_DISPLAY_INTENT = "com.proglove.api.DISPLAY_DISCONNECT"
    const val ACTION_GET_DISPLAY_STATE_INTENT = "com.proglove.api.GET_DISPLAY_STATE"
    const val ACTION_DISPLAY_STATE_INTENT = "com.proglove.api.DISPLAY_STATE"
    const val ACTION_BUTTON_PRESSED_INTENT = "com.proglove.api.DISPLAY_BUTTON"
    const val ACTION_SET_SCREEN_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN"
    const val ACTION_SET_SCREEN_RESULT_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN_RESULT"
    const val ACTION_GET_DISPLAY_DEVICE_TYPE_INTENT = "com.proglove.api.GET_DISPLAY_DEVICE_TYPE"
    const val ACTION_DISPLAY_DEVICE_TYPE_INTENT = "com.proglove.api.DISPLAY_DEVICE_TYPE"

    const val EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE"
    const val EXTRA_DATA_STRING_PG = "com.proglove.api.extra.BARCODE_DATA"
    const val EXTRA_SYMBOLOGY_STRING_PG = "com.proglove.api.extra.BARCODE_SYMBOLOGY"

    const val EXTRA_CONFIG_BUNDLE = "com.proglove.api.extra.CONFIG_BUNDLE"
    const val EXTRA_CONFIG_DEFAULT_SCAN_FEEDBACK_ENABLED = "com.proglove.api.extra.config.DEFAULT_SCAN_FEEDBACK_ENABLED"
    const val EXTRA_CONFIG_PROFILE_ID = "com.proglove.api.extra.CONFIG_PROFILE_ID"
    const val EXTRA_CONFIG_PROFILE_ACTIVE_ID = "com.proglove.api.extra.CONFIG_PROFILE_ACTIVE_ID"
    const val EXTRA_SCANNER_CONFIG_CHANGE_STATUS = "com.proglove.api.extra.SCANNER_CONFIG_CHANGE_STATUS"
    const val EXTRA_SCANNER_CONFIG_CHANGE_ERROR_TEXT = "com.proglove.api.extra.SCANNER_CONFIG_CHANGE_ERROR_TEXT"

    const val EXTRA_DISPLAY_TEMPLATE_ID = "com.proglove.api.extra.TEMPLATE_ID"
    const val EXTRA_DISPLAY_DATA = "com.proglove.api.extra.DATA"
    const val EXTRA_DISPLAY_RIGHT_HEADERS = "com.proglove.api.extra.RIGHT_HEADERS"
    const val EXTRA_DISPLAY_SEPARATOR = "com.proglove.api.extra.SEPARATOR"
    const val EXTRA_DISPLAY_DURATION = "com.proglove.api.extra.DURATION"
    const val EXTRA_DISPLAY_REFRESH_TYPE = "com.proglove.api.extra.REFRESH_TYPE"
    const val EXTRA_DISPLAY_STATE = "com.proglove.api.extra.DISPLAY_STATE"
    const val EXTRA_DISPLAY_BUTTON = "com.proglove.api.extra.DISPLAY_BUTTON"
    const val EXTRA_DISPLAY_BUTTON_GESTURE = "com.proglove.api.extra.BUTTON_GESTURE"
    const val EXTRA_DISPLAY_DEVICE = "com.proglove.api.extra.DISPLAY_DEVICE_NAME"
    const val EXTRA_DISPLAY_SET_SCREEN_SUCCESS = "com.proglove.api.extra.DISPLAY_SET_SCREEN_SUCCESS"
    const val EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT = "com.proglove.api.extra.DISPLAY_SET_SCREEN_ERROR"
    const val EXTRA_FEEDBACK_SEQUENCE_ID = "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID"
    const val EXTRA_DISPLAY_DEVICE_TYPE = "com.proglove.api.extra.DISPLAY_DEVICE_TYPE"

    const val EXTRA_TRIGGERS_BLOCK = "com.proglove.api.extra.TRIGGERS_BLOCK"
    const val EXTRA_TRIGGERS_UNBLOCK_BY = "com.proglove.api.extra.TRIGGERS_UNBLOCK_BY"

    const val EXTRA_REPLACE_QUEUE = "com.proglove.api.extra.REPLACE_QUEUE"

    // Pick display orientation
    const val DISPLAY_ORIENTATION_ACTIVITY_PACKAGE_NAME = "de.proglove.connect"
    const val DISPLAY_ORIENTATION_ACTIVITY_CLASS_NAME = "de.proglove.coreui.activities.DisplayOrientationActivity"

    /**
     * NOTE: In order to receive device visibility info, you need to have valid ProGlove License
     * imported in the Insight Mobile.
     * For more info reach out to your contact person at ProGlove.
     */
    const val ACTION_OBTAIN_DEVICE_VISIBILITY_INFO =
            "com.proglove.api.OBTAIN_DEVICE_VISIBILITY_INFO"
    const val ACTION_RECEIVE_DEVICE_VISIBILITY_INFO =
            "com.proglove.api.RECEIVE_DEVICE_VISIBILITY_INFO"
    const val EXTRA_DEVICE_VISIBILITY_INFO_SERIAL_NUMBER =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_SERIAL_NUMBER"
    const val EXTRA_DEVICE_VISIBILITY_INFO_MODEL_NUMBER =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_MODEL_NUMBER"
    const val EXTRA_DEVICE_VISIBILITY_INFO_FIRMWARE_REVISION =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_FIRMWARE_REVISION"
    const val EXTRA_DEVICE_VISIBILITY_INFO_BCE_REVISION =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_BCE_REVISION"
    const val EXTRA_DEVICE_VISIBILITY_INFO_BATTERY_LEVEL =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_BATTERY_LEVEL"
    const val EXTRA_DEVICE_VISIBILITY_INFO_MANUFACTURER =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_MANUFACTURER"
    const val EXTRA_DEVICE_VISIBILITY_INFO_APP_VERSION =
            "com.proglove.api.extra.DEVICE_VISIBILITY_INFO_APP_VERSION"
    const val EXTRA_DEVICE_VISIBILITY_INFO_DEVICE_BLUETOOTH_MAC_ADDRESS =
            "com.proglove.api.extra.EXTRA_DEVICE_VISIBILITY_INFO_DEVICE_BLUETOOTH_MAC_ADDRESS"


    const val ACTION_CONFIGURE_ACTIVITY_GOALS = "com.proglove.api.CONFIGURE_WORKER_GOALS"
    const val EXTRA_ACTIVITY_GOAL_TOTAL_STEPS = "com.proglove.api.extra.WORKER_GOAL_TOTAL_STEPS"
    const val EXTRA_ACTIVITY_GOAL_TOTAL_SCANS = "com.proglove.api.extra.WORKER_GOAL_TOTAL_SCANS"
    const val EXTRA_ACTIVITY_GOAL_AVERAGE_SCAN_SPEED = "com.proglove.api.extra.WORKER_GOAL_AVERAGE_SCAN_SPEED"

    // Set Display V2 Extras
    const val ACTION_SET_SCREEN_V2_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN_V2"

    const val EXTRA_DISPLAY_SCREEN_ID = "com.proglove.api.extra.SCREEN_ID"
    const val EXTRA_DISPLAY_SCREEN_VIEWS = "com.proglove.api.extra.SCREEN_VIEWS"
    const val EXTRA_DISPLAY_ACTIVE_SCREEN_VIEW_ID = "com.proglove.api.extra.ACTIVE_SCREEN_VIEW_ID"
    const val EXTRA_ACTION_BUTTONS = "com.proglove.api.extra.ACTION_BUTTONS"
    const val EXTRA_DISPLAY_SCREEN_TIMER = "com.proglove.api.extra.SCREEN_TIMER"
    const val EXTRA_ORIENTATION = "com.proglove.api.extra.DISPLAY_FORCED_ORIENTATION"

    const val ACTION_SET_DISPLAY_SCREEN_V2_RESULT_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN_V2_RESULT"
    const val ACTION_DISPLAY_SCREEN_EVENT_INTENT = "com.proglove.api.DISPLAY_SCREEN_EVENT"
    const val EXTRA_DISPLAY_SCREEN_CONTEXT = "com.proglove.api.extra.SCREEN_CONTEXT"
    const val EXTRA_DISPLAY_SCREEN_EVENT = "com.proglove.api.extra.SCREEN_EVENT"

    const val EVENT_COMPONENT_CLICKED = "component_clicked"
    const val EVENT_DATA_UPDATED = "data_updated"
    const val EVENT_TIMER_EXPIRED = "timer_expired"

    const val EVENT_REFERENCE_ID = "ref_id"
}
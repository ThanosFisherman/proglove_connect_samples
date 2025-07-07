package de.proglove.example.intent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.proglove.example.common.ApiConstants
import de.proglove.example.common.DisplaySampleData
import de.proglove.example.intent.enums.DeviceConnectionStatus
import de.proglove.example.intent.enums.DisplayConnectionStatus
import de.proglove.example.intent.enums.DisplayDeviceType
import de.proglove.example.intent.enums.ScannerConnectionStatus
import de.proglove.example.intent.interfaces.IIntentDisplayOutput
import de.proglove.example.intent.interfaces.IIntentScannerOutput
import de.proglove.example.intent.interfaces.IScannerConfigurationChangeOutput
import de.proglove.example.intent.interfaces.IStatusOutput
import kotlinx.android.synthetic.main.activity_goals.activityGoalsAverageScansGoalEdit
import kotlinx.android.synthetic.main.activity_goals.activityGoalsScansGoalEdit
import kotlinx.android.synthetic.main.activity_goals.activityGoalsStepsGoalEdit
import kotlinx.android.synthetic.main.activity_goals.setActivityGoalsBtn
import kotlinx.android.synthetic.main.activity_intent.blockAllTriggersButton
import kotlinx.android.synthetic.main.activity_intent.blockTriggerButton
import kotlinx.android.synthetic.main.activity_intent.connectScannerBtn
import kotlinx.android.synthetic.main.activity_intent.defaultFeedbackSwitch
import kotlinx.android.synthetic.main.activity_intent.deviceVisibilityBtn
import kotlinx.android.synthetic.main.activity_intent.disconnectDisplayBtn
import kotlinx.android.synthetic.main.activity_intent.disconnectScannerBtn
import kotlinx.android.synthetic.main.activity_intent.displayStateOutput
import kotlinx.android.synthetic.main.activity_intent.displayTypeOutput
import kotlinx.android.synthetic.main.activity_intent.getDisplayDeviceTypeBtn
import kotlinx.android.synthetic.main.activity_intent.getDisplayStateBtn
import kotlinx.android.synthetic.main.activity_intent.getScannerStateBtn
import kotlinx.android.synthetic.main.activity_intent.intentInputField
import kotlinx.android.synthetic.main.activity_intent.lastContactOutput
import kotlinx.android.synthetic.main.activity_intent.lastResponseValue
import kotlinx.android.synthetic.main.activity_intent.lastScreenContextOutput
import kotlinx.android.synthetic.main.activity_intent.lastSymbologyOutput
import kotlinx.android.synthetic.main.activity_intent.pickDisplayOrientationDialogBtn
import kotlinx.android.synthetic.main.activity_intent.scannerStateOutput
import kotlinx.android.synthetic.main.activity_intent.sendFeedbackWithReplaceQueueSwitch
import kotlinx.android.synthetic.main.activity_intent.sendNotificationTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPartialRefreshTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPg1ATestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPg1TestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPg3WithRightHeadersTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPgListT1Btn
import kotlinx.android.synthetic.main.activity_intent.sendPgNtfT5Btn
import kotlinx.android.synthetic.main.activity_intent.sendPgWork3Btn2T1
import kotlinx.android.synthetic.main.activity_intent.sendTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendTestScreenBtnFailing
import kotlinx.android.synthetic.main.activity_intent.sendTimerScreenBtn
import kotlinx.android.synthetic.main.activity_intent.unblockTriggerButton
import kotlinx.android.synthetic.main.activity_intent.versionOutput
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId1RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId2RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId3RB
import kotlinx.android.synthetic.main.feedback_selection_layout.radioGroup
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackButton
import kotlinx.android.synthetic.main.profiles_layout.changeProfileLabel
import kotlinx.android.synthetic.main.profiles_layout.profilesRecycler
import kotlinx.android.synthetic.main.profiles_layout.refreshConfigProfilesButton
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date

/**
 * PG Intent API usage example with a scanner and a display.
 */
class IntentActivity : AppCompatActivity(), IIntentDisplayOutput, IIntentScannerOutput, IStatusOutput, IScannerConfigurationChangeOutput {

    override var defaultFeedbackEnabled: Boolean
        get() = defaultFeedbackSwitch.isChecked
        set(value) {
            defaultFeedbackSwitch.isChecked = value
        }

    private lateinit var profilesAdapter: ProfilesAdapter

    private var scannerConnectionState = ScannerConnectionStatus.DISCONNECTED
    private var displayConnectionState = DisplayConnectionStatus.DISCONNECTED
    private var displayType = DisplayDeviceType.UNKNOWN
    private val messageHandler: MessageHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intent)

        registerReceiver(messageHandler, messageHandler.filter)
        messageHandler.registerDisplayOutput(this)
        messageHandler.registerScannerOutput(this)
        messageHandler.setStatusListener(this)
        messageHandler.setScannerConfigurationChangeListener(this)

        // Handle intent sent with start activity action which created this activity.
        // That Intent will not trigger #onNewIntent.
        messageHandler.handleNewIntent(intent)

        versionOutput.text = BuildConfig.VERSION_CODE.toString()

        getScannerStateBtn.setOnClickListener {
            messageHandler.requestScannerState()
        }

        connectScannerBtn.setOnClickListener {
            messageHandler.sendConnect()
        }

        disconnectScannerBtn.setOnClickListener {
            messageHandler.sendDisconnectScanner()
        }

        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()
            val shouldReplaceQueue = sendFeedbackWithReplaceQueueSwitch.isChecked
            messageHandler.triggerFeedback(selectedFeedbackId, shouldReplaceQueue)
        }
        //setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        defaultFeedbackSwitch.setOnClickListener {
            val defaultScanFeedback = defaultFeedbackSwitch.isChecked
            messageHandler.updateScannerConfig(defaultScanFeedback)
        }

        refreshConfigProfilesButton.setOnClickListener {
            messageHandler.getActiveConfigProfile()
        }

        setupProfilesRecycler()

        blockTriggerButton.setOnClickListener {
            messageHandler.blockTrigger()
        }

        blockAllTriggersButton.setOnClickListener {
            messageHandler.blockAllTriggersFor10s()
        }

        unblockTriggerButton.setOnClickListener {
            messageHandler.unblockTrigger()
        }

        disconnectDisplayBtn.setOnClickListener {
            messageHandler.sendDisconnectDisplay()
        }

        getDisplayStateBtn.setOnClickListener {
            messageHandler.requestDisplayState()
        }

        getDisplayDeviceTypeBtn.setOnClickListener {
            messageHandler.requestDisplayDeviceType()
        }

        sendTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, null, separator)
        }

        sendPartialRefreshTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, null, separator, 0, "PARTIAL_REFRESH")
        }

        sendNotificationTestScreenBtn.setOnClickListener {
            val templateId = "PG2I"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, null, separator, 3000)
        }

        sendPg1TestScreenBtn.setOnClickListener {
            val templateId = "PG1"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, null, separator, 3000)
        }

        sendPg1ATestScreenBtn.setOnClickListener {
            val templateId = "PG1A"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, null, separator, 3000)
        }

        sendPg3WithRightHeadersTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val separator = "|"

            val allData = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                val headerAndText = "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
                val rightHeader = "${index + 1}$separator${DisplaySampleData.SAMPLE_RIGHT_HEADERS.random()}"
                Pair(headerAndText, rightHeader)
            }
            val headerAndText = allData.joinToString(separator) { pair -> pair.first }
            val rightHeaders = allData.joinToString(separator) { pair -> pair.second }

            messageHandler.sendTestScreen(templateId, headerAndText, rightHeaders, separator, 3000)
        }

        sendTestScreenBtnFailing.setOnClickListener {
            messageHandler.sendTestScreen("PG2", "|||", null, ";")
        }

        pickDisplayOrientationDialogBtn.setOnClickListener {
            messageHandler.showPickDisplayOrientationDialog()
        }

        deviceVisibilityBtn.setOnClickListener {
            messageHandler.obtainDeviceVisibility()
        }

        sendPgNtfT5Btn.setOnClickListener {
            messageHandler.sendPgNtfT5()
        }

        sendPgWork3Btn2T1.setOnClickListener {
            messageHandler.sendPgWork3Btn2T1()
        }

        sendPgListT1Btn.setOnClickListener {
            messageHandler.sendPgListT1()
        }

        sendTimerScreenBtn.setOnClickListener {
            messageHandler.sendTimerScreen()
        }

        setActivityGoalsBtn.setOnClickListener {
            setActivityGoals()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        messageHandler.handleNewIntent(intent)
    }

    private fun getSampleDataForTemplate(template: String): List<Pair<String, Array<String>>> {
        return when (template) {
            "PG2" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_DESTINATION
            )
            "PG3" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_ITEM,
                    DisplaySampleData.SAMPLE_QUANTITY
            )
            "PG1I" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1E" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1C" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG2I" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2E" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2C" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG1" -> listOf(arrayOf(DisplaySampleData.SAMPLE_MESSAGES, DisplaySampleData.SAMPLE_MESSAGES_2, DisplaySampleData.SAMPLE_ITEM).random())
            "PG1A" -> listOf(DisplaySampleData.SAMPLE_MESSAGES_NO_HEADER)
            else -> listOf()
        }
    }

    private fun getFeedbackId() = when (radioGroup.checkedRadioButtonId) {
        feedbackId1RB.id -> 1
        feedbackId2RB.id -> 2
        feedbackId3RB.id -> 3
        // returning 1 as default
        else -> 1
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(messageHandler)
        messageHandler.unregisterDisplayOutput(this)
        messageHandler.unregisterScannerOutput(this)
    }

    private fun updateConnectionLabel() {
        runOnUiThread {
            if (scannerConnectionState == ScannerConnectionStatus.CONNECTED) {
                scannerStateOutput.setText(R.string.scanner_connected)
            } else if (scannerConnectionState == ScannerConnectionStatus.DISCONNECTED) {
                scannerStateOutput.setText(R.string.scanner_disconnected)
            }

            if (displayConnectionState == DisplayConnectionStatus.CONNECTED) {
                displayStateOutput.setText(R.string.display_connected)
            } else if (displayConnectionState == DisplayConnectionStatus.DISCONNECTED) {
                displayStateOutput.setText(R.string.display_disconnected)
            }

            when (displayType) {
                DisplayDeviceType.UNKNOWN -> displayTypeOutput.setText(R.string.display_type_unknown)
                DisplayDeviceType.NOT_CONNECTED -> displayTypeOutput.setText(R.string.display_not_connected)
                DisplayDeviceType.NOT_DISPLAY_DEVICE -> displayTypeOutput.setText(R.string.not_a_display_device)
                DisplayDeviceType.DISPLAY_V1 -> displayTypeOutput.setText(R.string.display_type_v1)
                DisplayDeviceType.DISPLAY_V2 -> displayTypeOutput.setText(R.string.display_type_v2)
            }
        }

        updateLastContact()
    }

    private fun updateLastContact() {
        val date = Date()
        val dateFormat = DateFormat.getDateTimeInstance()
        val formattedDate = dateFormat.format(date)
        runOnUiThread {
            lastContactOutput.text = formattedDate
        }
    }

    private fun updateScreenContextOutput(screenContext: String) {
        val screenContextJsonObject = screenContext.getJSONObject()
        if (screenContextJsonObject.has(ApiConstants.EVENT_REFERENCE_ID)) {
            runOnUiThread {
                lastScreenContextOutput.text =
                    "Screen ID: ${screenContextJsonObject.getString(ApiConstants.EVENT_REFERENCE_ID)}"
            }
        } else {
            runOnUiThread {
                lastScreenContextOutput.text = ""
            }
        }
    }

    private fun String.getJSONObject() = if (isNullOrEmpty()) {
        JSONObject()
    } else {
        JSONObject(this)
    }

    override fun onDeviceVisibilityInfoReceived(
            serialNumber: String,
            firmwareRevision: String,
            batteryLevel: Int,
            bceRevision: String,
            modelNumber: String,
            manufacturer: String,
            deviceBluetoothMacAddress: String,
            appVersion: String
    ) {
        val message = getString(R.string.device_visibility_alert_content,
                serialNumber,
                firmwareRevision,
                batteryLevel,
                bceRevision,
                modelNumber,
                manufacturer,
                deviceBluetoothMacAddress,
                appVersion
        )
        // Display content of deviceVisibilityInfo
        Log.i(TAG, "Did receive device visibility info:\n$message")
        runOnUiThread {
            AlertDialog.Builder(this@IntentActivity).apply {
                setTitle(R.string.device_visibility_alert_title)
                setMessage(message)
            }.create().show()
        }
    }

    override fun onBarcodeScanned(barcode: String, symbology: String, screenContext: String) {
        runOnUiThread {
            intentInputField?.text = barcode
            Toast.makeText(this, "Got barcode: $barcode", Toast.LENGTH_LONG).show()
            lastSymbologyOutput.text = symbology
        }
        updateScreenContextOutput(screenContext)
        updateLastContact()
    }

    override fun onScannerStateChanged(status: DeviceConnectionStatus) {
        Log.i(TAG, "Did receive scanner status: $status")
        scannerConnectionState = when (status) {
            DeviceConnectionStatus.CONNECTED -> {
                ScannerConnectionStatus.CONNECTED
            }
            DeviceConnectionStatus.DISCONNECTED -> {
                ScannerConnectionStatus.DISCONNECTED
            }
            else -> {
                ScannerConnectionStatus.CONNECTING
            }
        }
        updateConnectionLabel()
    }

    override fun onConfigProfilesReceived(profileIds: Array<String>, activeProfileId: String) {
        val profiles: List<ProfileUiData> = profileIds.map { profileId ->
            ProfileUiData(profileId, profileId == activeProfileId)
        }

        runOnUiThread {
            changeProfileLabel.visibility = if (profiles.isEmpty()) GONE else VISIBLE
            profilesAdapter.updateProfiles(profiles)
        }
    }

    override fun onScannerConfigurationChange(status: String, errorMessage: String?) {
        runOnUiThread {
            lastResponseValue.text = status
        }
    }

    private fun setupProfilesRecycler() {
        profilesAdapter = ProfilesAdapter(
                onProfileClicked = { profileId ->
                    messageHandler.changeConfigProfile(profileId)
                }
        )
        profilesRecycler.adapter = profilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setActivityGoals() {
        val totalStepsGoal = activityGoalsStepsGoalEdit.text.toString().toIntOrNull() ?: 650
        val totalScansGoal = activityGoalsScansGoalEdit.text.toString().toIntOrNull() ?: 10000
        val averageScantimeGoal = activityGoalsAverageScansGoalEdit.text.toString().toFloatOrNull() ?: 1.5f

        messageHandler.updateGoals(totalStepsGoal, totalScansGoal, averageScantimeGoal)
    }

    override fun onButtonPressed(buttonId: String, screenContext: String) {
        Toast.makeText(this, "Button $buttonId pressed", Toast.LENGTH_SHORT).show()

        updateScreenContextOutput(screenContext)
    }

    override fun onDisplayStateChanged(status: DeviceConnectionStatus) {
        Log.i(TAG, "Did receive display status: $status")
        displayConnectionState = when (status) {
            DeviceConnectionStatus.CONNECTED -> {
                DisplayConnectionStatus.CONNECTED
            }
            DeviceConnectionStatus.DISCONNECTED -> {
                DisplayConnectionStatus.DISCONNECTED
            }
            else -> {
                DisplayConnectionStatus.CONNECTING
            }
        }
        updateConnectionLabel()
    }

    override fun onDisplayDeviceTypeChanged(displayType: DisplayDeviceType) {
        Log.i(TAG, "Did receive display type: $displayType")
        this.displayType = displayType
        updateConnectionLabel()
    }

    override fun onDisplayEventReceived(event: String, context: String) {
        Log.i(TAG, "Did receive display event: $event")

        val eventJsonObject = event.getJSONObject()
        val contextJsonObject = context.getJSONObject()
        val screenMessage = when {
            eventJsonObject.has(ApiConstants.EVENT_COMPONENT_CLICKED) -> {
                "Component clicked: ${
                    eventJsonObject.getJSONObject(ApiConstants.EVENT_COMPONENT_CLICKED)
                        .getString(ApiConstants.EVENT_REFERENCE_ID)
                }"
            }

            eventJsonObject.has(ApiConstants.EVENT_TIMER_EXPIRED) -> {
                "Timer expired on screen: ${contextJsonObject.getString(ApiConstants.EVENT_REFERENCE_ID)}"
            }

            eventJsonObject.has(ApiConstants.EVENT_DATA_UPDATED) -> {
                "Data updated: ${
                    eventJsonObject.getJSONObject(ApiConstants.EVENT_DATA_UPDATED)
                        .getString(ApiConstants.EVENT_REFERENCE_ID)
                }"
            }

            else -> "Unknown event"
        }

        runOnUiThread {
            Toast.makeText(this, screenMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStatusReceived(status: String) {
        runOnUiThread {
            lastResponseValue.text = status
        }
    }

    companion object {

        const val TAG = "PGIntentActivity"
    }
}

/**
 * Profile data for displaying on UI.
 */
data class ProfileUiData(val profileId: String, var active: Boolean)
package de.proglove.example.sdk

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.proglove.example.common.DisplaySampleData
import de.proglove.example.sdk.databinding.ActivityMainBinding
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.IServiceOutput
import de.proglove.sdk.PgError
import de.proglove.sdk.PgManager
import de.proglove.sdk.button.BlockPgTriggersParams
import de.proglove.sdk.button.ButtonPress
import de.proglove.sdk.button.IBlockPgTriggersCallback
import de.proglove.sdk.button.IButtonOutput
import de.proglove.sdk.button.IPgTriggersUnblockedOutput
import de.proglove.sdk.button.PredefinedPgTrigger
import de.proglove.sdk.commands.PgCommand
import de.proglove.sdk.commands.PgCommandParams
import de.proglove.sdk.configuration.IPgConfigProfileCallback
import de.proglove.sdk.configuration.IPgGetConfigProfilesCallback
import de.proglove.sdk.configuration.IPgScannerConfigurationChangeOutput
import de.proglove.sdk.configuration.PgConfigProfile
import de.proglove.sdk.configuration.PgScannerConfigurationChangeResult
import de.proglove.sdk.configuration.ScannerConfigurationChangeStatus
import de.proglove.sdk.display.IDisplayOutput
import de.proglove.sdk.display.IPgSetScreenCallback
import de.proglove.sdk.display.PgScreenData
import de.proglove.sdk.display.PgTemplateField
import de.proglove.sdk.display.RefreshType
import de.proglove.sdk.scanner.BarcodeScanResults
import de.proglove.sdk.scanner.DeviceVisibilityInfo
import de.proglove.sdk.scanner.IPgDeviceVisibilityCallback
import de.proglove.sdk.scanner.IPgFeedbackCallback
import de.proglove.sdk.scanner.IPgImageCallback
import de.proglove.sdk.scanner.IPgScannerConfigCallback
import de.proglove.sdk.scanner.IScannerOutput
import de.proglove.sdk.scanner.ImageResolution
import de.proglove.sdk.scanner.PgImage
import de.proglove.sdk.scanner.PgImageConfig
import de.proglove.sdk.scanner.PgPredefinedFeedback
import de.proglove.sdk.scanner.PgScannerConfig
import de.proglove.sdk.utils.IPgSetActivityGoalsCallback
import de.proglove.sdk.workerperformance.PgActivityGoals
import de.proglove.sdk.display.model.v2.DisplayType
import de.proglove.sdk.display.model.v2.PgActionButton.Assigned
import de.proglove.sdk.display.model.v2.PgActionButton.IndicatorColor
import de.proglove.sdk.display.model.v2.PgActionButton.Unassigned
import de.proglove.sdk.display.model.v2.PgActionButtons
import de.proglove.sdk.display.model.v2.PgListViewItem
import de.proglove.sdk.display.model.v2.PgScreen
import de.proglove.sdk.display.model.v2.PgScreenAction
import de.proglove.sdk.display.model.v2.PgScreenComponent
import de.proglove.sdk.display.model.v2.PgScreenEvent
import de.proglove.sdk.display.model.v2.PgScreenInputMethod
import de.proglove.sdk.display.model.v2.PgScreenOrientation
import de.proglove.sdk.display.model.v2.PgScreenResources
import de.proglove.sdk.display.model.v2.PgScreenTimer
import de.proglove.sdk.display.model.v2.PgScreenView
import java.util.logging.Level
import java.util.logging.Logger

/**
 * PG SDK example for a scanner.
 */
class SdkActivity : AppCompatActivity(), IScannerOutput, IServiceOutput, IDisplayOutput, IButtonOutput, IPgTriggersUnblockedOutput, IPgScannerConfigurationChangeOutput {

    private val logger = Logger.getLogger("sample-logger")
    private val pgManager = PgManager(logger)

    private var serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
    private var scannerConnected = false
    private var displayConnected = false

    private lateinit var profilesAdapter: ProfilesAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pgManager.subscribeToServiceEvents(this)
        pgManager.subscribeToScans(this)
        pgManager.subscribeToDisplayEvents(this)
        pgManager.subscribeToButtonPresses(this)
        pgManager.subscribeToPgTriggersUnblocked(this)
        pgManager.subscribeToPgScannerConfigurationChanges(this)

        binding.versionOutput.text = BuildConfig.VERSION_CODE.toString()

        binding.serviceConnectBtn.setOnClickListener {
            pgManager.ensureConnectionToService(this.applicationContext)
            updateButtonStates()
        }

        binding.connectScannerRegularBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairing()
            }
        }

        binding.connectScannerPinnedBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairingFromPinnedActivity(this)
            }
        }

        binding.feedBackLayout.triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()

            // Creating new PgCommandParams setting the queueing behaviour
            val pgCommandParams =
                PgCommandParams(binding.sendFeedbackWithReplaceQueueSwitch.isChecked)

            // Wrapping the feedback data in a PgCommand with the PgCommandData
            val triggerFeedbackCommand = selectedFeedbackId.toCommand(pgCommandParams)

            pgManager.triggerFeedback(
                    command = triggerFeedbackCommand,
                    callback = object : IPgFeedbackCallback {

                        override fun onSuccess() {
                            logger.log(Level.INFO, "Feedback successfully played.")
                            runOnUiThread {
                                binding.lastResponseValue.text = getString(R.string.feedback_success)
                            }
                        }

                        override fun onError(error: PgError) {
                            val errorMessage = "An Error occurred during triggerFeedback: $error"
                            logger.log(Level.WARNING, errorMessage)
                            runOnUiThread {
                                Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                binding.lastResponseValue.text = error.toString()
                            }
                        }
                    }
            )
        }
        // setting first Item as selected by default
        binding.feedBackLayout.radioGroup.check(R.id.feedbackId1RB)

        // set image configurations
        setDefaultImageConfigurations()
        binding.takeImageLayout.takeImageButton.setOnClickListener {
            takeImage()
        }

        binding.defaultFeedbackSwitch.setOnClickListener {
            val config =
                PgScannerConfig(isDefaultScanAckEnabled = binding.defaultFeedbackSwitch.isChecked)

            binding.defaultFeedbackSwitch.isEnabled = false

            pgManager.setScannerConfig(config, object : IPgScannerConfigCallback {

                override fun onScannerConfigSuccess(config: PgScannerConfig) {
                    runOnUiThread {
                        logger.log(Level.INFO, "Successfully updated config on scanner")
                        binding.defaultFeedbackSwitch.isEnabled = true
                        binding.lastResponseValue.text = getString(R.string.scanner_config_success)
                    }
                }

                override fun onError(error: PgError) {
                    runOnUiThread {
                        val errorMessage = "Could not set config on scanner: $error"
                        logger.log(Level.WARNING, errorMessage)
                        Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        // restore old state
                        binding.defaultFeedbackSwitch.toggle()
                        binding.defaultFeedbackSwitch.isEnabled = true
                        binding.lastResponseValue.text = error.toString()
                    }
                }
            })
        }

        binding.profilesLayout.refreshConfigProfilesButton.setOnClickListener {
            getConfigProfiles()
        }

        setupProfilesRecycler()

        binding.blockTriggerButton.setOnClickListener {
            blockTrigger()
        }

        binding.blockAllTriggersButton.setOnClickListener {
            blockAllTriggersFor10s()
        }

        binding.unblockTriggerButton.setOnClickListener {
            unblockTrigger()
        }

        addDisplayClickListeners()
        addDisplayV2ClickListeners()

        binding.pickDisplayOrientationDialogBtn.setOnClickListener {
            val error = pgManager.showPickDisplayOrientationDialog(this)
            if (error != null) {
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                binding.lastResponseValue.text = error.toString()
            }
        }

        binding.deviceVisibilityBtn.setOnClickListener {
            obtainDeviceVisibilityInfo()
        }

        binding.activityGoals.setActivityGoalsBtn.setOnClickListener {
            setActivityGoals()
        }
    }

    private fun addDisplayClickListeners() {

        binding.displayDeviceTypeBtn.setOnClickListener {
            updateDisplayConnectionUiState()
        }

        val loggingCallback = object : IPgSetScreenCallback {

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(
                        this@SdkActivity,
                        "Got error setting text: $error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.lastResponseValue.text = error.toString()
                }
            }

            override fun onSuccess() {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "set screen successfully", Toast.LENGTH_SHORT)
                        .show()
                    binding.lastResponseValue.text = getString(R.string.set_screen_success)
                }
            }
        }

        binding.disconnectDisplayBtn.setOnClickListener {
            pgManager.disconnectDisplay()
        }

        binding.sendTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    data = PgScreenData(templateId, templateFields),
                    callback = loggingCallback
            )
        }

        binding.sendPartialRefreshTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    data = PgScreenData(templateId, templateFields, RefreshType.PARTIAL_REFRESH),
                    callback = loggingCallback
            )
        }

        binding.sendNotificationTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setNotificationScreen(
                    data = PgScreenData("PG2", templateFields),
                    callback = loggingCallback,
                    durationMs = 3000
            )
        }

        binding.sendTestScreenBtnFailing.setOnClickListener {
            pgManager.setScreen(
                    data = PgScreenData(
                            "PG1",
                            listOf(
                                    PgTemplateField(1, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(2, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(3, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(4, "not going to be displayed", "not going to be displayed")
                            )
                    ),
                    callback = loggingCallback
            )
        }

        binding.sendPg1TestScreenBtn.setOnClickListener {
            val templateId = "PG1"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }

        binding.sendPg1ATestScreenBtn.setOnClickListener {
            val templateId = "PG1A"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }

        binding.sendPg3WithRightHeadersTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                val rightHeader = DisplaySampleData.SAMPLE_RIGHT_HEADERS.random()
                PgTemplateField(index + 1, pair.first, pair.second.random(), rightHeader)
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }
    }

    private fun addDisplayV2ClickListeners() {
        binding.sendPgNtfT5Btn.setOnClickListener {
            pgManager.setScreen(
                PgScreen(
                    referenceId = "pgNtfT5Screen",
                    screenView = PgScreenView.TemplateV2.NotificationView.PgNtfT5(
                        referenceId = "",
                        tagline = "Notification",
                        message = "This is a message with two buttons",
                        primaryButton = PgScreenComponent.Button(
                            referenceId = "BUTTON_PRIMARY",
                            text = "OK",
                            onSingleClick = PgScreenAction.Notify
                        ),
                        secondaryButton = PgScreenComponent.Button(
                            referenceId = "BUTTON_SECONDARY",
                            text = "Cancel",
                            onSingleClick = PgScreenAction.Notify
                        ),
                    ),
                    actionButtons = PgActionButtons(
                        frontOutside = Assigned(
                            referenceId = "actionButton1",
                            indicatorLabelText = "Notify",
                            indicatorColor = IndicatorColor.Yellow,
                            onSingleClick = PgScreenAction.Notify,
                        ),
                        backOutside = Assigned(
                            referenceId = "actionButton2",
                            indicatorLabelText = "Back",
                            indicatorColor = IndicatorColor.Red,
                            onSingleClick = PgScreenAction.NavigateBack,
                        ),
                        frontInside = Assigned(
                            referenceId = "actionButton3",
                            indicatorLabelText = "Ok",
                            indicatorColor = IndicatorColor.Cyan,
                            onSingleClick = PgScreenAction.ClickOnPgScreenComponent(
                                "BUTTON_PRIMARY"
                            ),
                        ),
                        backInside = Assigned(
                            referenceId = "actionButton4",
                            indicatorLabelText = "Cancel",
                            indicatorColor = IndicatorColor.Green,
                            onSingleClick = PgScreenAction.ClickOnPgScreenComponent(
                                "BUTTON_SECONDARY"
                            ),
                        ),
                    ),
                    forcedOrientation = PgScreenOrientation.LANDSCAPE
                ).toCommand(),
                object : IPgSetScreenCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Notification screen set successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.set_screen_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Error setting notification screen: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
            )
        }
        
        binding.sendPgWork3Btn2T1.setOnClickListener {
            pgManager.setScreen(
                PgScreen(
                    referenceId = "work3Btn2T1Screen",
                    screenViews = arrayOf(PgScreenView.TemplateV2.WorkflowView.PgWork3Btn2T1(
                        fieldTop = PgScreenComponent.TextField(
                            referenceId = "fieldTop",
                            headerText = "Top Field",
                            contentText = "Workflow View with 2 buttons and 1 text field"
                        ),
                        fieldMiddleLeft = PgScreenComponent.TextField(
                            headerText = "Middle Left Field",
                            contentText = "This is the left field in the middle section"
                        ),
                        fieldMiddleRight = PgScreenComponent.TextField(
                            referenceId = "fieldMiddleRight",
                            headerText = "Middle Right Field",
                            contentText = "This is the right field in the middle section",
                            inputMethod = PgScreenInputMethod.NumPad()
                        ),
                        button1 = PgScreenComponent.Button(
                            referenceId = "button1",
                            text = "Ok",
                            onSingleClick = PgScreenAction.Notify
                        ),
                        button2 = PgScreenComponent.Button(
                            referenceId = "button2",
                            text = "Cancel",
                            onSingleClick = PgScreenAction.Notify
                        )),
                        PgScreenView.TemplateV2.WorkflowView.PgWork1T1(
                            fieldMain = PgScreenComponent.TextField(
                                headerText = "Main Field",
                                contentText = "This is the main text field"
                            ),
                        )),
                    actionButtons = PgActionButtons(
                        frontOutside = Unassigned,
                        backOutside = Unassigned,
                        frontInside = Assigned(
                            referenceId = "actionButton1",
                            indicatorLabelText = "Action 1",
                            indicatorColor = IndicatorColor.Green,
                            onSingleClick = PgScreenAction.Notify
                        ),
                        backInside = Assigned(
                            referenceId = "actionButton2",
                            indicatorLabelText = "Back",
                            indicatorColor = IndicatorColor.Red,
                            onSingleClick = PgScreenAction.NavigateBack
                        )
                    ),
                    forcedOrientation = PgScreenOrientation.PORTRAIT
                ).toCommand(),

                object : IPgSetScreenCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Work3T1 screen set successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.set_screen_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Error setting Work3T1 screen: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
            )
        }

        binding.sendPgListT1Btn.setOnClickListener {
            val listItems = listOf(
                PgListViewItem.PgListT1Item(
                    mainText = "Item 1",
                    underlineText = "Description 1",
                    trailingIcon = PgScreenResources.ListItemTrailingIcon.None,
                    trailingText = "1"
                ),
                PgListViewItem.PgListT1Item(
                    mainText = "Item 2",
                    underlineText = "Description 2",
                    trailingIcon = PgScreenResources.ListItemTrailingIcon.Arrow,
                    trailingText = "2"
                ),
                PgListViewItem.PgListT1Item(
                    mainText = "Item 3",
                    underlineText = "Description 3",
                    trailingIcon = PgScreenResources.ListItemTrailingIcon.None,
                    trailingText = "3"
                ),
                PgListViewItem.PgListT1Item(
                    mainText = "Item 4",
                    underlineText = "Description 4",
                    trailingIcon = PgScreenResources.ListItemTrailingIcon.Arrow,
                    trailingText = "4"
                ),
                PgListViewItem.PgListT1Item(
                    mainText = "Item 5",
                    underlineText = "Description 5",
                    trailingIcon = PgScreenResources.ListItemTrailingIcon.None,
                    trailingText = "5"
                )
            )

            pgManager.setScreen(
                PgScreen(
                    referenceId = "listScreen",
                    screenView = PgScreenView.TemplateV2.ListView.PgListT1(
                        referenceId = "",
                        header = "List View Example",
                        items = listItems
                    ),
                    actionButtons = PgActionButtons(
                        frontOutside = Unassigned,
                        backOutside = Unassigned,
                        frontInside = Assigned(
                            referenceId = "actionButton1",
                            indicatorLabelText = "Notify",
                            indicatorColor = IndicatorColor.Yellow,
                            onSingleClick = PgScreenAction.Notify
                        ),
                        backInside = Assigned(
                            referenceId = "actionButton2",
                            indicatorLabelText = "Back",
                            indicatorColor = IndicatorColor.Red,
                            onSingleClick = PgScreenAction.NavigateBack
                        )
                    ),
                    forcedOrientation = PgScreenOrientation.PORTRAIT
                ).toCommand(),

                object : IPgSetScreenCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "List T1 screen set successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.set_screen_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Error setting List T1 screen: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
            )
        }

        binding.sendTimerScreenBtn.setOnClickListener {
            pgManager.setScreen(
                PgScreen(
                    referenceId = "timerScreen",
                    screenView = PgScreenView.TemplateV2.WorkflowView.PgWork1T1(
                        referenceId = "",
                        fieldMain = PgScreenComponent.TextField(
                        headerText = "Timer Example",
                        contentText = "This screen will automatically navigate back after 2 seconds.",
                            state = PgScreenComponent.TextField.State.Focused(true)
                        )
                    ),
                    timer = PgScreenTimer.Enabled(
                        timeoutMs = 2000,
                        onExpire = PgScreenAction.NavigateBack
                    ),
                    forcedOrientation = PgScreenOrientation.PORTRAIT
                ).toCommand(),

                object : IPgSetScreenCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Timer screen set successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.set_screen_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                this@SdkActivity,
                                "Error setting Timer screen: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
            )
        }
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

    private fun setDefaultImageConfigurations() {
        val imageConfig = PgImageConfig()
        binding.takeImageLayout.jpegQualityEditText.setText(imageConfig.jpegQuality.toString())
        val defaultTimeout = DEFAULT_IMAGE_TIMEOUT
        binding.takeImageLayout.timeoutEditText.setText(defaultTimeout.toString())
    }

    private fun setupProfilesRecycler() {
        profilesAdapter = ProfilesAdapter(
                onProfileClicked = { profileId ->
                    changeConfigProfile(profileId)
                }
        )
        binding.profilesLayout.profilesRecycler.adapter = profilesAdapter
        binding.profilesLayout.profilesRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun takeImage() {
        var timeout = DEFAULT_IMAGE_TIMEOUT
        var quality = 20

        try {
            timeout = binding.takeImageLayout.timeoutEditText.text.toString().toInt()
            quality = binding.takeImageLayout.jpegQualityEditText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            logger.log(Level.WARNING, "use positive numbers only")
        }

        val resolution = when (binding.takeImageLayout.resolutionRadioGroup.checkedRadioButtonId) {
            R.id.highResolution -> ImageResolution.RESOLUTION_1280_960
            R.id.mediumResolution -> ImageResolution.RESOLUTION_640_480
            R.id.lowResolution -> ImageResolution.RESOLUTION_320_240
            else -> ImageResolution.values()[1]
        }

        val config = PgImageConfig(quality, resolution)
        val imageCallback = object : IPgImageCallback {
            override fun onImageReceived(image: PgImage) {
                val bmp = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
                runOnUiThread {
                    binding.takeImageLayout.imageTaken.setImageBitmap(bmp)
                    binding.lastResponseValue.text = getString(R.string.image_success)
                }
            }

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "error code is $error", Toast.LENGTH_LONG)
                        .show()
                    binding.lastResponseValue.text = error.toString()
                }
            }
        }
        pgManager.takeImage(config, timeout, imageCallback)
    }


    private fun getFeedbackId(): PgPredefinedFeedback {
        return when (binding.feedBackLayout.radioGroup.checkedRadioButtonId) {
            R.id.feedbackId1RB -> PgPredefinedFeedback.SUCCESS
            R.id.feedbackId2RB -> PgPredefinedFeedback.ERROR
            R.id.feedbackId3RB -> PgPredefinedFeedback.SPECIAL_1
            else -> PgPredefinedFeedback.ERROR
        }
    }

    override fun onResume() {
        super.onResume()

        pgManager.ensureConnectionToService(this.applicationContext)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        runOnUiThread {
            updateServiceConnectionButtonState()
            updateScannerConnectionButtonState()
            updateDisplayConnectionUiState()
        }
    }

    private fun updateDisplayConnectionUiState() {
        when {
            serviceConnectionState != ServiceConnectionStatus.CONNECTED -> binding.displayStateOutput.setText(R.string.display_disconnected)
            displayConnected -> binding.displayStateOutput.setText(R.string.display_connected)
            else -> binding.displayStateOutput.setText(R.string.display_disconnected)
        }

        when (pgManager.getConnectedDisplayType()) {
            DisplayType.UNKNOWN -> binding.displayTypeOutput.setText(R.string.display_type_unknown)
            DisplayType.NOT_CONNECTED -> binding.displayTypeOutput.setText(R.string.display_not_connected)
            DisplayType.NOT_DISPLAY_DEVICE -> binding.displayTypeOutput.setText(R.string.not_a_display_device)
            DisplayType.V1 -> binding.displayTypeOutput.setText(R.string.display_type_v1)
            DisplayType.V2 -> binding.displayTypeOutput.setText(R.string.display_type_v2)
        }
    }

    private fun updateScannerConnectionButtonState() {
        if (scannerConnected) {
            binding.connectScannerPinnedBtn.setText(R.string.disconnect_scanner)
            binding.connectScannerRegularBtn.setText(R.string.disconnect_scanner)
        } else {
            binding.connectScannerPinnedBtn.setText(R.string.pair_scanner)
            binding.connectScannerRegularBtn.setText(R.string.pair_scanner)
        }
    }

    private fun updateServiceConnectionButtonState() {
        when (serviceConnectionState) {
            ServiceConnectionStatus.CONNECTING -> {
                binding.serviceConnectBtn.isEnabled = false
                binding.serviceConnectBtn.setText(R.string.service_connecting)

                binding.connectScannerPinnedBtn.setText(R.string.pair_scanner)
                binding.connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
            ServiceConnectionStatus.CONNECTED -> {
                logger.log(Level.INFO, "Connection to ProGlove SDK Service successful.")

                binding.serviceConnectBtn.isEnabled = false
                binding.serviceConnectBtn.setText(R.string.service_connected)

                binding.connectScannerPinnedBtn.setText(R.string.disconnect_scanner)
                binding.connectScannerRegularBtn.setText(R.string.disconnect_scanner)
            }
            ServiceConnectionStatus.DISCONNECTED -> {
                binding.serviceConnectBtn.isEnabled = true
                binding.serviceConnectBtn.setText(R.string.connect_service)

                binding.connectScannerPinnedBtn.setText(R.string.pair_scanner)
                binding.connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
        }
    }

    private fun changeConfigProfile(profileId: String) {
        pgManager.changeConfigProfile(
                PgCommand(PgConfigProfile(profileId)),
                object : IPgConfigProfileCallback {
                    override fun onConfigProfileChanged(profile: PgConfigProfile) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "${profile.profileId} set successfully",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.change_profile_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to set $profileId - $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
        )
    }

    private fun getConfigProfiles() {
        pgManager.getConfigProfiles(
                object : IPgGetConfigProfilesCallback {
                    override fun onConfigProfilesReceived(profiles: Array<PgConfigProfile>) {
                        logger.log(Level.INFO, "received ${profiles.size} config profiles")
                        val uiProfiles: List<ProfileUiData> = profiles.map { profile ->
                            ProfileUiData(profile.profileId, profile.isActive)
                        }

                        runOnUiThread {
                            binding.profilesLayout.changeProfileLabel.visibility = if (profiles.isEmpty()) GONE else VISIBLE
                            profilesAdapter.updateProfiles(uiProfiles)
                            binding.lastResponseValue.text = getString(R.string.get_profiles_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to get profiles - $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                }
        )
    }

    private fun blockTrigger() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(
                        listOf(PredefinedPgTrigger.DefaultPgTrigger),
                        listOf(PredefinedPgTrigger.DoubleClickMainPgTrigger),
                        0,
                        true)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Blocking trigger success",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.block_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to block the trigger: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    // Requires Insight Mobile v1.13.0+ and Scanner v2.5.0+
    private fun blockAllTriggersFor10s() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(emptyList(), emptyList(), 10000, true)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Blocking all triggers success",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.block_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to block all triggers: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    private fun unblockTrigger() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(emptyList(), emptyList(), 0, false)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Unblocking triggers success",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = getString(R.string.unblock_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to unblock the trigger: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            binding.lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    /**
     * NOTE: In order to use [PgManager.obtainDeviceVisibilityInfo] you need to have valid ProGlove License
     * imported in the Insight Mobile.
     * For more info reach out to your contact person at ProGlove.
     */
    private fun obtainDeviceVisibilityInfo() {
        pgManager.obtainDeviceVisibilityInfo(callback = object : IPgDeviceVisibilityCallback {
            override fun onDeviceVisibilityInfoObtained(deviceVisibilityInfo: DeviceVisibilityInfo) {
                // Display content of deviceVisibilityInfo
                logger.log(Level.INFO, "deviceVisibilityInfo: $deviceVisibilityInfo")
                runOnUiThread {
                    AlertDialog.Builder(this@SdkActivity).apply {
                        setTitle(R.string.device_visibility_alert_title)
                        setMessage(getString(R.string.device_visibility_alert_content,
                                deviceVisibilityInfo.serialNumber,
                                deviceVisibilityInfo.firmwareRevision,
                                deviceVisibilityInfo.batteryLevel,
                                deviceVisibilityInfo.bceRevision,
                                deviceVisibilityInfo.modelNumber,
                                deviceVisibilityInfo.manufacturer,
                                deviceVisibilityInfo.appVersion
                        ))
                    }.create().show()
                }
            }

            override fun onError(error: PgError) {
                // Handle error
                logger.log(Level.SEVERE, "Error during obtainDeviceVisibilityInfo: $error")
                runOnUiThread {
                    AlertDialog.Builder(this@SdkActivity).apply {
                        setTitle(R.string.device_visibility_alert_title)
                        setMessage(
                                getString(
                                        R.string.device_visibility_alert_content_error, error
                                ))
                    }.create().show()
                }
            }
        })
    }

    private fun setActivityGoals() {
        val activityGoals = PgActivityGoals(
            binding.activityGoals.activityGoalsStepsGoalEdit.text.toString().toIntOrNull() ?: 650,
            binding.activityGoals.activityGoalsScansGoalEdit.text.toString().toIntOrNull() ?: 10000,
            binding.activityGoals.activityGoalsAverageScansGoalEdit.text.toString().toDoubleOrNull() ?: 1.5
        )
        pgManager.setActivityGoals(activityGoals.toCommand(), callback = object : IPgSetActivityGoalsCallback {
            override fun onSuccess() {
                logger.log(Level.INFO, "Goals set successfully.")
            }

            override fun onError(error: PgError) {
                logger.log(Level.SEVERE, "Error during setActivityGoals: $error")
                runOnUiThread {
                    Toast.makeText(
                            applicationContext,
                            "Failed to update goals: $error",
                            Toast.LENGTH_LONG
                    ).show()
                    binding.lastResponseValue.text = error.toString()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        pgManager.unsubscribeFromScans(this)
        pgManager.unsubscribeFromDisplayEvents(this)
        pgManager.unsubscribeFromServiceEvents(this)
        pgManager.unsubscribeFromButtonPresses(this)
        pgManager.unsubscribeFromPgTriggersUnblocked(this)
        pgManager.unsubscribeFromPgScannerConfigurationChanges(this)
    }

    /*
     * IServiceOutput Implementation BEGIN
     */

    override fun onServiceConnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.CONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    override fun onServiceDisconnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    /*
     * IServiceOutput Implementation END
     */

    /*
     * IScannerOutput Implementation:
     */

    override fun onBarcodeScanned(barcodeScanResults: BarcodeScanResults) {
        runOnUiThread {
            binding.inputField.text = barcodeScanResults.barcodeContent
            binding.symbologyResult.text = barcodeScanResults.symbology ?: ""
            barcodeScanResults.screenContext?.let {
                binding.lastScreenContextOutput.text = "Screen ID: ${barcodeScanResults.screenContext?.screenId}"
            } ?: run {
                binding.lastScreenContextOutput.text = ""
            }
            if (barcodeScanResults.symbology?.isNotEmpty() == true) {
                Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with symbology ${barcodeScanResults.symbology}",
                        Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with no symbology",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onScannerConnected() {
        runOnUiThread {
            scannerConnected = true
            updateButtonStates()
        }
    }

    override fun onScannerDisconnected() {
        runOnUiThread {
            scannerConnected = false

            // Connecting a new scanner will reset this config to default, which is true
            binding.defaultFeedbackSwitch.isChecked = true
            updateButtonStates()
        }
    }

    override fun onScannerStateChanged(status: ConnectionStatus) {
        runOnUiThread {
            Toast.makeText(this, "Scanner State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IScannerOutput Implementation
     */

    /*
     * IDisplayOutput Implementation:
     */

    override fun onDisplayConnected() {
        Log.i("DISPLAY", "connected")
        displayConnected = true
        updateButtonStates()
    }

    override fun onDisplayDisconnected() {
        Log.i("DISPLAY", "disconnected")
        displayConnected = false
        updateButtonStates()
    }

    override fun onDisplayStateChanged(status: ConnectionStatus) {
        Log.i("DISPLAY", "newState: $status")
        runOnUiThread {
            Toast.makeText(this, "Display State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onScreenEvent(screenEvent: PgScreenEvent) {
        runOnUiThread {
            val toastText = when (screenEvent) {
                is PgScreenEvent.ScreenDataUpdated ->
                    "Screen data updated: ${screenEvent.componentId}"

                is PgScreenEvent.ScreenComponentClicked ->
                    "Screen component clicked: ${screenEvent.componentId}"

                is PgScreenEvent.ScreenTimerExpired ->
                    "Screen timer expired: ${screenEvent.screenContext.screenId}"
            }
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IDisplayOutput Implementation
     */

    /*
     * IButtonOutput Implementation:
     */
    override fun onButtonPressed(buttonPressed: ButtonPress) {
        runOnUiThread {
            Toast.makeText(this, "Button Pressed: ${buttonPressed.id}", Toast.LENGTH_SHORT).show()
            buttonPressed.screenContext?.let {
                binding.lastScreenContextOutput.text = "Screen ID: ${buttonPressed.screenContext?.screenId}"
            } ?: run {
                binding.lastScreenContextOutput.text = ""
            }
        }
    }
    /*
     * End of IButtonOutput Implementation
     */

    /*
     * ITriggersUnblockedOutput Implementation:
     */
    override fun onPgTriggersUnblocked() {
        runOnUiThread {
            Toast.makeText(this, "Triggers unblocked", Toast.LENGTH_SHORT).show()
        }
    }
    /*
     * End of ITriggersUnblockedOutput Implementation
     */


    /*
     * IScannerConfigurationChangeOutput Implementation:
     */
    override fun onScannerConfigurationChange(scannerConfigurationChangeResult: PgScannerConfigurationChangeResult) {
        runOnUiThread {
            var message = when (val status = scannerConfigurationChangeResult.scannerConfigurationChangeStatus) {
                is ScannerConfigurationChangeStatus.Success -> "Scanner configuration successfully changed"
                is ScannerConfigurationChangeStatus.NoScannerConfiguration -> "No scanner configuration in the profile"
                is ScannerConfigurationChangeStatus.Unknown -> "Unknown scanner configuration status"
                is ScannerConfigurationChangeStatus.Error -> "Fail to change scanner configuration, status: ${status.cause}"
            }

            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
            binding.lastResponseValue.text = message
        }
    }
    /*
     * End of IScannerConfigurationChangeOutput Implementation
     */

    companion object {

        const val DEFAULT_IMAGE_TIMEOUT = 10000
    }
}

/**
 * Profile data for displaying on UI.
 */
data class ProfileUiData(val profileId: String, var active: Boolean)


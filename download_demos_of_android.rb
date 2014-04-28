json = {
    "Background" => [
        "http://developer.android.com/downloads/samples/RepeatingAlarm.zip"
    ],
    "Connectivity" => [
        "http://developer.android.com/downloads/samples/BasicNetworking.zip",
        "http://developer.android.com/downloads/samples/BasicSyncAdapter.zip",
        "http://developer.android.com/downloads/samples/BluetoothLeGatt.zip",
        "http://developer.android.com/downloads/samples/CardEmulation.zip",
        "http://developer.android.com/downloads/samples/CardReader.zip",
        "http://developer.android.com/downloads/samples/NetworkConnect.zip"
    ],
    "Content" => [
        "http://developer.android.com/downloads/samples/AppRestrictions.zip",
        "http://developer.android.com/downloads/samples/BasicContactables.zip",
        "http://developer.android.com/downloads/samples/StorageClient.zip",
        "http://developer.android.com/downloads/samples/StorageProvider.zip"
    ],
    "Input" => [
        "http://developer.android.com/downloads/samples/BasicGestureDetect.zip",
        "http://developer.android.com/downloads/samples/BasicMultitouch.zip"
    ],
    "Media" => [
        "http://developer.android.com/downloads/samples/BasicMediaDecoder.zip",
        "http://developer.android.com/downloads/samples/BasicMediaRouter.zip",
        "http://developer.android.com/downloads/samples/MediaRecorder.zip",
        "http://developer.android.com/downloads/samples/MediaRouter.zip"
    ],
    "RenderScript" => [
        "http://developer.android.com/downloads/samples/BasicRenderScript.zip",
        "http://developer.android.com/downloads/samples/RenderScriptIntrinsic.zip"
    ],
    "Security" => [
        "http://developer.android.com/downloads/samples/BasicAndroidKeyStore.zip"
    ],
    "Sensors" => ["http://developer.android.com/downloads/samples/BatchStepSensor.zip"],
    "Testing" => ["http://developer.android.com/downloads/samples/ActivityInstrumentation.zip"],
    "UI" => ["http://developer.android.com/downloads/samples/ActionBarCompat-Basic.zip",
             "http://developer.android.com/downloads/samples/ActionBarCompat-ListPopupMenu.zip",
             "http://developer.android.com/downloads/samples/ActionBarCompat-ShareActionProvider.zip",
             "http://developer.android.com/downloads/samples/ActionBarCompat-Styled.zip",
             "http://developer.android.com/downloads/samples/AdapterTransition.zip",
             "http://developer.android.com/downloads/samples/AdvancedImmersiveMode.zip",
             "http://developer.android.com/downloads/samples/BasicAccessibility.zip",
             "http://developer.android.com/downloads/samples/BasicImmersiveMode.zip",
             "http://developer.android.com/downloads/samples/BasicNotifications.zip",
             "http://developer.android.com/downloads/samples/BasicTransition.zip",
             "http://developer.android.com/downloads/samples/BorderlessButtons.zip",
             "http://developer.android.com/downloads/samples/CustomChoiceList.zip",
             "http://developer.android.com/downloads/samples/CustomNotifications.zip",
             "http://developer.android.com/downloads/samples/CustomTransition.zip",
             "http://developer.android.com/downloads/samples/DisplayingBitmaps.zip",
             "http://developer.android.com/downloads/samples/DoneBar.zip",
             "http://developer.android.com/downloads/samples/FragmentTransition.zip",
             "http://developer.android.com/downloads/samples/HorizontalPaging.zip",
             "http://developer.android.com/downloads/samples/ImmersiveMode.zip",
             "http://developer.android.com/downloads/samples/SlidingTabsBasic.zip",
             "http://developer.android.com/downloads/samples/SlidingTabsColors.zip",
             "http://developer.android.com/downloads/samples/SwipeRefreshLayoutBasic.zip",
             "http://developer.android.com/downloads/samples/SwipeRefreshListFragment.zip",
             "http://developer.android.com/downloads/samples/SwipeRefreshMultipleViews.zip",
             "http://developer.android.com/downloads/samples/TextSwitcher.zip"],
    "Views" => ["http://developer.android.com/downloads/samples/TextLinkify.zip"]
}


json.each do |folder, arr|
  name = "~/workspace/android_demos/#{folder}"
  `mkdir -p #{name}`
  arr.each do |url|
    `
      cd #{name}
      wget -T 5 #{url}
    `
  end
end

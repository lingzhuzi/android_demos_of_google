
    
/*
*
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
 
package com.example.android.batchstepsensor;
 
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
 
import com.example.android.common.logger.Log;
import com.example.android.batchstepsensor.cardstream.Card;
import com.example.android.batchstepsensor.cardstream.CardStream;
import com.example.android.batchstepsensor.cardstream.CardStreamFragment;
import com.example.android.batchstepsensor.cardstream.OnCardClickListener;
 
public class BatchStepSensorFragment extends Fragment implements OnCardClickListener {
 
    public static final String TAG = "StepSensorSample";
    // Cards
    private CardStreamFragment mCards = null;
 
    // Card tags
    public static final String CARD_INTRO = "intro";
    public static final String CARD_REGISTER_DETECTOR = "register_detector";
    public static final String CARD_REGISTER_COUNTER = "register_counter";
    public static final String CARD_BATCHING_DESCRIPTION = "register_batching_description";
    public static final String CARD_COUNTING = "counting";
    public static final String CARD_EXPLANATION = "explanation";
    public static final String CARD_NOBATCHSUPPORT = "error";
 
    // Actions from REGISTER cards
    // Action from COUNTING card
    // Actions from description cards
 
    // State of application, used to register for sensors when app is restored
 
    // Bundle tags used to store data when restoring application state
    private static final String BUNDLE_STATE = "state";
    private static final String BUNDLE_LATENCY = "latency";
    private static final String BUNDLE_STEPS = "steps";
 
    // max batch latency is specified in microseconds
 
    /*
    For illustration we keep track of the last few events and show their delay from when the
    event occurred until it was received by the event listener.
    These variables keep track of the list of timestamps and the number of events.
     */
    // Number of events to keep in queue and display on card
    // List of timestamps when sensor events occurred
    private float[] mEventDelays = new float[EVENT_QUEUE_LENGTH];
 
    // number of events in event list
    // pointer to next entry in sensor event list
 
    // Steps counted in current session
    // Value of the step counter sensor when the listener was registered.
    // (Total steps are calculated from this value.)
    // Steps counted by the step counter previously. Used to keep counter consistent across rotation
    // changes
    // State of the app (STATE_OTHER, STATE_COUNTER or STATE_DETECTOR)
    private int mState = STATE_OTHER;
    // When a listener is registered, the batch sensor delay in microseconds
 
    @Override
    public void onResume() {
        super.onResume();
 
        CardStreamFragment stream = getCardStream();
            // No cards are visible, started for the first time
            // Prepare all cards and show the intro card.
            initialiseCards();
            showIntroCard();
            // Show the registration card if the hardware is supported, show an error otherwise
            if (isKitkatWithStepSensor()) {
                showRegisterCard();
            } else {
                showErrorCard();
            }
        }
    }
 
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        unregisterListeners();
    }
 
    /**
     * higher and has a step counter and step detector sensor.
     * This check is useful when an app provides an alternative implementation or different
     * functionality if the step sensors are not available or this code runs on a platform version
     * below Android KitKat. If this functionality is required, then the minSDK parameter should
     * be specified appropriately in the AndroidManifest.
     *
     * @return True iff the device can run this sample
     */
    private boolean isKitkatWithStepSensor() {
        // Require at least Android KitKat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        PackageManager packageManager = getActivity().getPackageManager();
        return currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }
 
    /**
     * Handles a click on a card action.
     * Registers a SensorEventListener (see {@link #registerEventListener(int, int)}) with the
     * selected delay, dismisses cards and unregisters the listener
     * (see {@link #unregisterListeners()}).
     * Actions are defined when a card is created.
     *
     * @param cardActionId
     * @param cardTag
     */
    @Override
    public void onCardClick(int cardActionId, String cardTag) {
 
        switch (cardActionId) {
            // Register Step Counter card
            case ACTION_REGISTER_COUNT_NOBATCHING:
                break;
                break;
                break;
 
            // Register Step Detector card
            case ACTION_REGISTER_DETECT_NOBATCHING:
                break;
                break;
                break;
 
            // Unregister card
            case ACTION_UNREGISTER:
                showRegisterCard();
                unregisterListeners();
                // reset the application state when explicitly unregistered
                mState = STATE_OTHER;
                break;
            // Explanation cards
            case ACTION_BATCHING_DESCRIPTION_DISMISS:
                // permanently remove the batch description card, it will not be shown again
                getCardStream().removeCard(CARD_BATCHING_DESCRIPTION);
                break;
            case ACTION_EXPLANATION_DISMISS:
                // permanently remove the explanation card, it will not be shown again
                getCardStream().removeCard(CARD_EXPLANATION);
        }
 
        // For register cards, display the counting card
        if (cardTag.equals(CARD_REGISTER_COUNTER) || cardTag.equals(CARD_REGISTER_DETECTOR)) {
            showCountingCards();
        }
    }
 
    /**
     * Register a {@link android.hardware.SensorEventListener} for the sensor and max batch delay.
     * The maximum batch delay specifies the maximum duration in microseconds for which subsequent
     * sensor events can be temporarily stored by the sensor before they are delivered to the
     * registered SensorEventListener. A larger delay allows the system to handle sensor events more
     * efficiently, allowing the system to switch to a lower power state while the sensor is
     * capturing events. Once the max delay is reached, all stored events are delivered to the
     * registered listener. Note that this value only specifies the maximum delay, the listener may
     * continuous mode.
     * higher may be appropriate for an  application that does not update the UI in real time.
     *
     * @param maxdelay
     * @param sensorType
     */
    private void registerEventListener(int maxdelay, int sensorType) {
 
        // Keep track of state so that the correct sensor type and batch delay can be set up when
        // the app is restored (for example on screen rotation).
        mMaxDelay = maxdelay;
        if (sensorType == Sensor.TYPE_STEP_COUNTER) {
            mState = STATE_COUNTER;
            /*
            Reset the initial step counter value, the first event received by the event listener is
            stored in mCounterSteps and used to calculate the total number of steps taken.
             */
            Log.i(TAG, "Event listener for step counter sensor registered with a max delay of "
                    + mMaxDelay);
        } else {
            mState = STATE_DETECTOR;
            Log.i(TAG, "Event listener for step detector sensor registered with a max delay of "
                    + mMaxDelay);
        }
 
        // Get the default sensor for the sensor type from the SenorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
 
        // Register the listener for this sensor in batch mode.
        final boolean batchMode = sensorManager.registerListener(
                mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
 
        if (!batchMode) {
            // Batch mode could not be enabled, show a warning message and switch to continuous mode
            getCardStream().getCard(CARD_NOBATCHSUPPORT)
                    .setDescription(getString(R.string.warning_nobatching));
            getCardStream().showCard(CARD_NOBATCHSUPPORT);
            Log.w(TAG, "Could not register sensor listener in batch mode, " +
                    "falling back to continuous mode.");
        }
 
            // Batch mode was enabled successfully, show a description card
            getCardStream().showCard(CARD_BATCHING_DESCRIPTION);
        }
 
        // Show the explanation card
        getCardStream().showCard(CARD_EXPLANATION);
 
 
    }
 
    /**
     * Unregisters the sensor listener if it is registered.
     */
    private void unregisterListeners() {
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(mListener);
        Log.i(TAG, "Sensor listener unregistered.");
 
    }
 
    /**
     * Resets the step counter by clearing all counting variables and lists.
     */
    private void resetCounter() {
        mEventDelays = new float[EVENT_QUEUE_LENGTH];
    }
 
 
    /**
     * Listener that handles step sensor events for step detector and step counter sensors.
     */
    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // store the delay of this event
            recordDelay(event);
            final String delayString = getDelayString();
 
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // A step detector event is received for each step.
                // This means we need to count steps ourselves
 
                mSteps += event.values.length;
 
                // Update the card with the latest step count
                getCardStream().getCard(CARD_COUNTING)
                        .setTitle(getString(R.string.counting_title, mSteps))
                        .setDescription(getString(R.string.counting_description,
                                getString(R.string.sensor_detector), mMaxDelay, delayString));
 
                Log.i(TAG,
                        "New step detected by STEP_DETECTOR sensor. Total step count: " + mSteps);
 
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
 
                /*
                A step counter event contains the total number of steps since the listener
                was first registered. We need to keep track of this initial value to calculate the
                number of steps taken, as the first value a listener receives is undefined.
                 */
                    // initial value
                }
 
                // Calculate steps taken based on first counter value received.
 
                // This is needed to keep the counter consistent across rotation changes.
                mSteps = mSteps + mPreviousCounterSteps;
 
                // Update the card with the latest step count
                getCardStream().getCard(CARD_COUNTING)
                        .setTitle(getString(R.string.counting_title, mSteps))
                        .setDescription(getString(R.string.counting_description,
                                getString(R.string.sensor_counter), mMaxDelay, delayString));
                Log.i(TAG, "New step detected by STEP_COUNTER sensor. Total step count: " + mSteps);
            }
        }
 
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
        }
    };
 
    /**
     * Records the delay for the event.
     *
     * @param event
     */
    private void recordDelay(SensorEvent event) {
        // Calculate the delay from when event was recorded until it was received here in ms
        // Event timestamp is recorded in us accuracy, but ms accuracy is sufficient here
 
        // Increment length counter
        // Move pointer to the next (oldest) location
    }
 
    private final StringBuffer mDelayStringBuffer = new StringBuffer();
 
    /**
     * Returns a string describing the sensor delays recorded in
     * {@link #recordDelay(android.hardware.SensorEvent)}.
     *
     * @return
     */
    private String getDelayString() {
        // Empty the StringBuffer
 
        // Loop over all recorded delays and append them to the buffer as a decimal
                mDelayStringBuffer.append(", ");
            }
            final int index = (mEventData + i) % EVENT_QUEUE_LENGTH;
        }
 
        return mDelayStringBuffer.toString();
    }
 
    /**
     * Records the state of the application into the {@link android.os.Bundle}.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store all variables required to restore the state of the application
        outState.putInt(BUNDLE_LATENCY, mMaxDelay);
        outState.putInt(BUNDLE_STATE, mState);
        outState.putInt(BUNDLE_STEPS, mSteps);
    }
 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Fragment is being restored, reinitialise its state with data from the bundle
        if (savedInstanceState != null) {
            resetCounter();
            mSteps = savedInstanceState.getInt(BUNDLE_STEPS);
            mState = savedInstanceState.getInt(BUNDLE_STATE);
            mMaxDelay = savedInstanceState.getInt(BUNDLE_LATENCY);
 
            // Register listeners again if in detector or counter states with restored delay
            if (mState == STATE_DETECTOR) {
                registerEventListener(mMaxDelay, Sensor.TYPE_STEP_DETECTOR);
            } else if (mState == STATE_COUNTER) {
                // store the previous number of steps to keep  step counter count consistent
                mPreviousCounterSteps = mSteps;
                registerEventListener(mMaxDelay, Sensor.TYPE_STEP_COUNTER);
            }
        }
    }
 
    /**
     * Hides the registration cards, reset the counter and show the step counting card.
     */
    private void showCountingCards() {
        // Hide the registration cards
        getCardStream().hideCard(CARD_REGISTER_DETECTOR);
        getCardStream().hideCard(CARD_REGISTER_COUNTER);
 
        // Show the explanation card if it has not been dismissed
        getCardStream().showCard(CARD_EXPLANATION);
 
        // Reset the step counter, then show the step counting card
        resetCounter();
 
        // Set the inital text for the step counting card before a step is recorded
        String sensor = "-";
        if (mState == STATE_COUNTER) {
            sensor = getString(R.string.sensor_counter);
        } else if (mState == STATE_DETECTOR) {
            sensor = getString(R.string.sensor_detector);
        }
        // Set initial text
        getCardStream().getCard(CARD_COUNTING)
                .setDescription(getString(R.string.counting_description, sensor, mMaxDelay, "-"));
 
        // Show the counting card and make it undismissable
        getCardStream().showCard(CARD_COUNTING, false);
 
    }
 
    /**
     * Show the introduction card
     */
    private void showIntroCard() {
        Card c = new Card.Builder(this, CARD_INTRO)
                .setTitle(getString(R.string.intro_title))
                .setDescription(getString(R.string.intro_message))
                .build(getActivity());
        getCardStream().addCard(c, true);
    }
 
    /**
     * Show two registration cards, one for the step detector and counter sensors.
     */
    private void showRegisterCard() {
        // Hide the counting and explanation cards
        getCardStream().hideCard(CARD_BATCHING_DESCRIPTION);
        getCardStream().hideCard(CARD_EXPLANATION);
        getCardStream().hideCard(CARD_COUNTING);
 
        // Show two undismissable registration cards, one for each step sensor
        getCardStream().showCard(CARD_REGISTER_DETECTOR, false);
        getCardStream().showCard(CARD_REGISTER_COUNTER, false);
    }
 
    /**
     * Show the error card.
     */
    private void showErrorCard() {
        getCardStream().showCard(CARD_NOBATCHSUPPORT, false);
    }
 
    /**
     * Initialise Cards.
     */
    private void initialiseCards() {
        // Step counting
        Card c = new Card.Builder(this, CARD_COUNTING)
                .setTitle("Steps")
                .setDescription("")
                .addAction("Unregister Listener", ACTION_UNREGISTER, Card.ACTION_NEGATIVE)
                .build(getActivity());
        getCardStream().addCard(c);
 
        // Register step detector listener
        c = new Card.Builder(this, CARD_REGISTER_DETECTOR)
                .setTitle(getString(R.string.register_detector_title))
                .setDescription(getString(R.string.register_detector_description))
                        ACTION_REGISTER_DETECT_NOBATCHING, Card.ACTION_NEUTRAL)
                .build(getActivity());
        getCardStream().addCard(c);
 
        // Register step counter listener
        c = new Card.Builder(this, CARD_REGISTER_COUNTER)
                .setTitle(getString(R.string.register_counter_title))
                .setDescription(getString(R.string.register_counter_description))
                        ACTION_REGISTER_COUNT_NOBATCHING, Card.ACTION_NEUTRAL)
                .build(getActivity());
        getCardStream().addCard(c);
 
 
        // Batching description
        c = new Card.Builder(this, CARD_BATCHING_DESCRIPTION)
                .setTitle(getString(R.string.batching_queue_title))
                .setDescription(getString(R.string.batching_queue_description))
                .addAction(getString(R.string.action_notagain),
                        ACTION_BATCHING_DESCRIPTION_DISMISS, Card.ACTION_POSITIVE)
                .build(getActivity());
        getCardStream().addCard(c);
 
        // Explanation
        c = new Card.Builder(this, CARD_EXPLANATION)
                .setDescription(getString(R.string.explanation_description))
                .addAction(getString(R.string.action_notagain),
                        ACTION_EXPLANATION_DISMISS, Card.ACTION_POSITIVE)
                .build(getActivity());
        getCardStream().addCard(c);
 
        // Error
        c = new Card.Builder(this, CARD_NOBATCHSUPPORT)
                .setTitle(getString(R.string.error_title))
                .setDescription(getString(R.string.error_nosensor))
                .build(getActivity());
        getCardStream().addCard(c);
    }
 
    /**
     * Returns the cached CardStreamFragment used to show cards.
     *
     * @return
     */
    private CardStreamFragment getCardStream() {
        if (mCards == null) {
            mCards = ((CardStream) getActivity()).getCardStream();
        }
        return mCards;
    }
 
}
  


    
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
 
package com.example.android.basicmultitouch;
 
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
 
import com.example.android.basicmultitouch.Pools.SimplePool;
 
/**
 * View that shows touch events and their history. This view demonstrates the
 * use of {@link #onTouchEvent(android.view.MotionEvent)} and {@link android.view.MotionEvent}s to keep
 * track of touch pointers across events.
 */
public class TouchDisplayView extends View {
 
    // Hold data for active touch pointer IDs
    private SparseArray<TouchHistory> mTouches;
 
    // Is there an active touch?
    private boolean mHasTouch = false;
 
    /**
     * Holds data related to a touch pointer, including its current position,
     * pressure and historical positions. Objects are allocated through an
     * object pool using {@link #obtain()} and {@link #recycle()} to reuse
     * existing objects.
     */
    static final class TouchHistory {
 
        // number of historical points to store
 
        public float x;
        public float y;
        public String label = null;
 
        // current position in history array
 
        // arrray of pointer position history
        public PointF[] history = new PointF[HISTORY_COUNT];
 
        private static final SimplePool<TouchHistory> sPool =
                new SimplePool<TouchHistory>(MAX_POOL_SIZE);
 
        public static TouchHistory obtain(float x, float y, float pressure) {
            TouchHistory data = sPool.acquire();
            if (data == null) {
                data = new TouchHistory();
            }
 
            data.setTouch(x, y, pressure);
 
            return data;
        }
 
        public TouchHistory() {
 
            // initialise history array
                history[i] = new PointF();
            }
        }
 
        public void setTouch(float x, float y, float pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }
 
        public void recycle() {
            sPool.release(this);
        }
 
        /**
         * Add a point to its history. Overwrites oldest point if the maximum
         * number of historical points is already stored.
         *
         * @param point
         */
        public void addHistory(float x, float y) {
            PointF p = history[historyIndex];
            p.x = x;
            p.y = y;
 
 
            if (historyCount < HISTORY_COUNT) {
                historyCount++;
            }
        }
 
    }
 
    public TouchDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
 
        // SparseArray for touch events, indexed by touch id
 
        initialisePaint();
    }
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
 
        final int action = event.getAction();
 
        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {
 
            case MotionEvent.ACTION_DOWN: {
                // first pressed gesture has started
 
                /*
                 * Only one touch event is stored in the MotionEvent. Extract
                 * the pointer identifier of this touch from the first index
                 * within the MotionEvent object.
                 */
 
 
                /*
                 * Store the data under its pointer identifier. The pointer
                 * number stays consistent for the duration of a gesture,
                 * accounting for other pointers going up or down.
                 */
                mTouches.put(id, data);
 
                mHasTouch = true;
 
                break;
            }
 
            case MotionEvent.ACTION_POINTER_DOWN: {
                /*
                 * A non-primary pointer has gone down, after an event for the
                 * primary pointer (ACTION_DOWN) has already been received.
                 */
 
                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
 
                TouchHistory data = TouchHistory.obtain(event.getX(index), event.getY(index),
                        event.getPressure(index));
                data.label = "id: " + id;
 
                /*
                 * Store the data under its pointer identifier. The index of
                 * this pointer can change over multiple events, but this
                 * pointer is always identified by the same identifier for this
                 * active gesture.
                 */
                mTouches.put(id, data);
 
                break;
            }
 
            case MotionEvent.ACTION_UP: {
                /*
                 * Final pointer has gone up and has ended the last pressed
                 * gesture.
                 */
 
                /*
                 * Extract the pointer identifier for the only event stored in
                 * the MotionEvent object and remove it from the list of active
                 * touches.
                 */
                TouchHistory data = mTouches.get(id);
                mTouches.remove(id);
                data.recycle();
 
                mHasTouch = false;
 
                break;
            }
 
            case MotionEvent.ACTION_POINTER_UP: {
                /*
                 * A non-primary pointer has gone up and other pointers are
                 * still active.
                 */
 
                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
 
                TouchHistory data = mTouches.get(id);
                mTouches.remove(id);
                data.recycle();
 
                break;
            }
 
            case MotionEvent.ACTION_MOVE: {
                /*
                 * A change event happened during a pressed gesture. (Between
                 * ACTION_DOWN and ACTION_UP or ACTION_POINTER_DOWN and
                 * ACTION_POINTER_UP)
                 */
 
                /*
                 * Loop through all active pointers contained within this event.
                 * Data for each pointer is stored in a MotionEvent at an index
                 * loop goes through each of these active pointers, extracts its
                 * data (position and pressure) and updates its stored data. A
                 * pointer is identified by its pointer number which stays
                 * constant across touch events as long as it remains active.
                 * This identifier is used to keep track of a pointer across
                 * events.
                 */
                    // get pointer id for data stored at this index
                    int id = event.getPointerId(index);
 
                    // get the data stored externally about this pointer.
                    TouchHistory data = mTouches.get(id);
 
                    // add previous position to history and add new values
                    data.addHistory(data.x, data.y);
                    data.setTouch(event.getX(index), event.getY(index),
                            event.getPressure(index));
 
                }
 
                break;
            }
        }
 
        // trigger redraw on UI thread
        this.postInvalidate();
 
        return true;
    }
 
 
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
 
        // Canvas background color depends on whether there is an active touch
        if (mHasTouch) {
            canvas.drawColor(BACKGROUND_ACTIVE);
        } else {
            // draw inactive border
            canvas.drawRect(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth, getHeight()
                    - mBorderWidth, mBorderPaint);
        }
 
        // loop through all active touches and draw them
 
            // get the pointer id and associated data for this index
            int id = mTouches.keyAt(i);
            TouchHistory data = mTouches.valueAt(i);
 
            // draw the data and its history to the canvas
            drawCircle(canvas, id, data);
        }
    }
 
    /*
     * Below are only helper methods and variables required for drawing.
     */
 
    // radius of active touch circle in dp
    // radius of historical circle in dp
 
    // calculated radiuses in px
    private float mCircleRadius;
    private float mCircleHistoricalRadius;
 
    private Paint mCirclePaint = new Paint();
    private Paint mTextPaint = new Paint();
 
    private static final int BACKGROUND_ACTIVE = Color.WHITE;
 
    // inactive border
    private Paint mBorderPaint = new Paint();
    private float mBorderWidth;
 
    public final int[] COLORS = {
    };
 
    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {
 
        // Calculate radiuses in px from dp based on screen density
        float density = getResources().getDisplayMetrics().density;
        mCircleRadius = CIRCLE_RADIUS_DP * density;
        mCircleHistoricalRadius = CIRCLE_HISTORICAL_RADIUS_DP * density;
 
        // Setup text paint for circle label
        mTextPaint.setColor(Color.BLACK);
 
        // Setup paint for inactive border
        mBorderWidth = INACTIVE_BORDER_DP * density;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(INACTIVE_BORDER_COLOR);
        mBorderPaint.setStyle(Paint.Style.STROKE);
 
    }
 
    /**
     * Draws the data encapsulated by a {@link TouchDisplayView.TouchHistory} object to a canvas.
     * A large circle indicates the current position held by the
     * {@link TouchDisplayView.TouchHistory} object, while a smaller circle is drawn for each
     * entry in its history. The size of the large circle is scaled depending on
     *
     * @param canvas
     * @param id
     * @param data
     */
    protected void drawCircle(Canvas canvas, int id, TouchHistory data) {
        // select the color based on the id
        int color = COLORS[id % COLORS.length];
        mCirclePaint.setColor(color);
 
        /*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         */
        float radius = pressure * mCircleRadius;
 
                mCirclePaint);
 
        // draw all historical points with a lower alpha value
            PointF p = data.history[j];
            canvas.drawCircle(p.x, p.y, mCircleHistoricalRadius, mCirclePaint);
        }
 
        // draw its label next to the main circle
        canvas.drawText(data.label, data.x + radius, data.y
                - radius, mTextPaint);
    }
 
}
  

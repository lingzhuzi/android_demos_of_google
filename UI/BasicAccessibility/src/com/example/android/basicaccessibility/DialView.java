
    
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
 
package com.example.android.basicaccessibility;
 
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
 
/**
 * Custom view to demonstrate accessibility.
 *
 * <p>This view does not use any framework widgets, so does not get any accessibility features
 * automatically. Instead, we use {@link android.view.accessibility.AccessibilityEvent} to provide accessibility hints to
 * the OS.
 *
 * <p>For example, if TalkBack is enabled, users will be able to receive spoken feedback as they
 * interact with this view.
 *
 * <p>More generally, this view renders a multi-position "dial" that can be used to select a value
 * the maximum number of positions).
 */
public class DialView extends View {
 
    private float mWidth;
    private float mHeight;
    private float mWidthPadded;
    private float mHeightPadded;
    private Paint mTextPaint;
    private Paint mDialPaint;
    private float mRadius;
    private int mActiveSelection;
 
    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file.
     *
     * <p>In our case, this constructor just calls init().
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #View(android.content.Context, android.util.AttributeSet, int)
     */
    public DialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
 
    /**
     * Helper method to initialize instance variables. Called by constructor.
     */
    private void init() {
        // Paint styles used for rendering are created here, rather than at render-time. This
        // is a performance optimization, since onDraw() will get called frequently.
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(FONT_SIZE);
 
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setColor(Color.GRAY);
 
        // Initialize current selection. This will store where the dial's "indicator" is pointing.
 
        // Setup onClick listener for this view. Rotates between each of the different selection
        // states on each click.
        //
        // Notice that we call sendAccessibilityEvent here. Some AccessibilityEvents are generated
        // by the system. However, custom views will typically need to send events manually as the
        // user interacts with the view. The type of event sent will vary, depending on the nature
        // of the view and how the user interacts with it.
        //
        // In this case, we are sending TYPE_VIEW_SELECTED rather than TYPE_VIEW_CLICKED, because
        // clicking on this view selects a new value.
        //
        // We will give our AccessibilityEvent further information about the state of the view in
        // onPopulateAccessibilityEvent(), which will be called automatically by the system
        // for each AccessibilityEvent.
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rotate selection to the next valid choice.
                // Send an AccessibilityEvent, since the user has interacted with the view.
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                // Redraw the entire view. (Inefficient, but this is sufficient for demonstration
                // purposes.)
                invalidate();
            }
        });
    }
 
    /**
     * This is where a View should populate outgoing accessibility events with its text content.
     * While this method is free to modify event attributes other than text content, doing so
     * should normally be performed in
     * {@link #onInitializeAccessibilityEvent(android.view.accessibility.AccessibilityEvent)}.
     * <p/>
     * <p>Note that the behavior of this method will typically vary, depending on the type of
     * accessibility event is passed into it. The allowed values also very, and are documented
     * in {@link android.view.accessibility.AccessibilityEvent}.
     * <p/>
     * <p>Typically, this is where you'll describe the state of your custom view. You may also
     * want to provide custom directions when the user has focused your view.
     *
     * @param event The accessibility event which to populate.
     */
    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
 
        // Detect what type of accessibility event is being passed in.
        int eventType = event.getEventType();
 
        // Common case: The user has interacted with our view in some way. State may or may not
        // have been changed. Read out the current status of the view.
        //
        // We also set some other metadata which is not used by TalkBack, but could be used by
        // other TTS engines.
        if (eventType == AccessibilityEvent.TYPE_VIEW_SELECTED ||
                eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            event.setItemCount(SELECTION_COUNT);
            event.setCurrentItemIndex(mActiveSelection);
        }
 
        // When a user first focuses on our view, we'll also read out some simple instructions to
        // make it clear that this is an interactive element.
        if (eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            event.getText().add("Tap to change.");
        }
    }
 
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     *
     * <p>This is where we determine the drawing bounds for our custom view.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Account for padding
        float xPadding = (float) (getPaddingLeft() + getPaddingRight());
        float yPadding = (float) (getPaddingTop() + getPaddingBottom());
 
        // Compute available width/height
        mWidth = w;
        mHeight = h;
        mWidthPadded = w - xPadding;
        mHeightPadded = h - yPadding;
    }
 
    /**
     * Render view content.
     *
     * <p>We render an outer grey circle to serve as our "dial", and then render a smaller black
     * circle to server as our indicator. The position for the indicator is determined based
     * on mActiveSelection.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw dial
 
        // Draw text labels
            float[] xyData = computeXYForPosition(i, labelRadius);
        }
 
        // Draw indicator mark
        float[] xyData = computeXYForPosition(mActiveSelection, markerRadius);
    }
 
    /**
     * Compute the X/Y-coordinates for a label or indicator, given the position number and radius
     * where the label should be drawn.
     *
     * @param pos    Zero based position index
     * @param radius Radius where label/indicator is to be drawn.
     */
    private float[] computeXYForPosition(final int pos, final float radius) {
        return result;
    }
}
  

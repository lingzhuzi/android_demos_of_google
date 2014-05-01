
    
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
 
package com.example.android.renderscriptintrinsic;
 
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
 
public class MainActivity extends Activity {
    /* Number of bitmaps that is used for renderScript thread and UI thread synchronization.
       Investigating a root cause.
     */
    private Bitmap mBitmapIn;
    private Bitmap[] mBitmapsOut;
    private ImageView mImageView;
 
    private RenderScript mRS;
    private Allocation mInAllocation;
    private Allocation[] mOutAllocations;
 
    private ScriptIntrinsicBlur mScriptBlur;
    private ScriptIntrinsicColorMatrix mScriptMatrix;
 
 
    private int mFilterMode = MODE_BLUR;
 
    private RenderScriptTask mLatestTask = null;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setContentView(R.layout.main_layout);
 
        /*
         * Initialize UI
         */
 
        //Set up main image view
        mBitmapIn = loadBitmap(R.drawable.data);
        mBitmapsOut = new Bitmap[NUM_BITMAPS];
            mBitmapsOut[i] = Bitmap.createBitmap(mBitmapIn.getWidth(),
                    mBitmapIn.getHeight(), mBitmapIn.getConfig());
        }
 
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmapsOut[mCurrentBitmap]);
 
        //Set up seekbar
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updateImage(progress);
            }
 
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
 
        //Setup effect selector
 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFilterMode = MODE_BLUR;
                    updateImage(seekbar.getProgress());
                }
            }
        });
 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFilterMode = MODE_CONVOLVE;
                    updateImage(seekbar.getProgress());
                }
            }
        });
 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFilterMode = MODE_COLORMATRIX;
                    updateImage(seekbar.getProgress());
                }
            }
        });
 
        /*
         * Create renderScript
         */
        createScript();
 
        /*
         * Create thumbnails
         */
        createThumbnail();
 
 
        /*
         * Invoke renderScript kernel and update imageView
         */
        mFilterMode = MODE_BLUR;
    }
 
    private void createScript() {
        mRS = RenderScript.create(this);
 
        mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn);
 
        mOutAllocations = new Allocation[NUM_BITMAPS];
            mOutAllocations[i] = Allocation.createFromBitmap(mRS, mBitmapsOut[i]);
        }
 
        /*
        Create intrinsics.
        RenderScript has built-in features such as blur, convolve filter etc.
        These intrinsics are handy for specific operations without writing RenderScript kernel.
        In the sample, it's creating blur, convolve and matrix intrinsics.
         */
 
        mScriptMatrix = ScriptIntrinsicColorMatrix.create(mRS,
    }
 
    private void performFilter(Allocation inAllocation,
                               Allocation outAllocation, Bitmap bitmapOut, float value) {
        switch (mFilterMode) {
            case MODE_BLUR:
            /*
             * Set blur kernel size
             */
                mScriptBlur.setRadius(value);
 
            /*
             * Invoke filter kernel
             */
                mScriptBlur.setInput(inAllocation);
                mScriptBlur.forEach(outAllocation);
                break;
            case MODE_CONVOLVE: {
 
                // Emboss filter kernel
            /*
             * Set kernel parameter
             */
                mScriptConvolve.setCoefficients(coefficients);
 
            /*
             * Invoke filter kernel
             */
                mScriptConvolve.setInput(inAllocation);
                mScriptConvolve.forEach(outAllocation);
                break;
            }
            case MODE_COLORMATRIX: {
            /*
             * Set HUE rotation matrix
             * The matrix below performs a combined operation of,
             * RGB->HSV transform * HUE rotation * HSV->RGB transform
             */
                float cos = (float) Math.cos((double) value);
                float sin = (float) Math.sin((double) value);
                mScriptMatrix.setColorMatrix(mat);
 
            /*
             * Invoke filter kernel
             */
                mScriptMatrix.forEach(inAllocation, outAllocation);
            }
            break;
        }
 
        /*
         * Copy to bitmap and invalidate image view
         */
        outAllocation.copyTo(bitmapOut);
    }
 
    /*
     */
    private float getFilterParameter(int i) {
        switch (mFilterMode) {
            case MODE_BLUR: {
            }
            break;
            case MODE_CONVOLVE: {
            }
            break;
            case MODE_COLORMATRIX: {
                final float max = (float) Math.PI;
                final float min = (float) -Math.PI;
            }
            break;
        }
        return f;
 
    }
 
    /*
     * In the AsyncTask, it invokes RenderScript intrinsics to do a filtering.
     * After the filtering is done, an operation blocks at Allication.copyTo() in AsyncTask thread.
     * Once all operation is finished at onPostExecute() in UI thread, it can invalidate and update ImageView UI.
     */
    private class RenderScriptTask extends AsyncTask<Float, Integer, Integer> {
        Boolean issued = false;
 
        protected Integer doInBackground(Float... values) {
            if (isCancelled() == false) {
                issued = true;
                index = mCurrentBitmap;
 
            }
            return index;
        }
 
        void updateView(Integer result) {
                // Request UI update
                mImageView.setImageBitmap(mBitmapsOut[result]);
                mImageView.invalidate();
            }
        }
 
        protected void onPostExecute(Integer result) {
            updateView(result);
        }
 
        protected void onCancelled(Integer result) {
            if (issued) {
                updateView(result);
            }
        }
    }
 
    /*
    Invoke AsynchTask and cancel previous task.
    When AsyncTasks are piled up (typically in slow device with heavy kernel),
    Only the latest (and already started) task invokes RenderScript operation.
     */
    private void updateImage(int progress) {
        float f = getFilterParameter(progress);
 
        if (mLatestTask != null)
            mLatestTask.cancel(false);
        mLatestTask = new RenderScriptTask();
 
        mLatestTask.execute(f);
    }
 
    /*
    Helper to load Bitmap from resource
     */
    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }
 
    /*
    Create thumbNail for UI. It invokes RenderScript kernel synchronously in UI-thread,
    which is OK for small thumbnail (but not ideal).
     */
    private void createThumbnail() {
        float scale = getResources().getDisplayMetrics().density;
 
        //Temporary image
        Bitmap tempBitmap = Bitmap.createScaledBitmap(mBitmapIn, pixelsWidth, pixelsHeight, false);
        Allocation inAllocation = Allocation.createFromBitmap(mRS, tempBitmap);
 
        //Create thumbnail with each RS intrinsic and set it to radio buttons
        int[] modes = {MODE_BLUR, MODE_CONVOLVE, MODE_COLORMATRIX};
        for (int mode : modes) {
            mFilterMode = mode;
            float f = getFilterParameter(parameter[mode]);
 
            Bitmap destBitpmap = Bitmap.createBitmap(tempBitmap.getWidth(),
                    tempBitmap.getHeight(), tempBitmap.getConfig());
            Allocation outAllocation = Allocation.createFromBitmap(mRS, destBitpmap);
            performFilter(inAllocation, outAllocation, destBitpmap, f);
 
            ThumbnailRadioButton button = (ThumbnailRadioButton) findViewById(ids[mode]);
            button.setThumbnail(destBitpmap);
        }
    }
}
  

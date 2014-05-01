
    
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
 
 
 
 
package com.example.android.batchstepsensor.cardstream;
 
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
 
class DefaultCardStreamAnimator extends CardStreamAnimator {
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public ObjectAnimator getDisappearingAnimator(Context context){
 
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(new Object(),
 
        return animator;
    }
 
    @Override
    public ObjectAnimator getAppearingAnimator(Context context){
 
        final Point outPoint = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(outPoint);
 
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(new Object(),
 
        return animator;
    }
 
    @Override
    public ObjectAnimator getInitalAnimator(Context context){
 
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(new Object(),
 
        return animator;
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public ObjectAnimator getSwipeInAnimator(View view, float deltaX, float deltaY){
 
        float deltaXAbs = Math.abs(deltaX);
 
 
        // Animate position and alpha of swiped item
 
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view,
 
        animator.setDuration(duration).setInterpolator(new BounceInterpolator());
 
        return  animator;
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public ObjectAnimator getSwipeOutAnimator(View view, float deltaX, float deltaY){
 
        float endX;
        float endRotationY;
 
        float deltaXAbs = Math.abs(deltaX);
 
 
        else
 
        // Animate position and alpha of swiped item
        return ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("translationX", endX),
                PropertyValuesHolder.ofFloat("rotationY", endRotationY)).setDuration(duration);
 
    }
 
}
  

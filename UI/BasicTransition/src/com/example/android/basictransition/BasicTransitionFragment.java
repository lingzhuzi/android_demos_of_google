
    
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
 
package com.example.android.basictransition;
 
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
 
public class BasicTransitionFragment extends Fragment
        implements RadioGroup.OnCheckedChangeListener {
 
    // We transition between these Scenes
 
    /** A custom TransitionManager */
 
    private ViewGroup mSceneRoot;
 
    public static BasicTransitionFragment newInstance() {
        return new BasicTransitionFragment();
    }
 
    public BasicTransitionFragment() {
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_transition, container, false);
        assert view != null;
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.select_scene);
        radioGroup.setOnCheckedChangeListener(this);
        mSceneRoot = (ViewGroup) view.findViewById(R.id.scene_root);
 
        // A Scene can be instantiated from a live view hierarchy.
 
        // You can also inflate a generate a Scene from a layout resource file.
 
        // Another scene from a layout resource file.
 
        // take place at the same time.
 
        return view;
    }
 
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
                // You can start an automatic transition with TransitionManager.go().
                break;
            }
                break;
            }
                // You can also start a transition with a custom TransitionManager.
                break;
            }
                // Alternatively, transition can be invoked dynamically without a Scene.
                // For this, we first call TransitionManager.beginDelayedTransition().
                TransitionManager.beginDelayedTransition(mSceneRoot);
                // Then, we can just change view properties as usual.
                View square = mSceneRoot.findViewById(R.id.transition_square);
                ViewGroup.LayoutParams params = square.getLayoutParams();
                int newSize = getResources().getDimensionPixelSize(R.dimen.square_size_expanded);
                params.width = newSize;
                params.height = newSize;
                square.setLayoutParams(params);
                break;
            }
        }
    }
 
}
  

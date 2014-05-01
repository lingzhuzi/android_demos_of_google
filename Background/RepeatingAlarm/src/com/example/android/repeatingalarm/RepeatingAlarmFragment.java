
    
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
 
package com.example.android.repeatingalarm;
 
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import com.example.android.common.logger.*;
 
 
public class RepeatingAlarmFragment extends Fragment {
 
    // This value is defined and consumed by app code, so any value will work.
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sample_action) {
 
            // First create an intent for the alarm to activate.
            // This code simply starts an Activity, or brings it to the front if it has already
            // been created.
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 
            // Because the intent must be fired by a system service from outside the application,
            // it's necessary to wrap it in a PendingIntent.  Providing a different process with
            // a PendingIntent gives that other process permission to fire the intent that this
            // application has created.
            // Also, this code creates a PendingIntent to start an Activity.  To create a
            // BroadcastIntent instead, simply call getBroadcast instead of getIntent.
            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), REQUEST_CODE,
 
 
            // There are two clock types for alarms, ELAPSED_REALTIME and RTC.
            // ELAPSED_REALTIME uses time since system boot as a reference, and RTC uses UTC (wall
            // clock) time.  This means ELAPSED_REALTIME is suited to setting an alarm according to
            // timezone/locale.  RTC is better suited for alarms that should be dependant on current
            // locale.
 
            // Both types have a WAKEUP version, which says to wake up the device if the screen is
            // off.  This is useful for situations such as alarm clocks.  Abuse of this flag is an
            // efficient way to skyrocket the uninstall rate of an application, so use with care.
            // For most situations, ELAPSED_REALTIME will suffice.
            int alarmType = AlarmManager.ELAPSED_REALTIME;
 
            // The AlarmManager, like most system services, isn't created by application code, but
            // requested from the system.
            AlarmManager alarmManager = (AlarmManager)
                    getActivity().getSystemService(getActivity().ALARM_SERVICE);
 
            // setRepeating takes a start delay and period between alarms as arguments.
            // useful for demonstration purposes, but horrendous for production.  Don't be that dev.
            alarmManager.setRepeating(alarmType, SystemClock.elapsedRealtime() + FIFTEEN_SEC_MILLIS,
                    FIFTEEN_SEC_MILLIS, pendingIntent);
            Log.i("RepeatingAlarmFragment", "Alarm set.");
        }
        return true;
    }
}
  

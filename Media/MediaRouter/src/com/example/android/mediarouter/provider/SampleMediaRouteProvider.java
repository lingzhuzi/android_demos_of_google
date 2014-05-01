
    
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
 
package com.example.android.mediarouter.provider;
 
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
 
import com.example.android.mediarouter.player.Player;
import com.example.android.mediarouter.player.PlaylistItem;
import com.example.android.mediarouter.R;
import com.example.android.mediarouter.player.SessionManager;
 
import java.util.ArrayList;
 
/**
 * Demonstrates how to create a custom media route provider.
 *
 * @see SampleMediaRouteProviderService
 */
public final class SampleMediaRouteProvider extends MediaRouteProvider {
    private static final String TAG = "SampleMediaRouteProvider";
 
    private static final String FIXED_VOLUME_ROUTE_ID = "fixed";
    private static final String VARIABLE_VOLUME_BASIC_ROUTE_ID = "variable_basic";
    private static final String VARIABLE_VOLUME_QUEUING_ROUTE_ID = "variable_queuing";
    private static final String VARIABLE_VOLUME_SESSION_ROUTE_ID = "variable_session";
 
    /**
     * A custom media control intent category for special requests that are
     * supported by this provider's routes.
     */
    public static final String CATEGORY_SAMPLE_ROUTE =
            "com.example.android.mediarouteprovider.CATEGORY_SAMPLE_ROUTE";
 
    /**
     * A custom media control intent action for special requests that are
     * supported by this provider's routes.
     * <p>
     * This particular request is designed to return a bundle of not very
     * interesting statistics for demonstration purposes.
     * </p>
     *
     * @see #DATA_PLAYBACK_COUNT
     */
    public static final String ACTION_GET_STATISTICS =
            "com.example.android.mediarouteprovider.ACTION_GET_STATISTICS";
 
    /**
     * {@link #ACTION_GET_STATISTICS} result data: Number of times the
     * playback action was invoked.
     */
    public static final String DATA_PLAYBACK_COUNT =
            "com.example.android.mediarouteprovider.EXTRA_PLAYBACK_COUNT";
 
    private static final ArrayList<IntentFilter> CONTROL_FILTERS_BASIC;
    private static final ArrayList<IntentFilter> CONTROL_FILTERS_QUEUING;
    private static final ArrayList<IntentFilter> CONTROL_FILTERS_SESSION;
 
    static {
 
 
 
 
 
 
        CONTROL_FILTERS_BASIC = new ArrayList<IntentFilter>();
 
        CONTROL_FILTERS_QUEUING =
                new ArrayList<IntentFilter>(CONTROL_FILTERS_BASIC);
 
        CONTROL_FILTERS_SESSION =
                new ArrayList<IntentFilter>(CONTROL_FILTERS_QUEUING);
    }
 
    private static void addDataTypeUnchecked(IntentFilter filter, String type) {
        try {
            filter.addDataType(type);
        } catch (MalformedMimeTypeException ex) {
            throw new RuntimeException(ex);
        }
    }
 
    private int mEnqueueCount;
 
    public SampleMediaRouteProvider(Context context) {
        super(context);
 
        publishRoutes();
    }
 
    @Override
    public RouteController onCreateRouteController(String routeId) {
        return new SampleRouteController(routeId);
    }
 
    private void publishRoutes() {
        Resources r = getContext().getResources();
 
                FIXED_VOLUME_ROUTE_ID,
                r.getString(R.string.fixed_volume_route_name))
                .setDescription(r.getString(R.string.sample_route_description))
                .addControlFilters(CONTROL_FILTERS_BASIC)
                .setPlaybackStream(AudioManager.STREAM_MUSIC)
                .setPlaybackType(MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE)
                .setVolumeHandling(MediaRouter.RouteInfo.PLAYBACK_VOLUME_FIXED)
                .setVolume(VOLUME_MAX)
                .build();
 
                VARIABLE_VOLUME_BASIC_ROUTE_ID,
                r.getString(R.string.variable_volume_basic_route_name))
                .setDescription(r.getString(R.string.sample_route_description))
                .addControlFilters(CONTROL_FILTERS_BASIC)
                .setPlaybackStream(AudioManager.STREAM_MUSIC)
                .setPlaybackType(MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE)
                .setVolumeHandling(MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE)
                .setVolumeMax(VOLUME_MAX)
                .setVolume(mVolume)
                .build();
 
                VARIABLE_VOLUME_QUEUING_ROUTE_ID,
                r.getString(R.string.variable_volume_queuing_route_name))
                .setDescription(r.getString(R.string.sample_route_description))
                .addControlFilters(CONTROL_FILTERS_QUEUING)
                .setPlaybackStream(AudioManager.STREAM_MUSIC)
                .setPlaybackType(MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE)
                .setVolumeHandling(MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE)
                .setVolumeMax(VOLUME_MAX)
                .setVolume(mVolume)
                .build();
 
                VARIABLE_VOLUME_SESSION_ROUTE_ID,
                r.getString(R.string.variable_volume_session_route_name))
                .setDescription(r.getString(R.string.sample_route_description))
                .addControlFilters(CONTROL_FILTERS_SESSION)
                .setPlaybackStream(AudioManager.STREAM_MUSIC)
                .setPlaybackType(MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE)
                .setVolumeHandling(MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE)
                .setVolumeMax(VOLUME_MAX)
                .setVolume(mVolume)
                .build();
 
        MediaRouteProviderDescriptor providerDescriptor =
                new MediaRouteProviderDescriptor.Builder()
                .build();
        setDescriptor(providerDescriptor);
    }
 
    private final class SampleRouteController extends MediaRouteProvider.RouteController {
        private final String mRouteId;
        private final SessionManager mSessionManager = new SessionManager("mrp");
        private final Player mPlayer;
        private PendingIntent mSessionReceiver;
 
        public SampleRouteController(String routeId) {
            mRouteId = routeId;
            mPlayer = Player.create(getContext(), null);
            mSessionManager.setPlayer(mPlayer);
            mSessionManager.setCallback(new SessionManager.Callback() {
                @Override
                public void onStatusChanged() {
                }
 
                @Override
                public void onItemChanged(PlaylistItem item) {
                    handleStatusChange(item);
                }
            });
            Log.d(TAG, mRouteId + ": Controller created");
        }
 
        @Override
        public void onRelease() {
            Log.d(TAG, mRouteId + ": Controller released");
            mPlayer.release();
        }
 
        @Override
        public void onSelect() {
            Log.d(TAG, mRouteId + ": Selected");
            mPlayer.connect(null);
        }
 
        @Override
        public void onUnselect() {
            Log.d(TAG, mRouteId + ": Unselected");
            mPlayer.release();
        }
 
        @Override
        public void onSetVolume(int volume) {
            Log.d(TAG, mRouteId + ": Set volume to " + volume);
            if (!mRouteId.equals(FIXED_VOLUME_ROUTE_ID)) {
                setVolumeInternal(volume);
            }
        }
 
        @Override
        public void onUpdateVolume(int delta) {
            Log.d(TAG, mRouteId + ": Update volume by " + delta);
            if (!mRouteId.equals(FIXED_VOLUME_ROUTE_ID)) {
                setVolumeInternal(mVolume + delta);
            }
        }
 
        @Override
        public boolean onControlRequest(Intent intent, ControlRequestCallback callback) {
            Log.d(TAG, mRouteId + ": Received control request " + intent);
            String action = intent.getAction();
            if (intent.hasCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                boolean success = false;
                if (action.equals(MediaControlIntent.ACTION_PLAY)) {
                    success = handlePlay(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_ENQUEUE)) {
                    success = handleEnqueue(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_REMOVE)) {
                    success = handleRemove(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_SEEK)) {
                    success = handleSeek(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_GET_STATUS)) {
                    success = handleGetStatus(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_PAUSE)) {
                    success = handlePause(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_RESUME)) {
                    success = handleResume(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_STOP)) {
                    success = handleStop(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_START_SESSION)) {
                    success = handleStartSession(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_GET_SESSION_STATUS)) {
                    success = handleGetSessionStatus(intent, callback);
                } else if (action.equals(MediaControlIntent.ACTION_END_SESSION)) {
                    success = handleEndSession(intent, callback);
                }
                Log.d(TAG, mSessionManager.toString());
                return success;
            }
 
            if (action.equals(ACTION_GET_STATISTICS)
                    && intent.hasCategory(CATEGORY_SAMPLE_ROUTE)) {
                Bundle data = new Bundle();
                data.putInt(DATA_PLAYBACK_COUNT, mEnqueueCount);
                if (callback != null) {
                    callback.onResult(data);
                }
                return true;
            }
            return false;
        }
 
        private void setVolumeInternal(int volume) {
                mVolume = volume;
                Log.d(TAG, mRouteId + ": New volume is " + mVolume);
                AudioManager audioManager =
                        (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
                publishRoutes();
            }
        }
 
        private boolean handlePlay(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            if (sid != null && !sid.equals(mSessionManager.getSessionId())) {
                Log.d(TAG, "handlePlay fails because of bad sid="+sid);
                return false;
            }
            if (mSessionManager.hasSession()) {
                mSessionManager.stop();
            }
            return handleEnqueue(intent, callback);
        }
 
        private boolean handleEnqueue(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            if (sid != null && !sid.equals(mSessionManager.getSessionId())) {
                Log.d(TAG, "handleEnqueue fails because of bad sid="+sid);
                return false;
            }
 
            Uri uri = intent.getData();
            if (uri == null) {
                Log.d(TAG, "handleEnqueue fails because of bad uri="+uri);
                return false;
            }
 
            boolean enqueue = intent.getAction().equals(MediaControlIntent.ACTION_ENQUEUE);
            String mime = intent.getType();
            Bundle metadata = intent.getBundleExtra(MediaControlIntent.EXTRA_ITEM_METADATA);
            Bundle headers = intent.getBundleExtra(MediaControlIntent.EXTRA_ITEM_HTTP_HEADERS);
            PendingIntent receiver = (PendingIntent)intent.getParcelableExtra(
                    MediaControlIntent.EXTRA_ITEM_STATUS_UPDATE_RECEIVER);
 
            Log.d(TAG, mRouteId + ": Received " + (enqueue?"enqueue":"play") + " request"
                    + ", uri=" + uri
                    + ", mime=" + mime
                    + ", sid=" + sid
                    + ", pos=" + pos
                    + ", metadata=" + metadata
                    + ", headers=" + headers
                    + ", receiver=" + receiver);
            PlaylistItem item = mSessionManager.add(uri, mime, receiver);
            if (callback != null) {
                if (item != null) {
                    Bundle result = new Bundle();
                    result.putString(MediaControlIntent.EXTRA_SESSION_ID, item.getSessionId());
                    result.putString(MediaControlIntent.EXTRA_ITEM_ID, item.getItemId());
                    result.putBundle(MediaControlIntent.EXTRA_ITEM_STATUS,
                            item.getStatus().asBundle());
                    callback.onResult(result);
                } else {
                    callback.onError("Failed to open " + uri.toString(), null);
                }
            }
            return true;
        }
 
        private boolean handleRemove(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            if (sid == null || !sid.equals(mSessionManager.getSessionId())) {
                return false;
            }
 
            String iid = intent.getStringExtra(MediaControlIntent.EXTRA_ITEM_ID);
            PlaylistItem item = mSessionManager.remove(iid);
            if (callback != null) {
                if (item != null) {
                    Bundle result = new Bundle();
                    result.putBundle(MediaControlIntent.EXTRA_ITEM_STATUS,
                            item.getStatus().asBundle());
                    callback.onResult(result);
                } else {
                    callback.onError("Failed to remove" +
                            ", sid=" + sid + ", iid=" + iid, null);
                }
            }
            return (item != null);
        }
 
        private boolean handleSeek(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            if (sid == null || !sid.equals(mSessionManager.getSessionId())) {
                return false;
            }
 
            String iid = intent.getStringExtra(MediaControlIntent.EXTRA_ITEM_ID);
            Log.d(TAG, mRouteId + ": Received seek request, pos=" + pos);
            PlaylistItem item = mSessionManager.seek(iid, pos);
            if (callback != null) {
                if (item != null) {
                    Bundle result = new Bundle();
                    result.putBundle(MediaControlIntent.EXTRA_ITEM_STATUS,
                            item.getStatus().asBundle());
                    callback.onResult(result);
                } else {
                    callback.onError("Failed to seek" +
                            ", sid=" + sid + ", iid=" + iid + ", pos=" + pos, null);
                }
            }
            return (item != null);
        }
 
        private boolean handleGetStatus(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            String iid = intent.getStringExtra(MediaControlIntent.EXTRA_ITEM_ID);
            Log.d(TAG, mRouteId + ": Received getStatus request, sid=" + sid + ", iid=" + iid);
            PlaylistItem item = mSessionManager.getStatus(iid);
            if (callback != null) {
                if (item != null) {
                    Bundle result = new Bundle();
                    result.putBundle(MediaControlIntent.EXTRA_ITEM_STATUS,
                            item.getStatus().asBundle());
                    callback.onResult(result);
                } else {
                    callback.onError("Failed to get status" +
                            ", sid=" + sid + ", iid=" + iid, null);
                }
            }
            return (item != null);
        }
 
        private boolean handlePause(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            boolean success = (sid != null) && sid.equals(mSessionManager.getSessionId());
            mSessionManager.pause();
            if (callback != null) {
                if (success) {
                    callback.onResult(new Bundle());
                    handleSessionStatusChange(sid);
                } else {
                    callback.onError("Failed to pause, sid=" + sid, null);
                }
            }
            return success;
        }
 
        private boolean handleResume(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            boolean success = (sid != null) && sid.equals(mSessionManager.getSessionId());
            mSessionManager.resume();
            if (callback != null) {
                if (success) {
                    callback.onResult(new Bundle());
                    handleSessionStatusChange(sid);
                } else {
                    callback.onError("Failed to resume, sid=" + sid, null);
                }
            }
            return success;
        }
 
        private boolean handleStop(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            boolean success = (sid != null) && sid.equals(mSessionManager.getSessionId());
            mSessionManager.stop();
            if (callback != null) {
                if (success) {
                    callback.onResult(new Bundle());
                    handleSessionStatusChange(sid);
                } else {
                    callback.onError("Failed to stop, sid=" + sid, null);
                }
            }
            return success;
        }
 
        private boolean handleStartSession(Intent intent, ControlRequestCallback callback) {
            String sid = mSessionManager.startSession();
            Log.d(TAG, "StartSession returns sessionId "+sid);
            if (callback != null) {
                if (sid != null) {
                    Bundle result = new Bundle();
                    result.putString(MediaControlIntent.EXTRA_SESSION_ID, sid);
                    result.putBundle(MediaControlIntent.EXTRA_SESSION_STATUS,
                            mSessionManager.getSessionStatus(sid).asBundle());
                    callback.onResult(result);
                    mSessionReceiver = (PendingIntent)intent.getParcelableExtra(
                            MediaControlIntent.EXTRA_SESSION_STATUS_UPDATE_RECEIVER);
                    handleSessionStatusChange(sid);
                } else {
                    callback.onError("Failed to start session.", null);
                }
            }
            return (sid != null);
        }
 
        private boolean handleGetSessionStatus(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
 
            MediaSessionStatus sessionStatus = mSessionManager.getSessionStatus(sid);
            if (callback != null) {
                if (sessionStatus != null) {
                    Bundle result = new Bundle();
                    result.putBundle(MediaControlIntent.EXTRA_SESSION_STATUS,
                            mSessionManager.getSessionStatus(sid).asBundle());
                    callback.onResult(result);
                } else {
                    callback.onError("Failed to get session status, sid=" + sid, null);
                }
            }
            return (sessionStatus != null);
        }
 
        private boolean handleEndSession(Intent intent, ControlRequestCallback callback) {
            String sid = intent.getStringExtra(MediaControlIntent.EXTRA_SESSION_ID);
            boolean success = (sid != null) && sid.equals(mSessionManager.getSessionId())
                    && mSessionManager.endSession();
            if (callback != null) {
                if (success) {
                    Bundle result = new Bundle();
                    MediaSessionStatus sessionStatus = new MediaSessionStatus.Builder(
                            MediaSessionStatus.SESSION_STATE_ENDED).build();
                    result.putBundle(MediaControlIntent.EXTRA_SESSION_STATUS, sessionStatus.asBundle());
                    callback.onResult(result);
                    handleSessionStatusChange(sid);
                    mSessionReceiver = null;
                } else {
                    callback.onError("Failed to end session, sid=" + sid, null);
                }
            }
            return success;
        }
 
        private void handleStatusChange(PlaylistItem item) {
            if (item == null) {
                item = mSessionManager.getCurrentItem();
            }
            if (item != null) {
                PendingIntent receiver = item.getUpdateReceiver();
                if (receiver != null) {
                    Intent intent = new Intent();
                    intent.putExtra(MediaControlIntent.EXTRA_SESSION_ID, item.getSessionId());
                    intent.putExtra(MediaControlIntent.EXTRA_ITEM_ID, item.getItemId());
                    intent.putExtra(MediaControlIntent.EXTRA_ITEM_STATUS,
                            item.getStatus().asBundle());
                    try {
                        Log.d(TAG, mRouteId + ": Sending status update from provider");
                    } catch (PendingIntent.CanceledException e) {
                        Log.d(TAG, mRouteId + ": Failed to send status update!");
                    }
                }
            }
        }
 
        private void handleSessionStatusChange(String sid) {
            if (mSessionReceiver != null) {
                Intent intent = new Intent();
                intent.putExtra(MediaControlIntent.EXTRA_SESSION_ID, sid);
                intent.putExtra(MediaControlIntent.EXTRA_SESSION_STATUS,
                        mSessionManager.getSessionStatus(sid).asBundle());
                try {
                    Log.d(TAG, mRouteId + ": Sending session status update from provider");
                } catch (PendingIntent.CanceledException e) {
                    Log.d(TAG, mRouteId + ": Failed to send session status update!");
                }
            }
        }
    }
}
  

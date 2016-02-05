package se.joelpet.android.toyreaderforreddit.volley;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.RequestFuture;

import java.util.HashMap;
import java.util.Map;

import se.joelpet.android.toyreaderforreddit.domain.AccessToken;

public class ApplicationAccessTokenRequest extends AccessTokenRequest {

    public static final String DEVICE_ID_DO_NOT_TRACK = "DO_NOT_TRACK_THIS_DEVICE";

    private final String mDeviceId;

    /**
     * @param deviceId A unique per-device 20-30 character ASCII string id. A randomized or
     *                 pseudo-randomized value is acceptable; however, it should be retained and
     *                 re-used when renewing the access token. Clients that wish to remain
     *                 anonymous should use the value DO_NOT_TRACK_THIS_DEVICE.
     */
    public ApplicationAccessTokenRequest(@NonNull String deviceId,
                                         @Nullable RequestFuture<AccessToken> future) {
        super(future);
        mDeviceId = deviceId;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();
        params.put("grant_type", "https://oauth.reddit.com/grants/installed_client");
        params.put("device_id", mDeviceId);
        return params;
    }
}

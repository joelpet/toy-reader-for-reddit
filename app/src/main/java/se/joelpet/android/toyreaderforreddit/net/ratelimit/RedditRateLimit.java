package se.joelpet.android.toyreaderforreddit.net.ratelimit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public enum RedditRateLimit {

    INSTANCE;

    /** Response header: Approximate number of requests left to use */
    public static final String X_RATELIMIT_REMAINING = "X-Ratelimit-Remaining";
    /** Response header: Approximate number of seconds to end of period */
    public static final String X_RATELIMIT_RESET = "X-Ratelimit-Reset";

    private static final int MIN_ALLOWED_RATELIMIT_REMAINING = 1;

    @NonNull
    private Map<String, String> mHeaders = Collections.emptyMap();
    @NonNull
    private Float mRemaining = Float.MAX_VALUE;
    @NonNull
    private DateTime mReset = DateTime.now();

    public boolean isExceeded() {
        return mReset.isAfterNow() && mRemaining < MIN_ALLOWED_RATELIMIT_REMAINING;
    }

    public void update(@NonNull Map<String, String> headers) {
        mHeaders = checkNotNull(headers);
        refresh();
    }

    private void refresh() {
        String rawRemaining = getHeaderValueIgnoreNameCase(X_RATELIMIT_REMAINING);
        String rawReset = getHeaderValueIgnoreNameCase(X_RATELIMIT_RESET);

        if (rawRemaining == null || rawReset == null) return;

        Float remaining = Float.parseFloat(rawRemaining);
        int secondsBeforeReset = (int) Float.parseFloat(rawReset);

        mRemaining = remaining;
        mReset = DateTime.now().plusSeconds(secondsBeforeReset);

        Timber.d("Approximately %f requests left to use with %d seconds to end of period (%s).",
                mRemaining, secondsBeforeReset, mReset);
    }

    @Nullable
    private String getHeaderValueIgnoreNameCase(@NonNull String name) {
        String value = mHeaders.get(name);
        return value != null ? value : mHeaders.get(name.toLowerCase());
    }
}

package se.joelpet.android.toyreaderforreddit.net.ratelimit;

import android.text.TextUtils;

import java.util.Map;

public class RedditRateLimit {

    /** Response header name for remaining requests allowed by Reddit API. */
    private static final String X_RATELIMIT_REMAINING = "X-Ratelimit-Remaining";

    private static final int MIN_ALLOWED_RATELIMIT_REMAINING = 1;

    public void checkRemaining(Map<String, String> headers) throws RedditRateLimitExceededError {
        String rawRemaining;
        boolean remainingFound = (rawRemaining = headers.get(X_RATELIMIT_REMAINING)) != null ||
                (rawRemaining = headers.get(X_RATELIMIT_REMAINING.toLowerCase())) != null;

        if (!remainingFound || TextUtils.isEmpty(rawRemaining))
            return;

        Float remaining = Float.parseFloat(rawRemaining);

        if (remaining < MIN_ALLOWED_RATELIMIT_REMAINING) {
            throw new RedditRateLimitExceededError();
        }
    }
}

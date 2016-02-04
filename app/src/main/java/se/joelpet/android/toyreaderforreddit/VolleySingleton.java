package se.joelpet.android.toyreaderforreddit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import se.joelpet.android.toyreaderforreddit.net.ratelimit.RateLimitExceededError;
import se.joelpet.android.toyreaderforreddit.net.ratelimit.RedditRateLimit;

public class VolleySingleton {

    private static final int LRU_CACHE_MAX_SIZE = 20;

    private static Context mContext;

    private static VolleySingleton mInstance;

    private RequestQueue mRequestQueue;

    private final ImageLoader mImageLoader;

    private final RedditRateLimit mRedditRateLimit = RedditRateLimit.INSTANCE;

    private VolleySingleton(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(LRU_CACHE_MAX_SIZE);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    /**
     * Returns the VolleySingleton instance.
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mInstance = new VolleySingleton(context.getApplicationContext());
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public <T> Request<T> addToRequestQueue(Request<T> req) throws RateLimitExceededError {
        if (mRedditRateLimit.isExceeded()) {
            throw new RateLimitExceededError();
        }
        return getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}

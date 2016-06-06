package se.joelpet.android.toyreaderforreddit.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import se.joelpet.android.toyreaderforreddit.model.Link;

public class LinkUtils {

    private static final String BASE_URL_COMMENTS = "https://m.reddit.com";

    @NonNull
    public static Uri getLinkUri(Link link) {
        return Uri.parse(link.getUrl());
    }

    @NonNull
    public static Uri getCommentsUri(@NonNull Link link) {
        return Uri.parse(BASE_URL_COMMENTS + link.getPermalink());
    }
}

package se.joelpet.android.toyreaderforreddit.ui.linklisting;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

import se.joelpet.android.toyreaderforreddit.model.Link;
import se.joelpet.android.toyreaderforreddit.ui.BasePresenter;
import se.joelpet.android.toyreaderforreddit.ui.BaseView;

public interface LinkListingContract {

    interface View extends BaseView<Presenter> {

        void showLinks(List<Link> links);

        void showMoreLinks(List<Link> links);

        void showWebUi(@NonNull Uri linkUri);

        void setRefreshingIndicator(boolean active);

        void setLoadingIndicator(boolean active);

        void showAuthFailureError();

        void showLoadFailureError();
    }

    interface Presenter extends BasePresenter {

        void loadLinks();

        void reloadLinks();

        void openLink(@NonNull Link link);

        void openLinkComments(@NonNull Link link);

        void renewCredentials(Activity activity);
    }
}

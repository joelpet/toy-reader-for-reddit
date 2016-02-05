package se.joelpet.android.toyreaderforreddit.accounts;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AddAccountResult {

    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final Bundle result;

    public AddAccountResult(@NonNull Bundle result) {
        this.result = result;
    }

    @NonNull
    public String getName() {
        return checkNotNull(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                "Account name missing");
    }

    @NonNull
    public String getAccountType() {
        return checkNotNull(result.getString(AccountManager.KEY_ACCOUNT_TYPE),
                "Account type missing");
    }

    @NonNull
    public String getAuthToken() {
        return checkNotNull(result.getString(AccountManager.KEY_AUTHTOKEN),
                "Auth token missing");
    }

    @NonNull
    public String getRefreshToken() {
        return checkNotNull(getUserdata().getString(KEY_REFRESH_TOKEN),
                "Refresh token missing from userdata bundle");
    }

    @NonNull
    private Bundle getUserdata() {
        return checkNotNull(result.getBundle(AccountManager.KEY_USERDATA),
                "Userdata bundle missing");
    }

    @NonNull
    private static <T> T checkNotNull(T value, @Nullable Object errorMessage) {
        if (value == null) throw new NullPointerException(String.valueOf(errorMessage));
        return value;
    }

    @Override
    public String toString() {
        return "AddAccountResult{" +
                "result=" + AccountManagerHelper.sanitizeResult(result) +
                '}';
    }
}

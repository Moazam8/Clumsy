package com.clumsy.finance;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.IntentSender;

public class DriveAuthorizationActivity extends Activity {
    static final String EXTRA_PENDING_INTENT = "google_drive_pending_intent";
    private static final int REQUEST_AUTHORIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingIntent = getIntent().getParcelableExtra(EXTRA_PENDING_INTENT, PendingIntent.class);
        } else {
            pendingIntent = getIntent().getParcelableExtra(EXTRA_PENDING_INTENT);
        }
        if (pendingIntent == null) {
            setResult(RESULT_CANCELED, new Intent().putExtra("authorization_error", "Google did not provide an authorization request."));
            finish();
            return;
        }
        try {
            startIntentSenderForResult(
                pendingIntent.getIntentSender(), REQUEST_AUTHORIZATION, null, 0, 0, 0
            );
        } catch (IntentSender.SendIntentException error) {
            setResult(RESULT_CANCELED, new Intent().putExtra("authorization_error", "Could not open Google's authorization screen."));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZATION) {
            setResult(resultCode, data);
            finish();
        }
    }
}

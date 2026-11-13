package com.clumsy.finance;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.ActivityCallback;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;

import java.util.Collections;

@CapacitorPlugin(name = "GoogleDriveAuth")
public class GoogleDriveAuthPlugin extends Plugin {
    @PluginMethod
    public void authorize(PluginCall call) {
        AuthorizationRequest request = AuthorizationRequest.builder()
            .setRequestedScopes(Collections.singletonList(new Scope("https://www.googleapis.com/auth/drive.file")))
            .build();

        Identity.getAuthorizationClient(getContext())
            .authorize(request)
            .addOnSuccessListener(result -> handleAuthorization(call, result))
            .addOnFailureListener(error -> call.reject("Google Drive authorization failed.", error));
    }

    private void handleAuthorization(PluginCall call, AuthorizationResult result) {
        if (!result.hasResolution()) {
            resolveWithToken(call, result);
            return;
        }
        Intent intent = new Intent(getContext(), DriveAuthorizationActivity.class);
        intent.putExtra(DriveAuthorizationActivity.EXTRA_PENDING_INTENT, result.getPendingIntent());
        startActivityForResult(call, intent, "authorizationResult");
    }

    @ActivityCallback
    private void authorizationResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            String error = result.getData() == null ? null : result.getData().getStringExtra("authorization_error");
            call.reject(error == null ? "Google Drive authorization was cancelled." : error);
            return;
        }
        try {
            AuthorizationResult authorization = Identity.getAuthorizationClient(getContext())
                .getAuthorizationResultFromIntent(result.getData());
            resolveWithToken(call, authorization);
        } catch (Exception error) {
            call.reject("Could not complete Google Drive authorization.", error);
        }
    }

    private void resolveWithToken(PluginCall call, AuthorizationResult result) {
        String accessToken = result.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            call.reject("Google did not provide a Drive access token.");
            return;
        }
        JSObject response = new JSObject();
        response.put("accessToken", accessToken);
        call.resolve(response);
    }
}

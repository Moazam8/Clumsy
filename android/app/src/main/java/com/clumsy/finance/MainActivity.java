package com.clumsy.finance;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(GoogleDriveAuthPlugin.class);
        registerPlugin(BiometricUnlockPlugin.class);
        super.onCreate(savedInstanceState);
    }
}

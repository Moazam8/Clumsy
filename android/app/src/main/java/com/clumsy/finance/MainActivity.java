package com.clumsy.finance;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(GoogleDriveAuthPlugin.class);
        super.onCreate(savedInstanceState);
    }
}

package net.jahnan00.zerotierfix.ui;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import net.jahnan00.zerotierfix.R;
import net.jahnan00.zerotierfix.ZerotierFixApplication;
import net.jahnan00.zerotierfix.model.Network;
import net.jahnan00.zerotierfix.service.ZeroTierOneService;
import net.jahnan00.zerotierfix.service.ZeroTierTileService;
import net.jahnan00.zerotierfix.util.Constants;

public class QuickSettingsTileActivity extends AppCompatActivity {
    public static final String EXTRA_CONNECT_AFTER_AUTHORIZATION =
            "net.jahnan00.zerotierfix.extra.CONNECT_AFTER_AUTHORIZATION";

    private long selectedNetworkId;
    private boolean connectAfterAuthorization;
    private ActivityResultLauncher<Intent> vpnAuthorizationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_settings_tile);
        setTitle(R.string.quick_settings_tile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        connectAfterAuthorization = getIntent().getBooleanExtra(EXTRA_CONNECT_AFTER_AUTHORIZATION, false);
        vpnAuthorizationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && selectedNetworkId != 0) {
                        ZeroTierOneService.start(this, selectedNetworkId);
                    }
                    finish();
                });

        renderNetworks();
        if (connectAfterAuthorization) {
            requestVpnAuthorization();
        }
    }

    private void renderNetworks() {
        LinearLayout container = findViewById(R.id.tile_network_list);
        TextView emptyView = findViewById(R.id.tile_network_empty);
        Button clearButton = findViewById(R.id.tile_clear_binding);
        var preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long boundNetworkId = preferences.getLong(Constants.PREF_TILE_NETWORK_ID, 0);
        var networks = ((ZerotierFixApplication) getApplication()).getDaoSession()
                .getNetworkDao().loadAll();
        container.removeAllViews();

        emptyView.setVisibility(networks.isEmpty() ? View.VISIBLE : View.GONE);
        clearButton.setVisibility(boundNetworkId == 0 ? View.GONE : View.VISIBLE);
        clearButton.setOnClickListener(view -> {
            preferences.edit()
                    .remove(Constants.PREF_TILE_NETWORK_ID)
                    .remove(Constants.PREF_TILE_NETWORK_NAME)
                    .apply();
            ZeroTierTileService.refresh(this);
            renderNetworks();
        });

        for (Network network : networks) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String configuredName = network.getNetworkName();
            if (configuredName == null || configuredName.isEmpty()) {
                configuredName = getString(R.string.empty_network_name);
            }
            final String networkName = configuredName;
            option.setText(getString(R.string.tile_network_option, networkName, network.getNetworkIdStr()));
            option.setChecked(network.getNetworkId().equals(boundNetworkId));
            option.setPadding(12, 18, 12, 18);
            option.setOnClickListener(view -> bindNetwork(network, networkName));
            container.addView(option);
        }
    }

    private void bindNetwork(Network network, String name) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putLong(Constants.PREF_TILE_NETWORK_ID, network.getNetworkId())
                .putString(Constants.PREF_TILE_NETWORK_NAME, name)
                .apply();
        selectedNetworkId = network.getNetworkId();
        ZeroTierTileService.refresh(this);
        if (connectAfterAuthorization) {
            requestVpnAuthorization();
        } else {
            finish();
        }
    }

    private void requestVpnAuthorization() {
        long boundNetworkId = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(Constants.PREF_TILE_NETWORK_ID, 0);
        if (boundNetworkId == 0) {
            return;
        }
        selectedNetworkId = boundNetworkId;
        Intent prepare = VpnService.prepare(this);
        if (prepare == null) {
            ZeroTierOneService.start(this, selectedNetworkId);
            finish();
        } else {
            vpnAuthorizationLauncher.launch(prepare);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

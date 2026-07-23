package net.jahnan00.zerotierfix.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.VpnService;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import net.jahnan00.zerotierfix.R;
import net.jahnan00.zerotierfix.ui.QuickSettingsTileActivity;
import net.jahnan00.zerotierfix.util.Constants;

@RequiresApi(Build.VERSION_CODES.N)
public class ZeroTierTileService extends TileService {
    private boolean stateReceiverRegistered;
    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTile();
        }
    };
    public static void refresh(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(context,
                    new ComponentName(context, ZeroTierTileService.class));
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if (!stateReceiverRegistered) {
            ContextCompat.registerReceiver(this, stateReceiver,
                    new IntentFilter(Constants.ACTION_CONNECTION_STATE_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED);
            stateReceiverRegistered = true;
        }
        updateTile();
    }

    @Override
    public void onStopListening() {
        if (stateReceiverRegistered) {
            unregisterReceiver(stateReceiver);
            stateReceiverRegistered = false;
        }
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        var preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long networkId = preferences.getLong(Constants.PREF_TILE_NETWORK_ID, 0);
        if (networkId == 0) {
            openBindingActivity(false);
            return;
        }

        long activeNetworkId = preferences.getLong(Constants.PREF_ACTIVE_NETWORK_ID, 0);
        if (activeNetworkId == networkId) {
            ZeroTierOneService.stop(this);
        } else if (VpnService.prepare(this) != null) {
            openBindingActivity(true);
            return;
        } else {
            ZeroTierOneService.toggle(this, networkId);
        }
    }

    private void openBindingActivity(boolean connectAfterAuthorization) {
        Intent intent = new Intent(this, QuickSettingsTileActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(QuickSettingsTileActivity.EXTRA_CONNECT_AFTER_AUTHORIZATION,
                        connectAfterAuthorization);
        startActivityAndCollapse(intent);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        var preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long networkId = preferences.getLong(Constants.PREF_TILE_NETWORK_ID, 0);
        long activeNetworkId = preferences.getLong(Constants.PREF_ACTIVE_NETWORK_ID, 0);
        if (networkId == 0) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.tile_unconfigured));
        } else {
            tile.setState(activeNetworkId == networkId ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel(preferences.getString(Constants.PREF_TILE_NETWORK_NAME,
                    getString(R.string.tile_label)));
        }
        tile.updateTile();
    }
}

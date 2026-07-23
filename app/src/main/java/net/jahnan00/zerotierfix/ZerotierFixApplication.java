package net.jahnan00.zerotierfix;

import android.util.Log;
import android.preference.PreferenceManager;

import androidx.multidex.MultiDexApplication;

import net.jahnan00.zerotierfix.model.DaoMaster;
import net.jahnan00.zerotierfix.model.DaoSession;
import net.jahnan00.zerotierfix.model.ZTOpenHelper;
import net.jahnan00.zerotierfix.util.Constants;

/**
 * 主程序入口
 *
 * @author jahnan00
 */
public class ZerotierFixApplication extends MultiDexApplication {
    private DaoSession mDaoSession;

    public void onCreate() {
        super.onCreate();
        Log.i("Application", "Starting Application");
        // A killed process cannot keep a VPN tunnel alive; discard stale UI state before services restore.
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(Constants.PREF_TILE_RUNNING, false)
                .remove(Constants.PREF_ACTIVE_NETWORK_ID)
                .apply();
        // 创建 DAO 会话
        this.mDaoSession = new DaoMaster(
                new ZTOpenHelper(this, "ztfixdb", null)
                        .getWritableDatabase()
        ).newSession();
    }

    public DaoSession getDaoSession() {
        return this.mDaoSession;
    }
}

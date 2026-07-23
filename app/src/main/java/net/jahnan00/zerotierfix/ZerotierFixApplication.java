package net.jahnan00.zerotierfix;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import net.jahnan00.zerotierfix.model.DaoMaster;
import net.jahnan00.zerotierfix.model.DaoSession;
import net.jahnan00.zerotierfix.model.ZTOpenHelper;

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

package net.jahnan00.zerotierfix.util;

import android.content.Context;

import java.io.File;

/**
 * 文件工具类
 *
 * @author jahnan00
 */
public class FileUtil {

    /**
     * 清理临时文件
     */
    public static File tempFile(Context context) {
        return new File(context.getCacheDir(), Constants.FILE_TEMP);
    }

    /**
     * 清理临时文件
     */
    public static void clearTempFile(Context context) {
        File temp = tempFile(context);
        if (temp.exists()) {
            temp.delete();
        }
    }
}

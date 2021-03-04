package com.qugengting.email.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by xuruibin on 2018/5/11.
 * 描述：
 */

public class FileUtils {
    public static boolean copyFile(String oldPath, String newPath, boolean deleteOldFile) {
        File oldfile = new File(oldPath);
        File newFile = new File(newPath);
        if (newFile.exists()) {
            if (oldfile.exists() && deleteOldFile && !oldPath.equals(newPath)) {
                oldfile.delete();
            }
            return true;
        }
        try {
            if (oldfile.exists()) { //文件存在时
                InputStream is = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fos = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
                fos.close();
                is.close();
                if (deleteOldFile) {
                    oldfile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 计算文件大小
     */
    public static String getFileSize(long length) {
        if (length >= 1048576) {
            return (length / 1048576) + " MB";
        } else if (length >= 1024) {
            return (length / 1024) + " KB";
        } else {
            return length + " B";
        }
    }
}

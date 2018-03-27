package com.tming.common.util;

import android.os.Environment;
import com.tming.common.CommonApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>对文件操作帮助类</p>
 * <p>Created by yusongying on 2014/10/16</p>
 */
public class FileUtil {

    private static List<File> tempFiles = new ArrayList<File>();

    /**
     * 获取创建的临时文件列表
     *
     * @return 临时文件列表
     */
    public static List<File> getCreatedTempFiles() {
        return tempFiles;
    }

    /**
     * 创建临时文件
     *
     * @param prefix 临时文件前缀
     * @param suffix 临时文件后缀
     * @return 创建后的文件路径
     * @throws IOException
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
        File tempFile;
        if (sdCardExist) {
            tempFile = File.createTempFile(prefix, suffix, CommonApp.getAppContext().getExternalCacheDir());
        } else {
            tempFile = File.createTempFile(prefix, suffix);
        }
        tempFiles.add(tempFile);
        return tempFile;
    }

    /**
     * 删除所有创建的临时文件
     */
    public static void deleteCreatedTempFiles() {
        for(File file : tempFiles) {
            file.delete();
        }
        tempFiles.clear();
    }

    /**
     * 如果文件目录不存在，创建文件所在目录
     *
     * @param file
     */
    public static void makeParentDirIfNeed(File file) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    /**
     * 拷贝文件方法
     * @param orgFile 原始文件
     * @param newFile 新文件
     */
    public static void copyFile(File orgFile, File newFile) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(orgFile);
            fos = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取临时文件路径
     *
     * @return
     * @throws IOException
     */
    public static File getTempDir() throws IOException {
        File file = createTempFile("test", "");
        file.delete();
        tempFiles.remove(file);
        return file.getParentFile();
    }

}

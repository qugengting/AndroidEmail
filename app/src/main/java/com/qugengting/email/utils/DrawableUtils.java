package com.qugengting.email.utils;

import com.qugengting.email.R;

/**
 * Created by xuruibin on 2018/5/15.
 * 描述：
 */

public class DrawableUtils {
    public static int getFileIcon(String fileName) {
        String ext = "";
        if (fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        if ("doc".equals(ext) || "docx".equals(ext) || "rtf".equals(ext) || "wps".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_doc;
        } else if ("xls".equalsIgnoreCase(ext) || "xlsx".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_excle;
        } else if ("ppt".equalsIgnoreCase(ext) || "pptx".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_ppt;
        } else if ("xml".equalsIgnoreCase(ext) || "html".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_unknown;
        } else if ("pdf".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_pdf;
        } else if ("txt".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_txt;
        } else if ("png".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext) || "bmp".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_img;
        } else if ("swf".equalsIgnoreCase(ext) || "rmvb".equalsIgnoreCase(ext) || "avi".equalsIgnoreCase(ext) || "mp4".equalsIgnoreCase(ext) || "rm".equalsIgnoreCase(ext) || "mkv".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_video;
        } else if ("amr".equalsIgnoreCase(ext) || "wav".equalsIgnoreCase(ext) || "mp3".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_music;
        } else if ("rar".equalsIgnoreCase(ext) || "zip".equalsIgnoreCase(ext) || "7z".equalsIgnoreCase(ext)) {
            return R.drawable.ic_file_zip;
        } else {
            return R.drawable.ic_file_unknown;
        }
    }
}

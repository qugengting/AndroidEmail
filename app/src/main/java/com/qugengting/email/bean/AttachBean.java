package com.qugengting.email.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @author:xuruibin
 * @date:2021/2/22 Description:
 */
public class AttachBean extends LitePalSupport {
    private String name;
    private String path;
    private String size;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}

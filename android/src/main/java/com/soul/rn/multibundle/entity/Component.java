package com.soul.rn.multibundle.entity;

import androidx.annotation.Keep;

@Keep
public class Component {
    public int version;
    public String hash;
    public String commonHash;
    public String componentName;
    public Integer componentType;
    public String downloadUrl;
    public Long buildTime;

    public Component(int version, String hash, String commonHash, String componentName,  Integer componentType, String downloadUrl, Long buildTime) {
        this.version = version;
        this.hash = hash;
        this.commonHash = commonHash;
        this.componentName = componentName;
        this.componentType = componentType;
        this.downloadUrl = downloadUrl;
        this.buildTime = buildTime;
    }
}

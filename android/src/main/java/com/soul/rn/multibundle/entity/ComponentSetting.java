package com.soul.rn.multibundle.entity;

public class ComponentSetting {
    public String hash;
    public String commonHash;
    public String bundleName;
    public String componentName;
    public Integer componentType;
    public Long timestamp;

    public ComponentSetting(String hash, String commonHash, String bundleName, String componentName, Integer componentType, Long timestamp) {
        this.hash = hash;
        this.commonHash = commonHash;
        this.bundleName = bundleName;
        this.componentName = componentName;
        this.componentType = componentType;
        this.timestamp = timestamp;
    }
}

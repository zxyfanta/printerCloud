package com.printercloud.admin.model;

import java.util.List;

/**
 * 打印机信息模型
 */
public class PrinterInfo {
    
    private String name;
    private String displayName;
    private boolean isDefault;
    private boolean isOnline;
    private String description;
    private String location;
    private List<String> supportedPaperSizes;
    private List<String> supportedColorModes;
    private boolean supportsDuplex;
    private int queueCount;

    // 构造函数
    public PrinterInfo() {}

    public PrinterInfo(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean isOnline) { this.isOnline = isOnline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getSupportedPaperSizes() { return supportedPaperSizes; }
    public void setSupportedPaperSizes(List<String> supportedPaperSizes) { this.supportedPaperSizes = supportedPaperSizes; }

    public List<String> getSupportedColorModes() { return supportedColorModes; }
    public void setSupportedColorModes(List<String> supportedColorModes) { this.supportedColorModes = supportedColorModes; }

    public boolean isSupportsDuplex() { return supportsDuplex; }
    public void setSupportsDuplex(boolean supportsDuplex) { this.supportsDuplex = supportsDuplex; }

    public int getQueueCount() { return queueCount; }
    public void setQueueCount(int queueCount) { this.queueCount = queueCount; }

    @Override
    public String toString() {
        return "PrinterInfo{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isDefault=" + isDefault +
                ", isOnline=" + isOnline +
                '}';
    }
}

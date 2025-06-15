package com.printercloud.admin.model;

/**
 * 打印设置模型
 */
public class PrintSettings {
    
    private String printerName;
    private Integer copies;
    private Boolean doubleSided;
    private String paperSize;
    private String colorMode;
    private String quality;
    private String pageRange;
    
    // 构造函数
    public PrintSettings() {
        this.copies = 1;
        this.doubleSided = false;
        this.paperSize = "A4";
        this.colorMode = "BLACK_WHITE";
        this.quality = "NORMAL";
    }
    
    public PrintSettings(String printerName) {
        this();
        this.printerName = printerName;
    }
    
    // Getters and Setters
    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }
    
    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }
    
    public Boolean getDoubleSided() { return doubleSided; }
    public void setDoubleSided(Boolean doubleSided) { this.doubleSided = doubleSided; }
    
    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }
    
    public String getColorMode() { return colorMode; }
    public void setColorMode(String colorMode) { this.colorMode = colorMode; }
    
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    
    public String getPageRange() { return pageRange; }
    public void setPageRange(String pageRange) { this.pageRange = pageRange; }
    
    // 便利方法
    public boolean isColor() {
        return "COLOR".equalsIgnoreCase(colorMode);
    }
    
    public void setColor(boolean color) {
        this.colorMode = color ? "COLOR" : "BLACK_WHITE";
    }
    
    public boolean isDuplex() {
        return doubleSided != null && doubleSided;
    }
    
    public void setDuplex(boolean duplex) {
        this.doubleSided = duplex;
    }
    
    /**
     * 验证设置是否有效
     */
    public boolean isValid() {
        return printerName != null && !printerName.trim().isEmpty() &&
               copies != null && copies > 0 &&
               paperSize != null && !paperSize.trim().isEmpty();
    }
    
    /**
     * 获取设置摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("打印机: ").append(printerName);
        summary.append(", 份数: ").append(copies);
        summary.append(", 纸张: ").append(paperSize);
        summary.append(", 颜色: ").append(isColor() ? "彩色" : "黑白");
        summary.append(", 双面: ").append(isDuplex() ? "是" : "否");
        
        if (pageRange != null && !pageRange.trim().isEmpty()) {
            summary.append(", 页面: ").append(pageRange);
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "PrintSettings{" +
                "printerName='" + printerName + '\'' +
                ", copies=" + copies +
                ", doubleSided=" + doubleSided +
                ", paperSize='" + paperSize + '\'' +
                ", colorMode='" + colorMode + '\'' +
                ", quality='" + quality + '\'' +
                ", pageRange='" + pageRange + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PrintSettings that = (PrintSettings) o;
        
        if (!printerName.equals(that.printerName)) return false;
        if (!copies.equals(that.copies)) return false;
        if (!doubleSided.equals(that.doubleSided)) return false;
        if (!paperSize.equals(that.paperSize)) return false;
        return colorMode.equals(that.colorMode);
    }
    
    @Override
    public int hashCode() {
        int result = printerName.hashCode();
        result = 31 * result + copies.hashCode();
        result = 31 * result + doubleSided.hashCode();
        result = 31 * result + paperSize.hashCode();
        result = 31 * result + colorMode.hashCode();
        return result;
    }
}

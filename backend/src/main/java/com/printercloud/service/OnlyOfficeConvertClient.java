package com.printercloud.service;

import java.io.File;

public interface OnlyOfficeConvertClient {
    File convertToPdf(File sourceFile, String fileExt) throws Exception;
}


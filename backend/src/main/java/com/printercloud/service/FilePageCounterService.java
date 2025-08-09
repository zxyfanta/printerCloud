package com.printercloud.service;

import java.io.File;

public interface FilePageCounterService {
    int countPages(File file, String originalName, String contentType);
}


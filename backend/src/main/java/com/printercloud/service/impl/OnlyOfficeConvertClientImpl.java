package com.printercloud.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printercloud.service.OnlyOfficeConvertClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlyOfficeConvertClientImpl implements OnlyOfficeConvertClient {

    @Value("${onlyoffice.url:http://onlyoffice:80}")
    private String onlyOfficeUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public File convertToPdf(File sourceFile, String fileExt) throws Exception {
        byte[] bytes = Files.readAllBytes(sourceFile.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String key = UUID.randomUUID().toString();

        String url = onlyOfficeUrl.replaceAll("/$", "") + "/ConvertService.ashx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        String body = String.format("{\n  \"async\": false,\n  \"filetype\": \"%s\",\n  \"key\": \"%s\",\n  \"outputtype\": \"pdf\",\n  \"title\": \"%s\",\n  \"inputType\": \"base64\",\n  \"file\": \"%s\"\n}",
                fileExt.toLowerCase(), key, sourceFile.getName(), base64);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        String resp = restTemplate.postForObject(url, entity, String.class);
        JsonNode json = objectMapper.readTree(resp);
        if (json == null || json.get("error") == null || json.get("error").asInt() != 0) {
            int code = json != null && json.get("error") != null ? json.get("error").asInt() : -1;
            throw new RuntimeException("OnlyOffice convert error code=" + code);
        }
        String fileUrl = json.get("fileUrl").asText();
        byte[] pdfData = restTemplate.getForObject(fileUrl, byte[].class);
        File pdf = File.createTempFile("oo-", ".pdf");
        Files.write(pdf.toPath(), pdfData);
        return pdf;
    }
}


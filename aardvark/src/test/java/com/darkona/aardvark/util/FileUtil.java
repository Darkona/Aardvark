package com.darkona.aardvark.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@Slf4j
public class FileUtil {

    public static String readModelJson(String filename) throws IOException {
        File resource = new ClassPathResource("model/"+filename).getFile();
        byte[] byteArray = Files.readAllBytes(resource.toPath());
        String jsonData = new String(byteArray);
        log.info("\nLoaded test data: \n{}", jsonData);
        return jsonData;
    }
}

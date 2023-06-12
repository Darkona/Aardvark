package com.darkona.aardvark.util;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public interface FileUtil {

    default String readModelJson(String filename) throws IOException {
        File resource = new ClassPathResource("model/"+filename).getFile();
        byte[] byteArray = Files.readAllBytes(resource.toPath());
        return new String(byteArray);
    }
}

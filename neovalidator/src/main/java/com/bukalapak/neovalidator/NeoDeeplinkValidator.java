package com.bukalapak.neovalidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NeoDeeplinkValidator {
    public static void main(String[] args) {
        if (args.length<2) {
            System.err.println("missing argument\njava -jar NeoDeeplinkValidator.jar configId filePath");
            System.exit(1);
        }
        final String configId = args[0];
        String filePath = args[1];
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            DeeplinkValidator.validateDeeplinkConfig(configId, json, new OnDeeplinkCheckListener() {
                @Override
                public void onDeeplinkValid() {
                    System.out.println("VALID JSON, key: " + configId);
                    System.exit(0);
                }

                @Override
                public void onDeeplinkInvalid(Exception e) {
                    System.err.println("INVALID JSON, key: " + configId);
                    e.printStackTrace();
                    System.exit(1);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
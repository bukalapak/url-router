package com.bukalapak.neovalidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NeoDeeplinkValidator {
    public static void main(String[] args) {
        if (args.length<2) {
            System.err.println("missing argument\njava -jar NeoDeeplinkValidator.jar filePath validatorName");
            System.exit(1);
        }
        String filePath = args[0];
        final String validatorName = args[1];
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            DeeplinkValidator.validateDeeplinkConfig(validatorName, json, new OnDeeplinkCheckListener() {
                @Override
                public void onDeeplinkValid() {
                    System.out.println("VALID JSON, validator name: " + validatorName);
                    System.exit(0);
                }

                @Override
                public void onDeeplinkInvalid(Exception e) {
                    System.err.println("INVALID JSON, validator name: " + validatorName);
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
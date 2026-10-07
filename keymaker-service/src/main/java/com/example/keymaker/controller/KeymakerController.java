package com.example.keymaker.controller;

import com.example.keymaker.model.KeyMaterialRecord;
import com.example.keymaker.service.KeymakerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*

curl -s   "http://localhost:8080/api/v1/keymaker/key-material?safeName=udm_batch&keyName=gcor-analyts&keyRegion=us-east1"

curl -i   "http://localhost:8080/api/v1/keymaker/decrypt-test?safeName=udm_batch&keyName=gcor-analyts&keyRegion=us-east1"


TOKEN=$(gcloud auth application-default print-access-token)
curl -s   -H "Authorization: Bearer ${TOKEN}"   "https://oauth2.googleapis.com/tokeninfo?access_token=${TOKEN}"   | python3 -m json.tool

*/

@RestController
@RequestMapping("/api/v1/keymaker")
public class KeymakerController {

    private final KeymakerService keymakerService;

    public KeymakerController(KeymakerService keymakerService) {
        this.keymakerService = keymakerService;
    }

    @GetMapping("/key-material")
    public KeyMaterialRecord getKeyMaterial(
            @RequestParam String safeName,
            @RequestParam String keyName,
            @RequestParam String keyRegion) {

        return keymakerService.findKeyMaterial(
                safeName,
                keyName,
                keyRegion
        );
    }

    @GetMapping("/decrypt-test")
    public String decryptTest(
            @RequestParam String safeName,
            @RequestParam String keyName,
            @RequestParam String keyRegion) {

        byte[] dek =
                keymakerService.fetchAndDecrypt(
                        safeName,
                        keyName,
                        keyRegion
                );

        return "DEK decrypted successfully; length="
                + dek.length;
    }
}

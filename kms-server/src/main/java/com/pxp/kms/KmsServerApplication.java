package com.pxp.kms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
java -jar target/kms-server-0.0.1.jar 

*/

@SpringBootApplication
public class KmsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KmsServerApplication.class, args);
	}

}

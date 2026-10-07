package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.app.service.PaymentProcessingService;

/*
java -jar target/client-app-0.0.1-SNAPSHOT.jar 

curl -X POST http://localhost:8074/v1/keys/generate      -H "Content-Type: application/json"      -d '{"keyIdentifier": "payment-service-key-v1", "cryptoType": "AES"}'
*/

@SpringBootApplication
public class ClientAppApplication {

	@Autowired 
	PaymentProcessingService paymentProcessingService;

	public static void main(String[] args) {
		SpringApplication.run(ClientAppApplication.class, args);
	}

	@Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("🚀 Inline Command Line Application Started!");
            
            if (args.length > 0) {
                System.out.println("Arguments received:");
                for (String arg : args) {
                    System.out.println(" - " + arg);
                }
            } else {
                System.out.println("No arguments were provided.");
            }
        };
    }


	@Bean
	public CommandLineRunner callPaymentProcessing() {
		return args -> {
			paymentProcessingService.processCreditCard("4111111111111111");
		};
	}
}

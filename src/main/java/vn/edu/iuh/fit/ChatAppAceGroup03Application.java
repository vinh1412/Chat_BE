package vn.edu.iuh.fit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import vn.edu.iuh.fit.config.RSAKeyRecord;

@EnableConfigurationProperties(RSAKeyRecord.class)
@SpringBootApplication
public class ChatAppAceGroup03Application {

	public static void main(String[] args) {
		SpringApplication.run(ChatAppAceGroup03Application.class, args);
	}

}

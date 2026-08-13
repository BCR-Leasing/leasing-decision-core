package ro.bcrleasing.leasingdecisioncore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LeasingDecisionCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeasingDecisionCoreApplication.class, args);
	}

}

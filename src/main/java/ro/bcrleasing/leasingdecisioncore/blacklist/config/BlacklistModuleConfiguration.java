package ro.bcrleasing.leasingdecisioncore.blacklist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlacklistModuleConfiguration {

    @Bean
    BlacklistDecisionEvaluator blacklistDecisionEvaluator() {
        return new DefaultBlacklistDecisionEvaluator();
    }
}

package ro.bcrleasing.leasingdecisioncore.blacklist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecisionEvaluator;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.DefaultBlacklistDecisionEvaluator;

@Configuration
public class BlacklistModuleConfiguration {

    @Bean
    BlacklistDecisionEvaluator blacklistDecisionEvaluator() {
        return new DefaultBlacklistDecisionEvaluator();
    }
}

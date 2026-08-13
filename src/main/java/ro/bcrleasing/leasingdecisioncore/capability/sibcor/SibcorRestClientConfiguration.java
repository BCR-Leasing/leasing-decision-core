package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SibcorRestClientConfiguration {

    @Bean(name = "sibcorRestClient")
    RestClient sibcorRestClient(
            SibcorProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Math.toIntExact(
                        properties
                                .getConnectTimeout()
                                .toMillis()
                )
        );

        requestFactory.setReadTimeout(
                Math.toIntExact(
                        properties
                                .getReadTimeout()
                                .toMillis()
                )
        );

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class SibcorRestClientConfiguration {

    @Bean
    @Qualifier("sibcorRestClient")
    RestClient sibcorRestClient(SibcorProperties properties) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Math.toIntExact(properties.getConnectTimeout().toMillis())
        );
        requestFactory.setReadTimeout(
                Math.toIntExact(properties.getReadTimeout().toMillis())
        );

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory);

        if (StringUtils.hasText(properties.getUsername())) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(
                    properties.getUsername(),
                    properties.getPassword() == null
                            ? ""
                            : properties.getPassword()
            ));
        }

        properties.getHeaders().forEach(builder::defaultHeader);

        return builder.build();
    }
}

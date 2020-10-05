package com.backbase.stream.bootstrap;

import com.backbase.stream.bootstrap.configuration.BootstrapTaskConfiguration;
import com.backbase.stream.bootstrap.service.BootstrapService;
import com.backbase.stream.configuration.LegalEntitySagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@EnableTask
@SpringBootApplication
@EnableConfigurationProperties({BootstrapTaskConfiguration.class})
@Import({
    LegalEntitySagaConfiguration.class,
    ProductCatalogServiceConfiguration.class,
})
@RequiredArgsConstructor
public class BootstrapTaskApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BootstrapTaskApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

}

@Component
@RequiredArgsConstructor
class BoostrapTaskApplicationRunner implements CommandLineRunner {

    private final BootstrapService bootstrapService;

    public void run(String... args) {
        bootstrapService.setupProductCatalog();
        bootstrapService.setupLegalEntityHieararchy();
    }
}
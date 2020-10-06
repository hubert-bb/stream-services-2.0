package com.backbase.stream.legalentity.generator;

import com.backbase.stream.legalentity.generator.configuration.LegalEntityGeneratorConfigurationProperties;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.LegalEntityType;
import com.backbase.stream.legalentity.model.ProductCatalog;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.product.generator.ProductGenerator;
import com.github.javafaker.Faker;
import java.text.Normalizer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

@AllArgsConstructor
@Slf4j
public class LegalEntityGenerator {

    public final LegalEntityGeneratorConfigurationProperties options;
    public final ProductGenerator productGenerator;

    public LegalEntity generate(ProductCatalog productCatalog) {
        Faker faker = new Faker(options.getLocale());

        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        String username = firstName.toLowerCase() + "." + lastName.toLowerCase();
        username = Normalizer.normalize(username, Normalizer.Form.NFD);
        String fullName = firstName + " " + lastName;

        LegalEntity legalEntity = createLegalEntityFor(username, fullName);

        if (options.isGenerateProducts()) {
            List<ProductGroup> productGroups = productGenerator.generate(legalEntity, productCatalog);
            legalEntity.setProductGroups(productGroups);
        }

        if (RandomUtils.nextDouble(0d, 1d) <= options.getErrorRate()) {
            // Introduce random error;
            legalEntity.setUsers(null);
            legalEntity.setAdministrators(null);
            legalEntity.id(null);
            legalEntity.setName(null);
        }

        return legalEntity;
    }

    private LegalEntity createLegalEntityFor(String username, String fullName) {
        User user = new User().id(username).fullName(fullName);

        JobProfileUser jobProfileUser = new JobProfileUser()
            .user(user);

        LegalEntity legalEntity = new LegalEntity()
            .name(fullName)
            .id(username)
            .parentExternalId(options.getParentLegalEntityId())
            .legalEntityType(LegalEntityType.CUSTOMER)
            .addAdministratorsItem(user);
        legalEntity.addUsersItem(jobProfileUser);
        return legalEntity;
    }

    public LegalEntityGeneratorConfigurationProperties getOptions() {
        return options;
    }

    public ProductGenerator getProductGenerator() {
        return productGenerator;
    }


}

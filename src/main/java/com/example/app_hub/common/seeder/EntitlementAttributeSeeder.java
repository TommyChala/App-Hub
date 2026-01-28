package com.example.app_hub.common.seeder;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import com.example.app_hub.entitlement.repository.EntitlementAttributeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EntitlementAttributeSeeder implements ApplicationRunner {

    private final EntitlementAttributeRepository entitlementAttributeRepository;

    public EntitlementAttributeSeeder (EntitlementAttributeRepository entitlementAttributeRepository) {
        this.entitlementAttributeRepository = entitlementAttributeRepository;
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        addMandatoryAttributes();
    }
    public void addMandatoryAttributes () {
        EntitlementAttributeModel businessKey = new EntitlementAttributeModel("businesskey","BusinessKey",DataType.STRING,true);
        EntitlementAttributeModel name = new EntitlementAttributeModel("name","Name",DataType.STRING,true);
        EntitlementAttributeModel status = new EntitlementAttributeModel("status","Status", DataType.STRING,true);

        entitlementAttributeRepository.save(businessKey);
        entitlementAttributeRepository.save(name);
        entitlementAttributeRepository.save(status);
    }
}

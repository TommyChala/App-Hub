package com.example.app_hub.common.seeder;

import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.account.repository.AccountAttributeRepository;
import com.example.app_hub.common.datatype.DataType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountAttributeSeeder implements ApplicationRunner {

    private final AccountAttributeRepository accountAttributeRepository;

    public AccountAttributeSeeder (AccountAttributeRepository accountAttributeRepository) {
        this.accountAttributeRepository = accountAttributeRepository;
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        addMandatoryAttributes();
    }
    public void addMandatoryAttributes () {
        AccountAttributeModel businessKey = new AccountAttributeModel("businesskey", "BusinessKey", DataType.STRING, true);
        AccountAttributeModel accountName = new AccountAttributeModel("name", "Name", DataType.STRING, true);
        AccountAttributeModel status = new AccountAttributeModel("status", "Status", DataType.STRING, true);

        accountAttributeRepository.save(businessKey);
        accountAttributeRepository.save(accountName);
        accountAttributeRepository.save(status);
    }
}

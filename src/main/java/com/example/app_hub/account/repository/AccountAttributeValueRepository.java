package com.example.app_hub.account.repository;

import com.example.app_hub.account.model.AccountAttributeValueModel;
import com.example.app_hub.account.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountAttributeValueRepository extends JpaRepository<AccountAttributeValueModel, UUID> {
    Optional<AccountAttributeValueModel> findByAccountAndAttribute_Name(AccountModel account, String attributeName);
}

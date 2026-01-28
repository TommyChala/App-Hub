package com.example.app_hub.account.repository;

import com.example.app_hub.account.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, UUID> {
    Optional<AccountModel> findByAccountId (String accountId);
    //Optional<AccountModel> findByAccountName (String accountName);

}

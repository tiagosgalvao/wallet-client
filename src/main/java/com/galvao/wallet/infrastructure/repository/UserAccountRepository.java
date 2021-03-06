package com.galvao.wallet.infrastructure.repository;

import com.galvao.wallet.infrastructure.impl.UserAccountEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserAccountRepository extends CrudRepository<UserAccountEntity, Long> {
}
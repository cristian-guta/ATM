package com.test.demo.repository;

import com.test.demo.dto.AccountDTO;
import com.test.demo.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    Account findAccountsByName(String name);

    AccountDTO findAccountByClient_Cnp(String cnp);

    AccountDTO findAccountByClient_Id(int id);


    @Query("select a from Account a where a.id = ?1")
    Account findAccountById(int id);

    @Transactional
    @Modifying
    @Query("delete  from Account a where a.id = ?1")
    void deleteAccountById(int id);


    Page<Account> findAll(Pageable pageable);
//    Page<Account> findAccountsByClient_Cnp(String cnp, Pageable pageable);

}


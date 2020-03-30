package com.test.demo.repository;

import com.test.demo.model.Account;
import com.test.demo.model.Subscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Integer> {

    @Query("select s from Subscription s where s.name like ?1")
    Subscription findSubscriptionByName(String name);

    @Query("select s from Subscription s where s.id = ?1")
    Subscription getById(int id);

    List<Subscription> findByIdIn(int [] ids);


}

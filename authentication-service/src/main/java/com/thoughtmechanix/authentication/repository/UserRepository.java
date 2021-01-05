package com.thoughtmechanix.authentication.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.thoughtmechanix.authentication.model.User;

@Repository
public interface UserRepository extends CrudRepository<User,String> {
    User findByUserName(String userName);
}

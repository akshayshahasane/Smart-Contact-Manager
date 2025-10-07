package com.example.smartcontactmanager.repository;

import com.example.smartcontactmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

}

package com.seashade.api_seashade.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seashade.api_seashade.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Role findByName(String name);

}

package com.baloombaz.userservice.repositories;

import com.baloombaz.userservice.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}

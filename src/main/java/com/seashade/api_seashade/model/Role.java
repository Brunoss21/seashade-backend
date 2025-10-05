package com.seashade.api_seashade.model;


import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "tb_roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    private UUID roleId;

    private String name;

    public enum Values{
        BASIC(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        ADMIN(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        UUID roleId;

        Values(UUID roleId){
            this.roleId = roleId;
        }


        public UUID getRoleId() {
            return roleId;
        }
    }
}

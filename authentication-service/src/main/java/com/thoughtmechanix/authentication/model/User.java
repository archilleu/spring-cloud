package com.thoughtmechanix.authentication.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "password")
    String password;

    @Column(name = "enabled")
    boolean enabled;
}

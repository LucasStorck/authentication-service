package com.lucas.auth.entities;

public enum RoleType {
    SUPERUSER(2L),
    USER(1L);

    private final Long id;

    RoleType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}


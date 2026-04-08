package com.gestionconges;

import model.Admin;

import java.util.Optional;

/**
 * Holds the signed-in admin for the current JVM. Kept in {@code com.gestionconges}
 * so it is part of the exported application package for the Java module system.
 */
public final class SessionContext {

    private static Admin currentAdmin;

    private SessionContext() {
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    public static void clear() {
        currentAdmin = null;
    }

    public static Optional<Admin> getCurrentAdmin() {
        return Optional.ofNullable(currentAdmin);
    }
}

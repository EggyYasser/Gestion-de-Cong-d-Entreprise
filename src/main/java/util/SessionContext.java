package util;

import model.Admin;

import java.util.Optional;

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

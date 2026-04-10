package util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Departments used in Add/Edit employee and Excel import ({@link FonctionDepartmentMapper}).
 */
public final class DepartmentOptions {

    private static final List<String> ALL = List.of(
            "Human Resources",
            "Finance",
            "IT",
            "Marketing",
            "Logistics & Transport",
            "Logistics & Warehousing",
            "Operations & Production",
            "Operations (Supervision)",
            "Maintenance & Engineering",
            "Security",
            "Catering & Kitchen",
            "General Operations"
    );

    private DepartmentOptions() {
    }

    public static List<String> all() {
        return ALL;
    }

    /**
     * Mutable copy for ComboBox; includes {@code extra} first if not already present (e.g. legacy "HR").
     */
    public static ObservableList<String> observableListWithOptionalExtra(String extra) {
        List<String> copy = new ArrayList<>(ALL);
        if (extra != null && !extra.isBlank() && copy.stream().noneMatch(extra::equalsIgnoreCase)) {
            copy.add(0, extra.trim());
        }
        return FXCollections.observableArrayList(copy);
    }
}

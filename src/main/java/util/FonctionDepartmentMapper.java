package util;

import java.text.Normalizer;
import java.util.Locale;

public final class FonctionDepartmentMapper {

    private FonctionDepartmentMapper() {
    }

    public static String departmentForFonction(String fonction, String fallback) {
        String fb = (fallback == null || fallback.isBlank()) ? "General Operations" : fallback.trim();
        if (fonction == null || fonction.isBlank()) {
            return fb;
        }
        String n = normalize(fonction);

        if (containsAny(n, "comptable", "comptabil", "compta", "tresorier", "tresorerie")) {
            return "Human Resources";
        }
        if (containsAny(n, "rh", "ressources human", "ressource human", "personnel", "paie")) {
            return "Human Resources";
        }
        if (containsAny(n, "administratif", "administration", "secretair", "agent admin")) {
            return "Human Resources";
        }
        if (containsAny(n, "caissier", "caisse", "recouvrement")) {
            return "Finance";
        }
        if (containsAny(n, "chauffeur", "conducteur", "convoyeur")) {
            return "Logistics & Transport";
        }
        if (containsAny(n, "cariste", "magasinier")) {
            return "Logistics & Warehousing";
        }
        if (containsAny(n, "operateur", "manoeuvre", "balance lourd", "ouvrier")) {
            return "Operations & Production";
        }
        if (containsAny(n, "chef d", "chef d'", "contremaitre")) {
            return "Operations (Supervision)";
        }
        if (containsAny(n, "maintenance", "mecanicien", "soudeur", "electricien", "agent de maintenance")) {
            return "Maintenance & Engineering";
        }
        if (containsAny(n, "securite", "gardien", "agent de securite")) {
            return "Security";
        }
        if (containsAny(n, "cuisinier", "cuisine", "aide cuisin")) {
            return "Catering & Kitchen";
        }
        if (containsAny(n, "informatic", "developpeur", "developpement")) {
            return "IT";
        }
        return fb;
    }

    private static boolean containsAny(String normalizedFonction, String... fragments) {
        for (String f : fragments) {
            if (f == null || f.isBlank()) {
                continue;
            }
            String frag = normalize(f);
            if (!frag.isEmpty() && normalizedFonction.contains(frag)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.toLowerCase(Locale.ROOT);
        t = t.replace("œ", "oe");
        return t.replaceAll("\\s+", " ");
    }
}

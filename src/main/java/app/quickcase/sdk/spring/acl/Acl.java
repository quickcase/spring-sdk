package app.quickcase.sdk.spring.acl;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

/**
 * Defines binary role-based ACL model and implement permission evaluation.
 */
public final class Acl {
    public static int CREATE = 0b1000;
    public static int READ = 0b0100;
    public static int UPDATE = 0b0010;
    public static int DELETE = 0b0001;
    public static int CRUD = CREATE | READ | UPDATE | DELETE;

    public static Map<Character, Integer> LETTERS = Map.of(
            'C', CREATE,
            'R', READ,
            'U', UPDATE,
            'D', DELETE
    );

    public static int fromFlags(boolean create, boolean read, boolean update, boolean delete) {
        return (create ? CREATE : 0) | (read ? READ : 0) | (update ? UPDATE : 0) | (delete ? DELETE : 0);
    }

    public static int fromString(String permission) {
        return permission.chars()
                         .reduce(0, (acc, letter) -> LETTERS.getOrDefault((char) letter, 0) | acc);
    }

    private final Map<String, Integer> acl;

    public Acl(Map<String, Integer> acl) {
        this.acl = acl;
    }

    public boolean check(@NonNull Set<String> roles, int verb) {
        return roles.stream()
                    .anyMatch(role -> (acl.getOrDefault(role, 0) & verb) > 0);
    }

    public boolean checkAny(@NonNull Set<String> roles, int... verbs) {
        return check(roles, combine(verbs));
    }

    public boolean checkAll(@NonNull Set<String> roles, int... verbs) {
        var combinedVerbs = combine(verbs);
        var cumulatedPermissions = roles.stream().reduce(0, (acc, role) -> (acl.getOrDefault(role, 0) | acc), (a, b) -> a | b);
        return (combinedVerbs & cumulatedPermissions) == combinedVerbs;
    }

    private static int combine(int[] verbs) {
        return Arrays.stream(verbs).reduce(0, (acc, verb) -> acc | verb);
    }
}

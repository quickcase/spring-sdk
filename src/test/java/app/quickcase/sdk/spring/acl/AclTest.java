package app.quickcase.sdk.spring.acl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static app.quickcase.sdk.spring.acl.Acl.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AclTest {

    @Nested
    class FromFlags {
        @ParameterizedTest
        @MethodSource
        @DisplayName("should convert permission flags to binary")
        void shouldConvertPermissionFlagsToBinary(boolean create, boolean read, boolean update, boolean delete, int expected) {
            assertThat(Acl.fromFlags(create, read, update, delete), is(expected));
        }

        static Stream<Arguments> shouldConvertPermissionFlagsToBinary() {
            return Stream.of(
                    Arguments.of(true, false, false, false, CREATE),
                    Arguments.of(false, true, false, false, READ),
                    Arguments.of(false, false, true, false, UPDATE),
                    Arguments.of(false, false, false, true, DELETE),
                    Arguments.of(true, true, false, false, CREATE | READ),
                    Arguments.of(false, true, true, false, READ | UPDATE),
                    Arguments.of(true, true, true, true, CRUD)
            );
        }
    }

    @Nested
    class FromString {
        @ParameterizedTest
        @MethodSource
        @DisplayName("should convert permission string to binary")
        void shouldConvertPermissionStringToBinary(String permission, int expected) {
            assertThat(Acl.fromString(permission), is(expected));
        }

        static Stream<Arguments> shouldConvertPermissionStringToBinary() {
            return Stream.of(
                    Arguments.of("C", CREATE),
                    Arguments.of("R", READ),
                    Arguments.of("U", UPDATE),
                    Arguments.of("D", DELETE),
                    Arguments.of("CR", CREATE | READ),
                    Arguments.of("RU", READ | UPDATE),
                    Arguments.of("CRUD", CRUD),
                    Arguments.of("DUCR", CRUD),
                    Arguments.of("", 0)
            );
        }
    }

    @Nested
    class Check {
        @Test
        @DisplayName("should be true when any role is granted requested verb")
        void shouldBeTrueWhenAnyRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.check(Set.of("role-2", "role-3", "role-4"), READ),
                    is(true)
            );
        }

        @Test
        @DisplayName("should be false when no role is granted requested verb")
        void shouldBefalseWhenNoRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE | UPDATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.check(Set.of("role-2", "role-3", "role-4"), UPDATE),
                    is(false)
            );
        }
    }

    @Nested
    class CheckAny {
        @Test
        @DisplayName("should be true when any role is granted any of requested verbs")
        void shouldBeTrueWhenAnyRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.checkAny(Set.of("role-2", "role-3", "role-4"), READ, UPDATE),
                    is(true)
            );
        }

        @Test
        @DisplayName("should be false when no role is any of requested verbs")
        void shouldBefalseWhenNoRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE | UPDATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.checkAny(Set.of("role-2", "role-4"), UPDATE, DELETE),
                    is(false)
            );
        }
    }

    @Nested
    class CheckAll {
        @Test
        @DisplayName("should be true when combined roles are granted all of requested verbs")
        void shouldBeTrueWhenAnyRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.checkAll(Set.of("role-2", "role-3", "role-4"), CREATE, READ, DELETE),
                    is(true)
            );
        }

        @Test
        @DisplayName("should be false when no role is any of requested verbs")
        void shouldBefalseWhenNoRoleGranted() {
            var acl = new Acl(Map.of(
                    "role-1", CREATE | UPDATE,
                    "role-2", CREATE | READ,
                    "role-3", DELETE
            ));

            assertThat(
                    acl.checkAll(Set.of("role-1", "role-2"), CREATE, READ, DELETE),
                    is(false)
            );
        }
    }

}
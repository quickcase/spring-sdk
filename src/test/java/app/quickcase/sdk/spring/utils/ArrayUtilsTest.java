package app.quickcase.sdk.spring.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

class ArrayUtilsTest {

    @Nested
    class Concat {
        @Test
        @DisplayName("should concatenate 2 arrays")
        void shouldConcatenateTwoArrays() {
            var result = ArrayUtils.concat(
                    new String[] {"elem1", "elem2"},
                    new String[] {"elem3", "elem4"}
            );

            assertThat(result, arrayContaining("elem1", "elem2", "elem3", "elem4"));
        }
    }

}
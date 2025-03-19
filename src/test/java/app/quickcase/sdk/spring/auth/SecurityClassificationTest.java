package app.quickcase.sdk.spring.auth;

import org.junit.jupiter.api.Test;

import static app.quickcase.sdk.spring.auth.SecurityClassification.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class SecurityClassificationTest {

    @Test
    void highestPrecedenceThan() {
        assertAll(
                () -> assertThat(PUBLIC.highestPrecedenceThan(PUBLIC), is(false)),
                () -> assertThat(PUBLIC.highestPrecedenceThan(PRIVATE), is(true)),
                () -> assertThat(PUBLIC.highestPrecedenceThan(RESTRICTED), is(true)),
                () -> assertThat(PRIVATE.highestPrecedenceThan(PUBLIC), is(false)),
                () -> assertThat(PRIVATE.highestPrecedenceThan(PRIVATE), is(false)),
                () -> assertThat(PRIVATE.highestPrecedenceThan(RESTRICTED), is(true)),
                () -> assertThat(RESTRICTED.highestPrecedenceThan(PUBLIC), is(false)),
                () -> assertThat(RESTRICTED.highestPrecedenceThan(PRIVATE), is(false)),
                () -> assertThat(RESTRICTED.highestPrecedenceThan(RESTRICTED), is(false))
        );
    }

    @Test
    void lowestPrecedenceThan() {
        assertAll(
                () -> assertThat(PUBLIC.lowestPrecedenceThan(PUBLIC), is(false)),
                () -> assertThat(PUBLIC.lowestPrecedenceThan(PRIVATE), is(false)),
                () -> assertThat(PUBLIC.lowestPrecedenceThan(RESTRICTED), is(false)),
                () -> assertThat(PRIVATE.lowestPrecedenceThan(PUBLIC), is(true)),
                () -> assertThat(PRIVATE.lowestPrecedenceThan(PRIVATE), is(false)),
                () -> assertThat(PRIVATE.lowestPrecedenceThan(RESTRICTED), is(false)),
                () -> assertThat(RESTRICTED.lowestPrecedenceThan(PUBLIC), is(true)),
                () -> assertThat(RESTRICTED.lowestPrecedenceThan(PRIVATE), is(true)),
                () -> assertThat(RESTRICTED.lowestPrecedenceThan(RESTRICTED), is(false))
        );
    }
}
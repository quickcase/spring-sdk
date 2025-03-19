package app.quickcase.sdk.spring.auth;

import org.junit.jupiter.api.Test;

import static app.quickcase.sdk.spring.auth.AccessLevel.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class AccessLevelTest {

    @Test
    void highestPrecedenceThan() {
        assertAll(
                () -> assertThat(INDIVIDUAL.highestPrecedenceThan(INDIVIDUAL), is(false)),
                () -> assertThat(INDIVIDUAL.highestPrecedenceThan(GROUP), is(true)),
                () -> assertThat(INDIVIDUAL.highestPrecedenceThan(ORGANISATION), is(true)),
                () -> assertThat(GROUP.highestPrecedenceThan(INDIVIDUAL), is(false)),
                () -> assertThat(GROUP.highestPrecedenceThan(GROUP), is(false)),
                () -> assertThat(GROUP.highestPrecedenceThan(ORGANISATION), is(true)),
                () -> assertThat(ORGANISATION.highestPrecedenceThan(INDIVIDUAL), is(false)),
                () -> assertThat(ORGANISATION.highestPrecedenceThan(GROUP), is(false)),
                () -> assertThat(ORGANISATION.highestPrecedenceThan(ORGANISATION), is(false))
        );
    }

    @Test
    void lowestPrecedenceThan() {
        assertAll(
                () -> assertThat(INDIVIDUAL.lowestPrecedenceThan(INDIVIDUAL), is(false)),
                () -> assertThat(INDIVIDUAL.lowestPrecedenceThan(GROUP), is(false)),
                () -> assertThat(INDIVIDUAL.lowestPrecedenceThan(ORGANISATION), is(false)),
                () -> assertThat(GROUP.lowestPrecedenceThan(INDIVIDUAL), is(true)),
                () -> assertThat(GROUP.lowestPrecedenceThan(GROUP), is(false)),
                () -> assertThat(GROUP.lowestPrecedenceThan(ORGANISATION), is(false)),
                () -> assertThat(ORGANISATION.lowestPrecedenceThan(INDIVIDUAL), is(true)),
                () -> assertThat(ORGANISATION.lowestPrecedenceThan(GROUP), is(true)),
                () -> assertThat(ORGANISATION.lowestPrecedenceThan(ORGANISATION), is(false))
        );
    }
}
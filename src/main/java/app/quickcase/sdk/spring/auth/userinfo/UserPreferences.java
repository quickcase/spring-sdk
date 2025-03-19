package app.quickcase.sdk.spring.auth.userinfo;

import lombok.Builder;
import lombok.Value;

/**
 * Provides QuickCase user's preferences.
 * Hold user customisable settings, like their case list defaults.
 *
 * @deprecated Authentication-based user preferences are not viable and will be replaced in the future.
 */
@Deprecated
@Value
@Builder
public class UserPreferences {
    String defaultJurisdiction;
    String defaultCaseType;
    String defaultState;
}

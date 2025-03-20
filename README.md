# spring-sdk
Development kit to build QuickCase-flavoured JDK applications.

Supported JVM versions:
- 21

## Documentation

### Field path extraction

Extract the value of a metadata or field from the given record using field path notation.

```java
var record = new Record();

var extractor = new RecordExtractor(record);

// Extracting metadata
extractor.extract('[workspace]');
extractor.extract('[type]');
extractor.extract('[state]');
extractor.extract('[id]');
extractor.extract('[classification]');
extractor.extract('[created]');
extractor.extract('[modified]');

// Extracting data field
extractor.extract('field1');
extractor.extract('level1.level2.nestedField');

// Extracting from collection items
extractor.extract('collectionField[0].value'); // By item index, zero-based
extractor.extract('collectionField[id:abc123].value'); // By item ID
```

### OpenID Connect: Resource servers

Provider-agnostic OIDC integration using Spring Security for QuickCase APIs (OAuth2 Resource Servers).

To achieve compatibility with the broadest range of OIDC providers, this library follows 2 guidelines:
- **Convention**: Follows the [OpenID Connect specs](https://openid.net/developers/specs/) as best as possible
- **Configuration**: Allow defaults from the OpenID Connect specs to be overridden through configuration (see below).

In theory, this allows compatibility of QuickCase with any OIDC provider.
In practice, the following integrations have been proven:
- [Auth0](https://auth0.com/)
- [AWS Cognito](https://aws.amazon.com/cognito/)
- [Keycloak](https://www.keycloak.org/)

#### Usage

Configuration properties and beans are auto-configured via Spring's native auto-configuration.
This makes available a bean `QuickcaseOAuth2ResourceServerCustomizer` which can be used directly to confire Spring's
`SecurityFilterChain`.

```java
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        QuickcaseOAuth2ResourceServerCustomizer quickcaseOAuth2ResourceServer
    ) throws Exception {
        return http
            // ... other security configuration
            .oauth2ResourceServer(quickcaseOAuth2ResourceServer)
            .build();
    }
```

#### Configuration

Settings can be amended to suite the OpenID Connect provider using the following Spring configuration properties: 

```yaml
quickcase:
  oidc:
    authorisation-strategy: roles # One of: roles, legacy; `legacy` is deprecated and should no longer be used
    jwk-set-uri: <url> # Required
    user-info-uri: <url> # Required when mode is `user-info`
    mode: user-info # One of: jwt-access-token, user-info; `user-info` is deprecated and should no longer be used
    openid-scope: openid # Scope used to differentiate client credentials from authorisation grants
    claims:
      prefix: '' # Prefix applied to private claim names
      names:
        sub: sub
        name: name
        email: email
        roles: app.quickcase.claims/roles # Required, comma-separated list of role references
        groups: app.quickcase.claims/groups # Optional, comma-separated list of group references
        organisations: app.quickcase.claims/organisations # Deprecated
        default-jurisdiction: app.quickcase.claims/default_jurisdiction # Deprecated
        default-case-type: app.quickcase.claims/default_case_type # Deprecated
        default-state: app.quickcase.claims/default_state # Deprecated
```

Values documented above are the default values.

As any other Spring externalised configuration, these values can be amended using [a property file or environment variables](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding).

#### Private claims

QuickCase relies on each users having private claims exposed through OpenID Connect by the identity provider.

##### app.quickcase.claims/roles

Required, String. Comma-separated list of user roles used by QuickCase to enforce role-based ACLs.

##### app.quickcase.claims/groups

Optional, String. Comma-separated list of groups the user is a member of, relevant when the user access level is `GROUP`.
Mutually exclusive with claim `app.quickcase.claims/organisations`, this claim should only be used when role-driven authorisation is used.

#### Limitations

##### JWT access token

The current implementation mandates for the access token to be a JWT token with a `scope` claim which is used to drive support for [Client credentials grant](#client-credentials-grant).

For users, additional custom claims are required to expose:
- name
- email
- roles
- groups

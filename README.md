# security.augustus

`security.augustus` performs stateful token currentness checks.

In `v1`, Augustus is intentionally narrow: it verifies that a trusted token's embedded version matches the current version for that user in system state.

## What Augustus Owns

- token version currentness checks
- a small public API for version verification
- a result model for `success`, `version mismatch`, and `version lookup failed`

## What Augustus Does Not Own

- JWT signature verification
- token issuance
- claim normalization
- authorization

Those concerns belong in neighboring libraries such as `themis` and `hermes`. Augustus only assumes it is given a trusted token-shaped input.

## What Consumers Must Provide

Consumers provide two things:

1. A `Token<U, V>` implementation containing:
   - the user identity to check
   - the token version to compare
2. A `UserVersionResolver<U, V>` that returns the current version for that user from system state

## Basic Usage

```java
import de.gupta.security.augustus.api.TokenVersionVerifier;
import de.gupta.security.augustus.api.TokenVersionVerifierFactory;
import de.gupta.security.augustus.api.UserVersionResolver;
import de.gupta.security.augustus.domain.model.Token;

record UserToken(String user, Long version) implements Token<String, Long>
{
}

UserVersionResolver<String, Long> resolver = user -> 7L;
TokenVersionVerifier<String, Long> verifier = TokenVersionVerifierFactory.create(resolver);

boolean current = verifier.verifies(new UserToken("alice", 7L));
```

If you need failure detail instead of a boolean, use `verifyResult(...)`.

## Version Check Semantics

Augustus currently treats a token as current only when:

- `token.version().equals(currentVersionForUser)`

If version lookup throws at runtime, Augustus returns `VERSION_LOOKUP_FAILED`.

## Scope of v1

`v1.0.0` is suitable if you want Augustus to remain a small, framework-agnostic version-currentness library.

If you later want broader token currentness support, likely next additions would be:

- revocation checks
- session currentness checks
- composition of multiple currentness checks
package de.gupta.security.augustus.api;

import de.gupta.aletheia.trials.Fallible;
import de.gupta.aletheia.trials.Portent;
import de.gupta.security.augustus.domain.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class TokenRevocationVerifierImpl<ExternalIdentity, User>
		implements TokenRevocationVerifier<ExternalIdentity, User>
{
	private final UserResolver<ExternalIdentity, User> userResolver;
	private final UserRevocationResolver<User> revocationResolver;
	private final Duration clockSkew;

	static <ExternalIdentity, User> TokenRevocationVerifier<ExternalIdentity, User> create(
			final UserResolver<ExternalIdentity, User> userResolver,
			final UserRevocationResolver<User> revocationResolver,
			final Duration clockSkew)
	{
		return new TokenRevocationVerifierImpl<>(userResolver, revocationResolver, clockSkew);
	}

	@Override
	public TokenRevocationVerificationResult verify(final TokenIssuance<ExternalIdentity> issuance)
	{
		return Fallible.beckon(issuance)
		               .metamorphose(this::checkRevocation, exceptionally())
		               .coronate(Function.identity(),
							   _ -> TokenRevocationVerificationFailure.of(
									   TokenRevocationVerificationFailureReason.REVOCATION_STATE_UNAVAILABLE));
	}

	private TokenRevocationVerificationResult checkRevocation(final TokenIssuance<ExternalIdentity> issuance)
	{
		final Optional<User> user = userResolver.resolve(issuance.externalIdentity());
		if (user.isEmpty())
		{
			return TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.USER_NOT_FOUND);
		}

		final Instant lastRevokedAt = revocationResolver.lastRevokedAt(user.get());
		final Instant issuedAt = issuance.issuedAt();

		// Token is current if it was issued at or after the last revocation,
		// adjusted for clock skew: issuedAt + clockSkew >= lastRevokedAt
		if (!issuedAt.plus(clockSkew).isBefore(lastRevokedAt))
		{
			return TokenRevocationVerificationSuccess.instance();
		}

		return TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.TOKEN_SUPERSEDED);
	}

	private List<Portent<TokenRevocationVerificationResult>> exceptionally()
	{
		return List.of(
				Portent.foretell(RuntimeException.class,
						exception -> TokenRevocationVerificationFailure.of(
								TokenRevocationVerificationFailureReason.REVOCATION_STATE_UNAVAILABLE,
								exception.getMessage())));
	}

	private TokenRevocationVerifierImpl(final UserResolver<ExternalIdentity, User> userResolver,
	                                    final UserRevocationResolver<User> revocationResolver,
	                                    final Duration clockSkew)
	{
		this.userResolver = userResolver;
		this.revocationResolver = revocationResolver;
		this.clockSkew = clockSkew;
	}
}
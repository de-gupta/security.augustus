package de.gupta.security.augustus.api;

import java.time.Duration;

public final class TokenRevocationVerifierFactory
{
	public static <ExternalIdentity, User> TokenRevocationVerifier<ExternalIdentity, User> create(
			final UserResolver<ExternalIdentity, User> userResolver,
			final UserRevocationResolver<User> revocationResolver)
	{
		return create(userResolver, revocationResolver, Duration.ZERO);
	}

	public static <ExternalIdentity, User> TokenRevocationVerifier<ExternalIdentity, User> create(
			final UserResolver<ExternalIdentity, User> userResolver,
			final UserRevocationResolver<User> revocationResolver,
			final Duration clockSkew)
	{
		return TokenRevocationVerifierImpl.create(userResolver, revocationResolver, clockSkew);
	}

	private TokenRevocationVerifierFactory()
	{
	}
}
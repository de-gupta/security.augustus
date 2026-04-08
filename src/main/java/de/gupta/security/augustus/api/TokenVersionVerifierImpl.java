package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.Token;

final class TokenVersionVerifierImpl<U, V extends Comparable<V>> implements TokenVersionVerifier<U, V>
{
	private final UserVersionResolver<U, V> userVersionResolver;

	static <U, V extends Comparable<V>> TokenVersionVerifier<U, V> create(final UserVersionResolver<U, V> userVersionResolver)
	{
		return new TokenVersionVerifierImpl<>(userVersionResolver);
	}

	@Override
	public boolean verify(final Token<U, V> token)
	{
		return token.version().compareTo(userVersionResolver.versionForUser(token.user())) >= 0;
	}

	private TokenVersionVerifierImpl(final UserVersionResolver<U, V> userVersionResolver)
	{
		this.userVersionResolver = userVersionResolver;
	}
}
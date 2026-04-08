package de.gupta.security.augustus.api;

public final class TokenVersionVerifierFactory
{
	public static <U, V extends Comparable<V>> TokenVersionVerifier<U, V> create(
			final UserVersionResolver<U, V> userVersionResolver)
	{
		return TokenVersionVerifierImpl.create(userVersionResolver);
	}
}
package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.Token;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationResult;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationSuccess;

@FunctionalInterface
public interface TokenVersionVerifier<U, V extends Comparable<V>>
{
	TokenVersionVerificationResult<V> verify(final Token<U, V> token);

	default boolean verifies(final Token<U, V> token)
	{
		return verify(token) instanceof TokenVersionVerificationSuccess<V>;
	}
}
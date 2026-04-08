package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.Token;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationResult;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationSuccess;

public interface TokenVersionVerifier<U, V extends Comparable<V>>
{
	TokenVersionVerificationResult<V> verifyResult(Token<U, V> token);

	default boolean verify(final Token<U, V> token)
	{
		return verifyResult(token) instanceof TokenVersionVerificationSuccess<V>;
	}
}
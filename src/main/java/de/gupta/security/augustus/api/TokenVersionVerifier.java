package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.Token;

public interface TokenVersionVerifier<U, V extends Comparable<V>>
{
	boolean verify(Token<U, V> token);
}
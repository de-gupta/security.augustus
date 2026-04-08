package de.gupta.security.augustus.domain.model;

public record TokenVersionVerificationSuccess<V extends Comparable<V>>(V version)
		implements TokenVersionVerificationResult<V>
{
	public static <V extends Comparable<V>> TokenVersionVerificationSuccess<V> of(final V version)
	{
		return new TokenVersionVerificationSuccess<>(version);
	}
}
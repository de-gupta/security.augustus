package de.gupta.security.augustus.domain.model;

import java.util.Optional;

public record TokenVersionVerificationFailure<V extends Comparable<V>>(
		TokenVersionVerificationFailureReason reason, Optional<String> details)
		implements TokenVersionVerificationResult<V>
{
	public static <V extends Comparable<V>> TokenVersionVerificationFailure<V> of(
			final TokenVersionVerificationFailureReason reason)
	{
		return new TokenVersionVerificationFailure<>(reason, Optional.empty());
	}

	public static <V extends Comparable<V>> TokenVersionVerificationFailure<V> of(
			final TokenVersionVerificationFailureReason reason,
			final String details)
	{
		return new TokenVersionVerificationFailure<>(reason, Optional.ofNullable(details));
	}
}
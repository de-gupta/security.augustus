package de.gupta.security.augustus.domain.model;

import java.util.Optional;

public record TokenRevocationVerificationFailure(TokenRevocationVerificationFailureReason reason,
                                                 Optional<String> details)
		implements TokenRevocationVerificationResult
{
	public static TokenRevocationVerificationFailure of(final TokenRevocationVerificationFailureReason reason)
	{
		return new TokenRevocationVerificationFailure(reason, Optional.empty());
	}

	public static TokenRevocationVerificationFailure of(final TokenRevocationVerificationFailureReason reason,
	                                                    final String details)
	{
		return new TokenRevocationVerificationFailure(reason, Optional.ofNullable(details));
	}
}
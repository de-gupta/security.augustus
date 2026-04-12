package de.gupta.security.augustus.domain.model;

import java.time.Instant;
import java.util.Objects;

public record TokenIssuance<ExternalIdentity>(ExternalIdentity externalIdentity, Instant issuedAt)
{
	public static <ExternalIdentity> TokenIssuance<ExternalIdentity> of(final ExternalIdentity externalIdentity,
	                                                                    final Instant issuedAt)
	{
		return new TokenIssuance<>(externalIdentity, issuedAt);
	}

	public TokenIssuance
	{
		Objects.requireNonNull(externalIdentity, "externalIdentity must not be null");
		Objects.requireNonNull(issuedAt, "issuedAt must not be null");
	}
}
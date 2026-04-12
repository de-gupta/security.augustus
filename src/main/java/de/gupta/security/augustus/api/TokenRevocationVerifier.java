package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.TokenIssuance;
import de.gupta.security.augustus.domain.model.TokenRevocationVerificationResult;
import de.gupta.security.augustus.domain.model.TokenRevocationVerificationSuccess;

@FunctionalInterface
public interface TokenRevocationVerifier<ExternalIdentity, User>
{
	TokenRevocationVerificationResult verify(TokenIssuance<ExternalIdentity> issuance);

	default boolean verifies(final TokenIssuance<ExternalIdentity> issuance)
	{
		return verify(issuance) instanceof TokenRevocationVerificationSuccess;
	}
}
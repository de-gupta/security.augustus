package de.gupta.security.augustus.domain.model;

public record TokenRevocationVerificationSuccess() implements TokenRevocationVerificationResult
{
	private static final TokenRevocationVerificationSuccess INSTANCE = new TokenRevocationVerificationSuccess();

	public static TokenRevocationVerificationSuccess instance()
	{
		return INSTANCE;
	}
}
package de.gupta.security.augustus.domain.model;

public sealed interface TokenRevocationVerificationResult
		permits TokenRevocationVerificationSuccess, TokenRevocationVerificationFailure
{
}
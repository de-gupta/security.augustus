package de.gupta.security.augustus.domain.model;

public sealed interface TokenVersionVerificationResult<V extends Comparable<V>>
		permits TokenVersionVerificationSuccess, TokenVersionVerificationFailure
{
}
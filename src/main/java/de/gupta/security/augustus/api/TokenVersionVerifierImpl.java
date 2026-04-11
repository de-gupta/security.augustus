package de.gupta.security.augustus.api;

import de.gupta.aletheia.functional.Unfolding;
import de.gupta.aletheia.trials.Fallible;
import de.gupta.aletheia.trials.Portent;
import de.gupta.security.augustus.domain.model.*;

import java.util.List;
import java.util.function.Function;

final class TokenVersionVerifierImpl<U, V extends Comparable<V>> implements TokenVersionVerifier<U, V>
{
	private final UserVersionResolver<U, V> userVersionResolver;

	static <U, V extends Comparable<V>> TokenVersionVerifier<U, V> create(
			final UserVersionResolver<U, V> userVersionResolver)
	{
		return new TokenVersionVerifierImpl<>(userVersionResolver);
	}

	@Override
	public TokenVersionVerificationResult<V> verifyResult(final Token<U, V> token)
	{
		return Fallible.beckon(token)
		               .metamorphose(this::verifyResolvedVersion, exceptionally())
		               .coronate(Function.identity(),
							   _ -> TokenVersionVerificationFailure.of(
									   TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED));
	}

	private TokenVersionVerificationResult<V> verifyResolvedVersion(final Token<U, V> token)
	{
		return Unfolding.beckon(userVersionResolver.versionForUser(token.user()))
		                .<TokenVersionVerificationResult<V>>cleave(
		                        v -> v.compareTo(token.version()) == 0,
		                        TokenVersionVerificationSuccess::of,
		                        _ -> TokenVersionVerificationFailure.of(
		                                TokenVersionVerificationFailureReason.VERSION_MISMATCH))
		                .infuse(TokenVersionVerificationFailure.of(
		                        TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED));
	}

	private List<Portent<TokenVersionVerificationResult<V>>> exceptionally()
	{
		return List.of(
				Portent.foretell(RuntimeException.class,
						exception -> TokenVersionVerificationFailure.of(
								TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED,
								exception.getMessage())));
	}

	private TokenVersionVerifierImpl(final UserVersionResolver<U, V> userVersionResolver)
	{
		this.userVersionResolver = userVersionResolver;
	}
}
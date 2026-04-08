package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.Token;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationFailure;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationFailureReason;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationResult;
import de.gupta.security.augustus.domain.model.TokenVersionVerificationSuccess;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class TokenVersionVerifierTest
{
	private record TestToken(String user, Integer version) implements Token<String, Integer> {}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	final class VerifyResult
	{
		@Test
		void shouldReturnSuccessWhenTokenVersionMatchesResolvedVersion()
		{
			final TokenVersionVerifier<String, Integer> verifier = TokenVersionVerifierFactory.create(_ -> 7);

			assertThat(verifier.verifyResult(new TestToken("Alice", 7)))
					.isEqualTo(TokenVersionVerificationSuccess.of(7));
		}

		@ParameterizedTest
		@MethodSource("mismatchingVersions")
		void shouldReturnVersionMismatchWhenVersionsDoNotMatch(final TestCase testCase)
		{
			final TokenVersionVerifier<String, Integer> verifier =
					TokenVersionVerifierFactory.create(_ -> testCase.currentVersion());

			assertThat(verifier.verifyResult(new TestToken("Alice", testCase.tokenVersion())))
					.isEqualTo(
							TokenVersionVerificationFailure.of(TokenVersionVerificationFailureReason.VERSION_MISMATCH));
		}

		@Test
		void shouldReturnLookupFailedWhenResolverThrows()
		{
			final TokenVersionVerifier<String, Integer> verifier = TokenVersionVerifierFactory.create(_ ->
			{
				throw new IllegalStateException("resolver unavailable");
			});

			assertThat(verifier.verifyResult(new TestToken("Alice", 7)))
					.isEqualTo(TokenVersionVerificationFailure.of(
							TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED,
							"resolver unavailable"));
		}

		@Test
		void shouldReturnLookupFailedWhenResolverReturnsNull()
		{
			final TokenVersionVerifier<String, Integer> verifier = TokenVersionVerifierFactory.create(_ -> null);

			assertThat(verifier.verifyResult(new TestToken("Alice", 7)))
					.isEqualTo(TokenVersionVerificationFailure.of(
							TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED));
		}

		private Stream<Arguments> mismatchingVersions()
		{
			return Stream.of(
						 TestCase.of(4, 5),
						 TestCase.of(6, 5))
			             .map(Arguments::of);
		}

		private record TestCase(int tokenVersion, int currentVersion)
		{
			private static TestCase of(final int tokenVersion, final int currentVersion)
			{
				return new TestCase(tokenVersion, currentVersion);
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	final class Verify
	{
		@Test
		void shouldReturnTrueWhenResultIsSuccess()
		{
			final TokenVersionVerifier<String, Integer> verifier = TokenVersionVerifierFactory.create(_ -> 9);

			assertThat(verifier.verify(new TestToken("Alice", 9))).isTrue();
		}

		@ParameterizedTest
		@MethodSource("unsuccessfulResults")
		void shouldReturnFalseWhenResultIsNotSuccessful(final TokenVersionVerificationResult<Integer> result)
		{
			final TokenVersionVerifier<String, Integer> verifier = _ -> result;

			assertThat(verifier.verify(new TestToken("Alice", 9))).isFalse();
		}

		private Stream<Arguments> unsuccessfulResults()
		{
			return Stream.of(
						 TokenVersionVerificationFailure.<Integer>of(TokenVersionVerificationFailureReason.VERSION_MISMATCH),
						 TokenVersionVerificationFailure.<Integer>of(TokenVersionVerificationFailureReason.VERSION_LOOKUP_FAILED,
								 "resolver unavailable"))
			             .map(Arguments::of);
		}
	}
}
package de.gupta.security.augustus.api;

import de.gupta.security.augustus.domain.model.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class TokenRevocationVerifierTest
{
	private static final Instant EPOCH = Instant.EPOCH;
	private static final Instant T1 = Instant.parse("2026-01-01T10:00:00Z");
	private static final Instant T2 = Instant.parse("2026-01-01T11:00:00Z");

	private static final String EXTERNAL_ID = "user-123";
	private static final String USER = "local-user-123";

	private TokenRevocationVerifier<String, String> verifier(final Instant lastRevokedAt)
	{
		return TokenRevocationVerifierFactory.create(
				_ -> Optional.of(USER),
				_ -> lastRevokedAt);
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	final class VerifyResult
	{
		@Test
		void shouldReturnSuccessWhenTokenIssuedAfterLastRevocation()
		{
			// lastRevokedAt = T1, issuedAt = T2 (after revocation) → current
			final TokenRevocationVerificationResult result =
					verifier(T1).verify(TokenIssuance.of(EXTERNAL_ID, T2));

			assertThat(result).isEqualTo(TokenRevocationVerificationSuccess.instance());
		}

		@Test
		void shouldReturnSuccessWhenTokenIssuedAtSameInstantAsRevocation()
		{
			// issuedAt == lastRevokedAt → current (edge case: issued exactly at revocation time)
			final TokenRevocationVerificationResult result =
					verifier(T1).verify(TokenIssuance.of(EXTERNAL_ID, T1));

			assertThat(result).isEqualTo(TokenRevocationVerificationSuccess.instance());
		}

		@Test
		void shouldReturnSuccessWhenNeverRevoked()
		{
			// lastRevokedAt = EPOCH (never revoked sentinel) → always current
			final TokenRevocationVerificationResult result =
					verifier(EPOCH).verify(TokenIssuance.of(EXTERNAL_ID, T1));

			assertThat(result).isEqualTo(TokenRevocationVerificationSuccess.instance());
		}

		@Test
		void shouldReturnTokenSupersededWhenTokenIssuedBeforeLastRevocation()
		{
			// lastRevokedAt = T2, issuedAt = T1 (before revocation) → superseded
			final TokenRevocationVerificationResult result =
					verifier(T2).verify(TokenIssuance.of(EXTERNAL_ID, T1));

			assertThat(result).isEqualTo(
					TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.TOKEN_SUPERSEDED));
		}

		@Test
		void shouldReturnUserNotFoundWhenResolverReturnsEmpty()
		{
			final TokenRevocationVerifier<String, String> verifier = TokenRevocationVerifierFactory.create(
					_ -> Optional.empty(),
					_ -> T1);

			final TokenRevocationVerificationResult result =
					verifier.verify(TokenIssuance.of(EXTERNAL_ID, T2));

			assertThat(result).isEqualTo(
					TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.USER_NOT_FOUND));
		}

		@Test
		void shouldReturnRevocationStateUnavailableWhenUserResolverThrows()
		{
			final TokenRevocationVerifier<String, String> verifier = TokenRevocationVerifierFactory.create(
					_ ->
					{
						throw new IllegalStateException("db unavailable");
					},
					_ -> T1);

			final TokenRevocationVerificationResult result =
					verifier.verify(TokenIssuance.of(EXTERNAL_ID, T2));

			assertThat(result).isEqualTo(
					TokenRevocationVerificationFailure.of(
							TokenRevocationVerificationFailureReason.REVOCATION_STATE_UNAVAILABLE,
							"db unavailable"));
		}

		@Test
		void shouldReturnRevocationStateUnavailableWhenRevocationResolverThrows()
		{
			final TokenRevocationVerifier<String, String> verifier = TokenRevocationVerifierFactory.create(
					_ -> Optional.of(USER),
					_ ->
					{
						throw new RuntimeException("revocation store offline");
					});

			final TokenRevocationVerificationResult result =
					verifier.verify(TokenIssuance.of(EXTERNAL_ID, T2));

			assertThat(result).isEqualTo(
					TokenRevocationVerificationFailure.of(
							TokenRevocationVerificationFailureReason.REVOCATION_STATE_UNAVAILABLE,
							"revocation store offline"));
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	final class ClockSkew
	{
		@Test
		void shouldAcceptTokenIssuedSlightlyBeforeRevocationWhenWithinClockSkew()
		{
			// lastRevokedAt = T2, issuedAt = T2 - 20s, clockSkew = 30s
			// issuedAt + clockSkew = T2 + 10s >= T2 → accepted
			final Instant issuedAt = T2.minusSeconds(20);
			final Duration clockSkew = Duration.ofSeconds(30);

			final TokenRevocationVerifier<String, String> verifier = TokenRevocationVerifierFactory.create(
					_ -> Optional.of(USER),
					_ -> T2,
					clockSkew);

			assertThat(verifier.verify(TokenIssuance.of(EXTERNAL_ID, issuedAt)))
					.isEqualTo(TokenRevocationVerificationSuccess.instance());
		}

		@Test
		void shouldRejectTokenIssuedBeforeRevocationWhenBeyondClockSkew()
		{
			// lastRevokedAt = T2, issuedAt = T2 - 60s, clockSkew = 30s
			// issuedAt + clockSkew = T2 - 30s < T2 → rejected
			final Instant issuedAt = T2.minusSeconds(60);
			final Duration clockSkew = Duration.ofSeconds(30);

			final TokenRevocationVerifier<String, String> verifier = TokenRevocationVerifierFactory.create(
					_ -> Optional.of(USER),
					_ -> T2,
					clockSkew);

			assertThat(verifier.verify(TokenIssuance.of(EXTERNAL_ID, issuedAt)))
					.isEqualTo(TokenRevocationVerificationFailure.of(
							TokenRevocationVerificationFailureReason.TOKEN_SUPERSEDED));
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	final class Verifies
	{
		@Test
		void shouldReturnTrueWhenResultIsSuccess()
		{
			assertThat(verifier(T1).verifies(TokenIssuance.of(EXTERNAL_ID, T2))).isTrue();
		}

		@ParameterizedTest
		@MethodSource("failureResults")
		void shouldReturnFalseWhenResultIsFailure(final TokenRevocationVerificationResult result)
		{
			final TokenRevocationVerifier<String, String> fixed = _ -> result;
			assertThat(fixed.verifies(TokenIssuance.of(EXTERNAL_ID, T2))).isFalse();
		}

		private Stream<Arguments> failureResults()
		{
			return Stream.of(
								 TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.TOKEN_SUPERSEDED),
								 TokenRevocationVerificationFailure.of(TokenRevocationVerificationFailureReason.USER_NOT_FOUND),
								 TokenRevocationVerificationFailure.of(
										 TokenRevocationVerificationFailureReason.REVOCATION_STATE_UNAVAILABLE,
										 "offline"))
			             .map(Arguments::of);
		}
	}
}
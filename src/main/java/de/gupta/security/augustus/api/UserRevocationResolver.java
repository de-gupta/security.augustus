package de.gupta.security.augustus.api;

import java.time.Instant;

@FunctionalInterface
public interface UserRevocationResolver<User>
{
	Instant lastRevokedAt(User user);
}
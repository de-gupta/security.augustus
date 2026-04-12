package de.gupta.security.augustus.api;

import java.util.Optional;

@FunctionalInterface
public interface UserResolver<ExternalIdentity, User>
{
	Optional<User> resolve(ExternalIdentity externalIdentity);
}
package de.gupta.security.augustus.api;

@FunctionalInterface
public interface UserVersionResolver<U, V extends Comparable<V>>
{
	V versionForUser(U user);
}
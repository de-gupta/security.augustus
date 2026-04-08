package de.gupta.security.augustus.domain.model;

public interface Token<U, V extends Comparable<V>>
{
	U user();
	V version();
}
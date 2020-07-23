package de.tu_berlin.imolcean.tdm.api.interfaces.updater;

/**
 * Represents a special case of {@link SchemaUpdater} that migrates data
 * to the new schema by itself or using external tools.
 *
 * If a schema updater performs data migration (e.g. with DML section in Liquibase changesets),
 * then it should extend this interface and not {@link SchemaUpdater}. When a schema update
 * is performed using a {@link DataAwareSchemaUpdater}, the system will not request mapping scripts
 * from the user, neither will it try to map data automatically.
 */
public interface DataAwareSchemaUpdater extends SchemaUpdater {}

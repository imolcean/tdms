package de.tu_berlin.imolcean.tdm.core;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;

public class SchemaDifferBuilder
{
    private final ObjectDifferBuilder objectDifferBuilder;

    public SchemaDifferBuilder()
    {
        objectDifferBuilder = ObjectDifferBuilder.startBuilding();
        objectDifferBuilder
                .filtering()
                .omitNodesWithState(DiffNode.State.UNTOUCHED);
        objectDifferBuilder
                .filtering()
                .omitNodesWithState(DiffNode.State.CIRCULAR);
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("fullName");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("parent");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("exportedForeignKeys");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("importedForeignKeys");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("deferrable");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("initiallyDeferred");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("crawlInfo");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("caseSensitive");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("indexes");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("databaseInfo");
        objectDifferBuilder
                .inclusion()
                .exclude()
                .propertyName("jdbcDriverInfo");
    }

    public ObjectDiffer build()
    {
        return objectDifferBuilder.build();
    }
}

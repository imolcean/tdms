package io.github.imolcean.tdms.core.utils;

import de.danielbechler.diff.node.DiffNode;
import schemacrawler.schema.Column;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Table;

@Deprecated
public class SchemaDiffPrinter
{
    public static void print(DiffNode diff)
    {
        diff.visit((node, visit) -> {
            final DiffNode.State nodeState = node.getState();
            final boolean print = DatabaseObject.class.isAssignableFrom(node.getValueType());

            if (print)
            {
                System.out.println(node.getPath() + " (" + nodeState + ")");
            }

            if (Table.class.isAssignableFrom(node.getValueType()) && nodeState != DiffNode.State.CHANGED)
            {
                visit.dontGoDeeper();
            }

            if (Column.class.isAssignableFrom(node.getValueType()))
            {
                visit.dontGoDeeper();
            }
        });

        System.out.println("Diff has changes: " + diff.hasChanges());
        System.out.println("Diff has children: " + diff.hasChildren());

        System.out.println("=====================================================");

        diff.visit((node, visit) -> System.out.println(node.getPath() + " => " + node.getState()));
    }
}

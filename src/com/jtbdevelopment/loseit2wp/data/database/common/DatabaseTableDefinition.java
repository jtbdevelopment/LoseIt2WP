package com.jtbdevelopment.loseit2wp.data.database.common;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/22/12
 * Time: 11:32 AM
 */
public class DatabaseTableDefinition {
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String OPEN_PAREN = " ( ";
    private static final String FIELD_DELIM = ", ";
    private static final String CLOSE_PAREN = " ); ";
    private final String tableName;
    private final List<DatabaseFieldDefinition> fields;

    public DatabaseTableDefinition(final String tableName, final List<DatabaseFieldDefinition> fields) {
        this.tableName = tableName;
        this.fields = fields;
    }

    public String getTableName() {
        return tableName;
    }

    public String createDatabaseTable() {
        StringBuilder builder = new StringBuilder();

        builder.append(CREATE_TABLE).append(tableName).append(OPEN_PAREN);
        boolean first = true;
        for (DatabaseFieldDefinition field : fields) {
            if (!first) {
                builder.append(FIELD_DELIM);
            } else {
                first = false;
            }

            builder.append(field.createColumn());
        }
        builder.append(CLOSE_PAREN);

        for (DatabaseFieldDefinition field : fields) {
            builder.append(field.createIndex(tableName));
        }
        return builder.toString();
    }
}

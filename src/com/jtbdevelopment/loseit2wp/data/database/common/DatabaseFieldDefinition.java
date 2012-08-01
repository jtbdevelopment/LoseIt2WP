package com.jtbdevelopment.loseit2wp.data.database.common;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/22/12
 * Time: 11:32 AM
 */
public class DatabaseFieldDefinition {
    //  Define whitespace around before all strings so automatically included
    public static final String INTEGER = " INTEGER";
    public static final String TEXT = " TEXT";

    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String AUTO_INCREMENT = " AUTOINCREMENT";
    private static final String CREATE_INDEX = "CREATE INDEX ";
    private static final String ON = " ON ";
    private static final String OPEN_PAREN = " ( ";
    private static final String CLOSE_PAREN = " ); ";

    private final String fieldName;
    private final String dataType;
    private final boolean primaryKey;
    private final boolean simpleIndex;
    private final boolean autoIncrement;

    public DatabaseFieldDefinition(final String fieldName, String dataType, boolean primaryKey, boolean autoIncrement, boolean simpleIndex) {
        this.fieldName = fieldName;
        this.dataType = dataType;
        this.primaryKey = primaryKey;
        this.simpleIndex = simpleIndex;
        this.autoIncrement = autoIncrement;
    }

    public String createColumn() {
        StringBuilder builder = new StringBuilder();
        builder.append(fieldName).append(dataType);
        if (primaryKey) {
            builder.append(PRIMARY_KEY);
        }
        if (autoIncrement) {
            builder.append(AUTO_INCREMENT);
        }
        return builder.toString();
    }

    public String createIndex(final String tableName) {
        if (simpleIndex) {
            return new StringBuilder()
                    .append(CREATE_INDEX)
                    .append(fieldName)
                    .append(ON)
                    .append(tableName)
                    .append(OPEN_PAREN)
                    .append(fieldName)
                    .append(CLOSE_PAREN)
                    .toString();
        } else {
            return "";
        }
    }
}

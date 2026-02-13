package sqlbuilder;

import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.exceptions.ValueCannotBeEmptyException;
import sqlbuilder.expressions.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class SelectBuilder {
    private final SqlDialect DIALECT;
    private final String SCHEMA;

    private final List<String> columns = new ArrayList<>();
    private final List<String> tables = new ArrayList<>();
    private final List<String> tablesContext = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    private int limit = -1;
    private int offset = 0;

    public SelectBuilder(SqlDialect dialect) {
        this(dialect, null);
    }

    public SelectBuilder(SqlDialect dialect, String schema) {
        this.DIALECT = dialect;
        this.SCHEMA = schema;
    }

    public SelectBuilder select(String... columns) {
        if(columns.length == 0) {
            throw new ValueCannotBeEmptyException("columns");
        }

        Arrays.stream(columns)
                .map(DIALECT::quote)
                .forEach(this.columns::add);
        return this;
    }

    public SelectBuilder from(String... tables) {
        if(tables.length == 0) {
            throw new ValueCannotBeEmptyException("tables");
        }

        List<String> tableList = Arrays.asList(tables);
        this.tables.addAll(tableList.stream().map(this::addSchemaToTable).toList());
        this.tablesContext.addAll(tableList);
        return this;
    }

    public SelectBuilder from(String table) {
        if(table == null || table.isBlank()) {
            throw new ValueCannotBeEmptyException("table");
        }

        this.tables.add(addSchemaToTable(table));
        this.tablesContext.add(table);
        return this;
    }

    public SelectBuilder from(String table, String alias) {
        if(alias == null || alias.isBlank()) {
            return from(table);
        }

        this.tables.add(addSchemaToTable(table) + " " + alias);
        this.tablesContext.add(table);
        return this;
    }

    /**
     * Define conditions to limit the query result.
     * When called multiple times the conditions are chained together using an AND.
     *
     * @param condition The condition
     */
    public SelectBuilder where(Condition condition) {
        if(condition == null) {
            return this;
        }

        conditions.add(condition);
        return this;
    }

    /**
     * Sets the limit for how many rows the SQL statement will return.
     *
     * @param limit The number of rows. All values smaller than 1 are interpreted as no limit
     */
    public SelectBuilder limit(int limit) {
        this.limit = limit < 1 ? -1 : limit;
        return this;
    }

    /**
     * Sets the offset where the limit starts
     *
     * @param offset The offset. All values smaller than 1 are interpreted as an offset of 0
     */
    public SelectBuilder offset(int offset) {
        this.offset = offset < 1 ? 0 : offset;
        return this;
    }

    public Query build() {
        if(tables.isEmpty()) {
            throw new IllegalStateException("A table to select from must be specified");
        }

        if(columns.isEmpty()) {
            columns.add("*");
        }

        StringJoiner statement = new StringJoiner(" ")
                .add("SELECT")
                .add(String.join(", ", columns))
                .add("FROM")
                .add(String.join(", ", tables));

        if(!conditions.isEmpty()) {
            statement.add("WHERE");
            conditions.forEach(condition -> statement.add(condition.toSql()));
        }

        return new Query(statement.toString());
    }

    private String addSchemaToTable(String table) {
        if(SCHEMA == null || SCHEMA.isBlank()) {
            return table;
        }

        return SCHEMA + "." + table;
    }
}

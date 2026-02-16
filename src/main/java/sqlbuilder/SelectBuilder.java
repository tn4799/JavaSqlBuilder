package sqlbuilder;

import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.exceptions.ValueCannotBeEmptyException;
import sqlbuilder.expressions.Condition;

import java.util.*;

public class SelectBuilder {
    private static final String ERROR_MESSAGE_MULTIPLE_ORDER_DIRECTION_CALLS = "order direction can only be set once. Multiple calls of desc() or asc() are not allowed!";
    private final SqlDialect dialect;
    private final String schema;

    private final List<String> columns = new ArrayList<>();
    private final List<String> tables = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();
    private final List<String> groupColumns = new ArrayList<>();
    private final List<String> orderColumns = new ArrayList<>();
    private String orderDirection = null;
    private boolean distinct = false;

    private int limit = -1;
    private int offset = 0;

    public SelectBuilder(SqlDialect dialect) {
        this(dialect, null);
    }

    public SelectBuilder(SqlDialect dialect, String schema) {
        this.dialect = dialect;
        this.schema = schema;
    }

    public SelectBuilder select(String... columns) {
        if(columns == null || columns.length == 0) {
            throw new ValueCannotBeEmptyException("columns");
        }

        Arrays.stream(columns)
                .map(column -> addAliasToColumn(dialect.quote(column), column))
                .forEach(this.columns::add);
        return this;
    }

    public SelectBuilder select(String column) {
        select(column, column);
        return this;
    }

    public SelectBuilder select(String column, String alias) {
        if(column == null || column.isBlank()) {
            throw new ValueCannotBeEmptyException("column");
        }

        column = addAliasToColumn(dialect.quote(column), alias);

        this.columns.add(column);
        return this;
    }

    public SelectBuilder selectDistinct(String... columns) {
        select(columns);
        distinct = true;
        return this;
    }

    public SelectBuilder selectDistinct(String column) {
        select(column);
        distinct = true;
        return this;
    }

    public SelectBuilder selectDistinct(String column, String alias) {
        select(column, alias);
        distinct = true;
        return this;
    }

    public SelectBuilder distinct() {
        distinct = true;
        return this;
    }

    public SelectBuilder from(String... tables) {
        if(tables.length == 0) {
            throw new ValueCannotBeEmptyException("tables");
        }

        List<String> tableList = Arrays.asList(tables);
        this.tables.addAll(tableList.stream().map(this::addSchemaToTable).toList());
        return this;
    }

    public SelectBuilder from(String table) {
        if(table == null || table.isBlank()) {
            throw new ValueCannotBeEmptyException("table");
        }

        from(table, table);
        return this;
    }

    public SelectBuilder from(String table, String alias) {
        if(alias == null || alias.isBlank()) {
            return from(table);
        }

        if(table == null || table.isBlank()) {
            throw new ValueCannotBeEmptyException("table");
        }

        this.tables.add(addSchemaToTable(table) + " " + alias);
        return this;
    }

    public SelectBuilder join(String table, Condition joinCondition) {
        join(table, table, joinCondition);
        return this;
    }

    public SelectBuilder join(String table, String alias, Condition joinCondition) {
        StringJoiner join = new StringJoiner(" ")
                .add("JOIN");
        join.add(addSchemaToTable(table))
                .add(alias)
                .add("ON")
                .add(joinCondition.toSql());

        joins.add(join.toString());
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

    public SelectBuilder groupBy(String... columns) {
        groupColumns.addAll(List.of(columns));
        return this;
    }

    public SelectBuilder orderBy(String... columns) {
        orderColumns.addAll(List.of(columns));
        return this;
    }

    public SelectBuilder desc() {
        if(orderDirection != null) {
            throw new IllegalStateException(ERROR_MESSAGE_MULTIPLE_ORDER_DIRECTION_CALLS);
        }

        orderDirection = "DESC";
        return this;
    }

    public SelectBuilder asc() {
        if(orderDirection != null) {
            throw new IllegalStateException(ERROR_MESSAGE_MULTIPLE_ORDER_DIRECTION_CALLS);
        }

        orderDirection = "ASC";
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

        List<Object> parameters = new ArrayList<>();

        if(columns.isEmpty()) {
            columns.add("*");
        }

        StringJoiner statement = new StringJoiner(" ")
                .add("SELECT");
        if(distinct) {
            statement.add("DISTINCT");
        }
        statement.add(String.join(", ", columns))
                .add("FROM")
                .add(String.join(", ", tables));

        joins.forEach(statement::add);

        if(!conditions.isEmpty()) {
            statement.add("WHERE");

            Condition.CompositeCondition chainedConditions = new Condition.CompositeCondition("AND", conditions);
            parameters.addAll(chainedConditions.getParameters());
            statement.add(chainedConditions.toSql());
        }

        if(!groupColumns.isEmpty()) {
            statement.add("GROUP BY")
                    .add(String.join(", ", groupColumns));
        }

        if(!orderColumns.isEmpty()) {
            if(orderDirection == null) {
                orderDirection = "DESC";
            }

            statement.add("ORDER BY").add(String.join(", ", orderColumns)).add(orderDirection);
        }

        if(limit > -1) {
            statement.add(dialect.applyPaging(limit, offset));
        }

        return new Query(statement.toString(), parameters);
    }

    private String addSchemaToTable(String table) {
        if(schema == null || schema.isBlank()) {
            return table;
        }

        return schema + "." + table;
    }

    private static String addAliasToColumn(String column, String alias) {
        if(column.contains(".")){
            column = column + " AS " + quoteAlias(alias);
        }

        return column;
    }

    private static String quoteAlias(String alias) {
        return "\"%s\"".formatted(alias);
    }
}

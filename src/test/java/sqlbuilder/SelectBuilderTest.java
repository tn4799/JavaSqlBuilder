package sqlbuilder;

import org.junit.Test;
import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.exceptions.ValueCannotBeEmptyException;
import sqlbuilder.expressions.Expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SelectBuilderTest {
    public static final String COLUMN_A = "columnA";
    public static final String COLUMN_B = "columnB";
    public static final SqlDialect DIALECT = new SqlDialect.OracleDialect();
    private static final String TABLE_A = "TABLE_A";

    @Test
    public void testSelectAll() {
        String expected = "SELECT * FROM " + getTableWithAlias(TABLE_A);
        Query query = new SelectBuilder(DIALECT)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    @Test
    public void testSelectOneColumn() {
        String expected = "SELECT " + getColumnWithAlias(COLUMN_A) + " FROM " + getTableWithAlias(TABLE_A);

        SqlDialect dialect = new SqlDialect.OracleDialect();
        Query query = new SelectBuilder(dialect)
                .select(COLUMN_A)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(dialect));
    }

    @Test
    public void testSelectMultipleColumns() {
        String expected = "SELECT " + getColumnWithAlias(COLUMN_A) + ", " + getColumnWithAlias(COLUMN_B) + " FROM " + getTableWithAlias(TABLE_A);

        Query query = new SelectBuilder(DIALECT)
                .select(COLUMN_A, COLUMN_B)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    @Test
    public void testSelectColumnWithAlias() {
        String alias = "alias1";
        String expected = "SELECT " + getColumnWithAlias(COLUMN_A, alias) + " FROM " + getTableWithAlias(TABLE_A);

        Query query = new SelectBuilder(DIALECT)
                .selectWithAlias(COLUMN_A, alias)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    @Test
    public void testEmptyBuilder() {
        assertThrows(IllegalStateException.class, () -> new SelectBuilder(DIALECT).build());
    }

    @Test
    public void testColumnWithAliasWithEmptyColumn() {
        assertThrows(ValueCannotBeEmptyException.class, () -> new SelectBuilder(DIALECT)
                .selectWithAlias("", "alias1").from(TABLE_A));
    }

    @Test
    public void testEmptyFrom() {
        assertThrows(ValueCannotBeEmptyException.class, () -> new SelectBuilder(DIALECT).from());
    }

    @Test
    public void testEmptyTableName() {
        assertThrows(ValueCannotBeEmptyException.class, () -> new SelectBuilder(DIALECT).from("  "));
        assertThrows(ValueCannotBeEmptyException.class, () -> new SelectBuilder(DIALECT).from((String) null));
    }

    @Test
    public void testSelectFromWhereColumnEqualsStringValue() {
        String stmt = "SELECT * FROM " + getTableWithAlias(TABLE_A) + " WHERE " + COLUMN_A;
        String expectedPopulated = stmt + " = 'A'";
        String expectedPrepared = stmt + " = ?";

        Query query = new SelectBuilder(DIALECT)
                .select()
                .from(TABLE_A)
                .where(Expression.eq(COLUMN_A, "A"))
                .build();
        
        assertEquals(expectedPopulated, query.getPopulatedStatement(DIALECT));
        assertEquals(expectedPrepared, query.getStatement());
    }

    @Test
    public void testSelectFromWhereColumnEqualsNumberValue() {
        String stmt = "SELECT * FROM " + getTableWithAlias(TABLE_A) + " WHERE " + COLUMN_A;
        int value = 42;
        String expectedPopulated = stmt + " = " + value;
        String expectedPrepared = stmt + " = ?";

        Query query = new SelectBuilder(DIALECT)
                .select()
                .from(TABLE_A)
                .where(Expression.eq(COLUMN_A, value))
                .build();

        assertEquals(expectedPopulated, query.getPopulatedStatement(DIALECT));
        assertEquals(expectedPrepared, query.getStatement());
    }

    private static String getTableWithAlias(String table) {
        return table + " " + table;
    }

    private static String getColumnWithAlias(String column) {
        return getColumnWithAlias(column, column);
    }

    private static String getColumnWithAlias(String column, String alias) {
        return column + " AS " + DIALECT.quote(alias);
    }
}

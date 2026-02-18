package sqlbuilder;

import org.junit.Test;
import sqlbuilder.dialects.SqlDialect;

import static org.junit.Assert.assertEquals;

public class SelectBuilderTest {
    public static final String COLUMN_A = "columnA";
    public static final String COLUMN_B = "columnB";
    public static final SqlDialect DIALECT = new SqlDialect.OracleDialect();
    private static final String TABLE_A = "TABLE_A";

    @Test
    public void testSelectAll() {
        String expected = "SELECT * FROM " + getValueWithAlias(TABLE_A);
        Query query = new SelectBuilder(DIALECT)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    @Test
    public void testSelectOneColumn() {
        String COLUMN_A = "columnA";
        String expected = "SELECT " + COLUMN_A + " FROM " + getValueWithAlias(TABLE_A);

        SqlDialect dialect = new SqlDialect.OracleDialect();
        Query query = new SelectBuilder(dialect)
                .select(COLUMN_A)
                .from(TABLE_A)
                .build()
                ;

        assertEquals(expected, query.getPopulatedStatement(dialect));
    }

    @Test
    public void testSelectMultipleColumns() {
        String expected = "SELECT " + COLUMN_A + ", " + COLUMN_B + " FROM " + getValueWithAlias(TABLE_A);

        Query query = new SelectBuilder(DIALECT)
                .select(COLUMN_A, COLUMN_B)
                .from(TABLE_A)
                .build()
                ;

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    @Test
    public void testSelectColumnWithAlias() {
        String alias = "alias1";
        String expected = "SELECT " + COLUMN_A + " " + alias + " FROM " + getValueWithAlias(TABLE_A);

        Query query = new SelectBuilder(DIALECT)
                .selectWithAlias(COLUMN_A, alias)
                .from(TABLE_A)
                .build();

        assertEquals(expected, query.getPopulatedStatement(DIALECT));
    }

    private static String getValueWithAlias(String value) {
        return value + " " + value;
    }
}

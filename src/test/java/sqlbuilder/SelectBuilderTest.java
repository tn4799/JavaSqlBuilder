package sqlbuilder;

import org.junit.jupiter.api.Test;
import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.expressions.Expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectBuilderTest {

    @Test
    void testBasicSelect() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.from("users").build();
        assertEquals("SELECT * FROM users", query.getStatement());
    }

    @Test
    void testSelectColumns() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.select("id", "name").from("users").build();
        assertEquals("SELECT \"id\", \"name\" FROM users", query.getStatement());
    }

    @Test
    void testSelectDistinct() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.selectDistinct("role").from("users").build();
        assertEquals("SELECT DISTINCT \"role\" FROM users", query.getStatement());
    }

    @Test
    void testWhereClause() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.from("users")
                .where(Expression.eq("id", 1))
                .build();
        assertEquals("SELECT * FROM users WHERE id = ?", query.getStatement());
    }

    @Test
    void testComplexWhereClause() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.from("users")
                .where(Expression.eq("status", "active").and().gt("age", 18))
                .build();
        assertEquals("SELECT * FROM users WHERE status = ? AND age > ?", query.getStatement());
    }

    @Test
    void testJoin() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.select("u.name", "o.order_date")
                .from("users", "u")
                .join("orders", "o", Expression.eq("u.id", Expression.column("o.user_id")))
                .build();
        assertEquals("SELECT \"u.name\", \"o.order_date\" FROM users u JOIN orders o ON u.id = o.user_id", query.getStatement());
    }

    @Test
    void testOrderByLimitOffset() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.from("products")
                .orderBy("price").desc()
                .limit(10)
                .offset(5)
                .build();
        assertEquals("SELECT * FROM products ORDER BY price DESC LIMIT 10 OFFSET 5", query.getStatement());
    }

    @Test
    void testOracleDialectPaging() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.OracleDialect());
        Query query = builder.from("tasks")
                .limit(5)
                .offset(0)
                .build();
        assertEquals("SELECT * FROM tasks LIMIT 5 OFFSET 0", query.getStatement());
    }

    @Test
    void testNullConditions() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.from("users")
                .where(Expression.isNull("deleted_at"))
                .where(Expression.isNotNull("email"))
                .build();
        assertEquals("SELECT * FROM users WHERE deleted_at IS NULL AND email IS NOT NULL", query.getStatement());
    }

    @Test
    void testMultipleJoins() {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
        Query query = builder.select("u.name", "o.id", "p.name")
                .from("users", "u")
                .join("orders", "o", Expression.eq("u.id", Expression.column("o.user_id")))
                .join("order_items", "oi", Expression.eq("o.id", Expression.column("oi.order_id")))
                .join("products", "p", Expression.eq("oi.product_id", Expression.column("p.id")))
                .build();
        assertEquals("SELECT \"u.name\", \"o.id\", \"p.name\" FROM users u JOIN orders o ON u.id = o.user_id JOIN order_items oi ON o.id = oi.order_id JOIN products p ON oi.product_id = p.id", query.getStatement());
    }
}

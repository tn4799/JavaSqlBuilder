# JavaSqlBuilder

JavaSqlBuilder is a lightweight, fluent API library for Java 21 designed to construct SQL queries programmatically. It provides a type-safe and readable way to build complex SELECT statements, handling various SQL dialects and preventing common syntax errors.

## Features

- **Fluent Interface:** Build queries using a natural, readable method chaining approach.
- **Multi-Dialect Support:** Out-of-the-box support for Oracle, PostgreSQL, H2, DB2, and MSSQL.
- **Dynamic Conditions:** Complex `WHERE` clause construction with `AND`/`OR` chaining.
- **Joins & Aliases:** Support for table joins and column/table aliasing.
- **Paging:** Simplified `LIMIT` and `OFFSET` handling tailored to specific database dialects.
- **Schema Support:** Easily prefix tables with database schemas.

## Requirements

- **Java:** 21 or higher
- **Build Tool:** Maven (or any tool compatible with Maven dependencies)

## Installation

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>JavaSqlBuilder2</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Technical Usage

### 1. Initializing the Builder
Every query starts with a `SelectBuilder`, which requires a `SqlDialect`.

```java
import sqlbuilder.SelectBuilder;
import sqlbuilder.dialects.SqlDialect;

SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
```

### 2. Basic Select Query
Define columns and the source table. If no columns are specified, `*` is used by default.

```java
Query query = builder
    .select("id", "username", "email")
    .from("users")
    .build();

System.out.println(query.getStatement()); 
// Output: SELECT "id", "username", "email" FROM users
```

### 3. Complex Where Clauses
Use the `Expression` utility to create conditions. Conditions can be chained using `.and()` or `.or()`.

```java
import sqlbuilder.expressions.Expression;

builder.select("name")
       .from("employees")
       .where(Expression.eq("department", "IT")
           .and().gt("salary", 50000)
           .or().eq("role", "Admin"));
```

### 4. Joins
Perform joins by specifying the target table and the join condition.

```java
builder.select("u.name", "o.order_date")
       .from("users", "u")
       .join("orders", "o", Expression.eq("u.id", Expression.column("o.user_id")));
```

### 5. Ordering and Paging
Easily add sorting and pagination to your results.

```java
builder.from("products")
       .orderBy("price").desc()
       .limit(10)
       .offset(20);
```

## Architecture Overview

- **`SelectBuilder`**: The core engine that orchestrates the query construction.
- **`SqlDialect`**: An interface allowing the builder to adapt to different SQL syntaxes (e.g., paging logic).
- **`Condition` & `Expression`**: A robust system for building logical SQL expressions. `Expression` acts as a factory for creating various comparison conditions.
- **`Operand`**: Represents the building blocks of conditions (Columns, Values, or Parameters).
- **`Query`**: The final product containing the generated SQL string and prepared statement parameters.

## Contributing

1. Implement the `SqlDialect` interface for new database engines.
2. Extend `Expression` for more complex SQL functions (e.g., `IN`, `BETWEEN`).
3. Enhance the `Query.execute()` method to integrate with JDBC.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

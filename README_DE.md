# JavaSqlBuilder

JavaSqlBuilder ist eine leichtgewichtige Library mit einer fluiden API für Java 21, die zur programmatischen Erstellung von SQL-Abfragen entwickelt wurde. Sie bietet eine typsichere und gut lesbare Möglichkeit, komplexe SELECT-Statements zu erstellen, unterstützt verschiedene SQL-Dialekte und verhindert gängige Syntaxfehler.

## Features

- **Fluide Schnittstelle:** Erstellen Sie Abfragen durch intuitives Method-Chaining.
- **Multi-Dialekt-Unterstützung:** Standardunterstützung für Oracle, PostgreSQL, H2, DB2 und MSSQL.
- **Dynamische Bedingungen:** Erstellung komplexer `WHERE`-Klauseln mit `AND`/`OR`-Verknüpfungen.
- **Joins & Aliase:** Unterstützung für Table-Joins sowie Spalten- und Tabellen-Aliase.
- **Paging:** Vereinfachte Handhabung von `LIMIT` und `OFFSET`, angepasst an den jeweiligen Datenbank-Dialekt.
- **Schema-Unterstützung:** Tabellen können einfach mit Datenbank-Schemata präfixiert werden.

## Anforderungen

- **Java:** 21 oder höher
- **Build-Tool:** Maven (oder jedes kompatible Tool)

## Installation

Fügen Sie folgendes zu Ihrer `pom.xml` hinzu:

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>JavaSqlBuilder2</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Technische Nutzung

### 1. Initialisierung des Builders
Jede Abfrage beginnt mit einem `SelectBuilder`, der einen `SqlDialect` benötigt.

```java
import sqlbuilder.SelectBuilder;
import sqlbuilder.dialects.SqlDialect;

SelectBuilder builder = new SelectBuilder(new SqlDialect.PostgresDialect());
```

### 2. Einfache Select-Abfrage
Definieren Sie Spalten und die Quell-Tabelle. Wenn keine Spalten angegeben werden, wird standardmäßig `*` verwendet.

```java
Query query = builder
    .select("id", "username", "email")
    .from("users")
    .build();

System.out.println(query.getStatement()); 
// Ausgabe: SELECT "id", "username", "email" FROM users
```

### 3. Komplexe Where-Klauseln
Nutzen Sie die `Expression`-Utility, um Bedingungen zu erstellen. Bedingungen können mit `.and()` oder `.or()` verkettet werden.

```java
import sqlbuilder.expressions.Expression;

builder.select("name")
       .from("employees")
       .where(Expression.eq("department", "IT")
           .and().gt("salary", 50000)
           .or().eq("role", "Admin"));
```

### 4. Joins
Führen Sie Joins aus, indem Sie die Ziel-Tabelle und die Join-Bedingung angeben.

```java
builder.select("u.name", "o.order_date")
       .from("users", "u")
       .join("orders", "o", Expression.eq("u.id", Expression.column("o.user_id")));
```

### 5. Sortierung und Paging
Fügen Sie Sortierung und Paginierung einfach hinzu.

```java
builder.from("products")
       .orderBy("price").desc()
       .limit(10)
       .offset(20);
```

## Architektur-Überblick

- **`SelectBuilder`**: Die Kern-Komponente, die den Aufbau der Abfrage steuert.
- **`SqlDialect`**: Ein Interface, das es dem Builder ermöglicht, sich an verschiedene SQL-Syntaxen anzupassen (z. B. Paging-Logik).
- **`Condition` & `Expression`**: Ein robustes System zum Aufbau logischer SQL-Ausdrücke. `Expression` fungiert als Factory für verschiedene Vergleichsoperatoren.
- **`Operand`**: Repräsentiert die Bausteine von Bedingungen (Spalten, Werte oder Parameter).
- **`Query`**: Das fertige Produkt, das den generierten SQL-String und die Parameter für Prepared Statements enthält.

## Mitwirken

1. Implementieren Sie das `SqlDialect`-Interface für neue Datenbank-Engines.
2. Erweitern Sie `Expression` um komplexere SQL-Funktionen (z. B. `IN`, `BETWEEN`).
3. Verbessern Sie die `Query.execute()` Methode zur Integration mit JDBC.

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die LICENSE-Datei für Details.

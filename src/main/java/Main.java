import sqlbuilder.Query;
import sqlbuilder.SelectBuilder;
import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.expressions.Expression;

import static sqlbuilder.expressions.Expression.*;

public class Main {
    public static void main(String[] args) {
        SqlDialect.OracleDialect dialect = new SqlDialect.OracleDialect();
        SelectBuilder builder = new SelectBuilder(dialect);
        SelectBuilder subQuery = new SelectBuilder(dialect);

        subQuery
                .select("col1")
                .from("table")
                .where(eq("fuenf", 5)
                        .or().eq("fuenf", 6))
                ;

        Query query = builder
                //.select("A")
                .from("TABLE")
                .where(Expression.eq("C", 3).and().neq("C", 4))
                .where(Expression.gt("E", "ABC").and().leq("F", Expression.param("paramF")))
                .where(Expression.eq("CAST", Expression._case().whenThen(Expression.isNull("HAUS"), Expression.column("AUTO"))._else("HAUS")))
                .where(Expression.in("I", 1,2,3).or().like("J", Expression.column("K")))
                .where(Expression.in("BASE",
                        subQuery
                ))
                .build();

        System.out.println("prepared statement: " + query.getStatement());
        System.out.println("populated statement: " + query.getPopulatedStatement());
    }
}

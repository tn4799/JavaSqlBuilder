import sqlbuilder.Query;
import sqlbuilder.SelectBuilder;
import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.expressions.Expression;

public class Main {
    public static void main(String[] args) {
        SelectBuilder builder = new SelectBuilder(new SqlDialect.OracleDialect());

        Query query = builder
                .select("A")
                .from("TABLE")
                .where(Expression.eq("C", 3).and().neq("C", 4))
                .where(Expression.gt("E", "ABC"))
                .build();

        System.out.println(query.getStatement());
    }
}

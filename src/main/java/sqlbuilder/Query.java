package sqlbuilder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Query {
    private final String STATEMENT;
    private final List<Object> parameters = new ArrayList<>();

    public Query(String statement) {
        this.STATEMENT = statement;
    }

    public void addParameter(Object parameter) {
        parameters.add(parameter);
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public String getStatement() {
        return STATEMENT;
    }

    public ResultSet execute() {
        //TODO: implement execution of sql statement
        return null;
    }
}

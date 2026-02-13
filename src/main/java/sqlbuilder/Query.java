package sqlbuilder;

import java.sql.ResultSet;
import java.util.Map;

public class Query {
    private final String STATEMENT;
    private Map<Integer, Object> parameters;

    public Query(String statement) {
        this.STATEMENT = statement;
    }

    public void setParameter(int idx, Object parameter) {
        if(!parameters.containsKey(idx)) {
            throw new IllegalArgumentException("Unknown parameter index cannot be set.");
        }

        parameters.put(idx, parameter);
    }

    public String getStatement() {
        return STATEMENT;
    }

    public ResultSet execute() {
        //TODO: implement execution of sql statement
        return null;
    }
}

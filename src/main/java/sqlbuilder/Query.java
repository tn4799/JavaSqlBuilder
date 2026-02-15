package sqlbuilder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Query {
    private final String statement;
    private final List<Object> parameters = new ArrayList<>();

    public Query(String statement, List<Object> parameters) {
        this.statement = statement;
        this.parameters.addAll(parameters);
    }

    public void addParameter(Object parameter) {
        parameters.add(parameter);
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public String getStatement() {
        return statement;
    }

    public String getPopulatedStatement() {
        StringBuffer populatedStatement = new StringBuffer();
        Matcher matcher = Pattern.compile("\\?").matcher(statement);

        for(Object param : parameters) {
            if(param == null) {
                // move the matcher forward to ignore prepared statement parameter
                matcher.find();
                continue;
            }
            String parameter = param instanceof String ? "'%s'".formatted(param) : param.toString();
            replacePreparedParameter(matcher, populatedStatement, parameter);
        }

        matcher.appendTail(populatedStatement);
        return populatedStatement.toString();
    }

    private void replacePreparedParameter(Matcher matcher, StringBuffer buffer, String replacement) {
        if(!matcher.find()) {
            return;
        }

        matcher.appendReplacement(buffer, replacement);
    }

    public ResultSet execute() {
        //TODO: implement execution of sql statement
        return null;
    }
}

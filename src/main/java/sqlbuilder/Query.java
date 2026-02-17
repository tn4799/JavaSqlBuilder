package sqlbuilder;

import sqlbuilder.dialects.SqlDialect;
import sqlbuilder.expressions.Operand;

import java.lang.reflect.Parameter;
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

    public void setParameter(String parameterKey, String value) {
        Operand.Parameter.Param parameter = getParameterForKey(parameterKey);
        parameter.setValue(value);
    }

    public void setParameter(String parameterKey, Number value) {
        Operand.Parameter.Param parameter = getParameterForKey(parameterKey);
        parameter.setValue(value);
    }

    public void setParameter(String parameterKey, boolean value) {
        Operand.Parameter.Param parameter = getParameterForKey(parameterKey);
        parameter.setValue(value);
    }

    private Operand.Parameter.Param getParameterForKey(String parameterKey) {
        return parameters.stream()
                .filter(param -> param instanceof Operand.Parameter.Param)
                .map(param -> (Operand.Parameter.Param) param)
                .filter(param -> parameterKey.equals(param.getNameKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Parameter with key '%s' is not defined!".formatted(parameterKey)));
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public String getStatement() {
        return statement;
    }

    public String getPopulatedStatement(SqlDialect dialect) {
        StringBuffer populatedStatement = new StringBuffer();
        Matcher matcher = Pattern.compile("\\?").matcher(statement);

        for(Object param : parameters) {
            String parameterValue;
            if(param instanceof Operand.Parameter.Param parameter) {
                if(parameter.getValue() == null) {
                    // move the matcher forward to ignore prepared statement parameter
                    matcher.find();
                    continue;
                }

                parameterValue = parameter.toSqlValue(dialect);
            } else {
                parameterValue = param instanceof String ? "'%s'".formatted(param) : param.toString();
            }
            replacePreparedParameter(matcher, populatedStatement, parameterValue);
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

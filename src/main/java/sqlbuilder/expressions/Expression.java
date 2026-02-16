package sqlbuilder.expressions;

import sqlbuilder.SelectBuilder;

import java.util.List;

public class Expression {
    public static Operand column(String columnName) {
        return new ColumnOperand(columnName);
    }

    public static Operand column(String alias, String columnName) {
        if(alias == null || alias.isBlank()) {
            return column(columnName);
        }

        if(columnName.contains(".")) {
            columnName = columnName.substring(columnName.lastIndexOf('.') + 1);
        }

        return new ColumnOperand(alias + "." + columnName);
    }

    public static Operand value(Object value) {
        return new ValueOperand(value);
    }

    public static Operand param(String nameKey) {
        return new Operand.Parameter(nameKey);
    }

    public static Operand.CaseBuilder _case() {
        return new Operand.CaseBuilder();
    }

    public static Condition eq(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, "=", right);
    }

    public static Condition neq(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, "<>", right);
    }

    public static Condition lt(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, "<", right);
    }

    public static Condition leq(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, "<=", right);
    }

    public static Condition gt(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, ">", right);
    }

    public static Condition geq(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, ">=", right);
    }

    public static Condition like(String column, Object comparisonValue) {
        Operand left = new ColumnOperand(column);
        Operand right = getCorrectOperand(comparisonValue);
        return new ComparisionCondition(left, "LIKE", right);
    }

    public static Condition isNull(String column) {
        return new NullCondition(column);
    }

    public static Condition isNotNull(String column) {
        return new NotNullCondition(column);
    }

    public static Condition not(Condition condition) {
        return new NotCondition(condition);
    }

    public static Condition in(String column, List<Object> values) {
        return new InCondition(column, values);
    }

    public static Condition in(String column, Object... values) {
        return in(column, List.of(values));
    }

    public static Condition in(String column, SelectBuilder subQuery) {
        return new InCondition(column, subQuery);
    }

    public static Condition notIn(String column, List<Object> values) {
        return new NotInCondition(column, values);
    }

    public static Condition notIn(String column, Object... values) {
        return notIn(column, List.of(values));
    }

    public static Condition notIn(String column, SelectBuilder subQuery) {
        return new NotInCondition(column, subQuery);
    }

    public static Condition exists(SelectBuilder subQuery) {
        return new ExistsCondition(subQuery);
    }

    public static Condition notExists(SelectBuilder subQuery) {
        return new NotExistsCondition(subQuery);
    }

    public static Condition between(String column, Object lowerBound, Object upperBound) {
        return new BetweenCondition(column(column), value(lowerBound), value(upperBound));
    }

    public static Condition notBetween(String column, Object lowerBound, Object upperBound) {
        return not(between(column, lowerBound, upperBound));
    }

    public static Condition brackets(Condition expression) {
        //TODO: Implement bracket support
        return null;
    }

    private static Operand getCorrectOperand(Object comparisonValue) {
        return comparisonValue instanceof Operand ?
                (Operand) comparisonValue : new ValueOperand(comparisonValue);
    }
}

package sqlbuilder.expressions;

public class Expression {
    public static Operand column(String columnName) {
        return new ColumnOperand(columnName);
    }

    public static Operand value(Object value) {
        return new ValueOperand(value);
    }

    public static Operand param(int index) {
        return new Parameter(index);
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

    public static Condition brackets(Condition expression) {
        //TODO: Implement bracket support
        return null;
    }

    private static Operand getCorrectOperand(Object comparisonValue) {
        return comparisonValue instanceof Operand ?
                (Operand) comparisonValue : new ValueOperand(comparisonValue);
    }
}

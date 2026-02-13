package sqlbuilder.expressions;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public interface Condition {
    public String toSql();
    List<Object> getParameters();

    default ConditionChain and() {
        return new ConditionChain(this, "AND");
    }

    default ConditionChain or() {
        return new ConditionChain(this, "OR");
    }

    class ConditionChain {
        private final Condition leftCondition;
        private final String chainingOperator;

        public ConditionChain(Condition leftCondition, String chainingOperator) {
            this.leftCondition = leftCondition;
            this.chainingOperator = chainingOperator;
        }

        public Condition eq(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.eq(column, value));
        }

        public Condition neq(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.neq(column, value));
        }

        public Condition lt(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.lt(column, value));
        }

        public Condition leq(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.leq(column, value));
        }

        public Condition gt(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.gt(column, value));
        }

        public Condition geq(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.geq(column, value));
        }

        public Condition like(String column, Object value) {
            return new CompositeCondition(chainingOperator, leftCondition, Expression.like(column, value));
        }
    }
}

class ComparisionCondition implements Condition {
    private final Operand column;
    private final String operator;
    private final Operand comparisonValue;

    public ComparisionCondition(Operand column, String operator, Operand comparisonValue) {
        this.column = column;
        this.operator = operator;
        this.comparisonValue = comparisonValue;
    }

    @Override
    public String toSql() {
        return new StringJoiner(" ")
                .add(column.toSql())
                .add(operator)
                .add(comparisonValue.toSql())
                .toString();
    }

    @Override
    public List<Object> getParameters() {
        return List.of();
    }
}

class SimpleCondition implements Condition {
    private final String column;
    private final String comparisonOperator;
    private final Operand comparisonValue;

    public SimpleCondition(String column, String operator, Operand value) {
        this.column = column;
        this.comparisonOperator = operator;
        this.comparisonValue = value;
    }

    @Override
    public String toSql() {
        return new StringJoiner(" ")
                .add(column)
                .add(comparisonOperator)
                .add("?")
                .toString();
    }

    @Override
    public List<Object> getParameters() {
        return Collections.singletonList(comparisonValue);
    }
}

class CompositeCondition implements Condition {
    private String type;
    private List<Condition> conditions;

    public CompositeCondition(String type, Condition... conditions) {
        this.type = type;
        this.conditions = List.of(conditions);
    }

    @Override
    public String toSql() {
        if(conditions.isEmpty()) {
            return "";
        }

        return conditions.stream()
                .map(Condition::toSql)
                .collect(Collectors.joining(" " + type + " "));
    }

    @Override
    public List<Object> getParameters() {
        return conditions.stream()
                .flatMap(condition -> condition.getParameters().stream())
                .toList();
    }
}

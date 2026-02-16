package sqlbuilder.expressions;

import sqlbuilder.SelectBuilder;
import sqlbuilder.exceptions.ValueCannotBeEmptyException;

import java.util.ArrayList;
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
            return createCompositeCondition(Expression.eq(column, value));
        }

        public Condition neq(String column, Object value) {
            return createCompositeCondition(Expression.neq(column, value));
        }

        public Condition lt(String column, Object value) {
            return createCompositeCondition(Expression.lt(column, value));
        }

        public Condition leq(String column, Object value) {
            return createCompositeCondition(Expression.leq(column, value));
        }

        public Condition gt(String column, Object value) {
            return createCompositeCondition(Expression.gt(column, value));
        }

        public Condition geq(String column, Object value) {
            return createCompositeCondition(Expression.geq(column, value));
        }

        public Condition like(String column, Object value) {
            return createCompositeCondition(Expression.like(column, value));
        }

        public Condition isNull(String column) {
            return createCompositeCondition(Expression.isNull(column));
        }

        public Condition not(Condition condition) {
            return createCompositeCondition(Expression.not(condition));
        }

        public Condition in(String column, List<Object> values) {
            return createCompositeCondition(Expression.in(column, values));
        }

        public Condition in(String column, Object... values) {
            return in(column, List.of(values));
        }

        public Condition in(String column, SelectBuilder subQuery) {
            return createCompositeCondition(Expression.in(column, subQuery));
        }

        public Condition notIn(String column, List<Object> values) {
            return createCompositeCondition(Expression.notIn(column, values));
        }

        public Condition notIn(String column, Object... values) {
            return notIn(column, List.of(values));
        }

        public Condition notIn(String column, SelectBuilder subQuery) {
            return createCompositeCondition(Expression.notIn(column, subQuery));
        }

        public Condition exists(SelectBuilder subQuery) {
            return createCompositeCondition(Expression.exists(subQuery));
        }

        public Condition notExists(SelectBuilder subQuery) {
            return createCompositeCondition(Expression.notExists(subQuery));
        }

        public Condition between(String column, Object lowerBound, Object upperBound) {
            return createCompositeCondition(Expression.between(column, lowerBound, upperBound));
        }

        public Condition notBetween(String column, Object lowerBound, Object upperBound) {
            return createCompositeCondition(Expression.notBetween(column, lowerBound, upperBound));
        }

        private Condition createCompositeCondition(Condition expression) {
            return new CompositeCondition(chainingOperator, leftCondition, expression);
        }
    }

    class CompositeCondition implements Condition {
        private final String type;
        private final List<Condition> conditions;

        public CompositeCondition(String type, Condition... conditions) {
            this.type = type;
            this.conditions = List.of(conditions);
        }

        public CompositeCondition(String type, List<Condition> conditions) {
            this.type = type;
            this.conditions = conditions;
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
        List<Object> params = new ArrayList<>();
        column.addParameters(params);
        comparisonValue.addParameters(params);
        return params;
    }
}

class NullCondition implements Condition {
    protected final String column;

    public NullCondition(String column) {
        this.column = column;
    }

    @Override
    public String toSql() {
        return column + " IS NULL";
    }

    @Override
    public List<Object> getParameters() {
        // IS NULL has no parameters
        return List.of();
    }
}

class NotNullCondition extends NullCondition {
    public NotNullCondition(String column) {
        super(column);
    }

    @Override
    public String toSql() {
        return column + "IS NOT NULL";
    }
}

class NotCondition implements Condition {
    private final Condition condition;

    public NotCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public String toSql() {
        return "NOT " + condition.toSql();
    }

    @Override
    public List<Object> getParameters() {
        return condition.getParameters();
    }
}

class InCondition implements Condition {
    protected final String column;
    protected final List<Object> values;
    protected final SelectBuilder subQuery;
    protected final String operator;

    public InCondition(String column, List<Object> values) {
        this(column, values, null, "IN");
    }

    public InCondition(String column, Object... values) {
        this(column, List.of(values));
    }

    public InCondition(String column, SelectBuilder subQuery) {
        this(column, null, subQuery, "IN");
    }

    protected InCondition(String column, List<Object> values, SelectBuilder subQuery, String operator) {
        this.column = column;
        this.values = values;
        this.subQuery = subQuery;
        this.operator = operator;
    }

    @Override
    public String toSql() {
        StringJoiner sql = new StringJoiner(" ")
                .add(column)
                .add(operator)
                .add("(");
        if(values != null) {
            if(values.isEmpty()) {
                throw new ValueCannotBeEmptyException("IN-values");
            }

            sql.add(values.stream().map(v -> "?").collect(Collectors.joining(", ")));
        // no null check for sub query needed because only either values or subQuery can be null because of the constructor
        } else {
            sql.add(subQuery.build().getStatement());
        }
        sql.add(")");
        return sql.toString();
    }

    @Override
    public List<Object> getParameters() {
        if(values != null) {
            return values;
        }
        return subQuery.build().getParameters();
    }
}

class NotInCondition extends InCondition {
    public NotInCondition(String column, SelectBuilder subQuery) {
        this(column, null, subQuery);
    }

    public NotInCondition(String column, List<Object> values) {
        this(column, values, null);
    }

    public NotInCondition(String column, Object... values) {
        this(column, List.of(values));
    }

    private NotInCondition(String column, List<Object> values, SelectBuilder subQuery) {
        super(column, values, subQuery, "NOT IN");
    }
}

class ExistsCondition implements Condition {
    protected final SelectBuilder subQuery;
    protected final String operator;

    public ExistsCondition(SelectBuilder subQuery) {
        this(subQuery, "EXISTS");
    }

    protected ExistsCondition(SelectBuilder subQuery, String operator) {
        this.subQuery = subQuery;
        this.operator = operator;
    }

    @Override
    public String toSql() {
        StringJoiner exists = new StringJoiner(" ")
                .add(operator)
                .add("(")
                .add(subQuery.build().getStatement())
                .add(")");
        return exists.toString();
    }

    @Override
    public List<Object> getParameters() {
        return subQuery.build().getParameters();
    }
}

class NotExistsCondition extends ExistsCondition {
    public NotExistsCondition(SelectBuilder subQuery) {
        super(subQuery, "NOT EXISTS");
    }
}

class BetweenCondition implements Condition {
    private final Operand column;
    private final Operand lowerBound;
    private final Operand upperBound;

    public BetweenCondition(Operand column, Operand lowerBound, Operand upperBound) {
        this.column = column;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public String toSql() {
        return new StringJoiner(" ")
                .add(column.toSql())
                .add("BETWEEN")
                .add(lowerBound.toSql())
                .add("AND")
                .add(upperBound.toSql())
                .toString();
    }

    @Override
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        lowerBound.addParameters(params);
        upperBound.addParameters(params);
        return params;
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
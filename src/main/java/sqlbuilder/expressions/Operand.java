package sqlbuilder.expressions;

import sqlbuilder.SelectBuilder;
import sqlbuilder.exceptions.DuplicateKeyException;

import java.util.*;
import java.util.stream.Collectors;

public interface Operand {
    public String toSql();
    public void addParameters(List<Object> parameters);

    /**
     * Returns the passed object as an Operand object. If the object isn't an Operand object then
     * it is returned as a {@link ValueOperand}
     *
     * @param operand The object that is converted
     * @return The converted object
     */
    private static Operand convertToOperand(Object operand) {
        return operand instanceof Operand ? (Operand) operand : new ValueOperand(operand);
    }

    public class CaseBuilder implements Operand {
        private final Map<Condition, Operand> whenThenCases = new LinkedHashMap<>();
        private Operand _else;

        public CaseBuilder whenThen(Condition when, Object then) {
            whenThenCases.put(when, Operand.convertToOperand(then));
            return this;
        }

        public CaseBuilder _else(Object _else) {
            this._else = Operand.convertToOperand(_else);
            return this;
        }

        @Override
        public String toSql() {
            if(whenThenCases.isEmpty()) {
                throw new IllegalStateException("At least one When-Then combination is needed.");
            }

            StringJoiner _case = new StringJoiner(" ")
                    .add("CASE");
            whenThenCases.forEach((condition, operand) -> _case.add("WHEN")
                    .add(condition.toSql())
                    .add("THEN")
                    .add(operand.toSql()));
            if(_else != null) {
                _case.add("ELSE")
                        .add(_else.toSql());
            }
            _case.add("END");
            return _case.toString();
        }

        @Override
        public void addParameters(List<Object> parameters) {
            for (Map.Entry<Condition, Operand> entry : whenThenCases.entrySet()) {
                parameters.addAll(entry.getKey().getParameters());
                entry.getValue().addParameters(parameters);
            }
            if (_else != null) {
                _else.addParameters(parameters);
            }
        }
    }

    public class SubQuery implements Operand {
        private final SelectBuilder subQuery;

        public SubQuery(SelectBuilder subQuery) {
            this.subQuery = subQuery;
        }

        @Override
        public String toSql() {
            return "( %s )".formatted(subQuery.build().getStatement());
        }

        @Override
        public void addParameters(List<Object> parameters) {
            parameters.addAll(subQuery.build().getParameters());
        }
    }

    public class Parameter implements Operand {
        private static final Set<String> registeredKeys = new HashSet<>();
        private final String nameKey;

        public Parameter(String nameKey) {
            if(registeredKeys.contains(nameKey)) {
                throw new DuplicateKeyException(nameKey);
            }
            this.nameKey = nameKey;
            registeredKeys.add(nameKey);
        }

        public String getNameKey() {
            return nameKey;
        }

        @Override
        public String toSql() {
            return "?";
        }

        @Override
        public void addParameters(List<Object> parameters) {
            parameters.add(new Param(nameKey, parameters.size()));
        }

        public class Param {
            private final String nameKey;
            private final int internalKey;
            private ValueOperand value = null;

            private Param(String nameKey, int internalKey) {
                this.nameKey = nameKey;
                this.internalKey = internalKey;
            }

            public String getNameKey() {
                return nameKey;
            }

            public int getInternalKey() {
                return internalKey;
            }

            public void setValue(String value) {
                this.value = new ValueOperand(value);
            }

            public void setValue(boolean value) {
                this.value = new ValueOperand(value);
            }

            public void setValue(Number value) {
                this.value = new ValueOperand(value);
            }

            public Operand getValue() {
                return this.value;
            }

            @Override
            public String toString() {
                return this.value.toSql();
            }
        }
    }
}

class ValueOperand implements Operand {
    private final Object value;

    public ValueOperand(Object value) {
        this.value = value;
    }

    @Override
    public String toSql() {
        return "?";
    }

    @Override
    public void addParameters(List<Object> parameters) {
        parameters.add(value);
    }
}

class ColumnOperand implements Operand {
    private final String columnName;

    public ColumnOperand(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toSql() {
        return columnName;
    }

    @Override
    public void addParameters(List<Object> parameters) {
        // do nothing
    }
}

class AnyAllOperand implements Operand {
    private final String operand;
    private final List<Operand> values;
    private final SelectBuilder subQuery;

    protected AnyAllOperand(String operand, List<Operand> values) {
        this(operand, values, null);
    }

    protected AnyAllOperand(String operand, SelectBuilder subQuery) {
        this(operand, null, subQuery);
    }

    private AnyAllOperand(String operand, List<Operand> values, SelectBuilder subQuery) {
        this.operand = operand;
        this.values = values;
        this.subQuery = subQuery;
    }

    @Override
    public String toSql() {
        StringJoiner sql = new StringJoiner(" ")
                .add(operand)
                .add("(");
        if(values != null) {
          sql.add(values.stream().map(v -> "?").collect(Collectors.joining(", ")));
        } else {
            sql.add(subQuery.build().getStatement());
        }
        sql.add(")");
        return sql.toString();
    }

    @Override
    public void addParameters(List<Object> parameters) {
        if(values != null) {
            parameters.addAll(values);
        } else {
            parameters.addAll(subQuery.build().getParameters());
        }
    }

    public String getOperand() {
        return operand;
    }
}

class AnyOperand extends AnyAllOperand {
    AnyOperand(List<Operand> values) {
        super("ANY", values);
    }

    AnyOperand(SelectBuilder subQuery) {
        super("ANY", subQuery);
    }
}

class AllOperand extends AnyAllOperand {
    AllOperand(List<Operand> values) {
        super("ALL", values);
    }

    AllOperand(SelectBuilder subQuery) {
        super("ALL", subQuery);
    }
}
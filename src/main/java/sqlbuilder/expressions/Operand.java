package sqlbuilder.expressions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

class Parameter implements Operand {
    private final int index;

    public Parameter(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toSql() {
        return "?";
    }

    @Override
    public void addParameters(List<Object> parameters) {
        // do nothing
    }
}
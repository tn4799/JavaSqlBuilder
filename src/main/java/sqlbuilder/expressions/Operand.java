package sqlbuilder.expressions;

import java.util.List;

public interface Operand {
    public String toSql();
    public void addParameters(List<Object> parameters);
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

package sqlbuilder.exceptions;

public class ValueCannotBeEmptyException extends IllegalArgumentException {
    public ValueCannotBeEmptyException(String valueName) {
        super(valueName + " cannot be empty");
    }
}

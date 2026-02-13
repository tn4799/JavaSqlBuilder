package sqlbuilder.exceptions;

public class ValueCannotBeNullException extends IllegalArgumentException {
    public ValueCannotBeNullException(String valueName) {
        super(valueName + " cannot be null");
    }
}

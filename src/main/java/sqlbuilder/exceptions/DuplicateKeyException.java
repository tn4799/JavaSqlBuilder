package sqlbuilder.exceptions;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException(String key) {
        super("Key '%s' is already registered. Keys have to be unique".formatted(key));
    }
}

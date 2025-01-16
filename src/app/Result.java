package app;

/**
 * Generic Either monad for returning a result or an error.
 * Similar to Rust's app.Result or C++'s std::expected.
 */
public class Result<T> {
    private final T result;
    private final String error;

    private Result(T result, String error) {
        this.result = result;
        this.error = error;
    }

    public static <T> Result<T> ok(T result) {
        return new Result<>(result, null);
    }

    public static <T> Result<T> error(String error, Object... args) {
        return new Result<>(null, String.format(error, args));
    }

    public boolean isOk() {
        return result != null;
    }

    public T getResult() {
        assert isOk() : "getResult() called but result is an error. Always check isOk() first!";
        return result;
    }

    public String getError() {
        assert !isOk() : "getError() called but result is not an error. Always check isOk() first!";
        return error;
    }
}

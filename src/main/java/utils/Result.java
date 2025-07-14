package utils;

/**
 * Generic wrapper for service call outcomes.
 */
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String message;

    /** Construct a result. */
    private Result(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /** Create a success result containing data. */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    /** Create a failure result with an error message. */
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message);
    }

    /** @return whether the call succeeded */
    public boolean isSuccess() {
        return success;
    }

    /** @return payload or null */
    public T getData() {
        return data;
    }

    /** @return associated message or null */
    public String getMessage() {
        return message;
    }
}

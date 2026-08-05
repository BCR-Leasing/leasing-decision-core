package ro.bcrleasing.leasingdecisioncore.common.exception;

public class InvalidSubjectDataException extends RuntimeException {

    public InvalidSubjectDataException(String message) {
        super(message);
    }

    public InvalidSubjectDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

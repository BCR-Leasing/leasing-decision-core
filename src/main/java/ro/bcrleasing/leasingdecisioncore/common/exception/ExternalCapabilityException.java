package ro.bcrleasing.leasingdecisioncore.common.exception;

public class ExternalCapabilityException extends RuntimeException {

    private final String capability;

    public ExternalCapabilityException(String capability, String message) {
        super(message);
        this.capability = capability;
    }

    public ExternalCapabilityException(String capability, String message, Throwable cause) {
        super(message, cause);
        this.capability = capability;
    }

    public String getCapability() {
        return capability;
    }
}

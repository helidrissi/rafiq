package ma.rafiq.application;

public interface ProcessedMessageRepository {
    boolean markIfNew(String messageSid);
}

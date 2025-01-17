import java.time.LocalDateTime;

class ShortLink {
    private final String longUrl;
    private int maxTransitions;
    private int transitionCount;
    private LocalDateTime expiration;

    public ShortLink(String longUrl, int maxTransitions, LocalDateTime expiration) {
        this.longUrl = longUrl;
        this.maxTransitions = maxTransitions;
        this.transitionCount = 0;
        this.expiration = expiration;
    }

    public boolean incrementTransitionCount() {
        if (transitionCount < maxTransitions) {
            transitionCount++;
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiration);
    }

    public void setMaxTransitions(int maxTransitions) {
        this.maxTransitions = maxTransitions;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public String getLongUrl() {
        return longUrl;
    }
}

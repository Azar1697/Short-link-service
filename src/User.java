import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class User {
    private final String username;
    private final String uuid;
    private final Map<String, ShortLink> links = new HashMap<>();

    public User(String username) {
        this.username = username;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public String createShortLink(String longUrl, int maxTransitions, int lifetimeHours) {

        String shortUrl = ShortLinkService.generateShortLink(longUrl);
        if (shortUrl == null) {
            System.err.println("Failed to generate short link for: " + longUrl);
            return null;
        }

        LocalDateTime expiration = LocalDateTime.now().plusHours(lifetimeHours);
        links.put(shortUrl, new ShortLink(longUrl, maxTransitions, expiration));
        return shortUrl;
    }

    public boolean editShortLink(String shortUrl, int newMaxTransitions, int newLifetime) {
        ShortLink link = links.get(shortUrl);
        if (link != null && !link.isExpired()) {
            link.setMaxTransitions(newMaxTransitions);
            link.setExpiration(LocalDateTime.now().plusHours(newLifetime));
            return true;
        }
        return false;
    }

    public boolean deleteShortLink(String shortUrl) {
        return links.remove(shortUrl) != null;
    }

    public String getOriginalUrl(String shortUrl) {
        ShortLink link = links.get(shortUrl);
        if (link != null && !link.isExpired() && link.incrementTransitionCount()) {
            return link.getLongUrl();
        }
        return null;
    }
}

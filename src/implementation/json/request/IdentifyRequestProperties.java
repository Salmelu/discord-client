package cz.salmelu.discord.implementation.json.request;

public class IdentifyRequestProperties {
    private String $os = System.getProperty("os.name");
    private String $browser;
    private String $device;
    private String $referrer = "";
    private String $referringDomain = "";

    public String get$os() {
        return $os;
    }

    public void set$os(String $os) {
        this.$os = $os;
    }

    public String get$browser() {
        return $browser;
    }

    public void set$browser(String $browser) {
        this.$browser = $browser;
    }

    public String get$device() {
        return $device;
    }

    public void set$device(String $device) {
        this.$device = $device;
    }

    public String get$referrer() {
        return $referrer;
    }

    public void set$referrer(String $referrer) {
        this.$referrer = $referrer;
    }

    public String get$referringDomain() {
        return $referringDomain;
    }

    public void set$referringDomain(String $referringDomain) {
        this.$referringDomain = $referringDomain;
    }
}

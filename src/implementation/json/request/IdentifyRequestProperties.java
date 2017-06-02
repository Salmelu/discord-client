package cz.salmelu.discord.implementation.json.request;

public class IdentifyRequestProperties {
    private String $os;
    private String $browser;
    private String $name = System.getProperty("os.name");
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

    public String get$name() {
        return $name;
    }

    public void set$name(String $name) {
        this.$name = $name;
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

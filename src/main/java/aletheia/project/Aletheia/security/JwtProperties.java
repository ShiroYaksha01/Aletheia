package aletheia.project.Aletheia.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// register class jwtproperties
@Component
// piont to application properties and take prefix app.jwt
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long exp;
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
    public long getExp() {
        return exp;
    }
    public void setExp(long exp) {
        this.exp = exp;
    }
}

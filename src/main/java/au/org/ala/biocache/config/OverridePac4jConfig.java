package au.org.ala.biocache.config;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class OverridePac4jConfig {


    // Override bean from ala-security to include cookieMatcher, should enforce authentication when the cookie is present
    @Bean
    @Primary
    public Config overridePac4jConfig(List<Client> clients, SessionStore sessionStore, WebContextFactory webContextFactory) {
        Config config = new Config(clients);

        config.setSessionStore(sessionStore);
        config.setWebContextFactory(webContextFactory);
        config.addMatcher("ALA_COOKIE_MATCHER",
                (ctx, ss) -> {
                    return ctx.getRequestCookies().stream().anyMatch(cookie -> cookie.getName().equals(authCookieName));
                });
        return config;
    }
}

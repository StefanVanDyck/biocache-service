package au.org.ala.biocache.config;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.filter.SecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.FilterChainProxy;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

import static org.pac4j.core.authorization.authorizer.DefaultAuthorizers.IS_AUTHENTICATED;
import static org.pac4j.core.util.Pac4jConstants.DEFAULT_CLIENT;

@Configuration
public class CustomPac4jConfig {

    @Value("${security.core.authCookieName:ALA_AUTH}")
    String authCookieName;

    @Bean
    @ConditionalOnProperty(prefix= "security.oidc", name="enabled")
    FilterRegistrationBean pac4jOptionalFilter(Config pac4jConfig) {

        pac4jConfig.addMatcher("ALA_COOKIE_MATCHER",
                (ctx, ss) -> {
                    return ctx.getRequestCookies().stream().anyMatch(cookie -> cookie.getName().equals(authCookieName));
                });


        // This filter will apply the optional auth filter patterns - will only SSO if a cookie is present
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setName("Pac4j Optional Security Filter");
        SecurityFilter securityFilter = new SecurityFilter(pac4jConfig,
                DEFAULT_CLIENT,
                IS_AUTHENTICATED, "ALA_COOKIE_MATCHER");
        frb.setFilter(securityFilter);
        frb.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        frb.setOrder(10);
        frb.setUrlPatterns(List.of("*"));
        frb.setEnabled(true);
        frb.setAsyncSupported(true);

        return frb;
    }
}

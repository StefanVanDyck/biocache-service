package au.org.ala.biocache.config;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.callback.CallbackUrlResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.pac4j.core.http.url.DefaultUrlResolver;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.jee.filter.CallbackFilter;
import org.pac4j.jee.filter.SecurityFilter;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

import static org.pac4j.core.authorization.authorizer.DefaultAuthorizers.IS_AUTHENTICATED;

@Configuration
public class CustomPac4jConfig {

    @Value("${security.core.authCookieName:ALA_AUTH}")
    String authCookieName;


    @Bean
    @ConditionalOnProperty(prefix = "security.oidc", name = "enabled")
    OidcClient oidcClient(OidcConfiguration oidcConfiguration) {
        OidcClient client = new OidcClient(oidcConfiguration);
        client.setCallbackUrl("callback");


        client.setCallbackUrlResolver(new QueryParameterCallbackUrlResolver() {
            final UrlResolver completingUrlResolver = new DefaultUrlResolver(true);

            @Override
            public String compute(final UrlResolver urlResolver, final String url, final String clientName, final WebContext context) {
                return super.compute(completingUrlResolver, url, clientName, context);
            }

        });
        return client;
    }

    @Bean
    @ConditionalOnProperty(prefix = "security.oidc", name = "enabled")
    FilterRegistrationBean<SecurityFilter> pac4jOptionalFilter(Config pac4jConfig) {

        pac4jConfig.addMatcher("ALA_COOKIE_MATCHER",
                (ctx, ss) -> ctx.getRequestCookies().stream().anyMatch(cookie -> cookie.getName().equals(authCookieName)));


        // This filter will apply the optional auth filter patterns - will only SSO if a cookie is present
        var frb = new FilterRegistrationBean<SecurityFilter>();
        frb.setName("Pac4j Optional Security Filter");
        SecurityFilter securityFilter = new SecurityFilter(pac4jConfig, "OidcClient", IS_AUTHENTICATED, "ALA_COOKIE_MATCHER");
        frb.setFilter(securityFilter);
        frb.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        frb.setOrder(10);
        frb.setUrlPatterns(List.of("*"));
        frb.setEnabled(true);
        frb.setAsyncSupported(true);

        return frb;
    }

    @ConditionalOnProperty(prefix = "security.oidc", name = "enabled")
    @Bean
    FilterRegistrationBean<CallbackFilter> pac4jCallbackFilter(Config pac4jConfig) {
        var frb = new FilterRegistrationBean<CallbackFilter>();
        frb.setName("Pac4j Callback Filter");
        CallbackFilter callbackFilter = new CallbackFilter(pac4jConfig, "/callback");
        callbackFilter.setDefaultClient("OidcClient");
        frb.setFilter(callbackFilter);
        frb.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        frb.setOrder(9);
        frb.setUrlPatterns(List.of("/callback"));
        frb.setEnabled(true);
        frb.setAsyncSupported(true);
        return frb;
    }
}

package au.org.ala.biocache.config;

import au.org.ala.ws.security.AlaWebServiceAuthFilter;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;

@Configuration
@ComponentScan(basePackages = { "au.org.ala.ws.security" })
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AlaWebServiceAuthFilter alaWebServiceAuthFilter;

    @Value("${security.core.authCookieName:ALA_AUTH}")
    String authCookieName;

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.addFilterBefore(alaWebServiceAuthFilter, BasicAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers(
                        "/",
                        "/**"
                ).permitAll()
                .and().csrf().disable();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return  new GrantedAuthorityDefaults("");
    }


    // Override bean from ala-security to include cookieMatcher, should enforce authentication when the cookie is present
    @Bean
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

package com.example.demo.security;

import com.example.demo.security.csrf.SecurityCsrfFilter;
import com.example.demo.security.jwt.JwtAuthEntryPoint;
import com.example.demo.security.jwt.JwtAuthTokenFilter;
import com.example.demo.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] CSRF_IGNORE = {"/api/auth/**"};


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManagerBuilder authenticatedJwtTokenFilter;
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtAuthEntryPoint unauthorizedHandler;
    @Bean
    public JwtAuthTokenFilter authenticatedJwtTokenFilter() {
        return new JwtAuthTokenFilter();
    }
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .ignoringAntMatchers(CSRF_IGNORE)
                .csrfTokenRepository(csrfTokenRepository())
                .and()
                .addFilterAfter(new SecurityCsrfFilter(), CsrfFilter.class);
        http.cors().and().authorizeRequests()
                .antMatchers("/api/auth/**")
                .permitAll().anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(authenticatedJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName(SecurityCsrfFilter.CSRF_COOKIE_NAME);
        return repository;
    }
}

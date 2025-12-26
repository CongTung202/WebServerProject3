package k23cnt3.nguyencongtung.project3.config;

import k23cnt3.nguyencongtung.project3.service.NctUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class NctSecurityConfig {

    private final NctUserDetailsService nctUserDetailsService;
    private final AuthenticationSuccessHandler nctCustomAuthenticationSuccessHandler;

    @Autowired
    public NctSecurityConfig(NctUserDetailsService nctUserDetailsService, AuthenticationSuccessHandler nctCustomAuthenticationSuccessHandler) {
        this.nctUserDetailsService = nctUserDetailsService;
        this.nctCustomAuthenticationSuccessHandler = nctCustomAuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/payment/**","/api/zalopay/**")
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/",
                                "/home",
                                "/products/**",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                "/images/**",
                                "/webjars/**",
                                "/api/payment/**", //cho phep bo qua dag nhap tam thoi de test
                                "/api/zalopay/**",
                                "/giftbox",
                                "/error/**"

                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(nctCustomAuthenticationSuccessHandler)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("a-very-secret-key-for-nct-project") // A secret key for hashing the cookie
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                        .userDetailsService(nctUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me") // Delete remember-me cookie on logout
                        .permitAll()
                )
                .userDetailsService(nctUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // This is used because the project currently stores passwords in plain text.
        // For production, you should switch to a strong encoder like BCryptPasswordEncoder.
        return NoOpPasswordEncoder.getInstance();
    }
}

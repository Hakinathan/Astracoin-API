package be.helmo.astracoinapi;

import be.helmo.astracoinapi.security.JWTAuthorizationFilter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
public class AstracoinApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AstracoinApiApplication.class, args);
    }

    @EnableWebSecurity
    @Configuration
    @EnableScheduling
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
    class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

        @Override
        public void addCorsMappings(CorsRegistry registry) {

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.httpBasic().disable();
            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());
            http.csrf().disable()
                    .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests()
                    .antMatchers( "/login","/register","/currency/*", "/user/ranking","/callback/discord", "/contact","/marketdata/*",
                            "/v2/api-docs",
                            "/swagger-resources",
                            "/swagger-resources/**",
                            "/configuration/ui",
                            "/configuration/security",
                            "/swagger-ui.html",
                            "/webjars/**",
                            // -- Swagger UI v3 (OpenAPI)
                            "/v3/api-docs/**",
                            "/swagger-ui/**")
                    .permitAll()
                    .antMatchers("/admin/*").hasAnyAuthority("ADMIN")
                    .anyRequest().authenticated();
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {

        return new Jackson2ObjectMapperBuilder() {

            @Override
            public void configure(ObjectMapper objectMapper) {
                super.configure(objectMapper);
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
                objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            }
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST","PUT", "DELETE");
            }
        };
    }

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("astracoin-58583-firebase-adminsdk-s1aao-18e618d09e.json").getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions);
        return FirebaseMessaging.getInstance(app);
    }

}

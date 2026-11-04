package com.example.drinkshop.config;

import com.example.drinkshop.repository.PosRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import java.util.List;

/** 員工登入與 API 權限：目前使用 HTTP Basic，每次請求帶帳密，不是 JWT。 */
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    // 從員工資料表載入密碼雜湊、等級與在職狀態；明文密碼由 BCrypt 比對。
    @Bean UserDetailsService users(PosRepository repo) {
        return username -> {
            var staff = repo.staff(username).orElseThrow(() -> new UsernameNotFoundException("帳號不存在"));
            return User.withUsername(staff.username()).password(staff.passwordHash())
                .roles(staff.level()).disabled(!staff.active()).build();
        };
    }
    // 允許這兩個本機 Angular 網址跨來源呼叫；CORS 不等於登入授權。
    @Bean CorsConfigurationSource cors() {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        cors.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization","Content-Type"));
        cors.setAllowCredentials(false);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/api/**",cors);
        return source;
    }
    // STATELESS：後端不靠登入 Session；文件、health 與預檢免登入，其餘 /api 都需驗證。
    // 這版 API 由前端明確帶 Authorization，不使用 Cookie Session，CSRF 設定依此設計。
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable()).cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b -> b.authenticationEntryPoint((request,response,ex) -> {
                response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"請使用有效的員工帳號登入\"}");
            }))
            .exceptionHandling(e -> e.accessDeniedHandler((request,response,ex) -> {
                response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"沒有操作權限\"}");
            }))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()).build();
    }
}

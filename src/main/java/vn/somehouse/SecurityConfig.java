package vn.somehouse;
import com.fasterxml.jackson.databind.ObjectMapper;import jakarta.servlet.http.*;import org.springframework.context.annotation.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.core.userdetails.*;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.csrf.*;import org.springframework.http.HttpStatus;import org.springframework.http.HttpMethod;import org.springframework.core.annotation.Order;import java.io.IOException;import java.util.*;
@Configuration public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    @Bean UserDetailsService users(JdbcTemplate db){return email->{var users=db.query("SELECT email,password_hash,enabled FROM app_users WHERE email=?",(rs,n)->User.withUsername(rs.getString(1)).password(rs.getString(2)).disabled(!rs.getBoolean(3)).roles("ADMIN").build(),email.trim().toLowerCase(Locale.ROOT));if(users.isEmpty())throw new UsernameNotFoundException("Invalid credentials");return users.getFirst();};}
    @Bean @Order(1) SecurityFilterChain projectPresentation(HttpSecurity http)throws Exception{
        http.securityMatcher("/project-pages/oriva-bay-quy-nhon/**","/project-pages/altara-residences-quy-nhon/**").authorizeHttpRequests(a->a.requestMatchers(HttpMethod.GET,"/**").permitAll().requestMatchers(HttpMethod.HEAD,"/**").permitAll().anyRequest().denyAll());
        http.headers(h->h.frameOptions(f->f.sameOrigin()).contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' https: data: blob:; font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'")));
        return http.build();
    }
    @Bean @Order(2) SecurityFilterChain security(HttpSecurity http,ObjectMapper json)throws Exception{
        var handler=new CsrfTokenRequestAttributeHandler();
        http.csrf(c->c.csrfTokenRepository(new HttpSessionCsrfTokenRepository()).csrfTokenRequestHandler(handler));
        http.authorizeHttpRequests(a->a.requestMatchers("/api/v1/admin/**").hasRole("ADMIN").requestMatchers("/api/v1/auth/password").authenticated().anyRequest().permitAll());
        http.formLogin(f->f.loginProcessingUrl("/api/v1/auth/login").usernameParameter("email").passwordParameter("password").successHandler((q,s,a)->{s.setContentType("application/json");json.writeValue(s.getOutputStream(),Map.of("authenticated",true,"email",a.getName(),"role","ADMIN"));}).failureHandler((q,s,e)->write(json,q,s,401,"INVALID_CREDENTIALS","Email hoặc mật khẩu không đúng.")));
        http.exceptionHandling(e->e.authenticationEntryPoint((q,s,x)->write(json,q,s,401,"UNAUTHENTICATED","Vui lòng đăng nhập.")).accessDeniedHandler((q,s,x)->write(json,q,s,403,x instanceof CsrfException?"CSRF_INVALID":"FORBIDDEN",x instanceof CsrfException?"Phiên xác thực cần làm mới.":"Bạn không có quyền thực hiện thao tác này.")));
        http.logout(l->l.logoutUrl("/api/v1/auth/logout").invalidateHttpSession(true).deleteCookies("SESSION").logoutSuccessHandler((q,s,a)->{s.setContentType("application/json");json.writeValue(s.getOutputStream(),Map.of("authenticated",false));}));
        http.headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' https: data: blob:; font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'")));
        return http.build();}
    static void write(ObjectMapper json,HttpServletRequest q,HttpServletResponse s,int status,String code,String message)throws IOException{s.setStatus(status);s.setContentType("application/problem+json");json.writeValue(s.getOutputStream(),ApiErrors.problem(HttpStatus.valueOf(status),code,message,q));}
}

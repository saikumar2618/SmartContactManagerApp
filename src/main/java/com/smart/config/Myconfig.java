package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.Filter;

@Configuration
//@EnableWebSecurity   // Deprecated one
@EnableMethodSecurity             // Added in latest spring security version
public class Myconfig {

	@Bean
	public UserDetailsService getuserDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getuserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		
		return daoAuthenticationProvider;
	}
	
	
	//protected void configure(AuthenticationManagerBuilder auth) throws Exception{
	//	auth.authenticationProvider(authenticationProvider());
	//}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		   /* http.authorizeHttpRequests((requests) -> {
				try {
					requests
					        .requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/user/**").hasRole("USER")
					        .requestMatchers("/**").permitAll().and().formLogin().and().csrf().disable();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}); */
		    
		    http
            .authorizeHttpRequests((authz) -> {
				try {
					authz
					    .requestMatchers("/admin/**").hasRole("ADMIN")
					    .requestMatchers("/user/**").hasRole("USER")
					    .requestMatchers("/**").permitAll().and().formLogin()
					    .loginPage("/signin")  // we want signin page to get displayed while logging in. Else it will show the bydefault login page which is built by spring security
					    .and().csrf().disable();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		            

		    return http.build();
		  }
	
	
	
}

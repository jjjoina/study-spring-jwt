package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

import com.cos.jwt.config.auth.PrincipalDetailsService;
import com.cos.jwt.config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsFilter corsFilter;
	private final PrincipalDetailsService principalDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// authenticationManager
		AuthenticationManagerBuilder sharedObject = http.getSharedObject(AuthenticationManagerBuilder.class);
		sharedObject.userDetailsService(this.principalDetailsService);
		AuthenticationManager authenticationManager = sharedObject.build();
		http.authenticationManager(authenticationManager);

		// http.addFilterBefore(new MyFilter3(), BasicAuthenticationFilter.class);
		http.csrf(CsrfConfigurer::disable);
		http
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.addFilter(corsFilter)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.addFilter(new JwtAuthenticationFilter(authenticationManager))
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers("/api/v1/user/**").hasAnyRole("USER", "MANAGER", "ADMIN")
					.requestMatchers("/api/v1/manager/**").hasAnyRole("MANAGER", "ADMIN")
					.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN")
					.anyRequest().permitAll()
			);

		return http.build();
	}
}

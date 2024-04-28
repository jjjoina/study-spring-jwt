package com.cos.jwt.config.jwt;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	// /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		System.out.println("JwtAuthenticationFilter : 로그인 시도 중");

		try {
			// 1. username, password 받기
			ObjectMapper om = new ObjectMapper();	// ObjectMapper : JSON을 파싱해준다.
			User user = om.readValue(request.getInputStream(), User.class);
			System.out.println("user = " + user);

			UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

			// 2. 정상인지 로그인 시도
			// 이 시점에서 PrincipalDetailsService의 loadUserByUsername()이 실행된다.
			// password에 관한 것은 Spring security가 내부적으로 처리해준다.
			// authenticaionToken을 통해서 로그인 시도를 해보고 로그인 시도가 정상적으로 이루어지면 authenticaion이 만들어진다.
			// 로그인 시도 : 입력받은 username, password와 매칭되는 것이 DB에 있는가?
			Authentication authentication =
				authenticationManager.authenticate(authenticationToken);

			// authentication의 principal에 User가 잘 담겨져 있는지 출력해보자.
			// 잘 담겨져 있다는 것은 로그인이 정상적으로 되었다는 의미.
			PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
			System.out.println("principalDetails.getUser().getUsername() = " + principalDetails.getUser().getUsername());

			// 3. 반환받은 PrincipalDetails를 session에 담기
			// return함으로써 authentication 객체가 session 영역에 저장된다.
			// JWT 토큰을 이용하면 session을 만들 필요가 없지만 단지 권한 관리를 위해 만드는 것이다.
			return authentication;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// attemptAuthentication 실행 후 인증이 정상적으로 되었으면 실행되는 함수.
	// 여기서 JWT 토큰을 만들어서 응답해주면 된다.
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException, ServletException {

		System.out.println("JwtAuthenticationFilter.successfulAuthentication - 인증이 완료되었습니다.");

		super.successfulAuthentication(request, response, chain, authResult);
	}
}

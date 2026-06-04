package com.petshop.backend.security;

import com.petshop.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByEmail(username)
				.map(user -> User.builder()
						.username(user.getEmail())
						.password(user.getPassword())
						.roles(user.getRole().name())
						.disabled(!user.getEnabled())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}

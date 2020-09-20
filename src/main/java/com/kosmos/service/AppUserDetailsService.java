package com.kosmos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kosmos.dao.UserE;
import com.kosmos.dao.UserRepo;


@Service
public class AppUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepo repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserE user = repo.findByUserName(username);
		if(user == null)
		{
			throw new UsernameNotFoundException("user not found");
		}
		return new UserDetailsImpl(user);
	}

}

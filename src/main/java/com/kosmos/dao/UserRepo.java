package com.kosmos.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserE, String> {
	
	UserE findByUserName(String userID);

}

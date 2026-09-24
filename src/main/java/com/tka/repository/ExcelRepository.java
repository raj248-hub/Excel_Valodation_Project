package com.tka.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tka.Entity.*;
public interface ExcelRepository  extends JpaRepository<Students, Integer> 
{

	    boolean existsByEmail(String email);

	    boolean existsByMobile(String mobile);
	}


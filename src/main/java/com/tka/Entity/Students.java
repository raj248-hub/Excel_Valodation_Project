package com.tka.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class Students {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private int id;
	
	
	private String student_name;
	
	@Column (unique = true ) 
	private String email;
	@Column (unique = true ) 
	private String mobile;
	private String course;
	private String city;
	private double fees;


	private LocalDateTime createdAt;
	
	@PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }
	
}

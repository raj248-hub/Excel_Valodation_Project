package com.tka.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tka.Entity.ExcelResponse;
import com.tka.Service.StudentService;

@RestController
@RequestMapping("/students")
public class ExcelController {
	@Autowired
	StudentService service;
	
	@GetMapping("/hello")
	public String hello()
	{
		return "Hello";
	}
	
	 @PostMapping("/upload")
	    public ExcelResponse uploadExcel(@RequestParam("file") MultipartFile file) {

	        return service.uploadExcel(file);
	    }

}

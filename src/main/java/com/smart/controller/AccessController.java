package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AccessController {
	
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/forgot_password")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		System.out.println("Email: "+email);
		
		//check if user exists
		User user = this.userRepository.getUserByUserName(email);
		if(user==null) {
			session.setAttribute("message", "Not a registered email");
			return "forgot_email_form";
		}
		
		Random random = new Random();
		String otp = String.format("%04d", random.nextInt(10000));
		System.out.println(otp);
		
		
		boolean flag = emailservice.sendmail(email, Integer.parseInt(otp));
		if(flag) {
			session.setAttribute("email", email);
			session.setAttribute("otp_sent", otp);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Something went wrong ! Please check your email id");
			return "forgot_email_form";
		}
		
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") String otp, HttpSession session) {
		if(session.getAttribute("otp_sent").equals(otp)) {
			return "reset_password";
		}else {
			session.setAttribute("message", "Incorrect OTP provided");
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		User user = this.userRepository.getUserByUserName((String) session.getAttribute("email"));
		System.out.println(user.getPassword());
		System.out.println(bCryptPasswordEncoder.encode(newPassword));
		if(bCryptPasswordEncoder.matches(newPassword, user.getPassword())) {
			session.setAttribute("message", "New password cannot be your old password");
			return "reset_password";
		}
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		return "redirect:/signin?passwordChanged";
	}
}

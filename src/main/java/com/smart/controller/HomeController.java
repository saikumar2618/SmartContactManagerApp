package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	public BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	public UserRepository userrepository;
	
	@GetMapping("/")
	public String homeRedirect(Model model) {
		return "redirect:/user/index";
	}
	
	@GetMapping("/home")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@GetMapping("/signin")
	public String signin (Model model) {
		model.addAttribute("title","Sign-in - Smart Contact Manager");
		return "signin";
	}
	
	//handler for user registeration
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult validResult, @RequestParam(value = "agreement", 
	defaultValue = "false") boolean agreement, Model model, HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("you have not accepted terms & conditions");
				throw new Exception("Not accepted terms & conditions"); 
			}
			
			if(validResult.hasErrors()) {
				System.out.println("Error: "+validResult.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("user "+user);
			
			User result = this.userrepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!","alert-success"));
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
		}
		return "signup";
	}

}

package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.helper.StorageService;
import com.smart.helper.StorageServices1;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private StorageService storageService;
	
	/*private final AmazonS3 s3Client;
	public UserController(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}*/

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "normal/about";
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "Add contact");
		return "normal/user_dashboard";
	}

	// add-contact controller
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			/*
			 * Testing purpose - if(3>2) { throw new Exception(); }
			 */

			contact.setUser(user);

			// processing & uploading image
			if (file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				/*
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");

				// Note: After uploading image, open this in system explorer --> src --> target
				// --> static --> img
				*/     //This commented code runs only in local machine as it stores data in target directory
				
				//aws part
				storageService.upload(file);
			}

			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("Data " + contact);
			System.out.println("Added to database");
			// message success
			session.setAttribute("message", new Message("Contact added successfully !!", "success"));

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// message failure
			session.setAttribute("message", new Message("Something went wrong. Try again !", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findcontactsByUser(user.getId(), pageable);
		
		// aws code
		List base64ImageArray = new ArrayList<>();
		for(Contact c: contacts) {
			byte[] imageData = storageService.getFile(c.getImage());
			String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
			base64ImageArray.add(base64Image);
		}
		model.addAttribute("base64ImageArray", base64ImageArray);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	// show contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cid, Model model, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		// Security check for not extracting contacts of other users
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		if (user.getId() == contact.getUser().getId()) {
			
			//aws
			byte[] imageData = storageService.getFile(contact.getImage());
			
			// Convert the byte array to a Base64-encoded string for embedding in HTML
	        String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
	        model.addAttribute("base64Image", base64Image);   // aws end		
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_details";
	}

	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cid, Model model, Principal principal,
			HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		// contact.setUser(null); // In our case delete fn works smoothly so no need of
		// unlinking user & contact

		// Security check for not extracting contacts of other users
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if (user.getId() == contact.getUser().getId()) {
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
			return "redirect:/user/show-contacts/0";
		} else {
			return "normal/error-delete";
		}

	}

	// update-contact handler
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "Update contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		
		//aws code
		byte[] imageData = storageService.getFile(contact.getImage());
		
		// Convert the byte array to a Base64-encoded string for embedding in HTML
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
        model.addAttribute("base64Image", base64Image);   // aws end
		
		return "normal/update-form";
	}

	// process-update handler
	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {
		try {
			//old contact details
			Contact oldcontact = this.contactRepository.findById(contact.getCid()).get();

			if (!file.isEmpty()) {
				
				//delete old photo from 'target/img/' folder 
				/*File deletefile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deletefile, oldcontact.getImage());
				file1.delete();*/  //This code only runs in local machine as it deals data stored in target directory
				
				//aws delete old photo
				storageService.deleteFile(oldcontact.getImage());
				
				//upload new photo
				/*File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);*/
				
				//aws upload new photo
				storageService.upload(file);
				
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact name: " + contact.getName());
		System.out.println("Contact id: " + contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//profile page
	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//settings handler
	@GetMapping("/settings")
	public String settings() {
		return "normal/settings";
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
									@RequestParam("newPassword") String newPassword,
									Principal principal, HttpSession session) {
		String name = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(name);
		System.out.println(currentUser.getPassword());
		
											   //raw password      //encoded
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			if(oldPassword.equals(newPassword)) {
				session.setAttribute("message", new Message("New password cannot be same as old password", "danger"));
				return "redirect:/user/settings";
			}
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is changed successfully !", "success"));
		}else {
			session.setAttribute("message", new Message("Wrong old password", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}

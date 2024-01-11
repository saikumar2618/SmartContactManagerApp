package com.smart.helper;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public static boolean sendmail(String email_id,int otp) {
		 final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		 // Get a Properties object
		    Properties props = System.getProperties();
		    props.setProperty("mail.smtp.host", "smtp.gmail.com");
		    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		    props.setProperty("mail.smtp.socketFactory.fallback", "false");
		    props.setProperty("mail.smtp.port", "465");
		    props.setProperty("mail.smtp.socketFactory.port", "465");
		    props.put("mail.smtp.auth", "true");
		    props.put("mail.debug", "true");
		    props.put("mail.store.protocol", "pop3");
		    props.put("mail.transport.protocol", "smtp");
		    final String username = ""; //Email
		    final String password = ""; //password
		    try{
		      Session session = Session.getInstance(props, 
		                          new Authenticator(){
		                             protected PasswordAuthentication getPasswordAuthentication() {
		                                return new PasswordAuthentication(username, password);
		                             }});

		   // -- Create a new message --
		      MimeMessage msg = new MimeMessage(session);

		   // -- Set the FROM and TO fields --
		      msg.setFrom(new InternetAddress(""));
		      msg.setRecipient(Message.RecipientType.TO, 
		                        new InternetAddress(email_id));
		      msg.setSubject("System generated email..Do not reply !!");
		      msg.setText("OTP for your email verification is "+otp+"\nKindly do not share with anyone."
		    		  +"\n\nRegards,"+"\nSmart Contact Manager - Team");
		      msg.setSentDate(new Date());
		      Transport.send(msg);
		      System.out.println("Message sent.");
		      return true;
		    }catch(javax.mail.SendFailedException adr) {
		    	adr.printStackTrace();
		    	return false;
		    }catch (MessagingException e){
		      System.out.println("Error : " + e);
		      return false;
		    }
	}

}

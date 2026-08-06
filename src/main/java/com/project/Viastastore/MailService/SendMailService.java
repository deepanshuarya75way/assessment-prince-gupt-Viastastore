package com.project.Viastastore.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.project.Viastastore.Model.Users;

@Service
public class SendMailService {

	@Autowired
	public JavaMailSender javaMailSender;
	
	public void sendOtpMail(Users user) throws Exception {
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		String subject = "Welcome to Viasta Store, Please Verify otp";
		String message = "Hello"+user.getName()
				+",Welcome to Viasta Store,\n"
				+"Use the following OTP to complete your verification\n\n"
				+"OTP :"+user.getOtp()
				+"\n\n This otp will expire in 5 minutes. For security purpose do not share this otp with annyone"
				+"\n\n Thank you \n Team ViastaStore";
		
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
	
		mailMessage.setTo(user.getEmail());
		javaMailSender.send(mailMessage);
	}
}

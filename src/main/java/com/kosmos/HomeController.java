package com.kosmos;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.security.sasl.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.kosmos.app.AppConstants;
import com.kosmos.dao.UserE;
import com.kosmos.dao.UserRepo;
import com.kosmos.service.UserDetailsImpl;

import antlr.StringUtils;


@Controller
public class HomeController {
	
	@Autowired
	private UserRepo repo;
	private Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	private UserE getCurrentUser()
	{
		Object currUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String uname = (currUser instanceof UserDetails) ? ((UserDetailsImpl)currUser).getUsername() : currUser.toString();
		UserE user = repo.findById(uname).orElse(null);
		return user;
	}

	private String getAuthTokenFromString(String token)
	{
		try {
	      Signature sign = Signature.getInstance(AppConstants.AUTH_SIGN);
	      
	      KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(AppConstants.AUTH_ALG_RSA);
	      
	      keyPairGen.initialize(AppConstants.AUTH_CDE);
	      
	      KeyPair pair = keyPairGen.generateKeyPair();      
		
	      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        
	      cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
		  
	      byte[] input = token.getBytes();	  
	      cipher.update(input);
		  
	      byte[] cipherText = cipher.doFinal();
	      String authToken = new String(cipherText, "UTF8");
	      logger.info("Token Generated : " + authToken);
	      return authToken;
		}
		catch(Exception e)
		{
			logger.error("Error When generatih Auth Token : {}", e.getMessage());
			return "";
		}

	}

	
	@RequestMapping(value = "/viewProfile")
	public ModelAndView viewProfile() {
		try {
			logger.info("Into Home Controller , /viewProfile page");
			
			UserE user = getCurrentUser();
			ModelAndView mv = new ModelAndView();
			mv.addObject("user", user);
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	
	@RequestMapping(value = "/viewProfileMS")
	public ModelAndView viewProfileMS(String viewUser) {
		try {
			logger.info("Into Home Controller , /viewProfile MS page");
			
			RestTemplate ms = KosmosUserManagementMsApplication.getInvokeTemplate();
			if(viewUser == null || viewUser == "")
			{
				viewUser = getCurrentUser().getUserName();
			}
			UserE userObj = ms.getForObject("http://localhost:8090/service/viewProfile/" + viewUser, UserE.class);
			ModelAndView mv = new ModelAndView();
			mv.addObject("user", userObj);
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return null;
		}
	}
	
	
	@RequestMapping(value = "/createUser")
	public ModelAndView createUser(UserE newuser) {
		try {
			logger.info("Into Home Controller , /createUser page");
			UserE user = getCurrentUser();
			ModelAndView mv = new ModelAndView();
			
			if(AppConstants.USER_ROLE_SUPERUSER.equalsIgnoreCase(user.getUserRole()) || 
					AppConstants.USER_ROLE_ADMIN.equalsIgnoreCase(user.getUserRole()))
			{
				UserE ret = repo.save(newuser);
				
				if (ret != null)
				{
					mv.addObject("result",ret.getUserName() + " Updation Status : Success") ;
				}
				else
				{
					mv.addObject("result", " Not Updated : Failure !!! ");
				}
			}
			else
			{
				mv.addObject("result", " Not Updated : Failure !!! ");
				throw new AuthenticationException(user.getUserRole() + " is not supposed to perform createUser Operation. Operation Aborted!!! ");
			}
			
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	
	@RequestMapping(value = "/requestUserAuthToken")
	public ModelAndView requestUserAuthToken() {
		try {
			logger.info("Into Home Controller , /requestUserAuthToken page");
			UserE user = getCurrentUser();
			ModelAndView mv = new ModelAndView();
			String tokenString = user.getUserName() + user.getLastName() + System.currentTimeMillis();// Skipping URL based auth token as user name is retrieved from Principal.
			mv.addObject("USERTOKEN",getAuthTokenFromString(tokenString));
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	
	@RequestMapping(value = "/updPassword")
	public ModelAndView updPassword(String currPwd, String newPwd) {
		try {
			logger.info("Into Home Controller , /updPassword page");
			
			UserE user = getCurrentUser();
			BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
			boolean match = enc.matches(currPwd, user.getUserPassword());
			
			if(match)
			{
				repo.delete(user);
				user.setUserPassword(newPwd);
				repo.save(user);
			}
			else
			{
				logger.info("Current pwd mismatch!, cant update.");
			}
			
			ModelAndView mv = new ModelAndView();
			mv.addObject("user", user);
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	@RequestMapping(value = "/home")
	public ModelAndView home() {
		try {
			logger.info("Into Home Controller , /Home page");
			UserE user = getCurrentUser();
			ModelAndView mv = new ModelAndView();
			mv.addObject("user",user);
			mv.setViewName("home");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app home page " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	

	@RequestMapping(value = "/loginUser")
	public ModelAndView appLogin() {
		try {
			logger.info("Into Home Controller , /Login page");
			ModelAndView mv = new ModelAndView();
			mv.setViewName("login");
			return mv;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error in app login " + ex.getMessage());
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	@RequestMapping(value = "/logout")
	public ModelAndView appLogout() {
		try {
			logger.info("Into Home Controller , /Logoff page");
			ModelAndView mv = new ModelAndView();
			mv.setViewName("logoff");
			return mv;
		} catch (Exception ex) {
			logger.error("Error in app logoff " + ex.getMessage());
			ex.printStackTrace();
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
	@RequestMapping(value = "/logoff")
	public ModelAndView appLogoff() {
		try {
			logger.info("Into Home Controller , /Logoff page");
			ModelAndView mv = new ModelAndView();
			mv.setViewName("logoff");
			return mv;
		} catch (Exception ex) {
			logger.error("Error in app logoff " + ex.getMessage());
			ex.printStackTrace();
			ModelAndView mv = new ModelAndView();
			mv.setViewName("error");
			return mv;
		}
	}
	
}

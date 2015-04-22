package com.mail;

//handles just the mail sending part
//sets up a session
//then transmit a number of mails over that session
//finally ends

import java.util.Properties;

//below packages already included in fakeSMTP
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {
	
	private Transport t=null;
	private Session session=null;
	private Message message=null;
		
	public boolean setupSession() { //set up the session,transport and message
		//returns the success status boolean
		
		//Transport.send is a static method that creates a fresh connection every time
		//So, set up the Transport object to send a number of mails over a connection
		
      boolean setup_success=true;// set up status
      final String username = "user";//change accordingly
      final String password = "pass"; // on fakeSMTP no way to authenticate these
      // will need when using real SMTP server

      
      String host = "localhost"; // fakeSMTP server running on localhost; 
      String port="25";		//fakeSMTP runs default on port 25					

      Properties props = new Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", port);

       
      session=Session.getInstance(props); 
      
      try {
    	t=session.getTransport("smtp"); //initialize
		t.connect(username,password); //connect
		message = new MimeMessage(session);//message object initialize
		
	} catch (MessagingException e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		setup_success=false;//set up unsuccessful
	}
      return setup_success; //return session success
      
}//setupSession
	public  boolean sendMail(String from, String to, String subject, String body)
	{//actual mail sending 
		
	      boolean bool=true; //message placed successfully indicator
	      
      try {
    	  
         message.setFrom(new InternetAddress(from));//from

         message.setRecipients(Message.RecipientType.TO,
         InternetAddress.parse(to)); //to

         message.setSubject(subject);//subject line

         message.setText(body);//body

         // Send message
         t.sendMessage(message, message.getAllRecipients());
         

         System.out.println("Sent message successfully....");

      } catch (MessagingException e) {
            
    	  bool=false; //messaging failed
            
      }
      return bool; // return status
   }//sendMail
	
	public boolean checkStatus()//is Transport still connected
	{	boolean isConnected=false;
		if(t.isConnected())
		{isConnected=true;}//return true if connected
		return isConnected;
	}//check status
	
	public void endSession()// close the transport object
	{	if(t.isConnected()){
		try {
			t.close();
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
	}	
		
	}//endSession
	
}//class
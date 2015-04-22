package com.main;

//This is just to add dummy data into the DB.

import java.sql.*;

public class EditDB {
 
 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 static final String DB_URL = "jdbc:mysql://localhost:3306/email"; //schema:email

 //  Database credentials
 private static final String USER = "root";
 private static final String PASS = "";
 
 public static void main(String[] args) {
 Connection conn = null;
 PreparedStatement prepared=null;
 
 try{
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
	
    System.out.println("Connecting to database...");
    conn = DriverManager.getConnection(DB_URL,USER,PASS);

  
    String sql;
    
    sql = "insert into EmailQueue (from_email_address,to_email_address,subject_line,body,process_flg) values(?,?,?,?,'N')";
    int i=0;
    for(;i<2923;i++) // inserting dummy data
    {
    	String from="from_"+i+"@from.com";
    	String to="to_"+i+"@to.com";
    	String subject="sub_"+i;
    	String body="this is test body";
    	prepared=conn.prepareStatement(sql);
    	prepared.setString(1, from);
    	prepared.setString(2, to);
    	prepared.setString(3, subject);
    	prepared.setString(4, body);
    	prepared.executeUpdate();
    }
        
    prepared.close();
    conn.close(); // close both
 }catch(SQLException se){
        se.printStackTrace();
 }catch(Exception e){
        e.printStackTrace();
 }finally{
    try{
       if(prepared!=null)
          prepared.close();
    }catch(SQLException se2){
    }
    try{
       if(conn!=null)
          conn.close();
    }catch(SQLException se){
       se.printStackTrace();
    }//end finally try
 }//end try
 System.out.println("Goodbye!");
}//end main
}//end class
package com.db;

//connect the DB
//get mails and call send mail


import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.mail.*;//for send mail
//import com.main.*;//for restart call to main function of Combine after deadlock error


public class ConnectDB implements Runnable{
  
 private final Lock lock=new ReentrantLock();//lock
 
 String pID=new ProcessID().getProcessID();// Process ID --using ManagementFactory
 //same for both threads of a single process
 
 
 Connection conn = null;
 
	
 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 static final String DB_URL = "jdbc:mysql://localhost:3306/email";//?relaxAutoCommit=true";

 //  Database credentials
 private static final String USER = "root";
 private static final String PASS = "";
 
 //message check counters
 static int fail_count=0;//no of fails in messaging
 static final int RETRYVAL=3;
 int retry=RETRYVAL; // no. of retries to reestablish connection
 
 public ConnectDB(){}
 public ConnectDB(String id){//constructor adding passed thread value to process
	 pID+=id;
 }
 
 //run
 public void run() {

 try{
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();//register
	
    System.out.println("Connecting to database...");
    
    conn = DriverManager.getConnection(DB_URL,USER,PASS);
    conn.setAutoCommit(true); 
    
    System.out.println("Creating statement...");
    
    PreparedStatement prepared=null;//initialize
    ResultSet rs=null;
    boolean remainingMail=true;//mail remaining check
    
    
    SendMail mail=new SendMail();//send mail object
    mail.setupSession();//set up the connection
    
    
    while(remainingMail){	//if mail to be sent remains, go in
    	int lastRow=0;
    	remainingMail=false;
    
    conn.setAutoCommit(false);
    lock.tryLock(700, TimeUnit.MILLISECONDS);//lock
    String up1="select is_free_lock('lock')";//check for DB lock
    prepared =conn.prepareStatement(up1, ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
    int val=0;
    do{
    prepared.executeQuery(up1);
    rs=prepared.getResultSet();
    rs.next();
    val=rs.getInt(1);//1=>lock is free
    retry--;
    Thread.sleep(500);
    }while(val==0 && retry>0);// if DB lock not achievable retry(within limit)
    retry=RETRYVAL;
    prepared.executeQuery("select get_lock('lock',2)");//get DB lock
    
    String update="update EmailQueue set time_stamp=?,process_id=? where id in(select id from (select id from EmailQueue where process_flg='N' and time_stamp is null and process_id is null limit 500) alias)";
    // update the process id and the processing time stamp
    java.sql.Timestamp t_stamp=getCurrentTimeStamp();
    prepared=conn.prepareStatement(update);
    prepared.setTimestamp(1, t_stamp);
    prepared.setString(2, pID);
    prepared.executeUpdate();
    conn.commit();// complete update

    prepared.executeQuery("select release_lock('lock')");//release DB lock
    lock.unlock();//unlock

    conn.setAutoCommit(true);//set auto commit
    String sql;
    sql = "SELECT * FROM EmailQueue where time_stamp=? and process_flg='N'  and process_id=? limit 500";
    //only records with "N" i.e. not processed but with updated process id by the process
    prepared =conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
    // cursor only can move forward. update enabled
    prepared.setTimestamp(1, t_stamp);
    prepared.setString(2, pID);//set process id to be checked
    prepared.executeQuery();
    rs=prepared.getResultSet();//execute and get result
    
    if(rs.last()){
    	lastRow=rs.getRow();//get rowId
    }
   
    if(lastRow>0){//if greater than 0,mails remaining
    	remainingMail=true;
    	rs.beforeFirst();//move to before the start of result set
    
    //for each in the result set,
    //get the data, send mail
    //update the process_flg as Y
    
    
    while(rs.next()){
    	int id=rs.getInt("id");
    	String from=rs.getString("from_email_address");
    	String to=rs.getString("to_email_address");
    	String subject=rs.getString("subject_line");
    	String body=rs.getString("body");
    	//rs.updateString("process_flg", "Y");
    	
    	
    	
    	boolean isConnected=mail.checkStatus();//is connected?
        while(!isConnected && retry>0){  //if not connected retry. RETRYVAL is set to 3.
        	mail.setupSession();
        	isConnected=mail.checkStatus();//isConnected=mail.setupSession() can replace these 2 lines
        	retry--;
        }
        retry=RETRYVAL;
    	if(isConnected){//if connected
    	boolean success=mail.sendMail(from,to,subject,body);// call the mail sender and get status 
    	if (success){
    		Statement stmt=null;
    		conn.setAutoCommit(false);// start transaction
    		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); 
    		String sql2="update EmailQueue set process_flg='Y' where id="+id;
    		stmt=conn.createStatement();
    		stmt.executeUpdate(sql2);
    		conn.commit();// complete transaction
    		conn.setAutoCommit(true);
    		//rs.updateRow(); //if success then only update
    	}
    	else{
    		fail_count++;//failed msg count
    	 }
    	}//isConnected--if	
    	
       }//inner while
    
        
    
   }//lastRow>0--if
    
    
  }//remaining mail--while
   
    mail.endSession(); // all mail sent. close session
   
    
    rs.close();
    prepared.close();
    conn.close();
    System.out.println("fails:"+fail_count);// no. of fails
 }catch(SQLException se){
	 // NOT NEEDED anymore. using transaction
	 //.......................
	 if (se.getErrorCode()==1213)//code for deadlock error ER_LOCK_DEADLOCK: 1213
	 {System.out.println("Deadlock:restart!");
	 //Restart measures
	 try{
	 if(conn.isClosed()){//set up connection if got closed
		 Class.forName("com.mysql.jdbc.Driver").newInstance();
		 conn = DriverManager.getConnection(DB_URL,USER,PASS); 
	 }
	 conn.setAutoCommit(false);
	 
	 //last mail that got sent wasn't updated due to deadlock
	 String sql="update EmailQueue set process_flg='Y' where id in (select id from(select * from EmailQueue where process_flg='N' and process_id is not null order by id limit 1) alias)";
	 PreparedStatement prepared=conn.prepareStatement(sql);
	 prepared.executeUpdate();
	 
	 
	 //all the others are set for Re-sending
	 sql="update EmailQueue set process_id=null,time_stamp=null where process_flg='N' and process_id is not null";
	 prepared=conn.prepareStatement(sql);
	 prepared.executeUpdate();
	 conn.commit();
	 conn.close();
	 System.out.println("Restarting....");
	 
	 //Set a call to the main process for restarting the mail sending
	 //Combine.main(null); // restart the process
	 //This call to combine can be omitted if we have a different process in place that checks for mails to be sent
	 //and calls the combine process in the first place
	 //OR
	 //manually a call to combine can be placed after deadlock
	 }catch(Exception e){
		 try {
			conn.rollback();//roll back in case of transaction fails.
			System.out.println("Rolled back----check DB");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			System.out.println("check DB for proper updates ");
		}
		 
	 }
	 finally{
		 //
	 }
	 }
	 else{
    se.printStackTrace();}
	 //.....................
 }catch(Exception e){
	 e.printStackTrace();
 }
 finally{
    //close all remaining 
    
    try{
       if(conn!=null)
          conn.close();
    }catch(SQLException se){
       se.printStackTrace();
    }//end finally try
 }//end try
 System.out.println("Goodbye!"+pID);// end from process
}//end run
 private static java.sql.Timestamp getCurrentTimeStamp() { //generate time stamp
	 
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	} 

}//end class
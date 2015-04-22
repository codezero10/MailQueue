package com.main;

// This is the main execution point to start the mail sending

import com.db.*;


public class Combine {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread t1=new Thread(new ConnectDB("T1"));//passing thread id 
		Thread t2=new Thread(new ConnectDB("T2"));//to differentiate
		
		t1.start();
		t2.start();		

	}

}

package com.db;

//this gets the process id name of a running process
//will be same for both the threads here

import java.lang.management.*;

public class ProcessID {
	public String getProcessID(){
		String id=ManagementFactory.getRuntimeMXBean().getName();
		return id;
		
	}

}

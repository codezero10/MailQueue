Email Queue:

Developed and tested in Windows 7.
External Jars used:  1.fakeSMTP-1.13   2.mysql-connector-java-5.0.8-bin
MySQL queries:

A schema named “email” is created. Then the required table “EmailQueue” is created.
Along with required columns: id, from email, to email, subject and body; it also has 3 added columns.
1. Process_flg: (Y/N) to be set Y after the mail gets processed.
2. Process_id: id of the running process, to differentiate between different running processes.
3. Time_stamp: date time of the moment when it gets set for sending.



These are the queries used to create the table.
Dummy data insertion is done through “EditDB” java file in the main project.


Import and Execution:
Import the extracted zip as Existing project into Workspace

After Import, fakeSMTP.jar will be in lib folder.
This jar needs to be run.
The fakeSMTP.jar is auto-executable.
If your desktop environment supports it, you can directly double click on the .jar file.
Otherwise, run the following command:
 java -jar fakeSMTP.jar
https://nilhcem.github.io/FakeSMTP/

Set the Listening Port as “25” which is the default value and Start the server.

From com.main open the “EditDB” file and execute it to insert test data into the DB.
After that, from com.main open the “Combine” and execute it to start the main process.

FakeSMTP will capture all these outgoing mails and show the log.

Rerunning the program:
Update the EmailQueue table with this query:
“update EmailQueue set process_flg='N',time_stamp=null,process_id=null where id<561;”

And rerun the Combine.

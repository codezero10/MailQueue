create schema email;
use email;

create table EmailQueue(
id integer not null auto_increment primary key,
from_email_address varchar(50) not null,
to_email_address varchar(50) not null,
subject_line varchar(80) not null,
body varchar(1000)not null,
process_flg varchar(1) not null,
process_id varchar(50) default null,
time_stamp datetime default null,
index(id)
) engine=InnoDB;

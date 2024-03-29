drop sequence customer_seq;

drop sequence orders_seq;

drop sequence address_seq;

drop sequence logon_seq;

drop sequence card_details_seq;

begin
	declare
		cust_count	number :=0;
		order_count	number :=0;
		add_count	number :=0;
		logon_count	number :=0;
    card_count number :=0;
	begin
		select count(*) into cust_count from customers;
		select count(*) into order_count from orders;
		select count(*) into add_count from addresses;
		select count(*) into logon_count from logon;
    select count(*) into card_count from card_details;
		cust_count := cust_count + 1;
		order_count := order_count + 1;
		add_count := add_count + 1;
		logon_count := logon_count + 1;
    card_count := card_count +1;
		execute immediate 'create sequence customer_seq start with '||cust_count||'  cache 100000'; 
		execute immediate 'create sequence orders_seq start with '||order_count||' increment by &instancecount cache 100000';
		execute immediate 'create sequence address_seq start with '||add_count||'  cache 100000'; 
		execute immediate 'create sequence logon_seq start with '||logon_count||' cache 100000';
    	execute immediate 'create sequence card_details_seq start with '||card_count||' cache 100000';
	end;
end;
/

-- End

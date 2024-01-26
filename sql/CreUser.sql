grant	 connect, resource
to	&username	identified	by	&password
/

ALTER USER &username DEFAULT TABLESPACE &tablespace QUOTA UNLIMITED ON &tablespace;

grant alter session to &username;

grant create synonym to &username;

REM  grant	 select
REM on	sys.v_$mystat
REM to	&dbuser
REM /

REM grant	 select
REM on	sys.v_$instance
REM to	&dbuser
REM /


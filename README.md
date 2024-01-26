Kudos to `Dominic Giles` for building such a fantastic product.

> As the latest version of swingbench is not compatible with Oracle 11g, but most of the users onprem still are using Oracle 11g and seek a way to move to cloud to achieve better TCO. However, they still need to validate the performance between 11g and Cloud Database such as Oracle Autonomous Database. So that is why I keep this version to my repo or for others to replicate.

All code is provided as seen. Further information is provided at

http://www.dominicgiles.com/swingbench.html


## Swingbench Install
---
Clone the repo to get the source artifact of `swingbench`

Change into the newly created swingbench directory and then either the "bin" directory
for Linux/Unix or the "winbin" for windows systems.

Ensure java (1.6 or later) is in your executable path.

You should then be able to run swingbench or any of the wizards.

## Something You Need to Pay Attention
Please be aware that there will be some privileges problems during the data load generator, and if so, please add below sql for granting privileges to public.

```
GRANT EXECUTE ON dbms_stats TO public;
GRANT EXECUTE ON dbms_lock TO public;
GRANT SELECT ON sys.v_$parameter TO public;
```

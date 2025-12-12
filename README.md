# vehicle-log-parser
A program to parse my currently hand typed vehicle logs

To use, first prepare a key.csv file to match the log you wish to parse. The format of such is:

Line 1:
any possible keywords to signify a destination seperated by commas

Line 2:
any possible keywords to signify a fuel entry seperated by commas

Line 3:
any possible keywords to signify a service entry seperated by commas

Line 4:
any possible keywords to signify a odometer entry seperated by commas

Following Lines:
Section 1: The title of the destination
Section 2: A description of the destination
Following Comma Seperated Words: any possible keywords to signify said destination

Once the keyfile is prepared you may parse your log file using the command line through command line arguments or prompts via the program.

CLI structure:
VehicleLogParser <keyfile path> <logfile path> <outputfile path>

if any of those are omitted the program will prompt for input until validated or allow ctrl-c exit.

#include <stdio.c>
#include <errno.h>
#include <stderr.h>
#include <termios.h>

FILE* init_serial(void);
string get_serial(void);
char get_char(void);

int main(int argc, char *argv[])
{
	FILE* writefile, serialport;
	char[256] sprint;

	if (argc != 1)
	{
		printf("Incorrect number of arguments. Please only pass the address of an output file.\n");
		exit(1);
	}

	//Open the file to write recieved data to
	writefile = fopen(argv[1],"a");
	if (writefile == null)
	{
		perror("Failed to open output file");
		return errno;
	}

	//Intialize serial port
	serialport = init_serial();
	if (serialport == null)
	{
		perror("Failed to open serial communication");
		return errno;
	}

	//Read from serial port, write to output file
	while(sprint[0] != '\0')
	{
		fgets(sprint, sizeof(sprint), serialport);
		fprintf(writefile, sprint);
	}
	
	//Clean up
	fclose(writefile);
	fclose(serialport);

	return 0;
}

FILE* init_serial()
{
	FILE* sport;
	struct termios termc;

	//Open the file handle for serial comm
	sport = fopen("/dev/ttyACM0", "r+");

	//Set baud rate
	cfsetispeed(&termc, B9600);
	cfsetospeed(&termc, B9600);

	return sport;
}
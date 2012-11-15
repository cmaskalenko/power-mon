#include <stdio.h>
#include <errno.h>
#include <errno.h>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>

FILE* init_serial(void);

int main(int argc, char *argv[])
{
	FILE *writefile, *serialport;
	int i;
	char c;

	if (argc != 2)
	{
		printf("Incorrect number of arguments. Please only pass the address of an output file.\n");
		return 1;
	}

	//Open the file to write recieved data to
	writefile = fopen(argv[1],"a");
	if (writefile == NULL)
	{
		perror("Failed to open output file");
		return errno;
	}

	//Intialize serial port
	serialport = init_serial();
	if (serialport == NULL)
	{
		perror("Failed to open serial communication");
		return errno;
	}

	printf("Power (W):\r\n");

	//Read from serial port, write to output file
	
	for (i=0; i<25600; i++)
	{
		c = fgetc(serialport);
		//Add in new line when receiving carriage return
		if (c == '\r')
		{
			fputc('\r',writefile);
			fputc('\n',writefile);
			fflush(writefile);
			printf("\r\n");
		} 
		else 
		{
			fputc(c,writefile);
			fflush(writefile);
			printf("%c",c);
		}
		fflush(stdout);
	}

	//Clean up
	fclose(writefile);
	fclose(serialport);

	return 0;
}

FILE* init_serial()
{
	FILE* sport;
	int serialfp;
	struct termios termc;

	//Open the file handle for serial comm
	serialfp = open("/dev/ttyACM0", O_RDWR | O_NOCTTY);

	//Set baud rate to .5M
	cfsetispeed(&termc, B500000);
	cfsetospeed(&termc, B500000);
	termc.c_cflag |= (CLOCAL | CREAD);

	tcsetattr(serialfp, TCSANOW, &termc);

	sport = fdopen(serialfp, "rb+");

	return sport;
}

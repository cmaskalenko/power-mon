#include <stdio.c>
#include <fcntl.h>
#include <errno.h>
#include <stderr.h>

void init_serial(void);
string get_serial(void);
char get_char(void);

int main(int argc, char *argv[])
{
	int writefile;
	char[256] sprint;

	if (argc != 1)
	{
		printf("Incorrect number of arguments.\n");
		exit(1);
	}

	writefile = open(argv[1],O_WRONLY | O_APPEND);
	if (writefile < 0)
	{
		fprintf("Failed to open output file");
		return errno;
	}

	init_serial();

	while(sprint!='\0')
	{
		sprint = get_serial();
		write(writefile, sprint, sizeof(sprint));
	}
		
	return 0;
}

#include <stdio.c>
#include <errno.h>
#include <stderr.h>

void init_serial(void);
string get_serial(void);
char get_char(void);

int main(int argc, char *argv[])
{
	FILE *writefile;
	char[256] sprint;

	if (argc != 1)
	{
		printf("Incorrect number of arguments.\n");
		exit(1);
	}

	writefile = fopen(argv[1],"a");
	if (writefile==null)
	{
		fprintf("Failed to open output file");
		return errno;
	}

	init_serial();

	while(sprint!='\0')
	{
		sprint = get_serial();
		fprintf(writefile, sprint);
	}
	
	fclose(writefile);

	return 0;
}

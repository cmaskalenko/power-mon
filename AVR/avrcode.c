#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/iom88.h>
#include <stdio.h>
#include <stdlib.h>

#define F_CPU 8000000UL //8MHz clock

#include <util/delay.h>

void init_serial(void);
void transmit_serial(unsigned char data);
void transmit_message(char* message);

int counter = 0;

int main(void)
{
	init_serial();

	int i = 0, v = 0, p = 0, noise = 0;
	char msg[32];
	_delay_ms(1000);

	transmit_message("Init\n\0");

	ADCSRA = (1<<ADEN)|
			(0<<ADSC)|
			(0<<ADATE)|
			(0<<ADIF)|
			(0<<ADIE)|
			(1<<ADPS2)|
			(1<<ADPS1)|
			(0<<ADPS0);
	int j = 0;
	long long sum = 0;

	ADMUX = 1<<MUX1;
	
	while(1)
	{
		ADCSRA |= (1<<ADSC);
		while(ADCSRA&(1<<ADSC));
		v = ADC;
		ADMUX |= (1<<MUX0);
		_delay_us(100);

		ADCSRA |= (1<<ADSC);
		while(ADCSRA&(1<<ADSC));
		i = ADC;
		ADMUX = 0;
		_delay_us(100);
			
		i-=(1<<9);
		if(i < 0) i = -i;
		
//		itoa(v,msg,10);
//		transmit_message(msg);
//		transmit_message(",");
//		itoa(i,msg,10);
//		transmit_message(msg);
//		transmit_message("\r\n");
		
		sum += ((long)i*v);
		j++;
		if (j>10000)
		{
			p = (int)(.01535f*(sum/j));
			
			itoa(p,msg,10);
			transmit_message(msg);
			transmit_message("\r\n");
			j=0;
			sum=0;
		}
	}

	return 0;
}

void init_serial(void)
{
	//Set baud rate to 0.5M 
	UBRR0H = (unsigned char)(0>>8);
	UBRR0L = (unsigned char) 0;

	//Enable transmit/recieve
	UCSR0B = (1<<RXEN0) | (1<<TXEN0);

	//Configure tansmission for 8 bit, 2 stop, no parity
	UCSR0C = (3<<UCSZ00) | (1<<USBS0) | (0<<UPM00) | (0<<UPM01);
}

void transmit_message(char* message)
{
	int i = 0;
	
	//Transmit the entire message char by char
	while(message[i] != '\0')
	{
		transmit_serial(message[i]);
		i++;
	}
}

void transmit_serial(unsigned char data)
{
	//Wait for empty transmit buffer, then transmit
	while(!(UCSR0A&(1<<UDRE0)));
	UDR0 = data;
}

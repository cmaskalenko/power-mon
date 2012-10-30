#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/iom88.h>
#include <stdio.h>
#include <stdlib.h>

#define F_CPU 8000000UL //8MHz clock

long getPower(void);
void init_serial(void);
void transmit_serial(unsigned char data);
void transmit_message(char[255] message);
void init_timer(void);

int counter = 0;

int main(void)
{
	sei();
	init_serial();
	init_timer();
	return 0;
}

void testADCs(void)
{
	int i,v;
	char[255] msg;
	char[16] temp;

	ADMUX = 0; // Assumes voltage sensor is on ADC0
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	v = (ADCH<<8) | ADCL - 1<<9; // Get voltage value

	ADMUX = 1<<MUX0; // Assumes current sensor is on ADC1
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	i = (ADCH<<8) | ADCL - 1<<9; // Get current value

	itoa(v,msg,10);
	strcat(msg,",");
	itoa(i,temp,10);
	strcat(msg,temp);
	transmit_message(msg);
}

long getPower(void)
{
	long i,v;

	ADMUX = 0; // Assumes voltage sensor is on ADC0
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	v = (ADCH<<8) | ADCL - 1<<9; // Get voltage value
	
	ADMUX = 1<<MUX0; // Assumes current sensor is on ADC1
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	i = (ADCH<<8) | ADCL - 1<<9; // Get current value
	
	return i*v;
}

void init_serial(void)
{
	//Set baud rate to 9600
	UBRR0H = (unsigned char)(51>>8);
	UBRR0L = (unsigned char) 51;

	//Enable transmit/recieve
	UCSR0B = (1<<RXEN0) | (1<<TXEN0);

	//Configure tansmission for 8 bit, 2 stop, no parity
	UCSR0C = (3<<UCSZ00) | (1<<USBS0) | (0<<UPM00) | (0<<UPM01);
}

void transmit_message(char[255] message)
{
	int i;
	
	//Transmit the entire message char by char
	for(i=0; i<256; i++)
	{
		transmit_serial(message[i]);
	}
	
	//Ensure the last character is null-termination
	transmit_serial('\0');
}

void transmit_serial(unsigned char data)
{
	//Wait for empty transmit buffer, then transmit
	while(!(UCSR0A&(1<<UDRE0)));
	UDR0 = data;
}

void init_timer(void)
{
	//Ensure that OC1A is disconnected from output pin 
	TCCR1A = (0<<COM0A1) | (0<<COM0A0);
	
	//No prescaling, CTC mode
	TCCR1B = (1<<CS10) | (1<<WGM12);

	//Enable interupt flag
	TIMSK1 = (1<<OCIE1A);

	OCR1A = 3999;
}

ISR(TIMER1_COMPA_vect)
{
	counter++;
	if (counter>6000)
	{
		cli();
	}
	testADCs();
}

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/iom88.h>
#include <stdio.h>
#include <stdlib.h>

#define F_CPU 8000000UL //8MHz clock

#include <util/delay.h>

long getPower(void);
void init_serial(void);
void transmit_serial(unsigned char data);
void transmit_message(char* message);
void init_timer(void);
void init_ADC(void);

int counter = 0;

int main(void)
{
	init_serial();
	//init_ADC();
	//init_timer();
	//sei();

	int i = 0,v = 0,p=0,noise=0;
	char msg[32];
	_delay_ms(1000);

	transmit_message("Init\n\0");

	ADMUX = (0<<REFS1)|
			(0<<REFS0)|
			(0<<ADLAR)|
			(0<<MUX2)|
			(0<<MUX1)|
			(0<<MUX0);

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
		noise = ADC;
		//ADMUX = (ADMUX & (~(7<<MUX0))) | (2<<MUX0);
		ADMUX = 0;
		//ADMUX |= (1<<MUX0);
		//itoa(ADC, msg, 10);
		_delay_us(100);

		ADCSRA |= (1<<ADSC);
		while(ADCSRA&(1<<ADSC));
		v = ADC;
		ADMUX |= (1<<MUX0);
		//ADMUX = (ADMUX & (~(7<<MUX0))) | (1<<MUX0);
		//itoa(ADC, msg, 10);
		_delay_us(100);

		ADCSRA |= (1<<ADSC);
		while(ADCSRA&(1<<ADSC));
		i = ADC;

		ADMUX = 1<<MUX1;
		//ADMUX &= (~(1<<MUX0));
		//ADMUX = (ADMUX & (~(7<<MUX0))) | (0<<MUX0);

		_delay_us(100);
			
		//v-=(1<<9);
		//i-=(1<<9);
		v-=noise;
		i-=noise;

		/*itoa(v,msg,10);
		transmit_message(msg);
		transmit_message(",");
		itoa(i,msg,10);
		transmit_message(msg);
		transmit_message("\r\n");
		_delay_ms(999);*/
		

		sum += (i*v);
		j++;
		if (j>1000)
		{
			p = (int)(-.0275f*(sum/j));
			
			itoa(p,msg,10);
			transmit_message(msg);
			transmit_serial('\0');
			j=0;
			sum=0;
		}

		//transmit_message(msg);
		//transmit_message("\n");
	}


	return 0;
}

void init_ADC(void)
{
	PRR &= ~(1<<PRADC);
	ADCSRA = (1<<ADEN) | (6<<ADPS0);
	
}

void testADCs(void)
{
	int i,v;
	char msg[255];
	char temp[16];

	ADMUX = 0; // Assumes voltage sensor is on ADC0
	ADCSRA |= (1<<ADSC); // Enable ADC and start conversion
	while(ADCSRA & (1<<ADSC)); // Wait for conversion to complete
	//v = ((ADCH<<8) | ADCL) - (1<<9); // Get voltage value
	v = ADCL;

	ADMUX = 1<<MUX0; // Assumes current sensor is on ADC1
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	i = ((ADCH<<8) | ADCL) - (1<<9); // Get current value

	itoa(v,msg,10);
//	strcat(msg,",");
//	itoa(i,temp,10);
//	strcat(msg,temp);
	transmit_message(msg);	
	transmit_message(",");	
//	transmit_message(temp);	
	transmit_message("\n\0");	
}

long getPower(void)
{
	long i,v;

	ADMUX = 0; // Assumes voltage sensor is on ADC0
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	v = ((ADCH<<8) | ADCL) - (1<<9); // Get voltage value
	
	ADMUX = 1<<MUX0; // Assumes current sensor is on ADC1
	ADCSRA = (1<<ADEN) | (1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	i = ((ADCH<<8) | ADCL) - (1<<9); // Get current value
	
	return i*v;
}

void init_serial(void)
{
	//Set baud rate to 57.6K
	UBRR0H = (unsigned char)(8>>8);
	UBRR0L = (unsigned char) 8;

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

void init_timer(void)
{
	//Ensure that OC1A is disconnected from output pin 
	TCCR1A = (0<<COM0A1) | (0<<COM0A0);
	
	//No prescaling, CTC mode
	TCCR1B = (1<<CS10) | (1<<WGM12);

	//Enable interupt flag
	TIMSK1 = (1<<OCIE1A);

	OCR1A = 15999;
}

ISR(TIMER1_COMPA_vect)
{
	counter++;
	testADCs();
}

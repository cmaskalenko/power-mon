#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdio.h>

int main(void) {
	return 0;
}


long getPower()
{
	long i,v;
	
	ADMUX = 0; // Assumes voltage sensor is on ADC0
	ADCSRA = (1<<ADEN)|(1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	v = (ADCH<<8)|ADCL - 1<<9; // Get voltage value
	
	ADMUX = 1<<MUX0; // Assumes current sensor is on ADC1
	ADCSRA = (1<<ADEN)|(1<<ADSC); // Enable ADC and start conversion
	while((ADCSRA>>ADSC)&1); // Wait for conversion to complete
	i = (ADCH<<8)|ADCL - 1<<9; // Get current value
	
	return i*v;
}


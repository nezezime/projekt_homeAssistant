CFLAGS = -g -W all -std=c++0x -D WITH_GZIP
CC = g++
INCPATH = -I ./../../lib/inc
LIBPATH = -L ./../../lib/dodaj_pot
LDLIBS = -l dc -l protobuf -l rt -l z -l lpthread -l gsoapssl++
FILES = ./webService.cpp

all: webservice

webservice: $(FILES)
	sh create_from_wsdl.sh
	$(CC) -o webservice $(CFLAGS) $(INCPATH) $(LIBPATH) $(FILES) \
	./src/soapC.cpp ./src/soapGWSOAPService.cpp $(LDLIBS)

clean:
	rm webservice
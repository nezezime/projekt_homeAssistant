HOST_SYSTEM = $(shell uname | cut -f 1 -d_)
SYSTEM ?= $(HOST_SYSTEM)
CXX = g++
CPPFLAGS += `pkg-config --cflags protobuf grpc`
CXXFLAGS += -std=c++11

ifeq ($(SYSTEM),Darwin)
LDFLAGS += -L/usr/local/lib `pkg-config --libs protobuf grpc++`\
           -lgrpc++_reflection\
           -ldl
else
LDFLAGS += -L/usr/local/lib `pkg-config --libs protobuf grpc++`\
           -Wl,--no-as-needed -lgrpc++_reflection -Wl,--as-needed\
           -ldl
endif

PROTOC = protoc
GRPC_CPP_PLUGIN = grpc_cpp_plugin
GRPC_CPP_PLUGIN_PATH ?= `which $(GRPC_CPP_PLUGIN)`

PROTOS_PATH = ../../lib/proto

vpath %.proto $(PROTOS_PATH)

# non-protobuf flags
CFLAGS = -g -Wall -std=c++0x -DREMOTE_TESTING
INCPATH = -I./../../lib
LIBPATH = -L./../../lib
LDLIBS = -lprotobuf
 
FILES = storage.pb.o storage.grpc.pb.o \
				./src/dataBase.cpp

all: database

# $^ - The names of all the prerequisites, with spaces between them
database: $(FILES) 
	$(CXX) $^ $(LDFLAGS) -o $@
	$(CFLAGS) $(INCPATH) $(LIBPATH) $(FILES) $(LDLIBS)

# generate protobuf code
.PRECIOUS: %.grpc.pb.cc
%.grpc.pb.cc: %.proto
	$(PROTOC) -I $(PROTOS_PATH) --grpc_out=. --plugin=protoc-gen-grpc=$(GRPC_CPP_PLUGIN_PATH) $<

PRECIOUS: %.pb.cc
%.pb.cc: %.proto
	$(PROTOC) -I $(PROTOS_PATH) --cpp_out=. $<

clean: 
	rm -f *.o *.pb.cc *.pb.h
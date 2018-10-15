HOST_SYSTEM = $(shell uname | cut -f 1 -d_)
SYSTEM ?= $(HOST_SYSTEM)
# compiler selection
CXX = g++
# extra flags for preprocessor
CPPFLAGS += `pkg-config --cflags protobuf grpc`
# extra flags for c++ compiler
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

# path to .proto files
PROTOS_PATH = proto/

# generate proto code ?
vpath %.proto $(PROTOS_PATH)

# ZELIM POGNATI
# protoc -I proto/ --grpc_out=. --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` IME.proto
# protoc -I proto/ --cpp_out=. IME.proto

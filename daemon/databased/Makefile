PROTOC = protoc
GRPC_CPP_PLUGIN = grpc_cpp_plugin
GRPC_CPP_PLUGIN_PATH ?= `which $(GRPC_CPP_PLUGIN)`

#flags --------------------------------------------------

#compiler
CXX = g++

#compiler flags
#c++11 is required for mysql connector
CFLAGS = -g -Wall -std=c++11 -DREMOTE_TESTING

#make will look here for included library files
INCPATH = -I./../../lib -I/usr/include/mysql-cppconn \
					-I/usr/include/boost

#path to libraries
LIBPATH = -L/usr/local/include/grpc -L/usr/local/include/grpc++ \
					-L/usr/lib64

#linker flags, -l specifies runtime libraries that are reqired
LDFLAGS += -L/usr/local/lib -L /usr/include `pkg-config --libs protobuf grpc++` \
           -Wl,--no-as-needed -lgrpc++_reflection -Wl,--as-needed \
           -ldl -lmysqlcppconn
# za xdev je lmysqlcppconn8


#libraries to link
#LDLIBS = -llibgrpc++ -lprotobuf

#file prerequisites for webservice recipe
FILES = ../../lib/storage.pb.cc ../../lib/storage.grpc.pb.cc ./src/dataBase.cpp
# -------------------------------------------------------

all: system-check database

# $^ - The names of all the prerequisites, with spaces between them
database: $(FILES)
	$(CXX) $(CFLAGS) $^ $(LDFLAGS) $(INCPATH) -o $@ $(LIBPATH)

clean:
	rm -f *.o *.pb.cc *.pb.h database

# does not need to be run every time
# The following is to test your system and ensure a smoother experience.
# They are by no means necessary to actually compile a grpc-enabled software.
PROTOC_CMD = which $(PROTOC)
PROTOC_CHECK_CMD = $(PROTOC) --version | grep -q libprotoc.3
PLUGIN_CHECK_CMD = which $(GRPC_CPP_PLUGIN)
HAS_PROTOC = $(shell $(PROTOC_CMD) > /dev/null && echo true || echo false)
ifeq ($(HAS_PROTOC),true)
HAS_VALID_PROTOC = $(shell $(PROTOC_CHECK_CMD) 2> /dev/null && echo true || echo false)
endif
HAS_PLUGIN = $(shell $(PLUGIN_CHECK_CMD) > /dev/null && echo true || echo false)

SYSTEM_OK = false
ifeq ($(HAS_VALID_PROTOC),true)
ifeq ($(HAS_PLUGIN),true)
SYSTEM_OK = true
endif
endif

system-check:
ifneq ($(HAS_VALID_PROTOC),true)
	@echo " DEPENDENCY ERROR"
	@echo
	@echo "You don't have protoc 3.0.0 installed in your path."
	@echo "Please install Google protocol buffers 3.0.0 and its compiler."
	@echo "You can find it here:"
	@echo
	@echo "   https://github.com/google/protobuf/releases/tag/v3.0.0"
	@echo
	@echo "Here is what I get when trying to evaluate your version of protoc:"
	@echo
	-$(PROTOC) --version
	@echo
	@echo
endif
ifneq ($(HAS_PLUGIN),true)
	@echo " DEPENDENCY ERROR"
	@echo
	@echo "You don't have the grpc c++ protobuf plugin installed in your path."
	@echo "Please install grpc. You can find it here:"
	@echo
	@echo "   https://github.com/grpc/grpc"
	@echo
	@echo "Here is what I get when trying to detect if you have the plugin:"
	@echo
	-which $(GRPC_CPP_PLUGIN)
	@echo
	@echo
endif
ifneq ($(SYSTEM_OK),true)
	@false
endif
	@echo "system-check complete"
# storaged
# declares the service classes
protoc -I proto/ --grpc_out=. --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` storage.proto
# declares the message classes
protoc -I proto/ --cpp_out=. storage.proto

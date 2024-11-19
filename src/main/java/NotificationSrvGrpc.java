import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.67.1)",
    comments = "Source: UserStatus.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class NotificationSrvGrpc {

  private NotificationSrvGrpc() {}

  public static final String SERVICE_NAME = "com.chen.blogbackend.NotificationSrv";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.chen.blogbackend.UserStatus.UserOnlineInformation,
      com.chen.blogbackend.UserStatus.MessageResponse> getOnlineHandlerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "OnlineHandler",
      requestType = com.chen.blogbackend.UserStatus.UserOnlineInformation.class,
      responseType = com.chen.blogbackend.UserStatus.MessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chen.blogbackend.UserStatus.UserOnlineInformation,
      com.chen.blogbackend.UserStatus.MessageResponse> getOnlineHandlerMethod() {
    io.grpc.MethodDescriptor<com.chen.blogbackend.UserStatus.UserOnlineInformation, com.chen.blogbackend.UserStatus.MessageResponse> getOnlineHandlerMethod;
    if ((getOnlineHandlerMethod = NotificationSrvGrpc.getOnlineHandlerMethod) == null) {
      synchronized (NotificationSrvGrpc.class) {
        if ((getOnlineHandlerMethod = NotificationSrvGrpc.getOnlineHandlerMethod) == null) {
          NotificationSrvGrpc.getOnlineHandlerMethod = getOnlineHandlerMethod =
              io.grpc.MethodDescriptor.<com.chen.blogbackend.UserStatus.UserOnlineInformation, com.chen.blogbackend.UserStatus.MessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "OnlineHandler"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chen.blogbackend.UserStatus.UserOnlineInformation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chen.blogbackend.UserStatus.MessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationSrvMethodDescriptorSupplier("OnlineHandler"))
              .build();
        }
      }
    }
    return getOnlineHandlerMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NotificationSrvStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationSrvStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationSrvStub>() {
        @Override
        public NotificationSrvStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationSrvStub(channel, callOptions);
        }
      };
    return NotificationSrvStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NotificationSrvBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationSrvBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationSrvBlockingStub>() {
        @Override
        public NotificationSrvBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationSrvBlockingStub(channel, callOptions);
        }
      };
    return NotificationSrvBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NotificationSrvFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationSrvFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationSrvFutureStub>() {
        @Override
        public NotificationSrvFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationSrvFutureStub(channel, callOptions);
        }
      };
    return NotificationSrvFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void onlineHandler(com.chen.blogbackend.UserStatus.UserOnlineInformation request,
        io.grpc.stub.StreamObserver<com.chen.blogbackend.UserStatus.MessageResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getOnlineHandlerMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service NotificationSrv.
   */
  public static abstract class NotificationSrvImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return NotificationSrvGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service NotificationSrv.
   */
  public static final class NotificationSrvStub
      extends io.grpc.stub.AbstractAsyncStub<NotificationSrvStub> {
    private NotificationSrvStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected NotificationSrvStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationSrvStub(channel, callOptions);
    }

    /**
     */
    public void onlineHandler(com.chen.blogbackend.UserStatus.UserOnlineInformation request,
        io.grpc.stub.StreamObserver<com.chen.blogbackend.UserStatus.MessageResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getOnlineHandlerMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service NotificationSrv.
   */
  public static final class NotificationSrvBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<NotificationSrvBlockingStub> {
    private NotificationSrvBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected NotificationSrvBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationSrvBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.chen.blogbackend.UserStatus.MessageResponse onlineHandler(com.chen.blogbackend.UserStatus.UserOnlineInformation request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getOnlineHandlerMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service NotificationSrv.
   */
  public static final class NotificationSrvFutureStub
      extends io.grpc.stub.AbstractFutureStub<NotificationSrvFutureStub> {
    private NotificationSrvFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected NotificationSrvFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationSrvFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chen.blogbackend.UserStatus.MessageResponse> onlineHandler(
        com.chen.blogbackend.UserStatus.UserOnlineInformation request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getOnlineHandlerMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ONLINE_HANDLER = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ONLINE_HANDLER:
          serviceImpl.onlineHandler((com.chen.blogbackend.UserStatus.UserOnlineInformation) request,
              (io.grpc.stub.StreamObserver<com.chen.blogbackend.UserStatus.MessageResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getOnlineHandlerMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chen.blogbackend.UserStatus.UserOnlineInformation,
              com.chen.blogbackend.UserStatus.MessageResponse>(
                service, METHODID_ONLINE_HANDLER)))
        .build();
  }

  private static abstract class NotificationSrvBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NotificationSrvBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.chen.blogbackend.UserStatus.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NotificationSrv");
    }
  }

  private static final class NotificationSrvFileDescriptorSupplier
      extends NotificationSrvBaseDescriptorSupplier {
    NotificationSrvFileDescriptorSupplier() {}
  }

  private static final class NotificationSrvMethodDescriptorSupplier
      extends NotificationSrvBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NotificationSrvMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (NotificationSrvGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NotificationSrvFileDescriptorSupplier())
              .addMethod(getOnlineHandlerMethod())
              .build();
        }
      }
    }
    return result;
  }
}

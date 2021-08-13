package io.metrix.mnemosyne.interceptor

import io.grpc.*

class MetadataInterceptor(): ServerInterceptor {
    private val apiKey: String = "Rpcksf2ZjnEphYR4iFevmzw1w87lGpXf"

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        metadata: Metadata?,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val authHeader = metadata?.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER))
        println(apiKey)
        if (authHeader == null || authHeader != apiKey) {
            call?.close(Status.UNAUTHENTICATED
                .withDescription("Invalid API KEY"),
                metadata
            )
            return object: ServerCall.Listener<ReqT>() {}
        }

        return next.startCall(call, metadata)
    }
}
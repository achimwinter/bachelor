package com.example.bachelor.api;

import com.example.bachelor.DecryptRequest;
import com.example.bachelor.DecryptResponse;
import com.example.bachelor.DecrypterGrpc;
import io.grpc.stub.StreamObserver;

public class DeryptServiceImpl extends DecrypterGrpc.DecrypterImplBase {

    @Override
    public StreamObserver<DecryptRequest> subscribeMails(StreamObserver<DecryptResponse> responseObserver) {
        StreamObserver<DecryptRequest> streamObserver = new StreamObserver<DecryptRequest>() {
            @Override
            public void onNext(DecryptRequest value) {
                System.out.println();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
        return streamObserver;
    }


}

// package com.taskflow.user.infrastructure.adapter.out.messaging;

// import org.springframework.stereotype.Component;

// import com.taskflow.user.application.port.out.EventPublisherPort;

// import lombok.RequiredArgsConstructor;
// import reactor.core.publisher.Mono;
// import software.amazon.awssdk.services.sqs.SqsAsyncClient;
// import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


// import java.util.concurrent.CompletableFuture;


// @Component
// @RequiredArgsConstructor
// public class SqsEventPublisherAdapter implements EventPublisherPort {

//     private final SqsAsyncClient client;

//     @Override
//     public Mono<Void> publish(Object event) {
//         return Mono.fromFuture(
//             client.sendMessage(builder ->
//                 builder.queueUrl("...")
//                        .messageBody(Json.write(event))
//             )
//         ).then();
//     }
// }

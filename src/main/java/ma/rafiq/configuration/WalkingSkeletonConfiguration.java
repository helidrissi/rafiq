package ma.rafiq.configuration;

import ma.rafiq.application.HandleIncomingMessage;
import ma.rafiq.application.LlmClient;
import ma.rafiq.application.MessageSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalkingSkeletonConfiguration {
    @Bean
    HandleIncomingMessage handleIncomingMessage(LlmClient llmClient, MessageSender messageSender) {
        return new HandleIncomingMessage(llmClient, messageSender);
    }

    @Bean
    LlmClient walkingSkeletonLlmClient() {
        return message -> null;
    }

    @Bean
    MessageSender walkingSkeletonMessageSender() {
        return (recipient, message) -> { };
    }
}

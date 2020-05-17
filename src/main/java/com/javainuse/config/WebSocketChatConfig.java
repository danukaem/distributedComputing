package com.javainuse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketChatConfig implements WebSocketMessageBrokerConfigurer {
	@Value("${relay.host}")
	String hostName;
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/websocketApp").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableStompBrokerRelay("/topic").setRelayHost(hostName).setRelayPort(61613).setClientLogin("guest")
				.setClientPasscode("guest");

	}
}

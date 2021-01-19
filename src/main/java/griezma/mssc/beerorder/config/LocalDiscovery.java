package griezma.mssc.beerorder.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Profile("localdiscovery")
@Configuration
@EnableDiscoveryClient
public class LocalDiscovery {
}

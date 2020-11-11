package com.csye.webapp.config;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;



@Configuration
public class MetricsConfig{

    @Value("${publish.metrics:true}")
    private boolean publishMetrics;

    @Value("${metrics.server.hostname:localhost}")
    private String metricServerHost;

    @Value("${metrics.server.port:8125}")
    private int metricsServerPort;

    @Bean
    public  StatsDClient metricsClient(){
        if(publishMetrics){
            return new NonBlockingStatsDClient("csye6225",metricServerHost,metricsServerPort);
        }
        return  new NoOpStatsDClient();
    }
}
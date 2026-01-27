package com.forensicintelligencethreatreport.forensicintelligencethreatreport.configuration;

import com.google.maps.GeoApiContext;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeolocationConfiguration {

    //@Value("${app.google.maps.api-key:YOUR_API_KEY}")
    private String googleMapsApiKey;

    @Bean
    public GeoApiContext geoApiContext(){
        return new GeoApiContext.Builder().apiKey(googleMapsApiKey).build();
    }
}

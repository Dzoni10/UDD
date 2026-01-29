package com.forensicintelligencethreatreport.forensicintelligencethreatreport.configuration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GeolocationConfiguration {

    @Value("${app.locationiq.api-key}")
    private String locationIqApiKey;


    public String getLocationIqApiKey(){
        if(locationIqApiKey == null || locationIqApiKey.isEmpty()){
            log.warn("Open street location iq api key not configured. Geolocation not working.");
            throw new IllegalArgumentException(
                    "Location IQ cannot be found. " +
                            "Set locatio iq API key in application.properties"
            );
        }
        return locationIqApiKey;
    }
}

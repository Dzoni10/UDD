package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.configuration.GeolocationConfiguration;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.*;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.LoadingException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.GeoLocationService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.GeoLocationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

    private final RestTemplate restTemplate;
    private final GeolocationConfiguration geoConfig;
    private static final String LOCATIONIQ_API_URL = "https://us1.locationiq.com/v1/search";

    private Map<String, GeoLocationResultDTO> getMockOrganizations() {
        Map<String, GeoLocationResultDTO> orgs = new HashMap<>();
        orgs.put("cert_rs", new GeoLocationResultDTO(
                "cert_rs",
                "CERT.RS",
                "Beograd",
                "Srbija",
                44.8179, 20.4557,
                0.0,
                15,
                "high"
        ));
        orgs.put("cert_nis", new GeoLocationResultDTO(
                "cert_nis",
                "CERT Niš",
                "Niš",
                "Srbija",
                43.3209, 21.8954,
                0.0,
                8,
                "medium"
        ));
        orgs.put("cert_novi_sad", new GeoLocationResultDTO(
                "cert_novi_sad",
                "CERT Novi Sad",
                "Novi Sad",
                "Srbija",
                45.2671, 19.8335,
                0.0,
                12,
                "medium"
        ));
        orgs.put("cert_kragujevac", new GeoLocationResultDTO(
                "cert_kragujevac",
                "CERT Kragujevac",
                "Kragujevac",
                "Srbija",
                44.0165, 20.9105,
                0.0,
                5,
                "low"
        ));
        orgs.put("cert_bor", new GeoLocationResultDTO(
                "cert_bor",
                "CERT Bor",
                "Bor",
                "Srbija",
                44.0658, 22.0966,
                0.0,
                3,
                "low"
        ));
        return orgs;
    }

    @Override
    public GeocodeResponseDTO geocodeAddress(String cityOrAddress) {
        log.info("Geocoding address: {}", cityOrAddress);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(LOCATIONIQ_API_URL)
                    .queryParam("key", geoConfig.getLocationIqApiKey())
                    .queryParam("q", cityOrAddress)
                    .queryParam("format", "json")
                    .queryParam("addressdetails","1")
                    .toUriString();

            log.debug("LocationIQ URL: {}", url);
            ResponseEntity<LocationIqResponse[]> response = restTemplate.getForEntity(
                    url,
                    LocationIqResponse[].class
            );

            if (response.getBody() == null || response.getBody().length == 0) {
                log.warn("No geocoding results for: {}", cityOrAddress);
                throw new LoadingException("Adresa nije pronađena: " + cityOrAddress);
            }
            LocationIqResponse result = response.getBody()[0];
            double lat = Double.parseDouble(result.lat());
            double lng = Double.parseDouble(result.lon());
            // Izvuci grad i zemlju iz rezultata
            String city = extractCity(result.address());
            String country = extractCountry(result.address());
            log.info("Successfully geocoded {} to lat: {}, lng: {}, city: {}, country: {}",
                    cityOrAddress, lat, lng, city, country);
            return new GeocodeResponseDTO(lat, lng, city, country);

        } catch (LoadingException e) {
            throw e;
        }catch (Exception e){
            log.error("Geocoding error: {}", e.getMessage(), e);
            throw new LoadingException("Greška pri geokodiranju: " + e.getMessage());
        }
    }

    @Override
    public List<GeoLocationResultDTO> searchByGeoLocation(GeoLocationSearchRequestDTO request) {
        log.info("Searching by geolocation - lat: {}, lng: {}, radius: {} km",
                request.latitude(), request.longitude(), request.radiusKm());
        List<GeoLocationResultDTO> results = new ArrayList<>();
        Map<String, GeoLocationResultDTO> organizations = getMockOrganizations();
        for (GeoLocationResultDTO org : organizations.values()) {
            double distance = GeoLocationUtil.calculateDistance(
                    request.latitude(), request.longitude(),
                    org.latitude(), org.longitude()
            );
            if (distance <= request.radiusKm()) {
                // Kreiraj novi rezultat sa izračunatim rastojanjem
                GeoLocationResultDTO result = new GeoLocationResultDTO(
                        org.id(),
                        org.organization(),
                        org.city(),
                        org.country(),
                        org.latitude(),
                        org.longitude(),
                        Math.round(distance * 100.0) / 100.0, // Zaokruži na 2 decimale
                        org.incidentCount(),
                        org.threatLevel()
                );
                results.add(result);
            }
        }
        // Sortiraj po rastojanju (blize prvo)
        results.sort((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()));
        log.info("Found {} organizations within {} km radius",
                results.size(), request.radiusKm());
        return results;
    }

    private String extractCity(LocationIqAddress address) {
        if(address==null) return "Unknown";
        if (address.city() != null && !address.city().isEmpty()) {
            return address.city();
        }
        if (address.town() != null && !address.town().isEmpty()) {
            return address.town();
        }
        if (address.village() != null && !address.village().isEmpty()) {
            return address.village();
        }
        return "Unknown";
    }

    private String extractCountry(LocationIqAddress address) {
        return address.country() != null && !address.country().isEmpty() ? address.country() : "Unknown";
    }
}

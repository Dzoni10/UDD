package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.configuration.GeolocationConfiguration;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.*;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.LoadingException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexrepository.DummyIndexRepository;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.GeoLocationService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.GeoLocationUtil;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.StatisticLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.*;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

    private final RestTemplate restTemplate;
    private final GeolocationConfiguration geoConfig;
    private final DummyIndexRepository dummyIndexRepository;
    private static final String LOCATIONIQ_API_URL = "https://us1.locationiq.com/v1/search";
    private static final long RATE_LIMIT_DELAY_MS = 1200;


    @Override
    public GeocodeResponseDTO geocodeAddress(String cityOrAddress) {
        log.info("Geocoding address: {}", cityOrAddress);
        List<String> addressFormats = generateAddressVariations(cityOrAddress);
        for (int i = 0; i < addressFormats.size(); i++) {
            String addressToTry = addressFormats.get(i);
            log.info("Geocoding attempt {} with address: '{}'", i + 1, addressToTry);
            // ✅ Čekaj između pokušaja (osim prvog)
            if (i > 0) {
                try {
                    log.debug("Rate limiting delay: waiting {} ms before next attempt", RATE_LIMIT_DELAY_MS);
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Rate limit delay interrupted");
                }
            }
            try {
                return geocodeWithRetry(addressToTry);
            } catch (Exception ex) {
                log.warn("Attempt {} failed for address '{}': {}", i + 1, addressToTry, ex.getMessage());
                if (i < addressFormats.size() - 1) {
                    continue;
                } else {
                    throw new LoadingException("Could not geocode any variation of address: " + cityOrAddress);
                }
            }
        }
        throw new LoadingException("Could not geocode address: " + cityOrAddress);
    }

    private List<String> generateAddressVariations(String originalAddress) {
        if (originalAddress == null || originalAddress.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> variations = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String cleanedOriginal = originalAddress.trim()
                .replaceAll("[()]", "")
                .replaceAll(" +", " ");

        String[] parts = cleanedOriginal.split(",");
        // ✅ VARIJACIJA 1 - Samo grad (najjednostavnije, najčešće radi)

        String normalized = normalizeAddress(originalAddress);
        if(!normalized.isEmpty() && !seen.contains(normalized)) {
            variations.add(normalized);
            seen.add(normalized);
            log.info("Added variation 0 (FIRST - full normalized address): '{}'", normalized);
        }

        if (parts.length >= 2) {
            String city = parts[parts.length - 2].trim();
            if (!city.isEmpty() && !seen.contains(city)) {
                variations.add(city);
                seen.add(city);
                log.info("Added variation 1 (city only): '{}'", city);
            }
        } else if (parts.length == 1) {
            String city = parts[0].trim();
            if(!city.isEmpty() && !seen.contains(city)) {
                variations.add(city);
                seen.add(city);
                log.info("Added variation 1 (single part): '{}'", city);
            }
           }
        // ✅ VARIJACIJA 2 - Grad bez razmaka
        if (parts.length >= 2) {
            String city = parts[parts.length - 2].trim();
            String cityNoSpace = city.replaceAll("\\s+","");
            if (!cityNoSpace.isEmpty() && !seen.contains(cityNoSpace)) {
                variations.add(cityNoSpace);
                seen.add(cityNoSpace);
                log.info("Added variation 2 (city without spaces): '{}'", cityNoSpace);
            }
        }
        // ✅ VARIJACIJA 3 - Grad + Srbija → Serbia
        if (parts.length >= 2) {
            String city = parts[parts.length - 2].trim();
            String country = parts[parts.length - 1].trim();
            if (!city.isEmpty()) {
                String cityCountry = city + ", " + country;
                if (!variations.contains(cityCountry) && !seen.contains(cityCountry)) {
                    variations.add(cityCountry);
                    seen.add(cityCountry);
                    log.info("Added variation 3 (city + country): '{}'", cityCountry);
                }
            }
        }
//        // ✅ VARIJACIJA 4 - Engleski naziv grada (ako postoji mapping)
//        if (parts.length >= 2) {
//            String serbianCity = parts[parts.length - 2].trim();
//            //String englishCity = translateSerbianCityToEnglish(serbianCity);
//            if (!englishCity.equals(serbianCity) && !seen.contains(englishCity)) {
//                variations.add(englishCity);
//                seen.add(englishCity);
//                log.info("Added variation 4 (English city): '{}'", englishCity);
//            }
//        }
        log.debug("Generated {} address variations (optimized for precision): {}", variations.size(), variations);
        return variations;
    }

    private String normalizeAddress(String address) {
            if (address == null || address.isEmpty()) {
                return address;
            }

            String normalized = address.trim();

            normalized = normalized.replaceAll("(?i)\\bsrbija\\b", "Serbia");
            normalized = normalized.replaceAll("(?i)\\bsrpska\\b", "Serbia");
            normalized = normalized.replaceAll("(?i)\\bRS\\b", "Serbia");
            normalized = normalized.replaceAll("(?i)\\bSRB\\b", "Serbia");
            normalized = normalized.replaceAll("[()]", "");
            normalized = normalized.replaceAll(" +", " ").trim();

            log.debug("Address normalization: '{}' -> '{}'", address, normalized);
            return normalized;
        }

    private GeocodeResponseDTO geocodeWithRetry(String address) {
        int maxRetries = 1;
        int retryCount = 0;

        while (retryCount <= maxRetries) {
            try {
                return geocodeAddressInternal(address);
            } catch (HttpClientErrorException.NotFound ex) {
                retryCount++;
                if (retryCount > maxRetries) {
                    log.error("Geocoding failed after {} retries for: {}", maxRetries, address);
                    throw new LoadingException("Address not found: " + address + ". Error: " + ex.getMessage());
                }
                log.warn("Geocoding attempt {} failed for: {}, retrying...", retryCount, address);
                try {
                    Thread.sleep(300); // Čekaj malo pre retry-a
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception ex) {
                log.error("Unexpected error during geocoding: {}", ex.getMessage());
                throw new LoadingException("Geocoding error: " + ex.getMessage());
            }
        }
        throw new LoadingException("Could not geocode address: " + address);
    }

    private GeocodeResponseDTO geocodeAddressInternal(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(LOCATIONIQ_API_URL)
                    .queryParam("key", geoConfig.getLocationIqApiKey())
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", "1")
                    .toUriString();

            log.debug("LocationIQ URL: {}", url);
            var response = restTemplate.getForEntity(url, LocationIqResponse[].class);

            if (response.getBody() == null || response.getBody().length == 0) {
                log.warn("No geocoding results for: {}", address);
                throw new HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND,
                        "No results found for address: " + address);
            }
            LocationIqResponse result = response.getBody()[0];
            double lat = Double.parseDouble(result.lat());
            double lng = Double.parseDouble(result.lon());

            String city = extractCity(result.address());
            String country = extractCountry(result.address());
            log.info("Successfully geocoded {} to lat: {}, lng: {}, city: {}, country: {}",
                    address, lat, lng, city, country);

            return new GeocodeResponseDTO(lat, lng, city, country);
        } catch (HttpClientErrorException.NotFound ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Geocoding error: {}", ex.getMessage(), ex);
            throw new LoadingException("Greška pri geokodiranju: " + ex.getMessage());
        }
    }

    public List<OrganizationLocationDTO> getOrganizationsWithCoordinates() {
        log.info("Fetching all organizations with valid coordinates from Elasticsearch");

        try {
            List<DummyIndex> allDocuments = dummyIndexRepository.findAll();
            log.info("Found total {} documents in Elasticsearch", allDocuments.size());
            Map<String, OrganizationLocationDTO> organizationMap = new HashMap<>();

            for (DummyIndex doc : allDocuments) {
                log.info("Check doc {}, Lat: {}, Lng: {}", doc.getOrganization(), doc.getOrganizationLatitude(), doc.getOrganizationLongitude());

                if (doc.getOrganizationLatitude() == null || doc.getOrganizationLongitude() == null ||
                        (doc.getOrganizationLatitude() == 0.0 && doc.getOrganizationLongitude() == 0.0)) {
                    log.debug("Skipping organization {} - no valid coordinates", doc.getOrganization());
                    continue;
                }
                String orgName = doc.getOrganization();
                if (orgName == null || orgName.isEmpty()) {
                    log.warn("Skipping document - organization name is empty");
                    continue;
                }
                if (organizationMap.containsKey(orgName)) {
                    OrganizationLocationDTO existing = organizationMap.get(orgName);
                    organizationMap.put(orgName, new OrganizationLocationDTO(
                            existing.organization(),
                            existing.city(),
                            existing.country(),
                            existing.latitude(),
                            existing.longitude(),
                            existing.incidentCount() + 1,
                            existing.threatLevel()
                    ));
                } else {
                    organizationMap.put(orgName, new OrganizationLocationDTO(
                            doc.getOrganization(),
                            doc.getOrganizationCity() != null ? doc.getOrganizationCity() : "Unknown",
                            doc.getOrganizationCountry() != null ? doc.getOrganizationCountry() : "Unknown",
                            doc.getOrganizationLatitude(),
                            doc.getOrganizationLongitude(),
                            1,
                            doc.getThreatLevel() != null ? doc.getThreatLevel() : "low"
                    ));
                }
            }
            List<OrganizationLocationDTO> result = new ArrayList<>(organizationMap.values());
            log.info("Returning {} unique organizations with coordinates", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error fetching organizations from Elasticsearch: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrganizationLocationDTO> getOrganizationsFromElasticsearch(String city) {
        log.info("Fetching organizations from Elasticsearch for city: {}", city);
        try {
            List<DummyIndex> documents = dummyIndexRepository.findByOrganizationCity(city);
            log.info("Found {} documents for city: {}", documents.size(), city);
            Map<String, OrganizationLocationDTO> organizationMap = new HashMap<>();

            for (DummyIndex doc : documents) {
                String orgName = doc.getOrganization();
                if (organizationMap.containsKey(orgName)) {
                    OrganizationLocationDTO existing = organizationMap.get(orgName);
                    organizationMap.put(orgName, new OrganizationLocationDTO(
                            existing.organization(),
                            existing.city(),
                            existing.country(),
                            existing.latitude(),
                            existing.longitude(),
                            existing.incidentCount() + 1,  // Povećaj broj incidenata
                            existing.threatLevel()
                    ));
                } else {
                    organizationMap.put(orgName, new OrganizationLocationDTO(
                            doc.getOrganization(),
                            doc.getOrganizationCity(),
                            doc.getOrganizationCountry(),
                            doc.getOrganizationLatitude() != null ? doc.getOrganizationLatitude() : 0.0,
                            doc.getOrganizationLongitude() != null ? doc.getOrganizationLongitude() : 0.0,
                            1,  // Prvi incident
                            doc.getThreatLevel()
                    ));
                }
            }
            List<OrganizationLocationDTO> result = new ArrayList<>(organizationMap.values());
            log.info("Returning {} unique organizations", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error fetching organizations from Elasticsearch: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<GeoLocationResultDTO> searchByGeoLocation(GeoLocationSearchRequestDTO request) {
        log.info("Searching by geolocation - lat: {}, lng: {}, radius: {} km",
                request.latitude(), request.longitude(), request.radiusKm());
        List<GeoLocationResultDTO> results = new ArrayList<>();
        try {
            List<OrganizationLocationDTO> organizations = getOrganizationsWithCoordinates();
            if (organizations.isEmpty()) {
                log.warn("No organizations found in Elasticsearch for city: {}", request.cityOrAddress());
                return results;
            }
            for (OrganizationLocationDTO org : organizations) {
                double distance = GeoLocationUtil.calculateDistance(
                        request.latitude(), request.longitude(),
                        org.latitude(), org.longitude()
                );
                log.debug("Org: {}, Distance: {} km", org.organization(), distance);

                if (distance <= request.radiusKm()) {
                    GeoLocationResultDTO result = new GeoLocationResultDTO(
                            UUID.randomUUID().toString(),
                            org.organization(),
                            org.city(),
                            org.country(),
                            org.latitude(),
                            org.longitude(),
                            Math.round(distance * 100.0) / 100.0,  // Zaokruži na 2 decimale
                            org.incidentCount(),
                            org.threatLevel()
                    );
                    results.add(result);
                }
            }
            results.sort((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()));

            log.info("Found {} organizations within {} km radius",
                    results.size(), request.radiusKm());
            return results;
        } catch (Exception e) {
            log.error("Error searching by geolocation: {}", e.getMessage(), e);
            return results;
        }
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
        if (address == null) return "Unknown";
        return address.country() != null && !address.country().isEmpty() ? address.country() : "Unknown";
    }
}

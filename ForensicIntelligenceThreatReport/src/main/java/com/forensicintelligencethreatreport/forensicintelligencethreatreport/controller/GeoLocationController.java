package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeoLocationResultDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeoLocationSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeocodeResponseDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.GeoLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geolocation")
@RequiredArgsConstructor
@Slf4j
public class GeoLocationController {

    private final GeoLocationService geoLocationService;

    @GetMapping("/geocode")
    public ResponseEntity<GeocodeResponseDTO> geocodeAddress(
            @RequestParam String address) {

        log.info("Geocoding request for: {}", address);
        GeocodeResponseDTO response = geoLocationService.geocodeAddress(address);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<List<GeoLocationResultDTO>> searchByGeolocation(
            @RequestBody GeoLocationSearchRequestDTO request) {

        log.info("Geolocation search - City: {}, Radius: {} km",
                request.cityOrAddress(), request.radiusKm());

        List<GeoLocationResultDTO> results = geoLocationService.searchByGeoLocation(request);

        return ResponseEntity.ok(results);
    }
}

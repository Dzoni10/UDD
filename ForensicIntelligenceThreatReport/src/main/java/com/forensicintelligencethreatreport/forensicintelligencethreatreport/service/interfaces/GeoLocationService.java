package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeoLocationResultDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeoLocationSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeocodeResponseDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.OrganizationLocationDTO;

import java.util.List;

public interface GeoLocationService {

    GeocodeResponseDTO geocodeAddress(String cityOrAddress);

    List<GeoLocationResultDTO> searchByGeoLocation(GeoLocationSearchRequestDTO request);

    List<OrganizationLocationDTO> getOrganizationsFromElasticsearch(String city);
}

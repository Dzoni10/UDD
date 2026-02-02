package com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexrepository;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DummyIndexRepository extends ElasticsearchRepository<DummyIndex,String> {

    List<DummyIndex> findByOrganization(String organization);

    //Pronađi sve dokumente po gradu/
    List<DummyIndex> findByOrganizationCity(String city);

    //Pronađi sve jedinstvene organizacije
    @Query("{\"match_all\": {}}")
    List<DummyIndex> findAll();
}

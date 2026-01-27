package com.forensicintelligencethreatreport.forensicintelligencethreatreport.repository;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.DummyTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<DummyTable,Integer> {
    void deleteByServerFilename(String serverFilename);
}

package com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.lang.annotation.Documented;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "dummy_index")
@Setting(settingPath = "/configuration/serbian-analyzer-config.json")
public class DummyIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, name = "title")
    private String title;

    @Field(type = FieldType.Text, store = true, name = "content_sr", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String contentSr;

    @Field(type = FieldType.Text, store = true, name = "content_en", analyzer = "english", searchAnalyzer = "english")
    private String contentEn;

    @Field(type = FieldType.Text, name = "forensician_name")
    private String forensicianName;

    @Field(type = FieldType.Text, name = "organization")
    private String organization;

    @Field(type = FieldType.Text, name = "organization_city")  // ✅ NOVO
    private String organizationCity;

    @Field(type = FieldType.Text, name = "organization_country")  // ✅ NOVO
    private String organizationCountry;

    @Field(type = FieldType.Double, name = "organization_latitude")  // ✅ NOVO
    private Double organizationLatitude;

    @Field(type = FieldType.Double, name = "organization_longitude")  // ✅ NOVO
    private Double organizationLongitude;

    @Field(type = FieldType.Text, name = "malware_name")
    private String malwareName;

    @Field(type = FieldType.Keyword, name = "threat_level") // Keyword je bolji za kategorije (low, high)
    private String threatLevel;

    @Field(type = FieldType.Keyword, name = "hash_md5")
    private String hashMd5;

    @Field(type = FieldType.Keyword, name = "hash_sha256")
    private String hashSha256;

    @Field(type = FieldType.Text, store = true, name = "server_filename", index = false)
    private String serverFilename;

    @Field(type = FieldType.Integer, store = true, name = "database_id")
    private Integer databaseId;

    @Field(type = FieldType.Dense_Vector, dims = 384, similarity = "cosine")
    private float[] vectorizedContent;
}

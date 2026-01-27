export interface BasicSearchRequest {
  forensician_name: string;
  organization: string;
  malware_name: string;
  hash_value: string;
  threat_level: string;
  search_text: string;
  is_knn: boolean;
}
export interface ParsedDocument{
  server_filename: string;
  title: string;
  forensician_name: string;
  organization: string;
  malware_name: string;
  malware_description: string;
  threat_level: string;
  hash_md5: string;
  hash_sha256: string;
  confidence_score: number;
}
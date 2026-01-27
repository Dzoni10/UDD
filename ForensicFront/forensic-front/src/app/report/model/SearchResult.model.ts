export interface SearchResult {
  id: string;
  title: string;
  forensician_name: string;
  organization: string;
  malware_name: string;
  threat_level: string;
  hash_md5: string;
  hash_sha256: string;
  content_summary: string;
  highlighted_content: { [key: string]: string };
  relevance_score: number;
}
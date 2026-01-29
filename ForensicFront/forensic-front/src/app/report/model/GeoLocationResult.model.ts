export interface GeoLocationResult {
  id: string;
  organization: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  distance_km: number;
  incident_count: number;
  threat_level: string;
}
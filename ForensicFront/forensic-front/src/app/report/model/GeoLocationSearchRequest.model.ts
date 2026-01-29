export interface GeoLocationSearchRequest{
    city_or_address: string;
    latitude: number;
    longitude: number;
    radius_km: number;
}
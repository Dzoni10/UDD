import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet'
import { GeoLocationResult } from '../model/GeoLocationResult.model';
import { ReportService } from '../report.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GeoLocationSearchRequest } from '../model/GeoLocationSearchRequest.model';

@Component({
  selector: 'app-geo-search',
  templateUrl: './geo-search.component.html',
  styleUrls: ['./geo-search.component.css']
})
export class GeoSearchComponent implements OnInit{

  @ViewChild('mapContainer', {static: false}) mapContainer!: ElementRef;

  cityOrAddress: string='';
  radiusKm: number =50;
  isSearching: boolean = false;

  results: GeoLocationResult[]=[];
  totalResults: number=0;
  hasSearched: boolean = false;

  map: L.Map | null=null;
  mapMarkers: L.Marker[]=[];
  searchRadiusCircle: L.Circle | null = null;
  currentLocation: {lat:number; lng: number} | null =null;

  threatLevelColors: {[key:string]: string} ={
     'low': '#4CAF50',
    'medium': '#FF9800',
    'high': '#FF5722',
    'critical': '#F44336'
  };

  constructor(private reportService:ReportService, private snackBar: MatSnackBar){}

  ngOnInit(): void {
    // Inicijalizuj mapu nakon što se komponenta učita
    setTimeout(() => {
      this.initializeMap();
    }, 100);
  }

   private initializeMap(): void {
    if (!this.mapContainer) {
      console.error('Map container not found');
      return;
    }
    // Kreiraj mapu
    this.map = L.map(this.mapContainer.nativeElement).setView([44.8179, 20.4557], 6);
    
    setTimeout(()=>{
      this.map?.invalidateSize();
    },200);
    
    // Dodaj tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);

    console.log('Map initialized');
  }


  performGeoSearch(): void {
    if (!this.cityOrAddress.trim()) {
      this.snackBar.open('❌ Input city or address!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    if (this.radiusKm <= 0) {
      this.snackBar.open('❌ Radius must be positive number!', 'Close', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    }

    this.isSearching = true;

    // Prvo geocodiraj adresu
    this.reportService.geocodeAddress(this.cityOrAddress).subscribe({
      next: (geocodeResponse) => {
        this.currentLocation = {
          lat: geocodeResponse.latitude,
          lng: geocodeResponse.longitude
        };

        // Zatim pretraži po geolokaciji
        const searchRequest: GeoLocationSearchRequest = {
          city_or_address: this.cityOrAddress,
          latitude: geocodeResponse.latitude,
          longitude: geocodeResponse.longitude,
          radius_km: this.radiusKm
        };

        this.reportService.searchByGeolocation(searchRequest).subscribe({
          next: (results) => {
            this.results = results;
            this.totalResults = results.length;
            this.hasSearched = true;
            this.isSearching = false;

            this.updateMap();

            this.snackBar.open(
              `✅ Found ${this.totalResults} organizations!`,
              'Close',
              { duration: 3000, panelClass: ['snackbar-success'] }
            );
          },
          error: (error) => {
            this.isSearching = false;
            const msg = error.error?.message || 'Error during search!';
            this.snackBar.open(`❌ ${msg}`, 'Close', {
              duration: 5000,
              panelClass: ['snackbar-error']
            });
          }
        });
      },
      error: (error) => {
        this.isSearching = false;
        const msg = error.error?.message || 'City not found!';
        this.snackBar.open(`❌ ${msg}`, 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  private updateMap(): void {
    if (!this.map || !this.currentLocation) return;

    // Obriši stare markere
    this.mapMarkers.forEach(marker => this.map!.removeLayer(marker));
    this.mapMarkers = [];

    // Obriši stari krug
    if (this.searchRadiusCircle) {
      this.map.removeLayer(this.searchRadiusCircle);
    }

    // Dodaj radijus krug
    this.searchRadiusCircle = L.circle(
      [this.currentLocation.lat, this.currentLocation.lng],
      {
        radius: this.radiusKm * 1000, // Convert km to meters
        color: '#667eea',
        fill: true,
        fillColor: '#667eea',
        fillOpacity: 0.1,
        weight: 2
      }
    ).addTo(this.map);

    // Dodaj markere za pronađene organizacije
    this.results.forEach((result) => {
      const color = this.threatLevelColors[result.threat_level] || '#9E9E9E';

      const markerIcon = L.divIcon({
        html: `<div style="background-color: ${color}; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);">📍</div>`,
        iconSize: [30, 30],
        className: 'geo-marker'
      });

      const marker = L.marker(
        [result.latitude, result.longitude],
        { icon: markerIcon }
      ).addTo(this.map!);

      // Popup sa detaljima
      const popupContent = `
        <div class="marker-popup">
          <strong>${result.organization}</strong><br>
          📍 ${result.city}, ${result.country}<br>
          📏 ${result.distance_km.toFixed(2)} km<br>
          🚨 ${result.incident_count} cases<br>
          ⚠️ Threat level: ${this.getThreatLevelLabel(result.threat_level)}
        </div>
      `;

      marker.bindPopup(popupContent);
      this.mapMarkers.push(marker);
    });

    // Dodaj marker za centralni grad
    const centerMarker = L.marker([this.currentLocation.lat, this.currentLocation.lng], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(this.map);

    centerMarker.bindPopup('Searching : ' + this.cityOrAddress);
    this.mapMarkers.push(centerMarker);

    // Fituj mapu na sve markere
    if (this.mapMarkers.length > 0) {
      const group = new L.FeatureGroup(this.mapMarkers);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  getThreatLevelLabel(level: string): string {
    const labels: { [key: string]: string } = {
      'low': '🟢 Niska',
      'medium': '🟡 Srednja',
      'high': '🟠 Visoka',
      'critical': '🔴 Kritična'
    };
    return labels[level] || level;
  }

  getThreatLevelColor(level: string): string {
    return this.threatLevelColors[level] || '#9E9E9E';
  }


  clearSearch(): void {
    this.cityOrAddress = '';
    this.radiusKm = 50;
    this.results = [];
    this.totalResults = 0;
    this.hasSearched = false;
    this.currentLocation = null;

    // Očisti mapu
    this.mapMarkers.forEach(marker => this.map?.removeLayer(marker));
    this.mapMarkers = [];

    if (this.searchRadiusCircle) {
      this.map?.removeLayer(this.searchRadiusCircle);
      this.searchRadiusCircle = null;
    }
  }

  zoomToResult(result: GeoLocationResult): void {
    if (!this.map) return;
    this.map.setView([result.latitude, result.longitude], 12);
  }


}

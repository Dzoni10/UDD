import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DocumentUploadResponse } from './model/DocumentploadResponse.model';
import { AuthService } from '../auth/auth.service';
import { ParsedDocument } from './model/ParsedDocument.model';
import { ConfirmIndexDocument } from './model/ConfirmIndexDocument.model';
import { BasicSearchRequest } from './model/BasicSearchRequest.model';
import { AdvancedSearchRequest } from './model/AdvancedSearchRequest.model';
import { GeocodeResponse } from './model/GeocodeResponse.model';
import { GeoLocationSearchRequest } from './model/GeoLocationSearchRequest.model';
import { GeoLocationResult } from './model/GeoLocationResult.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private apiUrl = 'http://localhost:8080/api/file';
  private apiParseUrl = 'http://localhost:8080/api/parse-preview';
  private apiSearchUrl = 'http://localhost:8080/api/search';
  private apiGeoSearchUrl = 'http://localhost:8080/api/geolocation'

  constructor(private http: HttpClient, private authService:AuthService) { }

  uploadDocument(file: File): Observable<HttpEvent<DocumentUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    const request = new HttpRequest('POST', `${this.apiUrl}/upload`, formData, {
      reportProgress: true,
      responseType: 'json',
    });

    return this.http.request<DocumentUploadResponse>(request);
  }


  downloadDocument(filename:string): Observable<Blob>{
    return this.http.get(`${this.apiUrl}/download/${filename}`,{
      responseType:'blob',
      headers: this.authService.getAuthHeaders()
    });
  }

  deleteDocument(filename:string): Observable<void>{
    return this.http.delete<void>(`${this.apiUrl}/delete/${filename}`,{
      headers: this.authService.getAuthHeaders()
    });
  }

  getSignedUrl(filename:string): Observable<string>{
    return this.http.get(`${this.apiUrl}/signed-url/${filename}`,{
      responseType:'text',
      headers: this.authService.getAuthHeaders()
    });
  }


  //PARSE DOCUMENT


  parseDocument(serverFilename: string): Observable<ParsedDocument> {
    return this.http.get<ParsedDocument>(
      `${this.apiParseUrl}/${serverFilename}`
    );
  }

  confirmAndIndex(document: ConfirmIndexDocument): Observable<{message:string}> {
    return this.http.post<{message:string}>(
      `${this.apiParseUrl}/confirm`,
      document
    );
  }

  
  cancelAndDelete(serverFilename: string): Observable<string> {
    return this.http.delete<string>(
      `${this.apiParseUrl}/${serverFilename}`
    );
  }


//Search 


  basicSearch(request: BasicSearchRequest, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post<any>(
      `${this.apiSearchUrl}/basic?page=${page}&size=${size}`,
      request
    );
  }

  advancedSearch(request: AdvancedSearchRequest, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post<any>(
      `${this.apiSearchUrl}/advanced?page=${page}&size=${size}`,
      request
    );
  }
  
  knnSearch(query: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post<any>(
      `${this.apiSearchUrl}/knn?query=${encodeURIComponent(query)}&page=${page}&size=${size}`,
      {}
    );
  }
  
  phraseSearch(phrase: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post<any>(
      `${this.apiSearchUrl}/phrase?phrase=${encodeURIComponent(phrase)}&page=${page}&size=${size}`,
      {}
    );
  }

  fullTextSearch(text: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post<any>(
      `${this.apiSearchUrl}/fulltext?text=${encodeURIComponent(text)}&page=${page}&size=${size}`,
      {}
    );
  }


  //GEOCODE SEARCH

  geocodeAddress(address: string): Observable<GeocodeResponse> {
    return this.http.get<GeocodeResponse>(
      `${this.apiGeoSearchUrl}/geocode?address=${encodeURIComponent(address)}`
    );
  }

  searchByGeolocation(request: GeoLocationSearchRequest): Observable<GeoLocationResult[]> {
    return this.http.post<GeoLocationResult[]>(
      `${this.apiGeoSearchUrl}/search`,
      request
    );
  }

}

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { DecodedToken } from './model/DecodedToken';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Register } from './model/Register.model';
import { AuthResponse } from './model/AuthResponse.model';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<DecodedToken | null>(this.loadUserFromToken());
  currentUser$ = this.currentUserSubject.asObservable();

  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }


   register(registration: Register): Observable<any>{
    return this.http.post(`${this.apiUrl}/signup`,registration);
  }

  login(email: string, password: string):  Observable<AuthResponse>{
    const body = {email, password};
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`,body)
  }

  getAuthHeaders(): HttpHeaders {
  const token = this.getToken();
  let headers = new HttpHeaders();
  if(token)
  {
    headers=headers.set('Authorization',`Bearer ${token}`);
  }
  return headers;
}

saveToken(token: string){
    localStorage.setItem('jwtToken',token)
    const decoded=jwtDecode<DecodedToken>(token);
    this.currentUserSubject.next(decoded);
  }

  getCurrentUser(): DecodedToken|null{
    return this.currentUserSubject.value;
  }

getToken():string|null{
    return localStorage.getItem('jwtToken')
  }

logout(){
    localStorage.removeItem('jwtToken');
    this.currentUserSubject.next(null);
}

private loadUserFromToken(): DecodedToken | null {
    const token = this.getToken();
    if(token){
      try{
        const decoded = jwtDecode<DecodedToken>(token);

        if(this.isTokenExpired(decoded)){
          this.logout();
          return null;
        }

        return decoded;
        }
      catch
          {
              return null;
          }
    }
    return null;
  }

  private isTokenExpired(decoded: DecodedToken): boolean{
    return decoded.exp*1000 < Date.now();
  }
}

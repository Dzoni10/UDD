import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {MatCardModule} from '@angular/material/card'
import {MatSnackBarModule} from "@angular/material/snack-bar"
import {MatIconModule} from "@angular/material/icon"
import { AuthModule } from './auth/auth.module';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import {MatDialogModule} from '@angular/material/dialog'
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from './shared/shared.module';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import {MatInputModule} from '@angular/material/input';
import { ReportModule } from './report/report.module';
import { AuthInterceptor } from './auth/interceptor-jwt/jwt-interceptor';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatCardModule,
    MatSnackBarModule,
    MatIconModule,
    AuthModule,
    NoopAnimationsModule,
    RouterModule,
    HttpClientModule,
    MatDialogModule,
    CommonModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    SharedModule,
    MatButtonModule,
    MatOptionModule,
    MatInputModule,
    ReportModule
    
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi:true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

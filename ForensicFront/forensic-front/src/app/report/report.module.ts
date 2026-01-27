import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchComponent } from './search/search.component';
import { UploadComponent } from './upload/upload.component';
import { ParseReviewComponent } from './parse-review/parse-review.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatLineModule } from '@angular/material/core';
import {MatProgressBarModule} from '@angular/material/progress-bar'
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { GeoSearchComponent } from './geo-search/geo-search.component'

@NgModule({
  declarations: [
    SearchComponent,
    UploadComponent,
    ParseReviewComponent,
    GeoSearchComponent
  ],
  imports: [
    CommonModule,
    MatSnackBarModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatLineModule,
    MatProgressBarModule,
    FormsModule,
    MatProgressSpinnerModule
  ]
})
export class ReportModule { }

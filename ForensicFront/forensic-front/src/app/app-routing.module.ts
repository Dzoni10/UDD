import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { UploadComponent } from './report/upload/upload.component';
import { SearchComponent } from './report/search/search.component';
import { ParseReviewComponent } from './report/parse-review/parse-review.component';
import { GeoSearchComponent } from './report/geo-search/geo-search.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent},
  { path: 'signup', component: SignupComponent},
  { path: 'search', component: SearchComponent},
  { path: 'upload', component: UploadComponent},
  { path: 'parse', component:ParseReviewComponent},
  { path: 'geo', component:GeoSearchComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

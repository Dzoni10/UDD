import { Component } from '@angular/core';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { AuthService } from '../auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  animations: [
    trigger('slideIn', [
      state('void', style({ transform: 'translateY(0)', opacity: 0 })),
      transition(':enter', [
        animate('2.0s ease-out', style({ transform: 'translateY(0)', opacity: 1 }))
      ])
    ])
  ]
})
export class LoginComponent {

  loginForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required,
    ])
  });

  constructor(
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  login(): void {
    
  if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.snackBar.open("Input valid email and password", "Close", {
        duration: 3000,
        horizontalPosition: "center"
      });
      return;
    }


    if (this.loginForm.value.email !== null) {
      this.authService.login(
        this.loginForm.get('email')!.value!,
        this.loginForm.get('password')!.value!
      ).subscribe({
        next: (res) => {
          this.authService.saveToken(res.accessToken);
          console.log("Logged successfully");
          this.snackBar.open("Login successfull!", "Close", {
            duration: 3000,
            horizontalPosition: "center"
          });
          this.loginForm.reset();
          const user = this.authService.getCurrentUser();
        },
        error: (err) => {
          let message = '';

          switch (err.status) {
            case 404:
              message = "User does not exist";
              break;
            case 401:
              message = "Incorrect password";
              break;
            default:
              message = "Unsuccessfull login, try again";
          }
          this.openSnackBar(message);
        }
      });
    }
}

openSnackBar(message: string): void {
    this.snackBar.open(message, '', {
      duration: 4000,
      horizontalPosition: 'center'
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.loginForm.get(fieldName);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) return `${fieldName} is required`;
    if (control.errors['email']) return 'Incorrect format email-a';
    if (control.errors['minlength']) return 'Too short passwrod';
    return '';
  }

  hasError(fieldName: string): boolean {
    const control = this.loginForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }



}

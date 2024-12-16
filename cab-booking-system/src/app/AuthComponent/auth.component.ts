import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth',
  template: `
    <div class="login-container">
      <h2>Login</h2>
      <form (submit)="onLogin()">
        <mat-form-field>
          <input matInput placeholder="Employee ID" [(ngModel)]="employeeId" name="employeeId" required>
        </mat-form-field>
        <br>
        <button mat-raised-button color="primary" type="submit">Login</button>
      </form>
    </div>
  `,
  styles: [`
    .login-container { width: 300px; margin: auto; margin-top: 100px; text-align: center; }
  `]
})
export class AuthComponent {
  employeeId: string = '';

  constructor(private router: Router) {}

  onLogin() {
    if (this.employeeId) {
      sessionStorage.setItem('employeeId', this.employeeId);
      this.router.navigate(['/dashboard']);
    }
  }
}

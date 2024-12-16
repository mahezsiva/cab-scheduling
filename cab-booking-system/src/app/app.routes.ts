import {RouterModule, Routes} from '@angular/router';
import {AuthComponent} from './AuthComponent/auth.component';
import {NgModule} from '@angular/core';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' }, // Default route redirects to 'login'
  { path: 'login', component: AuthComponent },
  // Auth Component for login
  // { path: 'dashboard', component: AdminComponent },   // for admin view
  { path: '**', redirectTo: 'login' }                   // Wildcard route to handle unknown paths
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

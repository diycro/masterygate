import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'dashboard', canActivate: [authGuard], loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'topics', canActivate: [authGuard], loadComponent: () => import('./pages/topics/topics.component').then(m => m.TopicsComponent) },
  { path: 'path', canActivate: [authGuard], loadComponent: () => import('./pages/path/path.component').then(m => m.PathComponent) },
  { path: 'module', canActivate: [authGuard], loadComponent: () => import('./pages/module/module.component').then(m => m.ModuleComponent) },
  { path: 'gate', canActivate: [authGuard], loadComponent: () => import('./pages/gate/gate.component').then(m => m.GateComponent) },
  { path: 'mock/start', canActivate: [authGuard], loadComponent: () => import('./pages/mock-start/mock-start.component').then(m => m.MockStartComponent) },
  { path: 'mock/round', canActivate: [authGuard], loadComponent: () => import('./pages/mock-round/mock-round.component').then(m => m.MockRoundComponent) },
  { path: 'mock/report', canActivate: [authGuard], loadComponent: () => import('./pages/mock-report/mock-report.component').then(m => m.MockReportComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];

import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

const SUFFIX = ' · MasteryGate';

export const routes: Routes = [
  { path: 'login', title: 'MasteryGate — Free Gated Learning Paths & Interview Prep', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'dashboard', title: 'Dashboard' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'topics', title: 'Topics' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/topics/topics.component').then(m => m.TopicsComponent) },
  { path: 'path', title: 'Your Path' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/path/path.component').then(m => m.PathComponent) },
  { path: 'module', title: 'Module' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/module/module.component').then(m => m.ModuleComponent) },
  { path: 'course', title: 'Course' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/course/course-player.component').then(m => m.CoursePlayerComponent) },
  { path: 'gate', title: 'Mastery Gate' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/gate/gate.component').then(m => m.GateComponent) },
  { path: 'mock/start', title: 'Mock Interview' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/mock-start/mock-start.component').then(m => m.MockStartComponent) },
  { path: 'mock/round', title: 'Mock Interview' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/mock-round/mock-round.component').then(m => m.MockRoundComponent) },
  { path: 'mock/report', title: 'Mock Interview Report' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/mock-report/mock-report.component').then(m => m.MockReportComponent) },
  { path: 'settings', title: 'Settings' + SUFFIX, canActivate: [authGuard], loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];

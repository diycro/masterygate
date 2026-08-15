import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { StoreService } from '../services/store.service';

export const authGuard: CanActivateFn = () => {
  const store = inject(StoreService);
  const router = inject(Router);
  if (store.isLoggedIn) return true;
  router.navigate(['/login']);
  return false;
};

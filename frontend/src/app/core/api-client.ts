import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_BASE } from './api';
import type {
  AuthResponse,
  InvestmentRequest,
  InvestmentResponse,
  OfferingCreateRequest,
  OfferingResponse,
  PropertyCreateRequest,
  PropertyResponse,
} from './types';

export class ApiClient {
  private http = inject(HttpClient);

  register(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_BASE}/api/auth/register`, {
      email,
      password,
    });
  }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_BASE}/api/auth/login`, {
      email,
      password,
    });
  }

  ping() {
    return this.http.get<{ status: string }>(`${API_BASE}/api/proptech/ping`);
  }

  createProperty(req: PropertyCreateRequest) {
    return this.http.post<PropertyResponse>(`${API_BASE}/api/proptech/properties`, req);
  }

  listProperties() {
    return this.http.get<PropertyResponse[]>(`${API_BASE}/api/proptech/properties`);
  }

  createOffering(req: OfferingCreateRequest) {
    return this.http.post<OfferingResponse>(`${API_BASE}/api/proptech/offerings`, req);
  }

  listOfferings(propertyId?: string) {
    const url = propertyId
      ? `${API_BASE}/api/proptech/offerings?propertyId=${encodeURIComponent(propertyId)}`
      : `${API_BASE}/api/proptech/offerings`;
    return this.http.get<OfferingResponse[]>(url);
  }

  setOfferingStatus(offeringId: string, status: 'OPEN' | 'CLOSED') {
    return this.http.put<OfferingResponse>(`${API_BASE}/api/proptech/offerings/${offeringId}/status`, { status });
  }

  invest(req: InvestmentRequest) {
    return this.http.post<InvestmentResponse>(`${API_BASE}/api/proptech/investments`, req);
  }

  listInvestments(offeringId: string) {
    return this.http.get<InvestmentResponse[]>(
      `${API_BASE}/api/proptech/investments?offeringId=${encodeURIComponent(offeringId)}`,
    );
  }
}


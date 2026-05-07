import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiClient } from './core/api-client';
import type { OfferingResponse, PropertyResponse } from './core/types';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe, DecimalPipe, DatePipe],
  template: `
    <div class="container">
      <div class="topbar">
        <div class="brand">
          <h1>PropTech · Tokenización de Activos</h1>
          <small>Demo monolito: JWT + Propiedades + Ofertas + Inversiones</small>
        </div>

        <div class="pill">
          <span class="dot" [class.good]="isAuthed()" [class.bad]="!isAuthed()"></span>
          <span>{{ isAuthed() ? 'Autenticado' : 'No autenticado' }}</span>
        </div>
      </div>

      <div class="grid">
        <div class="card">
          <h2>Acceso</h2>

          <label>Email</label>
          <input [(ngModel)]="authEmail" placeholder="tu@email.com" />

          <label>Contraseña</label>
          <input [(ngModel)]="authPassword" type="password" placeholder="mínimo 6 caracteres" />

          <div class="row" style="margin-top: 12px">
            <button class="btn primary" (click)="register()" [disabled]="busy()">Registrar</button>
            <button class="btn" (click)="login()" [disabled]="busy()">Iniciar sesión</button>
          </div>

          <div class="row" style="margin-top: 10px">
            <button class="btn danger" (click)="logout()" [disabled]="busy() || !isAuthed()">Cerrar sesión</button>
            <button class="btn" (click)="ping()" [disabled]="busy() || !isAuthed()">Ping protegido</button>
          </div>

          <div *ngIf="error()" class="error">{{ error() }}</div>
          <div *ngIf="success()" class="success">{{ success() }}</div>
          <div class="hint">
            Backend local por defecto: <b>http://localhost:8080</b> (configurable en <code>src/app/core/api.ts</code>)
          </div>
        </div>

        <div class="card">
          <div class="sectionTitle">
            <h2>Módulo PropTech</h2>
            <span *ngIf="!isAuthed()">Inicia sesión para ver el módulo</span>
          </div>

          <ng-container *ngIf="isAuthed(); else locked">
            <div class="row">
              <div>
                <h2 style="margin-top: 14px">Nueva propiedad</h2>
                <label>Título</label>
                <input [(ngModel)]="pTitle" placeholder="Apartamento Centro" />
                <label>Descripción</label>
                <textarea [(ngModel)]="pDesc" placeholder="Describe la propiedad..."></textarea>
                <div class="row">
                  <div>
                    <label>Ciudad</label>
                    <input [(ngModel)]="pCity" placeholder="Medellín" />
                  </div>
                  <div>
                    <label>País</label>
                    <input [(ngModel)]="pCountry" placeholder="CO" />
                  </div>
                </div>
                <label>Valoración (USD)</label>
                <input [(ngModel)]="pValuation" type="number" min="1" step="0.01" />
                <div class="row" style="margin-top: 12px">
                  <button class="btn primary" (click)="createProperty()" [disabled]="busy()">Crear propiedad</button>
                  <button class="btn" (click)="refresh()" [disabled]="busy()">Actualizar listas</button>
                </div>
              </div>

              <div>
                <h2 style="margin-top: 14px">Nueva oferta</h2>
                <label>Propiedad</label>
                <select [(ngModel)]="selectedPropertyId">
                  <option value="" disabled>Selecciona una propiedad</option>
                  <option *ngFor="let p of properties()" [value]="p.id">
                    {{ p.title }} · {{ p.city }} ({{ p.valuationUsd | currency: 'USD' }})
                  </option>
                </select>
                <div class="row">
                  <div>
                    <label>Total tokens</label>
                    <input [(ngModel)]="oTotalTokens" type="number" min="1" step="0.01" />
                  </div>
                  <div>
                    <label>Precio por token (USD)</label>
                    <input [(ngModel)]="oTokenPrice" type="number" min="0.01" step="0.01" />
                  </div>
                </div>
                <div class="row" style="margin-top: 12px">
                  <button class="btn primary" (click)="createOffering()" [disabled]="busy() || !selectedPropertyId">
                    Crear oferta
                  </button>
                </div>
                <div class="hint">
                  Regla: totalTokens × precio debe estar entre 50% y 150% de la valoración.
                </div>
              </div>
            </div>

            <div class="row" style="margin-top: 14px">
              <div style="flex: 1.4">
                <h2>Propiedades</h2>
                <div class="list">
                  <div class="item" *ngFor="let p of properties()">
                    <div class="itemTop">
                      <div>
                        <h3>{{ p.title }}</h3>
                        <p>{{ p.description }}</p>
                        <p style="margin-top: 6px">
                          {{ p.city }}, {{ p.country }} ·
                          {{ p.valuationUsd | currency: 'USD' }} ·
                          {{ p.createdAt | date: 'medium' }}
                        </p>
                      </div>
                      <span class="tag">ID {{ p.id.slice(0, 8) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div style="flex: 1">
                <h2>Ofertas</h2>
                <div class="list">
                  <div class="item" *ngFor="let o of offerings()">
                    <div class="itemTop">
                      <div>
                        <h3>Oferta · {{ o.id.slice(0, 8) }}</h3>
                        <p>
                          Tokens: {{ o.totalTokens | number: '1.0-2' }} · Vendidos:
                          {{ o.tokensSold | number: '1.0-2' }}
                        </p>
                        <p style="margin-top: 6px">
                          Precio: {{ o.tokenPriceUsd | currency: 'USD' }} · Propiedad: {{ o.propertyId.slice(0, 8) }}
                        </p>
                      </div>
                      <span class="tag"
                        [class.good]="o.status === 'OPEN'"
                        [class.warn]="o.status === 'DRAFT'"
                        [class.bad]="o.status === 'CLOSED'">
                        {{ o.status }}
                      </span>
                    </div>

                    <div class="row" style="margin-top: 10px">
                      <button class="btn" (click)="setStatus(o, 'OPEN')" [disabled]="busy() || o.status === 'OPEN'">
                        Abrir
                      </button>
                      <button class="btn" (click)="setStatus(o, 'CLOSED')" [disabled]="busy() || o.status === 'CLOSED'">
                        Cerrar
                      </button>
                      <button class="btn primary" (click)="selectOffering(o)" [disabled]="busy()">
                        Invertir
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="card" style="margin-top: 14px; padding: 14px" *ngIf="selectedOffering()">
              <div class="row" style="align-items: center">
                <div>
                  <h2 style="margin:0">Inversión</h2>
                  <div class="hint" style="margin-top: 6px">
                    Oferta seleccionada: <b>{{ selectedOffering()!.id }}</b> ({{ selectedOffering()!.status }})
                  </div>
                </div>
                <div style="text-align:right">
                  <button class="btn danger" (click)="clearOffering()" [disabled]="busy()">Cerrar</button>
                </div>
              </div>

              <div class="row" style="margin-top: 8px">
                <div>
                  <label>Tokens a comprar</label>
                  <input [(ngModel)]="invTokens" type="number" min="0.01" step="0.01" />
                </div>
                <div>
                  <label>Monto estimado (USD)</label>
                  <input [value]="estimatedAmount()" disabled />
                </div>
              </div>
              <div class="row" style="margin-top: 12px">
                <button class="btn primary" (click)="invest()" [disabled]="busy()">Confirmar inversión</button>
                <button class="btn" (click)="loadInvestments()" [disabled]="busy()">Ver inversiones</button>
              </div>

              <div class="list" *ngIf="investments().length">
                <div class="item" *ngFor="let inv of investments()">
                  <div class="itemTop">
                    <div>
                      <h3>{{ inv.investorEmail }}</h3>
                      <p>
                        Tokens: {{ inv.tokensRequested | number: '1.0-2' }} · Monto:
                        {{ inv.amountUsd | currency: 'USD' }}
                      </p>
                      <p style="margin-top: 6px">{{ inv.createdAt | date: 'medium' }}</p>
                    </div>
                    <span class="tag good">{{ inv.status }}</span>
                  </div>
                </div>
              </div>
            </div>
          </ng-container>

          <ng-template #locked>
            <div class="hint" style="margin-top: 12px">
              Este módulo está protegido por JWT. Regístrate o inicia sesión para crear propiedades, tokenizarlas y simular inversiones.
            </div>
          </ng-template>
        </div>
      </div>
    </div>
  `,
})
export class AppComponent {
  private api = new ApiClient();

  authEmail = 'demo@proptech.com';
  authPassword = 'secret123';

  pTitle = 'Apartamento Centro';
  pDesc = 'Propiedad con alto potencial de renta.';
  pCity = 'Medellín';
  pCountry = 'CO';
  pValuation = 120000;

  selectedPropertyId = '';
  oTotalTokens = 1000;
  oTokenPrice = 120;

  invTokens = 10;

  busy = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  token = signal<string | null>(localStorage.getItem('proptech_token'));
  isAuthed = computed(() => !!this.token());

  properties = signal<PropertyResponse[]>([]);
  offerings = signal<OfferingResponse[]>([]);
  selectedOffering = signal<OfferingResponse | null>(null);
  investments = signal<any[]>([]);

  estimatedAmount = computed(() => {
    const o = this.selectedOffering();
    if (!o) return '';
    const amount = Number(this.invTokens || 0) * Number(o.tokenPriceUsd || 0);
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'USD' }).format(amount);
  });

  private setStatusMsg(kind: 'ok' | 'err', msg: string) {
    if (kind === 'ok') {
      this.error.set(null);
      this.success.set(msg);
    } else {
      this.success.set(null);
      this.error.set(msg);
    }
  }

  private handleError(e: any) {
    const msg =
      e?.error?.error ||
      e?.error?.message ||
      (typeof e?.error === 'string' ? e.error : null) ||
      e?.message ||
      'Error inesperado.';
    this.setStatusMsg('err', msg);
  }

  register() {
    this.busy.set(true);
    this.api
      .register(this.authEmail, this.authPassword)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (r) => {
          localStorage.setItem('proptech_token', r.token);
          this.token.set(r.token);
          this.setStatusMsg('ok', 'Registro exitoso. Sesión iniciada.');
          this.refresh();
        },
        error: (e) => this.handleError(e),
      });
  }

  login() {
    this.busy.set(true);
    this.api
      .login(this.authEmail, this.authPassword)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (r) => {
          localStorage.setItem('proptech_token', r.token);
          this.token.set(r.token);
          this.setStatusMsg('ok', 'Inicio de sesión correcto.');
          this.refresh();
        },
        error: (e) => this.handleError(e),
      });
  }

  logout() {
    localStorage.removeItem('proptech_token');
    this.token.set(null);
    this.properties.set([]);
    this.offerings.set([]);
    this.selectedOffering.set(null);
    this.investments.set([]);
    this.setStatusMsg('ok', 'Sesión cerrada.');
  }

  ping() {
    this.busy.set(true);
    this.api
      .ping()
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.setStatusMsg('ok', 'Ping OK (endpoint protegido).'),
        error: (e) => this.handleError(e),
      });
  }

  refresh() {
    this.busy.set(true);
    this.error.set(null);
    this.success.set(null);
    this.api
      .listProperties()
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (props) => {
          this.properties.set(props);
          if (!this.selectedPropertyId && props.length) this.selectedPropertyId = props[0].id;
          this.api.listOfferings().subscribe({
            next: (offs) => this.offerings.set(offs),
            error: (e) => this.handleError(e),
          });
        },
        error: (e) => this.handleError(e),
      });
  }

  createProperty() {
    this.busy.set(true);
    this.api
      .createProperty({
        title: this.pTitle,
        description: this.pDesc,
        city: this.pCity,
        country: this.pCountry,
        valuationUsd: Number(this.pValuation),
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (p) => {
          this.setStatusMsg('ok', `Propiedad creada: ${p.title}`);
          this.refresh();
        },
        error: (e) => this.handleError(e),
      });
  }

  createOffering() {
    if (!this.selectedPropertyId) return;
    this.busy.set(true);
    this.api
      .createOffering({
        propertyId: this.selectedPropertyId,
        totalTokens: Number(this.oTotalTokens),
        tokenPriceUsd: Number(this.oTokenPrice),
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (o) => {
          this.setStatusMsg('ok', `Oferta creada en estado ${o.status}.`);
          this.refresh();
        },
        error: (e) => this.handleError(e),
      });
  }

  setStatus(o: OfferingResponse, status: 'OPEN' | 'CLOSED') {
    this.busy.set(true);
    this.api
      .setOfferingStatus(o.id, status)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (updated) => {
          this.setStatusMsg('ok', `Oferta actualizada: ${updated.status}`);
          this.refresh();
        },
        error: (e) => this.handleError(e),
      });
  }

  selectOffering(o: OfferingResponse) {
    this.selectedOffering.set(o);
    this.investments.set([]);
    this.invTokens = 10;
  }

  clearOffering() {
    this.selectedOffering.set(null);
    this.investments.set([]);
  }

  invest() {
    const o = this.selectedOffering();
    if (!o) return;
    this.busy.set(true);
    this.api
      .invest({ offeringId: o.id, tokensRequested: Number(this.invTokens) })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (inv) => {
          this.setStatusMsg('ok', `Inversión aceptada: ${inv.amountUsd} USD`);
          this.refresh();
          this.loadInvestments();
        },
        error: (e) => this.handleError(e),
      });
  }

  loadInvestments() {
    const o = this.selectedOffering();
    if (!o) return;
    this.busy.set(true);
    this.api
      .listInvestments(o.id)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (list) => this.investments.set(list),
        error: (e) => this.handleError(e),
      });
  }
}


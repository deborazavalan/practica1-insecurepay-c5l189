import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { BancoService } from '../../services/banco.service';
import { AuthService } from '../../services/auth.service';
import { Pagina, Transaccion } from '../../models/banco.model';

@Component({
  selector: 'app-transacciones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './transacciones.component.html'
})
export class TransaccionesComponent implements OnInit, OnDestroy {
  readonly tamanoPagina = 10;

  transacciones: Transaccion[] = [];
  transaccionSeleccionada: Transaccion | null = null;
  conceptoSeguro: SafeHtml = '';
  terminoBusqueda = '';
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  loading = false;
  errorInfo = '';

  private readonly buscadorSubject = new Subject<string>();
  private subscripcion?: Subscription;

  constructor(
    private bancoService: BancoService,
    public authService: AuthService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.subscripcion = this.buscadorSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap(texto => {
        this.loading = true;
        this.errorInfo = '';
        this.paginaActual = 0;
        return this.bancoService.buscarTransaccionesPaginadas(texto.trim(), 0, this.tamanoPagina);
      })
    ).subscribe({
      next: pagina => this.aplicarPagina(pagina),
      error: () => this.manejarError()
    });

    this.cargarTransacciones(0);
  }

  ngOnDestroy(): void {
    this.subscripcion?.unsubscribe();
  }

  onInputCambio(valor: string): void {
    this.buscadorSubject.next(valor);
  }

  cargarTransacciones(pagina: number): void {
    this.loading = true;
    this.errorInfo = '';
    this.bancoService.buscarTransaccionesPaginadas(this.terminoBusqueda.trim(), pagina, this.tamanoPagina).subscribe({
      next: data => this.aplicarPagina(data),
      error: () => this.manejarError()
    });
  }

  restablecer(): void {
    this.terminoBusqueda = '';
    this.buscadorSubject.next('');
    this.cargarTransacciones(0);
  }

  paginaAnterior(): void {
    if (this.paginaActual > 0) {
      this.cargarTransacciones(this.paginaActual - 1);
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual + 1 < this.totalPaginas) {
      this.cargarTransacciones(this.paginaActual + 1);
    }
  }

  verComprobante(t: Transaccion): void {
    this.transaccionSeleccionada = t;
    this.conceptoSeguro = t.conceptoHtml ? this.sanitizer.bypassSecurityTrustHtml(t.conceptoHtml) : '';
  }

  cerrarComprobante(): void {
    this.transaccionSeleccionada = null;
    this.conceptoSeguro = '';
  }

  private aplicarPagina(pagina: Pagina<Transaccion>): void {
    this.transacciones = pagina.content;
    this.paginaActual = pagina.number;
    this.totalPaginas = pagina.totalPages;
    this.totalElementos = pagina.totalElements;
    this.loading = false;
  }

  private manejarError(): void {
    this.loading = false;
    this.transacciones = [];
    this.errorInfo = 'No fue posible consultar las transacciones.';
  }
}

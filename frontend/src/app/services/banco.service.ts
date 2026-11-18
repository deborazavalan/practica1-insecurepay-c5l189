import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta, Pagina, Transaccion, TransferenciaRequest } from '../models/banco.model';

@Injectable({
  providedIn: 'root'
})
export class BancoService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  obtenerCuentasUsuario(usuarioId: number): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.apiUrl}/cuentas/usuario/${usuarioId}`);
  }

  obtenerCuentaPorId(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.apiUrl}/cuentas/${id}`);
  }

  listarTodasLasCuentas(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.apiUrl}/cuentas`);
  }

  listarTransacciones(): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/transacciones`);
  }

  obtenerTransaccionPorId(id: number): Observable<Transaccion> {
    return this.http.get<Transaccion>(`${this.apiUrl}/transacciones/${id}`);
  }

  obtenerTransaccionesPorCuenta(cuentaId: number): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/transacciones/cuenta/${cuentaId}`);
  }

  buscarTransaccionesPaginadas(termino: string, pagina: number, tamano: number): Observable<Pagina<Transaccion>> {
    const params = new HttpParams()
      .set('termino', termino)
      .set('page', pagina)
      .set('size', tamano);
    return this.http.get<Pagina<Transaccion>>(`${this.apiUrl}/transacciones/buscar-seguro`, { params });
  }

  transferir(req: TransferenciaRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.apiUrl}/transacciones/transferir`, req);
  }
}


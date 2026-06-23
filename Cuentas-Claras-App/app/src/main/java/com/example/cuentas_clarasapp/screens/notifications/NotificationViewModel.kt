package com.example.cuentas_clarasapp.screens.notifications

import androidx.lifecycle.ViewModel
import com.example.cuentas_clarasapp.screens.home.HomeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificaciones: StateFlow<List<AppNotification>> = _notificaciones.asStateFlow()

    // Evaluador dinámico condicional basado en el estado financiero del usuario
    fun evaluarCondicionesFinancieras(datos: HomeData) {
        val listaAlertas = mutableListOf<AppNotification>()

        val saldoDisponible = datos.saldoDisponible
        val montoInicialConfigurado = datos.montoInicialConfigurado
        val limiteDiarioInicial = datos.limiteDiarioInicial
        val gastoDiarioActual = datos.gastoDiarioActual
        val gastosTotalesCiclo = datos.gastosTotalesCiclo

        val dineroRestanteHoy = (limiteDiarioInicial - gastoDiarioActual).coerceAtLeast(0.0)

        // -----------------------------------------------------------------------
        // CONDICIÓN 1: Billetera Intacta (Gasto Cero)
        // -----------------------------------------------------------------------
        if (gastoDiarioActual == 0.0 && limiteDiarioInicial > 0.0) {
            listaAlertas.add(
                AppNotification(
                    id = "gasto_cero",
                    titulo = "¡Billetera Intacta! 🎯",
                    mensaje = "Enhorabuena, no has gastado nada de tu presupuesto diario. ¡Tu cuenta te lo agradece!",
                    fecha = "Hoy",
                    prioridad = NotificationPriority.INFO
                )
            )
        }

        // -----------------------------------------------------------------------
        // CONDICIÓN 2: El 50% Disponible del Día (Interacción amigable)
        // -----------------------------------------------------------------------
        if (limiteDiarioInicial > 0.0 && gastoDiarioActual > 0.0) {
            val porcentajeGastadoDiario = (gastoDiarioActual / limiteDiarioInicial) * 100
            if (porcentajeGastadoDiario >= 50.0 && porcentajeGastadoDiario < 80.0) {
                listaAlertas.add(
                    AppNotification(
                        id = "charamusca_alerta",
                        titulo = "Mitad del día calculado 🌤️",
                        mensaje = "Te queda el 50% disponible para hoy... ¿No se te antoja una charamusca o un café para relajarte?",
                        fecha = "Hoy",
                        prioridad = NotificationPriority.INFO
                    )
                )
            }
        }

        // -----------------------------------------------------------------------
        // CONDICIÓN 3: Alerta Crítica Diaria (Menos del 20% disponible)
        // -----------------------------------------------------------------------
        if (limiteDiarioInicial > 0.0 && dineroRestanteHoy <= (limiteDiarioInicial * 0.20) && dineroRestanteHoy > 0.0) {
            listaAlertas.add(
                AppNotification(
                    id = "limite_critico_diario",
                    titulo = "Cupo diario casi agotado ⚠️",
                    mensaje = "🚨 Cuidado: Te queda menos del 20% de tu límite de gasto diario. Ponele un freno a las compras de hoy.",
                    fecha = "Hace 10 min",
                    prioridad = NotificationPriority.WARNING
                )
            )
        }

        // -----------------------------------------------------------------------
        // CONDICIÓN 4: Alerta Global del Ciclo (50% del Presupuesto Total consumido)
        // -----------------------------------------------------------------------
        if (montoInicialConfigurado > 0.0 && gastosTotalesCiclo >= (montoInicialConfigurado * 0.50)) {
            listaAlertas.add(
                AppNotification(
                    id = "mitad_presupuesto_global",
                    titulo = "Punto medio del periodo 📊",
                    mensaje = "¡Ojo! Has consumido la mitad o más de tu presupuesto total configurado para este ciclo.",
                    fecha = "Hoy",
                    prioridad = NotificationPriority.WARNING
                )
            )
        }

        // -----------------------------------------------------------------------
        // CONDICIÓN EXTRA 5: Alerta de Fondo de Emergencia Global (< 10% del total)
        // -----------------------------------------------------------------------
        if (montoInicialConfigurado > 0.0 && saldoDisponible <= (montoInicialConfigurado * 0.10)) {
            listaAlertas.add(
                AppNotification(
                    id = "reserva_critica_global",
                    titulo = "Frenazo de Emergencia 🚨",
                    mensaje = "¡Alerta máxima! Tu balance disponible global está por debajo del 10% del presupuesto inicial. Modera drásticamente los flujos.",
                    fecha = "Ahora",
                    prioridad = NotificationPriority.CRITICAL
                )
            )
        }

        _notificaciones.value = listaAlertas
    }
}

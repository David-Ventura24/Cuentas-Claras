const express = require('express');
const cors = require('cors');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);

app.get('/', (req, res) => {
    res.send('Backend de CuentasClarasApp activo');
});

// ─────────────────────────────────────────
// MIDDLEWARE
// ─────────────────────────────────────────
const verificarToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];

    console.log("--- NUEVA PETICIÓN ---");
    console.log("Ruta:", req.path);
    console.log("Cabecera Authorization recibida:", authHeader);

    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        console.log("❌ ERROR 401: No se envió ningún token.");
        return res.status(401).json({ error: "Acceso denegado. Token no proporcionado." });
    }

    try {
        const verificado = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = verificado;
        console.log(" Token válido para el usuario ID:", verificado.id);
        next();
    } catch (error) {
        console.log(" ERROR 403: Token inválido o expirado.", error.message);
        return res.status(403).json({ error: "Token inválido o expirado" });
    }
};

// ─────────────────────────────────────────
// AUTH
// ─────────────────────────────────────────
app.post('/api/auth/register', async (req, res) => {
    const { correo_electronico, nombre, contrasena } = req.body;

    if (!correo_electronico || !nombre || !contrasena) {
        return res.status(400).json({ error: "Todos los campos son obligatorios" });
    }

    try {
        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(contrasena, salt);

        const { data, error } = await supabase
            .from('Usuario')
            .insert([{ correo_electronico, nombre, contrasena: contrasenaEncriptada }])
            .select('id, correo_electronico, nombre')
            .single();

        if (error) return res.status(400).json({ error: error.message });

        return res.status(201).json({ mensaje: "¡Usuario registrado con éxito!", usuario: data });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/login', async (req, res) => {
    const { correo_electronico, contrasena } = req.body;

    if (!correo_electronico || !contrasena) {
        return res.status(400).json({ error: "Correo y contraseña son obligatorios" });
    }

    try {
        const { data: usuario, error } = await supabase
            .from('Usuario')
            .select('*')
            .eq('correo_electronico', correo_electronico)
            .single();

        if (error || !usuario) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        const contrasenaValida = await bcrypt.compare(contrasena, usuario.contrasena);
        if (!contrasenaValida) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo_electronico },
            process.env.JWT_SECRET,
            { expiresIn: '365d' }
        );

        return res.status(200).json({
            mensaje: "¡Inicio de sesión exitoso!",
            token: token,
            usuario: {
                id: usuario.id,
                nombre: usuario.nombre,
                correo_electronico: usuario.correo_electronico
            }
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/recuperar-password', async (req, res) => {
    const { correo_electronico } = req.body;

    if (!correo_electronico) {
        return res.status(400).json({ error: "El correo electrónico es obligatorio" });
    }

    try {
        const { data: usuario, error: errUsuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (errUsuario || !usuario) {
            return res.status(404).json({ error: "No existe ningún usuario con ese correo" });
        }

        const tokenAleatorio = String(Math.floor(100000 + Math.random() * 900000));
        const fechaExpiracion = new Date();
        fechaExpiracion.setMinutes(fechaExpiracion.getMinutes() + 15);

        const { error: errToken } = await supabase
            .from('token_recuperacion')
            .insert([{
                id_usuario: usuario.id,
                token: tokenAleatorio,
                expiracion: fechaExpiracion,
                usado: false
            }]);

        if (errToken) return res.status(400).json({ error: errToken.message });

        return res.status(200).json({
            mensaje: "Código de recuperación generado con éxito (Simulación)",
            codigo_simulado: tokenAleatorio
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/restablecer-password', async (req, res) => {
    const { correo_electronico, token, nueva_contrasena } = req.body;

    if (!correo_electronico || !token || !nueva_contrasena) {
        return res.status(400).json({ error: "Todos los campos son obligatorios" });
    }

    try {
        const { data: usuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        const { data: registroToken, error: errT } = await supabase
            .from('token_recuperacion')
            .select('*')
            .eq('id_usuario', usuario.id)
            .eq('token', token)
            .eq('usado', false)
            .maybeSingle();

        if (errT || !registroToken) {
            return res.status(400).json({ error: "El código es inválido o ya fue utilizado" });
        }

        if (new Date() > new Date(registroToken.expiracion)) {
            return res.status(400).json({ error: "El código de recuperación ha expirado" });
        }

        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(nueva_contrasena, salt);

        const { error: errUpdateUser } = await supabase
            .from('Usuario')
            .update({ contrasena: contrasenaEncriptada })
            .eq('id', usuario.id);

        if (errUpdateUser) return res.status(400).json({ error: errUpdateUser.message });

        await supabase
            .from('token_recuperacion')
            .update({ usado: true })
            .eq('id', registroToken.id);

        return res.status(200).json({ mensaje: "Contraseña actualizada con éxito!" });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// USUARIO
// ─────────────────────────────────────────
app.get('/api/usuario/perfil', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    console.log("--- 👤 PIDIENDO PERFIL --- ID:", usuarioId);

    try {
        const { data: usuario, error } = await supabase
            .from('Usuario')
            .select('nombre, carrera')
            .eq('id', usuarioId)
            .maybeSingle();

        if (error || !usuario) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        return res.status(200).json({
            nombre: usuario.nombre,
            carrera: usuario.carrera || "Carrera no definida",
            moneda: "USD ($)",
            estadoCuenta: "Activa"
        });

    } catch (error) {
        console.log(" Fallo crítico en Perfil:", error.message);
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// PRESUPUESTO
// ─────────────────────────────────────────
app.post('/api/presupuestos', verificarToken, async (req, res) => {
    console.log("--- 💾 PROCESANDO PRESUPUESTO BLINDADO ---");
    const usuarioId = req.usuario.id;
    const { cantidad_total, periodo, porcentaje_ahorro } = req.body;

    const total = parseFloat(cantidad_total);
    const pctAhorro = parseFloat(porcentaje_ahorro || 0);
    const montoAhorro = total * (pctAhorro / 100);
    const dineroDisponibleTotal = total - montoAhorro;
    const dias = periodo.toLowerCase() === 'semanal' ? 7 : 30;
    const limiteDiarioCalculado = dineroDisponibleTotal / dias;

    try {
        // Consultar si ya existe un registro para preservar la fecha original de creación del ciclo
        const { data: presupuestoExistente } = await supabase
            .from('Presupuesto')
            .select('created_at, fecha_inicio')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        const fechaInicioOriginal = presupuestoExistente?.fecha_inicio || new Date().toISOString();
        const fechaCreatedOriginal = presupuestoExistente?.created_at || new Date().toISOString();

        const { data, error } = await supabase
            .from('Presupuesto')
            .upsert({
                id_usuario: usuarioId,
                cantidad_total: total,
                ahorro: montoAhorro,
                periodo: periodo,
                cantidad_disponible: dineroDisponibleTotal,
                dinero_disponible: dineroDisponibleTotal,
                limite_diario: limiteDiarioCalculado,
                monto_inicial_real: dineroDisponibleTotal,
                fecha_inicio: fechaInicioOriginal, // BLINDAJE: Conserva el inicio del ciclo activo
                created_at: fechaCreatedOriginal,
                updated_at: fechaCreatedOriginal   // BLINDAJE: Evita que se mueva la referencia de los gastos pasados
            }, { onConflict: 'id_usuario' })
            .select()
            .single();

        if (error) {
            console.log(" Error Supabase:", error.message);
            return res.status(400).json({ error: error.message });
        }

        console.log(" Presupuesto guardado y blindado con éxito");
        return res.status(200).json({ mensaje: "OK", presupuesto: data });

    } catch (error) {
        console.log(" Error Crítico:", error.message);
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// HOME
// ─────────────────────────────────────────
app.get('/api/home', verificarToken, async (req, res) => {
    try {
        const usuarioId = req.usuario.id;
        const { data: usuario } = await supabase.from('Usuario').select('nombre').eq('id', usuarioId).single();
        const { data: presupuesto } = await supabase.from('Presupuesto').select('*').eq('id_usuario', usuarioId).maybeSingle();

        if (!presupuesto) {
            return res.status(200).json({
                error: null,
                nombre_usuario: usuario?.nombre || "Usuario",
                cantidad_disponible: 0,
                monto_total_configurado: 0,
                periodo: "Sin configurar",
                porcentaje_ahorro: 0,
                limite_diario: 0,
                total_gastado_hoy: 0,
                total_gastado_ciclo: 0,
                gastos_hoy: []
            });
        }

        // BLINDAJE: Usamos la fecha original del ciclo para que ningún gasto previo desaparezca
        const fechaReferencia = new Date(presupuesto.fecha_inicio || presupuesto.created_at).getTime();
        const { data: todosLosGastos } = await supabase.from('Gastos').select('*').eq('id_usuario', usuarioId);

        const gastosCiclo = (todosLosGastos || []).filter(g => {
            return new Date(g.fecha).getTime() >= fechaReferencia;
        });

        const totalGastadoCiclo = gastosCiclo.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        
        // MATEMÁTICA REAL BLINDADA:
        const montoTotalBruto = parseFloat(presupuesto.cantidad_total || 0);
        const montoBaseDisponible = parseFloat(presupuesto.monto_inicial_real || (montoTotalBruto - parseFloat(presupuesto.ahorro || 0)));
        const saldoReal = montoBaseDisponible - totalGastadoCiclo;

        console.log(`--- CÁLCULO DE BALANCE MATEMÁTICO ---`);
        console.log(`Bruto Configurado: ${montoTotalBruto} | Base Disponible: ${montoBaseDisponible}`);
        console.log(`Gastos Ciclo: ${totalGastadoCiclo} | Saldo Disponible Neto: ${saldoReal}`);

        const hoyISO = new Date().toISOString().split('T')[0];
        const gastosHoy = (todosLosGastos || []).filter(g => (g.fecha || "").startsWith(hoyISO));

        return res.status(200).json({
            error: null,
            nombre_usuario: usuario?.nombre || "Usuario",
            cantidad_disponible: Math.max(0, saldoReal), // Muestra exactamente los $179.00 correctos
            monto_total_configurado: montoTotalBruto,    // Devuelve limpio el valor configurado ($100.00)
            periodo: presupuesto.periodo || "Mensual",
            porcentaje_ahorro: Math.round((parseFloat(presupuesto.ahorro || 0) / parseFloat(presupuesto.cantidad_total || 1)) * 100),
            limite_diario: parseFloat(presupuesto.limite_diario || 0),
            total_gastado_hoy: gastosHoy.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0),
            total_gastado_ciclo: totalGastadoCiclo,
            gastos_hoy: gastosHoy.map(g => ({
                id: g.id,
                categoria: g.categoria,
                monto: g.total_gastado,
                fecha: g.fecha
            }))
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// GASTOS
// ─────────────────────────────────────────
app.post('/api/gastos', verificarToken, async (req, res) => {
    try {
        const { categoria, total_gastado } = req.body;
        if (!categoria || !total_gastado) return res.status(400).json({ error: "Faltan datos" });

        const { data, error } = await supabase.from('Gastos').insert([{
            id_usuario: req.usuario.id,
            categoria,
            total_gastado: parseFloat(total_gastado),
            fecha: new Date().toISOString()
        }]).select();

        if (error) return res.status(400).json({ error: error.message });
        return res.status(201).json({ mensaje: "Gasto registrado", error: null });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/gastos/historial', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const mesFiltro = parseInt(req.query.mes) || new Date().getMonth() + 1;
    const anioFiltro = parseInt(req.query.anio) || new Date().getFullYear();

    try {
        const { data: gastos, error: errGastos } = await supabase
            .from('Gastos')
            .select('*')
            .eq('id_usuario', usuarioId);

        if (errGastos) return res.status(400).json({ error: errGastos.message });

        const { data: ahorros, error: errAhorros } = await supabase
            .from('Ahorros')
            .select('*')
            .eq('id_usuario', usuarioId);

        if (errAhorros) return res.status(400).json({ error: errAhorros.message });

        const transaccionesFiltradas = (gastos || []).filter(g => {
            if (!g.fecha) return false;
            const partes = g.fecha.split('T')[0].split('-');
            return parseInt(partes[0]) === anioFiltro && parseInt(partes[1]) === mesFiltro;
        });

        const ahorrosFiltrados = (ahorros || []).filter(a => {
            if (!a.fecha) return false;
            const partes = a.fecha.split('T')[0].split('-');
            return parseInt(partes[0]) === anioFiltro && parseInt(partes[1]) === mesFiltro;
        });

        const totalGastadoMes = transaccionesFiltradas.reduce(
            (acc, g) => acc + parseFloat(g.total_gastado || 0), 0
        );

        const totalAhorradoMesNeto = ahorrosFiltrados.reduce((acc, a) => {
            const montoNumerico = parseFloat(a.monto || 0);
            const tipoMovimiento = (a.tipo || "").trim().toUpperCase();

            if (tipoMovimiento === 'RETIRO') {
                return acc - montoNumerico;
            } else {
                return acc + montoNumerico;
            }
        }, 0);

        const transaccionesMapeadas = transaccionesFiltradas.map(g => {
            let colorHex = "#70777A";
            const cat = (g.categoria || "").toLowerCase();
            if (cat === "alimentacion") colorHex = "#E54B4B";
            else if (cat === "educacion") colorHex = "#2EC4B6";
            else if (cat === "transporte") colorHex = "#3A86FF";
            else if (cat === "ocio") colorHex = "#F7A072";
            else if (cat === "compras") colorHex = "#8338EC";

            return {
                id: g.id.toString(),
                descripcion: g.categoria || "Gasto general",
                monto: parseFloat(g.total_gastado || 0),
                categoria: g.categoria || "Otros",
                fecha: g.fecha ? g.fecha.split('T')[0] : "Fecha desconocida",
                colorHex: colorHex
            };
        });

        return res.status(200).json({
            error: null,
            totalGastadoMes: totalGastadoMes,
            totalAhorradoMes: totalAhorradoMesNeto,
            ahorro_neto: totalAhorradoMesNeto,
            transacciones: transaccionesMapeadas
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/gastos/verificar-cupo', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const montoAGastar = parseFloat(req.query.monto || 0);

    try {
        const { data: presupuesto } = await supabase
            .from('Presupuesto')
            .select('dinero_disponible')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        const { data: gastos } = await supabase
            .from('Gastos')
            .select('total_gastado')
            .eq('id_usuario', usuarioId);

        const totalGastado = (gastos || []).reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const dineroDisponibleReal = (presupuesto?.dinero_disponible || 0) - totalGastado;

        return res.status(200).json({ permitido: dineroDisponibleReal >= montoAGastar });

    } catch (error) {
        return res.status(500).json({ permitido: false, error: error.message });
    }
});

// ─────────────────────────────────────────
// AHORROS
// ─────────────────────────────────────────
app.get('/api/ahorros/status', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    console.log("--- PIDIENDO AHORROS para usuario:", usuarioId);

    try {
        const { data: movimientos, error } = await supabase
            .from('Ahorros')
            .select('*')
            .eq('id_usuario', usuarioId)
            .order('fecha', { ascending: false });

        if (error) {
            console.log(" Error leyendo Ahorros:", error.message);
            return res.status(400).json({ error: error.message });
        }

        const totalAcumulado = (movimientos || []).reduce((acc, m) => {
            const montoNumerico = parseFloat(m.monto || 0);
            const tipoMovimiento = (m.tipo || "").trim().toUpperCase();

            if (tipoMovimiento === 'RETIRO') {
                return acc - montoNumerico;
            } else {
                return acc + montoNumerico;
            }
        }, 0);

        console.log("✅ Total neto calculado:", totalAcumulado, "| Movimientos:", movimientos?.length);

        return res.status(200).json({
            ahorro_neto: totalAcumulado,
            movimientos: movimientos || []
        });

    } catch (error) {
        console.log(" Error crítico en ahorros/status:", error.message);
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/ahorros/movimiento', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { monto, tipo, nota } = req.body;

    console.log("--- 💾 REGISTRANDO MOVIMIENTO DE AHORRO ---");
    console.log("Monto:", monto, "| Tipo recibido:", tipo, "| Nota:", nota);

    if (!monto || parseFloat(monto) <= 0) {
        return res.status(400).json({ error: "El monto debe ser mayor a 0" });
    }

    try {
        const tipoEstandarizado = (tipo || 'INGRESO').trim().toUpperCase();

        const { data, error } = await supabase
            .from('Ahorros')
            .insert([{
                id_usuario: usuarioId,
                monto: parseFloat(monto),
                tipo: tipoEstandarizado,
                nota: nota || '',
                fecha: new Date().toISOString()
            }])
            .select()
            .single();

        if (error) {
            console.log(" Error insertando ahorro:", error.message);
            return res.status(400).json({ error: error.message });
        }

        console.log(" Movimiento de ahorro registrado:", data);
        return res.status(201).json({
            mensaje: "Movimiento de ahorro registrado con éxito",
            ahorro: data
        });

    } catch (error) {
        console.log("🚨 Error crítico en ahorros/movimiento:", error.message);
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// GRÁFICAS
// ─────────────────────────────────────────
app.get('/api/grafica', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;

    try {
        const { data: gastos, error } = await supabase
            .from('Gastos')
            .select('total_gastado, categoria')
            .eq('id_usuario', usuarioId);

        if (error) return res.status(400).json({ error: error.message });

        const gastosTotales = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        const analisisCategorias = {};
        gastos.forEach(g => {
            const cat = g.categoria || 'Otros';
            const monto = parseFloat(g.total_gastado || 0);
            analisisCategorias[cat] = (analisisCategorias[cat] || 0) + monto;
        });

        const dataGrafica = Object.keys(analisisCategorias).map(cat => {
            const totalCategoria = analisisCategorias[cat];
            const porcentaje = gastosTotales > 0 ? (totalCategoria / gastosTotales) * 100 : 0;
            return {
                categoria: cat,
                total_gastado: totalCategoria,
                percentage: parseFloat(porcentaje.toFixed(2))
            };
        });

        return res.status(200).json({
            gastos_totales_general: gastosTotales,
            distribucion: dataGrafica
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/analytics/gastos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;

    try {
        const { data: gastos, error } = await supabase
            .from('Gastos')
            .select('total_gastado, categoria')
            .eq('id_usuario', usuarioId);

        if (error) return res.status(400).json({ error: error.message });

        const total = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        const agrupado = {};
        gastos.forEach(g => {
            const cat = g.categoria || 'Otros';
            agrupado[cat] = (agrupado[cat] || 0) + parseFloat(g.total_gastado || 0);
        });

        const categorias = Object.keys(agrupado).map(cat => ({
            categoria: cat,
            total: agrupado[cat],
            porcentaje: total > 0 ? parseFloat(((agrupado[cat] / total) * 100).toFixed(2)) : 0
        }));

        return res.status(200).json({ total, categorias });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// START
// ─────────────────────────────────────────
app.listen(PORT, () => {
    console.log(`Servidor activo y blindado corriendo en el puerto ${PORT}`);
});